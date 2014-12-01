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

"""Implements Context functionality."""

#from ._util import MetadataCredentials
#from ._util import MetadataService
from oauth2client.appengine import AppAssertionCredentials
from google.appengine.api import app_identity


class Context(object):
  """Maintains contextual state for connecting to Cloud APIs.
  """

  _global_context = None

  def __init__(self, project_id, credentials):
    """Initializes an instance of a Context object.

    Args:
      project_id: the current cloud project.
      credentials: the credentials to use to authorize requests.
    """
    self._project_id = project_id
    self._credentials = credentials

  @property
  def credentials(self):
    """Retrieves the value of the credentials property.

    Returns:
      The current credentials used in authorizing API requests.
    """
    return self._credentials

  @property
  def project_id(self):
    """Retrieves the value of the project_id property.

    Returns:
      The current project id to associate with API requests.
    """
    return self._project_id

  @staticmethod
  def default():
    """Creates a default Context object.

    For GCE, the default Context is based on project id and credentials inferred from
    metadata returned by the cloud metadata service. It is also managed as a
    global shared instance used everytime the default context is retrieved.
    For GAE, this has been modified to use AppAssertionCredentials because metadata service
    is not available.

    Returns:
      An initialized and shared instance of a Context object.
    """

    if Context._global_context is None:
      #ms = MetadataService()
      #project_id = ms.project_id
      project_id = app_identity.get_application_id()
      # The following only works on GCE; not GAE
      #credentials = MetadataCredentials(ms)
      # To make matters more interesting, multiple scopes are space-separated with parm name "scope"
      credentials = AppAssertionCredentials(scope='https://www.googleapis.com/auth/bigquery ' + 'https://www.googleapis.com/auth/devstorage.read_only')

      Context._global_context = Context(project_id, credentials)

    return Context._global_context
