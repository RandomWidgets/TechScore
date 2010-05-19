package edu.mit.techscore.tscore;

import javax.swing.JSpinner;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.SpinnerModel;
import javax.swing.JComponent;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.SwingUtilities;

/**
 * A JSpinner that automatically selects its content whenever focus
 * enters the spinner, for easy value overriding.
 *
 *
 * Created: Thu Oct  1 10:16:23 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class TSpinner extends JSpinner
  implements FocusListener {

  /**
   * Creates a new <code>TSpinner</code> instance.
   *
   * @param m a <code>SpinnerModel</code> value
   */
  public TSpinner(SpinnerModel m) {
    super(m);
    registerFocusListener();
  }
  public TSpinner() {
    super();
    registerFocusListener();
  }

  private void registerFocusListener() {
    JComponent editor = this.getEditor();
    if (editor instanceof DefaultEditor) {
      JTextField tf = ((DefaultEditor)editor).getTextField();
      tf.addFocusListener(this);
    }
  }

  @Override
  public void setModel(SpinnerModel m) {
    super.setModel(m);
    registerFocusListener();
  }

  // Implementation of java.awt.event.FocusListener

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
  public final void focusLost(final FocusEvent focusEvent) {}
}
