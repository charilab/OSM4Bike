package jp.ne.sakura.charilab.maplibs.POIcollector;

import jp.ne.sakura.charilab.maplibs.R;

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
public class OSMnamingTable {
    private OSMnamingElem[] table;

    private void init(int brandId, String brandName, String name, String name_en,
                 String name_ja, String name_ja_rm, int rid) {
        table[brandId] = new OSMnamingElem(brandId, brandName, name, name_en, name_ja, name_ja_rm, rid);
    }

    public OSMnamingTable() {
        table = new OSMnamingElem[12];
        init(0, "セブン-イレブン", "セブン-イレブン", "Seven-Eleven", "セブン-イレブン", "Sebun Irebun", R.drawable.conv_711);
        init(1, "LAWSON", "ローソン", "LAWSON", "ローソン", "Rōson", R.drawable.conv_lawson);
        init(2, "FamilyMart", "ファミリーマート", "FamilyMart", "ファミリーマート", "Famirī Māto", R.drawable.conv_famima);
        init(3, "MINISTOP", "ミニストップ", "MINISTOP", "ミニストップ", "Mini Stoppu", R.drawable.conv_ministop);
        init(4, "サークルK", "サークルK", "Circle K", "サークルK", "Sākuru-Kei", R.drawable.conv_circlek);
        init(5, "Daily YAMAZAKI", "デイリーヤマザキ", "Daily YAMAZAKI", "デイリーヤマザキ", "Deirī Yamazaki", R.drawable.conv_daily);
        init(6, "ポプラ", "ポプラ", "POPLAR", "ポプラ", "Popura", R.drawable.conv_poplar);
        init(7, "スリーエフ", "スリーエフ", "Three-F", "スリーエフ", "Surīefu", R.drawable.conv_threef);
        init(8, "sunkus サンクス", "サンクス", "sunkus", "サンクス", "sunkusu", R.drawable.conv_sunkus) ;
        init(9, "Coco!", "ココストア", "Coco Store", "ココストア", "KoKo Sutoa", R.drawable.conv_coco);
        init(10, "Seicomart", "セイコーマート", "Seicomart", "セイコーマート", "Seikōmāto", R.drawable.conv_seico);
        init(11, "コンビニ", null, null, null, null, R.drawable.conv_others);
    }

    public OSMnamingElem get(int bid) {
        return table[bid];
    }

    public int size() {
        return table.length;
    }
}