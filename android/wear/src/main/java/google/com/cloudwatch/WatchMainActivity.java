// WatchMainActivity.java
//

package google.com.cloudwatch;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.wearable.view.*;
import android.widget.*;
import com.fasterxml.jackson.jr.ob.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.wearable.*;

import java.io.*;
import java.text.*;
import java.util.*;

public final class WatchMainActivity extends Activity
    implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

  private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("kk:mm a");

  private GoogleApiClient _googleApiClient;
  private BroadcastReceiver _timeInfoReceiver;
  private TextView _timeLabel;

  @Override
  public void onConnected(Bundle bundle) {
    Wearable.MessageApi.addListener(_googleApiClient, this);
  }

  @Override
  public void onConnectionSuspended(int i) {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initializeGoogleApiClient();
    registerTimeReceiver();

    setContentView(R.layout.activity_watch_main);
    WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        _timeLabel = (TextView)stub.findViewById(R.id.watch_time);
        updateTime();
      }
    });
  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(_timeInfoReceiver);

    if (_googleApiClient != null) {
      _googleApiClient.unregisterConnectionCallbacks(this);
    }

    super.onDestroy();
  }

  @Override
  public void onMessageReceived(final MessageEvent messageEvent) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          String json = new String(messageEvent.getData());
          Map<String, Object> data = JSON.std.mapFrom(json);

          String newText = data.get("timestamp") + ":" + data.get("value");
          _timeLabel.setText(newText);
        }
        catch (IOException e) {
          e.printStackTrace();
          // Ignore any errors
        }
      }
    });
  }

  protected void onResume() {
    super.onResume();

    if ((_googleApiClient != null) &&
        !(_googleApiClient.isConnected() || _googleApiClient.isConnecting())) {
      _googleApiClient.connect();
    }
  }

  protected void onStart() {
    super.onStart();
  }

  protected void onStop() {
    if (_googleApiClient != null) {
      Wearable.MessageApi.removeListener(_googleApiClient, this);
      if (_googleApiClient.isConnected()) {
        _googleApiClient.disconnect();
      }
    }

    super.onStop();
  }

  private void initializeGoogleApiClient() {
    GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);

    _googleApiClient = builder.addApi(Wearable.API)
                              .addConnectionCallbacks(this)
                              .build();
    _googleApiClient.connect();
  }

  private void registerTimeReceiver() {
    _timeInfoReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        updateTime();
      }
    };

    IntentFilter timeIntentFilter = new IntentFilter();
    timeIntentFilter.addAction(Intent.ACTION_TIME_TICK);
    timeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    timeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);

    registerReceiver(_timeInfoReceiver, timeIntentFilter);
  }

  private void updateTime() {
    if (_timeLabel != null) {
      _timeLabel.setText(DATE_FORMAT.format(Calendar.getInstance().getTime()));
    }
  }
}
