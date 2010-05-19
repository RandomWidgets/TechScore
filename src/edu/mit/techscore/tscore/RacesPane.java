package edu.mit.techscore.tscore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.Division;
import edu.mit.techscore.regatta.Team;
import edu.mit.techscore.regatta.RegattaEvent;
import edu.mit.techscore.regatta.RegattaEvent.RegattaEventType;
import javax.swing.ImageIcon;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import javax.swing.JScrollBar;
import javax.swing.ScrollPaneConstants;

/**
 * Describe class RacesPane here.
 * TODO
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
 * Created: Thu Jun 11 21:46:35 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RacesPane extends AbstractPane
  implements ChangeListener {

  private JSpinner divSpinner, raceSpinner;
  private JScrollPane boatScroll, teamScroll;
  private AddTeamAction latestAddTeamAction;  // keep track of team action to add

  /**
   * Creates a new <code>RacesPane</code> instance.
   *
   */
  public RacesPane(Regatta reg) {
    super("Races & Teams");
    this.setIcon(Factory.getImageIcon("img/Team16.gif", toString()));
    this.setLayout(new GridBagLayout());
    this.setRegatta(reg);
  }

  public void fill() {
    this.removeAll();

    GridBagConstraints p1 = new GridBagConstraints();
    p1.weightx = 1.0;
    p1.fill = GridBagConstraints.BOTH;
    p1.gridx = 0; p1.gridy = 0;
    p1.insets = new Insets(2,2,2,2);

    // Disable certain buttons if there already exists a rotation
    boolean hasRotation = (regatta.getRotation() != null);
    boolean hasScores   = (regatta.getFinishedRaces().length > 0);
    boolean inProgress  = hasRotation || hasScores;

    JPanel divPanel = new JPanel();
    divPanel.setBorder(BorderFactory.createTitledBorder("Divisions"));
    this.add(divPanel, p1);

    // Division
    JLabel label, label2;
    divPanel.setLayout(new GridBagLayout());
    GridBagConstraints c1, c2, c3, c4;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();

    c1.gridx = 0;
    c2.gridx = 1;
    c3.gridx = 2;
    c4.gridx = 3;
    
    c2.weightx = 0.5;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c4.weightx = 0.5;
    c4.fill = GridBagConstraints.HORIZONTAL;

    label = Factory.label("# of Divisions:");
    SpinnerNumberModel sm =
      new SpinnerNumberModel(regatta.getNumDivisions(), 1, Division.values().length, 1);
    sm.addChangeListener(this);
    divSpinner = new TSpinner(sm);
    divPanel.add(label, c1);
    divPanel.add(divSpinner, c2);

    ////
    label = Factory.label("# of Races:");
    sm = new SpinnerNumberModel(regatta.getNumRaces(), 1, 50, 1);
    sm.addChangeListener(this);
    raceSpinner = new TSpinner(sm);
    divPanel.add(label, c3);
    divPanel.add(raceSpinner, c4);

    //////// Teams
    p1.gridy++;
    p1.weighty = 1.0;
    JPanel teamPanel = new JPanel();
    teamScroll = new JScrollPane(teamPanel);
    teamScroll.setBorder(BorderFactory.createTitledBorder("Teams"));
    this.add(teamScroll, p1);
    
    ////
    this.updateTeamPanel(!inProgress);
    this.updateBoatPanel();
  }

  public String toString() {
    return "Races & Teams";
  }

  /*
   * Listeners
   */
  public void stateChanged(ChangeEvent e) {
    SpinnerNumberModel sm = (SpinnerNumberModel)e.getSource();
    if (sm == this.divSpinner.getModel()) {
      int newDivCount = sm.getNumber().intValue();
      if (newDivCount != this.regatta.getNumDivisions()) {
	this.regatta.updateDivisions(newDivCount);
	this.updateBoatPanel();
      }
    }
    else if (sm == this.raceSpinner.getModel()) {
      int newRaceCount = sm.getNumber().intValue();
      if (newRaceCount != this.regatta.getNumRaces()) {
	this.regatta.updateRaces(newRaceCount);
	this.updateBoatPanel();
      }
    }
  }

  public boolean isUsable() {
    return true;
  }

  /*
   * Helpers
   */
  private void updateBoatPanel() {
    if (true) {
      return;
    }

    JPanel boatPanel = new JPanel();
    this.boatScroll.setViewportView(boatPanel);
    int numDivs  = this.regatta.getNumDivisions();
    int numRaces = this.regatta.getNumRaces();
    boatPanel.setLayout(new GridBagLayout());
    GridBagConstraints c, c2, c3;
    c  = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.25;
    c2.gridx = 0;
    c2.gridy = 0;
    c2.insets = new Insets(2, 2, 4, 2);
    c2.weightx = 0.5;

    // Header
    c2.fill = GridBagConstraints.HORIZONTAL;
    boatPanel.add(new JLabel(), c);
    Division [] divs = this.regatta.getDivisions();
    for (int i = 0; i < numDivs; i++) {
      c2.gridx = (i+1);
      boatPanel.add(new JLabel("Division " + divs[i].toString()), c2);
    }
    c.gridy = 1;
    boatPanel.add(Factory.label("All"), c);
    c2.gridy = 1;
    for (int i = 0; i < numDivs; i++) {
      c2.gridx = (i+1);
      boatPanel.add(Factory.editField(null), c2);
    }
    // Divider
    c3 = new GridBagConstraints();
    c3.fill = GridBagConstraints.HORIZONTAL;
    c3.gridwidth = (numDivs);
    c3.gridx = 1;
    c3.gridy = 2;
    c3.ipady = 10;
    boatPanel.add(new JSeparator(), c3);
    
    // Body
    c2.insets = new Insets(2, 2, 2, 2);
    for (int r = 0; r < numRaces; r++) {
      c.gridy  = (r+3);
      c2.gridy = (r+3);
      boatPanel.add(Factory.label("Race " + (r+1)), c);
      for (int d = 0; d < numDivs; d++) {
	c2.gridx = (d+1);
	boatPanel.add(Factory.editField(null), c2);
      }
    }
    // Tail
    c3.ipady = 0;
    c3.gridx = 0;
    c3.gridy = (numRaces + 3);
    c2.gridwidth++;
    c3.weighty = 1.0;
    boatPanel.add(new JPanel(), c3);

  }

  private void updateTeamPanel() {
    this.updateTeamPanel(regatta.getRotation() == null &&
			 regatta.getFinishedRaces().length == 0);
  }
  
  private void updateTeamPanel(boolean canAddTeam) {
    JPanel teamPanel = new JPanel();
    teamPanel.setLayout(new GridBagLayout());
    this.teamScroll.setViewportView(teamPanel);

    // c:   label
    // c2:  Long name
    // c3:  Short name
    // c4:  Button
    GridBagConstraints c1, c2, c3, c4, c5;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();
    c5 = new GridBagConstraints();
    Insets ints = new Insets(2, 2, 2, 2);
    c1.gridx = 0;
    c1.gridy = 0;
    // c2.weightx = 0.1;

    c2.gridx = 1;
    c2.gridy = 0;
    c2.insets = ints;
    c2.weightx = 1.0;
    c2.fill = GridBagConstraints.HORIZONTAL;

    c3.gridx = 2;
    c3.gridy = 0;
    c3.insets = ints;
    c3.weightx = 0.4;
    c3.fill = GridBagConstraints.HORIZONTAL;

    c4.gridx = 3;
    c4.gridy = 0;
    c4.insets = ints;
    c5.gridx = 4;
    c5.gridy = 0;
    c5.insets = ints;

    // Header
    teamPanel.add(new JLabel("#"), c1);
    teamPanel.add(new JLabel("Full name"), c2);
    teamPanel.add(new JLabel("Nick name"), c3);
    teamPanel.add(new JLabel(), c4);

    // Content
    Team [] teams = this.regatta.getTeams();
    for (int i = 0; i < teams.length; i++) {
      c1.gridy++;
      c2.gridy++;
      c3.gridy++;
      c4.gridy++;
      c5.gridy++;
      teamPanel.add(new JLabel(" " + (i+1) + "."), c1);
      JTextField l = new EditField(teams[i].getLongname(), 8);
      JTextField s = new EditField(teams[i].getShortname(),5);
      teamPanel.add(l, c2);
      teamPanel.add(s, c3);
      teamPanel.add(Factory.tightButton(new EditTeamAction(teams[i], l, s)), c4);
      JButton remButton =
	Factory.tightButton(new RemoveTeamAction(teams[i]));
      remButton.setEnabled(canAddTeam);
      remButton.setToolTipText("Cannot delete teams.");
      teamPanel.add(remButton, c5);
    }
    // Add new one, if no rotations exist yet
    if (canAddTeam) {
      c1.gridy++;
      c2.gridy++;
      c3.gridy++;
      c4.gridy++;
      c4.gridwidth = 2;
      c4.fill = GridBagConstraints.HORIZONTAL;
      teamPanel.add(new JLabel((teams.length+1) + "."), c1);
      JTextField l = new EditField("", 8);
      JTextField s = new EditField("", 5);
      this.latestAddTeamAction = new AddTeamAction(l, s);
      teamPanel.add(l, c2);
      teamPanel.add(s, c3);
      teamPanel.add(Factory.tightButton(this.latestAddTeamAction), c4);
      l.requestFocusInWindow();
    }
    else {
      // add message as to why no teams can be added
      c2.gridy++;
      c2.gridwidth = 5;
      c2.gridx = 0;
      String mes = "<html><em>No new teams can be added " +
	"after regatta is under way.</em>";
      teamPanel.add(new JLabel(mes), c2);
    }

    // Tail
    c2.gridy++;
    c2.gridx = 0;
    c2.gridwidth = 4;
    c2.weighty = 1.0;
    teamPanel.add(new JLabel(), c2);
  }

  /**
   * If necessary, prompts to create any pending teams
   *
   * @return true if done, or false if more editing is required
   */
  public boolean empty() {
    if (this.latestAddTeamAction == null)
      return true;
    
    JTextField longField  = this.latestAddTeamAction.getLongNameField();
    JTextField shortField = this.latestAddTeamAction.getShortNameField();
    String longName  = longField.getText().trim();
    if (longName.length() > 0) {
      String shortName = shortField.getText().trim();
      String mes = String.format("Create new team %s %s?\n\n", longName, shortName);
      int n = JOptionPane.showConfirmDialog(this, mes, "Pending new team",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE);
      if (n == JOptionPane.CANCEL_OPTION) {
	return false;
      }
      else if (n == JOptionPane.YES_OPTION) {
	this.latestAddTeamAction.actionPerformed(new ActionEvent(this, 0, "Update prior to leaving"));
      }
    }
    return true;
  }

  /**
   * Adds a team to the regatta
   */
  class AddTeamAction extends AbstractAction {
    private JTextField lName;
    private JTextField sName;
    
    AddTeamAction(JTextField l, JTextField s) {
      super("Create");
      putValue(SHORT_DESCRIPTION, "Add this team.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
						       InputEvent.SHIFT_MASK));
      this.lName = l;
      this.sName = s;
    }
    public void actionPerformed(ActionEvent ev) {
      String l = lName.getText().trim();
      String s = sName.getText().trim();
      if (l.length() == 0) {
	System.out.println("Primary name must not be empty.");
	return;
      }
      Team t = new Team(l, s);
      t.setAffiliation("_MISC");
      if (!RacesPane.this.regatta.addTeam(t)) {
	String mes = "Team with this name already exists.";
	System.err.println(mes);
	JOptionPane.showMessageDialog(RacesPane.this, mes, "Error",
				      JOptionPane.WARNING_MESSAGE);
	this.sName.setText("");
	this.sName.requestFocusInWindow();
	return;
      }
      RacesPane.this.updateTeamPanel();
      // Scroll to bottom
      int max = teamScroll.getVerticalScrollBar().getMaximum();
      teamScroll.getVerticalScrollBar().setValue(max);
    }

    public JTextField getLongNameField()  { return this.lName; }
    public JTextField getShortNameField() { return this.sName; }
  }

  /**
   * Edits a team name
   */
  class EditTeamAction extends AbstractAction {
    private Team team;
    private JTextField lName;
    private JTextField sName;

    EditTeamAction(Team t, JTextField l, JTextField s) {
      super(null, Factory.getImageIcon("img/Edit16.gif"));
      putValue(SHORT_DESCRIPTION, "Edit this team name.");
      this.lName = l;
      this.sName = s;
      this.team = t;
    }
    public void actionPerformed(ActionEvent ev) {
      String l = lName.getText().trim();
      String s = sName.getText().trim();
      if (l.length() == 0) {
	System.err.println("Primary name must not be empty.");
	RacesPane.this.updateTeamPanel();
	return;
      }
      Team t = new Team(l, s);
      Set<Team> teamSet =
	new HashSet<Team>(Arrays.asList(RacesPane.this.regatta.getTeams()));

      // If regatta allows the new team to be added
      // then edit the team's details
      if (teamSet.add(t)) {
	this.team.setLongname(l);
	this.team.setShortname(s);
	RegattaEvent evt = new RegattaEvent(regatta,
					    RegattaEventType.TEAM,
					    this);
	RacesPane.this.regatta.fireRegattaChange(evt);
      }
      else {
	String mes = "New name conflict with current team names.";
	JOptionPane.showMessageDialog(RacesPane.this, mes, "Error",
				      JOptionPane.WARNING_MESSAGE);
      }
      RacesPane.this.updateTeamPanel();
    }
  }

  /**
   * Removes a team from the regatta
   */
  class RemoveTeamAction extends AbstractAction {
    private Team team;

    RemoveTeamAction(Team t) {
      super(null, Factory.getImageIcon("img/Delete16.gif"));
      putValue(SHORT_DESCRIPTION, "Remove this team.");

      this.team = t;
    }
    public void actionPerformed(ActionEvent ev) {
      // If rotations exist already, ask before deleting
      if (regatta.getRotation() != null) {
	String mes = "Removing a team will also delete\n" +
	  "all rotations and scores associated\n" +
	  "with the team. Continue?";
	int returnVal = JOptionPane.showConfirmDialog(RacesPane.this,
						      mes,
						      "Delete team",
						      JOptionPane.OK_CANCEL_OPTION);
	if (returnVal == JOptionPane.OK_OPTION) {
	  RacesPane.this.regatta.removeTeam(this.team);
	  RacesPane.this.updateTeamPanel();
	}
	else {
	  System.err.println("Canceled team deletion.");
	}
      }
      else {
	RacesPane.this.regatta.removeTeam(this.team);
	RacesPane.this.updateTeamPanel();
      }
    }
  }
}
