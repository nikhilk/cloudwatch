from collections import namedtuple

class ipynb_evaluator():
	def __init__(self):
		pass

	@staticmethod
	def evaluate(bucket, notebook):
		Metric = namedtuple("Metric", "name items")
		# Make up sql_results for now - later to be set to BQ QueryResult
		sql_results = [{'ts': 12345678.654321, 'value': 78}]
		metric1 = Metric('latency', list(sql_results))
		global metric_results
		metric_results = []
		metric_results.append(metric1)

