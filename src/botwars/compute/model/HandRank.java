package botwars.compute.model;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.List;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

public class HandRank implements Comparable<HandRank> {

  public final Type type;
  public final List<Card> cards;

  public HandRank(Type type, List<Card> cards) {
    checkNotNull(type);

    this.type = type;
    this.cards = ImmutableList.copyOf(cards);
  }

  public Rank getHighCard() {
    if (type == Type.HIGH_CARD) {
      return cards.get(0).rank;
    } else if (type == Type.PAIR) {
      return cards.get(2).rank;
    } else if (type == Type.TWO_PAIR) {
      return cards.get(4).rank;
    } else if (type == Type.THREE_OF_A_KIND) {
      return cards.get(3).rank;
    } else if (type == Type.FOUR_OF_A_KIND) {
      return cards.get(4).rank;
    } else {
      return null;
    }
  }

  @Override
  public int compareTo(HandRank o) {
    ComparisonChain chain = ComparisonChain.start().compare(type.ordinal(), o.type.ordinal());

    for (int i = 0; i < cards.size(); i++) {
      chain = chain.compare(cards.get(i), o.cards.get(i));
    }

    return chain.result();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HandRank)) {
      return false;
    }
    HandRank that = (HandRank) obj;
    if (this.type != that.type) {
      return false;
    }
    for (int i = 0; i < cards.size(); i++) {
      if (cards.get(i).rank != that.cards.get(i).rank) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return type.toString() + " " + cards;
  }

  public static enum Type {
    HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND,
    STRAIGHT_FLUSH, ROYAL_FLUSH;

    public boolean beats(Type t) {
      return ordinal() > t.ordinal();
    }
  }



}
