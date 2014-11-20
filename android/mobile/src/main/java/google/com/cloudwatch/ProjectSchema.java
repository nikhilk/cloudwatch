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
}
