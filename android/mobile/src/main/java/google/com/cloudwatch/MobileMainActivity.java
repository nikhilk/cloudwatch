// MobileMainActivity.java
//

package google.com.cloudwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Map;

public class MobileMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

  ListView _projectsListView;
  final ArrayList<String> _projectNames = new ArrayList<String>();
  ArrayList<Map<String, Object>> _projects = new ArrayList<Map<String, Object>>();
  ArrayAdapter<String> _projectsListAdapter;

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

    GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(this);
    _googleApiClient = apiClientBuilder.addApi(Wearable.API).build();
    _googleApiClient.connect();

    Firebase.setAndroidContext(this);

    _projectsListView = (ListView) findViewById(R.id.projects_list);

    _projectsListAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,
        _projectNames);
    _projectsListView.setAdapter(_projectsListAdapter);

    _projectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, Object> project = _projects.get(position);
        Bundle projectBundle = getBundleFromProject(project);
        Intent intent = new Intent(MobileMainActivity.this, ProjectActivity.class);
        intent.putExtras(projectBundle);
        startActivity(intent);
      }
    });

    getProjects();
  }

  private Bundle getBundleFromProject(Map<String, Object> project) {
    Bundle result = new Bundle();
    result.putString(ProjectSchema.DISPLAY_NAME, ProjectSchema.getDisplayName(project));
    result.putBundle(ProjectSchema.METRICS, getBundleFromMetrics(ProjectSchema.getMetrics(project)));
    return result;
  }

  private Bundle getBundleFromMetrics(Map<String, Object> metrics) {
    Bundle result = new Bundle();
    for (Map.Entry<String, Object> entry: metrics.entrySet()) {
      Map<String, Object> metric = (Map<String, Object>) entry.getValue();
      result.putBundle(entry.getKey(), getBundleFromMetric(metric));
    }
    return result;
  }

  private Bundle getBundleFromMetric(Map<String, Object> metric) {
    Bundle result = new Bundle();
    result.putString(ProjectSchema.DISPLAY_NAME, ProjectSchema.getDisplayName(metric));
    return result;
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

  void processProjectData(Map<String, Object> projects) {
    _projects.clear();
    _projectNames.clear();
    for (Map.Entry<String, Object> entry : projects.entrySet()) {
      Map<String, Object> project = (Map<String, Object>) entry.getValue();
      _projects.add(project);
      _projectNames.add(ProjectSchema.getDisplayName(project));
    }
    _projectsListAdapter.notifyDataSetChanged();
  }

  void getProjects() {
    final Firebase ref = new Firebase("https://shining-fire-2617.firebaseio.com/metadata/");
    ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        processProjectData((Map<String, Object>) dataSnapshot.getValue());
      }

      @Override
      public void onCancelled(FirebaseError firebaseError) {

      }
    });
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
