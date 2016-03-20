package botwars.compute.model;

import java.util.Map;
import com.google.common.collect.Maps;

public enum Rank {

  TWO('2'), THREE('3'), FOUR('4'), FIVE('5'), SIX('6'), SEVEN('7'), EIGHT('8'), NINE('9'),
  TEN('T'), JACK('J'), QUEEN('Q'), KING('K'), ACE('A');

  private final char c;

  private Rank(char c) {
    this.c = c;
  }

  public char getChar() {
    return c;
  }

  public boolean beats(Rank otherRank) {
    return ordinal() > otherRank.ordinal();
  }

  private static final Map<Character, Rank> rankMap = Maps.newHashMap();
  static {
    for (Rank rank : Rank.values()) {
      rankMap.put(rank.c, rank);
    }
  }

  public static Rank getRank(char c) {
    c = Character.toUpperCase(c);
    Rank ret = rankMap.get(c);
    if (ret == null) {
      throw new IllegalArgumentException("Illegal rank: " + c);
    }
    return ret;
  }

}
