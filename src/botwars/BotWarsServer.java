package botwars;

import java.io.File;
import botwars.web.home.HomePage;
import bowser.WebServer;
import ox.Config;
import ox.Log;
import ox.OS;

public class BotWarsServer {

  private final Config config = Config.load("botwars");

  public void run() {
    boolean devMode = config.getBoolean("dev_mode", false);

    int httpPort = config.getInt("port", devMode ? 8080 : 443);
    WebServer server = new WebServer("BotWars", httpPort, devMode)
        .controller(new HomePage());
    server.start();

    Log.info("BotWars Server started on port " + httpPort);
  }

  public static void main(String[] args) {
    Log.logToFolder(new File(OS.getHomeFolder(), "log"));

    new BotWarsServer().run();
  }

}
