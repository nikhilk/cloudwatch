package google.com.cloudwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class ProjectActivity extends Activity {

  ListView _metricsList;
  final ArrayList<String> _metricNames = new ArrayList<String>();
  ArrayAdapter<String> _metricsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_project);

    _metricsList = (ListView) findViewById(R.id.metrics_list);

    _metricsAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,
        _metricNames);
    _metricsList.setAdapter(_metricsAdapter);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    populateUI(extras);
  }

  private void populateUI(Bundle extras) {
    setTitle(extras.getString(ProjectSchema.DISPLAY_NAME));
    Bundle metrics = extras.getBundle(ProjectSchema.METRICS);
    _metricNames.clear();
    for (String key: metrics.keySet()) {
      Bundle metric = metrics.getBundle(key);
      String metricName = metric.getString(ProjectSchema.DISPLAY_NAME);
      _metricNames.add(metricName);
    }
    _metricsAdapter.notifyDataSetChanged();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_project, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
