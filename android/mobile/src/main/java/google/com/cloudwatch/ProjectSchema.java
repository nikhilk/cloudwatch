package google.com.cloudwatch;

import java.util.Map;

/**
 * Created by ivann on 11/18/14.
 */
public final class ProjectSchema {
  public static final String METRICS = "metrics";

  private ProjectSchema() { }

  public static final String DISPLAY_NAME = "displayName";

  public static String getDisplayName(Map<String, Object> item) {
    return (String) item.get(DISPLAY_NAME);
  }

  public static Map<String, Object> getMetrics(Map<String, Object> project) {
    return (Map<String, Object>) project.get(METRICS);
  }

  public static String getMetricUrl(String projectId, String metricId) {
    return String.format("https://shining-fire-2617.firebaseio.com/data/%s/%s/",
        projectId, metricId);
  }





  private static final String METADATA_URL = "https://shining-fire-2617.firebaseio.com/metadata/";
  private static final String DATA_URL = "https://shining-fire-2617.firebaseio.com/data/";

  public static long getMetricTimestamp(Map<String, Object> values) {
    return ((Number) values.get("timestamp")).longValue();
  }

  public static double getMetricValue(Map<String, Object> values) {
    return ((Number) values.get("value")).doubleValue();
  }
}
