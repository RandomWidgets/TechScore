package tscore;

import regatta.Finish;
import regatta.Race;
import regatta.Team;

/**
 * Thin wrapper around <code>regatta.Finish</code>. Controls the
 * display using <code>toString</code> method.
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
public class FinishListItem implements Comparable<FinishListItem> {

  private Finish finish;
  private String display;

  /**
   * Creates a new <code>FinishListItem</code> instance.
   *
   * @param finish the finish to represent
   * @param display the text to display
   */
  public FinishListItem(Finish finish, String display) {
    this.finish = finish;
    this.display = display;
  }

  /**
   * Creates a new item for the given race and team
   *
   * @param race the race in question
   * @param team the team in question
   * @param display the text to display
   */
  public FinishListItem(Race race, Team team, String display) {
    this(new Finish(race, team), display);
  }

  /**
   * Displays the name of the team if the scoring is standard, or
   * preceded with the division number if combined scoring
   *
   * @return the String representation
   */
  public String toString() {
    return this.display;
  }

  /**
   * Fetches the finish this item represents
   *
   * @return the finish
   */
  public Finish getFinish() {
    return this.finish;
  }

  /**
   * Sets the text to display
   *
   * @param display the new text to display
   */
  public void setDisplay(String display) {
    this.display = display;
  }

  /**
   * Delegates comparison to finish object
   *
   */
  public int compareTo(FinishListItem other) {
    return this.getFinish().compareTo(other.getFinish());
  }
}
