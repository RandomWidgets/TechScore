package tscore;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import regatta.Finish;
import regatta.Finish.PlaceComparator;
import regatta.Race;
import regatta.Regatta;
import regatta.RegattaEvent;
import regatta.RegattaEvent.RegattaEventType;
import regatta.Rotation;
import regatta.Sail;
import regatta.Team;
import tscore.FinishesPane.Using;
import regatta.Regatta.RegattaScoring;
import tscore.RaceSpinnerModel.RaceComparator;
import regatta.Regatta.Division;

/**
 * Pane for entering finishes. This pane is organized in two main
 * sections. The first, is where the user chooses the race for which
 * to enter finishes, and what to use to enter them (rotations, or
 * direct team names).
 *
 * The second section responds to the first and is composed of two
 * lists side by side. Teams are moved from one list to another by
 * right-clicking on the item on the list or by pressing enter with
 * the item selected. In addition, if the user is entering items using
 * rotations, a textbox can be used to enter the items. The pane takes
 * care of validating the textbox input so that it matches one of the
 * possible sail numbers. This is to make entering values as easy as
 * possible.
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
 * Created: Sat Jun 27 12:52:23 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class FinishesPane extends AbstractPane
  implements ChangeListener, ActionListener {

  public static enum Using {
    ROTATION("Rotations"),
      TEAM("Teams");
    private String name;
    Using(String n) {
      this.name = n;
    }
    public String toString() {
      return this.name;
    }
  }
  public static String NO_TEAM = "";

  private RaceSpinnerModel raceSpinnerModel;
  private JComboBox usingCombo;
  private JPanel listsPanel;
  private JLinkedLists lists;
  private JTextField sailField;
  private AbstractAction enterAction, removeAction;

  /**
   * Creates a new <code>FinishesPane</code> instance.
   *
   */
  public FinishesPane(Regatta reg) {
    super("Finishes");
    this.setIcon(Factory.getImageIcon("img/Finish16.gif", toString()));
    this.setRegatta(reg);
  }

  public void fill() {
    this.removeAll();
    this.setLayout(new GridBagLayout());
    Insets insts = new Insets(2,2,2,2);
    GridBagConstraints p1, p2;
    p1 = new GridBagConstraints();
    p2 = new GridBagConstraints();
    p1.gridx = 0; p2.gridx = 1;
    p1.gridy = 0; p2.gridy = 0;
    p1.fill = GridBagConstraints.BOTH;
    p1.insets = insts;
    p2.insets = insts;
    p1.weightx = 1.0;

    //- Parameters
    p1.gridwidth = 2;
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Parameters"));
    this.add(panel, p1);

    JLabel label, label2;
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c1, c2, c3, c4;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();

    c1.gridx = 0;
    c2.gridx = 1;
    c3.gridx = 2;
    c4.gridx = 3;
    
    c2.weightx = 0.25;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c4.weightx = 0.75;
    c4.fill = GridBagConstraints.HORIZONTAL;

    c2.insets = insts;
    c4.insets = insts;

    //  -Race
    Race chosenRace = this.getNextUnscoredRace();
    label = Factory.label("Race:");
    this.raceSpinnerModel = new RaceSpinnerModel(regatta.getRaces(), regatta.getScoring());
    this.raceSpinnerModel.setRace(chosenRace);
    this.raceSpinnerModel.addChangeListener(this);
    panel.add(label, c1);
    panel.add(new JSpinner(raceSpinnerModel), c2);

    //  -Using
    label = Factory.label("Using:");
    ArrayList<Using> choices =
      new ArrayList<Using>(Arrays.asList(Using.values()));
    Rotation rot = regatta.getRotation();
    if (rot == null ||
	rot.getSails(chosenRace).length == 0) {
      choices.remove(Using.ROTATION);
    }
    this.usingCombo = new JComboBox(choices.toArray(new Using []{}));
    this.usingCombo.addActionListener(this);

    panel.add(label, c3);
    panel.add(usingCombo, c4);


    //- Participating sails/schools panel
    p1.gridy++;
    p2.gridy++;
    p1.weighty = 1.0;
    this.listsPanel = new JPanel();
    this.add(this.listsPanel, p1);

    //- Delete finish button
    p1.gridwidth = 1;
    p1.gridy++;
    p1.anchor  = GridBagConstraints.LAST_LINE_START;
    p1.fill    = GridBagConstraints.NONE;
    p1.weightx = 0.0;
    p1.weighty = 0.0;
    this.removeAction = new RemoveFinishAction();
    JButton button = new JButton(this.removeAction);
    this.add(button, p1);

    //- Enter finish button
    p2.gridy++;
    p2.anchor  = GridBagConstraints.LAST_LINE_END;
    p2.fill    = GridBagConstraints.NONE;
    p2.weighty = 0.0;
    this.enterAction = new EnterFinishAction();
    button = new JButton(this.enterAction);
    this.add(button, p2);

    updateFinishPanel();
  }

  public boolean isUsable() {
    if (regatta.getTeams().length < 2 ||
	regatta.getNumRaces() == 0) {
      return false;
    }
    return true;
  }

  /**
   * Determines whether the race selected in the race spinner has a
   * rotation associated with it in this regatta.
   *
   * @return a <code>boolean</code> value
   */
  private boolean selectedRaceHasRotation() {
    Rotation rot = this.regatta.getRotation();
    if (rot == null) {
      return false;
    }
    List<Race> rotRaces = Arrays.asList(rot.getRaces());
    return rotRaces.contains(this.raceSpinnerModel.getSelectedRace());
  }

  // Implementation of change listener
  public void stateChanged(ChangeEvent evt) {
    Object source = evt.getSource();
    if (source == this.raceSpinnerModel) {
      
      // Update the using combo to reflect the rotation state of this
      // race
      Using curChoice = (Using)this.usingCombo.getSelectedItem();
      DefaultComboBoxModel model =
	(DefaultComboBoxModel)this.usingCombo.getModel();
      boolean hasRotation = selectedRaceHasRotation();

      if (hasRotation && model.getIndexOf(Using.ROTATION) < 0) {
	this.usingCombo.addItem(Using.ROTATION);
      }
      else if (!hasRotation && model.getIndexOf(Using.ROTATION) >= 0) {
	this.usingCombo.removeItem(Using.ROTATION);
	this.usingCombo.setSelectedItem(Using.TEAM);
      }
      this.updateFinishPanel();
    }
  }

  // Implementation of action listener (for combo box)
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == this.usingCombo) {
      this.updateFinishPanel();
    }
  }

  /**
   * Updates the finish panel with the information from the "race" and
   * "using" spinners.
   *
   * When the scoring used is combined, add both the teams and the
   * division (if using Teams). Add sail numbers across all divisions
   * (if using Rotation).
   */
  private void updateFinishPanel() {

    // Get race and entry type
    Race race = this.raceSpinnerModel.getSelectedRace();
    Using use = (Using)this.usingCombo.getSelectedItem();
    Team [] teams = this.regatta.getTeams();
    int length = teams.length;
    if (this.regatta.getScoring() == RegattaScoring.COMBINED)
      length *= this.regatta.getDivisions().length;

    // If the race has already been scored, "postList" will have those
    // selections in the order they were entered. Otherwise, it will
    // be empty. "preList" contains the sails or teams in
    // numerical or alphabetical order.
    Finish [] preTeam, postTeam;
    
    // If race has been scored, rewrite those values, otherwise, list
    // the teams/sails for a new race.
    boolean isScored =
      Arrays.binarySearch(this.regatta.getFinishedRaces(), race) >= 0;
    if (isScored) {
      preTeam = new Finish[0];
      postTeam = new Finish[length];
      Finish[] f = this.regatta.getFinishes(race);
      for (int i = 0; i < f.length; i++)
	postTeam[i] = new FinishListItem(f[i], this.regatta.getScoring());
    }
    else {
      preTeam = new Finish[length];
      postTeam = new Finish[0];
      if (this.regatta.getScoring() == RegattaScoring.STANDARD) {
	for (int i = 0; i < teams.length; i++)
	  preTeam[i] = new FinishListItem(race, teams[i], this.regatta.getScoring());
      }
      else {
	int i = 0;
	for (Division d : this.regatta.getDivisions()) {
	  for (Team t : teams) {
	    preTeam[i] = new FinishListItem(this.regatta.getRace(d, race.getNumber()), t,
					    this.regatta.getScoring());
	    i++;
	  }
	}
      }
    }

    // Create the lists
    JList sailList, finishList;
    this.lists = new JLinkedLists(preTeam, postTeam);
    String fromTitle, toTitle;
    fromTitle = "Teams";
    toTitle   = "Order";
    
    // Translate if rotation in use
    if (use == Using.ROTATION) {
      Rotation rot = this.regatta.getRotation();
      Sail [] preSail, postSail;
      preSail  = new Sail [preTeam.length];
      postSail = new Sail [postTeam.length];
      for (int i = 0; i < preTeam.length; i++) {
	preSail[i] = rot.getSail(race, preTeam[i].getTeam());
      }
      for (int i = 0; i < postTeam.length; i++) {
	postSail[i] = rot.getSail(race, postTeam[i].getTeam());
      }
      this.lists = new JLinkedLists(preSail, postSail);

      fromTitle = "Sails";
    }
    sailList   = this.lists.getFromList();
    finishList = this.lists.getToList();

    this.listsPanel.removeAll();
    this.listsPanel.setLayout(new GridBagLayout());
    GridBagConstraints p1 = new GridBagConstraints();
    p1.insets = new Insets(2,2,2,2);
    p1.gridx = 0;
    p1.gridy = 0;
    p1.weightx = 1.0;
    p1.weighty = 0.0;
    p1.fill = GridBagConstraints.HORIZONTAL;
    p1.anchor = GridBagConstraints.LINE_START;

    // Add text field if sails in use
    JPanel panel;
    if (use == Using.ROTATION) {
      panel = new JPanel();
      this.listsPanel.add(panel, p1);
      panel.add(this.sailField = new JTextField(3));
      panel.add(new JButton(new AbstractAction("Add") {
	  public void actionPerformed(ActionEvent e) {
	    promoteSailInField();
	  }
	}));
      // Setup the sailField
      this.sailField.addKeyListener(new KeyAdapter() {
	  public void keyReleased(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	      promoteSailInField();
	    }
	  }
	});
      p1.gridy++;
    }
    else {
      this.sailField = null;
    }

    // Add scrollpanes
    p1.weighty = 1.0;
    p1.fill = GridBagConstraints.BOTH;
    this.listsPanel.add(panel = new JPanel(new GridLayout(0, 2)), p1);

    JScrollPane sailPanel, finishPanel;
    panel.add(sailPanel = new JScrollPane(sailList));
    panel.add(finishPanel = new JScrollPane(finishList));
    sailPanel.setBorder(BorderFactory.createTitledBorder(fromTitle));
    finishPanel.setBorder(BorderFactory.createTitledBorder(toTitle));

    this.listsPanel.revalidate();
    this.listsPanel.repaint();

    // Update the delete finish action
    this.removeAction.setEnabled(isScored);
    this.enterAction.setEnabled(isScored);
  }

  /**
   * Checks for the sail value entered in the <code>sailField</code>
   * and "promotes" it (moves it from the list of sails to the ordered
   * list) if it exists or is valid. If it is not, it leaves the
   * textfield's value selected, so that following key inputs are not
   * interrumpted.
   *
   */
  private void promoteSailInField() {
    String sail = this.sailField.getText().trim();
    if (sail.length() == 0) {
      return;
    }

    ListModel model = this.lists.getFromList().getModel();
    for (int i = 0; i < model.getSize(); i++) {
      Sail aSail = (Sail)model.getElementAt(i);
      if (aSail.equals(sail)) {
	this.lists.promote(i);
	this.sailField.setText("");
	return;
      }
    }
    this.sailField.selectAll();
  }

  private Race getNextUnscoredRace() {
    List<Race> allRaces = new ArrayList<Race>(Arrays.asList(this.regatta.getRaces()));
    Race lastRace = allRaces.get(allRaces.size() - 1);
    allRaces.removeAll(Arrays.asList(this.regatta.getFinishedRaces()));
    if (allRaces.size() == 0) {
      return lastRace;
    }
    // Sort
    Collections.sort(allRaces, new RaceSpinnerModel.RaceComparator());
    return allRaces.get(0);
  }

  /**
   * Action for entering finishes. As of 2010-05-03, objects are
   * actual finishes and must account for combined divisions
   *
   * @author Dayan Paez
   * @version 2010-05-03
   */
  class EnterFinishAction extends AbstractAction {
    public EnterFinishAction() {
      super("Enter finish");
    }
    
    public void actionPerformed(ActionEvent evt) {
      // Race
      Race race = FinishesPane.this.raceSpinnerModel.getSelectedRace();

      int size = regatta.getTeams().length;
      if (FinishesPane.this.regatta.getScoring() == RegattaScoring.COMBINED)
	size *= regatta.getDivisions().length;
      boolean usingRot = (usingCombo.getSelectedItem() == Using.ROTATION);
      Rotation rot = null;
      if (usingRot) {
	rot = FinishesPane.this.regatta.getRotation();
      }
      
      ArrayList<Finish> teams = new ArrayList<Finish>(size);
      DefaultListModel fModel =
	(DefaultListModel)FinishesPane.this.lists.getToList().getModel();
      for (Enumeration e = fModel.elements(); e.hasMoreElements();) {
	Object elem = e.nextElement();
	if (elem instanceof Sail) {
	  teams.add(new Finish(race, rot.getTeam(race, (Sail)elem)));
	}
	else {
	  // Finish
	  teams.add((Finish)elem);
	}
      }

      // Create timestamp
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      for (int i = 0; i < teams.size(); i++) {
	calendar.add(Calendar.SECOND, 2);
	Date timestamp = calendar.getTime();
	Finish list = teams.get(i);
	Finish copy = new Finish(list.getRace(), list.getTeam(), timestamp);
	FinishesPane.this.regatta.setFinish(copy);
      }

      regatta.fireRegattaChange(new RegattaEvent(regatta,
						 RegattaEventType.FINISH,
						 this));
      // turn to the next race
      raceSpinnerModel.setRace(FinishesPane.this.getNextUnscoredRace());
    }
  }

  /**
   * Action for removing the selected finishes from the race. This
   * action removes the finish from the regatta if there was one.
   *
   */
  class RemoveFinishAction extends AbstractAction {
    public RemoveFinishAction() {
      super("Remove finish");
      putValue(SHORT_DESCRIPTION,
	       "Delete finishes for this race and start over");
    }
    
    public void actionPerformed(ActionEvent evt) {
      // Remove finish from regatta
      regatta.removeFinishes(raceSpinnerModel.getSelectedRace());
      regatta.fireRegattaChange(new RegattaEvent(regatta,
						 RegattaEventType.FINISH,
						 this));
      updateFinishPanel();
    }
  }
  

  /**
   * A set of mutually exclusive JComboBox(es). Each time an item is
   * selected from one of the combo boxes it is removed from the
   * choices of the other combo boxes. There is one choice, "default",
   * that is allowed to appear in multiple combo boxes are a time.
   *
   * In addition, if the user presses ENTER while on a box, it
   * advances focus to the next empty box (one with a default value
   * chosen), or the last box if none are empty.
   *
   * This class takes care of synchronizing the boxes. To create a
   * combo box in this group, first instantiate this class, and then
   * create the new combo box model using <code>group.new
   * ComboBox</code> to create an instance of the inner class.
   *
   */
  class ComboBoxGroup
    extends KeyAdapter
    implements ActionListener {
    
    private final Object defaultObject;
    private final Object [] choices;
    private ArrayList<Object> items;
    private ArrayList<ComboBoxGroup.ComboBox> boxes;

    public ComboBoxGroup(Object def, Object [] choices) {
      this.defaultObject = def;
      this.choices       = choices;
      this.items = new ArrayList<Object>(choices.length + 1);
      this.items.add(this.defaultObject);
      for (Object t: this.choices) {
	this.items.add(t);
      }

      boxes = new ArrayList<ComboBoxGroup.ComboBox>();
    }

    private void addBox(ComboBoxGroup.ComboBox box) {
      boxes.add(box);
      box.addActionListener(this);
      box.addKeyListener(this);
    }

    // Implementation of KeyAdapter
    public void keyReleased(KeyEvent e) {
      // Get key
      int keyCode = e.getKeyCode();
      if (keyCode == KeyEvent.VK_ENTER) {
	ComboBox source = (ComboBox)e.getSource();
	int index = this.boxes.indexOf(source);
	for (int i = index + 1; i < this.boxes.size(); i++) {
	  ComboBox box = this.boxes.get(i);
	  if (box.getSelectedItem() == this.defaultObject) {
	    box.requestFocus();
	    return;
	  }
	}
	// Loop back around
	for (int i = 0; i <= index; i++) {
	  ComboBox box = this.boxes.get(i);
	  if (box.getSelectedItem() == this.defaultObject) {
	    box.requestFocus();
	    return;
	  }
	}
	// Just pick the last one
	this.boxes.get(this.boxes.size() - 1).requestFocus();
      }

    }

    // Implementation of ActionListener
    public void actionPerformed(ActionEvent evt) {
      ComboBox source = (ComboBox)evt.getSource();
      Object item = source.getSelectedItem();
      // remove the item from the other boxes, if it's not the default
      // choice
      if (item != this.defaultObject) {
	// Remove this item from other's list
	for (ComboBox box: this.boxes) {
	  if (source != box) {
	    box.removeItem(item);
	  }
	}
      }
      // Return the source's previous item to the list, unless it's
      // the default item which was never removed to begin with
      if (source.previousItem != this.defaultObject) {
	// Return the source's previous item to the other boxes list
	for (ComboBox box: this.boxes) {
	  if (source != box) {
	    box.addItem(source.previousItem);
	  }
	}
      }
      // Update previous item
      source.previousItem = item;
    }
    
    class ComboBox extends JComboBox {
      private Object previousItem;
      public ComboBox() {
	super(ComboBoxGroup.this.items.toArray());
	this.setSelectedItem(ComboBoxGroup.this.defaultObject);
	this.previousItem = ComboBoxGroup.this.defaultObject;
	ComboBoxGroup.this.addBox(this);
      }
    }
  }

  /**
   * A set of two JLists with items shared between them. Items in the
   * first list can be promoted to the second list and those on the
   * second can be demoted to the first. In addition, the second
   * list's items can be shuffled.
   *
   */
  private class JLinkedLists {
    
    final JList fromList, toList;

    private JLinkedLists(Object [] fromItems,
			Object [] toItems) {
      DefaultListModel model = new DefaultListModel();

      Arrays.sort(fromItems);
      for (Object i: fromItems) {
	model.addElement(i);
      }
      fromList = new JShuffleList(model, false);
      setupList(fromList);

      model = new DefaultListModel();
      for (Object i: toItems) {
	model.addElement(i);
      }
      toList   = new JShuffleList(model);
      setupList(toList);
    }

    private void setupList(JList list) {
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      DefaultListCellRenderer lr = new DefaultListCellRenderer();
      lr.setHorizontalAlignment(JLabel.CENTER);
      lr.setPreferredSize(new Dimension(50, 20));
      list.setCellRenderer(lr);
      list.addMouseListener(new MouseAdapter() {
	  public void mouseClicked(MouseEvent evt) {
	    if (evt.getButton() == MouseEvent.BUTTON1) {
	      JList source = (JList)evt.getSource();
	      int index = source.locationToIndex(evt.getPoint());
	    
	      if (source == JLinkedLists.this.fromList &&
		  index >= 0) {
		JLinkedLists.this.promote(index);
		if (JLinkedLists.this.fromList.getModel().getSize() > 0) {
		  JLinkedLists.this.fromList.setSelectedIndex(index);
		}
	      }
	      else if (source == JLinkedLists.this.toList &&
		       index >= 0) {
		JLinkedLists.this.demote(index);
		if (JLinkedLists.this.toList.getModel().getSize() > 0) {
		  JLinkedLists.this.toList.setSelectedIndex(index);
		}
	      }
	    }
	  }
	});
      list.addKeyListener(new KeyAdapter() {
	  public void keyPressed(KeyEvent e) {
	    JList src = (JList)e.getSource();
	    int index = src.getSelectedIndex();
	    int kc = e.getKeyCode();
	    if (index >= 0 && (kc == KeyEvent.VK_ENTER ||
			       kc == KeyEvent.VK_SPACE)) {
	      if (src == JLinkedLists.this.fromList) {
		JLinkedLists.this.promote(index);
	      }
	      else if (src == JLinkedLists.this.toList) {
		JLinkedLists.this.demote(index);
	      }

	    }

	  }
	});
    }
    
    private JList getFromList() {
      return this.fromList;
    }
    private JList getToList() {
      return this.toList;
    }
    private void promote(int index) {
      DefaultListModel model;
      model = (DefaultListModel)fromList.getModel();
      Object o = model.remove(index);

      model = (DefaultListModel)toList.getModel();
      model.addElement(o);

      // Enable the enter finish action if all the teams have been
      // added to the tolist (check just the number of items)
      int length = FinishesPane.this.regatta.getTeams().length;
      if (FinishesPane.this.regatta.getScoring() == RegattaScoring.COMBINED)
	length *= FinishesPane.this.regatta.getDivisions().length;
      FinishesPane.this.enterAction.setEnabled(length == model.getSize());
    }
    private void demote(int index) {
      DefaultListModel model;
      model = (DefaultListModel)toList.getModel();
      Object o = model.remove(index);

      model = (DefaultListModel)fromList.getModel();
      model.addElement(o);
      
      // Order the elements
      Object [] fromItems = new Object[model.getSize()];
      model.copyInto(fromItems);
      Arrays.sort(fromItems);
      model = new DefaultListModel();
      for (Object elem : fromItems) {
	model.addElement(elem);
      }
      fromList.setModel(model);

      // If an object has been demoted, there is clearly not a
      // complete set of finishes, so disable the enterfinish button
      FinishesPane.this.enterAction.setEnabled(false);
    }
  }
}
