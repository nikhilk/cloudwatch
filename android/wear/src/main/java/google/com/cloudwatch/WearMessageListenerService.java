// WearMessageListenerService.java
//

package google.com.cloudwatch;

import android.content.Intent;

import com.google.android.gms.wearable.*;

public class WearMessageListenerService extends WearableListenerService {

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equalsIgnoreCase("/start_activity")) {
      Intent intent = new Intent(this, WatchMainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      startActivity(intent);
    }
    else {
      super.onMessageReceived(messageEvent);
    }
  }
}
