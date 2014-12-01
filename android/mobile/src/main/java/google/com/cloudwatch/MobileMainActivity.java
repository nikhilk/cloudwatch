// MobileMainActivity.java
//

package google.com.cloudwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MobileMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

  private GoogleApiClient _googleApiClient;
  private Query _query;
  private ChildEventListener _queryListener;
  private TextView _selectedMetricTextView;

  private final ObjectMapper _mapper;
  private Entity _selectedMetric;
  private final ArrayList<Double> _values;
  private ArrayAdapter<Double> _valuesAdapter;

  private ListView _valuesView;

  public MobileMainActivity() {
    _mapper = new ObjectMapper();
    _values = new ArrayList<Double>(100);
  }

  @Override
  public void onConnected(Bundle bundle) {
  }

  @Override
  public void onConnectionSuspended(int i) {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mobile_main);

    _selectedMetricTextView = (TextView) findViewById(R.id.chosen_metric);
    _valuesView = (ListView) findViewById(R.id.values_list);

    Button chooseMetricButton = (Button) findViewById(R.id.chooseMetric);
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

    _valuesAdapter = new ArrayAdapter<Double>(this, android.R.layout.simple_list_item_1, _values);
    _valuesView.setAdapter(_valuesAdapter);

    Firebase.setAndroidContext(this);
  }

  @Override
  protected void onDestroy() {
    _googleApiClient.disconnect();
    super.onDestroy();
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
      }
      catch (IOException e) {
        Log.e("DATA", "Failed to deserialize metadata", e);
      }

      startListeningForMetric(projectId, metricId);
    }
  }

  private void processNewMetricValue(Map<String, Object> metricValue) {
    double value = ProjectSchema.getMetricValue(metricValue);
    if (_values.size() >= 100) {
      _values.remove(0);
    }
    _values.add(value);

    _valuesAdapter.notifyDataSetChanged();

    updateMetric();
  }

  private void startListeningForMetric(String projectId, String metricId) {
    if (_queryListener != null) {
      _query.removeEventListener(_queryListener);
      _query = null;
      _values.clear();
    }

    if (_queryListener == null) {
      _queryListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
          processNewMetricValue((Map<String, Object>) dataSnapshot.getValue());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
      };
    }

    Firebase ref = new Firebase(ProjectSchema.getMetricUrl(projectId, metricId));
    _query = ref.orderByChild("timestamp").limitToLast(100);
    _query.addChildEventListener(_queryListener);
  }

  private void updateMetric() {
    Map<String, Object> metric = new HashMap<String, Object>(_selectedMetric.getValues());
    metric.put("data", _values);

    try {
      String messageData = _mapper.writeValueAsString(metric);
      Log.d("DATA", messageData);
      MessageSender.sendData(_googleApiClient, messageData);
    }
    catch (JsonProcessingException e) {
      Log.e("DATA", "Failed to serialize", e);
    }
  }


  private static final class MessageSender implements Runnable {

    private final GoogleApiClient _apiClient;
    private final byte[] _data;

    public MessageSender(GoogleApiClient apiClient, String message) {
      _apiClient = apiClient;
      _data = message.getBytes();
    }

    @Override
    public void run() {
      NodeApi.GetConnectedNodesResult nodes =
          Wearable.NodeApi.getConnectedNodes(_apiClient).await();
      for (Node node : nodes.getNodes()) {
        Wearable.MessageApi.sendMessage(_apiClient, node.getId(), "/data", _data).await();
      }
    }

    public static void sendData(GoogleApiClient apiClient, String data) {
      MessageSender sender = new MessageSender(apiClient, data);
      new Thread(sender).start();
    }
  }
}
