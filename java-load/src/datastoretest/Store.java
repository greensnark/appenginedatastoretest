package datastoretest;

import com.google.appengine.api.datastore.Entity;
import java.util.HashMap;
import java.util.Random;

public class Store {
  public static final String MODEL_CLASS = "Person";
  public static final HashMap<String, Integer> MODEL_FIELDS =
    new HashMap<String, Integer>();

  public static final int INSERT_BATCH_SIZE = 500;
  public static final int INSERT_TRIALS = 20;

  public static final int QUERY_BATCH_SIZE = 1000;
  public static final int QUERY_TRIALS = 20;

  private Random random = new Random();

  static {
    MODEL_FIELDS.put("firstname", 8);
    MODEL_FIELDS.put("lastname", 8);
    MODEL_FIELDS.put("address", 20);
    MODEL_FIELDS.put("city", 10);
    MODEL_FIELDS.put("state", 2);
    MODEL_FIELDS.put("zip", 5);
  }

  public int random(int bound) {
    return random.nextInt(bound);
  }

  public String generateField(int length) {
    final StringBuilder field = new StringBuilder();
    final String characters = "abcdefghijklmnopqrstuvwxyz";
    for (int i = 0; i < length; ++i)
      field.append(characters.charAt(random(characters.length())));
    return field.toString();
  }

  public Entity createEntity() {
    final Entity e = new Entity(MODEL_CLASS);
    for (String key : MODEL_FIELDS.keySet()) {
      final int length = MODEL_FIELDS.get(key);
      e.setProperty(key, generateField(length));
    }
    return e;
  }
}