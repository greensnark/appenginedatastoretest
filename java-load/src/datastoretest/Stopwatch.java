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

import java.util.ArrayList;
import java.util.List;

public class Stopwatch {
  private List<Long> trials = new ArrayList<Long>();
  private long min = -1L;
  private long max = -1L;
  private long total = 0L;
  private long average = -1L;
  private long stdDev = 0L;
  private boolean calculatedStats = false;

  public void measureTime(Runnable task) {
    final long startTime = System.currentTimeMillis();
    task.run();
    final long endTime = System.currentTimeMillis();
    final long elapsedTime = endTime - startTime;
    recordTime(elapsedTime);
  }

  private void recordTime(long elapsedTime) {
    trials.add(elapsedTime);
    if (elapsedTime < min || min == -1)
      min = elapsedTime;
    if (elapsedTime > max)
      max = elapsedTime;
    total += elapsedTime;
  }

  public long getMin() {
    return min;
  }

  public long getMax() {
    return max;
  }

  public long getTotal() {
    return total;
  }

  public long getAverage() {
    calcStats();
    return average;
  }

  public long getStdDev() {
    calcStats();
    return stdDev;
  }

  private void calcStats() {
    if (calculatedStats)
      return;
    calculatedStats = true;
    average = total / trials.size();

    long variance = 0L;
    for (long time : trials) {
      final long delta =  time - average;
      variance += delta * delta;
    }
    stdDev = (long)Math.sqrt(variance / trials.size());
  }
}