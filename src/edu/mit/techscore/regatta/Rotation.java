package edu.mit.techscore.regatta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import regatta.Regatta.Division;
import regatta.Rotation.RotationStyle;
import regatta.Rotation.RotationType;

/**
 * Rotations manage the sails that each team is racing in for a given
 * race. This class manages those subscriptions. For this version,
 * there is no need for sails to be integers.
 * <p>
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
  private Map<Race, Map<Team, Sail>> rot;
  private Set<Team> teamSet;

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
    Map<Team, Sail> teamMap;
    Set<Sail> uniqueSails;
    for (Race race : this.rot.keySet()) {
      teamMap = this.rot.get(race);
      // Check for missing sails
      if (teamMap.containsValue(null)) {
	badRaces.add(race);
      }
      // Check for repeats
      else {
	uniqueSails = new HashSet<Sail>(teamMap.values());
	if (uniqueSails.size() < teamMap.size())
	  badRaces.add(race);
      }
    }
    return badRaces.toArray(new Race[]{});
  }
  
  /**
   * In the case of combined scoring, there must be a unique sail
   * number for each team in every race number across all
   * divisions. The list of incomplete races in this case will contain
   * the faulty race and division. Therefore, if for race 3 the
   * mandate of unique sails is not met, then all races numbered 3
   * across all the divisions will be returned.
   *
   * @param divisions the set of divisions to which normalize
   * @return list of bad divisions
   */
  public Race [] normalize(Division [] divisions) {
    Set<Race> badRaces = new TreeSet<Race>();
    Map<Team, Sail> teamMap;
    Set<Race> raceSet = this.rot.keySet();
    // 1. Check that every division has all the races by creating a
    // set of unique race numbers
    Set<Integer> raceNums = new HashSet<Integer>();
    for (Race race : raceSet)
      raceNums.add(new Integer(race.getNumber()));
    List<Integer> goodNums = new ArrayList<Integer>(raceNums);

    for (Integer num : raceNums) {
      for (Division d : divisions) {
	if (!raceSet.contains(new Race(d, num))) {
	  for (Division d2 : divisions)
	    badRaces.add(new Race(d2, num));
	  goodNums.remove(num);
	  break;
	}
      }
    }

    // At this point, goodNums contains the race numbers for which
    // there is a rotation set across all the divisions. Next, check
    // that no race contains a null sail (unset)
    for (Race race : raceSet) {
      teamMap = this.rot.get(race);
      // Check for missing sails
      if (teamMap.containsValue(null)) {
	badRaces.add(race);
      }
    }

    // Next, check that the sails are unique across all divisions
    Set<Sail> uniqueSails;
    for (int num : goodNums) {
      uniqueSails = new HashSet<Sail>();
      for (Division d : divisions) {
	int numUnique = uniqueSails.size();
	Collection<Sail> sails = this.rot.get(new Race(d, num)).values();
	uniqueSails.addAll(sails);

	if (uniqueSails.size() < numUnique + sails.size()) {
	  for (Division d2 : divisions)
	    badRaces.add(new Race(d2, num));
	  break;
	}
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
    Rotation rot = new Rotation ();
    Team t1 = new Team ("MIT");
    Team t2 = new Team ("HAR");
    Team t3 = new Team ("TUF");

    Race r1 = new Race (Division.A, 1);
    Race r2 = new Race (Division.B, 1);

    // Add sails
    rot.setSail (r1, t1, new Sail ("A"));
    rot.setSail (r1, t2, new Sail ("B"));
    rot.setSail (r1, t3, new Sail ("C"));
    rot.setSail (r2, t1, new Sail ("D"));
    rot.setSail (r2, t2, new Sail ("E"));
    rot.setSail (r2, t3, new Sail ("A"));

    rot.dump ();

    System.out.print ("Bad races: ");
    for (Race r : rot.normalize ())
      System.out.print (r + "  ");
    System.out.println ();

    System.out.print ("Bad races: ");
    for (Race r : rot.normalize (new Division [] {Division.A, Division.B}))
      System.out.print (r + "  ");}
}
