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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a rotation builder that understands all the
 * rotation styles and types.
 *
 * 2010-04-24: Sail numbers need not be numerical. To accomplish this,
 * users need to specify whether the sails should be sorted prior to
 * creating the rotation, and this sorting can be done either
 * numerically, or alpha-numerically (lexically).
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
 * @version 1.5
 */
public class ICSARotationBuilder {

  private int offset;

  public ICSARotationBuilder() {
    this(-1);
  }

  public ICSARotationBuilder(int offset) {
    this.offset = offset;
  }

  /**
   * Actually creates the rotation. Children of this class should
   * override this method instead of overriding "update".
   *
   * @return a <code>Rotation</code>.
   */
  public void fillRotation(Rotation rotation,
			   RotationType type,
			   RotationStyle style,
			   Race [][] raceMap,
			   Team [] teams,
			   Sail [] sails,
			   int setSize)
    throws RotationBuilderException {
    
    if (teams.length != sails.length) {
      throw new RotationBuilderException("Sails and teams must be equal length.");
    }
    if (setSize < 1) {
      throw new RotationBuilderException("Set size must be >= 1.");
    }

    List<Sail> sailList = new ArrayList<Sail>(Arrays.asList(sails));
    List<Race> orderedRaces = null;

    // OFFSET STYLE
    if (style == RotationStyle.FRANNY) {
      // Calculate offset
      int actualOffset = this.offset;
      if (actualOffset < 0) {
	actualOffset = teams.length / raceMap.length;
      }

      // First fill the first division of races
      for (Race [] div : raceMap) {
	fillRotation(rotation,
		     type,
		     RotationStyle.NONE,
		     new Race [][]{div},
		     teams,
		     sailList.toArray(new Sail[]{}),
		     setSize);
	// Shift sails accordingly
	for (int i = 0; i < actualOffset; i++) {
	  shift(sailList);
	}
      }

    }
    // INDIVIDUAL or COLLATED:
    else if (style == RotationStyle.NONE ||
	     style == RotationStyle.NAVY) {
      orderedRaces = confuseRaces(raceMap, setSize);

      // Set the actual set size, depending on rotation style
      int actualSetSize = setSize;
      if (style == RotationStyle.NONE)
	actualSetSize = setSize * raceMap.length;

      int swapWith = 1;
      for (int globalRaceIndex = 0;
	   globalRaceIndex < orderedRaces.size();
	   globalRaceIndex += actualSetSize) {

	for (int i = 0; i < actualSetSize; i++) {
	  Race race = orderedRaces.get(globalRaceIndex + i);
	  if (race != null) {
	    for (int t = 0; t < teams.length; t++) {
	      Team team = teams[t];
	      Sail sail = sailList.get(t);
	      rotation.setSail(race, team, sail);
	    }
	  }
	}
	if (type == RotationType.STANDARD)
	  shift(sailList);
	else if (type == RotationType.SWAP) {
	  swap(sailList, swapWith % teams.length);
	  swapWith += 2;
	}
	// else, if rotType == STATIC, don't do anything
      }
    }
    else {
      throw new RotationBuilderException("Unrecognized rotation style: " + style);
    }
  }

  // Helper methods for rotations
  private void swap(List<Sail> l, int dist) {
      int length = l.size();
    for (int i = 0; i < length; i += 2) {
	int j = (i + dist) % length;
	l.set(j, l.set(i, l.get(j)));
    }
  }
  private void shift(List<Sail> l) {
    l.add(l.remove(0));
  }

  private List<Race> flattenRaces(Race [][] map, int setSize) {
    if (map.length == 0) {
      return new ArrayList<Race>();
    }

    int numSets = (int)Math.ceil((double)map[0].length / (double)setSize);
    List<Race> ordered = new ArrayList<Race>(map.length * numSets * setSize);
    for (Race [] div : map) {
      int raceIndex = 0;
      for (Race race : div) {
	raceIndex++;
	ordered.add(race);
      }
      while (raceIndex < numSets * setSize) {
	raceIndex++;
	ordered.add(null);
      }
    }

    return ordered;
  }

  /**
   * Helper method. Confuses the races in the race map into a list of
   * races of setSize at a time, cycling through the divisions,
   * creating fake races as needed to have an integral number of sets.
   */
  private List<Race> confuseRaces(Race [][] map, int setSize) {
    if (map.length == 0) {
      return new ArrayList<Race>();
    }

    int numSets = (int)Math.ceil((double)map[0].length / (double)setSize);
    List<Race> ordered = new ArrayList<Race>(map.length * numSets * setSize);

    for (int setIndex = 0; setIndex < numSets; setIndex++) {
      for (Race [] div : map) {
	for (int i = 0; i < setSize; i++) {
	  int globalIndex = setIndex * setSize + i;
	  if (globalIndex < div.length) {
	    ordered.add(div[globalIndex]);
	  }
	  else {
	    ordered.add(null);
	  }
	}
      }
    }

    return ordered;
  }

  public static void main (String [] args) {

    // Rotation
    ICSARotationBuilder rotManager = new ICSARotationBuilder(4);
    Rotation rot = new Rotation();
    try {
      rotManager.fillRotation(rot,
			      RotationType.STANDARD,
			      RotationStyle.FRANNY,
			      new Race [][] {{new Race(Division.A, 1),
					      new Race(Division.A, 2),
					      new Race(Division.A, 3),
					      new Race(Division.A, 4),
					      new Race(Division.A, 5)},
					     {new Race(Division.B, 1),
					      new Race(Division.B, 2),
					      new Race(Division.B, 3),
					      new Race(Division.B, 4),
					      new Race(Division.B, 5)},
					     /*
					       {new Race(Division.C, 1),
					       new Race(Division.C, 2),
					       new Race(Division.C, 3),
					       new Race(Division.C, 4),
					       new Race(Division.C, 5)}*/},
			      new Team [] {new Team("Alb"),
					   new Team("Car"),
					   new Team("Foe"),
					   new Team("Hal"),
					   new Team("Joe"),
					   new Team("Lam"),
					   new Team("Oki"),
					   new Team("Pop"),
					   new Team("Sal"),
					   new Team("Ted")},
			      new Sail [] {new Sail(1),
					   new Sail(2),
					   new Sail(3),
					   new Sail(4),
					   new Sail(5),
					   new Sail(6),
					   new Sail(7),
					   new Sail(8),
					   new Sail(9),
					   new Sail(10)},
			      2); // set size
      rot.dump();
    } catch (RotationBuilderException e) {
      System.err.println("Unable to create rotation: " + e.getMessage());
    }

  }
}
