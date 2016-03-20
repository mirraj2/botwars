package botwars.compute.model;

import java.util.Map;
import com.google.common.collect.Maps;

public enum Suit {

  CLUBS('c'), DIAMONDS('d'), HEARTS('h'), SPADES('s');

  private final char c;

  private Suit(char c) {
    this.c = c;
  }

  public char getChar() {
    return c;
  }

  private static final Map<Character, Suit> suitMap = Maps.newHashMap();

  static {
    for (Suit suit : Suit.values()) {
      suitMap.put(suit.c, suit);
    }
  }

  public static Suit getSuit(char c) {
    c = Character.toLowerCase(c);
    Suit ret = suitMap.get(c);
    if (ret == null) {
      throw new IllegalArgumentException("Illegal suit: " + c);
    }
    return ret;
  }
}
