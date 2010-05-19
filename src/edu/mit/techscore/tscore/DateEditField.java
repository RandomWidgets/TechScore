package edu.mit.techscore.tscore;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.text.Keymap;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import javax.swing.SwingUtilities;
import javax.swing.JFormattedTextField;
import java.text.Format;
import javax.swing.JFormattedTextField.AbstractFormatter;
import java.text.ParseException;

/**
 * A subclass of JFormattedTextField which reverts its text when the
 * user presses ESCAPE, and highlights its content when focus first
 * arrives. In particular, it is used to change dates
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
 * Created: Sun Jun 14 19:35:19 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class DateEditField extends JFormattedTextField 
  implements FocusListener {

  /**
   * Creates a new <code>DateEditField</code> instance.
   *
   */
  public DateEditField(JFormattedTextField.AbstractFormatter format) {
    super(format);
    this.addFocusListener(this);
  }

  // Implementation of focus listener
  /**
   * Selects the content whenever focus comes to this spinner
   *
   * @param focusEvent a <code>FocusEvent</code> value
   */
  public final void focusGained(final FocusEvent focusEvent) {
    
    final JTextField tf = (JTextField)focusEvent.getSource();
    SwingUtilities.invokeLater(new Runnable(){
	public void run() {
	  tf.selectAll();
	}
      });
  }
  public final void focusLost(final FocusEvent focusEvent) {
    this.select(0, 0);
  }
}
