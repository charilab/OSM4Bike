package jp.ne.sakura.charilab.maplibs.Overlays;

import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.download.TileDownloadLayer;

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
 * Created by uno on 16/06/05.
 */
public class Overlay {
    protected Layer layer;

    public Overlay() {
        layer = null;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer l) {
        layer = l;
    }

    public void addTo(MapView mapView) {
        mapView.getLayerManager().getLayers().add(layer);
    }

    public void show() {
        layer.setVisible(true, true);
    }

    public void hide() {
        layer.setVisible(false, false);
    }

    public void onResume() {
        TileDownloadLayer tileLayer = (TileDownloadLayer)layer;
        tileLayer.onResume();
    }

    public void onPause() {
        TileDownloadLayer tileLayer = (TileDownloadLayer)layer;
        tileLayer.onResume();
    }
}