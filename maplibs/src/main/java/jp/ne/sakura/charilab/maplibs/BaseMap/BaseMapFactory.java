package jp.ne.sakura.charilab.maplibs.BaseMap;

import android.content.Context;
import android.util.Log;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;

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
 * Collection of base map layer
 */
public class BaseMapFactory {
    private static final String MAP_FILE = "5238.map";

    public BaseMapFactory() {
    }

    static public BaseMapLayer createLayer(Context ctx, MapView mapView, int id) {
        BaseMapLayer mapLayer=null;
        TileCache tileCache;
        switch (id) {
            case 0: // charilog city
                tileCache = AndroidUtil.createTileCache(ctx, "charilab",
                        mapView.getModel().displayModel.getTileSize(), 10f,
                        mapView.getModel().frameBufferModel.getOverdrawFactor(), true);
                mapView.getModel().displayModel.setFixedTileSize(256);
                OnlineTileSource onlineTileSource = new OnlineTileSource(new String[]{
                        "tile.charilog.net"}, 80);
                onlineTileSource.setName("Charilog").setAlpha(false)
                        .setBaseUrl("/city/").setExtension("png")
                        .setParallelRequestsLimit(8).setProtocol("http").setTileSize(256)
                        .setZoomLevelMax((byte) 18).setZoomLevelMin((byte) 10);
                onlineTileSource.setUserAgent("OSM4bike");
                mapLayer = new BaseMapLayer(tileCache,
                        mapView.getModel().mapViewPosition, onlineTileSource,
                        AndroidGraphicFactory.INSTANCE);
                break;

            case 1: // OpenStreetMap
                tileCache = AndroidUtil.createTileCache(ctx, "osm",
                        mapView.getModel().displayModel.getTileSize(), 1f,
                        mapView.getModel().frameBufferModel.getOverdrawFactor(), true);
                mapView.getModel().displayModel.setFixedTileSize(256);
                mapLayer = new BaseMapLayer(tileCache,
                        mapView.getModel().mapViewPosition, OpenStreetMapMapnik.INSTANCE,
                        AndroidGraphicFactory.INSTANCE);
                break;
        }
        return mapLayer;
    }
}
