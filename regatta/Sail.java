package regatta;


/**
 * The sail a team uses in a given race. This can be a String.
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
  private String sail;

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
    return sail;
  }

  /**
   * Set the <code>Sail</code> value.
   *
   * @param newSail The new Sail value.
   */
  public final void setSail(final String newSail) {
    this.sail = newSail;
  }

  public String toString() {
    return this.sail;
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
    return this.sail.compareTo(other.getSail());
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
}
