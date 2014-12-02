# Generate pseudo-random data with timestamp

import time
import datetime
import random

# Magic constant: generated value will be in [0, ceiling-1] range
ceiling = 97



# Generate a list of metrics
# This is based on the following assumptions:
# - Cron duration is 1 min and we want distinct timestamps within a minute
# - Loop time is smaller than a second
# - We would like to keep total order among timestamps across multiple cron invocations
# - n is between 1 and 30
# Per cron execution checks removed. Above contract is not checked/enforced
def gen(n=1):
    # if not (1 <= n <= 30) raise ValueError("Argument must be in [1,30]")
    # Unfortunately, Python datetime doesn't implement datetime.totimestamp() 
    # So make up for it with a custom implementation
    current_time = datetime.datetime.utcnow()
    timediff = current_time - datetime.datetime(1970,1,1)
    # Plan to use only the seconds part of timestamp; leaving out microseconds
    metric_timestamp = timediff.days * 24 * 3600 + timediff.seconds
    # Compute "safe" increment for seconds part; timestamp is non-random
    # Pseudo-random part is the value; not the timestamp
    sec_incr = 60/ (2*n)
    metric_list = []
    for i in range (0, n):
        time = metric_timestamp + i*sec_incr
        random_value = random.randrange(0,ceiling)
        dict = {'timestamp':time, 'value': random_value}
        metric_list.append(dict)
    return metric_list

# This is for testing only. Instead of using metrics obtained by evaluating a notebook, 
# this function makes up data for testing writes to Firebase
# It uses identical object model copied from App Metrics notebook

from collections import namedtuple
Metric = namedtuple('Metric', 'name items')

def gen_multiple_metrics():

    latency_results = gen(3)
    metric1 = Metric('latency', latency_results)
    cpu_results = gen(2)
    metric2 = Metric('cpu', cpu_results)
    metric_results = []
    metric_results.append(metric1)
    metric_results.append(metric2)
    return metric_results
