package jp.ne.sakura.charilab.maplibs.POIcollector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import jp.ne.sakura.charilab.ui.GridDialog;
import jp.ne.sakura.charilab.ui.TextDialog;
import jp.ne.sakura.charilab.ui.menuItem;

import java.util.Locale;

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
 * Created by uno on 12/17/15.
 */
public class conveniencePOIhandler extends POIhandler {
    private static menuItem[] items;
    final private static OSMnamingTable shopInfo = new OSMnamingTable();
    private int brandId;
    private String brandName = "";
    private String branch = "";

    public conveniencePOIhandler(Context ctx, OSMnodeDB db) {
        super(ctx, db);
        name = "コンビニ";

        items = new menuItem[12];
        for (int i=0; i<shopInfo.size(); i++) {
            items[i] = new menuItem(shopInfo.get(i).name, shopInfo.get(i).rscIcon);
        }
    }

    private void moreInfo() {
        TextDialog.makeDialog(context, brandName+" - 詳細", new TextDialog.eventListeners() {
            public void onDone(String text) {
                branch = text;
                insertData();
                Toast.makeText(context, brandName+" is selected.", Toast.LENGTH_SHORT).show();
            }
            public void onCancel() {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }).show(((Activity) context).getFragmentManager(), "detail");
    }

    @Override
    public void onSelected(Location loc) {
        location = loc;
        GridDialog.makeDialog(context, "コンビニ選択", items, new GridDialog.eventListeners() {
            public void onSelected(int i) {
                brandId = i;
                brandName = shopInfo.get(i).name;
                moreInfo();
            }
            public void onCancel() {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }).show(((Activity) context).getFragmentManager(), "shoplist");
    }

    @Override
    protected void insertData() {
        SQLiteDatabase db = PoiDB.getWritableDatabase();

        ContentValues insertValues = new ContentValues();
        insertValues.put("category", CAT_CONVENIENCE);
        insertValues.put("lat", location.getLatitude());
        insertValues.put("lon", location.getLongitude());
        insertValues.put("alt", location.getAltitude());
        insertValues.put("icon", brandId);
        insertValues.put("name", brandName);
        insertValues.put("desc", branch);
        db.insert(TABLE_NAME, null, insertValues);

        String msg = String.format(Locale.US, "insert POI to %s : lon=%f, lat=%f, name=%s, brand=%s, branch=%s\n",
                TABLE_NAME, location.getLongitude(), location.getLatitude(), name, brandName, branch);
        Log.d("DEBUG", msg);
    }

    @Override
    protected String dumpAsGPX(SQLiteDatabase db) {
        String buf = "";
        String[] column = new String[] {"lat", "lon", "name", "desc", "icon"};
        String select = String.format(Locale.US, "(category = %d)", CAT_CONVENIENCE);
        Cursor res = db.query(false, TABLE_NAME, column, select, null, null, null, null, null);

        boolean mov = res.moveToFirst();
        while (mov) {
            buf += String.format(Locale.US, "  <wpt lat=\"%f\" lon=\"%f\">\n" +
                            "    <name>%s</name>\n" +
                            "    <desc>%s</desc>\n" +
                            "    <cmt>%s</cmt>\n" +
                            "  </wpt>\n",
                    res.getFloat(0), res.getFloat(1), res.getString(2), res.getString(3), "");
            mov = res.moveToNext();
        }

        res.close();
        return buf;
    }

    @Override
    protected String dumpAsOSM(SQLiteDatabase db) {
        String buf = "";
        int seq=-1;
        String[] column = new String[] {"lat", "lon", "name", "desc", "icon"};
        String select = String.format(Locale.US, "(category = %d)", CAT_CONVENIENCE);
        Cursor res = db.query(false, TABLE_NAME, column, select, null, null, null, null, null);

        boolean mov = res.moveToFirst();
        while (mov) {
            OSMnamingElem info = shopInfo.get(res.getInt(4));

            String branchTag = "";
            if (!res.getString(3).equals("")) {
                branchTag = String.format(Locale.US, "<tag k=\"branch\" v=\"%s\"/>\n", res.getString(3));
            }
            buf += String.format(Locale.US, "<node id=\"%d\" lat=\"%f\" lon=\"%f\" visible=\"true\">\n" +
                "  <tag k=\"shop\" v=\"convenience\"/>\n" +
                "  <tag k=\"name\" v=\"%s\"/>\n" +
                "  <tag k=\"name:en\" v=\"%s\"/>\n" +
                "  <tag k=\"name:ja\" v=\"%s\"/>\n" +
                "  <tag k=\"name:ja_rm\" v=\"%s\"/>\n" +
                "  <tag k=\"brand\" v=\"%s\"/>\n" + branchTag +
                "</node>\n",
                    seq, res.getFloat(0), res.getFloat(1),
                    info.name, info.name_en, info.name_ja, info.name_ja_rm, info.brandName);
            mov = res.moveToNext();
            seq--;
        }

        res.close();
        return buf;
    }

}