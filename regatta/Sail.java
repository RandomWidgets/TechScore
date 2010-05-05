package regatta;

/**
 * The sail a team uses in a given race. This can be a String. It can
 * also have an optional numerical component.
 *
 *
 * Created: Sun Jun 28 21:34:02 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.5
 */
public class Sail implements Comparable<Sail> {

  /**
   * The sail: as a string
   */
  private Integer number;  // the (optional) numerical
  private int numLength;   // the length to format the integer part
  private String preSail;  // the prefix
  private String postSail; // the suffix for the sail

  /**
   * Creates a new <code>Sail</code> instance with the given value.
   *
   * @param n an <code>String</code> value for the sail
   */
  public Sail(String n) {
    this.setSail(n);
  }

  /**
   * Get the <code>Sail</code> value.
   *
   * @return an <code>String</code> value
   */
  public final String getSail() {
    return this.toString ();
  }

  /**
   * Set the <code>Sail</code> value.
   *
   * @param newSail The new Sail value.
   */
  public final void setSail(final String newSail) {
    // Interprets and parses the argument in the hopes of dividing it
    // into up to three parts: a String prefix, a numerical component,
    // and a String suffix. Should the newSail contain two sets of
    // numbers, the latter is used for the number. For example:
    //
    // newSail | sail | number | postSail
    // --------+------+--------+---------
    //      12 |   "" |     12 | ""
    //   MIT34 |  MIT |     34 | ""
    //    A23b |    A |     23 | b
    //      HG |   "" |   null | HG
    //   98def |   "" |     98 | def
    //    4b21 |   4b |     21 | ""
    //
    StringBuilder preSail = new StringBuilder ();
    StringBuilder postSail = new StringBuilder ();
    StringBuilder numString = new StringBuilder ();

    // keep track of whether the integer part of the sail has been
    // created, since only the first such sequence is to be used
    boolean hasStarted  = false;
    boolean hasFinished = false;
    for (int i = newSail.length (); i > 0; i--) {
      char chr = newSail.charAt (i - 1);
      if (chr >= '0' && chr <= '9') {
	if (!hasStarted) hasStarted = true;
	if (!hasFinished)
	  numString.insert (0, chr);
      }
      else {
	if (!hasStarted) {
	  postSail.insert (0, chr);
	}
	else {
	  hasFinished = true;
	  preSail.insert (0, chr);
	}
      }
    }

    this.preSail = preSail.toString ();
    this.numLength = numString.length ();
    if (this.numLength > 0) {
      try {
	this.number = new Integer (numString.toString ());
      } catch (Exception e) {
	this.number = null;
      }
    }
    else
      this.number = null;
    this.postSail = postSail.toString ();	
  }

  public String toString() {
    if (this.isNumerical ()) {
      String fmt = String.format ("%%s%%0%dd%%s", this.numLength);
      return String.format (fmt, this.preSail, this.number, this.postSail);
    }
    return this.postSail;
  }

  // Implementation of Comparable

  /**
   * Compares two sails lexically
   *
   * @param other the other Sail
   * @return -1 if this sail comes before
   * @return  0 if the two are equal
   * @return  1 if this sail comes after
   */
  public int compareTo(Sail other) {
    return this.getSail ().compareTo(other.getSail ());
  }

  /**
   * Compares the sail values
   *
   * @return true if they are equal
   */
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other instanceof String) {
      return other.equals(this.toString());
    }
    if (other instanceof Sail) {
      return this.compareTo((Sail)other) == 0;
    }
    return false;
  }

  public int hashCode () {
    return "Sail".hashCode () + this.toString ().hashCode ();
  }

  /**
   * Returns whether or not this sail as a numerical component to it
   *
   * @return true if it has a numerical component
   */
  public boolean isNumerical () {
    return this.number != null;
  }

  /**
   * Returns the numerical portion of this sail, if one exists.
   *
   * @return the number, or 0 if no numerical portion exists
   */
  public int getNumber () {
    if (this.number == null)
      return 0;
    return this.number;
  }

  /**
   * Adds the given amount to the numerical portion of this sail
   *
   * @param n the amount to add
   * @throws IllegalArgumentException if this sail has no numerical
   * component or if the resulting number is non-positive
   */
  public void add (int n) throws IllegalArgumentException {
    if (!this.isNumerical ())
      throw new IllegalArgumentException ("Sail has no numerical value");

    this.number += n;
  }

  public static void main (String [] args) {
    Sail [] sails = new Sail [] {new Sail ("MIT02"),
				 new Sail ("HAR"),
				 new Sail ("003bcd"),
				 new Sail ("32zz1")
    };

    for (Sail s: sails) {
      System.out.print (String.format ("%8s: %5s: ", s, s.isNumerical ()));
      try {
	s.add (3);
	System.out.println (s);
      } catch (Exception e) {
	System.out.println ();
      }
    }
  }
}
