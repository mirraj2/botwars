package botwars.compute.api;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import com.google.common.base.CharMatcher;
import com.google.common.base.Throwables;
import botwars.compute.model.GameTable;
import botwars.compute.model.Player;
import botwars.compute.service.GameWorld;
import bowser.Controller;
import bowser.Handler;
import ox.Json;

public class BotWarsAPI extends Controller {

  private GameWorld world = GameWorld.get();

  @Override
  public void init() {
    route("GET", "/tables").to(getTables);
    route("POST", "/sit").to(sit);
    route("GET", "/table").to(getMyTable);
    route("GET", "/table/*").to(getTable);
    route("POST", "/act").to(act);
  }

  private final Handler act = (request, response) -> {
    String token = request.param("token");

    checkNotNull(token, "You must supply your token.");

    GameTable table = world.tokenTables.get(token);

    String s = request.param("bet");
    checkNotNull(s, "You must supply a 'bet'.");

    int bet = parseInt(s.replace("$", ""));

    table.act(token, bet);
  };

  private final Handler getMyTable = (request, response) -> {
    String token = request.param("token");
    checkNotNull(token, "You must supply your token.");

    GameTable table = world.tokenTables.get(token);

    if (table == null) {
      response.write(Json.object());
    } else {
      try {
        response.contentType("application/json");
        response.getOutputStream().write(table.getState(token));
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  };

  private final Handler getTable = (request, response) -> {
    int tableId = request.getInt(1);
    GameTable table = world.idTables.get(tableId);
    response.write(table.toJson(request.param("token")));
  };

  private final Handler getTables = (request, response) -> {
    Json json = Json.array();
    for (GameTable table : world.getTables()) {
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

    Player player;
    GameTable table;

    synchronized (world) {
      for (GameTable t : world.getTables()) {
        for (Player p : t.players) {
          if (p != null && p.name.equalsIgnoreCase(name)) {
            throw new IllegalStateException("There is already a player with this name sitting at table " + t.id);
          }
        }
      }

      player = new Player(name);
      table = world.sit(player);
    }

    response.write(Json.object()
        .with("token", player.token)
        .with("table", table.id));
  };

}
