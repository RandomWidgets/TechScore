package tscore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import regatta.Breakdown.BreakdownType;
import regatta.Finish.PlaceComparator;
import regatta.Penalty.PenaltyType;
import regatta.Regatta.Division;
import tscore.Factory;
import regatta.Finish;
import regatta.Regatta;
import regatta.Scorer;
import regatta.Race;
import regatta.Team;
import regatta.FinishAdjustment;
import regatta.Breakdown;
import regatta.Penalty;
import regatta.TeamPenalty;
import java.util.Comparator;

/**
 * Scoring per Intercollegeiate Sailing Associations (ICSA)
 * rules. These rules are described in <a
 * href="http://www.collegesailing.org/archive/2009-2012_PR_And_2009_CDCR.pdf">ICSA
 * Producedure Rules</a>.
 *
 * According to PR 20(c)(ii): <em>[...] Even though other boats may have
 * finished ahead of the BKD boat, such boats shall be scored as
 * having finished behind the RDG boat.</em> This means that in the
 * case of an assigned handicap, other boats get bumped down.
 *
 * This file is part of TechScore.
 * 
 * TechScore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TechScore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with TechScore.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created: Mon Jul 13 22:07:53 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class ICSAScorer implements Scorer {

  private Regatta regatta;  
  private Map<Team, String> rankExplanations;
  
  /**
   * Creates a new <code>ICSAScorer</code> instance.
   *
   */
  public ICSAScorer() {
    this.regatta = null;
  }

  // Implementation of regatta.Scorer

  /**
   * Describe <code>score</code> method here.
   *
   * @param regatta a <code>Regatta</code> value
   */
  public final void score(final Regatta regatta) {
    this.regatta = regatta;

    // Parameters
    final int FLEET = regatta.getTeams().length;

    // Finishes whose score needs to be averaged (and their actual
    // place finish)
    ArrayList<Finish> averageFinishes = new ArrayList<Finish>();
    ArrayList<Integer> averagePlaces  = new ArrayList<Integer>();
	
    ArrayList<Finish> places;
    Race [] races = regatta.getFinishedRaces();
    // For each race
    for (Race race : races) {
      places =
	new ArrayList<Finish>(Arrays.asList(regatta.getFinishes(race)));
      Collections.sort(places, new CleanPlaceComparator());

      // Go through the finishes in "places", and place the assigned
      // breakdowns where they belong, according to PR-20(c)(ii)
      for (int i = 0; i < places.size(); i++) {
	Finish f = places.get(i);
	FinishAdjustment adjustment = f.getAdjustment();
	if (adjustment != null &&
	    adjustment instanceof Breakdown) {
	  Breakdown bd = (Breakdown)adjustment;
	  // If the handicap is better than the deserved finish...
	  int hc = bd.getHandicap();
	  if (hc > 0 && hc <= i) {
	    places.remove(f);
	    places.add(hc - 1, f);
	  }
	}
      }

      int place = 1;
      for (Finish f: places) {
	FinishAdjustment adjustment = f.getAdjustment();
	// Regular finish
	if (adjustment == null) {
	  f.setScore(place++);
	}
	// Breakdown
	else if (adjustment instanceof Breakdown) {
	  Breakdown bd = (Breakdown)adjustment;
	  if (bd.getHandicap() > 0) {
	    f.setScore(place++);
	  }
	  else {
	    averageFinishes.add(f);
	    averagePlaces.add(new Integer(place++));
	  }
	}
	// Penalty
	else {
	  f.setScore(FLEET + 1);
	}
      }
    }
    
    // Deal with average finishes
    for (int i = 0; i < averageFinishes.size(); i++) {
      Finish finish = averageFinishes.get(i);
      Integer place = averagePlaces.get(i);
      Race race = finish.getRace();
      Team team = finish.getTeam();

      int total = 0;
      int num = 0;
      for (Race otherRace: races) {
	if (race.getDivision() == otherRace.getDivision() &&
	    !race.equals(otherRace)) {
	  // Get other score
	  int score = regatta.getFinish(otherRace, team).getScore();
	  if (score > 0) {
	    total += score;
	    num++;
	  }
	}
      }
      // Update score
      if (num == 0) {
	if (place == null) {
	  finish.setScore(FLEET);
	}
	else {
	  finish.setScore(place.intValue());
	}
      }
      else {
	// Use the average, if its not better than actual
	int avg = Math.round((float)total / (float)num);
	finish.setScore(Math.min(place, avg));
      }
    }
  }

  /**
   * Describe <code>rules</code> method here.
   *
   * @return a <code>String</code> value
   */
  public final String rules() {
    String rules = "";

    File file = new File("rules/ICSAsummary.txt");
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = reader.readLine()) != null) {
	rules += (line + "\n");
      }
      in.close();
    } catch (IOException x) {
      System.err.println(x);
    }

    return rules;
  }

  /**
   * Ranks the teams in the regatta across all divisions, using the
   * ICSA rules for tiebreakers and accounting for team penalties
   *
   * @param reg a <code>Regatta</code> value
   * @return a <code>Team[]</code> value
   */
  public Team [] rank(Regatta reg) {
    return rank(reg, reg.getDivisions());
  }

  private Team [] rank(Regatta reg, Division [] divs) {

    List<Team> teamList = Arrays.asList(reg.getTeams());
    List<Race> allRaces = new ArrayList<Race>();
    
    // Total the score for each team
    List<Integer> totalList = new ArrayList<Integer>(teamList.size());
    for (int t = 0; t < teamList.size(); t++) {
      int total = 0;
      Team team = teamList.get(t);
      for (Division div: divs) {
	Race [] divRaces = reg.getFinishedRaces(div);
	allRaces.addAll(Arrays.asList(divRaces));
	for (Race race : divRaces) {
	  Finish f = reg.getFinish(race, team);
	  total += f.getScore();
	}
	// Add team penalties, if needed
	TeamPenalty pen = reg.getTeamPenalty(div, team);
	if (pen != null) {
	  total += 20;
	}
      }
      totalList.add(new Integer(total));
    }

    // Order
    Factory.multiSort(totalList, teamList);

    // Fill explanations with default
    this.rankExplanations = new HashMap<Team, String>(teamList.size());
    for (Team team : teamList) {
      this.rankExplanations.put(team, "Natural order");
    }

    // Settle ties
    ArrayList<Team> newOrder = new ArrayList<Team>();
    int numTeams = teamList.size();
    int i = 0;
    while (i < numTeams) {
      ArrayList<Team> tiedTeams = new ArrayList<Team>();
      tiedTeams.add(teamList.get(i));
      Integer aScore = totalList.get(i);
      i++;
      while (i < numTeams) {
	Integer nextScore = totalList.get(i);
	if (!nextScore.equals(aScore)) {
	  break;
	}
	tiedTeams.add(teamList.get(i));
	aScore = nextScore;
	i++;
      }

      // Head to head ties
      settleHeadToHead(tiedTeams, reg, allRaces.toArray(new Race[]{}));
      newOrder.addAll(tiedTeams);
    }
    // Add the last team, if necessary
    if (newOrder.size() < numTeams) {
      newOrder.add(teamList.get(numTeams - 1));
    }

    return newOrder.toArray(new Team[]{});
  }

  /**
   * Reshuffle the list of teams so that they are ranked in order of
   * the number of times one of the teams scored better than another
   * of the teams in the list.
   *
   * @param reg a <code>Regatta</code> value
   */
  private void settleHeadToHead(ArrayList<Team> teams,
				Regatta reg, Race [] races) {
    int numTeams = teams.size();
    if (numTeams < 2) {
      return;
    }

    // Go through each race, and score just the tied teams
    HashMap<Team, Integer> headWins = new HashMap<Team, Integer>(numTeams);
    for (Team team : teams) {
      headWins.put(team, new Integer(0));
      this.rankExplanations.put(team, "Head-to-head tiebreaker");
    }
    for (Race race : reg.getFinishedRaces()) {
      ArrayList<Integer> scoreList = new ArrayList<Integer>(numTeams);
      ArrayList<Team> teamList = new ArrayList<Team>(teams);
      for (Team team : teams) {
	scoreList.add(new Integer(reg.getFinish(race, team).getScore()));
      }
      Factory.multiSort(scoreList, teamList);
      // Update headWins
      Integer thisScore = scoreList.get(0);
      int priorPlace = 0;
      Team key = teamList.get(0);
      headWins.put(key, new Integer(headWins.get(key).intValue() + priorPlace));
      for (int i = 1; i < numTeams; i++) {
	Integer nextScore = scoreList.get(i);
	key = teamList.get(i);
	int place = i;
	if (nextScore.equals(thisScore)) {
	  place = priorPlace;
	}
	headWins.put(key, new Integer(headWins.get(key).intValue() + place));
	
	// Reset variables
	priorPlace = place;
	thisScore = nextScore;
      }
    }

    // Rank the teams again
    ArrayList<Integer> scoreList = new ArrayList<Integer>(numTeams);
    int i = 0;
    for (Team team : headWins.keySet()) {
      teams.set(i++, team);
      scoreList.add(headWins.get(team));
    }
    Factory.multiSort(scoreList, teams);

    // Determine if there are more ties
    i = 0;
    int originalSpot = 0;
    while (i < numTeams) {
      ArrayList<Team> tiedTeams = new ArrayList<Team>();
      tiedTeams.add(teams.get(i));
      Integer aScore = scoreList.get(i);
      i++;
      while (i < numTeams) {
	Integer nextScore = scoreList.get(i);
	if (!nextScore.equals(aScore)) {
	  break;
	}
	// Update variables
	tiedTeams.add(teams.get(i));
	aScore = nextScore;
	i++;
      }

      rankMostHighFinishes(tiedTeams, reg, races, 1);

      // Update original list with these findings
      for (Team team : tiedTeams) {
	teams.set(originalSpot++, team);
      }
    }
  }

  /**
   * Recursive method for tiebreaking: rank the teams in order of
   * highest place finishes.
   *
   * @param reg a <code>Regatta</code> value
   * @param placeFinish an <code>int</code> value
   */
  private void rankMostHighFinishes(ArrayList<Team> teams,
				    Regatta reg,
				    Race [] races,
				    int placeFinish) {
    // Base cases
    if (teams.size() < 2) {
      return;
    }
    int fleetSize = reg.getTeams().length;
    if (placeFinish > fleetSize) {
      // There are still ties, go to the third tiebreaker
      rankByLastRace(teams, reg, races, reg.getFinishedRaces().length - 1);
      return;
    }

    // Work with copy of teams
    ArrayList<Team> teamList = new ArrayList<Team>(teams);

    int numTeams = teamList.size();
    int [] numHighFinishes = new int [numTeams];
    for (int t = 0; t < teamList.size(); t++) {
      Team team = teamList.get(t);
      this.rankExplanations.put(team, "More high-place finishes (" + placeFinish + ")");
      for (Race race : races) {
	if (reg.getFinish(race, team).getScore() == placeFinish) {
	  numHighFinishes[t]++;
	}
      }
    }

    // Rank according to most wins
    ArrayList<Integer> numWins = new ArrayList<Integer>(numTeams);
    for (int n : numHighFinishes) {
      numWins.add(new Integer(races.length - n));
    }
    Factory.multiSort(numWins, teamList);

    // Go through ranked list and remove those no longer in a tie
    int originalSpot = 0;
    int i = 0;
    while (i < numTeams) {
      Integer thisScore = numWins.get(i);
      ArrayList<Team> tiedTeams = new ArrayList<Team>(1);
      tiedTeams.add(teamList.get(i));
      i++;
      while (i < numTeams) {
	Integer nextScore = numWins.get(i);
	if (!thisScore.equals(nextScore))
	  break;
	tiedTeams.add(teamList.get(i));
	thisScore = nextScore;
	i++;
      }

      rankMostHighFinishes(tiedTeams, reg, races, placeFinish + 1);
      for (Team team : tiedTeams) {
	teams.set(originalSpot++, team);
      }
    }
  }

  private void rankByLastRace(ArrayList<Team> teams,
			      Regatta reg,
			      Race [] races,
			      int raceIndex) {
    int numTeams = teams.size();
    if (numTeams < 2) {
      return;
    }
    if (raceIndex < 0) {
      // Let's go alphabetical
      Collections.sort(teams);
      for (Team team : teams) {
	this.rankExplanations.put(team, "Alphabetical");
      }
      return;
    }

    // Get the last race
    Race lastRace = reg.getFinishedRaces()[raceIndex];
    ArrayList<Integer> scoreList = new ArrayList<Integer>(numTeams);
    for (Team team : teams) {
      scoreList.add(new Integer(reg.getFinish(lastRace, team).getScore()));
      this.rankExplanations.put(team, "According to last race (" + lastRace + ")");
    }
    Factory.multiSort(scoreList, teams);

    // Check for more ties
    ArrayList<Team> teamList = new ArrayList<Team>(teams);
    int i = 0;
    int originalSpot = 0;
    while (i < numTeams) {
      Integer thisScore = scoreList.get(i);
      ArrayList<Team> tiedTeams = new ArrayList<Team>(1);
      tiedTeams.add(teamList.get(i));
      i++;
      while (i < numTeams) {
	Integer nextScore = scoreList.get(i);
	if (!nextScore.equals(thisScore)) {
	  break;
	}
	tiedTeams.add(teamList.get(i));
	// Update variables
	thisScore = nextScore;
	i++;
      }

      // Resolve ties
      rankByLastRace(tiedTeams, reg, races, raceIndex - 1);
      for (Team team : tiedTeams) {
	teams.set(originalSpot++, team);
      }
    }
  }

  /**
   * Ranks the team in the given division.
   *
   * @param reg a <code>Regatta</code> value
   * @param d a <code>Regatta.Division</code> value
   * @return a <code>Team[]</code> value
   */
  public Team [] rank(Regatta reg, Regatta.Division d) {
    return rank(reg, new Division [] {d});
  }

  private void print(String message, ArrayList<Team> teams) {
    System.out.print(message + ": ");
    for (Team t : teams) {
      System.out.print(t);
    }
    System.out.println();
  }

  public Map<Team, String> getRankExplanations() {
    return this.rankExplanations;
  }

  public static void main (String [] args) {
    Regatta reg = new Regatta("Test");
    reg.createRaces(2, 4);
    Team a, b, c, d;
    reg.addTeam(a = new Team("A"));
    reg.addTeam(b = new Team("B"));
    reg.addTeam(c = new Team("C"));
    reg.addTeam(d = new Team("D"));

    Scorer scorer = new ICSAScorer();
    Calendar cal = Calendar.getInstance();

    // Add a bunch of finishes
    /*
    Race race = reg.getRace(Division.A, 1);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    race = reg.getRace(Division.A, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    race = reg.getRace(Division.A, 3);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    Finish f1, f2, f3, f4;
    race = reg.getRace(Division.A, 4);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(f2 = new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(f4 = new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    f2.setPenalty(new Penalty(PenaltyType.DSQ));
    f4.setPenalty(new Penalty(PenaltyType.OCS));
    */
    
    Race race = reg.getRace(Division.A, 1);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    race = reg.getRace(Division.A, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    race = reg.getRace(Division.B, 1);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    Finish f1, f2, f3, f4;
    race = reg.getRace(Division.B, 2);
    reg.setFinish(new Finish(race, b, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, a, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, d, cal.getTime()));
    cal.add(Calendar.SECOND, 2);
    reg.setFinish(new Finish(race, c, cal.getTime()));
    cal.add(Calendar.SECOND, 2);

    scorer.score(reg);
    reg.dumpFinishes();
    Team [] rankedTeams = scorer.rank(reg);
    Map<Team, String> explanation = scorer.getRankExplanations();
      
    for (Team team : rankedTeams) {
      System.out.println(team + ", " + explanation.get(team));
    }

    rankedTeams = scorer.rank(reg, Division.A);
    explanation = scorer.getRankExplanations();
      
    for (Team team : rankedTeams) {
      System.out.println(team + ", " + explanation.get(team));
    }

    rankedTeams = scorer.rank(reg, Division.B);
    explanation = scorer.getRankExplanations();
      
    for (Team team : rankedTeams) {
      System.out.println(team + ", " + explanation.get(team));
    }
  }

  /**
   * A comparator that places non-penalized finishes ahead of
   * penalized ones. If the races of this finish are different, then
   * it returns the finish from the earliest race. As a last resort,
   * if the timestamps are equal, then the finish with the top team
   * name lexographically is returned.
   */
  public static class CleanPlaceComparator implements Comparator<Finish> {
    // Implementation of java.util.Comparator
    public int compare(Finish f1, Finish f2) {
      int rel = f1.getRace().compareTo(f2.getRace());
      if (rel != 0) return rel;

      if (f2.getPenalty() != null) {
	return -1;
      }
      else if (f1.getPenalty() != null) {
	return 1;
      }

      // Check for 
      rel = f1.getTimestamp().compareTo(f2.getTimestamp());
      if (rel != 0) {
	return rel;
      }

      return f1.getTeam().compareTo(f2.getTeam());
    }
  }
}
