package botwars.compute.service;

import java.util.List;
import com.google.common.collect.Lists;
import botwars.compute.model.GameTable;
import ox.Log;

public class GameWorld {

  private static final GameWorld instance = new GameWorld();

  public final List<GameTable> tables = Lists.newArrayList();

  private GameWorld() {
    Log.info("Creating the first table.");
    tables.add(new GameTable());
  }

  public GameTable newTable() {
    GameTable ret = new GameTable();
    Log.info("Created table " + ret.id);
    tables.add(ret);
    return ret;
  }

  public static GameWorld get() {
    return instance;
  }

}
