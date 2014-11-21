package google.com.cloudwatch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by ivann on 11/19/14.
 */
public class EntityAdapter extends ArrayAdapter<Entity> {
  final Context _context;
  final List<Entity> _entities;
  int _selectedRow = -1;

  public EntityAdapter(Context context, List<Entity> entities) {
    super(context, android.R.layout.simple_list_item_1, entities);
    _context = context;
    _entities = entities;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View result = super.getView(position, convertView, parent);
    TextView projectNameView = (TextView) result.findViewById(android.R.id.text1);
    Map<String, Object> project = _entities.get(position).getValues();
    projectNameView.setText(ProjectSchema.getDisplayName(project));
    if (position == _selectedRow) {
      result.setBackgroundResource(android.R.color.darker_gray);
    } else {
      result.setBackgroundResource(android.R.color.white);
    }
    return result;
  }

  public void setSelectedRow(int row) {
    _selectedRow = row;
    notifyDataSetChanged();
  }
}
