package jp.ne.sakura.charilab.maplibs.POIcollector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import jp.ne.sakura.charilab.ui.TextDialog;

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
 * POI handler for toilets
 */
public class toiletsPOIhandler extends POIhandler {
    private String name_ja = "";
    private int wheelchair = 0;
    String[] items = {"不明", "あり", "なし"};

    public toiletsPOIhandler(Context ctx, OSMnodeDB db) {
        super(ctx, db);
        name = "トイレ";
    }

    @Override
    public void onSelected(Location loc) {
        location = loc;
        // show dialog
        TextDialog.makeDialog(context, "詳細", new TextDialog.eventListeners() {
            public void onDone(String text) {
                name_ja = text;
                insertData();
                Toast.makeText(context, "POI has been Registered.", Toast.LENGTH_SHORT).show();
            }
            public void onCancel() {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }).show(((Activity) context).getFragmentManager(), "detail");

        /* Support checkbox dialog in future
        myCheckBoxDialog.makeDialog(context, "車椅子(wheelchair)", new myCheckBoxDialog.eventListeners() {
            public void onDone(String text) {
                wheelchair = i;
                insertData();
                Toast.makeText(context, "POI has been Registered.", Toast.LENGTH_SHORT).show();
            }
            public void onCancel() {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }).show(((Activity) context).getFragmentManager(), "detail");
        */
    }

    @Override
    protected void insertData() {
        SQLiteDatabase db = PoiDB.getWritableDatabase();

        ContentValues insertValues = new ContentValues();
        insertValues.put("category", CAT_TOILETS);
        insertValues.put("lat", location.getLatitude());
        insertValues.put("lon", location.getLongitude());
        insertValues.put("alt", location.getAltitude());
        insertValues.put("icon", 13); // Should be fixed
        insertValues.put("name", name_ja);
        insertValues.put("flags", wheelchair);
        db.insert(TABLE_NAME, null, insertValues);

        String msg = String.format(Locale.US, "insert POI to %s : lon=%f, lat=%f, alt=%f, kind=%s, name=%s, wheelchair=%d\n",
                TABLE_NAME, location.getLongitude(), location.getLatitude(), location.getAltitude(),
                name, name_ja, wheelchair);
        Log.d("DEBUG", msg);
    }

    @Override
    protected String dumpAsGPX(SQLiteDatabase db) {
        String buf = "";
        String[] column = new String[] {"lat", "lon", "name", "flags"};
        String select = String.format(Locale.US, "(category = %d)", CAT_TOILETS);
        Cursor res = db.query(false, TABLE_NAME, column, select, null, null, null, null, null);

        boolean mov = res.moveToFirst();
        while (mov) {
            buf += String.format(Locale.US, "  <wpt lat=\"%f\" lon=\"%f\">\n" +
                            "    <name>%s</name>\n" +
                            "    <desc>%s</desc>\n" +
                            "    <cmt>wheelchair=%d</cmt>\n" +
                            "  </wpt>\n",
                    res.getFloat(0), res.getFloat(1), "トイレ", res.getString(2), res.getInt(3));
            mov = res.moveToNext();
        }

        res.close();
        return buf;
    }
}
