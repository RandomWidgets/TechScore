package edu.mit.techscore.tscore;

import java.util.ArrayList;
import java.util.Arrays;

import edu.mit.techscore.regatta.Race;
import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.Division;
import edu.mit.techscore.regatta.Rotation;
import edu.mit.techscore.regatta.Rotation.RotationStyle;
import edu.mit.techscore.regatta.Rotation.RotationType;
import edu.mit.techscore.regatta.Sail;

/**
 * Creates and manages an offset (FRANNY-style) rotation for a given
 * regatta, remembering the options and providing a method to
 * <code>update</code> the rotation created. This class uses the
 * facilities provided by <code>RotationFactory</code>.
 *
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
 * Created: Wed Jul  1 15:47:39 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class OffsetRotationFactory extends RotationFactory {

  // Offset amount
  private int offset, numRaces;
  private Division [] divisions;
  /**
   * Creates a new <code>OffsetRotationFactory</code> instance.
   *
   * @param reg a <code>Regatta</code> the regatta
   * @param type a <code>RotationType</code>
   * @param sails an <code>int</code> of starting sail values
   * @param set an <code>int</code>: number of races in set
   * @param offset an <code>int</code>: amount to offset from one
   * division to the next
   */
  public OffsetRotationFactory(Regatta reg,
			       RotationType type,
			       Sail [] sails,
			       int set,
			       int offset) {
    super(reg, type, RotationStyle.FRANNY, sails, set);
  }

  protected Rotation createRotation() {
    Rotation rot = new Rotation();

    for (int d = 0; d < divisions.length; d++) {
      
    }

    

    return rot;
  }

  /**
   * Overrides parent method. Sets up orderedRaces to contain the
   * races collated
   */
  protected void prep() {
    divisions = regatta.getDivisions();
    numRaces = regatta.getNumRaces();
    int numDivs  = divisions.length;

    this.orderedRaces = new ArrayList<Race>(numRaces * numDivs);

    for (int d = 0; d < divisions.length; d++) {
      this.orderedRaces.addAll(Arrays.asList(regatta.getRaces(divisions[d])));
    }
    actualSetSize = setSize;
  }

}
