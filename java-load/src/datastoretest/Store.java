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

import com.google.appengine.api.datastore.Entity;
import java.util.HashMap;
import java.util.Random;

public class Store {
  public static final String MODEL_CLASS = "Person";
  public static final HashMap<String, Integer> MODEL_FIELDS =
    new HashMap<String, Integer>();

  public static final int INSERT_BATCH_SIZE = 500;
  public static final int INSERT_TRIALS = 4;

  public static final int QUERY_BATCH_SIZE = 1000;
  public static final int QUERY_TRIALS = 5;

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
      e.setUnindexedProperty(key, generateField(length));
    }
    return e;
  }
}