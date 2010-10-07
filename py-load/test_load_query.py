from __future__ import with_statement

import os
import logging

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from google.appengine.ext.webapp import template
from google.appengine.api import datastore
from random import randrange
from datetime import datetime, timedelta
import math

ALPHABETS = "abcdefghijklmnopqrstuvwxyz"

def random_string(charset, length):
  result = []
  nchars = len(charset)
  for i in range(length):
    result.append(charset[randrange(0, nchars)])
  return "".join(result)

MODEL_CLASS = 'Person'
MODEL_FIELDS = { 'firstname': 8,
                 'lastname': 8,
                 'address': 20,
                 'city': 10,
                 'state': 2,
                 'zip': 5 }

INSERT_BATCH_SIZE = 500
INSERT_TRIALS = 4

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

def create_entity():
  e = datastore.Entity(MODEL_CLASS)
  for f in MODEL_FIELDS:
    length = MODEL_FIELDS[f]
    e[f] = random_string(ALPHABETS, length)
  e.set_unindexed_properties(MODEL_FIELDS.keys())
  return e

# Query records and report times.
def query_timings(fetch_size, n_times):
  timer = Stopwatch()
  nfetched = 0
  for i in range(n_times):
    logging.info("Querying %d records (batch %d/%d)"
                 % (fetch_size, i + 1, n_times))
    with timer:
      q = datastore.Query(MODEL_CLASS)
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
    q = datastore.Query(MODEL_CLASS)
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
    timings = insert_timings(INSERT_BATCH_SIZE, INSERT_TRIALS)
    self.render_page('insertstats.html',
                     timings=timings,
                     batch_size=INSERT_BATCH_SIZE,
                     trials=INSERT_TRIALS)

class DeletePage(BasePage):
  def post(self):
    delete_entities()
    self.response.out.write("Deleted entities from store")

  def get(self):
    return self.post()

application = webapp.WSGIApplication([('/', MainPage),
                                      ('/query', QueryPage),
                                      ('/insert', InsertPage),
                                      ('/delete', DeletePage)],
                                     debug=False)

def main():
  run_wsgi_app(application)

if __name__ == '__main__':
  main()
