package botwars.compute.model;

import static com.google.common.base.Preconditions.checkState;
import java.util.concurrent.atomic.AtomicInteger;
import ox.Log;

public class GameTable {

  public static final int MAX_PLAYERS = 10;
  public static final int STARTING_CHIPS = 1000;
  public static final int BLIND_AMOUNT = 10;

  private final AtomicInteger idCounter = new AtomicInteger();

  public final int id = idCounter.incrementAndGet();

  public final Player[] players = new Player[MAX_PLAYERS];
  private int playerCount = 0;

  private GameModel model;

  private void startNewGame() {
    Log.info("Starting a new game on table " + id);

    model = new GameModel(model, players, BLIND_AMOUNT);
  }

  public boolean isFull() {
    return playerCount >= players.length;
  }

  public void sit(Player player) {
    checkState(!isFull());

    for (int i = 0; i < players.length; i++) {
      if (players[i] == null) {
        players[i] = player;
        playerCount++;
        player.chips = STARTING_CHIPS;
        player.status = Player.Status.SITTING_OUT;
        break;
      }
    }

    if (model == null && playerCount >= 2) {
      startNewGame();
    }
  }

}
