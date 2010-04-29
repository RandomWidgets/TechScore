package regatta;

import regatta.Regatta.Division;


/**
 * Implementation of a Regatta race.
 *
 *
 * Created: Fri Jun 12 11:42:46 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class Race implements Comparable<Race> {
  private Division division;
  private int number;

  /**
   * Creates a new <code>Race</code> instance.
   *
   */
  public Race(Division div, int num) {
    this.division = div;
    this.number = num;
  }

  public void setDivision(Division div) {
    this.division = div;
  }
  public Division getDivision() {return this.division;}
  public void setNumber(int num) {
    this.number = num;
  }
  public int getNumber() {return this.number;}

  public int compareTo(Race other) {
    int c = this.getDivision().compareTo(other.getDivision());
    if (c != 0) {
      return c;
    }
    c = this.getNumber() - other.getNumber();
    if (c == 0) return 0;
    if (c <  0) return -1;
    return 1;
  }

  public @Override boolean equals(Object r) {
    if (r instanceof String)
      return this.toString().equalsIgnoreCase((String)r);

    if (r instanceof Race) {
      return this.compareTo((Race)r) == 0;
    }
    return false;
  }

  public String toString() {
    return "" + this.number + this.division;
  }
}
