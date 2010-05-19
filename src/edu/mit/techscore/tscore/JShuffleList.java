package edu.mit.techscore.tscore;

import javax.swing.JList;
import javax.swing.BorderFactory;
import java.awt.event.MouseMotionListener;
import javax.swing.DefaultListModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ListSelectionModel;
import javax.swing.ListModel;

/**
 * A list whose items are selected on hovering with the mouse and
 * whose elements can be shuffled by dragging with the mouse.
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
 * Created: Tue Aug 25 17:17:40 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class JShuffleList extends JList
  implements MouseListener, MouseMotionListener {

  private ListModel model;
  private boolean isShuffleable;

  /**
   * Creates a new <code>JShuffleList</code> instance with the
   * specified list model.
   *
   * @param model a <code>ListModel</code> value
   * @param isShuffleable a <code>boolean</code> value indicating
   * whether the list can be shuffled by using the mouse.
   */
  public JShuffleList(ListModel model, boolean isShuffleable) {
    this.model = model;
    this.setModel(model);
    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.isShuffleable = isShuffleable;
  }

  /**
   * Creates a new <code>JShuffleList</code> instance with a
   * DefaultListModel initialized to the list of items specified.
   *
   * @param items an <code>Object</code> value
   * @param isShuffleable a <code>boolean</code> value indicating
   * whether the list can be shuffled using the mouse.
   */
  public JShuffleList(Object [] items, boolean isShuffleable) {
    this(new DefaultListModel(), isShuffleable);
    DefaultListModel m = (DefaultListModel)this.model;
    for (Object o : items) {
      m.addElement(o);
    }
  }

  /**
   * Creates a new shuffleable <code>JShuffleList</code> instance with
   * the specified objects.
   *
   */
  public JShuffleList(Object [] items) {
    this(items, true);
  }

  /**
   * Creates a new shuffleable <code>JShuffleList</code> instance with
   * the specified objects.
   *
   */
  public JShuffleList(ListModel model) {
    this(model, true);
  }

  // Implementation of mouse listener

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  /**
   * Start dragging
   *
   */
  public void mousePressed(MouseEvent e) {
    int index = this.locationToIndex(e.getPoint());
    if (index >= 0) {
      this.selItem = this.model.getElementAt(index);
    }
  }

  /**
   * Reset dragging
   *
   */
  public void mouseReleased(MouseEvent e) {
    this.selItem = null;
  }

  /**
   * Clears all selections, for aesthetics.
   *
   */
  public void mouseExited(MouseEvent e) {
    this.clearSelection();
  }

  // Implementation of mouse motion listener

  public void mouseMoved(MouseEvent e) {
    JList source = (JList)e.getSource();
    int index = source.locationToIndex(e.getPoint());
    source.setSelectedIndex(index);
  }

  Object selItem = null;
  public void mouseDragged(MouseEvent e) {
    if (!this.isShuffleable) {
      return;
    }

    JList source = (JList)e.getSource();
    DefaultListModel model = (DefaultListModel)source.getModel();
    int index = source.locationToIndex(e.getPoint());

    if (selItem == null) {
      selItem = model.getElementAt(index);
    }
    else {
      // Is currently moving
      int oldIndex = model.indexOf(selItem);
      model.removeElement(selItem);
      model.add(index, selItem);
      source.setSelectedIndex(index);
      source.revalidate();
      source.repaint();
    }
  }
}
