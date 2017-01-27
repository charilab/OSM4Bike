package jp.ne.sakura.charilab.maplibs.Overlays;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;

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

public class currentMarker extends Layer {
    private MapView map;
    private Marker marker=null;
    private Location curLocation=null;
    private Context ctx;
    private boolean redraw;

    private Bitmap createDirectionIcon(float azimuth) {
        android.graphics.Bitmap orgBitmap = BitmapFactory.decodeResource(ctx.getResources(),
                R.drawable.direction);
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f, 1.0f);
        matrix.postRotate(azimuth);
        android.graphics.Bitmap newBitmap = android.graphics.Bitmap.createBitmap(orgBitmap, 0, 0,
                orgBitmap.getWidth(), orgBitmap.getHeight(), matrix, true);
        return AndroidGraphicFactory.convertToBitmap(new BitmapDrawable(newBitmap));
    }

    public currentMarker(Context context, MapView mapView) {
        map = mapView;
        ctx = context;
    }

    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        if ((curLocation == null)||(!redraw)) {
            return;
        }

        if (marker != null) {
            map.getLayerManager().getLayers().remove(marker);
        }
        if (boundingBox.contains(curLocation.getLatitude(), curLocation.getLongitude())) {
            LatLong loc = new LatLong(curLocation.getLatitude(), curLocation.getLongitude());
            marker = new Marker(loc, createDirectionIcon(curLocation.getBearing()), 0, 0);
            map.getLayerManager().getLayers().add(marker);
        }
        redraw = false;
        return;
    }

    public synchronized void update_location(Location loc) {
        if (curLocation != loc) {
            curLocation = loc;
            redraw = true;
        }
    }
}