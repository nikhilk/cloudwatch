# Write sample metrics to BQ

import httplib2
from apiclient import discovery
from oauth2client import appengine
import generate_data
import logging

_SCOPE = 'https://www.googleapis.com/auth/bigquery'
PROJECT_ID = 'cloud-androidwear'
DATASET_ID = 'SampleMetrics'
TABLE_ID = 'PseudoRandomNumbers1'


class sample_metric_writer():
    def __init__(self):
        pass

    # Cron granularity is 1 min. So gen n metric tuples instead of 1
    @staticmethod
    def gen_body(n = 3):
        metric_list = generate_data.gen(n)
        rows = []
        body = {"rows":rows}
        for i in range (0, n):
            item = {"json": {"time": metric_list.items[i]['time'], "value": metric_list.items[i]['value'],}}
            body['rows'].append(item)
        return body

    @staticmethod
    def write(body):
        credentials = appengine.AppAssertionCredentials(scope=_SCOPE)
        http = credentials.authorize(httplib2.Http())

        bigquery = discovery.build('bigquery', 'v2', http=http)
        response = bigquery.tabledata().insertAll(
            projectId=PROJECT_ID,
            datasetId=DATASET_ID,
            tableId=TABLE_ID,
            body=body).execute()
        return response


    @staticmethod
    def write_sample():
        logging.getLogger().setLevel(logging.INFO)
        logging.info("In sample_metric_writer.write_sample\n")
        body = sample_metric_writer.gen_body()
        logging.info(str(body))
        response = sample_metric_writer.write(body)
        logging.info(str(response))
        return response
        #return body