package botwars.web.home;

import botwars.web.BotWarsSocketServer;
import bowser.Controller;
import bowser.template.Data;

public class HomePage extends Controller {

  @Override
  public void init() {
    route("GET", "/").to("home.html").data(data);
  }

  private final Data data = context -> {
    context.put("serverIP", "localhost");
    context.put("serverPort", BotWarsSocketServer.PORT);
  };

}
