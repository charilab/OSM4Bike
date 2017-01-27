package jp.ne.sakura.charilab.osm4bike;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import jp.ne.sakura.charilab.maplibs.BaseMap.BaseMapFactory;
import jp.ne.sakura.charilab.maplibs.BaseMap.BaseMapLayer;
import jp.ne.sakura.charilab.maplibs.Overlays.Overlay;
import jp.ne.sakura.charilab.maplibs.Overlays.OverlayFactory;
import jp.ne.sakura.charilab.maplibs.Overlays.currentMarker;
import jp.ne.sakura.charilab.maplibs.POIcollector.POIcollector;
import jp.ne.sakura.charilab.maplibs.trackLogger.trackLogger;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.input.MapZoomControls;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.renderer.MapWorkerPool;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.scalebar.MetricUnitAdapter;

import java.util.Map;

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
*//**
 * Created by uno on 16/05/12.
 */
public class MapFragment extends Fragment {
    private MapView mapView;
    private BaseMapLayer mapLayer=null;
    private Context ctx;
    public trackLogger.binder logger;
    public Config config;
    private boolean currentLoc = false;
    private currentMarker currentMarker;
    private OverlayFactory overlays;
    private POIcollector poicollector;

    public MapFragment() {
        Log.d("DEBUG", "MapFragment is created.");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
        MainActivity activity = (MainActivity) getActivity();
        logger = activity.logger;
        config = activity.config;
        poicollector = activity.poicollector;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AndroidGraphicFactory.createInstance(getActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("DEBUG", "MapFragment.onCreateView is called.");
        View rootView = inflater.inflate(R.layout.mapviewer, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);

        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setZoomLevelMin((byte) 10);
        mapView.setZoomLevelMax((byte) 18);
        AndroidUtil.setMapScaleBar(mapView, MetricUnitAdapter.INSTANCE, null);

        mapView.getMapZoomControls().setZoomControlsOrientation(MapZoomControls.Orientation.VERTICAL_IN_OUT);
        mapView.getMapZoomControls().setZoomInResource(R.drawable.zoom_control_in);
        mapView.getMapZoomControls().setZoomOutResource(R.drawable.zoom_control_out);
/*
        mapView.getMapZoomControls().setMarginHorizontal(getResources().getDimensionPixelOffset(R.dimen.controls_margin));
        mapView.getMapZoomControls().setMarginVertical(getResources().getDimensionPixelOffset(R.dimen.controls_margin));
*/
        MapWorkerPool.NUMBER_OF_THREADS = MapWorkerPool.DEFAULT_NUMBER_OF_THREADS;
        MapWorkerPool.DEBUG_TIMING = false;
        MapFile.wayFilterEnabled = true;
        MapFile.wayFilterDistance = 20;

        // only once a layer is associated with a mapView the rendering starts
        LatLong defaultLoc = new LatLong(35.126847, 138.909589);
        //LatLong defaultLoc = new LatLong(35.102326, 138.866308);
        mapLayer = BaseMapFactory.createLayer(ctx, mapView, 0);
        mapView.getLayerManager().getLayers().add(mapLayer);
        mapView.setCenter(defaultLoc);
        mapView.setZoomLevel((byte) 15);

        currentMarker = new currentMarker(ctx, mapView);
        mapView.getLayerManager().getLayers().add(currentMarker);
        currentLoc = true;

        overlays = new OverlayFactory(ctx, mapView);
        for(Map.Entry ov_status: config.overlayStatus.entrySet()) {
            if (overlays.containsKey(ov_status.getKey())) {
                Overlay overlay = overlays.get(ov_status.getKey());
                if ((boolean) ov_status.getValue()) {
                    Log.d("mapFragment", "add overlay " + ov_status.getKey());
                    overlay.show();
                } else {
                    Log.d("mapFragment", "remove overlay " + ov_status.getKey());
                    overlay.hide();
                }
                overlay.addTo(mapView);
            }
        }

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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapLayer.onResume();
        overlays.get("heatmap").onResume();
        Log.d("Main:onResume()", "called.");

        Button btnMain = (Button) getActivity().findViewById(R.id.btnMain);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                poicollector.registerMenu(getActivity(), logger.getCurrentStatistics().location);
                redrawOverlay();
            }
        });

        ImageButton btnLoc = (ImageButton) getActivity().findViewById(R.id.btnLoc);
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ctx, "Enable current Location.", Toast.LENGTH_SHORT).show();
                setCurrentLocation(true);
                redrawOverlay();
            }
        });

        ImageButton btnMenu = (ImageButton) getActivity().findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                poicollector.exportMenu(getActivity());
                redrawOverlay();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mapLayer.onPause();
        overlays.get("heatmap").onPause();
        Log.d("Main:onPause()", "called.");
    }

    @Override
    public void onDestroy() {
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    public void zoomIn() {
        mapView.getModel().mapViewPosition.zoomIn();
    }

    public void zoomOut() {
        mapView.getModel().mapViewPosition.zoomOut();
    }

    public void redrawOverlay() {
        for(Map.Entry ov_status: config.overlayStatus.entrySet()) {
            if (overlays.containsKey(ov_status.getKey())) {
                Overlay overlay = overlays.get(ov_status.getKey());
                if ((boolean) ov_status.getValue()) {
                    Log.d("mapFragment", "add overlay " + ov_status.getKey());
                    overlay.show();
                } else {
                    Log.d("mapFragment", "remove overlay " + ov_status.getKey());
                    overlay.hide();
                }
            }
        }
    }

    public void setCurrentLocation(boolean enable) {
        if (enable && (!currentLoc)) {
            currentLoc = true;
        } if ((!enable) && currentLoc) {
            currentLoc = false;
        }
    }
}
