package edu.mit.techscore.tscore;

import javax.swing.JFrame;

import edu.mit.techscore.dpxml.XMLTag;
import edu.mit.techscore.dpxml.XMLTextTag;
import edu.mit.techscore.regatta.Finish;
import edu.mit.techscore.regatta.Race;
import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.Division;
import edu.mit.techscore.regatta.RegattaEvent;
import edu.mit.techscore.regatta.RegattaEvent.RegattaEventType;
import edu.mit.techscore.regatta.Team;
import edu.mit.techscore.regatta.RP;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import edu.mit.techscore.regatta.Scorer;
import javax.swing.JTabbedPane;
import javax.swing.JEditorPane;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import java.util.HashMap;
import edu.mit.techscore.regatta.Sailor;
import edu.mit.techscore.regatta.RP.BoatRole;
import edu.mit.techscore.regatta.TeamPenalty;

/**
 * Displays the score for the regatta
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
 * Created: Thu Jul 16 17:49:54 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class ScoresDialog
  extends AbstractFrame {

  /**
   * Gets an HTML-compatible asterisk mark
   *
   * @param index an <code>int</code> value
   * @return a <code>String</code> value
   */
  private static String getAsterisk(int index) {
    switch (index) {
    case 0:
      return "*";

    default:
      return "<sup>" + (char)(index + 96) + "</sup>";
    }
  }

  private static XMLTag BLANK_TEXT = new XMLTag("html", new XMLTag[]{new XMLTextTag("No scores yet.")});

  private TScoreGUI parent;
  private JTabbedPane tabPane;
  private Map<Team, HashMap<Division, Integer>> teamDivisionTotalMap;
  
  /**
   * Creates a new <code>ScoresDialog</code> instance.
   *
   */
  public ScoresDialog(TScoreGUI parent, Regatta reg) {
    super(parent, "Scores");
    this.setRegatta(reg);
    this.parent = parent;
  }

  protected String getDialogContent() {
    JScrollPane sp = (JScrollPane)this.tabPane.getSelectedComponent();
    return ((JEditorPane)sp.getViewport().getView()).getText();
  }

  /**
   * Creates an HTML rendition of the totals table and updates a
   * number of the interior parameters to be used in rendering the
   * totals for the divisions. This method must be called prior to
   * getDivisionXML.
   *
   * @return a <code>String</code> value
   */
  private XMLTag getTotalsXML() {
    if (this.regatta.getFinishedRaces().length == 0) {
      return BLANK_TEXT;
    }

    XMLTag div, title, tab, row, cell;
    div = new XMLTag("div");

    // TOTALs
    this.parent.doScore();
    Scorer scorer = this.parent.getScorer();
    Team [] teams    = scorer.rank(this.regatta);

    Map<Team, String> explanations = scorer.getRankExplanations();
    List<String> priorExplanations = new ArrayList<String>();
    Division [] divs = this.regatta.getDivisions();
    int numRaces     = this.regatta.getNumRaces();

    // Map of team, division totals
    this.teamDivisionTotalMap = new HashMap<Team, HashMap<Division, Integer>>();
    
    title = new XMLTag("h2");
    title.add(new XMLTextTag("Total Scores"));
    tab = new XMLTag("table");
    div.add(title);
    div.add(tab);

    // Header
    tab.add(row  = new XMLTag("tr"));
    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag(""));
    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag(""));
    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("D."));
    for (int i = 0; i < numRaces; i++) {
      row.add(cell = new XMLTag("th"));
      cell.add(new XMLTextTag("" + (i+1)));
    }
    // Team penalty column
    XMLTag penaltyRow = new XMLTag("th");
    row.add(penaltyRow);
    penaltyRow.add(new XMLTextTag(""));
    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("TOT"));

    // Body
    HashMap<Division, Integer> divMap;
    for (int t = 0; t < teams.length; t++) {
      Team team = teams[t];
      int [] divTotals = new int[divs.length];
      int teamPenaltyTotal = 0;
      this.teamDivisionTotalMap.put(team,
				    divMap = new HashMap<Division, Integer>());

      // Explanation for ranking
      String exp = explanations.get(team);
      String symbol = "";
      if (!exp.equals("Natural order")) {
	int index = priorExplanations.indexOf(exp);
	if (index >= 0) {
	  symbol = ScoresDialog.getAsterisk(index);
	}
	else {
	  priorExplanations.add(exp);
	  symbol = ScoresDialog.getAsterisk(priorExplanations.size() - 1);
	}
      }

      // Array of race totals
      int [] raceTotals = new int[numRaces];
      for (int d = 0; d < divs.length; d++) {
	tab.add(row  = new XMLTag("tr"));
	switch (d) {
	case 0:
	  row.add(cell = new XMLTag("td"));
	  cell.addAttr("align", "right");
	  cell.add(new XMLTextTag(symbol + (t+1)));
	  row.add(cell = new XMLTag("td"));
	  cell.addAttr("align", "right");
	  cell.add(new XMLTextTag(team.getLongname()));
	  break;

	case 1:
	  row.add(cell = new XMLTag("td"));
	  cell.addAttr("align", "right");
	  cell.add(new XMLTextTag());
	  row.add(cell = new XMLTag("td"));
	  cell.addAttr("align", "right");
	  cell.add(new XMLTextTag(team.getShortname()));
	  break;

	default:
	  row.add(cell = new XMLTag("td"));
	  cell.add(new XMLTextTag());
	  row.add(cell = new XMLTag("td"));
	  cell.add(new XMLTextTag());
	}
	// Division
	row.add(cell = new XMLTag("th"));
	cell.add(new XMLTextTag(divs[d].toString()));
	// Races
	for (int r = 0; r < numRaces; r++) {
	  Race race = this.regatta.getRace(divs[d], (r+1));
	  Finish f  = this.regatta.getFinish(race, team);
	  String score;
	  score = (f == null) ? "" : String.valueOf(f);
	  int raceScore = (f == null) ? 0 : f.getScore();
	  divTotals[d] += raceScore;
	  raceTotals[r] += raceScore;
	  row.add(cell = new XMLTag("td"));
	  cell.addAttr("align", "right");
	  cell.addAttr("title", String.valueOf(raceScore));
	  cell.add(new XMLTextTag(score));
	}
	// Penalty cell
	row.add(cell = new XMLTag("td"));
	TeamPenalty pen = this.regatta.getTeamPenalty(divs[d], team);
	if (pen != null) {
	  teamPenaltyTotal += 20;
	  divTotals[d] += 20;
	  cell.add(new XMLTextTag(String.valueOf(pen.getType())));
	  cell.addAttr("title", pen.getComments());
	  cell.addAttr("align", "right");
	}
	else {
	  cell.add(new XMLTextTag(""));
	}
	// Total cell
	row.add(cell = new XMLTag("td"));
	cell.addAttr("align", "right");
	String divTotal =  (divTotals[d] == 0) ? "" : String.valueOf(divTotals[d]);
	cell.add(new XMLTextTag(divTotal));
	divMap.put(divs[d], new Integer(divTotals[d]));
      }
      // Totals row (and an extra row, too)
      XMLTag row2, cell2;
      tab.add(row = new XMLTag("tr"));
      tab.add(row2= new XMLTag("tr"));

      row.add(cell = new XMLTag("td"));
      row2.add(cell2 = new XMLTag("td"));

      cell.add(new XMLTextTag(""));
      cell2.add(new XMLTextTag(""));

      row.add(cell = new XMLTag("td"));
      row2.add(cell2 = new XMLTag("td"));
      
      cell.add(new XMLTextTag(""));
      cell2.add(new XMLTextTag(""));
      
      row.add(cell = new XMLTag("th"));
      row2.add(cell2 = new XMLTag("th"));
      
      cell.add(new XMLTextTag(""));
      cell2.add(new XMLTextTag(""));
      
      int runningTotal = 0;
      XMLTextTag value = null;
      for (int r = 0; r < numRaces; r++) {
	row.add(cell = new XMLTag("td"));
	row2.add(cell2 = new XMLTag("td"));
	
	cell.addAttr("align", "right");
	runningTotal += raceTotals[r];
	if (raceTotals[r] == 0) {
	  value = new XMLTextTag("");
	}
	else {
	  value = new XMLTextTag(String.valueOf(runningTotal));
	}
	cell.add(value);
	cell2.add(new XMLTextTag(""));
      }
      // Team penalty total
      row.add(cell = new XMLTag("td"));
      if (teamPenaltyTotal > 0) {
	cell.add(new XMLTextTag(String.valueOf(teamPenaltyTotal)));
	cell.addAttr("align", "right");
      }
      else {
	cell.add(new XMLTextTag(""));
      }
      // Net team total
      row.add(cell = new XMLTag("th"));
      row2.add(cell2 = new XMLTag("td"));
      cell.addAttr("align", "right");
      cell.add(new XMLTextTag(String.valueOf(runningTotal + teamPenaltyTotal)));
      cell2.add(new XMLTextTag(""));
    }

    // Legend, if necessary
    if (priorExplanations.size() > 0) {
      div.add(title = new XMLTag("h3"));
      title.add(new XMLTextTag("Tiebreaking legend"));

      XMLTag ul;
      div.add(ul = new XMLTag("dl"));
      XMLTag dt, dd;
      for (int i = 0; i < priorExplanations.size(); i++) {
	ul.add(dt = new XMLTag("dt"));
	dt.add(new XMLTextTag(ScoresDialog.getAsterisk(i)));
	ul.add(dd = new XMLTag("dd"));
	dd.add(new XMLTextTag(priorExplanations.get(i)));
      }
    }

    return div;
  }

  private XMLTag getDivisionXML(Division div) {
    RP rp = this.regatta.getRP();
    
    if (this.regatta.getFinishedRaces(div).length == 0) {
      return BLANK_TEXT;
    }

    XMLTag body = new XMLTag("div");
    XMLTag title, tab, row, row2, cell;
    body.add(title = new XMLTag("h3"));
    title.add(new XMLTextTag("Division " + div));

    body.add(tab = new XMLTag("table"));
    tab.add(row = new XMLTag("tr"));

    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("#"));

    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("Team"));

    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("T"));

    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("Sailors"));

    row.add(cell = new XMLTag("th"));
    cell.add(new XMLTextTag("Races"));
    
    Scorer scorer = this.parent.getScorer();
    Team [] teams = scorer.rank(this.regatta, div);
    Map<Team, String> explanations = scorer.getRankExplanations();
    List<String> priorExplanations = new ArrayList<String>();

    for (int t = 0; t < teams.length; t++) {
      Team team = teams[t];
      Sailor [] skippers = new Sailor[1];
      Sailor [] crews    = new Sailor[1];
      XMLTag [] skipRows, crewRows;
      if (rp != null) {
	skippers = rp.getSailors(team,
				 div,
				 BoatRole.SKIPPER);
	crews = rp.getSailors(team,
			      div,
			      BoatRole.CREW);
      }
      if (skippers.length == 0) {
	skippers = new Sailor [1];
      }
      if (crews.length == 0) {
	crews = new Sailor [1];
      }
      skipRows = new XMLTag[skippers.length];
      crewRows = new XMLTag[crews.length];

      // Explanation for ranking
      String exp = explanations.get(team);
      String symbol = "";
      if (!exp.equals("Natural order")) {
	int index = priorExplanations.indexOf(exp);
	if (index >= 0) {
	  symbol = ScoresDialog.getAsterisk(index);
	}
	else {
	  priorExplanations.add(exp);
	  symbol = ScoresDialog.getAsterisk(priorExplanations.size() - 1);
	}
      }

      // Row 1...n:   Team Longname and Skipper(s)
      // Row n+1...N: Team Shortname and 1st Crew...
      XMLTag cell1, cell2, cell3, cell4, cell5;
      for (int r = 0; r < skipRows.length; r++) {
	tab.add(skipRows[r] = new XMLTag("tr"));

	// Rank
	skipRows[r].add(cell1 = new XMLTag("td"));
	skipRows[r].add(cell2 = new XMLTag("td"));
	skipRows[r].add(cell3 = new XMLTag("th"));
	skipRows[r].add(cell4 = new XMLTag("td"));
	skipRows[r].add(cell5 = new XMLTag("td"));
	cell1.addAttr("align", "right");
	cell2.addAttr("align", "right");
	cell3.addAttr("align", "right");
	cell4.addAttr("align", "right");
	cell5.addAttr("class", "races");
	if (r == 0) {
	  cell1.add(new XMLTextTag(symbol + (t+1)));
	  cell2.add(new XMLTextTag(team.getLongname()));
	  String total = this.teamDivisionTotalMap.get(team).get(div).toString();
	  cell3.add(new XMLTextTag(total));
	}
	else {
	  cell1.add(new XMLTextTag(""));
	  cell2.add(new XMLTextTag(""));
	  cell3.add(new XMLTextTag(""));
	}
	// Add sailor
	if (skippers[r] != null) {
	  cell4.add(new XMLTextTag(skippers[r].toString()));
	  Race [] races = rp.getRaces(team,
				      skippers[r],
				      BoatRole.SKIPPER);
	  Integer [] raceNums = new Integer[races.length];
	  for (int i = 0; i < races.length; i++) {
	    raceNums[i] = new Integer(races[i].getNumber());
	  }
	  if (skipRows.length > 1) {
	    cell5.add(new XMLTextTag(Factory.formatList(raceNums)));
	  }
	  else {
	    cell5.add(new XMLTextTag(""));
	  }

	}
	else {
	  cell4.add(new XMLTextTag(""));
	  cell5.add(new XMLTextTag(""));
	}
      }
      for (int r = 0; r < crewRows.length; r++) {
	tab.add(crewRows[r] = new XMLTag("tr"));

	// Rank
	crewRows[r].add(cell1 = new XMLTag("td"));
	crewRows[r].add(cell2 = new XMLTag("td"));
	crewRows[r].add(cell3 = new XMLTag("th"));
	crewRows[r].add(cell4 = new XMLTag("td"));
	crewRows[r].add(cell5 = new XMLTag("td"));
	cell1.addAttr("align", "right");
	cell2.addAttr("align", "right");
	cell3.addAttr("align", "right");
	cell4.addAttr("align", "right");
	cell5.addAttr("class", "races");
	if (r == 0) {
	  cell1.add(new XMLTextTag(""));
	  cell2.add(new XMLTextTag(team.getShortname()));
	  cell3.add(new XMLTextTag(""));
	}
	else {
	  cell1.add(new XMLTextTag(""));
	  cell2.add(new XMLTextTag(""));
	  cell3.add(new XMLTextTag(""));
	}
	// Add sailor
	if (crews[r] != null) {
	  cell4.add(new XMLTextTag(crews[r].toString()));
	  Race [] races = rp.getRaces(team,
				      crews[r],
				      BoatRole.CREW);
	  Integer [] raceNums = new Integer[races.length];
	  for (int i = 0; i < races.length; i++) {
	    raceNums[i] = new Integer(races[i].getNumber());
	  }
	  if (crewRows.length > 1) {
	    cell5.add(new XMLTextTag(Factory.formatList(raceNums)));
	  }
	  else {
	    cell5.add(new XMLTextTag(""));
	  }

	}
	else {
	  cell4.add(new XMLTextTag(""));
	  cell5.add(new XMLTextTag(""));
	}
      }
    }

    // Legend, if necessary
    if (priorExplanations.size() > 0) {
      body.add(title = new XMLTag("h3"));
      title.add(new XMLTextTag("Tiebreaking legend"));

      XMLTag ul;
      body.add(ul = new XMLTag("dl"));
      XMLTag dt, dd;
      for (int i = 0; i < priorExplanations.size(); i++) {
	ul.add(dt = new XMLTag("dt"));
	dt.add(new XMLTextTag(ScoresDialog.getAsterisk(i)));
	ul.add(dd = new XMLTag("dd"));
	dd.add(new XMLTextTag(priorExplanations.get(i)));
      }
    }

    return body;
  }

  protected Component getContentComponent() {
    if (this.tabPane == null) {
      tabPane = new JTabbedPane();;
    }
    return this.tabPane;
  }

  /**
   * Sets up the content of the pane
   *
   */
  protected void fill() {
    this.tabPane.removeAll();
    if (this.regatta == null) {
      return;
    }

    JScrollPane scrollPane;
    JEditorPane editorPane;

    Division [] divs = regatta.getDivisions();
    XMLTag [] roots = new XMLTag[divs.length + 1];

    // Overall
    roots[0] = (this.isVisible()) ? getTotalsXML() : BLANK_TEXT;
    for (int i = 0; i < divs.length; i++) {
      roots[i + 1] = (this.isVisible()) ?
	getDivisionXML(divs[i]) :
	BLANK_TEXT;
    }

    // Totals
    editorPane = new JEditorPane("text/html", wrapHTML(roots[0]).toXMLString());
    editorPane.setEditable(false);
    tabPane.addTab("Totals", new JScrollPane(editorPane));

    // Divisions
    for (int i = 0; i < divs.length; i++) {
      editorPane = new JEditorPane("text/html", wrapHTML(roots[i + 1]).toXMLString());
      editorPane.setEditable(false);
      tabPane.addTab("Division " + divs[i], new JScrollPane(editorPane));

      for (XMLTag sub : roots[i + 1].getChildren()) {
	roots[0].add(sub);
      }
    }

    // Overall
    editorPane = new JEditorPane("text/html", wrapHTML(roots[0]).toXMLString());
    editorPane.setEditable(false);
    tabPane.addTab("All in one", new JScrollPane(editorPane));
  }

  protected void update() {
    if (this.regatta == null) {
      return;
    }

    List<XMLTag> roots = new ArrayList<XMLTag>(this.tabPane.getTabCount());
    roots.add(this.getTotalsXML());

    // Go through each tab and update its content
    // 1. the totals pane
    JEditorPane editorPane;
    JScrollPane scrollPane;
    scrollPane = (JScrollPane)this.tabPane.getComponentAt(0);
    editorPane = (JEditorPane)scrollPane.getViewport().getView();
    editorPane.setText(wrapHTML(roots.get(0)).toXMLString());

    // 2. divisions
    this.updateDivisions(roots);
  }

  protected void updateDivisions(List<XMLTag> roots) {
    JEditorPane editorPane;
    JScrollPane scrollPane;

    int num = this.tabPane.getTabCount() - 2;
    Division [] divs = Division.values();
    for (int i = 0; i < num; i++) {
      XMLTag other = this.getDivisionXML(divs[i]);

      scrollPane = (JScrollPane)this.tabPane.getComponentAt(i+1);
      editorPane = (JEditorPane)scrollPane.getViewport().getView();
      editorPane.setText(wrapHTML(other).toXMLString());

      roots.add(other);
    }

    // overall
    scrollPane = (JScrollPane)this.tabPane.getComponentAt(num + 1);
    editorPane = (JEditorPane)scrollPane.getViewport().getView();
    editorPane.setText(wrapHTML(roots).toXMLString());
  }

  // Implementation of regatta listener
  public void regattaChange(RegattaEvent evt) {
    if (this.isVisible()) {
      RegattaEventType type = evt.getChangeType();
      if (type == RegattaEventType.RACE) {
	this.fill();
      }
      else if (type == RegattaEventType.FINISH ||
	       type == RegattaEventType.TEAM) {
	this.update();
      }
      else if (type == RegattaEventType.RP) {
	this.update();
      }
      else if (type == RegattaEventType.SCORING) {
	this.update();
      }

    }
  }

  /**
   * Wraps the given element in the correct HTML element with tags and
   * all that jazz
   *
   * @param elems the elements to wrap
   * @return the HTML root
   */
  private XMLTag wrapHTML(List<XMLTag> elems) {
    XMLTag root = new XMLTag("html");

    XMLTag head;
    root.add(head = new XMLTag("head"));

    // Stylesheet
    XMLTag stylesheet = new XMLTag("style");
    stylesheet.addAttr("type", "text/css");
    stylesheet.add(new XMLTextTag(this.styleString));
    head.add(stylesheet);

    root.add(new XMLTag("body", elems.toArray(new XMLTag[]{})));
    return root;
  }
  private XMLTag wrapHTML(XMLTag elem) {
    List<XMLTag> list = new ArrayList<XMLTag>(1);
    list.add(elem);
    return this.wrapHTML(list);
  }
}
