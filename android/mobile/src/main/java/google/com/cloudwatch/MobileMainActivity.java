// MobileMainActivity.java
//

package google.com.cloudwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MobileMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

  Query _query;
  ValueEventListener _listener;
  final ObjectMapper _mapper = new ObjectMapper();
  Entity _selectedMetric;
  TextView _selectedMetricTextView;

  private GoogleApiClient _googleApiClient;

  public MobileMainActivity() {
  }

  @Override
  public void onConnected(Bundle bundle) {
    sendMessage("/start_activity", "");
  }

  @Override
  public void onConnectionSuspended(int i) {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mobile_main);

    _selectedMetricTextView = (TextView) findViewById(R.id.chosen_metric);

    final Button sendButton = (Button)findViewById(R.id.sendButton);
    final EditText messageText = (EditText)findViewById(R.id.messageText);
    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String text = messageText.getText().toString();

        if (!text.isEmpty()) {
          messageText.setText("");

          String json = "{\"timestamp\": %d, \"value\": %d}";
          String data = String.format(json, System.currentTimeMillis(), 123);
          sendMessage("/message", data);
        }
      }
    });

    final Button chooseMetricButton = (Button)findViewById(R.id.chooseMetric);
    chooseMetricButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MobileMainActivity.this, ProjectActivity.class);
        startActivityForResult(intent, ProjectActivity.CHOOSE_METRIC_REQUEST);
      }
    });

    GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(this);
    _googleApiClient = apiClientBuilder.addApi(Wearable.API).build();
    _googleApiClient.connect();

    Firebase.setAndroidContext(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_mobile_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.

    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ProjectActivity.CHOOSE_METRIC_REQUEST && resultCode == Activity.RESULT_OK) {
      final Bundle selection = data.getExtras();
      final String projectId = selection.getString(ProjectActivity.EXTRAS_PROJECT_ID);
      final String metricId = selection.getString(ProjectActivity.EXTRAS_METRIC_ID);
      final String metricMetadata = selection.getString(ProjectActivity.EXTRAS_METRIC_METADATA);
      try {
        _selectedMetric = new Entity(metricId, _mapper.readValue(metricMetadata, Map.class));
        _selectedMetricTextView.setText(ProjectSchema.getDisplayName(_selectedMetric.getValues()));
      } catch (IOException e) {
        Log.e("DATA", "Failed to deserialize metadata", e);
      }
      startListeningForMetric(projectId, metricId);
    }
  }

  void onMetricChanges(Map<String, Object> metricValues) {
    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    for (Map.Entry<String, Object> entry: metricValues.entrySet()) {
      data.add((Map<String, Object>) entry.getValue());
    }

    Map<String, Object> metric = new HashMap<String, Object>(_selectedMetric.getValues());
    metric.put("data", data);

    final String message;
    try {
      message = _mapper.writeValueAsString(metric);
      Log.d("DATA", message);
      sendMetricsMessage(message);
    } catch (JsonProcessingException e) {
      Log.e("DATA", "Failed to serialize", e);
    }
  }

  void sendMetricsMessage(String message) {
    sendMessage("/message", message);
  }

  void startListeningForMetric(String projectId, String metricId) {
    if (_listener != null) {
      _query.removeEventListener(_listener);
      _query = null;
    }

    if (_listener == null) {
      _listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          onMetricChanges((Map<String, Object>) dataSnapshot.getValue());
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
      };
    }

    Firebase ref = new Firebase(ProjectSchema.getMetricUrl(projectId, metricId));
    _query = ref.orderByChild("timestamp").limitToLast(100);
    _query.addValueEventListener(_listener);
  }

  @Override
  protected void onDestroy() {
    _googleApiClient.disconnect();
    super.onDestroy();
  }

  private void sendMessage(String path, String text) {
    MessageSender sender = new MessageSender(path, text);
    new Thread(sender).start();
  }

  private final class MessageSender implements Runnable {

    private final String _path;
    private final byte[] _data;

    public MessageSender(String path, String message) {
      _path = path;
      _data = message.getBytes();
    }

    @Override
    public void run() {
      NodeApi.GetConnectedNodesResult nodes =
          Wearable.NodeApi.getConnectedNodes(_googleApiClient).await();
      for (Node node : nodes.getNodes()) {
        Wearable.MessageApi.sendMessage(_googleApiClient, node.getId(), _path, _data).await();
      }
    }
  }
}
