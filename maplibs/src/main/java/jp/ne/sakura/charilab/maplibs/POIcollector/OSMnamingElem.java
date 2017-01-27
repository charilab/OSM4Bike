package jp.ne.sakura.charilab.maplibs.POIcollector;

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
 * Created by uno on 15/12/28.
 */
public class OSMnamingElem {
    public int brandId;
    public String brandName;
    public String name;
    public String name_en;
    public String name_ja;
    public String name_ja_rm;
    int rscIcon;

    public OSMnamingElem(int brandId, String brandName, String name, String name_en,
                         String name_ja, String name_ja_rm, int rscId) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.name = name;
        this.name_en = name_en;
        this.name_ja = name_ja;
        this.name_ja_rm = name_ja_rm;
        this.rscIcon = rscId;
    }
}

