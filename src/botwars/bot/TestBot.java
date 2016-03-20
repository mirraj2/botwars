package botwars.bot;

import botwars.compute.arch.Threads;
import ox.HttpRequest;
import ox.Json;
import ox.Log;

public class TestBot {

  private final String ip = "http://localhost:8080";

  public void run() {
    Threads.run(() -> {
      try {
        String t = System.nanoTime() + "";
        Json json = HttpRequest.post(ip + "/sit", true, "name", "bot" + t.substring(t.length() - 9, t.length()))
            .checkStatus().json();

        String token = json.get("token");
        int table = json.getInt("table");

        Json tableJson = HttpRequest.get(ip + "/table/" + table, true, "token", token).checkStatus().json();
        
        Log.debug(tableJson);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public static void main(String[] args) {
    for (int i = 0; i < 2; i++) {
      new TestBot().run();
    }
  }

}
