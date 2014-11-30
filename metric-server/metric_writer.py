# Write supplied metric to firebase via REST API
# Use Http class' POST default when body is specified 

from _http import Http
import json

class metric_writer():
	def __init__(self):
		pass

	
	@staticmethod
	def write_one(url, timestamp, value):
		result = Http.request(url, args=None, data = {'timestamp': timestamp, 'value': value})
		return result


	@staticmethod
	def write(url, auth_str, metric_results):
		result = ''
		for m in metric_results:
			full_url = url + m.name + auth_str
			# Unable to find appropriate Firebase REST API for batch submit
			# So sending individually - suboptimal and possibly problematic for larger lists
			for i in m.items:
				result += str(Http.request(full_url, args=None, data = i))
		return result
