package edu.mit.techscore.regatta;

import java.util.Date;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Describe class Sailor here.
 *
 *
 * Created: Thu Aug  6 10:04:01 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class Sailor implements Comparable<Sailor> {

  // Private variables
  private String id, name;
  private Date year;

  /**
   * Creates a new <code>Sailor</code> instance.
   *
   */
  public Sailor(String id, String name, Date year) {
    this.id   = id;
    this.name = name;
    this.year = year;
  }

  public String getID()   { return this.id; }
  public String getName() { return this.name; }
  public Date getYear() { return this.year; }

  // Implementation of java.lang.Comparable

  /**
   * Compares two sailors based on their names with their year as a
   * final tiebreaker (age before beauty)
   *
   * @param object an <code>Object</code> value
   * @return an <code>int</code> value
   */
  public final int compareTo(final Sailor other) {
    int rel = this.name.compareTo(other.getName());
    if (rel != 0) {
      return rel;
    }
    Date oYear = other.getYear();
    if (this.year == null && oYear != null) {
      return -1;
    }
    if (this.year != null && oYear == null) {
      return 1;
    }
    if (this.year == null && oYear == null) {
      return 0;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(this.year);
    int y = cal.get(Calendar.YEAR);
    cal.setTime(oYear);
    return y - cal.get(Calendar.YEAR);
  }

  public boolean equals(Object other) {
    if (other instanceof Sailor) {
      return this.compareTo((Sailor)other) == 0;
    }
    return false;
  }

  public String toString() {
    if (this.year == null) {
      return this.name;
    }

    Calendar cal = Calendar.getInstance();
    cal.setTime(this.year);
    String y = String.valueOf(cal.get(Calendar.YEAR));
    y = (y.length() < 2) ? y : "'" + y.substring(2);

    return this.name + " " + y;
  }

  public static class SailorIdComparator implements Comparator<Sailor> {

    // Implementation of java.util.Comparator
    public int compare(Sailor s1, Sailor s2) {
      return s1.getID().compareTo(s2.getID());
    }
  }
  
}
