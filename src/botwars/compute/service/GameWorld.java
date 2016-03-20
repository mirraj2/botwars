package botwars.compute.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Maps;
import botwars.compute.arch.Threads;
import botwars.compute.model.GameTable;
import ox.Log;

public class GameWorld {

  private static final GameWorld instance = new GameWorld();

  public final Map<Integer, GameTable> idTables = Maps.newLinkedHashMap();

  private GameWorld() {
    newTable();

    Threads.every(200, TimeUnit.MILLISECONDS).run(this::tick);
  }

  public Collection<GameTable> getTables() {
    return idTables.values();
  }

  private void tick() {

  }

  public GameTable newTable() {
    GameTable ret = new GameTable();
    Log.info("Created table " + ret.id);
    idTables.put(ret.id, ret);
    return ret;
  }

  public static GameWorld get() {
    return instance;
  }

}
