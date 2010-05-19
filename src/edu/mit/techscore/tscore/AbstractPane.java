package tscore;

import javax.swing.Action;
import javax.swing.JPanel;
import java.util.Vector;
import regatta.RegattaListener;
import regatta.Regatta;
import regatta.RegattaEvent.RegattaEventType;
import regatta.RegattaEvent;
import javax.swing.Icon;
import tscore.PaneChangeEvent.About;


/**
 * Parent class of all editing panes in TechScore. It extends the
 * <code>JPanel</code> class and provides methods for setting the
 * panel's regatta.
 * <p>
 *
 * Each object is created with a title and a regatta, and AbstractPane
 * adds itself as a RegattaListener. Children of AbstractPane must
 * take care to implement the regattaChange method.
 * <p>
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
 * Created: Sun Jun 21 19:39:18 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 * @since 1.4   Provide an implied method when closing the pane
 */
public abstract class AbstractPane
  extends JPanel
  implements RegattaListener {

  private boolean isEnabled;
  private Icon icon = null;

  /**
   * The name that identifies this pane
   */
  private String name;
  Vector<PaneChangeListener> listeners;


  /**
   * The regatta for this pane's details
   */
  protected Regatta regatta;

  /**
   * Creates a new <code>AbstractPane</code> instance.
   * @param title the title of both the action and the pane
   * @param parent the panel to add itself to when the action is
   * activated
   */
  public AbstractPane(String title) {
    this.name = title;
    this.regatta = null;
    this.listeners = new Vector<PaneChangeListener>(1);
  }

  /**
   * Fills the components, to be declared by children.
   */
  public void fill() {};

  /**
   * Frees the resources held by this pane and commits any pending
   * actions. Return value indicates whether the pane should be
   * removed or not.
   * <p>
   * Children of this class are strongly recommended to override this
   * method to complete whatever actions remain. This method ALWAYS
   * returns true.
   *
   * @return true if the pane is ready to be removed, false otherwise
   */
  public boolean empty() {
    return true;
  }

  /**
   * Returns whether or not this Pane/Action is enabled
   *
   * @return a <code>boolean</code> value
   */
  public final boolean isEnabled() {
    return this.isEnabled;
  }

  /**
   * Describe <code>setEnabled</code> method here.
   *
   * @param flag a <code>boolean</code> value
   */
  public final void setEnabled(final boolean flag) {
    this.isEnabled = flag;
    this.firePaneChangeEvent(PaneChangeEvent.About.AVAILABILITY,
			     this.isEnabled);
  }

  public String toString() {
    return this.name;
  }

  /**
   * Get the <code>Regatta</code> value.
   *
   * @return a <code>Regatta</code> value
   */
  public final Regatta getRegatta() {
    return regatta;
  }

  /**
   * Set the <code>Regatta</code> value.
   *
   * @param newRegatta The new Regatta value.
   */
  public final void setRegatta(final Regatta newRegatta) {
    this.regatta = newRegatta;
    if (this.regatta == null) {
      this.setEnabled(false);
    }
    else {
      this.regatta.addRegattaListener(this);
      this.setEnabled(this.isUsable());
    }
  }

  /**
   * Get the Icon that represents this pane, for inclusion in Actions,
   * for example.
   *
   * @return an <code>Icon</code> value
   * @return <code>null</code> if there is no Icon.
   */
  public Icon getIcon() {
    return this.icon;
  }

  /**
   * Set the Icon that identifies this pane.
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  /**
   * Get the <code>Name</code> value.
   *
   * @return a <code>String</code> value
   */
  public final String getName() {
    return name;
  }

  /**
   * Set the <code>Name</code> value.
   *
   * @param newName The new Name value.
   */
  public final void setName(final String newName) {
    this.name = newName;
  }
  
  /**
   * Regatta change listener event.
   */
  public void regattaChange(RegattaEvent e){
    this.setEnabled(this.regatta != null &&
		    this.isUsable());
  }

  /**
   * Is this pane allowed?
   *
   * @return a <code>boolean</code> value
   */
  public abstract boolean isUsable();

  /*
   * PaneChangeListeners
   */
  public void addPaneChangeListener(PaneChangeListener l) {
    this.listeners.add(l);
  }
  public void removePaneChangeListener(PaneChangeListener l) {
    this.listeners.remove(l);
  }
  protected void firePaneChangeEvent(PaneChangeEvent.About a,
				     boolean value) {
    PaneChangeEvent e = new PaneChangeEvent(this, a, value);
    for (int i = 0; i < this.listeners.size(); i++) {
      this.listeners.get(i).paneChanged(e);
    }
  }
}
