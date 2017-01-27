package jp.ne.sakura.charilab.maplibs.trackLogger;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
 * This service collects any tracking data for biking from several sensors
 * such as GPS, a cadence sensor, HR monitor and so on.
 * It provides the data to any activities in this application interactively
 * or with callback mechanism.
 */
public class trackLogger extends IntentService {
    public enum srvStatus { ACTIVE, LOGGING, PAUSED};
    private srvStatus status;
    private ArrayList<listener> listeners;
    private statistics stat;
    private Timer timer;
    private LocationManager locationManager;
    private trackDatabase trkDB;

    public trackLogger() {
        super("trackLogger");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stat = new statistics();
        trkDB = new trackDatabase(getBaseContext());

        listeners = new ArrayList();
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        int perm = getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getPackageName());
        if ((locationManager == null) || (perm != PackageManager.PERMISSION_GRANTED)) {
            Log.d("trackLogger", "There are no location services. trackLogger is disabled.");
            return;
        }

        try {
            stat.update(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location cur_loc) {
                    stat.update(cur_loc);
                    trkDB.add_track(stat);

                    for (int i = 0; i < listeners.size(); i++) {
                        listeners.get(i).onLocationChanged(stat);
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, locationListener);
        } catch (IllegalArgumentException e) {
            Log.d("trackLogger", "provider is null or doesn't exist");
        } catch (SecurityException e) {
            Log.d("trackLogger", "no suitable permission is present.");
        }

        // Setup interval timer for update stat
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    stat.update(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                } catch (IllegalArgumentException e) {
                    Log.d("trackLogger", "provider is null or doesn't exist.");
                } catch (SecurityException e) {
                    Log.d("trackLogger", "no suitable permission is present.");
                }
            }
        }, 1000, 1000);
        status = srvStatus.ACTIVE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return new binder();
        //return super.onBind(intent);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long endTime = System.currentTimeMillis() + 5*1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }
    }

    public interface listener {
        public void onLocationChanged(statistics stat);
    }

    public class binder extends Binder {
        public statistics getCurrentStatistics() {
            return stat;
        }

        public void addListener(listener listener) {
            listeners.add(listener);
        }

        public void removeListener(listener listener) {
            listeners.remove(listener);
        }

        public void removeListenerAll() {
            listeners.removeAll(listeners);
        }

        public srvStatus getStatus() {
            return status;
        }

        public boolean start() {
            if (status == srvStatus.ACTIVE) {
                stat.reset();
                trkDB.add_activity();
                trkDB.add_lap();
                stat.start();
                status = srvStatus.LOGGING;
                return true;
            } else if (status == srvStatus.PAUSED) {
                stat.start();
                status = srvStatus.LOGGING;
                return true;
            } else {
                return false;
            }
        }

        public boolean stop() {
            if (status != srvStatus.ACTIVE) {
                stat.stop();
                trkDB.finish_lap(stat.lap);
                trkDB.finish_activity(stat.total);
                status = srvStatus.ACTIVE;
                return true;
            } else {
                return false;
            }
        }

        public boolean pause() {
            if (status == srvStatus.LOGGING) {
                stat.pause();
                status = srvStatus.PAUSED;
                return true;
            } else {
                return false;
            }
        }

        public boolean lap() {
            if (status == srvStatus.LOGGING) {
                trkDB.finish_lap(stat.lap);
                trkDB.add_lap();
                stat.lap();
                return true;
            } else {
                return false;
            }
        }

        synchronized public void clear_distance() {
            stat.distance = 0;
        }
    }

    public class overallStatus {
        public Date    startDate, endDate;
        public float   max_speed;   // km/h
        public float   avg_speed;
        public float   distance;    // meters
        public long    time;        // milliseconds
        public double  ascent, descent;

        public overallStatus() {}

        public void reset() {
            startDate = endDate = null;
            max_speed = avg_speed = 0;
            distance  = 0;
            time = 0;
            ascent = descent = 0;
        }
    }

    public class statistics {
        // current Status
        public float    speed;       // km/h
        public float    distance;    // meters
        public double   grade;
        public Location location;
        public Date date;

        // overall status
        public overallStatus total;
        public overallStatus lap;

        // for internal use
        private Location baseLoc;
        private boolean initialized, active;

        public statistics() {
            total = new overallStatus();
            lap = new overallStatus();
            initialized = true;
        }

        synchronized public void start() {
            total.startDate = Calendar.getInstance().getTime();
            lap.startDate = Calendar.getInstance().getTime();
            active = true;
        }

        synchronized public void stop() {
            total.startDate = Calendar.getInstance().getTime();
            lap.startDate = Calendar.getInstance().getTime();
            active = false;
        }

        synchronized public void pause() {
            active = false;
        }

        synchronized public void reset() {
            total.reset();
            lap.reset();
            initialized = true;
            active = false;
        }

        synchronized public void lap() {
            lap.reset();
        }

        synchronized public void update(Location curLoc) {
            if (curLoc == null) {
                return;
            }

            if (initialized) {
                speed = curLoc.getSpeed()*3.6F;
                date = Calendar.getInstance().getTime(); // must be fixed
                distance = 0;  // should be fixed
                grade = 0;  // should be fixed
                location = baseLoc = curLoc;
                initialized = false;
                return;
            }

            float dist_diff = curLoc.distanceTo(location);
            Date now = Calendar.getInstance().getTime();
            long time_diff = now.getTime() - date.getTime();
            speed = ((dist_diff > 0)||(time_diff<1000)) ? curLoc.getSpeed()*3.6F : 0;

            // calculation altitude related statistics (MUST be tuned)
            double alt_diff = curLoc.getAltitude() - baseLoc.getAltitude();
            float dist_diff_base = curLoc.distanceTo(baseLoc);
            if ((dist_diff_base > 20) && (Math.abs(alt_diff) >= 5.0)) {
                grade = (alt_diff * 100) / dist_diff_base;  // grade in %
                baseLoc = curLoc;
            } else {
                alt_diff = 0;
            }

            if (active) {
                distance += dist_diff;
                total.distance += dist_diff;
                lap.distance += dist_diff;
                total.time = now.getTime() - total.startDate.getTime();
                lap.time = now.getTime() - lap.startDate.getTime();
                total.avg_speed = total.distance * 3600000 / total.time;
                lap.avg_speed = lap.distance * 3600000 / lap.time;
                total.max_speed = (speed > total.max_speed) ? speed : total.max_speed;
                lap.max_speed = (speed > lap.max_speed) ? speed : lap.max_speed;
                if (alt_diff > 0) {
                    total.ascent += alt_diff;
                    lap.ascent += alt_diff;
                } else {
                    total.descent -= alt_diff;
                    lap.ascent += alt_diff;
                }
            }

            date = now;
            location = curLoc;
        }
    }
}
