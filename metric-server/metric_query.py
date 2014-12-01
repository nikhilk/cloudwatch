import gcp.bigquery as bq


# todo: Add project parm
class metric_query():

	def getMetricString(self):
		return str(45)

	def getBQMetricString(self):
		return bq.query('SELECT timestamp, latency FROM [cloud-datalab:sampledata.requestlogs_20140616] order by timestamp desc LIMIT 3;').results()
