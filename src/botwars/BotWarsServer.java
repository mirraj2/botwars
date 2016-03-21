package botwars;

import java.io.File;
import java.util.Optional;
import com.google.common.base.Stopwatch;
import botwars.bot.TestBot;
import botwars.compute.api.BotWarsAPI;
import botwars.web.BotWarsSocketServer;
import botwars.web.GitAutoUpdater;
import botwars.web.home.HomePage;
import botwars.web.include.IncludeController;
import bowser.DefaultWebLogger;
import bowser.Request;
import bowser.Response;
import bowser.WebServer;
import ox.Config;
import ox.Log;
import ox.OS;

public class BotWarsServer {

  private final Config config = Config.load("botwars");

  public void run() {
    boolean devMode = config.getBoolean("dev_mode", false);

    int httpPort = config.getInt("port", devMode ? 8080 : 80);
    WebServer server = new WebServer("BotWars", httpPort, devMode)
        .controller(new BotWarsAPI())
        .controller(new IncludeController())
        .controller(new HomePage())
        .controller(new GitAutoUpdater("/root/botwars/build/update.sh"));

    server.logger(new DefaultWebLogger() {
      @Override
      public void log(Request request, Response response, Optional<Throwable> e, Stopwatch watch) {
        if (request.path.equals("/table") || request.path.equals("/act")) {
          // don't log
        } else {
          super.log(request, response, e, watch);
        }
      }
    });

    new BotWarsSocketServer().start();

    server.start();
    Log.info("BotWars Server started on port " + httpPort);

    if (devMode) {
      TestBot.main(null);
    }
  }

  public static void main(String[] args) {
    Log.logToFolder(new File(OS.getHomeFolder(), "log"));

    new BotWarsServer().run();
  }

}
