// MobileMainActivity.java
//

package google.com.cloudwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MobileMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

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

    final Button chooseMetricButton = (Button)findViewById(R.id.chooseMetric);
    chooseMetricButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MobileMainActivity.this, ProjectActivity.class);
        startActivity(intent);
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
