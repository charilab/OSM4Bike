package jp.ne.sakura.charilab.maplibs.trackLogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
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
 * For tracking GPS data
 */
public class trackDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "track.db";
    private static final int DATABASE_VERSION = 3;
    private String ACT_TABLE_NAME = "activities";
    private String ACT_TABLE_SCHEMA = "act_id integer, lap integer," +
            " st_date integer, en_date integer, dist real, time real, avg real, max real," +
            " ascent real, descent real";
    private String TRK_TABLE_NAME = "tracks";
    private String TRK_TABLE_SCHEMA = "act_id integer, lap integer, date integer, gpsdate integer," +
            "lat real, lon real, alt real, acc real," +
            "speed real, bearing real, grade real";
    private int curActId=0, curLapId=0;
    private long lastDate=0;

    private boolean createTable(SQLiteDatabase db, String tblName, String tblSchema) {
        Log.d("DEBUG:", "Checking if table "+tblName+" is exist.");
        String query = String.format("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='%s';",
                tblName);
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            // create tables
            String createCmd = String.format("CREATE TABLE %s (%s);", tblName, tblSchema);
            db.execSQL(createCmd);
            Log.d("DEBUG:", "SQLite table "+tblName+" has been created.");
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    private boolean dropTable(SQLiteDatabase db, String tblName) {
        Log.d("DEBUG:", "Drop table "+tblName+".");
        String createCmd = String.format("DROP TABLE %s;", tblName);
        db.execSQL(createCmd);
        return true;
    }

    public trackDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, ACT_TABLE_NAME, ACT_TABLE_SCHEMA);
        createTable(db, TRK_TABLE_NAME, TRK_TABLE_SCHEMA);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DEBUG:", "Upgrading tables from ver. "+oldVersion+" to ver."+newVersion+".");
        dropTable(db, ACT_TABLE_NAME);
        dropTable(db, TRK_TABLE_NAME);
        createTable(db, ACT_TABLE_NAME, ACT_TABLE_SCHEMA);
        createTable(db, TRK_TABLE_NAME, TRK_TABLE_SCHEMA);
    }

    synchronized public int add_activity() {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("DEBUG:", "Checking the number of maximum activity ID.");
        // determine new activity ID
        String query = String.format("SELECT MAX(act_id) FROM %s;", ACT_TABLE_NAME);
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        curActId = c.getInt(0) + 1;
        curLapId = 0;
        c.close();

        // create new activity entry
        ContentValues insertValues = new ContentValues();
        insertValues.put("act_id", curActId);
        insertValues.put("lap", curLapId);
        insertValues.put("st_date", Calendar.getInstance().getTime().getTime());
        db.insert(ACT_TABLE_NAME, null, insertValues);
        return curActId;
    }

    synchronized public boolean finish_activity(trackLogger.overallStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();

        // create new activity entry
        ContentValues updateValues = new ContentValues();
        updateValues.put("en_date", Calendar.getInstance().getTime().getTime());
        updateValues.put("dist", status.distance);
        updateValues.put("time", status.time);
        updateValues.put("avg", status.avg_speed);
        updateValues.put("max", status.max_speed);
        updateValues.put("ascent", status.ascent);
        updateValues.put("descent", status.descent);
        db.update(ACT_TABLE_NAME, updateValues, "act_id = "+curActId, null);
        curActId = 0;
        curLapId = 0;
        return true;
    }

    synchronized public int add_lap() {
        SQLiteDatabase db = this.getWritableDatabase();
        curLapId++;

        // create new activity entry
        ContentValues insertValues = new ContentValues();
        insertValues.put("act_id", curActId);
        insertValues.put("lap", curLapId);
        insertValues.put("st_date", Calendar.getInstance().getTime().getTime());
        db.insert(ACT_TABLE_NAME, null, insertValues);
        return curLapId;
    }

    synchronized public boolean finish_lap(trackLogger.overallStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();

        // create new activity entry
        ContentValues updateValues = new ContentValues();
        updateValues.put("en_date", Calendar.getInstance().getTime().getTime());
        updateValues.put("dist", status.distance);
        updateValues.put("time", status.time);
        updateValues.put("avg", status.avg_speed);
        updateValues.put("max", status.max_speed);
        updateValues.put("ascent", status.ascent);
        updateValues.put("descent", status.descent);
        String where = String.format(Locale.US, "act_id = %d and lap = %d", curActId, curLapId);
        db.update(ACT_TABLE_NAME, updateValues, where, null);
        return true;
    }

    synchronized public boolean add_track(trackLogger.statistics stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        Location loc = stat.location;

        if ((curActId == 0) || (loc.getTime() == lastDate)) {
            return false;
        }
        // create new activity entry
        ContentValues insertValues = new ContentValues();
        insertValues.put("act_id", curActId);
        insertValues.put("lap", curLapId);
        insertValues.put("date", Calendar.getInstance().getTime().getTime());
        insertValues.put("gpsdate", loc.getTime());
        insertValues.put("lat", loc.getLatitude());
        insertValues.put("lon", loc.getLongitude());
        insertValues.put("alt", loc.getAltitude());
        insertValues.put("acc", loc.getAccuracy());
        insertValues.put("speed", loc.getSpeed());
        insertValues.put("bearing", loc.getBearing());
        insertValues.put("grade", stat.grade);
        db.insert(TRK_TABLE_NAME, null, insertValues);
        lastDate = loc.getTime();
        return true;
    }

    private void exportToSDcard(InputStream in, String outFile) {
        if (in == null) {
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), outFile);
        try {
            int c;
            FileOutputStream out = new FileOutputStream(file);
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            in.close();
            out.flush();
            out.close();
            Log.d("DEBUG", outFile + " is created.");
        } catch (IOException e) {
            Log.e("ERROR:", "Creating dummy file failed with " + e.getMessage());
        }
    }

    public static boolean export() {
        String path = "/data/data/net.charilog.osm4bike2/databases";
        InputStream in;

        // Prepare input stream of track database
        try {
            in = new FileInputStream(path+"/"+DATABASE_NAME);
        } catch (IOException e) {
            Log.d("ERROR", "Create InputStream failed with "+e.getMessage());
            return false;
        }

        // get output file
        String date = DateFormat.format("yyyyMMdd-kkmm", Calendar.getInstance().getTime()).toString();
        String outFile = "trk_" + date + ".db";
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), outFile);
        try {
            int c;

            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.flush();
            out.close();
            Log.d("DEBUG", outFile + " is created.");
        } catch (IOException e) {
            Log.e("ERROR:", "Creating dummy file failed with " + e.getMessage());
        }
        return true;
    }
}
