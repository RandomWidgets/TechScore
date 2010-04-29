package regatta;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import regatta.RP.BoatRole;
import regatta.Regatta.Division;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * A record of participation for a given regatta. Each record of
 * participation contains all the information regarding a set of teams
 * in a regatta such as the name of the sailor, school year (if
 * applicable), and the races sailed. Note that no guarantee is made
 * that the team and races held by this RP belong to the same
 * regatta. RP Forms must be registered with a regatta object by
 * calling the <code>setRP</code> method in Regatta.
 *
 *
 * Created: Thu Aug  6 09:58:31 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class RP {

  public static enum BoatRole {
    SKIPPER("Skip", "skipper"),
      CREW("Crew", "crew");
    private String name, desc;
    BoatRole(String n, String d) {
      this.name = n;
      this.desc = d;
    }
    public String toString() {
      return this.name;
    }
    public String desc() {
      return this.desc;
    }
  };

  /**
   * Participation map: maps a given sailor a list of races
   */
  private Map<Team, TreeMap<Race, Boat>> partMap;

  /**
   * Creates a new <code>RP</code> instance.
   *
   */
  public RP() {
    this.partMap = new HashMap<Team, TreeMap<Race, Boat>>();
  }

  /**
   * Resets the RP information for the given team, regardless of
   * whether the team exists or not.
   *
   * @param team a <code>Team</code> value
   */
  public void resetTeam(Team team) {
    this.partMap.put(team, new TreeMap<Race, Boat>());
  }

  public void removeSailor(Team team,
			   Sailor sailor,
			   List<Race> races,
			   BoatRole role) {
    Map<Race, Boat> map = this.partMap.get(team);
    if (map == null) {
      return;
    }
    for (Race race : races) {
      try {
	Boat boat = map.get(race);
	if (role == BoatRole.SKIPPER) {
	  boat.setSkipper(null);
	  System.out.println("Removed skipper " + sailor);
	}
	else {
	  Collection<Sailor> crews = boat.getCrews();
	  crews.remove(sailor);
	  System.out.println("Removed crew " + sailor);
	}
      } catch (NullPointerException e) {
	System.err.println("No RP data exists for the given team and race.");
      }
    }
  }

  /**
   * Registers the sailor the given team and list of races with the
   * given role in the boat (skipper or crew)
   *
   * @param sailor a <code>Sailor</code> value
   * @param race a <code>Race</code> value
   */
  public void addSailorRaces(Team team,
			     Sailor sailor,
			     List<Race> races,
			     BoatRole role) {
    TreeMap<Race, Boat> map = this.partMap.get(team);
    if (map == null) {
      map = new TreeMap<Race, Boat>();
      this.partMap.put(team, map);
    }
    for (Race race : races) {
      Boat boat = map.get(race);
      if (boat == null) {
	boat = new Boat();
      }
      if (role == BoatRole.SKIPPER) {
	boat.setSkipper(sailor);
      }
      else {
	boat.addCrew(sailor);
      }
      map.put(race, boat);
    }
  }

  /**
   * Returns array of races for this sailor in this team (races in
   * order), in the specified role.
   *
   * @param sailor a <code>Sailor</code> value
   * @return a <code>Race[]</code> value
   * @return <code>null</code> if no such sailor registered here.
   */
  public Race [] getRaces(Team team, Sailor sailor, BoatRole role) {
    Map<Race, Boat> map = this.partMap.get(team);
    if (map == null) {
      return null;
    }

    // Search for sailor in each race
    List<Race> races = new ArrayList<Race>();
    for (Race race : map.keySet()) {
      Boat boat = map.get(race);
      if (boat.inBoat(sailor)) {
	races.add(race);
      }
    }

    return races.toArray(new Race[]{});
  }

  /**
   * Returns a list of sailors in this team that have the specified
   * role in the boat
   *
   * @param team a <code>Team</code> value
   * @param role a <code>BoatRole</code> value
   * @return a <code>Sailor[]</code> value
   */
  public Sailor [] getSailor(Team team, BoatRole role) {
    Map<Race, Boat> map = this.partMap.get(team);
    if (map == null) {
      return new Sailor []{};
    }

    Set<Sailor> sailorSet = new HashSet<Sailor>();
    for (Race race : map.keySet()) {
      Boat boat = map.get(race);
      if (role == BoatRole.SKIPPER) {
	sailorSet.add(boat.getSkipper());
      }
      else {
	sailorSet.addAll(boat.getCrews());
      }
    }

    return sailorSet.toArray(new Sailor []{});
  }

  /**
   * Get list of (unique) sailors for the given team
   *
   * @param team a <code>Team</code> value
   * @return a <code>Sailor[]</code> value
   */
  public Sailor [] getSailors(Team team) {
    Map<Race, Boat> map = this.partMap.get(team);
    if (map == null) {
      return new Sailor []{};
    }
    
    Set<Sailor> sailorSet = new HashSet<Sailor>();
    for (Race race : map.keySet()) {
      Boat boat = map.get(race);
      sailorSet.addAll(boat.getSailors());
    }

    return sailorSet.toArray(new Sailor[]{});
  }
  
  /**
   * Get list of (unique) sailors for given team in given division,
   * organized by skippers first
   *
   * @param team a <code>Team</code> value
   * @param division a <code>Division</code> value
   * @return a <code>Sailor[]</code> value
   */
  public Sailor [] getSailors(Team team,
			      Division division,
			      BoatRole role) {
    Map<Race, Boat> map = this.partMap.get(team);
    if (map == null) {
      return new Sailor []{};
    }
    Set<Sailor> sailorSet = new TreeSet<Sailor>();
    for (Race race : map.keySet()) {
      if (race.getDivision() == division) {
	Boat boat = map.get(race);
	
	if (role == BoatRole.SKIPPER) {
	  Sailor skip = boat.getSkipper();
	  if (skip != null) {
	    sailorSet.add(skip);
	  }
	}
	else {
	  sailorSet.addAll(boat.getCrews());
	}
      }
    }
    return sailorSet.toArray(new Sailor[]{});
  }

  public void dump() {
    for (Team team : this.partMap.keySet()) {
      System.out.println(team + ":");
      Map<Race, Boat> map = this.partMap.get(team);
      for (Race race : map.keySet()) {
	Boat boat = map.get(race);
	System.out.println(race + ":" + boat.toString());
      }
      System.out.println();
    }
  }

  /**
   * Returns the BoatRole object that matches the string value.
   *
   * @param role a <code>String</code> value
   * @return a <code>BoatRole</code> value
   */
  public static BoatRole parseRole(String role) {
    for (BoatRole r : BoatRole.values()) {
      if (r.toString().equalsIgnoreCase(role)) {
	return r;
      }
      if (r.desc().equalsIgnoreCase(role)) {
	return r;
      }
    }
    return null;
  }

  /**
   * A list of at least one sailor, who is a skipper, and any number
   * of crews.
   *
   */
  private class Boat {
    private Sailor skipper;
    private Set<Sailor> crews;

    private Boat() {
      this.crews = new HashSet<Sailor>(1);
    }

    private Sailor getSkipper() {
      return this.skipper;
    }

    private Collection<Sailor> getCrews() {
      return this.crews;
    }

    private void addCrew(Sailor sailor) {
      this.crews.add(sailor);
    }

    private void setSkipper(Sailor sailor) {
      this.skipper = sailor;
    }

    private boolean inBoat(Sailor sailor) {
      return this.skipper == sailor ||
	this.crews.contains(sailor);
    }

    private boolean hasSkipper() {
      return this.skipper != null;
    }

    private Collection<Sailor> getSailors() {
      Set<Sailor> set = new HashSet<Sailor>(this.crews);
      set.add(this.skipper);
      return set;
    }

    public String toString() {
      String rep = "S:" + this.skipper;
      for (Sailor sailor : this.crews) {
	rep += ",C:" + sailor;
      }
      return rep;
    }
  }
}
