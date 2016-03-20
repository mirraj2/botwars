package botwars.compute.model;

import static com.google.common.base.Preconditions.checkState;
import static ox.util.Functions.filter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import botwars.compute.service.SlowHandRanker;
import ox.Log;

public class GameModel {

  private static final AtomicInteger counter = new AtomicInteger();

  public final int id = counter.incrementAndGet();

  public final Player[] players;
  public int numPlayers = 0;
  public final int blindAmount;

  public final Deck deck = new Deck();
  public final List<Card> board = Lists.newArrayList();
  public int potSize = 0;
  public int totalAmountToCall;

  public int dealerIndex, lastBetIndex, currentIndex;

  public final Map<Player, Integer> winnings = Maps.newLinkedHashMap();

  public boolean finished = false;

  public GameModel(GameModel last, Player[] players, int blindAmount) {
    this.blindAmount = blindAmount;
    this.players = players;
    this.totalAmountToCall = blindAmount;

    for (Player player : players) {
      if (player != null) {
        player.betAmount = 0;
        player.totalBetAmount = 0;
        player.hand = null;
        player.status = Player.Status.PLAYING;
        numPlayers++;
      }
    }

    // assign the dealer
    currentIndex = last == null ? -1 : last.dealerIndex;
    dealerIndex = nextIndex(false);

    payBlinds();
    drawStartingHands();
  }

  public void act(int bet) {
    checkState(!finished, "This game is already over!");
    checkState(bet >= 0, "Bet cannot be negative.");

    Player current = currentPlayer();

    if (bet >= current.chips) {
      allIn();
    } else {
      if (current.betAmount + bet < totalAmountToCall) {
        fold();
        checkForWinner();
        if (finished) {
          return;
        }
      } else if (current.betAmount + bet == totalAmountToCall) {
        call();
      } else {
        raise(bet);
      }
    }

    if (nextIndex(true) == lastBetIndex) {
      nextRound();
    }
  }

  private void allIn() {
    Player current = currentPlayer();
    if (current.betAmount + current.chips > totalAmountToCall) {
      lastBetIndex = currentIndex;
    }
    pay(current, current.chips);
    Log.info(current + " went ALL IN with " + current.betAmount);
  }

  private void fold() {
    Player current = currentPlayer();
    current.betAmount = 0;
    current.status = Player.Status.FOLDED;
    Log.info(current + " folded.");
  }

  private void call() {
    Player current = currentPlayer();
    int bet = totalAmountToCall - current.betAmount;
    pay(current, bet);
    if (bet == 0) {
      Log.info(current + " checked.");
    } else {
      Log.info(current + " called.");
    }
  }

  private void raise(int bet) {
    Player current = currentPlayer();
    int total = current.betAmount + bet;
    if (total < totalAmountToCall * 2) {
      // they didn't raise at least to twice the current bet.
      call();
      return;
    }
    pay(current, bet);
    lastBetIndex = currentIndex;
    totalAmountToCall = current.betAmount;
    if (totalAmountToCall == 0) {
      Log.info(current + " bet " + bet);
    } else {
      Log.info(current + " raised to " + current.betAmount);
    }
  }

  private void pay(Player player, int amount) {
    checkState(player.chips >= amount, "Not enough chips.");
    player.chips -= amount;
    player.betAmount += amount;
    player.totalBetAmount += amount;
    potSize += amount;
  }

  private void checkForWinner() {
    Player winner = null;
    for (Player player : getActivePlayers()) {
      if (winner == null) {
        winner = player;
      } else {
        return;
      }
    }

    winnings.put(winner, potSize);

    Log.info(winner + " is the only player left and has won $" + potSize);

    allocateWinnings();
  }

  private void showdown() {
    Log.info("Showdown!");

    List<Player> players = getActivePlayers();
    Collections.sort(players, (a, b) -> a.totalBetAmount - b.totalBetAmount);

    Map<Player, HandRank> ranks = Maps.toMap(players, p -> SlowHandRanker.determineRank(p.hand, board));

    int potNumber = 0;

    while (!players.isEmpty()) {
      int sidepot = 0;
      int minBet = players.get(0).totalBetAmount;
      for (Player player : players) {
        int amount = Math.min(player.totalBetAmount, minBet);
        sidepot += amount;
        player.totalBetAmount -= amount;
      }

      List<Player> winners = determineWinners(players, ranks);
      int winningsPerPlayer = sidepot / winners.size();
      for (Player winner : winners) {
        winnings.put(winner, winnings.getOrDefault(winner, 0) + winningsPerPlayer);
      }

      if (potNumber == 0) {
        Log.info(winners + " won the main pot of $" + sidepot);
      } else {
        Log.info(winners + " won side-pot " + potNumber + " of $" + sidepot);
      }
      for (Player winner : winners) {
        Log.info(winner + " had " + ranks.get(winner));
      }
      potNumber++;

      players = filter(players, p -> p.totalBetAmount > 0);
    }

    allocateWinnings();
  }

  private List<Player> determineWinners(List<Player> players, Map<Player, HandRank> ranks) {
    players = Lists.newArrayList(players);

    Collections.sort(players, (a, b) -> ranks.get(b).compareTo(ranks.get(a)));

    HandRank winningHand = ranks.get(players.get(0));

    return filter(players, p -> ranks.get(p).compareTo(winningHand) == 0);
  }

  private void allocateWinnings() {
    winnings.forEach((player, amount) -> player.chips += amount);
    finished = true;
  }

  private void nextRound() {
    if (board.size() == 5) {
      showdown();
      return;
    }

    for (Player player : players) {
      if (player != null) {
        player.betAmount = 0;
      }
    }

    if (board.isEmpty()) {
      board.addAll(deck.draw(3));
      Log.info("Revealing the flop. " + board);
    } else {
      board.add(deck.draw());
      if (board.size() == 3) {
        Log.info("Revealing the turn. " + board);
      } else {
        Log.info("Revealing the river. " + board);
      }
    }

    currentIndex = dealerIndex;
    nextIndex(false);
    lastBetIndex = currentIndex;
    totalAmountToCall = 0;
  }

  private void payBlinds() {
    currentIndex = dealerIndex;
    if (numPlayers == 2) {
      pay(players[dealerIndex], blindAmount / 2);
    } else {
      pay(nextPlayer(), blindAmount / 2);
    }
    pay(nextPlayer(), blindAmount);
    nextPlayer();
    lastBetIndex = currentIndex;
  }

  private void drawStartingHands() {
    for (Player player : getActivePlayers()) {
      player.hand = new Hand(deck.draw(), deck.draw());
    }
  }

  private Player nextPlayer() {
    return players[nextIndex(false)];
  }

  private Player currentPlayer() {
    return players[currentIndex];
  }

  private List<Player> getActivePlayers() {
    List<Player> ret = Lists.newArrayList();
    for (Player player : players) {
      if (player != null && player.isActive()) {
        ret.add(player);
      }
    }
    return ret;
  }

  private int nextIndex(boolean stopAtBetIndex) {
    for (int i = 1; i < players.length; i++) {
      int k = (currentIndex + i) % players.length;
      if (stopAtBetIndex && k == lastBetIndex) {
        currentIndex = k;
        return currentIndex;
      }
      Player player = players[k];
      if (player != null && player.isActive() && player.chips > 0) {
        currentIndex = k;
        return currentIndex;
      }
    }
    Log.info("Out of players who can make actions -- going to the next round.");
    nextRound();
    return currentIndex;
  }

}
