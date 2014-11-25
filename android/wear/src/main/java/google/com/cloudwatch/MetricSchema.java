package google.com.cloudwatch;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ivann on 11/25/14.
 */
public class MetricSchema {
  private static final String METRIC_DATA = "data";
  private static final String METRIC_MAX_VALUE = "max";
  private static final String METRIC_MIN_VALUE = "min";
  private static final String METRIC_DISPLAY_NAME = "displayName";
  private static final String METRIC_UNIT_NAME = "unitName";

  private MetricSchema() { }

  public static ArrayList<Float> getValues(Map<String, Object> metric) {
    ArrayList<Float> result = new ArrayList<Float>();
    if (metric.containsKey(METRIC_DATA)) {
      ArrayList<Object> values = (ArrayList<Object>) metric.get(METRIC_DATA);
      for (Object obj: values) {
        Number value = (Number) obj;
        result.add(value.floatValue());
      }
    }
    return result;
  }

  public static float getMaxValue(Map<String, Object> metric) {
    if (metric.containsKey(METRIC_MAX_VALUE)) {
      Number value = (Number) metric.get(METRIC_MAX_VALUE);
      return value.floatValue();
    }
    return 1.0f;
  }

  public static float getMinValue(Map<String, Object> metric) {
    if (metric.containsKey(METRIC_MIN_VALUE)) {
      Number value = (Number) metric.get(METRIC_MIN_VALUE);
      return value.floatValue();
    }
    return 0.0f;
  }

  public static String getDisplayName(Map<String, Object> metric) {
    if (metric.containsKey(METRIC_DISPLAY_NAME)) {
      return (String) metric.get(METRIC_DISPLAY_NAME);
    }
    return "";
  }

  public static String getUnitName(Map<String, Object> metric) {
    if (metric.containsKey(METRIC_UNIT_NAME)) {
      return (String) metric.get(METRIC_UNIT_NAME);
    }
    return "";
  }
}
