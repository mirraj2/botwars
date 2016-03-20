package botwars.compute.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import botwars.compute.model.Card;
import botwars.compute.model.Hand;
import botwars.compute.model.HandRank;
import botwars.compute.model.HandRank.Type;
import botwars.compute.model.Rank;
import botwars.compute.model.Suit;

public class SlowHandRanker {

  public static HandRank determineRank(Hand hand, List<Card> board) {
    List<Card> cards = Lists.newArrayList(hand.a, hand.b);
    cards.addAll(board);
    return determineRank(cards);
  }

  public static HandRank determineRank(List<Card> cards) {
    cards = Ordering.natural().immutableSortedCopy(cards).asList();

    List<List<Card>> straights = getStraights(cards);

    for (List<Card> hand : straights) {
      if (isFlush(hand)) {
        if (Iterables.getLast(hand).rank == Rank.ACE) {
          return new HandRank(Type.ROYAL_FLUSH, hand);
        } else {
          return new HandRank(Type.STRAIGHT_FLUSH, hand);
        }
      }
    }

    Multimap<Rank, Card> rankCounts = getRankCounts(cards);

    HandRank ret = get4OfAKind(cards, rankCounts);

    if (ret != null) {
      return ret;
    }

    ret = getFullHouse(cards, rankCounts);

    if (ret != null) {
      return ret;
    }

    ret = getFlush(cards);

    if (ret != null) {
      return ret;
    }

    if (!straights.isEmpty()) {
      return new HandRank(Type.STRAIGHT, straights.get(0));
    }

    ret = get3OfAKind(cards, rankCounts);

    if (ret != null) {
      return ret;
    }

    ret = get2Pair(cards, rankCounts);

    if (ret != null) {
      return ret;
    }

    ret = getPair(cards, rankCounts);

    if (ret != null) {
      return ret;
    }

    return new HandRank(Type.HIGH_CARD, Ordering.natural().reverse()
        .immutableSortedCopy(cards.subList(Math.max(cards.size() - 5, 0), cards.size())));
  }

  private static HandRank getFullHouse(List<Card> cards, Multimap<Rank, Card> rankCounts) {
    List<Card> hand = Lists.newArrayList();

    Rank fullRank = null;

    for (Rank rank : rankCounts.keySet()) {
      if (rankCounts.get(rank).size() == 3) {
        fullRank = rank;
        hand.addAll(rankCounts.get(rank));
        break;
      }
    }

    if (fullRank == null) {
      return null;
    }

    for (Rank rank : rankCounts.keySet()) {
      if (rank != fullRank && rankCounts.get(rank).size() >= 2) {
        Iterator<Card> c = rankCounts.get(rank).iterator();
        hand.add(c.next());
        hand.add(c.next());
        break;
      }
    }

    if (hand.size() != 5) {
      return null;
    }

    return new HandRank(Type.FULL_HOUSE, hand);
  }

  private static HandRank getPair(List<Card> cards, Multimap<Rank, Card> rankCounts) {
    List<Card> highCards = Lists.newArrayList();
    List<Card> hand = Lists.newArrayList();

    for (Rank rank : rankCounts.keySet()) {
      if (rankCounts.get(rank).size() == 2) {
        hand.addAll(rankCounts.get(rank));
      } else if (highCards.size() < 3) {
        highCards.add(rankCounts.get(rank).iterator().next());
      }
    }

    if (hand.size() != 2) {
      return null;
    }

    hand.addAll(highCards);

    return new HandRank(Type.PAIR, hand);
  }

  private static HandRank get2Pair(List<Card> cards, Multimap<Rank, Card> rankCounts) {
    List<Card> hand = Lists.newArrayList();
    Card highCard = null;

    for (Rank rank : rankCounts.keySet()) {
      if (rankCounts.get(rank).size() == 2 && hand.size() < 4) {
        hand.addAll(rankCounts.get(rank));
      } else if (highCard == null) {
        highCard = rankCounts.get(rank).iterator().next();
      }
    }

    if (hand.size() != 4) {
      return null;
    }

    if (highCard != null) {
      hand.add(highCard);
    }

    return new HandRank(Type.TWO_PAIR, hand);
  }

  private static HandRank get3OfAKind(List<Card> cards, Multimap<Rank, Card> rankCounts) {
    List<Card> hand = getXOfAKind(cards, rankCounts, 3);
    return hand == null ? null : new HandRank(Type.THREE_OF_A_KIND, hand);
  }

  private static HandRank get4OfAKind(List<Card> cards, Multimap<Rank, Card> rankCounts) {
    List<Card> hand = getXOfAKind(cards, rankCounts, 4);
    return hand == null ? null : new HandRank(Type.FOUR_OF_A_KIND, hand);
  }

  private static List<Card> getXOfAKind(List<Card> cards, Multimap<Rank, Card> rankCounts, int x) {
    for (Rank rank : rankCounts.keySet()) {
      if (rankCounts.get(rank).size() == x) {
        List<Card> hand = Lists.newArrayList(rankCounts.get(rank));
        for (Card card : Lists.reverse(cards)) {
          if (card.rank != rank) {
            hand.add(card);
            if (hand.size() == 5) {
              break;
            }
          }
        }
        return hand;
      }
    }

    return null;
  }

  private static Multimap<Rank, Card> getRankCounts(List<Card> cards) {
    Multimap<Rank, Card> ret = LinkedHashMultimap.create();

    for (Card card : Lists.reverse(cards)) {
      ret.put(card.rank, card);
    }

    return ret;
  }

  private static List<List<Card>> getStraights(List<Card> cards) {
    List<List<Card>> ret = Lists.newArrayList();

    List<List<Card>> lastSet = Lists.newArrayList();
    List<List<Card>> currentSet = Lists.newArrayList();
    for (Rank startingRank : getStraightRanks(cards)) {
      int s = startingRank == Rank.ACE ? -1 : startingRank.ordinal();
      for (int i = 0; i < 5; i++) {
        Rank currentRank;
        if (i == 0 && s == -1) {
          currentRank = Rank.ACE;
        } else {
          currentRank = Rank.values()[s + i];
        }
        for (Card card : cards) {
          if (card.rank == currentRank) {
            if (currentRank == startingRank) {
              currentSet.add(Lists.newArrayList(card));
            } else {
              for (List<Card> list : lastSet) {
                List<Card> toAdd = Lists.newArrayList(list);
                toAdd.add(card);
                currentSet.add(toAdd);
              }
            }
          }
        }
        lastSet.clear();
        lastSet.addAll(currentSet);
        currentSet.clear();
      }
      ret.addAll(lastSet);
      lastSet.clear();
    }

    return ret;
  }

  private static Set<Rank> getStraightRanks(List<Card> cards) {
    Set<Rank> ret = Sets.newLinkedHashSet();

    boolean aceLowStraight = false;
    for (int i = cards.size() - 1; i >= 0; i--) {
      Card card = cards.get(i);
      if (hasStraight(cards, card.rank)) {
        if (card.rank == Rank.ACE) {
          aceLowStraight = true;
        } else {
          ret.add(card.rank);
        }
      }
    }

    if (aceLowStraight) {
      ret.add(Rank.ACE);
    }

    return ret;
  }

  private static boolean hasStraight(List<Card> cards, Rank startingRank) {
    boolean[] seen = new boolean[4];

    int s = startingRank.ordinal();

    // an ace can be the low card of a straight.
    if (startingRank == Rank.ACE) {
      s = -1;
    }

    for (Card card : cards) {
      int i = card.rank.ordinal() - s;
      if (i > 0 && i < 5) {
        seen[i - 1] = true;
      }
    }
    for (boolean b : seen) {
      if (!b) {
        return false;
      }
    }
    return true;
  }

  private static HandRank getFlush(List<Card> cards) {
    int[] counts = new int[4];
    for (Card card : cards) {
      counts[card.suit.ordinal()]++;
    }
    int index = -1;
    for (int i = 0; i < counts.length; i++) {
      if (counts[i] >= 5) {
        index = i;
        break;
      }
    }
    if (index == -1) {
      return null;
    }

    Suit suit = Suit.values()[index];

    List<Card> hand = Lists.newArrayList();
    for (Card card : Lists.reverse(cards)) {
      if (card.suit == suit) {
        hand.add(card);
        if (hand.size() == 5) {
          break;
        }
      }
    }

    return new HandRank(Type.FLUSH, hand);
  }

  /**
   * Given 5 cards, determines if it is a flush.
   */
  private static boolean isFlush(List<Card> cards) {
    Suit suit = cards.get(0).getSuit();
    for (int i = 1; i < 5; i++) {
      if (cards.get(i).suit != suit) {
        return false;
      }
    }
    return true;
  }

}
