// MobileMainActivity.java
//

package google.com.cloudwatch;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MobileMainActivity extends Activity {

  TextView _emailTextView;
  ListView _projectsListView;
  ArrayList<String> mProjects = new ArrayList<String>();
  ArrayAdapter<String> _projectsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mobile_main);

    Firebase.setAndroidContext(this);

    _emailTextView = (TextView) findViewById(R.id.email_text);
    _projectsListView = (ListView) findViewById(R.id.projects_list);

    _projectsAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,
        mProjects);
    _projectsListView.setAdapter(_projectsAdapter);

    getUsername();
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

  // Authorization management

  String _email;
  static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
  private static final String SCOPE =
      "oauth2:https://www.googleapis.com/auth/userinfo.profile";

  private void pickUserAccount() {
    String[] accountTypes = new String[]{"com.google"};
    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
        accountTypes, false, null, null, null, null);
    startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
  }

  void getUsername() {
    if (_email == null) {
      pickUserAccount();
    } else {
      new GetUsernameTask(this, _email, SCOPE).execute();
    }
  }

  void updateEmail(String email) {
    _email = email;
    _emailTextView.setText(email);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
      // Receiving a result from the AccountPicker
      if (resultCode == RESULT_OK) {
        String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        Log.d("AUTH", "Email name is: " + email);
        updateEmail(email);
        // With the account name acquired, go get the auth token
        getUsername();
      } else if (resultCode == RESULT_CANCELED) {
        // The account picker dialog closed without selecting an account.
        // Notify users that they must pick an account to proceed.
        Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
      }
    } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR
        && resultCode == RESULT_OK) {
      getUsername();
    }
  }

  static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

  public void handleException(final Exception e) {
    // Because this call comes from the AsyncTask, we must ensure that the following
    // code instead executes on the UI thread.
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (e instanceof GooglePlayServicesAvailabilityException) {
          // The Google Play services APK is old, disabled, or not present.
          // Show a dialog created by Google Play services that allows
          // the user to update the APK
          int statusCode = ((GooglePlayServicesAvailabilityException)e)
              .getConnectionStatusCode();
          Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
              MobileMainActivity.this,
              REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
          dialog.show();
        } else if (e instanceof UserRecoverableAuthException) {
          // Unable to authenticate, such as when the user has not yet granted
          // the app access to the account, but the user can fix this.
          // Forward the user to an activity in Google Play services.
          Intent intent = ((UserRecoverableAuthException)e).getIntent();
          startActivityForResult(intent,
              REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
        }
      }
    });
  }

  void processProjectData(List<Object> projects) {
    mProjects.clear();
    for (Object p: projects) {
      Map<String, Object> project = (Map<String, Object>) p;
      mProjects.add((String) project.get(ProjectSchema.ID));
    }
    _projectsAdapter.notifyDataSetChanged();
  }

  void gotToken(final String token) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          final Firebase ref = new Firebase("https://shining-fire-2617.firebaseio.com/projects/");
          ref.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
              Log.d("AUTH", "Authenticated with Firebase");
              ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                  Log.d("FIREBASE", dataSnapshot.toString());
                  processProjectData((List<Object>) dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
              });

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
              Log.d("AUTH", "Error: " + firebaseError.toString());
            }
          });
        } catch(Throwable ex) {
          Log.d("AUTH", "Failed to login", ex);
        }
      }
    });
  }

}
