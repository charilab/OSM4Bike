package jp.ne.sakura.charilab.maplibs.GeoDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

public class OSMDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    Context context;
    String dbname;

    public OSMDatabaseHelper(Context ctx, String db) {
        super(ctx, db, null, DATABASE_VERSION);
        context = ctx;
        dbname = db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    public void downloadDatabase() {
        Log.d("DEBUG1", "Download database from my server");

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                HttpURLConnection http = null;
                InputStream is = null;
                try {
                    URL url = new URL("http://charilab.sakura.ne.jp/downloads/poi.db");
                    http = (HttpURLConnection) url.openConnection();
                    http.setRequestMethod("GET");
                    http.connect();
                    is = http.getInputStream();

                    File dbFile = context.getDatabasePath(dbname);
                    OutputStream os = new FileOutputStream(dbFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error creating source database", e);
                } catch (Exception e) {
                    Log.d("DEBUG1", "Error during downloading database"+e.toString());
                    e.printStackTrace();
                } finally {
                    try {
                        if (http != null)
                            http.disconnect();
                        if (is != null)
                            is.close();
                    } catch (Exception e) {
                        Log.d("DEBUG1", "Error during finalize download");
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result){
                Log.i("DEBUG1", "Download completed.");
            }
        };
        task.execute();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(DICTIONARY_TABLE_CREATE);
    }
}
