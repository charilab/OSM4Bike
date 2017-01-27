package jp.ne.sakura.charilab.osm4bike;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import jp.ne.sakura.charilab.maplibs.POIcollector.POIcollector;
import jp.ne.sakura.charilab.maplibs.trackLogger.trackLogger;

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
public class MainActivity extends AppCompatActivity {
    public trackLogger.binder logger=null;
    public Config config;
    public POIcollector poicollector;
    public MapFragment map;
    public StatusFragment stat;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger = (trackLogger.binder) service;

            // create fragments after logger has been connected.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = new Config();
        poicollector = new POIcollector(this);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, trackLogger.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        map.setCurrentLocation(false);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BUTTON_X:
                        Toast.makeText(getApplicationContext(),
                                "Enable current Location", Toast.LENGTH_SHORT).show();
                        map.setCurrentLocation(true);
                        return true;

                    case KeyEvent.KEYCODE_BUTTON_Y:
                        map.zoomIn();
                        return true;

                    case KeyEvent.KEYCODE_BUTTON_A:
                        map.zoomOut();
                        return true;

                    case KeyEvent.KEYCODE_BUTTON_B:
                        poicollector.registerMenu(this, logger.getCurrentStatistics().location);
                        return true;

                    default:
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    stat = new StatusFragment();
                    return stat;

                case 1:
                    map = new MapFragment();
                    return map;

                case 2:
                    SettingFragment set = new SettingFragment();
                    return set;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
