package guestbook;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;

public class GuestbookServlet extends HttpServlet {
  private static int getDatastoreQueryCount(String queryCountString,
                                            int defaultCount)
  {
    if (queryCountString == null)
      return defaultCount;
    try {
      final int parsedQueryCount = Integer.parseInt(queryCountString);
      if (parsedQueryCount >= 1 && parsedQueryCount < 1000000)
        return parsedQueryCount;
    } catch (NumberFormatException ignored) { }
    return defaultCount;
  }

  private List<Entity> runQuery(int entityCount) {
    final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    final Query q = new Query("Logline");
    if (q.isKeysOnly())
      throw new RuntimeException("MOO");
    final PreparedQuery pq = ds.prepare(q);
    final List<Entity> entityList = pq.asList(withLimit(entityCount));
    return entityList;
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws IOException
  {
    final String queryCountString = req.getParameter("q");
    final int queryCount = getDatastoreQueryCount(queryCountString, 1000);

    final long queryStartTime = System.currentTimeMillis();
    final List<Entity> entities = runQuery(queryCount);
    final long queryEndTime = System.currentTimeMillis();
    res.getWriter().println("Ran query for " + queryCount
                            + " entities in "
                            + (queryEndTime - queryStartTime) + "ms; "
                            + "query fetched " + entities.size() + " entities");
  }
}