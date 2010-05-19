package tscore;

import regatta.Regatta;
import javax.swing.SpringLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSpinner;
import tscore.FinishesPane.Using;
import java.util.Arrays;
import java.util.ArrayList;
import regatta.Race;
import javax.swing.JTextArea;
import regatta.Finish;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import regatta.RegattaEvent;
import regatta.RegattaEvent.RegattaEventType;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import regatta.Team;
import regatta.Breakdown;
import regatta.Breakdown.BreakdownType;

import regatta.Breakdown.BreakdownType;
import javax.swing.JOptionPane;
import java.util.TreeSet;
import java.awt.BorderLayout;
import javax.swing.DefaultComboBoxModel;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import javax.swing.SpinnerNumberModel;
import java.util.Collections;
import javax.swing.SpinnerListModel;
import javax.swing.JTextField;
import tscore.ICSAScorer.CleanPlaceComparator;
import regatta.Rotation;
import regatta.Sail;
import javax.swing.SpinnerModel;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;

/**
 * Describe class BreakdownsPane here.
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
 * Created: Tue Jul 28 17:48:56 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class BreakdownsPane
  extends AbstractPane
  implements ChangeListener, ActionListener {

  private RaceSpinnerModel raceSpinnerModel;
  private JComboBox usingCombo, teamCombo, breakdownCombo;
  private JSpinner handicapSpinner;
  private AbstractButton averageAmount;
  private JTextField commentArea;
  private JScrollPane breakdownPanel;

  /**
   * Keep a list of finishes with penalties to expedite the search for
   * current penalties. This list must be synchronized every time the
   * score for the regatta is changed from without the BreakdownsPane,
   * as might be the case if the finishes are deleted.
   */
  private TreeSet<Finish> breakdownList;

  /**
   * Creates a new <code>BreakdownsPane</code> instance.
   *
   */
  public BreakdownsPane(Regatta reg) {
    super("Breakdowns");
    this.setRegatta(reg);
    this.setIcon(Factory.getImageIcon("img/Breakdown16.gif", toString()));
    this.breakdownList = new TreeSet<Finish>();
  }

  /**
   * Updates the list of team to choose from depending on the chosen
   * race so that teams that already have a breakdown (or a breakdown)
   * will not show up.
   *
   */
  private final void updateTeamList() {
    Race race = this.raceSpinnerModel.getSelectedRace();
    Using using = (Using)this.usingCombo.getSelectedItem();
    Team [] teams = this.regatta.getTeams();
    List<Object> teamList = new ArrayList<Object>(teams.length + 1);
    teamList.add(FinishesPane.NO_TEAM);
    if (using == Using.TEAM) {
      teamList.addAll(Arrays.asList(teams));
    }
    else {
      Rotation rot = this.regatta.getRotation();
      teamList.addAll(Arrays.asList(rot.getSails(race)));
    }
    this.teamCombo.setModel(new DefaultComboBoxModel(teamList.toArray()));
  }

  /**
   * Updates the handicap spinner so that the maximum new score that
   * can be awarded to a team does not exceed that team's place among
   * non-penalized, regular finishes.
   */
  private final void updateHandicapSpinner() {
    Race race = this.raceSpinnerModel.getSelectedRace();
    Using using = (Using)this.usingCombo.getSelectedItem();
    Object selected = this.teamCombo.getSelectedItem();
    BreakdownType type = (BreakdownType)this.breakdownCombo.getSelectedItem();
    if (selected == FinishesPane.NO_TEAM) {
      this.breakdownCombo.setEnabled(false);
      this.handicapSpinner.setEnabled(false);
      return;
    }
    this.breakdownCombo.setEnabled(true);
    this.handicapSpinner.setEnabled(true);
    Team team;
    if (using == Using.TEAM) {
      team = (Team)selected;
    }
    else {
      Rotation rot = this.regatta.getRotation();
      team = rot.getTeam(race, (Sail)selected);
    }

    // Use CleanPlaceComparator to determine where this team should be
    ArrayList<Finish> list =
      new ArrayList<Finish>(Arrays.asList(this.regatta.getFinishes(race)));
    Collections.sort(list, new ICSAScorer.CleanPlaceComparator());

    // Create a list of acceptable new scores
    int max = list.indexOf(this.regatta.getFinish(race, team));
    List<Integer> availableScores = new ArrayList<Integer>(list.size());
    for (int i = 0; i < max; i++) {
      availableScores.add(new Integer(i+1));
    }

    // Sift through the current list of breakdowns and remove
    // unavailable scores
    for (Finish f : this.breakdownList) {
      Breakdown bd = (Breakdown)f.getAdjustment();
      if (bd.getHandicap() > 0) {
	availableScores.remove(new Integer(bd.getHandicap()));
      }
    }

    this.handicapSpinner.setModel(new SpinnerListModel(availableScores));
  }

  /**
   * Updates the list of penalties displayed.
   *
   */
  private final void updateBreakdownList() {
    if (this.breakdownList.size() == 0) {
      JLabel label = new JLabel("No breakdowns.");
      label.setVerticalAlignment(SwingConstants.TOP);
      this.breakdownPanel.setViewportView(label);
      return;
    }

    // Add to breakdownPanel
    JPanel panel = new JPanel(new GridBagLayout());
	
    // Prep columns
    Insets insts = new Insets(2,2,2,2);
    GridBagConstraints c0, c1, c2, c3, c4;
    c0 = new GridBagConstraints();
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();
    c0.gridx = 0; c1.gridx = 1; c2.gridx = 2; c3.gridx = 3; c4.gridx = 4;
    c0.gridy = 0; c1.gridy = 0; c2.gridy = 0; c3.gridy = 0; c4.gridy = 0;
    c0.insets = insts;
    c1.insets = insts;
    c2.insets = insts;
    c3.insets = insts;
    c4.insets = insts;
    c0.fill = GridBagConstraints.HORIZONTAL;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c3.fill = GridBagConstraints.HORIZONTAL;
    c4.fill = GridBagConstraints.HORIZONTAL;
    c1.weightx = 0.5;

    // Header
    panel.add(new JLabel("<html><em>Race</em></html>"), c0);
    panel.add(new JLabel("<html><em>Team</em></html>"), c1);
    panel.add(new JLabel("<html><em>Type</em></html>"), c2);
    panel.add(new JLabel("<html><em>Amt</em></html>"), c3);

    c0.gridy++; c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    ArrayList<Finish> breakdownCopy = new ArrayList<Finish>(breakdownList);
    for (Finish f: breakdownCopy) {
      // Check that it is a valid finish, still
      this.breakdownList.remove(f);
      Finish fActual = this.regatta.getFinish(f.getRace(), f.getTeam());
      if (fActual != null && fActual.getBreakdown() != null) {
	this.breakdownList.add(fActual);
	Breakdown bkd = fActual.getBreakdown();
	String score  = (bkd.getHandicap() <= 0)
	  ? "Avg" : String.valueOf(bkd.getHandicap());

	panel.
	  add(new JLabel(fActual.getRace().toString()), c0);
	panel.
	  add(new JLabel(fActual.getTeam().getLongname()), c1);
	panel.add(new JLabel(bkd.getType().toString()), c2);
	panel.add(new JLabel(score), c3);
	panel.add(new JButton(new DropBreakdownAction(fActual)), c4);
	
	c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
      }
    }
    // Spacer
    c1.gridwidth = 5;
    c1.weighty   = 1.0;
    panel.add(new JLabel(), c1);

    this.breakdownPanel.setViewportView(panel);
  }

  public void fill() {
    this.removeAll();
    Race [] finishedRaces = this.regatta.getFinishedRaces();

    this.setLayout(new GridBagLayout());
    GridBagConstraints p1 = new GridBagConstraints();
    p1.weightx = 1.0;
    p1.fill = GridBagConstraints.BOTH;
    p1.gridx = 0; p1.gridy = 0;
    p1.insets = new Insets(2,2,2,2);

    //- New penalties
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("New breakdown"));
    this.add(panel, p1);

    JLabel label, label2;
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c1, c2, c3, c4;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();

    c1.gridx = 0; c2.gridx = 1; c3.gridx = 2; c4.gridx = 3;
    c1.gridy = 0; c2.gridy = 0; c3.gridy = 0; c4.gridy = 0;
    c1.anchor = GridBagConstraints.FIRST_LINE_END;
    c2.anchor = GridBagConstraints.PAGE_START;
    c3.anchor = GridBagConstraints.FIRST_LINE_END;
    c4.anchor = GridBagConstraints.PAGE_START;
    
    c1.weightx = 0.25;
    c2.weightx = 0.35;
    c3.weightx = 0.25;
    c4.weightx = 0.35;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c4.fill = GridBagConstraints.BOTH;

    Insets insts = new Insets(2,2,2,2);
    c1.insets = insts;
    c2.insets = insts;
    c3.insets = insts;
    c4.insets = insts;

    //  -Race
    label = Factory.label("Race:");
    this.raceSpinnerModel = new RaceSpinnerModel(finishedRaces);
    this.raceSpinnerModel.addChangeListener(this);
    panel.add(label, c1);
    panel.add(Factory.spinner(raceSpinnerModel), c2);

    //  -Type
    label = Factory.label("Type:");
    this.breakdownCombo = new JComboBox(BreakdownType.values());
    this.breakdownCombo.addActionListener(this);
    panel.add(label, c3);
    panel.add(this.breakdownCombo, c4);

    c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    c2.gridwidth = 3;
    //  -Using
    label = Factory.label("Using:");
    List<Using> useValues =
      new ArrayList<Using>(Arrays.asList(Using.values()));
    if (this.regatta.getRotation() == null) {
      useValues.remove(Using.ROTATION);
    }
    this.usingCombo = new JComboBox(useValues.toArray());
    this.usingCombo.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  BreakdownsPane.this.updateTeamList();
	}
      });
    panel.add(label, c1);
    panel.add(this.usingCombo, c2);

    c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    //  -Team
    label = Factory.label("Team:");
    this.teamCombo = new JComboBox();
    this.teamCombo.addActionListener(this);
    this.updateTeamList();
    panel.add(label, c1);
    panel.add(teamCombo, c2);

    c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    c2.gridwidth = 1;
    //  -Amount
    label = Factory.label("New score:");
    this.handicapSpinner = Factory.spinner();
    this.updateHandicapSpinner();
    panel.add(label, c1);
    panel.add(handicapSpinner, c2);

    //    -Average?
    this.averageAmount = new JCheckBox(new AbstractAction() {
	public void actionPerformed(ActionEvent evt) {
	  boolean sel = BreakdownsPane.this.averageAmount.isSelected();
	  boolean has =
	    (BreakdownsPane.this.teamCombo.getSelectedItem() !=
	     FinishesPane.NO_TEAM);
	  BreakdownsPane.this.handicapSpinner.setEnabled(!sel && has);
	}
      });
    label = new JLabel("Average");
    label.setLabelFor(this.averageAmount);
    c3.fill = GridBagConstraints.NONE;
    panel.add(this.averageAmount, c3);
    panel.add(label, c4);
    
    c1.gridy++; c2.gridy++;
    c2.gridwidth = 3;
    //  -Comment
    label = Factory.label("Comment:", 120);
    this.commentArea = Factory.editField(null);
    panel.add(label, c1);
    panel.add(this.commentArea, c2);

    //  -Button
    c1 = new GridBagConstraints();
    c1.gridwidth = 3;
    c1.gridx = 1;
    c1.gridy = 5;
    c1.fill = GridBagConstraints.NONE;
    c1.anchor = GridBagConstraints.LINE_END;
    panel.add(new TButton(new EnterBreakdownAction()), c1);

    //- Existing penalties
    p1.gridy++;
    p1.weighty = 1.0;
    this.breakdownPanel = new JScrollPane();
    breakdownPanel.
      setBorder(BorderFactory.createTitledBorder("Current penalties"));
    this.add(breakdownPanel, p1);

    // Traverse the finishes, looking for breakdowns, to initialize the
    // list of breakdowns, to be updated below
    Team [] teams = this.regatta.getTeams();
    for (Race race : this.regatta.getFinishedRaces()) {
      for (Team team : teams) {
	Finish finish = this.regatta.getFinish(race, team);
	if (finish.getBreakdown() != null) {
	  this.breakdownList.add(finish);
	}
      }
    }

    this.updateBreakdownList();
  }

  // Implementation of change listener
  public void stateChanged(ChangeEvent evt) {
    Object source = evt.getSource();
    if (source == this.raceSpinnerModel) {
      this.updateTeamList();
    }
  }

  // Implementation of action listener
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == this.teamCombo ||
	source == this.breakdownCombo) {
      try {
	this.updateHandicapSpinner();
      } catch (IllegalArgumentException e) {
	JOptionPane.showMessageDialog(this,
				      "Team is already in first place.",
				      "Error",
				      JOptionPane.WARNING_MESSAGE);
	this.teamCombo.setSelectedItem(FinishesPane.NO_TEAM);
	this.handicapSpinner.setEnabled(false);
      }
    }
  }

  /**
   * Enters the breakdown into the parent's regatta.
   *
   */
  class EnterBreakdownAction extends AbstractAction {
    public EnterBreakdownAction() {
      super("Enter breakdown");
    }
    
    public void actionPerformed(ActionEvent evt) {
      Object chosenTeam = BreakdownsPane.this.teamCombo.getSelectedItem();
      if (chosenTeam == FinishesPane.NO_TEAM) {
	System.err.println("Pick a team, first.");
	return;
      }
      Race race = (Race)BreakdownsPane.this.raceSpinnerModel.getSelectedRace();
      Team team;
      if (chosenTeam instanceof Team) {
	team = (Team)chosenTeam;
      }
      else {
	Rotation rot = BreakdownsPane.this.regatta.getRotation();
	team = rot.getTeam(race, (Sail)chosenTeam);
      }
      BreakdownType type =
	(BreakdownType)BreakdownsPane.this.breakdownCombo.getSelectedItem();
      String comm = BreakdownsPane.this.commentArea.getText();
      Breakdown brk = new Breakdown(type, comm);

      // Get handicap
      if (BreakdownsPane.this.averageAmount.isSelected()) {
	brk.setHandicap(-1);
      }
      else {
	SpinnerModel sm = handicapSpinner.getModel();
	brk.setHandicap(Integer.parseInt(sm.getValue().toString()));
      }

      Finish f = regatta.getFinish(race, team);
      f.setBreakdown(brk);
      BreakdownsPane.this.breakdownList.add(f);

      // Reset team/comments
      BreakdownsPane.this.teamCombo.setSelectedItem(FinishesPane.NO_TEAM);
      BreakdownsPane.this.commentArea.setText(null);

      BreakdownsPane.this.updateBreakdownList();
      BreakdownsPane.this.updateTeamList();

      // Notify of changed finishes
      BreakdownsPane.this.regatta.
	fireRegattaChange(new RegattaEvent(regatta,
					   RegattaEventType.FINISH,
					   BreakdownsPane.this));
    }
  }

  /**
   * Drops the object's finish from the list of breakdown finishes.
   *
   */
  class DropBreakdownAction extends AbstractAction {
    private Finish breakdown;

    public DropBreakdownAction(Finish breakdown) {
      super("Drop");
      this.breakdown = breakdown;
    }
    
    public void actionPerformed(ActionEvent evt) {
      Finish f = regatta.getFinish(this.breakdown.getRace(),
				   this.breakdown.getTeam());
      f.setBreakdown(null);
      BreakdownsPane.this.updateBreakdownList();
      BreakdownsPane.this.updateTeamList();
      regatta.fireRegattaChange(new RegattaEvent(regatta,
						 RegattaEventType.FINISH,
						 BreakdownsPane.this));
    }
  }
  
  public boolean isUsable() {
    if (this.regatta.getFinishedRaces().length == 0) {
      return false;
    }
    return true;
  }
}
