package edu.mit.techscore.tscore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.Division;
import edu.mit.techscore.regatta.Team;
import edu.mit.techscore.regatta.TeamPenalty;
import edu.mit.techscore.regatta.TeamPenalty.TeamPenaltyType;
import edu.mit.techscore.regatta.RegattaEvent;
import edu.mit.techscore.regatta.RegattaEvent.RegattaEventType;

/**
 * Describe class TeamPenaltiesPane here.
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
 * Created: Thu Sep  3 09:48:42 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class TeamPenaltiesPane extends AbstractPane {

  // Private variables
  private JComboBox teamBox, divisionBox, penaltyBox;
  private JTextArea commentBox;
  private JScrollPane penaltyPanel;

  /**
   * Creates a new <code>TeamPenaltiesPane</code> instance.
   *
   */
  public TeamPenaltiesPane(Regatta reg) {
    super("Team penalties");
    this.setRegatta(reg);
  }

  public void fill() {
    this.removeAll();
    this.setLayout(new GridBagLayout());
    GridBagConstraints p1 = new GridBagConstraints();
    p1.weightx = 1.0;
    p1.weighty = 0.0;
    p1.fill = GridBagConstraints.BOTH;
    p1.gridx = 0; p1.gridy = 0;
    p1.insets = new Insets(2,2,2,2);

    //- New penalties
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("New penalty"));
    this.add(panel, p1);

    JLabel label;
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c1, c2;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c1.gridx = 0; c2.gridx = 1;
    c1.gridy = 0; c2.gridy = 0;
    c1.weightx = 0.0;
    c2.weightx = 1.0;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.BOTH;
    c1.insets = new Insets(2,2,2,2);
    c2.insets = c1.insets;
    
    //  -Team
    label = Factory.label("Team:");
    this.teamBox = new JComboBox(this.regatta.getTeams());
    panel.add(label, c1);
    panel.add(teamBox, c2);

    //  -Divisions
    c1.gridy++; c2.gridy++;
    label = Factory.label("Division:");
    this.divisionBox = new JComboBox(this.regatta.getDivisions());
    panel.add(label, c1);
    panel.add(divisionBox, c2);

    //  -Penalty
    c1.gridy++; c2.gridy++;
    label = Factory.label("Penalty:");
    this.penaltyBox = new JComboBox(TeamPenaltyType.values());
    panel.add(label, c1);
    panel.add(penaltyBox, c2);

    //  -Comments
    c1.gridy++; c2.gridy++;
    c1.anchor = GridBagConstraints.PAGE_START;
    label = Factory.label("Comments:");
    this.commentBox = new JTextArea(3, 15);
    panel.add(label, c1);
    panel.add(new JScrollPane(commentBox), c2);

    //  -Enter button
    c2.gridy++;
    c2.anchor = GridBagConstraints.LINE_END;
    c2.fill   = GridBagConstraints.NONE;
    panel.add(new TButton(new AbstractAction("Add") {
	public void actionPerformed(ActionEvent evt) {
	  // Create penalty
	  Division div = (Division)divisionBox.getSelectedItem();
	  Team team    = (Team)teamBox.getSelectedItem();
	  TeamPenaltyType type
	    = (TeamPenaltyType) penaltyBox.getSelectedItem();
	  String comms = commentBox.getText();
	  TeamPenalty pen = new TeamPenalty(div, team, type, comms);
	  regatta.setTeamPenalty(pen);

	  // Update list of penalties
	  updatePenaltyList();

	  // Reset comment box
	  commentBox.setText("");

	  regatta.fireRegattaChange(new RegattaEvent(regatta,
						     RegattaEventType.FINISH,
						     TeamPenaltiesPane.this));
	}
      }), c2);

    //- Existing penalties
    p1.gridy++;
    p1.weighty = 1.0;
    this.penaltyPanel = new JScrollPane();
    penaltyPanel.
      setBorder(BorderFactory.createTitledBorder("Current penalties"));
    this.add(penaltyPanel, p1);

    updatePenaltyList();
  }

  /**
   * Publish each of the existing team penalties
   *
   */
  private void updatePenaltyList() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c1, c2, c3, c4;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c4 = new GridBagConstraints();
    c1.gridx = 0; c2.gridx = 1; c3.gridx = 2; c4.gridx = 3;
    c1.gridy = 0; c2.gridy = 0; c3.gridy = 0; c4.gridy = 0;
    Insets insts = new Insets(2,2,2,2);
    c1.insets = insts;
    c2.insets = insts;
    c3.insets = insts;
    c4.insets = insts;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c3.fill = GridBagConstraints.HORIZONTAL;
    c4.fill = GridBagConstraints.HORIZONTAL;
    c2.weightx = 1.0;

    TeamPenalty [] penalties = this.regatta.getTeamPenalties();
    if (penalties.length == 0) {
      c1.gridwidth = 4;
      c1.weightx = 1.0;
      panel.add(new JLabel("No penalties."), c1);
      c1.gridy++;
    }
    else {
      // Header
      panel.add(new JLabel("<html><em>Div.</em></html>"), c1);
      panel.add(new JLabel("<html><em>Team</em></html>"), c2);
      panel.add(new JLabel("<html><em>Type</em></html>"), c3);
      c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    }
    for (TeamPenalty pen : penalties) {
      panel.add(new JLabel(String.valueOf(pen.getDivision())), c1);
      panel.add(new JLabel(String.valueOf(pen.getTeam())),     c2);
      panel.add(new JLabel(String.valueOf(pen.getType())),     c3);
      panel.add(new JButton(new DropPenaltyAction(pen)),       c4);
      c1.gridy++; c2.gridy++; c3.gridy++; c4.gridy++;
    }

    // Spacer
    c1.weighty = 1.0;
    panel.add(new JLabel(""), c1);

    this.penaltyPanel.setViewportView(panel);
  }

  public boolean isUsable() {
    return (this.regatta.getTeams().length >= 2 &&
	    this.regatta.getDivisions().length > 0);
  }

  class DropPenaltyAction extends AbstractAction {
    private TeamPenalty penalty;

    public DropPenaltyAction(TeamPenalty pen) {
      super("Drop");
      this.penalty = pen;
    }
    
    public void actionPerformed(ActionEvent evt) {
      TeamPenaltiesPane.this.regatta.removeTeamPenalty(penalty.getDivision(),
						       penalty.getTeam());
      updatePenaltyList();
    }
  }
  
}
