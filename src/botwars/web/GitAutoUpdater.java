package botwars.web;

import java.io.IOException;
import java.util.List;
import bowser.Controller;
import bowser.Handler;
import ox.Json;
import ox.Log;

public class GitAutoUpdater extends Controller {

  private String keyword = "#deploy";
  private String updatePath;

  public GitAutoUpdater(String updatePath) {
    this.updatePath = updatePath;
  }

  @Override
  public void init() {
    route("POST", "/updateServer").to(updateServer);
  }

  // TODO secure this route to verify it is coming from github
  private Handler updateServer = (request, response) -> {
    Json json = new Json(request.getContent());

    String ref = json.get("ref"); // "ref": "refs/heads/test"

    if (ref == null || !ref.equals("refs/heads/master")) {
      Log.info("Not a master branch update: " + ref);
      return;
    }

    List<Json> commits = json.getJson("commits").asJsonArray();

    // see if any commit message has the deployment keyword
    for (Json commit : commits) {
      if (commit.get("message").toLowerCase().contains(keyword)) {
        restart();
        return;
      }
    }

    Log.debug("It was not a " + keyword + " commit.");
  };

  private void restart() {
    try {
      Log.info("Restarting server.");
      Runtime.getRuntime().exec(updatePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
