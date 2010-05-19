package tscore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import regatta.Finish;
import regatta.Penalty;
import regatta.Penalty.PenaltyType;
import regatta.Race;
import regatta.Regatta;
import regatta.RegattaEvent;
import regatta.RegattaEvent.RegattaEventType;
import regatta.Team;
import javax.swing.JTextField;
import tscore.FinishesPane.Using;
import regatta.Rotation;
import regatta.Sail;
import java.awt.event.ActionListener;

/**
 * Describe class PenaltiesPane here.
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
public class PenaltiesPane
  extends AbstractPane
  implements ChangeListener {

  private RaceSpinnerModel raceSpinnerModel;
  private JComboBox usingCombo, teamCombo, penaltyCombo;
  private JTextField commentArea;
  private JScrollPane penaltyPanel;

  /**
   * Keep a list of finishes with penalties to expedite the search for
   * current penalties. This list must be synchronized every time the
   * score for the regatta is changed from without the PenaltiesPane,
   * as might be the case if the finishes are deleted.
   */
  private TreeSet<Finish> penaltyList;

  /**
   * Creates a new <code>PenaltiesPane</code> instance.
   *
   */
  public PenaltiesPane(Regatta reg) {
    super("Penalties");
    this.setRegatta(reg);
    this.setIcon(Factory.getImageIcon("img/Penalty16.gif", toString()));
    this.penaltyList = new TreeSet<Finish>();
  }

  /**
   * Updates the list of team to choose from depending on the chosen
   * race so that teams that already have a penalty (or a breakdown)
   * will not show up.
   *
   * 2009-10-02: also attach the sail number, if it exists
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
   * Updates the list of penalties displayed.
   *
   */
  private final void updatePenaltyList() {

    // Add to penaltyPanel
    JPanel panel = new JPanel(new GridBagLayout());
	
    // Prep columns
    Insets insts = new Insets(2,2,2,2);
    GridBagConstraints c1, c2, c3, c4;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();
    c1.gridx = 0; c2.gridx = 1; c3.gridx = 2; c4.gridx = 3;
    c1.gridy = 0; c2.gridy = 0; c3.gridy = 0; c4.gridy = 0;
    c1.insets = insts;
    c2.insets = insts;
    c3.insets = insts;
    c4.insets = insts;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c3.fill = GridBagConstraints.HORIZONTAL;
    c4.fill = GridBagConstraints.HORIZONTAL;
    c2.weightx = 0.5;

    // Header
    panel.add(new JLabel("<html><em>Race</em></html>"), c1);
    panel.add(new JLabel("<html><em>Team</em></html>"), c2);
    panel.add(new JLabel("<html><em>Type</em></html>"), c3);

    c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    ArrayList<Finish> penaltyCopy = new ArrayList<Finish>(penaltyList);
    for (Finish f: penaltyCopy) {
      // Check that it is still a valid finish
      this.penaltyList.remove(f);
      Finish fActual = this.regatta.getFinish(f.getRace(), f.getTeam());
      if (fActual != null && fActual.getPenalty() != null) {
	this.penaltyList.add(fActual);

	panel.
	  add(new JLabel(fActual.getRace().toString()), c1);
	panel.
	  add(new JLabel(fActual.getTeam().getLongname()), c2);
	panel.
	  add(new JLabel(fActual.getPenalty().getType().toString()), c3);
	panel.add(new JButton(new DropPenaltyAction(fActual)), c4);
	
	c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
      }
    }
    // Spacer
    c1.gridwidth = 4;
    c1.weighty   = 1.0;
    panel.add(new JLabel(), c1);

    // If empty, use "No penalties" instead.
    if (this.penaltyList.size() == 0) {
      JLabel label = new JLabel("No penalties.");
      label.setVerticalAlignment(SwingConstants.TOP);
      this.penaltyPanel.setViewportView(label);
    }
    else {
      this.penaltyPanel.setViewportView(panel);
    }
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
    panel.setBorder(BorderFactory.createTitledBorder("New penalty"));
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
    this.penaltyCombo = new JComboBox(PenaltyType.values());
    panel.add(label, c3);
    panel.add(this.penaltyCombo, c4);

    c1.gridy++; c2.gridy++;
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
	  PenaltiesPane.this.updateTeamList();
	}
      });
    panel.add(label, c1);
    panel.add(this.usingCombo, c2);

    c1.gridy++; c2.gridy++;
    //  -Team
    label = Factory.label("Team:");
    this.teamCombo = new JComboBox();
    this.updateTeamList();
    panel.add(label, c1);
    panel.add(teamCombo, c2);

    c1.gridy++; c2.gridy++;
    //  -Comment
    label = Factory.label("Comment:");
    this.commentArea = Factory.editField(null);
    panel.add(label, c1);
    panel.add(this.commentArea, c2);

    //  -Button
    c1 = new GridBagConstraints();
    c1.gridx = 3;
    c1.gridy = 4;
    c1.anchor = GridBagConstraints.LINE_END;
    panel.add(new JButton(new EnterPenaltyAction()), c1);

    //- Existing penalties
    p1.gridy++;
    p1.weighty = 1.0;
    this.penaltyPanel = new JScrollPane();
    penaltyPanel.
      setBorder(BorderFactory.createTitledBorder("Current penalties"));
    this.add(penaltyPanel, p1);

    // Traverse the finishes, looking for penalties, to initialize the
    // list of penalties, to be updated below
    Team [] teams = this.regatta.getTeams();
    for (Race race : this.regatta.getFinishedRaces()) {
      for (Team team : teams) {
	Finish finish = this.regatta.getFinish(race, team);
	if (finish.getPenalty() != null) {
	  this.penaltyList.add(finish);
	}
      }
    }

    this.updatePenaltyList();
  }

  // Implementation of change listener
  public void stateChanged(ChangeEvent evt) {
    Object source = evt.getSource();
    if (source == this.raceSpinnerModel) {
      this.updateTeamList();
    }
  }

  /**
   * Enters the penalty into the parent's regatta.
   *
   */
  class EnterPenaltyAction extends AbstractAction {
    public EnterPenaltyAction() {
      super("Enter penalty");
    }
    
    public void actionPerformed(ActionEvent evt) {
      Object chosenTeam = PenaltiesPane.this.teamCombo.getSelectedItem();
      if (chosenTeam == FinishesPane.NO_TEAM) {
	System.err.println("Pick a team, first.");
	return;
      }
      Race race = (Race)PenaltiesPane.this.raceSpinnerModel.getSelectedRace();
      Team team;
      if (chosenTeam instanceof Team) {
	team = (Team)chosenTeam;
      }
      else {
	Rotation rot = PenaltiesPane.this.regatta.getRotation();
	team = rot.getTeam(race, (Sail)chosenTeam);
      }
      PenaltyType type =
	(PenaltyType)PenaltiesPane.this.penaltyCombo.getSelectedItem();
      String comm = PenaltiesPane.this.commentArea.getText();

      Finish f = regatta.getFinish(race, team);
      f.setPenalty(new Penalty(type, comm));
      PenaltiesPane.this.penaltyList.add(f);

      // Reset team/comments
      PenaltiesPane.this.teamCombo.setSelectedItem(FinishesPane.NO_TEAM);
      PenaltiesPane.this.commentArea.setText(null);

      regatta.fireRegattaChange(new RegattaEvent(regatta,
						 RegattaEventType.FINISH,
						 PenaltiesPane.this));
      PenaltiesPane.this.updatePenaltyList();
      PenaltiesPane.this.updateTeamList();
    }
  }

  /**
   * Drops the object's finish from the list of penalty finishes.
   *
   */
  class DropPenaltyAction extends AbstractAction {
    private Finish penalty;

    public DropPenaltyAction(Finish penalty) {
      super("Drop");
      this.penalty = penalty;
    }
    
    public void actionPerformed(ActionEvent evt) {
      Finish f = regatta.getFinish(this.penalty.getRace(),
				   this.penalty.getTeam());
      f.setPenalty(null);
      regatta.fireRegattaChange(new RegattaEvent(regatta,
						 RegattaEventType.FINISH,
						 PenaltiesPane.this));
      PenaltiesPane.this.updatePenaltyList();
      PenaltiesPane.this.updateTeamList();
    }
  }
  
  public boolean isUsable() {
    if (this.regatta.getFinishedRaces().length == 0) {
      return false;
    }
    return true;
  }
}
