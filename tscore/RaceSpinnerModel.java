package tscore;

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
   * Creates a new <code>RaceSpinnerModel</code> instance.
   *
   */
  public RaceSpinnerModel(Race [] races) {
    super();
    this.raceList = Arrays.asList(races);
    this.setList(raceList);
  }

  @Override public void setList(List<?> list) {
    // Order list by divisions, then number and ascertain no repeats
    TreeSet<Race> orderedList = new TreeSet<Race>(new RaceComparator());
    for (Object r: list) {
      orderedList.add((Race)r);
    }

    List<String> stringList = new ArrayList<String>(list.size());
    for (Race r: orderedList) {
      stringList.add(r.toString());
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
    Race race = this.stringToRace(value);

    int index = this.raceList.indexOf(race);
    return this.raceList.get(index);
  }

  private Race stringToRace(String text) {
    int length = text.length();
    int raceNum = Integer.parseInt(text.substring(0, length - 1));
    Division raceDiv = Factory.parseDivision(text.charAt(length - 1));
    
    return new Race(raceDiv, raceNum);
  }

  public static class RaceComparator implements Comparator<Race> {
    
    public int compare(Race r1, Race r2) {
      int rel = r1.getNumber() - r2.getNumber();
      if (rel != 0) {
	return rel;
      }
      return r1.getDivision().ordinal() - r2.getDivision().ordinal();
    }
  }
  
}
