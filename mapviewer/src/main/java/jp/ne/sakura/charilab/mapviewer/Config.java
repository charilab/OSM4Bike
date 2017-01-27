package jp.ne.sakura.charilab.mapviewer;

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
 * Define default configuration
 */
public class Config {
    public HashMap<String, Boolean> overlayStatus;

    public Config() {
        overlayStatus = new HashMap<>();
        overlayStatus.put("heatmap", false);
        overlayStatus.put("rainmap", false);
        overlayStatus.put("convenience", true);
        overlayStatus.put("signals", false);
        overlayStatus.put("toilets", true);
        overlayStatus.put("mountain pass", true);
        overlayStatus.put("misc", false);
        overlayStatus.put("route", false);
        overlayStatus.put("collection", false);
    }
}
