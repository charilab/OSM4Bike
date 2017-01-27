package jp.ne.sakura.charilab.maplibs.Overlays;

import android.content.Context;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;

import java.util.HashMap;

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
 * Created by uno on 16/06/03.
 */
public class OverlayFactory extends HashMap<String,Overlay> {
    public OverlayFactory(Context ctx, MapView map) {
        super();
        this.put("collection", new POIcollectorOverlay(ctx, map));
        this.put("convenience", new MarkersOverlay(ctx, map, new int[]{1}));
        this.put("signals", new MarkersOverlay(ctx, map, new int[]{2}));
        this.put("toilets", new MarkersOverlay(ctx, map, new int[]{3,4}));
        this.put("mountain pass", new MarkersOverlay(ctx, map, new int[]{5,6}));
        this.put("misc", new MarkersOverlay(ctx, map, new int[]{7,8}));
        this.put("heatmap", new TileOverlay(ctx, map, 0));
        //this.put("rainmap", new TileOverlay(ctx, map, 2));
        //this.put("route", new RouteOverlay(ctx, map, route));
    }

}
