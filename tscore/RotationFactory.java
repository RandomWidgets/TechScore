package tscore;

import regatta.Regatta;
import regatta.RegattaListener;
import regatta.RegattaEvent;
import regatta.Rotation;
import java.util.ArrayList;
import regatta.Regatta.Division;
import regatta.Race;
import java.util.TreeMap;
import regatta.Team;
import regatta.Sail;
import regatta.Rotation.RotationType;
import regatta.Rotation.RotationStyle;
import java.util.Arrays;
import java.util.Collections;

/**
 * Creates and manages a rotation for a given regatta, remembering the
 * options and providing a method to <code>update</code> the rotation
 * created. Objects of this class also implement the RegattaListener
 * interface and will update the rotation if a <code>RACE</code>
 * RegattaEvent is registered.
 *
 * Rotations created with this factory can be of either
 * <code>Type</code> but must not be of the <code>Franny</code>
 * style.
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
 * Created: Mon Jun 29 09:41:48 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RotationFactory {

  protected Regatta regatta;
  protected RotationType rotType;
  protected RotationStyle rotStyle;
  protected int [] sailInts;
  protected int setSize, actualSetSize;
  protected ArrayList<Race> orderedRaces;
  protected ArrayList<Team> teams;
  protected ArrayList<Sail> sails;
  
  /**
   * Creates a new <code>RotationFactory</code> instance.
   *
   * @param reg a <code>Regatta</code> value
   * @param type a <code>RotationType</code> value
   * @param sails an <code>ArrayList<Integer></code> of sail numbers
   * to use
   * @param set the number of races per set
   */
  public RotationFactory(Regatta reg,
			 RotationType type,
			 RotationStyle style,
			 int [] sails,
			 int set) {
    this.regatta  = reg;
    this.rotType  = type;
    this.rotStyle = style;
    this.sailInts = sails;
    this.setSize  = set;
  }

  /**
   * Recalculates the rotation for the regatta.
   *
   * @exception IllegalArgumentException if an error occurs
   */
  public void update()
    throws IllegalArgumentException {
    
    this.prep();
    this.prepTeams();
    this.regatta.setRotation(this.createRotation());
  }

  /**
   * Actually creates the rotation. Children of this class should
   * override this method instead of overriding "update".
   *
   * @return a <code>Rotation</code>.
   */
  protected Rotation createRotation() {
    Rotation rot = new Rotation();

    int swapWith = 1;
    boolean moreRaces = true;
    while (moreRaces) {
      for (int i = 0; i < actualSetSize; i++) {
	try {
	  Race race = orderedRaces.remove(0);
	  for (int t = 0; t < teams.size(); t++) {
	    Team team = teams.get(t);
	    Sail sail = sails.get(t);
	    rot.setSail(race, team, sail);
	  }
	} catch (IndexOutOfBoundsException e) {
	  moreRaces = false;
	}
      }
      if (rotType == RotationType.STANDARD)
	shift(sails);
      else if (rotType == RotationType.SWAP) {
	swap(sails, swapWith % teams.size());
	swapWith += 2;
      }
    }
    
    return rot;
  }

  protected static void fillRotation(Rotation rot,
				     RotationStyle style,
				     ArrayList<Race> races) {
    
  }
			      

  // Helper methods for rotations
  protected void swap(ArrayList<Sail> l, int dist) {
      int length = l.size();
    for (int i = 0; i < length; i += 2) {
	int j = (i + dist) % length;
	l.set(j, l.set(i, l.get(j)));
    }
  }
  protected void shift(ArrayList<Sail> l) {
    l.add(l.remove(0));
  }

  /**
   * Helper method. Sets up the orderedRaces protected variable to be
   * contain the regatta's races in the appropriate order for the
   * chosen rotation. It also sets up the set size and other
   * parameters.
   */
  protected void prep() {
    Division [] divisions = regatta.getDivisions();
    int numRaces = regatta.getNumRaces();
    int numDivs  = divisions.length;
    Race [][] raceMap = new Race[numRaces][numDivs];
    // Fill raceMap
    for (int d = 0; d < divisions.length; d++) {
      raceMap[d] = regatta.getRaces(divisions[d]);
    }
    
    this.orderedRaces = new ArrayList<Race>(regatta.getNumRaces() *
					    divisions.length);
    // un-collate the races into orderedRaces
    int start = 0;
    while (start < numRaces) {
      for (int d = 0; d < numDivs; d++) {
	int s = 0;
	while (s < setSize && start + s < numRaces) {
	  orderedRaces.add(raceMap[d][start + s]);
	  s++;
	}
      }
      start += setSize;
    }

    // update set size if necessary
    if (rotStyle == RotationStyle.NONE)
      actualSetSize = setSize * divisions.length;
    else
      actualSetSize = setSize;
  }

  /**
   * Prepares the list of sails and teams so that the teams are in
   * order of starting sail number
   */
  protected void prepTeams()
    throws IllegalArgumentException {
    // Order teams by sail numbers
    Team [] teamList = this.regatta.getTeams();
    TreeMap<Sail, Team> teamMap = new TreeMap<Sail, Team>();
    for (int i = 0; i < this.sailInts.length; i++) {
      if (teamMap.put(new Sail(sailInts[i]), teamList[i]) != null) {
	throw new IllegalArgumentException("Non-unique sail(s)");
      }
    }
    sails = new ArrayList<Sail>(teamMap.keySet());
    teams = new ArrayList<Team>(teamMap.values());

  }

  public static void main (String [] args) {
    ArrayList<Sail> list = new ArrayList<Sail>(Arrays.asList(new Sail[]{new Sail(1),
									new Sail(2),
									new Sail(3),
									new Sail(4),
									new Sail(5),
									new Sail(6)}));

  }
}
