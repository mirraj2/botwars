package botwars.compute.arch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Threads {

  private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(16);

  public static void run(Runnable r) {
    pool.execute(r);
  }

  public static ThreadBuilder every(long n, TimeUnit unit) {
    ThreadBuilder ret = new ThreadBuilder();
    ret.n = n;
    ret.unit = unit;
    return ret;
  }

  private static void run(ThreadBuilder builder) {
    if (builder.unit == null) {
      pool.execute(builder.r);
    } else {
      pool.scheduleAtFixedRate(builder.r, 0, builder.n, builder.unit);
    }
  }

  private static Runnable wrap(Runnable r) {
    return () -> {
      try {
        r.run();
      } catch (Throwable t) {
        t.printStackTrace();
      }
    };
  }

  public static class ThreadBuilder {
    public Runnable r;
    public long n;
    public TimeUnit unit;

    public void run(Runnable r) {
      this.r = wrap(r);
      Threads.run(this);
    }
  }

}
