package botwars.compute.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.collect.Lists;

public class Deck {

  public final LinkedList<Card> cards = Lists.newLinkedList();

  public Deck() {
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        cards.add(new Card(rank, suit));
      }
    }

    Collections.shuffle(cards);
  }

  public Card draw() {
    return cards.poll();
  }

  public List<Card> draw(int n) {
    List<Card> ret = Lists.newArrayList();
    for (int i = 0; i < n; i++) {
      ret.add(draw());
    }
    return ret;
  }

  @Override
  public String toString() {
    return cards.toString();
  }

}
