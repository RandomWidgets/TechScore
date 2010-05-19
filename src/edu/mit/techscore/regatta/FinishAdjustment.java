package edu.mit.techscore.regatta;

/**
 * Describe class FinishAdjustment here.
 *
 *
 * Created: Thu Jul 30 09:11:29 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class FinishAdjustment {

  // Private
  protected Enum type;
  protected String comments;

  /**
   * Creates a new <code>FinishAdjustment</code> instance.
   *
   */
  public FinishAdjustment(Enum type) {
    this(type, "");
  }

  /**
   * Creates a new <code>FinishAdjustment</code> instance.
   *
   * @param comments a <code>String</code> value for the adjustment's
   * comments
   */
  public FinishAdjustment(Enum type, String comments) {
    this.type = type;
    this.comments = comments;
  }

  public final String getComments() {return this.comments;}
  public final Enum getType() {return this.type;}
  public String toString() {return this.type.toString();}
}
