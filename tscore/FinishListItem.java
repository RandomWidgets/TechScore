package tscore;

import regatta.Finish;
import regatta.Regatta.RegattaScoring;
import regatta.Race;
import regatta.Team;

/**
 * Extends regatta.Finish for displaying in lists such as
 * JLinkedLists. Takes care of displaying correctly depending on
 * Regatta scoring
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
 *
 * Created: Sun May  2 18:40:53 2010
 *
 * @author <a href="mailto:dayan@marti">dpv</a>
 * @version 1.0
 */
public class FinishListItem extends Finish {

  private RegattaScoring mode;

  /**
   * Creates a new <code>FinishListItem</code> instance.
   *
   */
  public FinishListItem(Finish fin, RegattaScoring mode) {
    this(fin.getRace(), fin.getTeam(), mode);
  }

  /**
   * Creates a new item for the given race and team
   *
   * @param race the race in question
   * @param team the team in question
   */
  public FinishListItem(Race race, Team team, RegattaScoring mode) {
    super(race, team);
    this.mode = mode;
  }

  /**
   * Displays the name of the team if the scoring is standard, or
   * preceded with the division number if combined scoring
   *
   * @return the String representation
   */
  public String toString() {
    if (this.mode == RegattaScoring.COMBINED) {
      return String.format("%s: %s",
			   this.getRace().getDivision(),
			   this.getTeam().toString());
    }
    return this.getTeam().toString();
  }
}
