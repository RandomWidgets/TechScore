package regatta;

/**
 * Describe class RegattaEvent here.
 *
 *
 * Created: Sun Jun  7 11:50:43 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RegattaEvent {

  /**
   * Different types of changes
   */
  public static enum RegattaEventType {
    DETAILS, NAME, TEAM, RACE, ROTATION, FINISH, SCORE, RP, RP_DATA
      };
  
  private RegattaEventType changeType;

  /**
   * The regatta for this event.
   */
  private Regatta regatta;

  /**
   * The source of the event
   */
  private Object source;

  /**
   * Get the <code>Regatta</code> value.
   *
   * @return a <code>Regatta</code> value
   */
  public final Regatta getRegatta() {
    return regatta;
  }

  /**
   * Get the <code>ChangeType</code> value.
   *
   * @return an <code>int</code> value
   */
  public final RegattaEventType getChangeType() {
    return changeType;
  }

  /**
   * Get the source for this event.
   *
   * @return an <code>Object</code> value
   */
  public final Object getSource() {
    return source;
  }


  /**
   * Creates a new <code>RegattaEvent</code> instance.
   *
   * @param reg  the target regatta
   * @param changeType the type of change
   */
  public RegattaEvent(Regatta reg,
		      RegattaEventType changeType,
		      Object source) {
    this.regatta = reg;
    this.changeType = changeType;
    this.source  = source;
  }

  public String toString() {
    String rep = "RegattaEvent: " + this.regatta;
    rep += ":" + this.changeType;
    rep += ":" + this.source.toString();

    return rep;
  }
}
