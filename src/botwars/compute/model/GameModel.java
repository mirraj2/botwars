package botwars.compute.model;

import static com.google.common.base.Preconditions.checkState;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Lists;
import ox.Log;

public class GameModel {

  private static final AtomicInteger counter = new AtomicInteger();

  public final int id = counter.incrementAndGet();

  public final Player[] players;
  public int numPlayers = 0;
  public final int blindAmount;

  public final Deck deck = new Deck();
  public final List<Card> board = Lists.newArrayList();
  public int dealerIndex;
  public int potSize = 0;

  private int currentIndex;

  public GameModel(GameModel last, Player[] players, int blindAmount) {
    Log.info("New hand has begun: " + id);

    this.blindAmount = blindAmount;
    this.players = players;

    for (Player player : players) {
      if (player != null) {
        player.status = Player.Status.ACTIVE;
        numPlayers++;
      }
    }

    assignDealer(last);
    payBlinds();
    drawStartingHands();
  }

  private void pay(Player player, int amount) {
    checkState(player.chips >= amount, "Not enough chips.");
    player.chips -= amount;
    player.betAmount += amount;
    potSize += amount;
  }

  private void assignDealer(GameModel last) {
    currentIndex = last == null ? -1 : last.dealerIndex;
    dealerIndex = nextIndex();
  }

  private void payBlinds() {
    currentIndex = dealerIndex;
    if (numPlayers == 2) {
      pay(players[dealerIndex], blindAmount / 2);
      pay(nextPlayer(), blindAmount);
    } else {
      pay(nextPlayer(), blindAmount / 2);
      pay(nextPlayer(), blindAmount);
    }
  }

  private void drawStartingHands() {
    for (Player player : players) {
      if (player != null) {
        player.hand = new Hand(deck.draw(), deck.draw());
      }
    }
  }

  private Player nextPlayer() {
    return players[nextIndex()];
  }

  private int nextIndex() {
    for (int i = 1; i < players.length; i++) {
      int k = (currentIndex + i) % players.length;
      Player player = players[k];
      if (player != null && player.isActive()) {
        currentIndex = k;
        return currentIndex;
      }
    }
    throw new IllegalStateException("There is no other player left!");
  }

}
