package edu.mit.techscore.tscore;

import javax.swing.JList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListModel;

/**
 * A modified JList that guarantees that at least one element is
 * always chose. The elements in this list are final.
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
 * Created: Thu Aug 27 15:09:17 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public final class JNoneUnselectedList
  extends JList implements ListSelectionListener {

  private int [] lastSelected;

  /**
   * Creates a new <code>JNoneUnselectedList</code> instance, and
   * selects the first instance, by default.
   *
   */
  public JNoneUnselectedList(Object [] values) {
    super(values);
    this.lastSelected = new int [0];
    this.setSelectedIndex(0);
    this.addListSelectionListener(this);
  }

  // Implementation of javax.swing.event.ListSelectionListener

  /**
   * Describe <code>valueChanged</code> method here.
   *
   * @param listSelectionEvent a <code>ListSelectionEvent</code> value
   */
  public final void valueChanged(final ListSelectionEvent e) {
    // Find a selected value, and update the list of lastSelected
    int [] nextSelected = this.getSelectedIndices();
    if (nextSelected.length == 0) {
      this.setSelectedIndices(this.lastSelected);
    }
    else {
      this.lastSelected = nextSelected;
    }

  }

}
