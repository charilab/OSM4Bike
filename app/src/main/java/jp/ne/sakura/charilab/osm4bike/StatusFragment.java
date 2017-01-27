package jp.ne.sakura.charilab.osm4bike;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import jp.ne.sakura.charilab.maplibs.trackLogger.trackLogger;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by uno on 16/05/12.
 */
public class StatusFragment extends Fragment {
    trackLogger.binder logger;
    final Handler handler = new Handler();
    Timer timer;
    boolean active;

    public StatusFragment() {
        Log.d("DEBUG", "StatusFragment is created.");
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        MainActivity activity = (MainActivity) getActivity();
        logger = activity.logger;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("DEBUG", "StatusFragment.onCreateView is called.");
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        showCurrentStatus(logger.getCurrentStatistics());
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            handler.post(new Runnable() {
                @Override
                synchronized public void run() {
                    if (active && (logger != null)) {
                        showCurrentStatus(logger.getCurrentStatistics());
                    }
                }
            });
            }
        }, 1000, 1000);

        final Button startBtn = (Button) getActivity().findViewById(R.id.btn_start);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (logger.getStatus() == trackLogger.srvStatus.LOGGING) {
                    logger.pause();
                    startBtn.setText("START");
                } else {
                    logger.start();
                    startBtn.setText("PAUSE");
                }
            }
        });

        final Button stopBtn = (Button) getActivity().findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logger.stop();
                startBtn.setText("START");
            }
        });

        final Button lapBtn = (Button) getActivity().findViewById(R.id.btn_lap);
        lapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //logger.lap();
                logger.clear_distance();
            }
        });

        active = true;
    }

    @Override
    public void onPause() {
        active = false;
        timer.cancel();
        super.onPause();
    }


    private String getTime(trackLogger.statistics stat) {
        return String.format(Locale.US, "%02d:%02d:%02d",
                stat.total.time / 3600000,
                (stat.total.time % 3600000) / 60000,
                (stat.total.time % 60000) / 1000);
    }

    private String getDistance(trackLogger.statistics stat) {
        if (stat.total.distance < 1000) {
            if (stat.distance < 1000) {
                return String.format(Locale.US, "%3.0f m | %3.0f m", stat.total.distance, stat.distance);
            } else {
                return String.format(Locale.US, "%3.0f m | %8.2f km", stat.total.distance, stat.distance/1000);
            }
        } else {
            if (stat.distance < 1000) {
                return String.format(Locale.US, "%8.2f km | %3.0f m", stat.total.distance/1000, stat.distance);
            } else {
                return String.format(Locale.US, "%8.2f km | %8.2f km", stat.total.distance/1000, stat.distance/1000);
            }
        }
    }

    private String getSpeed(trackLogger.statistics stat) {
        return String.format(Locale.US, "%5.1f km/h", stat.speed);
    }

    private String getGrade(trackLogger.statistics stat) {
        return String.format(Locale.US, "%4.1f %% [+%4.0f m/-%4.0f m]",
                stat.grade, stat.total.ascent, stat.total.descent);
    }

    synchronized private void showCurrentStatus(trackLogger.statistics stat) {
        String text;

        //Log.d("StatusFragment", "update current status");
        if((stat == null)||(stat.location == null)) {
            return;
        }

        // Field1: Time
        TextView tv1 = (TextView) getActivity().findViewById(R.id.stField1);
        tv1.setText(getTime(stat));

        // Field2: Distance
        TextView tv2 = (TextView) getActivity().findViewById(R.id.stField2);
        tv2.setText(getDistance(stat));

        // Field3: Speed
        TextView tv3 = (TextView) getActivity().findViewById(R.id.stField3);
        tv3.setText(getSpeed(stat));

        // Field4: Altitude
        text = String.format(Locale.US, "%4.0f m", stat.location.getAltitude());
        TextView tv4 = (TextView) getActivity().findViewById(R.id.stField4);
        tv4.setText(text);

        // Field5: Total ascent/descent/grade
        TextView tv5 = (TextView) getActivity().findViewById(R.id.stField5);
        tv5.setText(getGrade(stat));

        // Field6: Bearing
        text = String.valueOf(stat.location.getBearing())+" deg";
        TextView tv6 = (TextView) getActivity().findViewById(R.id.stField6);
        tv6.setText(text);

        // GPS status field
        String date = DateFormat.format("yyyy/MM/dd kk:mm:ss", stat.location.getTime()).toString();
        text = String.format(Locale.US, "%s [%10.6f, %10.6f] Acc: %4.1f m",
                date, stat.location.getLongitude(), stat.location.getLatitude(),
                stat.location.getAccuracy());
        TextView tvGpsStatus = (TextView) getActivity().findViewById(R.id.stGpsStatus);
        tvGpsStatus.setText(text);
    }
}
