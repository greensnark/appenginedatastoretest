// Copyright (c) 2010 Darshan Shaligram

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package datastoretest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;

public class QueryTestServlet extends HttpServlet {
  private static int parseInt(String string, int defval,
                              int min, int max)
  {
    if (string == null)
      return defval;
    try {
      final int parsedInt = Integer.parseInt(string);
      if (parsedInt >= min && parsedInt < max)
        return parsedInt;
    } catch (NumberFormatException ignored) { }
    return defval;
  }

  private List<Entity> runQuery(int entityCount) {
    final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    final Query q = new Query(Store.MODEL_CLASS);
    if (q.isKeysOnly())
      throw new RuntimeException("MOO");
    final PreparedQuery pq = ds.prepare(q);
    // Force the query to run and return objects so we can be sure
    // we've timed a full query.
    final List<Entity> entityList =
      new ArrayList<Entity>(pq.asList(withLimit(entityCount)));
    return entityList;
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws IOException
  {
    final String queryCountString = req.getParameter("q");
    final int queryCount = parseInt(queryCountString,
                                     Store.QUERY_BATCH_SIZE, 1, 100000);

    final String queryTrialsString = req.getParameter("qt");
    final int queryTrials = parseInt(queryTrialsString,
                                      Store.QUERY_TRIALS, 1, 1000);

    final ArrayList<Integer> fetchSizes = new ArrayList<Integer>();
    Stopwatch watch = new Stopwatch();
    for (int trial = 0; trial < queryTrials; ++trial) {
      watch.measureTime(new Runnable() {
          public void run() {
            fetchSizes.add(runQuery(queryCount).size());
          }
        });
    }

    long totalFetchSize = 0L;
    for (int fetch : fetchSizes)
      totalFetchSize += fetch;

    final long queryStartTime = System.currentTimeMillis();
    final List<Entity> entities = runQuery(queryCount);
    final long queryEndTime = System.currentTimeMillis();

    res.setContentType("text/plain");
    final PrintWriter writer = res.getWriter();
    writer.println("Java query performance times with batch size: "
                   + queryCount + "; trials: " + queryTrials
                   + "; total records actually fetched: " + totalFetchSize);
    writer.println("");
    writer.println("Average time: " + watch.getAverage() + " ms");
    writer.println("Max time    : " + watch.getMax() + " ms");
    writer.println("Min time    : " + watch.getMin() + " ms");
    writer.println("Std Dev     : " + watch.getStdDev() + " ms");
  }
}