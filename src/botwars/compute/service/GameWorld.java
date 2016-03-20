package botwars.compute.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Maps;
import botwars.compute.arch.Threads;
import botwars.compute.model.GameTable;
import botwars.compute.model.Player;
import ox.Log;
import ox.util.Utils;

public class GameWorld {

  private static final GameWorld instance = new GameWorld();

  public final Map<Integer, GameTable> idTables = Maps.newLinkedHashMap();
  public final Map<String, GameTable> tokenTables = Maps.newConcurrentMap();

  private GameWorld() {
    newTable();

    Threads.every(200, TimeUnit.MILLISECONDS).run(this::tick);
  }

  public void onHandFinished(GameTable table) {
    reviveDeadPlayers(table);
    promotePlayers(table);
  }

  private void promotePlayers(GameTable table) {
    GameTable parent = idTables.get(table.id - 1);
    if (parent == null) {
      return;
    }

    List<Player> players = table.getPlayers();
    Collections.sort(players, (a, b) -> b.chips - a.chips);
    while (!parent.isFull() && !players.isEmpty()) {
      Player p = players.remove(0);
      Log.info("Promoting " + p + " to table " + parent.id);
      table.stand(p);
      parent.sit(p);
    }
  }

  private void reviveDeadPlayers(GameTable table) {
    for (Player player : table.getPlayers()) {
      if (player.chips < GameTable.BLIND_AMOUNT) {
        Log.info(player + " has run out of money.");
        table.stand(player);
        sit(player);
      }
    }
  }

  public Collection<GameTable> getTables() {
    return idTables.values();
  }

  private void tick() {

  }

  public GameTable sit(Player player) {
    GameTable table = Utils.last(getTables());
    if (table.isFull()) {
      table = newTable();
    }
    table.sit(player);
    return table;
  }

  private GameTable newTable() {
    GameTable ret = new GameTable();
    Log.info("Created table " + ret.id);
    idTables.put(ret.id, ret);
    return ret;
  }

  public static GameWorld get() {
    return instance;
  }

}
