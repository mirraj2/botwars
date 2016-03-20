package botwars.bot;

import static ox.util.Utils.sleep;
import botwars.compute.arch.Threads;
import ox.HttpRequest;
import ox.Json;

public class TestBot {

  private final String ip = "http://localhost:8080";

  public void run(int n, boolean debug) {
    Threads.run(() -> {
      try {
        Json json = HttpRequest.post(ip + "/sit", true, "name", "bot" + n)
            .checkStatus().json();

        String token = json.get("token");

        while (true) {
          try {
            json = HttpRequest.get(ip + "/table", true, "token", token).checkStatus().json();
            if (debug) {
              // Log.debug(json.prettyPrint());
            }
            if (json.getBoolean("myTurn")) {
              double r = Math.random();
              int bet;
              if (r < .5) {
                bet = 0;
              } else if (r < .7) {
                bet = json.getInt("amountToCall");
              } else if (r < .9) {
                bet = json.getInt("amountToCall") * 2;
              } else {
                bet = json.getInt("amountToCall") * 4;
              }
              HttpRequest.post(ip + "/act", true, "token", token, "bet", bet).checkStatus();
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            sleep(100);
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      new TestBot().run(i, i == 0);
      sleep(100);
    }
  }

}
