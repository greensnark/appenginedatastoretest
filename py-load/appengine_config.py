def webapp_add_wsgi_middleware(app):
  from google.appengine.ext.appstats import recording
  return recording.appstats_wsgi_middleware(app)
