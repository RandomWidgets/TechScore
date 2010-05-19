package edu.mit.techscore.regatta;

import regatta.Regatta.Division;

/**
 * Penalty assigned to a team in a given division
 *
 *
 * Created: Wed Sep  2 22:00:09 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class TeamPenalty {

  public static enum TeamPenaltyType {
    PFD("Illegal lifejacket"),
      LOP("Missing identification"),
      MRP("Missing RP info"),
      GDQ("Misc. disqualification");
    private String name;
    TeamPenaltyType(String n) {
      this.name = n;
    }
    public String desc() {return this.name;}
  };

  // Division
  private Division division;
  private Team team;
  private TeamPenaltyType type;
  private String comments;

  /**
   * Creates a new <code>TeamPenalty</code> instance.
   *
   */
  public TeamPenalty(Division div,
		     Team team,
		     TeamPenaltyType type,
		     String comments) {
    this.division = div;
    this.team     = team;
    this.type     = type;
    this.comments = comments;
  }

  public TeamPenalty(Division div,
		     Team team,
		     TeamPenaltyType type) {
    this(div, team, type, "");
  }

  public final Division getDivision() {
    return this.division;
  }

  public final Team getTeam() {
    return this.team;
  }

  public final TeamPenaltyType getType() {
    return this.type;
  }

  public final String getComments() {
    return this.comments;
  }

  /**
   * Parses the String representation of TeamPenaltyType and returns
   * the corresponding object.
   *
   * @param type a <code>String</code> value
   * @return a <code>TeamPenaltyType</code> value
   * @throws IllegalArgumentException is the type is non-recognized
   */
  public static TeamPenaltyType parseType(String type)
    throws IllegalArgumentException {
    for (TeamPenaltyType pen : TeamPenaltyType.values()) {
      if (pen.toString().equalsIgnoreCase(type)) {
	return pen;
      }
    }
    throw new IllegalArgumentException("Invalid TeamPenaltyType: " + type);
  }
}
