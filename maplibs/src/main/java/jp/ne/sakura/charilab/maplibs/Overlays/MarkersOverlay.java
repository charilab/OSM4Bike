package jp.ne.sakura.charilab.maplibs.Overlays;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.util.Log;

import jp.ne.sakura.charilab.geoutils.geoMesh;
import jp.ne.sakura.charilab.maplibs.GeoDatabase.OSMDatabaseHelper;
import jp.ne.sakura.charilab.maplibs.R;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Marker;

import java.util.ArrayList;
import java.util.Locale;

/*
MIT License

Copyright (c) 2017 Shunji Uno <uno@charilab.sakura.ne.jp>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

public class MarkersOverlay extends Overlay {
    static private SQLiteDatabase db;
    static private Bitmap[] iconBitmap;
    static private int iconList[] = {
            R.drawable.conv_711_20, R.drawable.conv_lawson_20,
            R.drawable.conv_famima_20, R.drawable.conv_ministop_20,
            R.drawable.conv_circlek_20, R.drawable.conv_daily_20,
            R.drawable.conv_poplar_20, R.drawable.conv_threef_20,
            R.drawable.conv_sunkus_20, R.drawable.conv_coco_20,
            R.drawable.conv_seico_20, R.drawable.conv_others_20,
            R.drawable.traffic_lights_20, R.drawable.toilets_16,
            R.drawable.michinoeki_16, R.drawable.pass_16,
            R.drawable.viewpoint_16, R.drawable.bicycle_16,
            R.drawable.spa_16, R.drawable.star_16
    };
    private int[] categories;
    private ArrayList<Marker> markers;
    private MapView map;
    private Context ctx;
    private geoMesh current_mesh;

    private void prepareIconBitmap(Context ctx) {
        iconBitmap = new Bitmap[iconList.length];
        for(int i=0; i<iconBitmap.length; i++) {
            Drawable icon = ctx.getResources().getDrawable(iconList[i]);
            iconBitmap[i] = AndroidGraphicFactory.convertToBitmap(icon);
        }
    }

    public MarkersOverlay(Context context, MapView mapview, int[] cat) {
        super();
        setLayer(new Layer() {
            @Override
            public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
                //Log.d("DEBUG1", "draw "+isVisible()+" called for category "+categories[0]);
                if (isVisible()) {
                    LatLong center = boundingBox.getCenterPoint();
                    if (!current_mesh.nearby(center.getLatitude(), center.getLongitude())) {
                        reload(center);
                    }
                } else {
                    for (Marker marker: markers) {
                        map.getLayerManager().getLayers().remove(marker);
                    }
                }
                return;
            }

            @Override
            public boolean onLongPress(LatLong tapLatLong, Point layerXY, Point tapXY) {
                reload(tapLatLong);
                return true;
            }
        });
        categories = cat;
        markers = new ArrayList();
        map = mapview;
        ctx = context;
        current_mesh = new geoMesh(0,0,0,2);

        if (db == null) {
            OSMDatabaseHelper helper = new OSMDatabaseHelper(ctx, "poi.db");
            //helper.downloadDatabase();
            db = helper.getReadableDatabase();
        }
        if (iconBitmap == null) {
            prepareIconBitmap(ctx);
        }
    }

    private Cursor query_database(geoMesh mesh) {
        String[] column = new String[] {"category", "lon", "lat", "alt", "icon", "name", "desc", "flags"};
        double area[] = mesh.toLatLngWithNeibours();
        String s1 = String.format(Locale.US, "(%f < lat) and (lat < %f) and (%f < lon) and (lon < %f)",
                area[0], area[2], area[1], area[3]);
        String s2 = String.format(Locale.US, "(category = %d)", categories[0]);
        for(int i=1; i<categories.length; i++) {
            s2 = String.format(Locale.US, "%s or (category = %d)", s2, categories[i]);
        }
        String select = String.format(Locale.US, "(%s) and (%s)", s1, s2);
        Log.d("Overlay", "Select "+select);
        try {
            Cursor res = db.query(false, "poi", column, select, null, null, null, null, null);
            return res;
        } catch (SQLiteException e) {
            Log.d("Overlay", "db query error (may be Database not found.");
            return null;
        }
    }

    private void reload(LatLong center) {
        //Log.d("DEBUG1", "reload called for category "+categories[0]);
        for (Marker marker: markers) {
            map.getLayerManager().getLayers().remove(marker);
        }

        geoMesh mesh = new geoMesh(center.getLatitude(), center.getLongitude(), 2);

        Cursor res = query_database(mesh);
        if (res != null) {
            if (res.getCount() > 500) {
                Log.d("Overlay", "Too many POIs("+res.getCount()+"). Re-query.");
                mesh = new geoMesh(center.getLatitude(), center.getLongitude(), 3);
                res = query_database(mesh);
            }

            boolean mov = res.moveToFirst();
            while (mov) {
                LatLong loc = new LatLong(res.getFloat(2), res.getFloat(1));
                Marker marker = new Marker(loc, iconBitmap[res.getInt(4)], 0, 0);
                markers.add(marker);
                map.getLayerManager().getLayers().add(marker);
                mov = res.moveToNext();
            }
            res.close();
        }
        current_mesh = mesh;
    }

    @Override
    public void show() {
        reload(map.getBoundingBox().getCenterPoint());
        layer.setVisible(true, true);
    }

    @Override
    public void hide() {
        for (Marker marker: markers) {
            map.getLayerManager().getLayers().remove(marker);
        }
        layer.setVisible(false, false);
    }
}
