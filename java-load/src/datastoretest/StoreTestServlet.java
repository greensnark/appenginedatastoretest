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
  public void insertRecords(ArrayList<Entity> entities) {
    final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    final List<Key> keys = ds.put(entities);
    //System.out.println("Got " + keys.size() + " keys after store");
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
      watch.measureTime(new Runnable() {
          public void run() {
            insertRecords(entities);
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