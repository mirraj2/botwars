package botwars.compute.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import botwars.compute.service.GameWorld;
import ox.Json;
import ox.Log;

public class GameTable {

  public static final int MAX_PLAYERS = 10;
  public static final int STARTING_CHIPS = 1000;
  public static final int BLIND_AMOUNT = 10;

  private static final AtomicInteger idCounter = new AtomicInteger();

  public final int id = idCounter.incrementAndGet();

  public final Player[] players = new Player[MAX_PLAYERS];
  private Map<String, Player> tokenPlayers = Maps.newHashMap();

  private GameModel model;

  private long version = 0;

  private void newHand() {
    checkState(tokenPlayers.size() >= 2, "Can't start a game with less than two players.");

    for (Player player : getPlayers()) {
      player.status = Player.Status.PLAYING;
    }

    model = new GameModel(model, players, BLIND_AMOUNT);
    Log.info("Starting a new game on table " + id + "  Hand #" + model.id);
    Log.info("Dealer in position " + model.dealerIndex);
  }

  public boolean isFull() {
    return tokenPlayers.size() >= players.length;
  }

  public boolean isEmpty() {
    return tokenPlayers.isEmpty();
  }

  public synchronized void act(String token, int bet) {
    Player player = tokenPlayers.get(token);
    checkNotNull(player, "No such player for token: " + token);
    checkState(player.seatIndex == model.currentIndex,
        "It is not your turn. " + player.seatIndex + " vs " + model.currentIndex);

    model.act(bet);

    if (model.finished) {
      GameWorld.get().onHandFinished(this);

      if (tokenPlayers.size() >= 2) {
        newHand();
      }
    }

    version++;
  }

  public synchronized void sit(Player player) {
    checkState(!isFull());

    for (int i = 0; i < players.length; i++) {
      if (players[i] == null) {
        players[i] = player;
        player.seatIndex = i;
        tokenPlayers.put(player.token, player);
        GameWorld.get().tokenTables.put(player.token, this);
        player.betAmount = 0;
        player.totalBetAmount = 0;
        player.chips = STARTING_CHIPS;
        player.status = Player.Status.SITTING_OUT;
        break;
      }
    }

    if ((model == null || model.finished) && tokenPlayers.size() >= 2) {
      newHand();
    }

    version++;
  }

  public synchronized void stand(Player player) {
    checkState(players[player.seatIndex] == player);
    players[player.seatIndex] = null;
    player.seatIndex = -1;
    tokenPlayers.remove(player.token);
    GameWorld.get().tokenTables.remove(player.token);

    version++;
  }

  public List<Player> getPlayers() {
    List<Player> ret = Lists.newArrayList();
    for (Player p : players) {
      if (p != null) {
        ret.add(p);
      }
    }
    return ret;
  }

  public byte[] getState(String token) {
    Player myPlayer = tokenPlayers.get(token);
    if (myPlayer.version == version) {
      return myPlayer.state;
    }
    Json j = toJson(token);
    myPlayer.version = version;
    myPlayer.state = j.asByteArray();
    return myPlayer.state;
  }

  private boolean isMyTurn(Player player) {
    return model != null && !model.finished && player != null && model.currentIndex == player.seatIndex
        && player.isActive();
  }

  public Json toJson(String token) {
    Json json = Json.object()
        .with("id", id);

    Json players = Json.array(Arrays.asList(this.players),
        p -> {
          if (p == null) {
            return null;
          }
          Json ret = Json.object()
              .with("name", p.name)
              .with("chips", p.chips)
              .with("status", p.status);
          if (p.isActive()) {
            ret.with("betAmount", p.betAmount);
            if (p.hand != null && p.token.equals(token)) {
              ret.with("hand", p.hand.toJson());
            }
          }
          return ret;
        });

    json.with("players", players);

    if (model != null) {
      json.with("blindAmount", model.blindAmount)
          .with("potSize", model.potSize)
          .with("board", Json.array(model.board, Card::toShortString))
          .with("dealerIndex", model.dealerIndex)
          .with("activeIndex", model.currentIndex);
    }

    Player myPlayer = tokenPlayers.get(token);
    boolean myTurn = isMyTurn(myPlayer);
    if (myTurn) {
      // make sure that there are no race conditions.
      synchronized (this) {
        myTurn = isMyTurn(myPlayer);
      }
    }
    json.with("myTurn", myTurn);

    if (myTurn) {
      json.with("amountToCall", model.totalAmountToCall - myPlayer.betAmount);
    }

    return json;
  }

}
