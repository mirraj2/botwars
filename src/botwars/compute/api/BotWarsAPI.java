package botwars.compute.api;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.CharMatcher;
import botwars.compute.model.GameTable;
import botwars.compute.model.Player;
import botwars.compute.service.GameWorld;
import bowser.Controller;
import bowser.Handler;
import ox.Json;
import ox.util.Utils;

public class BotWarsAPI extends Controller {

  private GameWorld world = GameWorld.get();

  @Override
  public void init() {
    route("GET", "/tables").to(getTables);
    route("POST", "/sit").to(sit);
  }

  private final Handler getTables = (request, response) -> {
    Json json = Json.array();
    for (GameTable table : world.tables) {
      json.add(Json.object()
          .with("id", table.id));
    }
    response.write(json);
  };

  private final Handler sit = (request, response) -> {
    String name = request.param("name");
    checkNotNull(name, "You must supply a name!");

    if (!CharMatcher.JAVA_LETTER_OR_DIGIT.matchesAllOf(name)) {
      throw new IllegalStateException("Your name must only have letters and digits.");
    }

    checkState(name.length() >= 3, "Your name must be at least 3 characters.");
    checkState(name.length() <= 12, "Your name cannot be longer than 12 characters.");

    for (GameTable table : world.tables) {
      for (Player player : table.players) {
        if (player.name.equalsIgnoreCase(name)) {
          throw new IllegalStateException("There is already a player with this name sitting at table " + table.id);
        }
      }
    }

    Player player = new Player(name);

    GameTable last = Utils.last(world.tables);
    if (last.isFull()) {
      last = world.newTable();
    }

    last.sit(player);

    response.write(Json.object().with("table", last.id));
  };

}
