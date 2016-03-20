package botwars.compute.model;

import static com.google.common.base.Preconditions.checkState;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import ox.Json;
import ox.Log;

public class GameTable {

  public static final int MAX_PLAYERS = 10;
  public static final int STARTING_CHIPS = 1000;
  public static final int BLIND_AMOUNT = 10;

  private static final AtomicInteger idCounter = new AtomicInteger();

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

  public Json toJson(String token) {
    Json json = Json.object()
        .with("id", id);

    Json players = Json.array(Arrays.asList(this.players),
        p -> {
          Json ret = Json.object()
              .with("name", p.name)
              .with("chips", p.chips)
              .with("status", p.status);
          if (p.isActive()) {
            ret.with("betAmount", p.betAmount);
            if (p.token.equals(token)) {
              ret.with("hand", p.hand.toJson());
            }
          }
          return ret;
        });

    json.with("players", players)
        .with("blindAmount", model.blindAmount)
        .with("potSize", model.potSize)
        .with("board", Json.array(model.board, Card::toShortString))
        .with("dealerIndex", model.dealerIndex);

    return json;
  }

}
