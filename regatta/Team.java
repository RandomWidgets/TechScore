package regatta;


/**
 * Teams for regattas. Each team has a long and a short version of the
 * name, for compatibility with Navy Scoring. The long name can be
 * used for formal names, like the name of a school, and the short
 * name for the name of a team.
 *
 * Teams that have the same long and short name are considered
 * "equal". Teams are sorted by long names first, then short names.
 *
 * Teams have an affiliation for the purposes of RP's.
 *
 * Created: Sun Jun 14 20:44:12 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class Team implements Comparable<Team> {

  /**
   * The nickname for the regatta, possibly the team name.
   */
  private String shortname;

  /**
   * The longer version of the name, possibly the school name.
   */
  private String longname;

  /**
   * The affiliation ID (for RP purposes)
   */
  private String rpAffiliation;

  public Team(String longname) {
    this(longname, "");
  }

  /**
   * Creates a new <code>Team</code> instance.
   *
   */
  public Team(String longname, String shortname) {
    if (longname == null || longname.trim().length() == 0) {
      String mes = "Team's primary name must not be empty";
      throw new IllegalArgumentException(mes);
    }

    this.longname = longname.trim();
    this.shortname = (shortname == null) ? "" : shortname.trim();
  }

  /**
   * Get the <code>Shortname</code> value.
   *
   * @return a <code>String</code> value
   */
  public final String getShortname() {
    return shortname;
  }

  /**
   * Set the <code>Shortname</code> value.
   *
   * @param newShortname The new Shortname value.
   */
  public final void setShortname(final String newShortname) {
    this.shortname = newShortname;
  }

  /**
   * Get the <code>Longname</code> value.
   *
   * @return a <code>String</code> value
   */
  public final String getLongname() {
    return longname;
  }

  /**
   * Set the <code>Longname</code> value.
   *
   * @param newLongname The new Longname value.
   */
  public final void setLongname(final String newLongname) {
    this.longname = newLongname;
  }

  /**
   * Get the <code>Affiliation</code> (RP) for this team.
   *
   * @return a <code>String</code> ID of affiliation
   */
  public final String getAffiliation() {
    return rpAffiliation;
  }

  /**
   * Set the <code>Affiliation</code> for this team.
   *
   * @param aff the new affiliation ID
   */
  public final void setAffiliation(String aff) {
    this.rpAffiliation = aff;
  }
  

  /*
   * Comparisons
   */
  @Override
  public boolean equals(Object other) {
    if (other instanceof String) {
      return this.toString().equalsIgnoreCase((String)other);
    }

    return this.compareTo((Team)other) == 0;
  }

  public int compareTo(Team o) {
    int l = this.getLongname().compareTo(o.getLongname());
    if (l != 0)
      return l;

    return this.getShortname().compareTo(o.getShortname());
  }

  public String toString() {
    return this.getLongname() + " " + this.getShortname();
  }
}
