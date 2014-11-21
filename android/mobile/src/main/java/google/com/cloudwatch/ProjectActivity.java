package google.com.cloudwatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class ProjectActivity extends Activity {

  static public final int CHOOSE_METRIC_REQUEST = 1000;
  static public final String EXTRAS_PROJECT_ID = "projectId";
  static public final String EXTRAS_METRIC_ID = "metricId";
  static public final String EXTRAS_METRIC_METADATA = "metricMetadata";

  final ObjectMapper _mapper = new ObjectMapper();

  Map<String, Object> _rootMetadata;

  Spinner _projectsSpinner;
  Spinner _metricsSpinner;

  final ArrayList<Entity> _projects = new ArrayList<Entity>();
  final ArrayList<Entity> _metricsPerProject = new ArrayList<Entity>();

  EntityAdapter _metricsAdapter;
  EntityAdapter _projectsAdapter;

  Entity _selectedProject;
  Entity _selectedMetric;

  ProgressDialog _progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_project);

    _metricsSpinner = (Spinner) findViewById(R.id.metrics_list);
    _projectsSpinner = (Spinner) findViewById(R.id.projects_list);

    _metricsSpinner.setEnabled(false);
    _projectsSpinner.setEnabled(false);

    _metricsAdapter = new EntityAdapter(this, _metricsPerProject);
    _metricsSpinner.setAdapter(_metricsAdapter);

    _projectsAdapter = new EntityAdapter(this, _projects);
    _projectsSpinner.setAdapter(_projectsAdapter);

    _projectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        _selectedProject = _projects.get(position);
        populateMetricsForProject(_selectedProject);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    _metricsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Entity metric = _metricsPerProject.get(position);
        _selectedMetric = metric;
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    final Button okButton = (Button) findViewById(R.id.ok_button);
    final Button cancelButton = (Button) findViewById(R.id.cancel_button);

    okButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bundle resultData = new Bundle();
        resultData.putString(EXTRAS_METRIC_ID, _selectedMetric.getId());
        resultData.putString(EXTRAS_PROJECT_ID, _selectedProject.getId());
        try {
          resultData.putString(EXTRAS_METRIC_METADATA,
              _mapper.writeValueAsString(_selectedMetric.getValues()));
        } catch (JsonProcessingException e) {
          Log.e("DATA", "Failed to serialize", e);
        }
        Intent result = new Intent();
        result.putExtras(resultData);
        setResult(Activity.RESULT_OK, result);
        finish();
      }
    });

    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(Activity.RESULT_CANCELED);
        finish();
      }
    });

    loadProjects();
  }

  private void loadProjects() {
    _progressDialog = new ProgressDialog(this);
    _progressDialog.setMessage("Loading projects...");
    _progressDialog.setCanceledOnTouchOutside(false);
    _progressDialog.setCancelable(false);
    _progressDialog.show();

    final Firebase ref = new Firebase("https://shining-fire-2617.firebaseio.com/metadata/");
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        _progressDialog.dismiss();
        _progressDialog = null;
        updateMetadata((Map<String, Object>) dataSnapshot.getValue());
      }

      @Override
      public void onCancelled(FirebaseError firebaseError) {

      }
    });
  }

  void populateMetricsForProject(Entity project) {
    Map<String, Object> metrics = ProjectSchema.getMetrics(project.getValues());
    _metricsPerProject.clear();
    for (Map.Entry<String, Object> entry: metrics.entrySet()) {
      Map<String, Object> metric = (Map<String, Object>) entry.getValue();
      _metricsPerProject.add(new Entity(entry.getKey(), metric));
    }
    _metricsAdapter.notifyDataSetChanged();
  }

  void updateMetadata(Map<String, Object> metadata) {
    _rootMetadata = metadata;
    _projects.clear();
    _metricsPerProject.clear();
    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
      Map<String, Object> project = (Map<String, Object>) entry.getValue();
      _projects.add(new Entity(entry.getKey(), project));
    }
    _projectsAdapter.notifyDataSetChanged();
    _metricsAdapter.notifyDataSetChanged();

    _projectsSpinner.setEnabled(true);
    _metricsSpinner.setEnabled(true);
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
