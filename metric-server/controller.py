import webapp2
import generate_data
from metric_query import metric_query
from metric_writer import metric_writer
from sample_metric_writer import sample_metric_writer
# Placeholder for testing
#import ipynb_evaluator as ie
from metric_reader import metric_reader
from metric_server import metric_server


# Final end-to-end metric server - to be invoked by cron
# This route is purely for testing / health check
class MetricHandler(webapp2.RequestHandler):
    def get(self):
        metric_results = metric_server.serve()
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('Processed metrics: ' + str(metric_results))


# The following routes are ALL for testing / health check of bits and pieces

# Evaluate code in ipynb to obtain metric value and write it to response
# Code in ipynb may make up or read from BQ
# Eval populates a global object (alas) "metric_result" that is used here
class EvalHandler(webapp2.RequestHandler):
    def get(self):
        metric_results = metric_reader.read()
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('Evaluated metric: ' + str(metric_results))

# Run a BQ query and write the result to response
class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('Metric value: ' + "Cloud-androidwear Metric Server Ready")

# Generate metric and write to Firebase 
# The logic is for cron job while the handler is for testing one execution
class MetGenHandler(webapp2.RequestHandler):
    def get(self):
        #m = generate_data.gen()     
        #result = metric_writer.write('https://shining-fire-2617.firebaseio.com/data/cloud-androidwear/testmetric.json?auth=wGLkuGRzoPqkBFvICYIhnI8jj3rUUQ9E1jZxTtoy', m.timestamp, m.value)
        metric_list = generate_data.gen_multiple_metrics()   
        result = metric_writer.write('https://shining-fire-2617.firebaseio.com/data/cloud-androidwear/', '.json?auth=wGLkuGRzoPqkBFvICYIhnI8jj3rUUQ9E1jZxTtoy', metric_list)
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('ScheduledMetric: ' + str(result))


# Another cron job with a test handler
# Generate sample data and write to BQ
class SampleHandler(webapp2.RequestHandler):
    def get(self):
        result = sample_metric_writer.write_sample()
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('ScheduledMetric: ' + str(result))

# All routes are for testing and health check only
# This server is entirely cron driven
application = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/Eval', EvalHandler),
    ('/MetGen', MetGenHandler),
    ('/Metric', MetricHandler),
    ('/Sample', SampleHandler),
], debug=True)
