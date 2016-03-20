package botwars.web.home;

import bowser.Controller;

public class HomePage extends Controller {

  @Override
  public void init() {
    route("GET", "/").to("home.html");
  }

}
