package botwars.compute.model;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.List;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

public class Card implements Comparable<Card>, Serializable {

  public final Rank rank;
  public final Suit suit;

  public Card(Rank rank, Suit suit) {
    checkNotNull(rank, "rank");
    checkNotNull(suit, "suit");

    this.rank = rank;
    this.suit = suit;
  }

  public Rank getRank() {
    return rank;
  }

  public Suit getSuit() {
    return suit;
  }

  @Override
  public String toString() {
    return rank + " of " + suit;
  }

  @Override
  public int compareTo(Card o) {
    return ComparisonChain.start().compare(rank.ordinal(), o.rank.ordinal()).result();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rank, suit);
  }

  public String toShortString() {
    return "" + rank.getChar() + suit.getChar();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Card)) {
      return false;
    }
    Card that = (Card) obj;
    return this.rank == that.rank && this.suit == that.suit;
  }

  public static Card create(String s) {
    try {
      return new Card(Rank.getRank(s.charAt(0)), Suit.getSuit(s.charAt(1)));
    } catch (Exception e) {
      throw new RuntimeException("Illegal card: " + s);
    }
  }

  public static List<Card> getCards(String s) {
    List<Card> cards = Lists.newArrayList();
    for (int i = 0; i < s.length(); i += 2) {
      cards.add(Card.create(s.substring(i, i + 2)));
    }
    return cards;
  }
}
