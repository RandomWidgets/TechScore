package tscore;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.text.Keymap;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import javax.swing.SwingUtilities;

/**
 * A subclass of JTextField which reverts its text when the user
 * presses ESCAPE, and highlights its content when focus first arrives
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
public class EditField extends JTextField
  implements FocusListener {
  private String previous;

  /**
   * Creates a new <code>EditField</code> instance.
   *
   */
  public EditField(String value, int cols) {
    super(value, cols);
    
    Keymap km = this.getKeymap();
    km.addActionForKeyStroke(KeyStroke.getKeyStroke("ESCAPE"),
			     new AbstractAction() {
			       public void actionPerformed(ActionEvent ev) {
				 EditField.this.rollBack();
			       }
			     });
    km.addActionForKeyStroke(KeyStroke.getKeyStroke("ENTER"),
			     new AbstractAction() {
			       public void actionPerformed(ActionEvent evt) {
				 EditField.this.commitEdit();
			       }
			     });
    this.previous = value;
    this.addFocusListener(this);
  }

  /**
   * Overrides the content of this textfield, and notifies action
   * listeners
   *
   * @param t a <code>String</code> value
   */
  @Override
  public void setText(String text) {
    text = (text != null) ? text.trim() : null;

    if (previous == null ||
	text     == null ||
	!previous.equals(text)) {
      previous = text;
      super.setText(text);
      this.fireActionPerformed();
    }
  }

  private void commitEdit() {
    this.setText(this.getText());
  }
  private void rollBack() {
    this.setText(this.previous);
  }

  public String previous() {
    return this.previous;
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
    this.commitEdit();
  }
}
