package regatta;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeMap;
import regatta.Regatta.Division;
import regatta.Rotation.RotationType;
import regatta.Rotation.RotationStyle;
import java.util.TreeSet;
import java.util.Map;
import java.util.List;
import java.util.HashSet;

/**
 * Rotations manage the sails that each team is racing in for a given
 * race. This class manages those subscriptions. For this version,
 * there is no need for sails to be integers.
 *
 * Created: Tue Jun 16 10:05:27 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.5
 */
public class Rotation {
  // Public variables
  public static enum RotationType {
    STANDARD("Standard (+1)"),
      SWAP("Direct Swap"),
      NONE("No rotation");
    private String name;
    RotationType(String n) {
      this.name = n;
    }
    public String toString() {
      return this.name;
    }
  }

  public static enum RotationStyle {
    NONE("Collated"),
      NAVY("Non-collated"),
      FRANNY("Non-collated with amount...");
    private String name;
    RotationStyle(String n) {
      this.name = n;
    }
    public String toString() {
      return this.name;
    }
  }

  // Private variables
  Map<Race, Map<Team, Sail>> rot;
  Set<Team> teamSet;

  /**
   * Creates a new <code>Rotation</code> instance.
   *
   */
  public Rotation() {
    this.rot = new TreeMap<Race, Map<Team, Sail>>();
    teamSet = new TreeSet<Team>();
  }

  /**
   * Adds rotation for a given race, team, and sail
   * @param race the race for which this is the rotation
   * @param team the team in this race
   * @param sail the sail number for the team in this race
   */
  public void setSail(Race race, Team team, Sail sail) {
    // Create a new map for that race, if needed
    Map<Team, Sail> tsMap = this.rot.get(race);
    if (tsMap == null) {
      // New race
      tsMap = new TreeMap<Team, Sail>();
      this.rot.put(race, tsMap);
      // Populate tsMap with all the known teams
      for (Team t : teamSet) {
	tsMap.put(t, null);
      }
    }
    if (teamSet.add(team)) {
      // New team
      // Add the team to all other race maps
      Map<Team, Sail> m;
      for (Race r : this.rot.keySet()) {
	m = this.rot.get(r);
	m.put(team, null);
      }
    }
    // Update the current team
    tsMap.put(team, sail);
  }

  /**
   * Get the sail for a team in a race
   * @param race the race to look in
   * @param team the team to look for 
   * @return the sail number for the given race and team
   */
  public Sail getSail(Race race, Team team) {
    Map<Team, Sail> map = this.rot.get(race);
    if (map == null) {
      return null;
    }
    return map.get(team);
  }

  /**
   * Gets the team with the given sail in the given race
   *
   * @param race a <code>Race</code> value
   * @param sail a <code>Sail</code> value
   * @return a <code>Team</code> value
   * @return <code>null</code> if no such team
   */
  public Team getTeam(Race race, Sail sail) {
    Map<Team, Sail> tsMap = this.rot.get(race);
    for (Team team : tsMap.keySet()) {
      if (tsMap.get(team).equals(sail)) {
	return team;
      }
    }
    return null;
  }

  /**
   * Returns array of sails in the given race in ascending order. This
   * is a convenience method.
   *
   * @param race a <code>Race</code> value
   * @return a <code>Sail[]</code> value
   */
  public Sail [] getSails(Race race) {
    // If no sails for the rotation, return empty list
    if (!this.rot.containsKey(race)) {
      return new Sail[]{};
    }
    Set<Sail> sails = new TreeSet<Sail>(this.rot.get(race).values());
    return sails.toArray(new Sail [] {});
  }

  /**
   * Returns an ordered set of all the sails present in the list of
   * races given.
   *
   * @param races a <code>Race</code> value
   * @return a <code>Sail[]</code> value
   */
  public Sail [] getSails(Race [] races) {
    Set<Sail> sails = new TreeSet<Sail>();
    for (Race race : races) {
      sails.addAll(this.rot.get(race).values());
    }
    return sails.toArray(new Sail[] {});
  }

  /**
   * Removes the team from this rotation
   *
   * @param t a <code>Team</code> value
   */
  public void removeTeam(Team t) {
    for (Race race : this.rot.keySet()) {
      this.rot.get(race).remove(t);
    }
    this.teamSet.remove(t);
  }

  // Races
  /**
   * Returns an array of the races that have sails in this rotation.
   *
   * @return a <code>Race[]</code> value
   */
  public Race [] getRaces() {
    return this.rot.keySet().toArray(new Race[]{});
  }

  public void removeRace(Race race) {
    this.rot.remove(race);
  }

  /**
   * Check for incomplete races. A race is incomplete if there is at
   * least one team without a sail. A list of incomplete races is
   * returned.
   *
   * @return the incomplete races in the rotation
   */
  public Race [] normalize() {
    List<Race> badRaces = new ArrayList<Race>();
    for (Race race : this.rot.keySet()) {
      if (this.rot.get(race).containsValue(null)) {
	badRaces.add(race);
      }
    }
    return badRaces.toArray(new Race[]{});
  }

  /**
   * Dump to standard output
   */
  public void dump() {
    String tForm = "%20s";
    String rForm = " %3s";
    Set<Race> races = this.rot.keySet();
    // Print heading
    System.out.print(String.format(tForm, ""));
    for (Race race : races) {
      System.out.print(String.format(rForm, race));
    }
    System.out.println();

    // Rows
    for (Team team : teamSet) {
      System.out.print(String.format(tForm, team));
      for (Race race : races) {
	Sail sail = this.rot.get(race).get(team);
	System.out.print(String.format(rForm, sail));
      }
      System.out.println();
    }
  }

  public static void main (String [] args) {
    Rotation rot = new Rotation();
    Race [] races = {new Race(Division.A, 1),
		     new Race(Division.A, 2),
		     new Race(Division.A, 3),
		     new Race(Division.B, 1),
		     new Race(Division.B, 2),
		     new Race(Division.C, 3)};
    Team [] teams = {new Team("A"), new Team("B"),
		     new Team("C"), new Team("D"),
		     new Team("E"), new Team("F")};

    rot.setSail(races[0], teams[0], new Sail(1));
    rot.setSail(races[0], teams[1], new Sail(2));
    rot.setSail(races[1], teams[2], new Sail(3));
    rot.setSail(races[1], teams[3], new Sail(4));
    rot.setSail(races[1], teams[0], new Sail(5));
    rot.setSail(races[2], teams[0], new Sail(6));
    rot.setSail(races[0], teams[2], new Sail(3));
    rot.setSail(races[0], teams[3], new Sail(4));
    rot.dump();

    System.out.println("Removing team " + teams[1]);
    rot.removeTeam(teams[1]);
    rot.dump();

    System.out.println("Problematic races:");
    for (Race race : rot.normalize()) {
      System.out.print(race + " ");
      rot.removeRace(race);
    }
    System.out.println();

    rot.dump();
  }
}
