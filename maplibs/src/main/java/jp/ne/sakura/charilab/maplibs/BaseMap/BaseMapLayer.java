package jp.ne.sakura.charilab.maplibs.BaseMap;

import android.util.Log;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.TilePosition;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.DownloadJob;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.TileSource;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.util.LayerUtil;

import java.util.List;

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
 * Currently, BaseMapLayer is just a wrapper of TileDownloadLayer for debug.
 */
public class BaseMapLayer extends TileDownloadLayer {
    TileCache tileCache;
    private boolean debugFlag=false;

    public BaseMapLayer(TileCache tileCache, MapViewPosition mapViewPosition, TileSource tileSource,
                        GraphicFactory graphicFactory) {
        super(tileCache, mapViewPosition, tileSource, graphicFactory);
        this.tileCache = tileCache;
    }

    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        if (debugFlag) {
            List<TilePosition> tilePositions = LayerUtil.getTilePositions(boundingBox, zoomLevel, topLeftPoint,

                    this.displayModel.getTileSize());

            Log.d("draw()", "called.");
            for (TilePosition tilePosition : tilePositions) {
                Tile tile = tilePosition.tile;
                Log.d("draw()", "" + tile.tileX + ", " + tile.tileY + ", " + tile.zoomLevel);
            }
        }
        super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
    }

    @Override
    public void start() {
        if (debugFlag) {
            Log.d("start()", "called.");
        }
        super.start();
    }

    @Override
    public void onPause() {
        if (debugFlag) {
            Log.d("onPause()", "called.");
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (debugFlag) {
            Log.d("onResume()", "called.");
        }
        super.onResume();
    }

    @Override
    protected DownloadJob createJob(Tile tile) {
        DownloadJob job = super.createJob(tile);
        if (debugFlag) {
            if (this.tileCache.containsKey(job)) {
                Log.d("createJob()", "containsKey is true.");
            } else {
                Log.d("createJob()", "containsKey is false.");
            }
        }
        return job;
    }

    @Override
    public synchronized void setDisplayModel(DisplayModel displayModel) {
        if (debugFlag) {
            if (displayModel != null) {
                Log.d("setDisplayModel()", "called with displayModel.");
            } else {
                Log.d("setDisplayModel()", "called without displayModel.");
            }
        }
        super.setDisplayModel(displayModel);
    }

    public void setDebug(boolean flag) {
        debugFlag = flag;
    }
}