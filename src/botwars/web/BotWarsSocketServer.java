package botwars.web;

import java.net.InetSocketAddress;
import java.util.Map;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.common.collect.Maps;
import botwars.compute.api.BotWarsAPI;
import botwars.compute.model.GameTable;
import botwars.compute.model.Player;
import botwars.compute.service.GameWorld;
import ox.Json;
import ox.Log;

public class BotWarsSocketServer extends WebSocketServer {

  public static final int PORT = 39141;

  private final Map<WebSocket, Player> players = Maps.newConcurrentMap();

  private final GameWorld world = GameWorld.get();
  private final BotWarsAPI api = new BotWarsAPI();

  public BotWarsSocketServer() {
    super(new InetSocketAddress(PORT));
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake handshake) {
    Player player = new Player(socket);
    players.put(socket, player);

    Log.info("Client connected!");

    Json tables = Json.object();
    for (GameTable table : world.idTables.values()) {
      tables.with(table.id + "", table.toJson());
    }

    player.send(Json.object().with("command", "tables").with("tables", tables));
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    Player player = players.get(conn);

    Json json = new Json(message);
    String command = json.get("command");
    if (command.equals("sit")) {
      String name = json.get("name");
      api.checkName(name);
      player.name = name;
      world.sit(player);
    } else {
      Log.warn("Unknown command: " + command);
    }
  }

  private void sendToAll(Json json) {
    sendToAll(json, null);
  }

  private void sendToAll(Json json, Player exception) {
    String s = json.toString();
    for (Player player : players.values()) {
      if (player == exception) {
        continue;
      }
      try {
        player.send(s);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    Player player = players.remove(conn);

    Log.info(player + " has left the server.");
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
  }

}
