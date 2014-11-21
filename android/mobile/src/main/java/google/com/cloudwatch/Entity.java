package google.com.cloudwatch;

import java.util.Map;

/**
 * Created by ivann on 11/20/14.
 */
public class Entity {
  private final String _id;
  private final Map<String, Object> _values;

  public Entity(String id, Map<String, Object> values) {
    _id = id;
    _values= values;
  }

  @Override
  public String toString() {
    return ProjectSchema.getDisplayName(_values);
  }

  String getId() {
    return _id;
  }

  Map<String, Object> getValues() {
    return _values;
  }
}
