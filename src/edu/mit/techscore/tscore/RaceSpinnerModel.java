package edu.mit.techscore.tscore;

import javax.swing.SpinnerListModel;
import regatta.Race;
import regatta.Regatta.Division;
import regatta.Regatta;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Comparator;
import regatta.Regatta.RegattaScoring;
import tscore.RaceSpinnerModel.RaceComparator;

/**
 * Manages a set of races to be used in JSpinner's.
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
 * Created: Tue Jul  7 20:47:00 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RaceSpinnerModel extends SpinnerListModel {

  private List<Race> raceList;

  /**
   * In combined scoring, display only the race NUMBER.
   */
  private RegattaScoring mode;
  
  /**
   * Creates a new <code>RaceSpinnerModel</code> instance with
   * Standard scoring mode as default
   *
   * @param races the races to list
   * @deprecated use the full constructor to specify regatta scoring
   */
  public RaceSpinnerModel(Race [] races) {
    this(races, RegattaScoring.STANDARD);
  }

  /**
   * Creates a new race spinner for the given type of regatta scoring
   * method. If using combined division scoring, then merely display
   * the number, but always return the race in the first division
   *
   * @param races the races to display in the spinner
   * @param mode the mode to use
   */
  public RaceSpinnerModel(Race [] races, RegattaScoring mode) {
    super();
    this.mode = mode;
    this.raceList = Arrays.asList(races);
    this.setList(raceList);
  }

  /**
   * Sets the race list. This method should not need to be called once
   * the object is created. If it is, however, the list should contain
   * races.
   *
   * @param list the list of <code>Race</code>s.
   */
  @Override public void setList(List<?> list) {

    // Choose the comparator to use depending on mode
    Comparator<Race> comp;
    if (this.mode == RegattaScoring.STANDARD)
      comp = new RaceComparator();
    else
      comp = new CombinedRaceComparator();
    
    // Order list by divisions, then number and ascertain no repeats
    TreeSet<Race> orderedList = new TreeSet<Race>(comp);
    for (Object r: list) {
      orderedList.add((Race)r);
    }

    List<String> stringList = new ArrayList<String>(list.size());
    for (Race r: orderedList) {
      // Add the full race representation, or just the number
      String s;
      if (this.mode == RegattaScoring.STANDARD)
	s = r.toString();
      else
	s = String.valueOf(r.getNumber());
      stringList.add(s);
    }

    super.setList(stringList);
  }

  /**
   * Returns the chosen race. Unlike <code>getValue</code> which is
   * defined in SpinnerModel, this method returns the
   * <code>Race</code> object.
   *
   * @return a <code>Race</code> value
   */
  public Race getSelectedRace() {
    String value = (String)this.getValue();

    // Create race based on mode
    Race race;
    if (this.mode == RegattaScoring.STANDARD)
      race = this.stringToRace(value);
    else
      race = new Race(Division.A, Integer.parseInt(value));

    int index = this.raceList.indexOf(race);
    return this.raceList.get(index);
  }

  private Race stringToRace(String text) {
    int length = text.length();
    int raceNum = Integer.parseInt(text.substring(0, length - 1));
    Division raceDiv = Factory.parseDivision(text.charAt(length - 1));
    
    return new Race(raceDiv, raceNum);
  }

  /**
   * Requires that the value to be set be a race
   *
   */
  public void setRace(Race r) {
    if (this.mode == RegattaScoring.STANDARD)
      super.setValue(r.toString());
    else
      super.setValue(String.valueOf(r.getNumber()));
  }

  /**
   * Compares races based on divisions AND numbers
   *
   * @author Dayan Paez
   * @version 1.0
   */
  public static class RaceComparator implements Comparator<Race> {
    public int compare(Race r1, Race r2) {
      int rel = r1.getNumber() - r2.getNumber();
      if (rel != 0) {
	return rel;
      }
      return r1.getDivision().ordinal() - r2.getDivision().ordinal();
    }
  }
  
  /**
   * Compares races based solely on their number. Made static for
   * speed considerations, I think.
   *
   * Created: Sat May  1 10:03:00 2010
   * @author Dayan Paez
   * @version 1.4
   */
  public static class CombinedRaceComparator implements Comparator<Race> {
    public int compare (Race r1, Race r2) {
      return r1.getNumber() - r2.getNumber();
    }
  }
}
