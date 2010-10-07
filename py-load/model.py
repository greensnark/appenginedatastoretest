from google.appengine.ext import db, deferred
from google.appengine.api import datastore
from random import randrange
import logging

MODEL_CLASS = 'Person'
MODEL_FIELDS = { 'firstname': 8,
                 'lastname': 8,
                 'address': 20,
                 'city': 10,
                 'state': 2,
                 'zip': 5 }

class InsertStatus(db.Model):
  inserted = db.IntegerProperty(default=0, indexed=False)
  pending = db.IntegerProperty(default=0, indexed=False)
  status = db.StringProperty(default='', indexed=False)

  @classmethod
  def find_existing(cls):
    return cls.all().get()

  @classmethod
  def find_or_create(cls):
    existing = cls.find_existing()
    if not existing:
      existing = cls()
      existing.put()
    return existing


ALPHABETS = "abcdefghijklmnopqrstuvwxyz"

def random_string(charset, length):
  result = []
  nchars = len(charset)
  for i in range(length):
    result.append(charset[randrange(0, nchars)])
  return "".join(result)

def create_entity():
  e = datastore.Entity(MODEL_CLASS)
  for f in MODEL_FIELDS:
    length = MODEL_FIELDS[f]
    e[f] = random_string(ALPHABETS, length)
  e.set_unindexed_properties(MODEL_FIELDS.keys())
  return e

def insert_records(insert_size, n_times):
  if n_times > 0:
    status = InsertStatus.find_or_create()
    if status.status == '':
      status.inserted = 0
      status.pending = n_times * insert_size
      status.status = 'Inserting'
      status.put()

    insert_batch = []
    for i in range(insert_size):
      insert_batch.append(create_entity())
    logging.info("Inserting %d records (batch #%d)"
                 % (len(insert_batch), n_times))
    datastore.Put(insert_batch)
    status.inserted += insert_size
    status.pending -= insert_size
    if n_times > 1:
      logging.info("Deferring further inserts (%d)" % (n_times - 1))
      deferred.defer(insert_records, insert_size, n_times - 1)
    else:
      status.status = ''
    status.put()
