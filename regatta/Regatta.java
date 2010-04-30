package regatta;

import java.util.Date;
import regatta.Regatta.Division;
import java.util.TreeSet;
import java.util.ArrayList;
import regatta.RegattaEvent.RegattaEventType;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Map;
import regatta.Penalty.PenaltyType;
import regatta.Penalty;
import java.util.Calendar;

import java.util.List;
import java.util.Arrays;
import regatta.RP.BoatRole;
import java.util.Collections;

/**
 * Encapsulates sailing regatta objects.
 *
 *
 * Created: Wed Jun  3 15:11:12 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class Regatta {
  // Public variables
  public static enum RegattaType {
    PERSONAL("Personal"),
      CONFERENCE("Conference"),
      CHAMPIONSHIP("Championship"),
      INTERSECTIONAL("Intersectional");
    private String name;
    RegattaType(String n) {
      this.name = n;
    }
    public String toString() {
      return this.name;
    }
  };
  public static enum Division {
    A, B, C, D, E, F, G, H, I,
      J, K, L, M, N, O, P, Q, R,
      S, T, U, V, W, X, Y, Z};
  
  // Private variables
  private String name, boat, blurb;
  private RegattaType type;
  private Date starttime;
  private int duration; // days
  private ArrayList<RegattaListener> regattaListeners;

  private int numDivisions, numRaces;
  private ArrayList<Race> races;
  private TreeSet<Team> teams;
  private Map<Race, Set<Finish>> finishes;
  private Rotation rotation = null;
  private RP rp = null;

  private Map<Division, Map<Team, TeamPenalty>> teamPenaltyMap;

  /**
   * Creates a new <code>Regatta</code> instance.
   *
   * @param name a <code>String</code> value
   */
  public Regatta(String name) {
    this.regattaListeners = new ArrayList<RegattaListener>();
    this.teams = new TreeSet<Team>();
    this.finishes = new TreeMap<Race, Set<Finish>>();
    this.setName(name);
    this.setDuration(1);
    this.setStartTime(getNextWeekend());
    this.setType(RegattaType.PERSONAL);
    this.teamPenaltyMap =
      new TreeMap<Division, Map<Team, TeamPenalty>>();

    numDivisions = 0;
    numRaces     = 0;
  }

  // Regatta fields
  
  /**
   * Set the start time for the regatta
   * @param d a <code>Date</code> value
   */
  public void setStartTime(Date d) {
    this.starttime = d;
    this.fireRegattaChange(RegattaEventType.DETAILS);
  }
  /**
   * Describe <code>getStartTime</code> method here.
   *
   * @return a <code>Date</code> value
   */
  public Date getStartTime() {return this.starttime;}

  /**
   * Set regatta type
   *
   * @param t a <code>RegattaType</code> value
   * @exception IllegalArgumentException if invalid regatta type
   */
  public void setType(RegattaType t) {
    this.type = t;
    this.fireRegattaChange(RegattaEventType.DETAILS);
  }
  /**
   * Describe <code>getType</code> method here.
   *
   * @return a <code>RegattaType</code> value
   */
  public RegattaType getType() {return this.type; }

  /**
   * Describe <code>setName</code> method here.
   *
   * @param n a <code>String</code> value
   */
  public void setName(String n) {
    this.name = n;
    this.fireRegattaChange(RegattaEventType.NAME);
  }
  /**
   * Describe <code>getName</code> method here.
   *
   * @return a <code>String</code> value
   */
  public String getName() {return this.name;}

  /**
   * Describe <code>setDuration</code> method here.
   *
   * @param n an <code>int</code> value
   */
  public void setDuration(int n) {
    if (n < 1) {
      throw new IllegalArgumentException("Duration must be at least 1.");
    }
    this.duration = n;
    this.fireRegattaChange(RegattaEventType.DETAILS);
  }
  /**
   * Describe <code>getDuration</code> method here.
   *
   * @return an <code>int</code> value
   */
  public int getDuration() {return this.duration;}

  /**
   * Describe <code>setBlurb</code> method here.
   *
   * @param s a <code>String</code> value
   */
  public void setBlurb(String s) {
    this.blurb = s;
    this.fireRegattaChange(RegattaEventType.DETAILS);
  }
  /**
   * Describe <code>getBlurb</code> method here.
   *
   * @return a <code>String</code> value
   */
  public String getBlurb() {return this.blurb;}

  
  /*
   * Divisions and races
   */
  /**
   * Sets the number of divisions in the regatta. If
   * <code>divCount</code> is equal to the current number of
   * divisions, do nothing. If greater, then new divisions are created
   * with as many races as currently exist in the other divisions. If
   * less, then the higher divisions are eliminated (as are their
   * races) until the two numbers match.
   *
   * @param divCount the new total number of divisions for this regatta
   * @throws IllegalArgumentException if <code>divCount</code>is too
   * great or too small (1 <= d <= POSDIVS).
   * @throws NullPointerException if there are no races
   */
  public void updateDivisions(int divCount)
    throws IllegalArgumentException, NullPointerException {
    Division [] posDivs = Division.values();
    if (divCount > posDivs.length || divCount <= 0) {
      throw new IllegalArgumentException("Regatta must have 1-" +
					 posDivs.length +
					 " divisions");
    }
    if (this.numRaces == 0) {
      throw new NullPointerException("There are no races.");
    }
    if (this.numDivisions == divCount) {
      return;
    }

    // Add the difference
    for (int i = this.numDivisions; i < divCount; i++) {
      for (int j = 0; j < this.numRaces; j++) {
	this.races.add(new Race(posDivs[i], (j+1)));
      }
    }
    // Remove the difference
    int newSize = divCount * this.numRaces;
    while (this.races.size() > newSize) {
      this.races.remove(newSize);
    }
    this.numDivisions = divCount;

    this.fireRegattaChange(RegattaEventType.RACE);
  }

  /**
   * Sets the number of races in each division in the regatta. if
   * <code>raceCount</code> is equal to the current number of races,
   * do nothing. If greater, then new races are added to each
   * division. If less, then the higher-numbered races are
   * eliminated.
   *
   * @param raceCount the new total number of races in each division
   * @exception IllegalArgumentException if <code>raceCount</code> is
   * less than 1.
   * @exception NullPointerException if an error occurs
   */
  public void updateRaces(int raceCount)
    throws IllegalArgumentException, NullPointerException {
    if (raceCount <= 0) {
      throw new IllegalArgumentException("There must be at least once race " +
					 "in each division.");
    }

    if (this.numRaces == 0) {
      throw new NullPointerException("There are no races.");
    }
    if (this.numRaces == raceCount) {
      return;
    }
    Division [] divs = this.getDivisions();
    for (int d = this.numDivisions; d > 0; d--) {
      int addIndex = d * this.numRaces;
      int remIndex = d * raceCount;
      // Add races
      for (int r = raceCount; r > this.numRaces; r--) {
	this.races.add(addIndex, new Race(divs[d - 1], r));
      }
      // Remove races
      for (int r = this.numRaces; r > raceCount; r--) {
	this.races.remove(remIndex);
      }
    }
    this.numRaces = raceCount;
    this.fireRegattaChange(RegattaEventType.RACE);
  }
  
  /**
   * Set (or replace) the number of divisions and races. This should
   * be the first call to create races.
   *
   * @param divCount  the number of divisions to add
   * @param raceCount the number of races to add in each division
   */
  public void createRaces(int divCount, int raceCount) {
    Regatta.Division [] posDivs = Regatta.Division.values();
    if (divCount > posDivs.length || divCount <= 0) {
      throw new IllegalArgumentException("Regatta must have 1-" +
					 posDivs.length +
					 " divisions");
    }
    this.numDivisions = divCount;
    this.numRaces     = raceCount;
    this.races = new ArrayList<Race>(divCount * raceCount);
    for (int d = 0; d < divCount; d++) {
      for (int r = 0; r < raceCount; r++) {
	this.races.add(new Race(posDivs[d], (r+1)));
      }
    }
  }

  /**
   * Returns array of divisions
   * @return a <code>Regatta.Division[]</code> value
   */
  public Division [] getDivisions() {
    Division [] posDivs = Division.values();
    Division [] divs = new Division[this.numDivisions];
    for (int i = 0; i < this.numDivisions; i++) {
      divs[i] = posDivs[i];
    }
    return divs;
  }

  /**
   * Get number of divisions in regatta
   * @return an <code>int</code> value
   */
  public int getNumDivisions() {
    return this.numDivisions;
  }
  /**
   * Get number of races per division
   *
   * @return number of races per division
   */
  public int getNumRaces() {
    return this.numRaces;
  }
  /**
   * Returns the race in this division, and with this number
   *
   * @param d a <code>Regatta.Division</code> value
   * @param num an <code>int</code> value
   * @return a <code>Race</code> value
   * @exception IllegalArgumentException if bogus
   */
  public Race getRace(Division d, int num)
    throws IllegalArgumentException {

    if (num <= 0 || num > this.numRaces) {
      throw new IllegalArgumentException("Invalid race number: " + num);
    }

    // Determine the index of the race
    int index = d.ordinal() * this.numRaces + num - 1;
    try {
      return this.races.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Invalid race " + d + num);
    }
  }

  /**
   * Return array of races in the specified division. The races will
   * be in order.
   *
   * @param d a <code>Division</code> value
   * @return a <code>Race[]</code> array of races
   */
  public Race [] getRaces(Division d) {
    int start = d.ordinal() * this.numRaces;

    Race [] r = new Race[this.numRaces];
    for (int i = 0; i < numRaces; i++ ){
      r[i] = this.races.get(start + i);
    }
    return r;
  }

  /**
   * Returns array of all the races in regatta in order.
   *
   * @return a <code>Race[]</code> of the races.
   */
  public Race [] getRaces() {
    return this.races.toArray(new Race [] {});
  }

  /*
   * Teams
   */
  /**
   * Get the number of teams.
   *
   * @return an <code>int</code> value
   */
  public int getNumTeams() {
    return this.teams.size();
  }
  /**
   * Adds team to regatta.
   *
   * @param team a <code>Team</code> value
   * @return a <code>boolean</code> value
   */
  public boolean addTeam(Team team) {
    boolean t = this.teams.add(team);
    if (t)
      this.fireRegattaChange(RegattaEventType.TEAM);
    return t;
  }
  /**
   * Remove the specified team from the regatta.
   *
   * @param team a <code>Team</code> value
   * @return a <code>boolean</code> value
   */
  public boolean removeTeam(Team team) {
    boolean t = this.teams.remove(team);
    if (t) {
      this.fireRegattaChange(RegattaEventType.TEAM);
      if (this.rotation != null) {
	this.rotation.removeTeam(team);
	this.fireRegattaChange(RegattaEventType.ROTATION);
      }
    }
    return t;
  }
  /**
   * Returns array of teams in order.
   *
   * @return array of teams in natural name order
   */
  public Team [] getTeams() {
    return this.teams.toArray(new Team [] {});
  }

  /*
   * Rotations
   */
  /**
   * Describe <code>setRotation</code> method here.
   *
   * @param rot a <code>Rotation</code> value
   */
  public void setRotation(Rotation rot) {
    this.rotation = rot;
    this.fireRegattaChange(RegattaEventType.ROTATION);
  }
  /**
   * Get the rotation object for this regatta
   *
   * @return a <code>Rotation</code> value
   */
  public Rotation getRotation() { return this.rotation; }

  /*
   * RP Forms
   */
  
  /**
   * Get the RP manager for this regatta
   *
   * @param rp a <code>RP</code> value
   */
  public void setRP(RP rp) {
    this.rp = rp;
  }

  /**
   * Get the RP manager for this regatta
   *
   * @return a <code>RP</code> value
   */
  public RP getRP() { return this.rp; }

  /*
   * Finishes
   */

  /**
   * Adds/updates the finish to this regatta.
   *
   * @param f a <code>Finish</code> value
   */
  public void setFinish(Finish f) {
    Race race = f.getRace();
    Set<Finish> set = this.finishes.get(race);
    if (set == null) {
      set = new HashSet<Finish>();
      this.finishes.put(race, set);
    }
    set.remove(f);
    set.add(f);
  }

  public void dumpFinishes() {
    Set<Race> raceSet = this.finishes.keySet();
    for (Race race: raceSet) {
      System.out.print("Race: " + race + ": ");
      Set<Finish> set = this.finishes.get(race);
      for (Finish f: set) {
	System.out.print("(" + f.getTeam() + ": " + f.getScore() + ") ");
      }
      System.out.println();
    }

  }

  /*
   * Team penalties
   */
  /**
   * Adds/replaces the team penalty
   *
   * @param p a <code>TeamPenalty</code> value
   */
  public void setTeamPenalty(TeamPenalty p) {
    Division theDiv = p.getDivision();
    Map<Team, TeamPenalty> map = this.teamPenaltyMap.get(theDiv);
    if (map == null) {
      map = new TreeMap<Team, TeamPenalty>();
      this.teamPenaltyMap.put(theDiv, map);
    }
    map.put(p.getTeam(), p);
  }

  /**
   * Returns the team penalty for the given team in the given
   * division, if it exists, <code>null</code> otherwise
   *
   * @param div a <code>Division</code> value
   * @param team a <code>Team</code> value
   * @return a <code>TeamPenalty</code> value, or <code>null</code> if
   * no penalty for the given team and division
   */
  public TeamPenalty getTeamPenalty(Division div, Team team) {
    Map<Team, TeamPenalty> map = this.teamPenaltyMap.get(div);
    if (map == null) {
      return null;
    }
    return map.get(team);
  }

  /**
   * Gets a list of all team penalties ordered by division and team.
   *
   * @return a <code>TeamPenalty[]</code> value
   */
  public TeamPenalty [] getTeamPenalties() {
    List<TeamPenalty> penalties =
      new ArrayList<TeamPenalty>(this.teamPenaltyMap.size());
    for (Division div : this.teamPenaltyMap.keySet()) {
      penalties.addAll(this.teamPenaltyMap.get(div).values());
    }
    return penalties.toArray(new TeamPenalty[]{});
  }

  /**
   * Removes the team penalty for the given team in the given division
   * from the list of penalties
   *
   * @param pen a <code>TeamPenalty</code> value
   */
  public void removeTeamPenalty(Division div, Team team) {
    Map<Team, TeamPenalty> map = this.teamPenaltyMap.get(div);
    if (map != null) {
      map.remove(team);
    }
  }

  /**
   * Inspects the finishes for races with missing teams. Returns list
   * of guilty races.
   *
   * @return a <code>Race[]</code> value
   */
  public Race [] normalizeFinishes() {
    List<Race> badRaces = new ArrayList<Race>();
    for (Race race : this.finishes.keySet()) {
      if (this.finishes.get(race).size() != this.teams.size()) {
	badRaces.add(race);
      }
    }
    return badRaces.toArray(new Race[]{});
  }

  /**
   * Removes the finish for the given team in the given race.
   *
   * @param race a <code>Race</code> value
   * @param team a <code>Team</code> value
   */
  public void removeFinish(Race race, Team team) {
    this.removeFinish(new Finish(race, team));
  }

  /**
   * Removes the specified finish (or one like it) from this
   * regatta.
   *
   * @param f a <code>Finish</code> value
   */
  public void removeFinish(Finish f) {
    this.finishes.get(f.getRace()).remove(f);
  }

  /**
   * Removes all finishes associated with this race.
   *
   * @param race a <code>Race</code> value
   */
  public void removeFinishes(Race race) {
    this.finishes.remove(race);
  }

  /**
   * Gets the finish for specified race and team
   *
   * @param race a <code>Race</code> value
   * @param team a <code>Team</code> value
   * @return a <code>Finish</code> value
   * @return null if no such finish exists
   */
  public Finish getFinish(Race race, Team team) {
    Set<Finish> set = this.finishes.get(race);
    if (set == null) {
      return null;
    }
    Iterator<Finish> iter = set.iterator();
    while (iter.hasNext()) {
      Finish finish = iter.next();
      if (finish.getTeam().equals(team)) {
	return finish;
      }
    }
    return null;
  }

  public Finish [] getFinishes(Race race) {
    Set<Finish> set = this.finishes.get(race);
    if (set == null) {
      return null;
    }
    return set.toArray(new Finish [] {});
  }

  /**
   * Returns an array of races with finishes
   *
   * @return a <code>Race[]</code> value
   */
  public Race [] getFinishedRaces() {
    return this.finishes.keySet().toArray(new Race[] {});
  }

  /**
   * Returns an array of races with finishes in the given division
   *
   * @param division a <code>Division</code> value
   * @return a <code>Race[]</code> value
   */
  public Race [] getFinishedRaces(Division division) {
    List<Race> races = new ArrayList<Race>();
    for (Race race : this.finishes.keySet()) {
      if (race.getDivision() == division) {
	races.add(race);
      }
    }
    return races.toArray(new Race[] {});
  }
  
  /*
   * Regatta listeners: inform whatever class is so interested that
   * something about the regatta has changed.
   */
  
  /**
   * Adds a regatta listener.
   * @param l a <code>RegattaListener</code> value
   */
  public void addRegattaListener(RegattaListener l) {
    this.regattaListeners.add(l);
  }
  
  /**
   * Removes the specified listener
   * @param l a <code>RegattaListener</code> value
   */
  public void removeRegattaListener(RegattaListener l) {
    this.regattaListeners.remove(l);
  }

  /**
   * Fires the specified regatta change event. See RegattaEvent
   * @param regEvent a <code>RegattaEventType</code> value
   */
  private void fireRegattaChange(RegattaEventType regEvent) {
    RegattaEvent evt = new RegattaEvent(this, regEvent, this);
    this.fireRegattaChange(evt);
  }

  public void fireRegattaChange(RegattaEvent evt) {
    for (RegattaListener l: this.regattaListeners) {
      l.regattaChange(evt);
    }
  }

  /**
   * Returns a <code>Date</code> value for the next saturday.
   *
   * @return a <code>Date</code> value
   */
  private static Date getNextWeekend() {
    Calendar now = Calendar.getInstance();
    int weekday = now.get(Calendar.DAY_OF_WEEK);
    if (weekday == Calendar.SATURDAY &&
	weekday != Calendar.SUNDAY) {
      // Choose the next saturday at 10:00 AM
      now.add(Calendar.DAY_OF_WEEK, (7 + Calendar.SATURDAY - weekday) % 7);
    }

    // Choose 10 for the hour
    now.add(Calendar.HOUR_OF_DAY, 10 - now.get(Calendar.HOUR_OF_DAY));
    now.add(Calendar.MINUTE, 0 - now.get(Calendar.MINUTE));
    now.add(Calendar.SECOND, 0 - now.get(Calendar.SECOND));

    return now.getTime();
  }

  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String</code> value
   */
  public static void main (String [] args) {
    /*
    Regatta reg = new Regatta("Test");
    reg.createRaces(1, 3);
    Race race = reg.getRace(Division.A, 1);
    Team team = new Team("MIT", "");
    reg.addTeam(team);

    Finish f1, f2;
    f1 = new Finish(race, team);
    f2 = new Finish(race, team);

    reg.setFinish(f1);
    reg.dumpFinishes();
    f1.setPenalty(new Penalty(PenaltyType.DSQ, ""));
    System.out.println(f1);
    */

    // Test RP stuff
    Regatta reg = new Regatta("Test");
    reg.createRaces(2, 4);
    Team t1, t2;
    reg.addTeam(t1 = new Team("One"));
    reg.addTeam(t2 = new Team("Two"));

    RP rpInfo = new RP();
    Race [] races = reg.getRaces();

  }
}
