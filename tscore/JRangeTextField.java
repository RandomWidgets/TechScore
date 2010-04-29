package tscore;

import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.InputVerifier;
import regatta.Race;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Describe class JRangeTextField here.
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
 * Created: Tue Aug 25 21:17:48 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class JRangeTextField extends JTextField {

  private final List<Integer> validNums;
  private Integer [] currentNums;

  public JRangeTextField(Integer [] valid, Integer [] nums) {
    this.validNums = Arrays.asList(valid);
    this.setInputVerifier(new RangeVerifier());
    this.updateText(nums);
  }
  public JRangeTextField(Integer [] valid, Race [] nums) {
    this(valid, Factory.raceToInteger(nums));
  }
  public JRangeTextField(Integer [] valid) {
    this(valid, new Integer[0]);
  }

  private class RangeVerifier extends InputVerifier {
    public boolean verify(JComponent input) {
      List<Integer> entries = new ArrayList<Integer>();
      for (Integer i : Factory.parseList(getText())) {
	if (validNums.contains(i)) {
	  entries.add(i);
	}
      }

      updateText(entries.toArray(new Integer[]{}));
      return true;
    }
  }

  public Integer [] getNumbers() {
    return this.currentNums;
  }

  /**
   * Sets this text field's text to represent the values in the array
   * and updates <code>currentNums</code> with these values
   *
   * @param nums an <code>Integer</code> value
   */
  private void updateText(Integer [] nums) {
    this.currentNums = nums;
    this.setText(Factory.formatList(nums));
  }
}
