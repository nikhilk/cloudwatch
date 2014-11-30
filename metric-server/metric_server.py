# Serves up metrics to CloudWatch client app
# Read metrics from stores used by cloud app and write to store used by client app
# This is where one could add calls to services for anomaly detection, prediction etc
# Otherwise it is just a wrapper for read(s) followed by write


# This is config info but declared as file-scope var in absence of a simple story
bucket = 'cloud-androidwear-notebooks'
path = 'App Metrics.ipynb'
fb_url_base = 'https://shining-fire-2617.firebaseio.com/data/cloud-androidwear/'
# Metric name needs to be spliced in between base url and suffix covering .json extension & auth token
fb_auth_suffix = '.json?auth=wGLkuGRzoPqkBFvICYIhnI8jj3rUUQ9E1jZxTtoy'

from metric_reader import metric_reader
from metric_writer import metric_writer


class metric_server():
    def __init__(self):
        pass

    @staticmethod
    def serve():
        metric_results = metric_reader.read(bucket, path)
        result = metric_writer.write(fb_url_base, fb_auth_suffix, metric_results)
        return metric_results