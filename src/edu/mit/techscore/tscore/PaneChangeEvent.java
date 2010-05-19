package edu.mit.techscore.tscore;

import regatta.RegattaEvent.RegattaEventType;

/**
 * Encapsulates a pane change event.
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
 * Created: Mon Jun 22 20:40:48 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class PaneChangeEvent {

  /**
   * The source of the event.
   */
  private AbstractPane source;

  /**
   * What the change event is in regard to.
   */
  private PaneChangeEvent.About about;

  /**
   * What the state of the pane is
   */
  private boolean value;

  public static enum About { VISIBILITY, AVAILABILITY };

  /**
   * Creates a new <code>PaneChangeEvent</code> instance.
   *
   */
  public PaneChangeEvent(AbstractPane source,
			 PaneChangeEvent.About about,
			 boolean value) {
    this.source = source;
    this.about  = about;
    this.value  = value;
  }

  /**
   * Get the <code>Source</code> value.
   *
   * @return an <code>AbstractPane</code> value
   */
  public final AbstractPane getSource() {
    return source;
  }

  /**
   * Get the <code>About</code> value.
   *
   * @return a <code>PaneChangeEvent.About</code> value
   */
  public final PaneChangeEvent.About getAbout() {
    return about;
  }

  /**
   * Get the <code>Value</code> value.
   *
   * @return a <code>boolean</code> value
   */
  public final boolean getValue() {
    return value;
  }
}
