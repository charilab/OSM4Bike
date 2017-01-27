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
 * Created by uno on 12/17/15.
 */
public class POIhandler {
    public String name;
    protected String TABLE_NAME = "poi";
    protected String TABLE_SCHEMA = "category integer, lon real, lat real, alt real," +
            " icon integer, name text, desc text, flags integer";
    protected Context context;
    protected Location location;
    protected OSMnodeDB PoiDB;
    private String desc = "";
    protected final int CAT_NONE = 0;
    protected final int CAT_CONVENIENCE = 1;
    protected final int CAT_SIGNALS = 2;
    protected final int CAT_TOILETS = 3;
    protected final int CAT_SERVICES = 4;
    protected final int CAT_PASS = 5;
    protected final int CAT_VIEWPOINT = 6;
    protected final int CAT_BICYCLE = 7;
    protected final int CAT_SPA = 8;

    public POIhandler(Context ctx, OSMnodeDB db) {
        context = ctx;
        name = "その他";
        PoiDB = db;
        PoiDB.createTable(TABLE_NAME, TABLE_SCHEMA);
    }

    void recreateTable() {
        PoiDB.dropTable(TABLE_NAME);
        PoiDB.createTable(TABLE_NAME, TABLE_SCHEMA);
    }

    public void registLoc(Location loc) {
        location = loc;
        desc="Something";
        insertData();
        return;
    }

    public void onSelected(Location loc) {
        location = loc;
        // show dialog
        TextDialog.makeDialog(context, "詳細", new TextDialog.eventListeners() {
            public void onDone(String text) {
                desc = text;
                insertData();
                Toast.makeText(context, "POI has been Registered.", Toast.LENGTH_SHORT).show();
            }
            public void onCancel() {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }).show(((Activity) context).getFragmentManager(), "detail");
    }

    protected void insertData() {
        SQLiteDatabase db = PoiDB.getWritableDatabase();

        ContentValues insertValues = new ContentValues();
        insertValues.put("category", CAT_NONE);
        insertValues.put("lat", location.getLatitude());
        insertValues.put("lon", location.getLongitude());
        insertValues.put("alt", location.getAltitude());
        insertValues.put("name", name);
        insertValues.put("desc", desc);
        insertValues.put("icon", 19); // Should be fixed
        db.insert(TABLE_NAME, null, insertValues);

        String msg = String.format(Locale.US, "insert POI to %s : lon=%f, lat=%f, name=%s, desc=%s\n",
                TABLE_NAME, location.getLongitude(), location.getLatitude(), name, desc);
        Log.d("DEBUG", msg);
    }

    protected String dumpAsGPX(SQLiteDatabase db) {
        String buf = "";
        String[] column = new String[] {"lat", "lon", "name", "desc"};
        String select = String.format(Locale.US, "(category = %d)", CAT_NONE);
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

    protected String dumpAsOSM(SQLiteDatabase db) {
        return "";
    }
}
