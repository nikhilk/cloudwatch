package google.com.cloudwatch;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ivann on 11/19/14.
 */
public class ProjectAdapter extends BaseAdapter {
  final Context _context;
  final List<Map<String, Object>> _projects;
  final ArrayList<DataSetObserver> _observers = new ArrayList<DataSetObserver>();
  final LayoutInflater _inflater;

  public ProjectAdapter(Context context, List<Map<String, Object>> projects) {
    _context = context;
    _projects = projects;
    _inflater = LayoutInflater.from(context);
  }

  public void notifyDataSetChanged() {
    for (DataSetObserver o: _observers) {
      o.onChanged();
    }
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {
    _observers.add(observer);
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {
    _observers.remove(observer);
  }

  @Override
  public int getCount() {
    return _projects.size();
  }

  @Override
  public Object getItem(int position) {
    return _projects.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View result = convertView;
    if (result == null) {
      result = _inflater.inflate(R.layout.project_row, null);
    }
    TextView projectNameView = (TextView) result.findViewById(R.id.project_name);
    Map<String, Object> project = _projects.get(position);

    projectNameView.setText(ProjectSchema.getDisplayName(project));

    return result;
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return _projects.isEmpty();
  }

  @Override
  public boolean areAllItemsEnabled() {
    return true;
  }

  @Override
  public boolean isEnabled(int position) {
    return true;
  }
}
