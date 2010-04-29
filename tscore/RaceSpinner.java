package tscore;

import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;

import regatta.Race;
import regatta.Regatta;
import regatta.Regatta.Division;
import java.util.Arrays;

/**
 * Describe class RaceSpinnerModel here.
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
 * Created: Tue Jul  7 20:47:00 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RaceSpinner extends JSpinner {

  private SpinnerListModel model;
  
  /**
   * Creates a new <code>RaceSpinnerModel</code> instance.
   *
   */
  public RaceSpinner(Race [] races) {
    super();
    this.model = new SpinnerListModel(races);
    JFormattedTextField editor = new JFormattedTextField(new RaceFormatter());
    editor.setValue(races[0]);
    this.setEditor(editor);
    this.setModel(this.model);
  }

  public void setRaces(Race [] races) {
    this.model.setList(Arrays.asList(races));
  }

  /**
   * Describe class <code>RaceFormatter</code> here.
   *
   */
  class RaceFormatter extends JFormattedTextField.AbstractFormatter {
    public RaceFormatter() {}

    
    public @Override Race stringToValue(String text)
      throws ParseException {

      try {
	int length = text.length();
	int raceNum = Integer.parseInt(text.substring(0, length - 1));
	Division raceDiv = Factory.parseDivision(text.charAt(length - 1));

	return new Race(raceDiv, raceNum);
      } catch (Exception e) {
	throw new ParseException("Unknown race text: " + text, 0);
      }
    }
    public @Override String valueToString(Object value)
      throws ParseException {

      try {
	Race race = (Race)value;
	
	return race.toString();
      } catch (ClassCastException e) {
	throw new ParseException("Object not a race.", 0);
      } catch (Exception e) {
	throw new ParseException("Object not parsable.", 0);
      }
    }
  }
  
  
}
