# Copyright 2014 Google Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Implements BigQuery related data parsing helpers."""

import datetime as dt


class Parser(object):
  """A set of helper functions to parse data in BigQuery responses."""

  def __init__(self):
    pass

  @staticmethod
  def parse_row(schema, data):
    """Parses a row from query results into an equivalent object.

    Args:
      schema: the array of fields defining the schema of the data.
      data: the JSON row from a query result.
    Returns:
      The parsed row object.
    """
    def parse_value(data_type, value):
      """Parses a value returned from a BigQuery response."""
      if value is not None:
        if value == 'null':
          value = None
        elif data_type == 'INTEGER':
          value = int(value)
        elif data_type == 'FLOAT':
          value = float(value)
        elif data_type == 'TIMESTAMP':
          value = dt.datetime.utcfromtimestamp(float(value))
        elif data_type == 'BOOLEAN':
          value = value == 'true'
        elif (type(value) != str) and (type(value) != unicode):
          # TODO(nikhilko): Handle nested JSON records
          value = str(value)
      return value

    row = {}
    for i, field in enumerate(data['f']):
      schema_field = schema[i]
      row[schema_field['name']] = parse_value(schema_field['type'], field['v'])

    return row

  @staticmethod
  def parse_timestamp(value):
    """Parses a timestamp.

    Args:
      value: the number of milliseconds since epoch.
    """
    return dt.datetime.utcfromtimestamp(float(value) / 1000.0)
