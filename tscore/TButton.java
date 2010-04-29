package tscore;

import javax.swing.JButton;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Action;

/**
 * A JButton derivative that accepts ENTER key presses as well as the
 * default SPACE.
 *
 *
 * Created: Thu Oct  1 10:07:41 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class TButton extends JButton implements KeyListener {

  /**
   * Creates a new <code>TButton</code> instance.
   *
   */
  public TButton(Action a) {
    super(a);
    this.addKeyListener(this);
  }

  // Implementation of java.awt.event.KeyListener

  /**
   * Describe <code>keyPressed</code> method here.
   *
   * @param keyEvent a <code>KeyEvent</code> value
   */
  public final void keyPressed(final KeyEvent keyEvent) {
    // Perform click if KEY == ENTER
    if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
      this.doClick();
    }
  }

  public final void keyReleased(final KeyEvent keyEvent) {}
  public final void keyTyped(final KeyEvent keyEvent) {}
}
