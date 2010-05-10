package tscore;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import regatta.Race;
import regatta.Regatta;
import regatta.Regatta.Division;
import regatta.RegattaEvent;
import regatta.Rotation;
import regatta.Rotation.RotationStyle;
import regatta.Rotation.RotationType;
import regatta.Sail;
import regatta.Team;

/**
 * The pane where rotations get created/edited.
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
 * Created: Mon Jun 15 22:07:14 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RotationsPane extends AbstractPane
  implements ActionListener, ChangeListener {

  // Private variables
  private JPanel paramPanel;
  private JComboBox typeField, styleField;
  private JSpinner setSizeField;
  private JLabel offsetLabel;
  private JComponent offsetField;
  private JTextField raceField;
  private JList divisionField;
  private ArrayList<SailSpinner> sails;

  private CreateRotationAction cAction;
  private DeleteRotationAction dAction;
  private RotationFactory rotFactory = null;

  /**
   * Creates a new <code>RotationsPane</code> instance.
   *
   */
  public RotationsPane(Regatta reg) {
    super("Rotations");
    this.setIcon(Factory.getImageIcon("img/Rotation16.gif", toString()));
    this.setRegatta(reg);
  }

  /**
   * Adds the components to this pane
   */
  public void fill() {
    this.removeAll();
    this.setLayout(new GridBagLayout());

    GridBagConstraints p1, p2;
    p1 = new GridBagConstraints();
    p2 = new GridBagConstraints();
    p1.gridx = 0;
    p1.gridy = 0;
    p1.gridwidth = 2;
    p1.weightx = 1.0;
    p1.weighty = 0.0;
    p1.fill = GridBagConstraints.BOTH;
    p1.insets = new Insets(2,2,2,2);

    //- Parameters panel contents
    paramPanel = new JPanel();
    paramPanel.setLayout(new GridBagLayout());
    paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
    this.add(paramPanel, p1);
    
    GridBagConstraints c1, c2;
    Insets insts = new Insets(2,2,2,2);
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c1.gridx = 0; c2.gridx = 1;
    c1.gridy = 0; c2.gridy = 0;
    c1.fill  = GridBagConstraints.HORIZONTAL;
    c2.fill  = GridBagConstraints.BOTH;
    c1.insets = insts;
    c2.insets = insts;
    c1.weightx = 0.0;
    c2.weightx = 1.0;    

    //  -Type
    Team [] teams = this.regatta.getTeams();
    int numTeams = teams.length;
    JLabel label = Factory.label("Type:");
    ArrayList<RotationType> types = new ArrayList<RotationType>(4);
    for (RotationType t: RotationType.values()) {
      if (t == RotationType.SWAP) {
	if (numTeams % 2 == 0) {
	  types.add(t);
	}
      }
      else {
	types.add(t);
      }
    }
    typeField = new JComboBox(types.toArray(new RotationType[]{}));
    typeField.addActionListener(this);
    paramPanel.add(label, c1);
    paramPanel.add(typeField, c2);

    //   -Style
    int divNum = this.regatta.getNumDivisions();
    c1.gridy++; c2.gridy++;
    label = Factory.label("Style:");
    styleField = new JComboBox(RotationStyle.values());
    styleField.addActionListener(this);
    paramPanel.add(label, c1);
    paramPanel.add(styleField, c2);
    if (divNum == 1) {
      label.setVisible(false);
      styleField.setVisible(false);
    }

    //   -Sets of races
    c1.gridy++; c2.gridy++;
    label = Factory.label("Races in set:");
    SpinnerNumberModel sm = new SpinnerNumberModel(2, 1,
						   this.regatta.getNumRaces(),
						   1);
    setSizeField = Factory.spinner(sm);
    paramPanel.add(label, c1);
    paramPanel.add(setSizeField, c2);

    //   -Offset: only for more than one division
    c1.gridy++; c2.gridy++;
    int suggest, max;
    max     = numTeams - 1;
    suggest = numTeams / divNum;
    if (suggest == 0 || suggest > max) {
      suggest = 1;
    }
    sm = new SpinnerNumberModel(suggest, 1, max, 1);
    offsetLabel = Factory.label("Amount:");
    offsetField = Factory.spinner(sm);
    paramPanel.add(offsetLabel, c1);
    paramPanel.add(offsetField, c2);
    this.updateOffsetFieldView();

    c1.gridy++; c2.gridy++;
    //      -Races
    Component field;
    label = Factory.label("Races:");
    Integer [] nums =
      Factory.raceToInteger(this.regatta.getRaces(Division.A));
    raceField = new JRangeTextField(nums, nums);
    paramPanel.add(label, c1);
    paramPanel.add(raceField, c2);
    //      -Division order
    c1.gridy++; c2.gridy++;
    label = Factory.label("Division order:");
    divisionField = new JShuffleList(regatta.getDivisions());    
    divisionField.setBorder(BorderFactory.createEtchedBorder());
    paramPanel.add(label, c1);
    paramPanel.add(divisionField, c2);
    // hide if no more than one division
    if (divNum == 1) {
      label.setVisible(false);
      divisionField.setVisible(false);
    }


    //- Boat panel contents: in the case of combined divisions, add
    //- the teams for each division 
    p1.gridy++;
    p1.weighty = 1.0;
    JPanel sailPanel = new JPanel();
    JScrollPane sailScroll = new JScrollPane(sailPanel);
    sailScroll.setBorder(BorderFactory.createTitledBorder("Starting sails"));
    this.add(sailScroll, p1);

    //   -Boats
    sailPanel.setLayout(new GridBagLayout());
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c1.gridx = 0;
    c1.gridy = 0;
    c2.gridx = 1;
    c2.gridy = 0;
    c2.insets = new Insets(2, 2, 2, 2);
    c1.weightx = 0.9;
    c2.weightx = 0.2;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.HORIZONTAL;
    this.sails = new ArrayList<SailSpinner>(teams.length);
    SailSpinner ss;
    for (int i = 0; i < teams.length; i++) {
      // sm = new SpinnerNumberModel(i+1, 1, 100, 1);
      ss = new SailSpinner(new Sail(String.valueOf(i+1)));
      this.sails.add(ss);
      ss.addChangeListener(this);
      String name = teams[i].getLongname() + " " +
	teams[i].getShortname();
      sailPanel.add(Factory.label(name, 150), c1);
      sailPanel.add(ss, c2);
      c1.gridy++;
      c2.gridy++;
    }
    //   add tail
    c1.gridwidth = 2;
    c1.weighty   = 1.0;
    sailPanel.add(new JPanel(), c1);


    //- Button(s)
    p1.gridy++;
    p2.gridx = 1;
    p2.gridy = p1.gridy;
    p2.insets = p1.insets;
    p1.weightx = 0.0;
    p2.weightx = 0.0;
    p1.weighty = 0.0;
    p2.weighty = 0.0;
    p1.fill = GridBagConstraints.NONE;
    p2.fill = GridBagConstraints.NONE;
    p1.anchor = GridBagConstraints.LINE_START;
    p2.anchor = GridBagConstraints.LINE_END;

    this.cAction = new CreateRotationAction();
    this.dAction = new DeleteRotationAction();
    this.add(new JButton(this.dAction), p1);
    this.add(new JButton(this.cAction), p2);
  }

  /**
   * Shows or hides the offset field view depending on the chosen
   * rotation style
   *
   */
  private void updateOffsetFieldView() {
    RotationStyle style =
      (RotationStyle)this.styleField.getSelectedItem();
    this.offsetLabel.setVisible(style == RotationStyle.FRANNY);
    this.offsetField.setVisible(style == RotationStyle.FRANNY);
    this.paramPanel.revalidate();
    this.paramPanel.repaint();
  }

  /*
   * Action Listeners
   */
  public void actionPerformed(ActionEvent ev) {
    JComponent source = (JComponent)ev.getSource();
    if (source == this.styleField) {
      this.updateOffsetFieldView();
    }
    else if (source == this.typeField) {
      RotationType type =
	(RotationType)this.typeField.getSelectedItem();
      this.setSizeField.setEnabled(type != RotationType.NONE);
      this.styleField.setEnabled(type != RotationType.NONE);
    }

  }

  /**
   * Listen to changes to the starting boat numbers. Validate the
   * starting sail numbers, checking for repeats.
   */
  public void stateChanged(ChangeEvent ev) {
    if (ev.getSource() instanceof SpinnerNumberModel) {

      HashSet<Sail> set = new HashSet<Sail>(sails.size());
      for (int i = 0; i < sails.size(); i++) {
	if (!set.add((Sail)sails.get(i).getValue())) {
	  this.cAction.setEnabled(false);
	  return;
	}
      }
      this.cAction.setEnabled(true);
    }
  }

  public boolean isUsable() {
    if (regatta.getTeams().length < 2) {
      return false;
    }
    return true;
  }

  /*
   * Actions
   */
  class CreateRotationAction extends AbstractAction {
    private ICSARotationBuilder builder = new ICSARotationBuilder();

    CreateRotationAction() {
      super("Set rotation");
      putValue(SHORT_DESCRIPTION, "Create/replace rotation.");
    }
    public void actionPerformed(ActionEvent ev) {
      // Style and type, and set size
      RotationType type = (RotationType)typeField.getSelectedItem();
      RotationStyle style =
	(RotationStyle)styleField.getSelectedItem();
      SpinnerNumberModel sm = (SpinnerNumberModel)setSizeField.getModel();
      int setSize = sm.getNumber().intValue();

      // Get organized list of sails and teams
      Map<Sail, Team> sailMap = new TreeMap<Sail, Team>();
      Team [] teams = regatta.getTeams();
      for (int i = 0; i < sails.size(); i++) {
	Sail sail = new Sail(String.valueOf(sails.get(i).getValue()));
	Team team = teams[i];
	if (sailMap.put(sail, team) != null) {
	  // Repeated sails
	  JOptionPane.showMessageDialog(RotationsPane.this,
					"No two teams can have the same sail.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
	  return;
	}

      }

      // Create race map
      ListModel model = divisionField.getModel();
      Integer [] nums = Factory.parseList(raceField.getText().trim());
      if (nums.length == 0) {
	return;
      }
      Race [][] map = new Race[model.getSize()][nums.length];
      for (int i = 0; i < map.length; i++) {
	Division div = (Division)model.getElementAt(i);
	for (int j = 0; j < nums.length; j++) {
	  map[i][j] = regatta.getRace(div, nums[j]);
	}
      }

      Rotation rot = regatta.getRotation();
      if (rot == null) {
	rot = new Rotation();
      }
      try {
	builder.fillRotation(rot,
			     type,
			     style,
			     map,
                             sailMap.values().toArray(new Team[]{}),
			     sailMap.keySet().toArray(new Sail[]{}),
			     setSize);
	regatta.setRotation(rot);
	fill();
      } catch (RotationBuilderException e) {
	System.out.println("Unable to create rotation: " + e.getMessage());
      }

    }
  }

  class DeleteRotationAction extends AbstractAction {
    DeleteRotationAction() {
      super("Delete");
      putValue(SHORT_DESCRIPTION, "Delete current rotation.");
      this.setEnabled(RotationsPane.this.regatta.getRotation() != null);
    }
    public void actionPerformed(ActionEvent e) {
      int returnVal =
	JOptionPane.showConfirmDialog(RotationsPane.this,
				      "Delete the rotation for this regatta?\n" +
				      "This will not affect the finishes.",
				      "Warning",
				      JOptionPane.YES_NO_OPTION,
				      JOptionPane.WARNING_MESSAGE);
      if (returnVal == JOptionPane.YES_OPTION) {
	RotationsPane.this.regatta.setRotation(null);
	fill();
      }
    }
  }
}
