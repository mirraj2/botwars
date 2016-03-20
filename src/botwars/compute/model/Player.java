package botwars.compute.model;

import java.util.UUID;

public class Player {

  public final String token = UUID.randomUUID().toString().replace("-", "");
  public final String name;

  public int chips;
  public Hand hand;

  public Status status;

  public Player(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean isActive() {
    return status == Status.ACTIVE;
  }

  public static enum Status {
    SITTING_OUT, FOLDED, ACTIVE;
  }

}
