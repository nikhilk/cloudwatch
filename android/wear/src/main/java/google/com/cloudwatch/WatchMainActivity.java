// WatchMainActivity.java
//

package google.com.cloudwatch;

import android.app.*;
import android.os.*;
import android.support.wearable.view.*;

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
  private WatchFace _watchFace;

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

    setContentView(R.layout.activity_watch_main);
    WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        _watchFace = new WatchFace(stub.getContext());
        stub.addView(_watchFace);
      }
    });
  }

  @Override
  protected void onDestroy() {
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
          _watchFace.updateMetrics(data);
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
}
