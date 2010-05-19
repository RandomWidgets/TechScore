package edu.mit.techscore.tscore;

import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.RegattaEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import edu.mit.techscore.regatta.Team;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import edu.mit.techscore.regatta.Regatta.Division;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import java.awt.Insets;
import javax.swing.SpinnerDateModel;
import java.util.Date;
import java.util.Calendar;
import javax.swing.JSpinner.DateEditor;
import java.util.List;
import java.util.ArrayList;
import edu.mit.techscore.regatta.RP;
import edu.mit.techscore.regatta.RP.BoatRole;
import edu.mit.techscore.regatta.Sailor;
import edu.mit.techscore.regatta.Race;
import javax.swing.JOptionPane;
import edu.mit.techscore.regatta.RegattaEvent.RegattaEventType;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.BufferedWriter;
import javax.swing.DefaultListModel;

/**
 * Pane for adding/editing record of participation. Use the sailor
 * names from the database of sailors, which must be edited
 * elsewhere.
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
 * Created: Wed Aug  5 23:10:42 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class RPFormPane
  extends AbstractPane
  implements ActionListener {

  // Private components
  private JComboBox teamCombo;
  private JTabbedPane form;
  private AbstractAction enterAction, removeAction;

  // Database
  private File databaseDir;
  private Sailor  [] currentSailors;

  private static final Sailor NO_SAILOR = new Sailor("", "", null);

  /**
   * Creates a new <code>RPFormPane</code> instance.
   *
   */
  public RPFormPane(Regatta reg) {
    super("RP Forms");
    setRegatta(reg);
    this.databaseDir = null;
  }

  public void setDatabaseDir(File dir) {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir + " not a directory.");
    }
    this.databaseDir = dir;
  }

  public File getDatabaseDir() {
    return this.databaseDir;
  }

  public Team [] getTeamsInDatabase() {
    List<String> validAff = new ArrayList<String>();
    try {
      for (File affFile : databaseDir.listFiles()) {
	if (affFile.length() > 1L) {
	  validAff.add(affFile.getName());
	}
      }
      List<Team> validTeams = new ArrayList<Team>();
      for (Team team : regatta.getTeams()) {
	if (validAff.contains(team.getAffiliation())) {
	  validTeams.add(team);
	}
      }
      return validTeams.toArray(new Team[]{});
    } catch (SecurityException e) {
      System.err.println("Unable to read from database.");
    }
    return new Team []{};
  }

  public void fill() {
    this.removeAll();
    this.setLayout(new GridBagLayout());

    GridBagConstraints c1, c2;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c1.gridx = 0; c2.gridx = 1;
    c1.gridy = 0; c2.gridy = 0;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c2.weightx = 1.0;
    c1.insets = new Insets(2,2,2,2);
    c2.insets = new Insets(2,2,2,2);

    //- Team
    JLabel label = Factory.label("Team:");
    this.teamCombo = new JComboBox(this.getTeamsInDatabase());
    this.teamCombo.addActionListener(this);
    this.add(label, c1);
    this.add(teamCombo, c2);

    //- Form
    c1.gridy++; c2.gridy++;
    c1.gridwidth = 2;
    c1.weightx = 1.0;
    c1.weighty = 1.0;
    c1.fill = GridBagConstraints.BOTH;
    this.form = new JTabbedPane();
    this.add(form, c1);

    //- Delete rp button
    c1.gridy++; c2.gridy++;
    c1.gridwidth = 1;
    c1.weightx = 0.0;
    c1.weighty = 0.0;
    c1.fill = GridBagConstraints.NONE;
    c2.fill = GridBagConstraints.NONE;
    c1.anchor = GridBagConstraints.LINE_START;
    c2.anchor = GridBagConstraints.LINE_END;
    this.removeAction = new ClearFormAction();
    JButton button = new JButton(this.removeAction);
    this.add(button, c1);

    //- "Add" button
    this.add(new JButton(new AbstractAction("Add/Update") {
	public void actionPerformed(ActionEvent evt) {
	  Team team = (Team)RPFormPane.this.teamCombo.getSelectedItem();
	  RP rp = RPFormPane.this.regatta.getRP();
	  if (rp == null) {
	    RPFormPane.this.regatta.setRP(rp = new RP());
	  }
	  rp.resetTeam(team);

	  RPRow [] rpRows =
	    ((RPDivisionForm)form.getSelectedComponent()).getRows();
	  for (RPRow row : rpRows) {
	    // Values
	    Sailor sailor = row.getSailor();
	    List<Race> races = row.getRaces();
	    BoatRole role = row.getRole();

	    // Check the validity of the row's data
	    // in this case: name and range of races
	    if (sailor != null &&
		races.size() > 0) {
	      // Enter the data into the RP form
	      rp.addSailorRaces(team, sailor, races, role);
	    }
	  }

	  regatta.fireRegattaChange(new RegattaEvent(regatta,
						     RegattaEventType.RP,
						     this));
	  RPFormPane.this.updateForm();
	}
      }), c2);

    // Setup the form
    for (Division div : this.regatta.getDivisions()) {
      this.form.add(new RPDivisionForm(div));
    }
    updateForm();
  }

  /**
   * Updates the content of the form with the data for the chosen
   * team.
   *
   */
  private void updateForm() {
    this.fillSailors();
    Division [] divisions = Division.values();
    for (int t = 0; t < this.form.getTabCount(); t++) {
      ((RPDivisionForm)this.form.getComponentAt(t)).fill();
    }
  }

  public boolean isUsable() {
    if (this.getTeamsInDatabase().length == 0) {
      return false;
    }
    return true;
  }

  // Implementation of action listener
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == this.teamCombo) {
      this.updateForm();
    }
  }

  /**
   * Fills the list of current sailors to be used in the RP form
   * entry.
   */
  private void fillSailors() {
    List<Sailor> sailors = new ArrayList<Sailor>();
    Calendar cal;
    Team team = (Team)teamCombo.getSelectedItem();
    File file = new File(this.databaseDir, team.getAffiliation());
    try {
      BufferedReader reader =
	new BufferedReader(new FileReader(file));
      String line;
      while ((line = reader.readLine()) != null) {
	String [] fields = line.split("\t");
	String id   = fields[0].trim();
	String name = fields[1].trim();
	cal = Calendar.getInstance();
	try {
	  cal.set(Calendar.YEAR, Integer.valueOf(fields[2]));
	} catch (NumberFormatException e) {}
	sailors.add(new Sailor(id, name, cal.getTime()));
      }

    } catch (FileNotFoundException e) {
      System.err.println("Unknown affiliation file: " + file);
    } catch (IOException e) {
      System.err.println("Unable to read database file: " +
			 file);
    } 
    this.currentSailors = sailors.toArray(new Sailor[]{});
  }

  // Actions
  /**
   * Clears all RP entries for this team, asking for permission first
   *
   */
  class ClearFormAction extends AbstractAction {
    public ClearFormAction() {
      super("Clear");
    }
    
    public void actionPerformed(ActionEvent evt) {
      int returnVal = JOptionPane.showConfirmDialog(RPFormPane.this,
						    "Delete all RP data for this team?",
						    "Warning",
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.WARNING_MESSAGE);
      if (returnVal == JOptionPane.YES_OPTION) {
	Team team = (Team)RPFormPane.this.teamCombo.getSelectedItem();
	RP rp = RPFormPane.this.regatta.getRP();
	if (rp != null) {
	  rp.resetTeam(team);
	}
	RPFormPane.this.updateForm();
	regatta.fireRegattaChange(new RegattaEvent(regatta,
						   RegattaEventType.RP,
						   this));
      }

    }
  }

  /**
   * Represents a row of RP data. The class listenes to changes in the
   * row and updates the RP object with the data.
   *
   */
  private class RPRow {
    // Private variables
    JComboBox roleCombo;
    JTextField raceField;
    JComboBox nameField;
    Division division;
    
    RPRow(Division div, BoatRole role,
	  Sailor sailor, Integer [] races) {
      this(div);
      roleCombo.setSelectedItem(role);
      nameField.setSelectedItem(sailor);
      raceField.setText(Factory.formatList(races));
    }

    RPRow(Division div) {
      this.division = div;
      roleCombo = new JComboBox(BoatRole.values());

      // Name field
      DefaultComboBoxModel lm = new DefaultComboBoxModel(currentSailors);
      lm.insertElementAt(NO_SAILOR, 0);
      nameField = new JComboBox(lm);
      nameField.setRenderer(new SailorNameCellRenderer());
      nameField.setSelectedItem(NO_SAILOR);

      // Race field
      raceField =
	new JRangeTextField(Factory.raceToInteger(regatta.getRaces(Division.A)));
      raceField.setPreferredSize(new Dimension(30,10));
    }
    
    JComponent [] getComponentArray() {
      return new JComponent [] {roleCombo, nameField, raceField};
    }

    boolean contains(Object key) {
      return (roleCombo == key ||
	      nameField == key || raceField == key);
    }

    /**
     * Returns the sailor object described by this row, or
     * <code>null</code> if no such sailor exists (i.e. missing name)
     *
     * @return a <code>Sailor</code> value
     * @return <code>null</code> if incomplete name
     */
    Sailor getSailor() {
      Sailor s = (Sailor)this.nameField.getSelectedItem();
      return (s == NO_SAILOR) ? null : s;
    }

    /**
     * Parses the races field of this row using
     * <code>Factory.parseList</code> method.
     *
     * @return a <code>Race[]</code> value
     */
    List<Race> getRaces() {
      Integer [] raceNums = Factory.parseList(raceField.getText().trim());
      List<Race> races = new ArrayList<Race>(raceNums.length);
      for (Integer n : raceNums) {
	Race race = RPFormPane.this.regatta.getRace(this.division, n);
	if (race != null) {
	  races.add(race);
	}
      }
      return races;
    }

    /**
     * Returns the row's selected boat role.
     *
     * @return a <code>BoatRole</code> value
     */
    BoatRole getRole() {
      return (BoatRole)this.roleCombo.getSelectedItem();
    }
  }

  /**
   * A panel with the form for the team in <code>teamCombo</code> and
   * the specified division.
   *
   */
  class RPDivisionForm extends JPanel {
    List<RPRow> rpRows;
    Division division;
    JScrollPane sp;

    RPDivisionForm(Division div) {
      this.setName("Division " + div);
      this.division = div;
      this.setLayout(new BorderLayout());
      this.add(this.sp = new JScrollPane(), BorderLayout.CENTER);
    }

    RPRow [] getRows() {
      return this.rpRows.toArray(new RPRow[]{});
    }
    
    /**
     * Fills this panel with the form for the division
     *
     */
    void fill() {
      JPanel rpPanel = new JPanel(new GridBagLayout());
      GridBagConstraints [] rpCols = new GridBagConstraints[3];
      Insets insts = new Insets(3,2,3,2);
      rpCols[0] = new GridBagConstraints();
      rpCols[1] = new GridBagConstraints();
      rpCols[2] = new GridBagConstraints();
      int colIndex = 0;
      for (GridBagConstraints c: rpCols) {
	c.fill = GridBagConstraints.BOTH;
	c.gridx = colIndex++;
	c.gridy = 0;
	c.insets = insts;
	c.weighty = 0.0;
      }
      // Weight the name field
      rpCols[1].weightx = 1.0;
      rpCols[2].weightx = 1.0;

      // Add existing rows
      this.rpRows = new ArrayList<RPRow>(3);
      RPRow row;
      JComponent [] comps;
      Team team = (Team)RPFormPane.this.teamCombo.getSelectedItem();
      RP rp = RPFormPane.this.regatta.getRP();
      if (rp != null) {
	for (BoatRole role : BoatRole.values()) {
	  Sailor [] sailors = rp.getSailors(team,
					    this.division,
					    role);
	  for (Sailor sailor : sailors) {
	    Race [] races = rp.getRaces(team, sailor, role);
	    Integer [] nums = new Integer[races.length];
	    for (int i = 0; i < races.length; i++) {
	      nums[i] = new Integer(races[i].getNumber());
	    }

	    // Add row
	    row = new RPRow(this.division, role, sailor, nums);
	    comps = row.getComponentArray();
	    for (int i = 0; i < comps.length; i++) {
	      rpPanel.add(comps[i], rpCols[i]);
	    }
	    for (GridBagConstraints c : rpCols) {
	      c.gridy++;
	    }
	    this.rpRows.add(row);
	  }
	}
      }

      // Add three empty rows
      for (int r = 0; r < 3; r++) {
	row = new RPRow(this.division);
	comps = row.getComponentArray();
	for (int i = 0; i < comps.length; i++) {
	  rpPanel.add(comps[i], rpCols[i]);
	}
	for (GridBagConstraints c : rpCols) {
	  c.gridy++;
	}
	this.rpRows.add(row);
	// Get focus for first button
	if (r == 0) {
	  comps[0].requestFocus();
	}

      }

      // Spacer
      rpCols[0].gridy++;
      rpCols[0].gridwidth = 5;
      rpCols[0].weighty = 1.0;
      rpPanel.add(new JPanel(), rpCols[0]);

      this.sp.setViewportView(rpPanel);
    }
  }
}
