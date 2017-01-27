package jp.ne.sakura.charilab.maplibs.Overlays;

import android.content.Context;

import jp.ne.sakura.charilab.maplibs.BaseMap.BaseMapLayer;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;

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
/**
 * Created by uno on 16/07/30.
 */
public class TileOverlay extends Overlay {
    public TileOverlay(Context context, MapView mapview, int id) {
        super();
        TileCache tileCache = AndroidUtil.createTileCache(context, "heatmap",
                mapview.getModel().displayModel.getTileSize(), 10f,
                mapview.getModel().frameBufferModel.getOverdrawFactor(), true);
        mapview.getModel().displayModel.setFixedTileSize(256);
        OnlineTileSource heatmapTileSource = new OnlineTileSource(new String[]{
                "globalheat.strava.com"}, 80);
        heatmapTileSource.setName("Heatmap").setAlpha(true)
                .setBaseUrl("/tiles/cycling/color1/").setExtension("png")
                .setParallelRequestsLimit(1).setProtocol("http").setTileSize(256)
                .setZoomLevelMax((byte) 17).setZoomLevelMin((byte) 10);
        heatmapTileSource.setUserAgent("OSM4bike");
        //setLayer(new TileDownloadLayer(tileCache,
        //mapview.getModel().mapViewPosition, heatmapTileSource,
        //        AndroidGraphicFactory.INSTANCE));
        BaseMapLayer layer = new BaseMapLayer(tileCache,
                mapview.getModel().mapViewPosition, heatmapTileSource,
                AndroidGraphicFactory.INSTANCE);
        //layer.setDebug(true);
        setLayer(layer);
    }
}
