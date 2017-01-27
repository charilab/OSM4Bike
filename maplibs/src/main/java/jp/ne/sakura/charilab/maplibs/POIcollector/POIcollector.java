package jp.ne.sakura.charilab.maplibs.POIcollector;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import jp.ne.sakura.charilab.maplibs.R;
import jp.ne.sakura.charilab.ui.ListDialog;
import jp.ne.sakura.charilab.ui.menuItem;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
 * Created by uno on 1/21/17.
 */

public class POIcollector {
    enum ExDest {SDCARD, APPLICATION}
    enum ExMode {RAW, GPX, OSM}
    private ArrayList<POIhandler> handlers;
    private OSMnodeDB osmDB;
    private Context ctx;
    private SoundPool mSoundPool;
    private int soundRegister, soundCancel;

    private void registPOIhandlers() {
        handlers = new ArrayList();
        handlers.add(new conveniencePOIhandler(ctx, osmDB));
        handlers.add(new trafficLightsPOIhandler(ctx, osmDB));
        handlers.add(new toiletsPOIhandler(ctx, osmDB));
        handlers.add(new POIhandler(ctx, osmDB));
    }

    public POIcollector(Context context) {
        // setup database and POI handler
        ctx = context;
        osmDB = new OSMnodeDB(ctx);
        registPOIhandlers();

        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundRegister = mSoundPool.load(context, R.raw.register, 0);
        soundCancel = mSoundPool.load(context, R.raw.cancel, 0);
    }

    private void exportToSDcard(InputStream in, String outFile) {
        if (in == null) {
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), outFile);
        try {
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
    }

    private CharSequence getDataFromStream(InputStream in) {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(in));

        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException e) {
            Log.e("ERROR", "fail to read from inputStream.");
        }

        return sb.toString();
    }

    private void exportToApplication(CharSequence data, String outFile) {
        Toast.makeText(ctx, "Sorry, this feature is not supported yet.", Toast.LENGTH_SHORT).show();
        /*
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/*");
            intent.putExtra(Intent.EXTRA_STREAM, data);
            startActivity(Intent.createChooser(intent, outFile));
        } catch (Exception e) {
            Log.e("ERROR", "Error at exporting data to application.");
        }
        */
        return;
    }

    public void exportData(ExDest dest, ExMode mode) {
        String outFile = "";
        InputStream in = null;
        Date now = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmm");

        switch (mode) {
            case RAW: // raw SQLite Database
                in = osmDB.dumpDB();
                outFile = "osm_" + formatter.format(now) + ".db";
                break;
            case GPX: // .gpx
                in = new ByteArrayInputStream(osmDB.dumpAsGPX(handlers));
                outFile = "osm_" + formatter.format(now) + ".gpx";
                break;
            case OSM: // .osm
                in = new ByteArrayInputStream(osmDB.dumpAsOSM(handlers));
                outFile = "osm_" + formatter.format(now) + ".osm";
                break;
        }

        switch (dest) {
            case SDCARD:
                exportToSDcard(in, outFile);
                break;
            case APPLICATION:
                CharSequence data = getDataFromStream(in);
                exportToApplication(data, outFile);
                break;
        }
    }

    public void registerMenu(Activity activity, final Location loc) {
        mSoundPool.play(soundRegister, 1.0F, 1.0F, 0, 0, 1.0F);
        if (loc == null) {
            Toast.makeText(ctx,
                    "Location Service unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        menuItem[] items = new menuItem[handlers.size()];
        for (int i = 0; i < handlers.size(); i++) {
            items[i] = new menuItem(handlers.get(i).name, null);
        }

        // show dialog
        ListDialog.makeDialog(ctx, null, items, new ListDialog.eventListeners() {
            public void onSelected(int i) {
                handlers.get(i).onSelected(loc);
            }

            public void onCancel() {
                mSoundPool.play(soundCancel, 1.0F, 1.0F, 0, 0, 1.0F);
                handlers.get(3).registLoc(loc);
                Toast.makeText(ctx, "Anonymous POI registered.", Toast.LENGTH_SHORT).show();
            }
        }).show(activity.getFragmentManager(), "selection");
    }

    public void exportMenu(Activity activity) {
        menuItem[] items = new menuItem[3];
        items[0] = new menuItem("SDCard", null);
        items[1] = new menuItem("Share", null);
        items[2] = new menuItem("Clear", null);

        // show dialog
        ListDialog.makeDialog(ctx, null, items, new ListDialog.eventListeners() {
            public void onSelected(int id) {
                switch (id) {
                    case 0:
                        exportData(ExDest.SDCARD, ExMode.RAW);
                        exportData(ExDest.SDCARD, ExMode.GPX);
                        exportData(ExDest.SDCARD, ExMode.OSM);
                        Toast.makeText(ctx, "DB was stored into SDcard.", Toast.LENGTH_SHORT).show();
                        return;

                    case 1:
                        //exportData(ExDest.APPLICATION, ExMode.GPX);
                        Toast.makeText(ctx, "Sorry, this feature is not supported yet.", Toast.LENGTH_SHORT).show();
                        return;

                    case 2:
                        // remove POI marker
                        handlers.get(0).recreateTable();
                        Toast.makeText(ctx, "DB was erased.", Toast.LENGTH_SHORT).show();
                        return;
                }
            }

            public void onCancel() {
                Toast.makeText(ctx, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }).show(activity.getFragmentManager(), "selection");
    }
}
