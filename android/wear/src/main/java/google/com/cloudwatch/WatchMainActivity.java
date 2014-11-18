package google.com.cloudwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.os.BatteryManager;

import java.util.Calendar;

import java.text.SimpleDateFormat;

public class WatchMainActivity extends Activity {

    private final static IntentFilter INTENT_FILTER;
    private final static SimpleDateFormat DATE_FORMAT;

    private TextView mTime, mBattery;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Intent.ACTION_TIME_TICK);
        INTENT_FILTER.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        INTENT_FILTER.addAction(Intent.ACTION_TIME_CHANGED);

        DATE_FORMAT = new SimpleDateFormat("kk:mm a");
    }

    private BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String timeText = DATE_FORMAT.format(Calendar.getInstance().getTime());
            mTime.setText(timeText);
        }
    };

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            mBattery.setText(String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) + "%"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTime = (TextView) stub.findViewById(R.id.watch_time);
                mBattery = (TextView) stub.findViewById(R.id.watch_battery);

                mTimeInfoReceiver.onReceive(null, null);
                registerReceiver(mTimeInfoReceiver, INTENT_FILTER);
                registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mTimeInfoReceiver);
        unregisterReceiver(mBatInfoReceiver);

        super.onDestroy();
    }
}
