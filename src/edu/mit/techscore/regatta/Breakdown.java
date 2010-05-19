package regatta;

/**
 * Describe class Breakdown here.
 *
 *
 * Created: Thu Jul 30 09:24:01 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class Breakdown extends FinishAdjustment {

  /**
   * New score awarded to the finish. The default value of 0 means no
   * handicap. Positive values expected.
   */
  private int handicap = 0;

  public static enum BreakdownType {
    BKD("Breakdown"),
      RDG("Yacht-given redress"),
      BYE("Bye");
    private String name;
    BreakdownType(String n) {
      this.name = n;
    }
    public String desc() {return this.name;}
  };


  /**
   * Creates a new <code>Breakdown</code> instance.
   *
   * @param type a <code>BreakdownType</code> value
   * @param comments a <code>String</code> value
   */
  public Breakdown(BreakdownType type, String comments) {
    super(type, comments);
  }

  public Breakdown(BreakdownType type, String comments, int handicap) {
    super(type, comments);
    this.handicap = handicap;
  }

  /**
   * Get the <code>Handicap</code> value.
   *
   * @return an <code>int</code> value
   */
  public final int getHandicap() {
    return handicap;
  }

  /**
   * Set the <code>Handicap</code> value.
   *
   * @param newHandicap The new Handicap value.
   */
  public final void setHandicap(final int newHandicap) {
    this.handicap = newHandicap;
  }
}
