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
import com.google.appengine.api.datastore.Key;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;

public class StoreTestServlet extends HttpServlet {
  public void insertRecords(int n, ArrayList<Entity> entities) {
    final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    final List<Key> keys = ds.put(entities);
    System.out.println("Got " + keys.size() + " keys after store #" + n);
  }

  private ArrayList<Entity> createEntities(int batchSize) {
    final ArrayList<Entity> entities = new ArrayList<Entity>();
    final Store s = new Store();
    for (int i = 0; i < batchSize; ++i)
      entities.add(s.createEntity());
    return entities;
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws IOException
  {
    final int batchSize = Store.INSERT_BATCH_SIZE;
    final int trials = Store.INSERT_TRIALS;

    Stopwatch watch = new Stopwatch();
    for (int i = 0; i < trials; ++i) {
      final ArrayList<Entity> entities = createEntities(batchSize);
      final int n = i;
      watch.measureTime(new Runnable() {
          public void run() {
            insertRecords(n, entities);
          }
        });
    }

    final PrintWriter writer = res.getWriter();
    writer.println("Java insert performance times with batch size: "
                   + batchSize + "; trials: " + trials);
    writer.println("");
    writer.println("Average time: " + watch.getAverage() + " ms");
    writer.println("Max time    : " + watch.getMax() + " ms");
    writer.println("Min time    : " + watch.getMin() + " ms");
    writer.println("Std Dev     : " + watch.getStdDev() + " ms");
  }
}