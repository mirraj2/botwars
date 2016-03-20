package botwars.compute.model;

import java.io.Serializable;
import java.util.Iterator;
import com.google.common.collect.ImmutableList;

public class Hand implements Iterable<Card>, Serializable {

  public final Card a;
  public final Card b;

  public Hand(Card a, Card b) {
    if (a.compareTo(b) >= 0) {
      this.a = a;
      this.b = b;
    } else {
      this.a = b;
      this.b = a;
    }
  }

  @Override
  public String toString() {
    return "[" + a + ", " + b + "]";
  }

  /**
   * Constructs a hand using poker notation.
   */
  public static Hand create(String s) {
    if (s == null || s.length() != 4) {
      throw new IllegalArgumentException(s);
    }

    return new Hand(Card.create(s.substring(0, 2)), Card.create(s.substring(2, 4)));
  }

  @Override
  public Iterator<Card> iterator() {
    return ImmutableList.of(a, b).iterator();
  }

  public String toShortString() {
    return a.toShortString() + b.toShortString();
  }

}
