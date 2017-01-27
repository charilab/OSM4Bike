package jp.ne.sakura.charilab.maplibs.POIcollector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
 * Created by uno on 9/7/15.
 */
public class OSMnodeDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "collection.db";
    private static final int DATABASE_VERSION = 1;
    private Context ctx;

    public OSMnodeDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    public boolean createTable(String tblName, String tblSchema) {
        SQLiteDatabase db = this.getWritableDatabase();
        String createCmd;
        Log.d("createTable", "Checking if table "+tblName+" is exist.");
        String query = String.format("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='%s';",
                tblName);
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            // create tables
            createCmd = String.format("CREATE TABLE %s (%s);", tblName, tblSchema);
            db.execSQL(createCmd);
            Log.d("createTable", "SQLite table "+tblName+" has been created.");
            return true;
        }
        return false;
    }

    public boolean dropTable(String tblName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String cmd;

        cmd = String.format("DROP TABLE %s;", tblName);
        db.execSQL(cmd);
        Log.d("dropTable", "SQLite table " + tblName + " has been deleted.");
        return true;
    }

    public byte[] dumpAsGPX(ArrayList<POIhandler> handlers) {
        String buf="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx version=\"1.0\">\n";

        SQLiteDatabase db = this.getReadableDatabase();
        for (int i=0; i<handlers.size(); i++) {
            buf += handlers.get(i).dumpAsGPX(db);
        }
        db.close();
        buf += "</gpx>\n";
        return buf.getBytes();
    }

    public byte[] dumpAsOSM(ArrayList<POIhandler> handlers) {
        String buf="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<osm version=\"0.6\" upload=\"false\" generator=\"osmview\">\n";

        SQLiteDatabase db = this.getReadableDatabase();
        for (int i=0; i<handlers.size(); i++) {
            buf += handlers.get(i).dumpAsOSM(db);
        }
        db.close();
        buf += "</osm>\n";
        return buf.getBytes();
    }

    public InputStream dumpDB() {
        String path = "/data/data/net.charilog.osm4bike2/databases";
        InputStream in = null;

        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                Log.d("dumpDB", "File #" + (i + 1) + ": " + file.getName());
            }
        } else {
            Log.d("dumpDB", "No files found.");
        }
        try {
            in = new FileInputStream(path+"/"+DATABASE_NAME);
        } catch (IOException e) {
            Log.e("dumpDB", "Create InputStream failed with "+e.getMessage());
        }
        return in;
    }
}