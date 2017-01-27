package jp.ne.sakura.charilab.mapviewer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.renderer.MapWorkerPool;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.scalebar.MetricUnitAdapter;

import java.util.Map;

import jp.ne.sakura.charilab.maplibs.BaseMap.BaseMapFactory;
import jp.ne.sakura.charilab.maplibs.GeoDatabase.OSMDatabaseHelper;
import jp.ne.sakura.charilab.maplibs.Overlays.Overlay;
import jp.ne.sakura.charilab.maplibs.Overlays.OverlayFactory;
import jp.ne.sakura.charilab.maplibs.Overlays.currentMarker;
import jp.ne.sakura.charilab.maplibs.trackLogger.trackLogger;
import jp.ne.sakura.charilab.ui.ListDialog;
import jp.ne.sakura.charilab.ui.menuItem;

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

/*
 MapViewer
 */
public class MainActivity extends AppCompatActivity {
    public trackLogger.binder logger=null;
    private MapView mapView;
    private TileDownloadLayer mapLayer;
    private boolean currentLoc = false;
    private currentMarker currentMarker;
    private OverlayFactory overlays;
    private Config config;

    private void setCurrentLocation(boolean enable) {
        if (enable && (!currentLoc)) {
            currentLoc = true;
        } if ((!enable) && currentLoc) {
            currentLoc = false;
        }
    }

    private void toggleHeatmap() {
        if (config.overlayStatus.get("heatmap")) {
            config.overlayStatus.put("heatmap", false);
            overlays.get("heatmap").hide();
            Toast.makeText(getApplicationContext(),
                    "Disable Strava heatmap.", Toast.LENGTH_SHORT).show();
        } else {
            config.overlayStatus.put("heatmap", true);
            overlays.get("heatmap").show();
            Toast.makeText(getApplicationContext(),
                    "Enable Strava heatmap.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMapview() {
        MapWorkerPool.NUMBER_OF_THREADS = MapWorkerPool.DEFAULT_NUMBER_OF_THREADS;
        MapWorkerPool.DEBUG_TIMING = false;
        MapFile.wayFilterEnabled = true;
        MapFile.wayFilterDistance = 20;

        // create map view
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setZoomLevelMin((byte) 10);
        mapView.setZoomLevelMax((byte) 18);
        AndroidUtil.setMapScaleBar(mapView, MetricUnitAdapter.INSTANCE, null);

        // add default map layer
        LatLong defaultLoc = new LatLong(35.126847, 138.909589);
        mapLayer = BaseMapFactory.createLayer(getBaseContext(), mapView, 0);
        mapView.getLayerManager().getLayers().add(mapLayer);
        mapView.setCenter(defaultLoc);
        mapView.setZoomLevel((byte) 15);

        // Show default overlays
        overlays = new OverlayFactory(getBaseContext(), mapView);
        for(Map.Entry ov_status: config.overlayStatus.entrySet()) {
            if (overlays.containsKey(ov_status.getKey())) {
                Overlay overlay = overlays.get(ov_status.getKey());
                if ((boolean) ov_status.getValue()) {
                    overlay.show();
                } else {
                    overlay.hide();
                }
                overlay.addTo(mapView);
            }
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger = (trackLogger.binder) service;
            currentLoc = true;

            // Show current Marker
            currentMarker = new currentMarker(getBaseContext(), mapView);
            mapView.getLayerManager().getLayers().add(currentMarker);

            if (logger != null) {
                logger.addListener(new trackLogger.listener() {
                    public void onLocationChanged(trackLogger.statistics stat) {
                        if (currentLoc) {
                            LatLong center = new LatLong(stat.location.getLatitude(), stat.location.getLongitude());
                            mapView.setCenter(center);
                        }
                        currentMarker.update_location(stat.location);
                    }
                });
            }

            // create buttons
            ImageButton btnLoc = (ImageButton) findViewById(R.id.location);
            btnLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),
                            "Enable current Location", Toast.LENGTH_SHORT).show();
                    setCurrentLocation(true);
                }
            });

            ImageButton btnMenu = (ImageButton) findViewById(R.id.menu);
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuItem[] items = new menuItem[2];
                    items[0] = new menuItem("HeatMap", null);
                    items[1] = new menuItem("Download", null);
                    ListDialog.makeDialog(getBaseContext(), null, items, new ListDialog.eventListeners() {
                        public void onSelected(int id) {
                            switch (id) {
                                case 0:
                                    toggleHeatmap();
                                    return;

                                case 1:
                                    OSMDatabaseHelper helper = new OSMDatabaseHelper(getBaseContext(), "poi.db");
                                    helper.downloadDatabase();
                                    return;
                            }
                        }

                        public void onCancel() {
                            Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
                        }
                    }).show(getFragmentManager(), "Menu");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = new Config();

        AndroidGraphicFactory.createInstance(this.getApplication());
        setContentView(R.layout.activity_main);
        createMapview();

        Intent intent = new Intent(this, trackLogger.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapLayer.onResume();
        overlays.get("heatmap").onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        overlays.get("heatmap").onPause();
        mapLayer.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BUTTON_X:
                        Toast.makeText(getApplicationContext(),
                                "Enable current Location", Toast.LENGTH_SHORT).show();
                        setCurrentLocation(true);
                        return true;

                    case KeyEvent.KEYCODE_BUTTON_Y:
                        mapView.getModel().mapViewPosition.zoomIn();
                        return true;

                    case KeyEvent.KEYCODE_BUTTON_A:
                        mapView.getModel().mapViewPosition.zoomOut();
                        return true;

                    case KeyEvent.KEYCODE_BUTTON_B:
                        toggleHeatmap();
                        return true;

                    default:
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
