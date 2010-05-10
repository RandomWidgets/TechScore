package tscore;

import javax.swing.JSpinner;
import javax.swing.JComponent;
import regatta.Sail;
import javax.swing.JTextField;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JFormattedTextField;

/**
 * Spinners optimized for displaying Sails and possibly scrolling
 * through them, if they are numerical, that is
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
 * Created: Sat May  8 08:58:13 2010
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class SailSpinner extends TSpinner {

  private JComponent editor;

  /**
   * Creates a new <code>SailSpinner</code> instance using the given
   * sail as seed
   *
   */
  public SailSpinner(Sail sail) {
    super(new SailSpinnerModel(sail));
    
    // Fix editor
    JComponent editor = new SailSpinnerEditor(this);
    this.setEditor(editor);
  }


  /**
   * An editor for sails: a specialized JFormattedTextField which is
   * editable (unlike JSpinner's default editor).
   *
   * @author Dayan Paez
   * @version 2010-05-08
   */
  class SailSpinnerEditor extends JSpinner.DefaultEditor {
    /**
     * Creates a new spinner for the given spinner
     */
    public SailSpinnerEditor(JSpinner spinner) {
      super(spinner);
      formatTextField();
    }
    
    /**
     * Formats the JFormattedTextField to behave correctly
     *
     */
    private void formatTextField() {
      JFormattedTextField field = this.getTextField();
      // field.
    }
  }
  
}
