package botwars.compute.model;

import java.util.UUID;
import org.java_websocket.WebSocket;

public class Player {

  public final String token = UUID.randomUUID().toString().replace("-", "");

  public String name;
  public WebSocket socket;

  public int seatIndex;

  public int chips;
  public int betAmount;

  /**
   * The total amount of money put into the pot this hand.
   */
  public int totalBetAmount;

  public Hand hand;

  public Status status;

  public long version = -1;
  public byte[] state = null;

  public Player(WebSocket socket) {
    this.socket = socket;
  }

  public Player(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean isActive() {
    return status == Status.PLAYING;
  }

  public void send(String message) {
    socket.send(message);
  }

  public static enum Status {
    SITTING_OUT, FOLDED, PLAYING;
  }

}
