// WatchMainActivity.java
//

package google.com.cloudwatch;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.wearable.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class WatchMainActivity extends Activity {

  private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("kk:mm a");

  private final BroadcastReceiver _timeInfoReceiver;
  private TextView _timeLabel;

  public WatchMainActivity() {
    super();

    _timeInfoReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        updateTime();
      }
    };
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_watch_main);
    WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        _timeLabel = (TextView)stub.findViewById(R.id.watch_time);

        IntentFilter timeIntentFilter = new IntentFilter();
        timeIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        timeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        registerReceiver(_timeInfoReceiver, timeIntentFilter);
        updateTime();
      }
    });
  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(_timeInfoReceiver);
    super.onDestroy();
  }

  private void updateTime() {
    String timeText = DATE_FORMAT.format(Calendar.getInstance().getTime());
    _timeLabel.setText(timeText);
  }
}
