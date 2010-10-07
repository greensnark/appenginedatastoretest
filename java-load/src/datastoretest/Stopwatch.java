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