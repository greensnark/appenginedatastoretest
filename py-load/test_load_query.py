# Copyright (c) 2010 Darshan Shaligram
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

from __future__ import with_statement

import os
import logging

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db, deferred
from google.appengine.ext.webapp import template
from google.appengine.api import datastore
from datetime import datetime, timedelta
import math
import model

INSERT_BATCH_SIZE = 100
INSERT_TRIALS = 20

QUERY_BATCH_SIZE = 1000
QUERY_TRIALS = 5

def timedelta_millis(delta):
  return (delta.days * 24 * 60 * 60 * 1000 + delta.seconds * 1000
          + int(delta.microseconds / 1000))

class Stopwatch(object):
  """Gathers timing data for a series of operations and reports stats
  on request."""
  def __init__(self):
    self.times = []
    self.total = None
    self.start = None
    self.calculated_stats = False

  def __enter__(self):
    self.start = datetime.now()

  def __exit__(self, exc_type, exc_value, exc_tb):
    self.end = datetime.now()
    self.elapsed_time = self.end - self.start
    self._record()

  def _record(self):
    millis = timedelta_millis(self.elapsed_time)
    self.times.append(millis)
    if self.total is None:
      self.total = millis
    else:
      self.total += millis

  def average(self):
    self._calc_stats()
    return self._average

  def std_dev(self):
    self._calc_stats()
    return self._std_dev

  def min(self):
    self._calc_stats()
    return self._min

  def max(self):
    self._calc_stats()
    return self._max

  def _calc_stats(self):
    if self.calculated_stats:
      return
    self.calculated_stats = True
    self._min = min(self.times)
    self._max = max(self.times)
    self._average = self.total / len(self.times)
    variance = 0
    for t in self.times:
      delta = t - self._average
      variance += delta * delta
    self._std_dev = math.sqrt(variance / len(self.times))

# Query records and report times.
def query_timings(fetch_size, n_times):
  timer = Stopwatch()
  nfetched = 0
  for i in range(n_times):
    logging.info("Querying %d records (batch %d/%d)"
                 % (fetch_size, i + 1, n_times))
    with timer:
      q = datastore.Query(model.MODEL_CLASS)
      objects = list(q.Get(fetch_size))
      nfetched += len(objects)
  return nfetched, timer

# Insert records and report times
def insert_timings(insert_size, n_times):
  timer = Stopwatch()
  for i in range(n_times):
    insert_batch = []
    for i in range(insert_size):
      insert_batch.append(create_entity())
    logging.info("Inserting %d records (batch %d/%d)"
                 % (len(insert_batch), i + 1, n_times))
    with timer:
      datastore.Put(insert_batch)
  return timer

def delete_entities():
  while True:
    q = datastore.Query(model.MODEL_CLASS)
    objects = q.Get(500)
    if not objects:
      break
    logging.info("Deleting %d objects of %s" % (len(objects), MODEL_CLASS))
    datastore.Delete(objects)

class BasePage(webapp.RequestHandler):
  def resolve_template(self, page):
    path = os.path.join(os.path.dirname(__file__), page)
    return path

  def render_page(self, pagename, **parameters):
    template_path = self.resolve_template(pagename)
    self.response.out.write(template.render(template_path, parameters))

class MainPage(BasePage):
  def get(self):
    self.render_page('index.html')

class QueryPage(BasePage):
  def get(self):
    nfetched, timings = query_timings(QUERY_BATCH_SIZE, QUERY_TRIALS)
    self.render_page('querystats.html',
                     timings=timings,
                     nfetched=nfetched,
                     batch_size=QUERY_BATCH_SIZE,
                     trials=QUERY_TRIALS)

class InsertPage(BasePage):
  def post(self):
    model.insert_records(INSERT_BATCH_SIZE, INSERT_TRIALS)
    self.redirect('/insert_status')

class InsertStatusPage(BasePage):
  def get(self):
    status = model.InsertStatus.find_existing()
    if not status or not status.status:
      self.response.out.write("Insert status: not inserting")
    else:
      self.response.out.write("Insert status: %d inserted, %d pending (%s)"
                              % (status.inserted, status.pending,
                                 status.status))

class DeletePage(BasePage):
  def post(self):
    delete_entities()
    self.response.out.write("Deleted entities from store")

  def get(self):
    return self.post()

application = webapp.WSGIApplication([('/', MainPage),
                                      ('/query', QueryPage),
                                      ('/insert', InsertPage),
                                      ('/insert_status', InsertStatusPage),
                                      ('/delete', DeletePage)],
                                     debug=False)

def main():
  run_wsgi_app(application)

if __name__ == '__main__':
  main()
