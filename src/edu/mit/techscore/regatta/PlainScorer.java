package edu.mit.techscore.regatta;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.Iterator;
import edu.mit.techscore.regatta.Finish.PlaceComparator;
import java.util.ArrayList;
import edu.mit.techscore.regatta.PlainScorer.FinishComparator;
import java.util.Arrays;
import edu.mit.techscore.tscore.Factory;
import java.util.List;


/**
 * Describe class PlainScorer here.
 *
 *
 * Created: Wed Jul 15 17:34:02 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class PlainScorer implements Scorer {

  private Regatta regatta;
  private ArrayList<Race> scoredRaces = new ArrayList<Race>();

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

    TreeSet<Finish> places;
    Race [] races = regatta.getFinishedRaces();
    for (int i = 0; i < races.length; i++) {
      if (!scoredRaces.contains(races[i])) {
	System.out.println("Scoring race " + races[i]);
	places = new TreeSet<Finish>(new Finish.PlaceComparator());
	places.addAll(regatta.getFinishes(races[i]));

	int place = 1;
	Iterator<Finish> iter = places.iterator();
	while (iter.hasNext()) {
	  Finish f = iter.next();
	  if (f.getPenalty() != null) {
	    f.setScore(FLEET + 1);
	  }
	  /*
	    else if (f.getBreakdown() != null) {
	    f.setScore();
	    }
	  */
	  else {
	    f.setScore(place++);
	  }
	}
      } // end of scoredRaces check
    }
  }

  /**
   * Describe <code>rules</code> method here.
   *
   * @return a <code>String</code> value
   */
  public final String rules() {
    String mes = "Breakdowns mean nothing to me. Your score is\n" +
      "simply equal to your place crossing the\n" +
      "finish line, barring penalties. Scores only\n" +
      "affect the current race.";
    return mes;
  }

  /**
   * Describe <code>rank</code> method here.
   *
   * @param reg a <code>Regatta</code> value
   * @return a <code>Team[]</code> value
   */
  public Team [] rank(Regatta reg) {

    List<Team> teamList = Arrays.asList(reg.getTeams());
    Race [] races = reg.getFinishedRaces();

    // Total the score for each team
    List<Integer> totalList = new ArrayList<Integer>(teamList.size());
    for (int t = 0; t < teamList.size(); t++) {
      int total = 0;
      for (int r = 0; r < races.length; r++) {
	Finish f = reg.getFinish(races[r], teamList.get(t));
	total += f.getScore();
      }
      totalList.add(new Integer(total));
    }

    // Order
    Factory.multiSort(totalList, teamList);
    return teamList.toArray(new Team[]{});
  }

  public Team [] rank(Regatta reg, Regatta.Division d) {
    return reg.getTeams();
  }

  class FinishComparator implements Comparator<Finish> {
    // Implementation of java.util.Comparator
    public int compare(Finish f1, Finish f2) {
      // Compare races first
      int rel = f1.getRace().compareTo(f2.getRace());
      if (rel != 0) {
	return rel;
      }

      // If f1 has penalty, the it comes after
      if (f2.getPenalty() != null) {
	return 1;
      }
      if (f1.getPenalty() != null) {
	return -1;
      }

      // Compare timestamps
      return f1.getTimestamp().compareTo(f2.getTimestamp());
    }
  }
  
}
