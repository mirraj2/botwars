package botwars.compute.model;

import static com.google.common.base.Preconditions.checkState;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Lists;

public class GameTable {

  public static final int MAX_PLAYERS = 10;

  private final AtomicInteger idCounter = new AtomicInteger();

  public final int id = idCounter.incrementAndGet();
  public List<Player> players = Lists.newArrayList();

  public boolean isFull() {
    return players.size() >= MAX_PLAYERS;
  }

  public void sit(Player player) {
    checkState(!isFull());

    players.add(player);
  }

}
