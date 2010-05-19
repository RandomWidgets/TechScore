package edu.mit.techscore.regatta;

/**
 * Describe class Penalty here.
 *
 *
 * Created: Thu Jul 30 09:16:39 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class Penalty extends FinishAdjustment {

  public static enum PenaltyType {
    DSQ("Disqualification"),
      OCS("On Course Side after start"),
      RAF("Retire After Finish"),
      DNS("Did Not Start"),
      DNF("Did Not Finish");
    private String name;
    PenaltyType(String n) {
      this.name = n;
    }
    public String desc() {return this.name;}
  };

  /**
   * Creates a new <code>Penalty</code> instance.
   *
   * @param type a <code>PenaltyType</code> value
   * @param comments a <code>String</code> value
   */
  public Penalty(PenaltyType type, String comments) {
    super(type, comments);
  }

  public Penalty(PenaltyType type) {
    this(type, "");
  }
}
