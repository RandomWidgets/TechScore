package edu.mit.techscore.tscore;

import javax.swing.JFrame;

import edu.mit.techscore.dpxml.XMLTag;
import edu.mit.techscore.dpxml.XMLTextTag;
import edu.mit.techscore.regatta.Race;
import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.Division;
import edu.mit.techscore.regatta.RegattaEvent;
import edu.mit.techscore.regatta.Rotation;
import edu.mit.techscore.regatta.Team;
import edu.mit.techscore.regatta.RegattaEvent.RegattaEventType;
import javax.swing.JEditorPane;
import java.awt.Component;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;

/**
 * View/print rotations
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
 * Created: Thu Jun 25 11:03:13 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RotationsDialog extends AbstractFrame {

  private static String BLANK_TEXT = "No rotations.";
  private JScrollPane scrollpane;
  private JEditorPane content;

  /**
   * Creates a new <code>RotationsDialog</code> instance.
   *
   */
  public RotationsDialog(JFrame parent, Regatta reg) {
    super(parent, "Rotations");
    this.setRegatta(reg);
  }

  protected Component getContentComponent() {
    if (this.scrollpane == null) {
      this.content = new JEditorPane("text/html", "");;
      this.content.setEditable(false);
      this.scrollpane = new JScrollPane(content);
    }
    return this.scrollpane;
  }

  /**
   * Draws the rotation for the regatta
   */
  protected void fill() {
    if (this.regatta == null) {
      this.content.setOpaque(false);
      this.content.setText("");
      return;
    }
    this.content.setOpaque(true);
    this.update();
  }

  protected void update() {
    if (this.regatta == null) {
      this.content.setText("");
      this.printAction.setEnabled(false);
      this.saveAction.setEnabled(false);
      return;
    }


    Rotation rot = this.regatta.getRotation();
    if (rot == null) {
      this.content.setText(BLANK_TEXT);
    }
    else {
      this.content.setText(getDialogContent());
    }
    this.printAction.setEnabled(rot != null);
    this.saveAction.setEnabled(rot != null);
  }

  protected String getDialogContent() {
    Rotation rot = this.regatta.getRotation();
    XMLTag root = new XMLTag("html");
    XMLTag head, body;
    root.add(head = new XMLTag("head"));

    // Stylesheet
    XMLTag stylesheet = new XMLTag("style");
    stylesheet.addAttr("type", "text/css");
    stylesheet.add(new XMLTextTag(this.styleString));
    head.add(stylesheet);

    root.add(body = new XMLTag("body"));
    XMLTag title, tab, row, cell;

    // For each division
    Team [] teams    = this.regatta.getTeams();
    Division [] divs = this.regatta.getDivisions();
    for (int d = 0; d < divs.length; d++) {
      title = new XMLTag("h2");
      title.add(new XMLTextTag("Division " + divs[d]));
      tab  = new XMLTag("table");
      body.add(title);
      body.add(tab);
      
      Race [] races = this.regatta.getRaces(divs[d]);
      // Header
      row = new XMLTag("tr");
      cell= new XMLTag("th");
      cell.add(new XMLTextTag(""));
      row.add(cell);
      for (int r = 0; r < races.length; r++) {
	cell = new XMLTag("th");
	cell.add(new XMLTextTag(races[r].toString()));
	cell.addAttr("align", "right");
	row.add(cell);
      }
      tab.add(row);

      // For each team
      for (int t = 0; t < teams.length; t++) {
	row = new XMLTag("tr");
	cell= new XMLTag("th");
	cell.add(new XMLTextTag(teams[t].toString()));
	cell.addAttr("align", "right");
	row.add(cell);

	// For each race
	for (int r = 0; r < races.length; r++) {
	  cell = new XMLTag("td");
	  Object sail = rot.getSail(races[r], teams[t]);
	  if (sail == null) {
	    sail = "";
	  }
	  cell.add(new XMLTextTag(sail.toString()));
	  cell.addAttr("align", "right");
	  row.add(cell);
	}
	tab.add(row);
      }
    }
    return root.toXMLString();
  }

  // Implementation of regatta change
  public void regattaChange(RegattaEvent e) {
    RegattaEventType type = e.getChangeType();
    if (this.isVisible()) {
      if (RegattaEventType.ROTATION == type ||
	  RegattaEventType.TEAM     == type ||
	  RegattaEventType.RACE     == type) {
	this.update();
      }
    }
    else if (type == RegattaEventType.ROTATION) {
      this.update();
      this.setVisible(true);
    }

  }

}
