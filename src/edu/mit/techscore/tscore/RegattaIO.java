/*
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
 */

package edu.mit.techscore.tscore;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import edu.mit.techscore.dpxml.XMLTag;
import edu.mit.techscore.dpxml.XMLTextTag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import edu.mit.techscore.regatta.Breakdown;
import edu.mit.techscore.regatta.Breakdown.BreakdownType;
import edu.mit.techscore.regatta.Finish;
import edu.mit.techscore.regatta.FinishAdjustment;
import edu.mit.techscore.regatta.Penalty;
import edu.mit.techscore.regatta.Penalty.PenaltyType;
import edu.mit.techscore.regatta.RP;
import edu.mit.techscore.regatta.RP.BoatRole;
import edu.mit.techscore.regatta.Race;
import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.Division;
import edu.mit.techscore.regatta.Regatta.RegattaType;
import edu.mit.techscore.regatta.Rotation;
import edu.mit.techscore.regatta.Rotation.RotationStyle;
import edu.mit.techscore.regatta.Rotation.RotationType;
import edu.mit.techscore.regatta.Sail;
import edu.mit.techscore.regatta.Sailor;
import edu.mit.techscore.regatta.Team;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import edu.mit.techscore.regatta.TeamPenalty;
import edu.mit.techscore.regatta.TeamPenalty.TeamPenaltyType;
import edu.mit.techscore.regatta.Regatta.RegattaScoring;

/**
 * <p>Manages a regatta connection to a file. When the regatta is
 * loaded, the information describing its teams (id), etc, are saved,
 * so that when the time comes to save the file back, all the original
 * information is kept intact. This helps keep data from being lost.
 * </p>
 *
 * <p>RegattaIO objects can inform registered propertyChangeListeners
 * of the progress being made.</p>
 *
 * <h2>Temporary RP database</h2>
 *
 * <p>When a regatta is loaded, a database for the RP information is
 * created in a directory specified by the client user. This database
 * consists of tab-delimited files, one for each different affiliation
 * found in the regatta. The format is:</p>
 *
 * <code>ID	Name	Year	New?</code>
 *
 * <p>Where the ID is as found in the regatta file, or created by the
 * client program. For speed, the flag <code>New</code> should be set
 * to <code>true</code>/<code>false</code> if the former or latter,
 * respectively. This speeds up the process of committing changes to
 * the database back to file when using the {@link rewriteFile}
 * method.</p>
 *
 * Created: Fri Jun 26 21:19:45 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class RegattaIO {

  /**
   * Describe regatta here.
   */
  private Regatta regatta;

  // Three-way map
  private List<Team> teamList;
  private List<String> idList;
  private List<Element> elementList;

  private Set<String> errors, warnings;

  // Listeners
  private List<PropertyChangeListener> listeners;

  /**
   * Creates a new <code>RegattaIO</code> instance.
   *
   */
  public RegattaIO() {
    this(null);
  }

  /**
   * Creates a new <code>RegattaIO</code> instance with the specified
   * regatta object.
   *
   * @param reg a <code>Regatta</code> value
   */
  public RegattaIO (Regatta reg) {
    this.setRegatta(reg);
    this.listeners = new ArrayList<PropertyChangeListener>();
  }

  /**
   * Get the <code>Regatta</code> value.
   *
   * @return a <code>Regatta</code> value
   */
  public final Regatta getRegatta() {
    return regatta;
  }

  /**
   * Set the <code>Regatta</code> value.
   *
   * @param newRegatta The new Regatta value.
   */
  public final void setRegatta(final Regatta newRegatta) {
    this.regatta = newRegatta;
  }

  /**
   * Writes a regatta to file by using the structure of inFile, and
   * saving to file outFile (which can be the same).
   * <p>
   * This function will attempt to update the format the file is saved
   * to match the current version.
   *
   * @param reg a <code>Regatta</code> value
   * @param inFile a <code>File</code> value
   * @param outFile a <code>File</code> value
   * @return a <code>boolean</code> value determining success
   */
  public final boolean rewriteFile(Regatta reg,
				   File inFile,
				   File outFile,
				   File databaseDir) {
    this.setRegatta(reg);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(inFile);
      Element rootElement = doc.getDocumentElement();
      rootElement.normalize();

      Element tag, subtag;
      Element text;

      // Version
      rootElement.setAttribute("version", TScoreGUI.VERSION);

      // REGATTA DETAILS
      // Name
      String textString = this.regatta.getName();
      tag = getOrCreateElement(doc, rootElement, "RegattaName");
      this.replaceElementContent(tag, doc.createTextNode(textString));

      // Start time
      DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.LONG,
						      DateFormat.LONG);
      textString = dtf.format(this.regatta.getStartTime());
      tag = getOrCreateElement(doc, rootElement, "StartTime");
      this.replaceElementContent(tag, doc.createTextNode(textString));

      // Duration
      textString = String.valueOf(this.regatta.getDuration());
      tag = getOrCreateElement(doc, rootElement, "Duration");
      this.replaceElementContent(tag, doc.createTextNode(textString));

      // Type
      textString = String.valueOf(this.regatta.getType());
      tag = getOrCreateElement(doc, rootElement, "RegattaType");
      this.replaceElementContent(tag, doc.createTextNode(textString));

      // Scoring
      textString = String.valueOf(this.regatta.getScoring());
      tag = getOrCreateElement(doc, rootElement, "RegattaScoring");
      this.replaceElementContent(tag, doc.createTextNode(textString));

      // Blurb:
      // Replace version < 1.4 "Blurb" with "Comments" tree. This
      // replaces the content of the "Comments" tag if it exists
      tag = getOrCreateElement(doc, rootElement, "Comments");
      while (tag.hasChildNodes())
	tag.removeChild(tag.getFirstChild());
      int day = 1;
      Map<Date, String> blurbs = regatta.getBlurbs();
      for (Date d : blurbs.keySet()) {
	subtag = doc.createElement("Comment");
	subtag.setAttribute("day", String.valueOf(day));
	subtag.appendChild(doc.createTextNode(blurbs.get(d)));
	tag.appendChild(subtag);
	day++;
      }
      // Remove the "Blurb" element, if it exists
      tag = getElement(rootElement, "Blurb");
      if (tag != null)
	rootElement.removeChild(tag);

      // Races and divisions
      rootElement.setAttribute("races", String.valueOf(this.regatta.getNumRaces()));
      rootElement.setAttribute("divisions", String.valueOf(this.regatta.getNumDivisions()));
      
      // Teams
      tag = getOrCreateElement(doc, rootElement, "Teams");
      this.updateTeamMaps(tag);
      final List<Team> teams = Arrays.asList(regatta.getTeams());
      //  Remove any team elements from teams that no longer exist
      for (int i = 0; i < this.teamList.size(); i++) {
	if (!teams.contains(this.teamList.get(i))) {
	  this.idList.remove(i);
	  this.teamList.remove(i);
	  tag.removeChild(this.elementList.remove(i));
	  i--;
	}
      }
      //  Add/edit existing teams as necessary
      Random rng = new Random();
      for (Team team : teams) {
	// Find the team in the map, if it exists
	int index = this.teamList.indexOf(team);
	Element teamElem;
	if (index < 0) {
	  // Create the node. Pick an id not already in the map
	  teamElem = doc.createElement("Team");
	  String id = "t" + (100 + rng.nextInt(900));
	  while (this.idList.contains(id)) {
	    id = "t" + (100 + rng.nextInt(900));
	  }
	  this.idList.add(id);
	  this.teamList.add(team);
	  this.elementList.add(teamElem);
	  teamElem.setAttribute("id", id);
	  // Add node
	  tag.appendChild(teamElem);
	}
	else {
	  teamElem = this.elementList.get(index);
	  // Also, replace the team
	  this.teamList.set(index, team);
	}

	Element name;
	name = getOrCreateElement(doc, teamElem, "LongName");
	this.replaceElementContent(name, doc.createTextNode(team.getLongname()));
	name = getOrCreateElement(doc, teamElem, "ShortName");
	this.replaceElementContent(name, doc.createTextNode(team.getShortname()));
	teamElem.setAttribute("affiliate", team.getAffiliation());
      }

      // Rotations
      Rotation rot = this.regatta.getRotation();
      if (rot != null) {
	List<Node> sails = new ArrayList<Node>();
	Division [] divs = this.regatta.getDivisions();
	for (int d = 0; d < divs.length; d++) {
	  Race [] races = this.regatta.getRaces(divs[d]);
	  for (int r = 0; r < races.length; r++) {
	    for (int i = 0; i < this.idList.size(); i++) {
	      String id = this.idList.get(i);
	      Team team = this.teamList.get(i);
	      Sail sail = rot.getSail(races[r], team);
	      subtag = doc.createElement("Sail");
	      subtag.setAttribute("race", races[r].toString());
	      subtag.setAttribute("team", id);
	      subtag.setAttribute("sail", String.valueOf(sail));
	      sails.add(subtag);
	    }
	  }
	}
	tag = getOrCreateElement(doc, rootElement, "Rotations");
	this.replaceElementContent(tag, sails);
      }
      else {
	// Remove all existing rotations
	removeSubElements(rootElement, "Rotations");
      }

      // Finishes
      List<Node> penaltyNodes   = new ArrayList<Node>();
      List<Node> breakdownNodes = new ArrayList<Node>();
      List<Node> finishNodes    = new ArrayList<Node>();
      List<Node> teamPenaltyNodes = new ArrayList<Node>();
      DateFormat tf = DateFormat.getTimeInstance(DateFormat.LONG);
      Race [] races = this.regatta.getFinishedRaces();
      for (Race race : races) {
	for (Finish finish : this.regatta.getFinishes(race)) {
	  if (finish != null) {
	    Team team = finish.getTeam();
	    int i = this.teamList.indexOf(team);
	    String id = this.idList.get(i);

	    subtag = doc.createElement("Finish");
	    subtag.setAttribute("race", String.valueOf(finish.getRace()));
	    subtag.setAttribute("team", id);
	    subtag.appendChild(doc.createTextNode(tf.format(finish.getTimestamp())));
	    finishNodes.add(subtag);

	    // Deal with possible penalties/breakdowns
	    FinishAdjustment adjust;
	    if ((adjust = finish.getPenalty()) != null) {
	      // Add to penalty list
	      subtag = doc.createElement("Penalty");
	      subtag.setAttribute("race", String.valueOf(race));
	      subtag.setAttribute("team", id);
	      subtag.setAttribute("type", String.valueOf(adjust.getType()));
	      penaltyNodes.add(subtag);
	    }
	    else if ((adjust = finish.getBreakdown()) != null) {
	      // Add to breakdown list
	      subtag = doc.createElement("Breakdown");
	      subtag.setAttribute("race", String.valueOf(race));
	      subtag.setAttribute("team", id);
	      Enum type = adjust.getType();
	      subtag.setAttribute("type", String.valueOf(type));
	      if (type == BreakdownType.RDG) {
		subtag.setAttribute("amount",
				    String.valueOf(((Breakdown)adjust).getHandicap()));
	      }
	      breakdownNodes.add(subtag);
	    }
	  }
	}
      }
      // Team penalty?
      for (int i = 0; i < this.teamList.size(); i++) {
	Team team = this.teamList.get(i);
	String id = this.idList.get(i);
	TeamPenalty pen;
	for (Division d : this.regatta.getDivisions()) {
	  pen = this.regatta.getTeamPenalty(d, team);
	  if (pen != null) {
	    subtag = doc.createElement("TeamPenalty");
	    subtag.setAttribute("team", id);
	    subtag.setAttribute("division", String.valueOf(d));
	    subtag.setAttribute("type", String.valueOf(pen.getType()));
	    subtag.appendChild(doc.createTextNode(pen.getComments()));
	    teamPenaltyNodes.add(subtag);
	  }
	}
      }

      if (races.length > 0) {
	tag = getOrCreateElement(doc, rootElement, "Finishes");
	replaceElementContent(tag, finishNodes);
      }
      else {
	removeSubElements(rootElement, "Finishes");
      }


      // Penalties
      if (penaltyNodes.size() > 0) {
	tag = getOrCreateElement(doc, rootElement, "Penalties");
	replaceElementContent(tag, penaltyNodes);
      }
      else {
	removeSubElements(rootElement, "Penalties");
      }

      // Breakdowns
      if (breakdownNodes.size() > 0) {
	tag = getOrCreateElement(doc, rootElement, "Breakdowns");
	replaceElementContent(tag, breakdownNodes);
      }
      else {
	removeSubElements(rootElement, "Breakdowns");
      }

      // Team penalties
      if (teamPenaltyNodes.size() > 0) {
	tag = getOrCreateElement(doc, rootElement, "TeamPenalties");
	replaceElementContent(tag, teamPenaltyNodes);
      }
      else {
	removeSubElements(rootElement, "TeamPenalties");
      }

      // RP
      RP rp = this.regatta.getRP();
      if (rp != null) {
	List<Node> sailorNodes = new ArrayList<Node>();
	Calendar cal = Calendar.getInstance();
	Division [] divs = this.regatta.getDivisions();
	for (int i = 0; i < this.idList.size(); i++) {
	  String id = this.idList.get(i);
	  Team team = this.teamList.get(i);

	  for (Division div : divs) {
	    Element name, year;
	    Race [] subRaces;
	    
	    // For each role
	    for (BoatRole role : BoatRole.values()) {

	      Sailor [] sailors = rp.getSailors(team,
						div,
						role);
	      for (Sailor sailor : sailors) {
		subRaces = rp.getRaces(team, sailor, role);
		String raceString = Factory.formatList(subRaces);

		subtag = doc.createElement("Sailor");
		subtag.setAttribute("team", id);
		subtag.setAttribute("role", role.desc());
		subtag.setAttribute("division", String.valueOf(div));
		subtag.setAttribute("races", raceString);
		subtag.setAttribute("id", sailor.getID());
		sailorNodes.add(subtag);
	      }
	    }
	  }
	}
	tag = getOrCreateElement(doc, rootElement, "RP");
	replaceElementContent(tag, sailorNodes);
      }
      else {
	removeSubElements(rootElement, "RP");
      }

      // RP database
      tag = getOrCreateElement(doc, rootElement, "Membership");
      
      // 1. Go through each affiliate, and fill in new members
      Set<String> doneAffiliates = new HashSet<String>();
      NodeList nl = tag.getElementsByTagName("Affiliate");
      for (int n = 0; n < nl.getLength(); n++) {
	Element affElem = (Element)nl.item(n);
	String affID = affElem.getAttribute("id");
	if (affID.length() > 0) {
	  File affFile = new File(databaseDir, affID);
	  if (affFile.exists()) {
	    doneAffiliates.add(affID);
	    // Add new ones
	    Element newSailor, subelem;
	    try {
	      BufferedReader reader =
		new BufferedReader(new FileReader(affFile));
	      String line;
	      while ((line = reader.readLine()) != null) {
		String [] fields = line.split("\t");
		try {
		  boolean isNew = Boolean.parseBoolean(fields[3]);
		  if (isNew) {
		    newSailor = doc.createElement("Member");
		    affElem.appendChild(newSailor);
		    newSailor.setAttribute("id", fields[0]);
		    newSailor.setAttribute("data", "TechScore v" + TScoreGUI.VERSION);
		    // Name
		    subelem = doc.createElement("Name");
		    newSailor.appendChild(subelem);
		    subelem.appendChild(doc.createTextNode(fields[1]));
		    // Year
		    subelem = doc.createElement("Year");
		    newSailor.appendChild(subelem);
		    subelem.appendChild(doc.createTextNode(fields[2]));
		  }
		} catch (Exception e) {
		  System.err.println("Error in format for sailor, ignoring: " + line);
		  e.printStackTrace();
		}
	      }
	    } catch (IOException e) {
	      System.err.println("Unable to read database file: " + affFile);
	    }
	  }
	}
      }
      
      // 2. Go through each new affiliate
      try {
	for (File affFile : databaseDir.listFiles()) {
	  if (!doneAffiliates.contains(affFile.getName())) {
	    // Create the member
	    Element affElem = doc.createElement("Affiliate");
	    tag.appendChild(affElem);
	    affElem.setAttribute("id", affFile.getName());
	    Element newSailor, subelem;
	    try {
	      BufferedReader reader =
		new BufferedReader(new FileReader(affFile));
	      String line;
	      while ((line = reader.readLine()) != null) {
		String [] fields = line.split("\t");
		try {
		  newSailor = doc.createElement("Member");
		  affElem.appendChild(newSailor);
		  newSailor.setAttribute("id", fields[0]);
		  newSailor.setAttribute("data", "TechScore v" + TScoreGUI.VERSION);
		  // Name
		  subelem = doc.createElement("Name");
		  newSailor.appendChild(subelem);
		  subelem.appendChild(doc.createTextNode(fields[1]));
		  // Year
		  subelem = doc.createElement("Year");
		  newSailor.appendChild(subelem);
		  subelem.appendChild(doc.createTextNode(fields[2]));
		} catch (Exception e) {
		  System.err.println("Error in format for sailor, ignoring: " + line);
		}
	      }
	    } catch (IOException e) {
	      System.err.println("Unable to read database file: " + affFile);
	    }
	  }
	}
      } catch (SecurityException e) {
	System.err.println("Reading files not allowed.");
      }

      // Save to new file
      TransformerFactory xformFactory = TransformerFactory.newInstance();  
      Transformer idTransform = xformFactory.newTransformer();
      Source input = new DOMSource(doc);
      Result output = new StreamResult(outFile);
      idTransform.transform(input, output);

    } catch (SAXException e) {
      errors.add("Error interpreting file: " + e.getMessage());
    } catch (IOException e) {
      errors.add("Could not open file: " + e.getMessage());
    } catch (ParserConfigurationException e) {
      errors.add("Error with parser: " + e.getMessage());
    } catch (TransformerConfigurationException e) {
      errors.add(e.getMessage());
    } catch (TransformerException e) {
      errors.add(e.getMessage());
    }

    return true;
  }

  /**
   * Convenience method for actual file writer which ignores the RP
   * database directory (sends <code>null</code>)
   *
   * @param reg a <code>Regatta</code> value
   * @param file a <code>File</code> value
   * @return a <code>boolean</code> value
   */
  public boolean writeFile(Regatta reg, File file) {
    return this.writeFile(reg, file, null);
  }
  
  /**
   * Writes the regatta and the RP database to a new file.
   *
   * @param reg a <code>Regatta</code> to save to file
   * @param file a <code>File</code> to save in
   * @param databaseDir the directory where the RP information is
   * saved.
   * @return <code>true</code> upon success.
   */
  public boolean writeFile(Regatta reg, File file, File databaseDir) {
    this.setRegatta(reg);
    try {

      // Create XML structure
      XMLTag subtag;
      XMLTag root = new XMLTag("Regatta");
      root.addAttr("xmlns", "http://techscore.mit.edu");
      XMLTag tag = new XMLTag("RegattaName");
      root.addAttr("version", TScoreGUI.VERSION);
      root.addAttr("divisions", "" + this.regatta.getNumDivisions());
      root.addAttr("races",     "" + this.regatta.getNumRaces());
      root.add(tag);
      tag.add(new XMLTextTag(this.regatta.getName()));

      Calendar cal = Calendar.getInstance();
      DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.LONG,
						      DateFormat.LONG);
      cal.setTime(this.regatta.getStartTime());
      XMLTag dateTag = new XMLTag("StartTime");
      dateTag.add(new XMLTextTag(dtf.format(this.regatta.getStartTime())));
      root.add(dateTag);

      tag = new XMLTag("Duration");
      tag.add(new XMLTextTag(Integer.toString(this.regatta.getDuration())));
      tag.addAttr("type", "days");
      root.add(tag);

      tag = new XMLTag("RegattaType");
      tag.add(new XMLTextTag(this.regatta.getType().toString()));
      tag.addAttr("class", "ICSA");
      root.add(tag);

      tag = new XMLTag("RegattaScoring");
      tag.add(new XMLTextTag(this.regatta.getScoring().toString()));
      tag.addAttr("class", "ICSA");
      root.add(tag);

      // Starting with version 1.4, Blurb --> Comments
      tag = new XMLTag("Comments");
      root.add(tag);
      Map<Date, String> blurbs = this.regatta.getBlurbs();
      int day = 1;
      for (Date d : blurbs.keySet()) {
	subtag = new XMLTag("Comment");
	subtag.addAttr("day", String.valueOf(day));
	subtag.add(new XMLTextTag(blurbs.get(d)));
	day++;
      }

      // Teams
      root.add(tag = new XMLTag("Teams"));
      XMLTag teamTag;
      Team [] teams = this.regatta.getTeams();
      for (int i = 0; i < teams.length; i++) {
	teamTag = new XMLTag("Team");
	teamTag.addAttr("id", "t" + (i+1));
	teamTag.addAttr("affiliate", teams[i].getAffiliation());
	XMLTag lName, sName;
	lName = new XMLTag("LongName");
	sName = new XMLTag("ShortName");
	lName.add(new XMLTextTag(teams[i].getLongname()));
	sName.add(new XMLTextTag(teams[i].getShortname()));
	teamTag.add(lName);
	teamTag.add(sName);
	tag.add(teamTag);
      }

      // Rotations
      Rotation rot = this.regatta.getRotation();
      if (rot != null) {
	tag = new XMLTag("Rotations");
	root.add(tag);

	Division [] divs = this.regatta.getDivisions();
	for (int d = 0; d < divs.length; d++) {
	  Race [] races = this.regatta.getRaces(divs[d]);
	  for (int r = 0; r < races.length; r++) {
	    for (int t = 0; t < teams.length; t++) {
	      Object sail = rot.getSail(races[r], teams[t]);
	      subtag = new XMLTag("Sail");
	      subtag.addAttr("race", races[r].toString());
	      subtag.addAttr("team", "t" + (t+1));
	      subtag.addAttr("sail", sail.toString());
	      tag.add(subtag);
	    }
	  }
	}
      }

      // Finishes
      List<XMLTag> penaltyList = new ArrayList<XMLTag>();
      List<XMLTag> breakdownList = new ArrayList<XMLTag>();
      List<XMLTag> teamPenaltyList = new ArrayList<XMLTag>();
      DateFormat tf = DateFormat.getTimeInstance(DateFormat.LONG);
      Race [] races = this.regatta.getFinishedRaces();
      if (races.length > 0) {
	root.add(tag = new XMLTag("Finishes"));
      }
      int teamIndex = 0;
      for (Team team: teams) {
	for (Race race: races) {
	  Finish finish = this.regatta.getFinish(race, team);
	  if (finish != null) {
	    tag.add(subtag = new XMLTag("Finish"));
	    subtag.addAttr("race", race.toString());
	    subtag.addAttr("team", "t" + (teamIndex+1));
	    subtag.add(new XMLTextTag(tf.format(finish.getTimestamp())));

	    FinishAdjustment adjust;
	    if ((adjust = finish.getPenalty()) != null) {
	      // Add to penaltyList
	      XMLTag penTag = new XMLTag("Penalty");
	      penTag.addAttr("race", race.toString());
	      penTag.addAttr("team", "t" + (teamIndex+1));
	      penTag.addAttr("type", adjust.getType().toString());
	      penaltyList.add(penTag);
	    }
	    else if ((adjust = finish.getBreakdown()) != null) {
	      // Add to breakdown list
	      XMLTag penTag = new XMLTag("Breakdown");
	      penTag.addAttr("race", race.toString());
	      penTag.addAttr("team", "t" + (teamIndex+1));
	      Enum type = adjust.getType();
	      penTag.addAttr("type", type.toString());
	      if (type == BreakdownType.RDG) {
		penTag.addAttr("amount",
			       "" + ((Breakdown)adjust).getHandicap());
	      }
	      breakdownList.add(penTag);
	    }
	  }
	}
	// Team penalty?
	TeamPenalty pen;
	for (Division d : this.regatta.getDivisions()) {
	  pen = this.regatta.getTeamPenalty(d, team);
	  if (pen != null) {
	    subtag = new XMLTag("TeamPenalty");
	    subtag.addAttr("team", "t" + (teamIndex+1));
	    subtag.addAttr("division", String.valueOf(d));
	    subtag.addAttr("type", String.valueOf(pen.getType()));
	    subtag.add(new XMLTextTag(pen.getComments()));
	    teamPenaltyList.add(subtag);
	  }
	}
	teamIndex++;
      }

      // Penalties
      if (penaltyList.size() > 0) {
	root.add(tag = new XMLTag("Penalties"));
	for (XMLTag t: penaltyList) {
	  tag.add(t);
	}
      }

      // Breakdowns
      if (breakdownList.size() > 0) {
	root.add(tag = new XMLTag("Breakdowns"));
	for (XMLTag t: breakdownList) {
	  tag.add(t);
	}
      }

      // Team penalties
      TeamPenalty [] penalties = this.regatta.getTeamPenalties();
      if (penalties.length > 0) {
	tag = new XMLTag("TeamPenalties");
	for (XMLTag t: teamPenaltyList) {
	  tag.add(t);
	}
      }

      // RP
      RP rp = this.regatta.getRP();
      if (rp != null) {
	root.add(tag = new XMLTag("RP"));
	Division [] divs = this.regatta.getDivisions();
	for (int t = 0; t < teams.length; t++) {
	  Team team = teams[t];
	  for (Division div : divs) {
	    XMLTag name, year;
	    Race [] subRaces;
	    Integer [] nums;
	    for (BoatRole role : BoatRole.values()) {
	      Sailor [] sailors = rp.getSailors(team,
						div,
						role);
	      for (Sailor sailor : sailors) {
		subRaces = rp.getRaces(team, sailor, role);
		nums = new Integer[subRaces.length];
		for (int i = 0; i < subRaces.length; i++) {
		  nums[i] = new Integer(subRaces[i].getNumber());
		}

		String raceString = Factory.formatList(nums);
		tag.add(subtag = new XMLTag("Sailor"));
		subtag.addAttr("team", "t" + (t+1));
		subtag.addAttr("role", role.desc());
		subtag.addAttr("division", div.toString());
		subtag.addAttr("races", raceString);
		subtag.addAttr("id", sailor.getID());
	      }
	    }
	  }
	}
      }

      // RP Database
      root.add(tag = new XMLTag("Membership"));
      XMLTag memtag, name, year;
      try {
	for (File affFile : databaseDir.listFiles()) {
	  tag.add(subtag = new XMLTag("Affiliate"));
	  subtag.addAttr("id", affFile.getName());
	  try {
	    BufferedReader reader =
	      new BufferedReader(new FileReader(affFile));
	    String line;
	    while ((line = reader.readLine()) != null) {
	      String [] fields = line.split("\t");
	      try {
		boolean isNew = Boolean.parseBoolean(fields[3]);
		if (isNew) {
		  subtag.add(memtag = new XMLTag("Member"));
		  memtag.addAttr("id", fields[0]);
		  memtag.addAttr("data", "TechScore v" + TScoreGUI.VERSION);
		  memtag.add(name = new XMLTag("Name"));
		  memtag.add(year = new XMLTag("Year"));
		  name.add(new XMLTextTag(fields[1]));
		  year.add(new XMLTextTag(fields[2]));
		}
	      } catch (Exception e) {
		System.err.println("Error in format for sailor, ignoring: " + line);
	      }
	    }

	  } catch (IOException e) {
	    System.err.println("Unable to read database file: " + affFile);

	  }
	}
      } catch (SecurityException e) {
	System.err.print("Error while reading membership database: ");
	e.printStackTrace(System.err);
      } catch (NullPointerException e) {
	// There is no membership database file. Ignore.
      }

      // Actually write the file
      FileWriter fstream = new FileWriter(file);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write(root.toXMLString());
      out.close();

      return true;      
    } catch (IOException e) {
      System.err.println("Could not print to file.");
    }
    return false;
  }

  private Element getElement(Element root, String tag) {
    NodeList nl = root.getElementsByTagName(tag);
    int len = nl.getLength();
    if (len == 0) {
      return null;
    }
    return (Element)nl.item(len - 1);
  }

  // Reading from file
  
  /**
   * Reads a file and creates a regatta object. If the file was read
   * without any problems, returns <code>true</code>. If there were
   * warnings, returns <code>false</code>. If there are unrecoverable
   * errors, an exception is thrown.
   * <p>
   * Goes out of its way to understand previous versions of the file
   * format.
   *
   * @param  f a <code>File</code> to read
   * @return <code>true</code> on success.
   * @see    #getRegatta
   */
  public boolean readFile(File f, File databaseDir)
    throws IllegalArgumentException {
    
    errors   = new LinkedHashSet<String>();
    warnings = new LinkedHashSet<String>();
    regatta = null;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    NodeList nl, snl;
    try {
      this.notifyListeners("done", new Boolean(false));

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(f);
      Element root = doc.getDocumentElement();
      root.normalize();

      // Version
      String version;
      if (root.hasAttribute("version"))
	version = root.getAttribute("version");
      else {
	version = TScoreGUI.VERSION;
	warnings.add("No version detected, assuming current.");
      }

      // REGATTA DETAILS
      final String prop = "Loading file";
      //      this.notifyListeners(prop, "Reading details");
      String name = getTagContent(root, "RegattaName");
      if (name == null) {
	warnings.add("No name found for regatta.");
	name = "Untitled";
      }
      // Date
      Calendar cal = Calendar.getInstance();   // version <= 0.7
      Date dateValue = null;                   // version >  0.7
      DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.LONG,
						      DateFormat.LONG);
      try {
	dateValue = dtf.parse(getTagContent(root, "StartTime"));
      } catch (ParseException e) {
	warnings.add("Unable to interpret starting date, using now.");
	dateValue = new Date();
      } catch (NullPointerException e) {
	warnings.add("Missing starting date, using now.");
	dateValue = new Date();
      }

      // Duration
      int duration = 1;
      try {
	duration = Integer.parseInt(getTagContent(root, "Duration"));
      } catch (NumberFormatException e) {
	warnings.add("Illegal or missing value for duration. Using default: 1.");
	duration = 1;
      } catch (NullPointerException e) {
	warnings.add("Missing duration. Using 1.");
      }
      if (duration < 0) {
	warnings.add("Found invalud value for duration. Using default: 1.");
	duration = 1;
      }

      // Type
      String type = getTagContent(root, "RegattaType");
      RegattaType theType = null;
      if (type == null) {
	warnings.add("Regatta type not found, using \"Personal\"");
	theType = RegattaType.PERSONAL;
      }
      else {
	for (RegattaType legalType: RegattaType.values()) {
	  if (type.equalsIgnoreCase(legalType.toString())) {
	    theType = legalType;
	  }
	}
	if (theType == null) {
	  warnings.add("Invalid regatta type, using \"Personal\"");
	  theType = RegattaType.PERSONAL;
	}
      }

      // Scoring
      String scoring = getTagContent(root, "RegattaScoring");
      RegattaScoring theScoring = null;
      if (scoring == null) {
	warnings.add ("Regatta scoring not found, using \"Standard\"");
	theScoring = RegattaScoring.STANDARD;
      }
      else {
	for (RegattaScoring legalScoring: RegattaScoring.values()) {
	  if (scoring.equalsIgnoreCase(legalScoring.toString())) {
	    theScoring = legalScoring;
	    break;
	  }
	}
	if (theScoring == null) {
	  warnings.add ("Invalid regatta scoring, using \"Standard\"");
	  theScoring = RegattaScoring.STANDARD;
	}
      }

      // CREATE REGATTA OBJECT
      regatta = new Regatta(name);
      regatta.setStartTime(dateValue);
      regatta.setType(theType);
      regatta.setScoring(theScoring);
      regatta.setDuration(duration);

      
      // Blurb/Comments:
      // version <  1.4: single blurb, assigned to first day of regatta
      // version >= 1.4: one blurb per racing day
      if (Factory.compareVersions(version, "1.4") < 0) {
	String blurb = getTagContent(root, "Blurb", true);
	if (blurb == null) blurb = "";
	regatta.setBlurb(dateValue, blurb);
      }
      else {
	nl = root.getElementsByTagName("Comments");
	int len = nl.getLength();
	if (len > 0) {
	  // Use the last one
	  Element comElem = (Element)nl.item(len - 1);
	  snl = comElem.getElementsByTagName("Comment");
	  cal.setTime(dateValue);
	  for (int i = 0; i < snl.getLength(); i++) {
	    try {
	      Element comm = (Element)snl.item(i);
	      String blurb = getTagContent(comm);
	      int day = Integer.parseInt(comm.getAttribute("day"));
	      cal.add(Calendar.DAY_OF_MONTH, (day - 1));
	      regatta.setBlurb(cal.getTime(), blurb);
	    } catch (Exception e) {
	      warnings.add("Invalid daily summary found (" + i + ")");
	    }
	  }
	}
      }

      // Number of divisions and races
      String [] attr;
      int numRaces, numDivs;
      attr = getTagAttribute(root, "races");
      try {
	numRaces = (attr.length == 0 ? 10 : Integer.parseInt(attr[0]));
	numRaces = (numRaces >= 1) ? numRaces : 10;
      } catch (NumberFormatException e) {
	warnings.add("Invalid number of races. Using 10");
	numRaces = 10;
      }
      attr = getTagAttribute(root, "divisions");
      try {
	numDivs = (attr.length == 0 ? 10 : Integer.parseInt(attr[0]));
	numDivs = (numDivs > 0 && numDivs <= Regatta.Division.values().length) ?
	  numDivs : 2;
      } catch (NumberFormatException e) {
	warnings.add("Invalid number of divisions. Using 10");
	numDivs = 2;
      }
      regatta.createRaces(numDivs, numRaces);


      // TEAMS
      this.notifyListeners(prop, "Loading teams");
      this.updateTeamMaps(getElement(root, "Teams"));
      for (int i = 0; i < this.teamList.size(); i++) {
	Team team = this.teamList.get(i);
		
	// Add team
	if (!regatta.addTeam(team)) {
	  // Remove the team from the list
	  this.teamList.remove(i);
	  this.idList.remove(i);
	  this.elementList.remove(i);
	  i--;
	}
      }

      this.notifyListeners(prop, "Loading rotations");
      // ROTATIONS
      Rotation rot;
      nl = root.getElementsByTagName("Rotations");
      int len = nl.getLength();
      if (len == 0)
	rot = null;
      else {
	rot = new Rotation();
	// Use the last rotation
	Element rotElem = (Element)nl.item(len - 1);
	snl = rotElem.getElementsByTagName("Sail");
	for (int i = 0; i < snl.getLength(); i++) {
	  try {
	    Element sail = (Element)snl.item(i);
	    String tid = sail.getAttribute("team");
	    String rid = sail.getAttribute("race");
	    String sid = sail.getAttribute("sail");

	    int index = this.idList.indexOf(tid);
	    if (index < 0) {
	      throw new IllegalArgumentException("Invalid team id in rotation: " + tid);
	    }
	    Team t = this.teamList.get(index);

	    Sail s = new Sail(String.valueOf(sid));
	    // race
	    int length = rid.length();
	    int raceNum = Integer.parseInt(rid.substring(0, length - 1));
	    Division raceDiv = Factory.parseDivision(rid.charAt(length - 1));
	    Race race = regatta.getRace(raceDiv, raceNum);

	    rot.setSail(race, t, s);
	  }
	  catch (ClassCastException e) {}
	  catch (IllegalArgumentException e) {
	    warnings.add(e.getMessage());
	  }
	}

	// Check integrity of rotation
	Race [] badRaces;
	if (regatta.getScoring() == RegattaScoring.COMBINED)
	  badRaces = rot.normalize(regatta.getDivisions());
	else
	  badRaces = rot.normalize();

	if (badRaces.length > 0) {
	  String raceString = Factory.implode(badRaces, ", ");
	  warnings.add("Rotation is incomplete for race(s): " + raceString);
	  // remove them too
	  for (Race race : badRaces) {
	    rot.removeRace(race);
	  }
	}
      }
      regatta.setRotation(rot);

      this.notifyListeners(prop, "Loading finishes");
      // FINISHES
      nl = root.getElementsByTagName("Finishes");
      len = nl.getLength();
      if (len > 0) {
	Element rotElem = (Element)nl.item(len - 1);
	snl = rotElem.getElementsByTagName("Finish");
	DateFormat tf = DateFormat.getTimeInstance(DateFormat.LONG);
	for (int i = 0; i < snl.getLength(); i++) {
	  try {
	    Element finElem = (Element)snl.item(i);
	    String tid = finElem.getAttribute("team");
	    String rid = finElem.getAttribute("race");
	    String typ = finElem.getAttribute("type");
	    String stamp = getTagContent(finElem);

	    int index = this.idList.indexOf(tid);
	    if (index < 0) {
	      throw new IllegalArgumentException("Invalid team id in finish: " + tid);
	    }
	    Team team = this.teamList.get(index);
	    
	    // race
	    int length = rid.length();
	    int raceNum = Integer.parseInt(rid.substring(0, length - 1));
	    Division raceDiv = Factory.parseDivision(rid.charAt(length - 1));
	    Race race = regatta.getRace(raceDiv, raceNum);

	    // Timestamp
	    Date time = tf.parse(stamp);

	    regatta.setFinish(new Finish(race, team, time));
	  } catch (IllegalArgumentException e) {
	    warnings.add(e.getMessage());
	  }
	}

	// Check that every team has a finish
	Race [] badRaces = regatta.normalizeFinishes();
	if (badRaces.length > 0) {
	  String raceString = Factory.implode(badRaces, ", ");
	  warnings.add("Incomplete finishes for race(s): " +
		       raceString +
		       ". Race(s) removed.");
	  for (Race race : badRaces) {
	    regatta.removeFinishes(race);
	  }
	}
      }

      this.notifyListeners(prop, "Entering penalties");
      // PENALTIES
      nl = root.getElementsByTagName("Penalties");
      len = nl.getLength();
      if (len > 0) {
	Element rotElem = (Element)nl.item(len - 1);
	snl = rotElem.getElementsByTagName("Penalty");
	for (int i = 0; i < snl.getLength(); i++) {
	  try {
	    Element finElem = (Element)snl.item(i);
	    String tid = finElem.getAttribute("team");
	    String rid = finElem.getAttribute("race");
	    String typ = finElem.getAttribute("type");

	    int index = this.idList.indexOf(tid);
	    if (index < 0) {
	      throw new IllegalArgumentException("Invalid team id in penalty: " + tid);
	    }
	    Team team = this.teamList.get(index);
	    // race
	    int length = rid.length();
	    int raceNum = Integer.parseInt(rid.substring(0, length - 1));
	    Division raceDiv = Factory.parseDivision(rid.charAt(length - 1));
	    Race race = regatta.getRace(raceDiv, raceNum);

	    // Get finish
	    Finish finish = regatta.getFinish(race, team);
	    if (finish == null) {
	      String mes = "Penalty ignored. No finish found for team " +
		team + " in race " + race;
	      throw new IllegalArgumentException(mes);
	    }

	    // Finish type
	    PenaltyType ftype = PenaltyType.DSQ; // Assume regular finish
	    for (PenaltyType t : PenaltyType.values()) {
	      if (t.toString().equalsIgnoreCase(typ)) {
		ftype = t;
		break;
	      }
	    }

	    // Comments
	    String comms = getTagContent(finElem);

	    finish.setPenalty(new Penalty(ftype, comms));
	  } catch (IllegalArgumentException e) {
	    warnings.add(e.getMessage());
	  }
	}
      }

      this.notifyListeners(prop, "Loading breakdowns");
      // BREAKDOWNS
      nl = root.getElementsByTagName("Breakdowns");
      len = nl.getLength();
      if (len > 0) {
	Element rotElem = (Element)nl.item(len - 1);
	snl = rotElem.getElementsByTagName("Breakdown");
	for (int i = 0; i < snl.getLength(); i++) {
	  try {
	    Element finElem = (Element)snl.item(i);
	    String tid = finElem.getAttribute("team");
	    String rid = finElem.getAttribute("race");
	    String typ = finElem.getAttribute("type");

	    int index = this.idList.indexOf(tid);
	    if (index < 0) {
	      throw new IllegalArgumentException("Invalid team id in breakdown: " + tid);
	    }
	    Team team = this.teamList.get(index);
	    // race
	    int length = rid.length();
	    int raceNum = Integer.parseInt(rid.substring(0, length - 1));
	    Division raceDiv = Factory.parseDivision(rid.charAt(length - 1));
	    Race race = regatta.getRace(raceDiv, raceNum);

	    // Get finish
	    Finish finish = regatta.getFinish(race, team);
	    if (finish == null) {
	      String mes = "Breakdown ignored. No finish found for team " +
		team + " in race " + race;
	      throw new IllegalArgumentException(mes);
	    }

	    // Finish type
	    BreakdownType ftype = BreakdownType.BKD; // Assume regular finish
	    for (BreakdownType t : BreakdownType.values()) {
	      if (t.toString().equalsIgnoreCase(typ)) {
		ftype = t;
		break;
	      }
	    }
	    // Comments
	    String comms = getTagContent(finElem);

	    // Create the breakdown
	    Breakdown breakdown = new Breakdown(ftype, comms);

	    // If RDG, get also the amount
	    if (ftype == BreakdownType.RDG) {
	      int handicap;
	      try {
		handicap = Integer.parseInt(finElem.getAttribute("amount"));
	      } catch (NumberFormatException e) {
		warnings.add("Invalid or missing amount for RDG breakdown. Using +2");
		handicap = 2;
	      }
	      breakdown.setHandicap(handicap);
	    }

	    finish.setBreakdown(breakdown);
	  } catch (IllegalArgumentException e) {
	    warnings.add(e.getMessage());
	  }
	}
      }

      // TEAM PENALTIES
      nl = root.getElementsByTagName("TeamPenalties");
      len = nl.getLength();
      if (len > 0) {
	Element rotElem = (Element)nl.item(len - 1);
	snl = rotElem.getElementsByTagName("TeamPenalty");
	for (int i = 0; i < snl.getLength(); i++) {
	  try {
	    Element finElem = (Element)snl.item(i);
	    String tid = finElem.getAttribute("team");
	    String rid = finElem.getAttribute("division");
	    String typ = finElem.getAttribute("type");

	    int index = this.idList.indexOf(tid);
	    if (index < 0) {
	      throw
		new IllegalArgumentException("Invalid team id in team penalty: " +
					     tid);
	    }
	    Team team = this.teamList.get(index);
	    // Division
	    Division div = Factory.parseDivision(rid.charAt(0));
	    // Type
	    TeamPenaltyType ptype = TeamPenalty.parseType(typ);
	    // Comments
	    String comms = getTagContent(finElem);
	    regatta.setTeamPenalty(new TeamPenalty(div, team, ptype, comms));
	    
	  } catch (IllegalArgumentException e) {
	    warnings.add(e.getMessage());
	  }
	}
      }


      // RP Database
      // Ensure global sailor ID uniqueness, and keep for quick
      // dereferencing when loading RPs
      Map<String, Sailor> ids = new HashMap<String, Sailor>();
      int highest_id = 0;
      if (databaseDir != null) {
	this.notifyListeners(prop, "Creating RP database");
	try {
	  // MISC file
	  FileWriter fw = new FileWriter(new File(databaseDir, "_MISC"));;
	  BufferedWriter out = new BufferedWriter(fw);
	  out.write("");
	  out.close();

	  // Other affiliations, as found in the Membership tag
	  Element memElem = getElement(root, "Membership");
	  if (memElem != null) {
	    String format = "%s\t%s\t%s\t%s";
	    nl = memElem.getElementsByTagName("Affiliate");
	    for (int i = 0; i < nl.getLength(); i++) {
	      Element e = (Element)nl.item(i);
	      String id = e.getAttribute("id").trim();

	      if (id.length() > 0) {
		fw = new FileWriter(new File(databaseDir, id));
		out = new BufferedWriter(fw);
		out.write("");
		snl = e.getElementsByTagName("Member");
		for (int j = 0; j < snl.getLength(); j++) {
		  Element subE = (Element)snl.item(j);
		  String sID = subE.getAttribute("id").trim();
		  String nam = getTagContent(subE, "Name");
		  String yer = getTagContent(subE, "Year");
		  cal = Calendar.getInstance();
		  Date year;
		  try {
		    cal.set(Calendar.YEAR, Integer.parseInt(yer));
		  } catch (NullPointerException ex) {
		    warnings.add("Missing year, using current.");
		  } catch (NumberFormatException ex) {
		    warnings.add("Invalid number value for sailor year.");
		  }
		  year = cal.getTime();

		  if (sID.length() > 0 && (!ids.containsKey(sID)) && nam != null) {
		    out.write(String.format(format, sID, nam, yer, "false"));
		    out.newLine();
		    ids.put(sID, new Sailor(sID, nam, year));
		    if (sID.matches(Factory.RP_PREFIX + "[0-9]+")) {
		      highest_id = Integer.parseInt(sID.substring(Factory.RP_PREFIX.length()));
		      Factory.setLastRpId(highest_id);
		    }
		  }
		  else {
		    warnings.add("Invalid sailor information in membership file.");
		  }
		}
		out.close();
	      }
	    }
	  }
	} catch (IllegalArgumentException e) {
	  warnings.add(e.getMessage());
	} catch (IOException e) {
	  e.printStackTrace();
	  warnings.add("Unable to create temp dir: " + e.getMessage());
	} catch (SecurityException e) {
	  warnings.add("Do not have priviledges to create directory: " +
		       e.getMessage());
	}
      }

      this.notifyListeners(prop, "Loading RP info");
      // RP
      nl = root.getElementsByTagName("RP");
      len = nl.getLength();
      if (len > 0) {
	RP rp = new RP();
	regatta.setRP(rp);
	Element rotElem = (Element)nl.item(len - 1);
	snl = rotElem.getElementsByTagName("Sailor");
	Sailor sailor;
	for (int i = 0; i < snl.getLength(); i++) {
	  try {
	    Element sailorElem = (Element)snl.item(i);
	    String roleID = sailorElem.getAttribute("role");
	    String divID  = sailorElem.getAttribute("division");
	    String raceStr= sailorElem.getAttribute("races");
	    String tid    = sailorElem.getAttribute("team");
	    String sid    = sailorElem.getAttribute("id");

	    // Role
	    BoatRole role = RP.parseRole(roleID);
	    if (role == null) {
	      warnings.add("Missing role, assuming CREW.");
	      role = BoatRole.CREW;
	    }

	    // Division
	    Division div = Factory.parseDivision(divID.charAt(0));
	    if (div == null) {
	      throw new IllegalArgumentException("Missing division.");
	    }

	    // Team
	    int index = this.idList.indexOf(tid);
	    if (index < 0) {
	      throw new IllegalArgumentException("Invalid team id in RP: " + tid);
	    }
	    Team team = this.teamList.get(index);

	    // ID
	    if (!ids.containsKey(sid)) {
	      throw new IllegalArgumentException("Unknown sailor ID in RP.");
	    }
	    sailor = ids.get(sid);
	    Integer [] nums = Factory.parseList(raceStr);
	    List<Race> raceList = new ArrayList<Race>();
	    for (int num : nums) {
	      Race race = regatta.getRace(div, num);
	      if (race != null) {
		raceList.add(race);
	      }
	    }

	    rp.addSailorRaces(team,
			      sailor,
			      raceList,
			      role);
	    
	  } catch (NumberFormatException e) {
	    warnings.add("Invalid value for year. Expected number. RP Ignored");
	  } catch (IllegalArgumentException e) {
	    warnings.add(e.getMessage());
	  }
	}
      }

      this.notifyListeners("done", new Boolean(true));
      return (warnings.size() == 0);

    } catch (ParserConfigurationException e) {
      errors.add("Error with parser: " + e.getMessage());
    } catch (SAXException e) {
      errors.add("Error interpreting file: " + e.getMessage());
    } catch (IOException e) {
      errors.add("Could not open file: " + e.getMessage());
    } catch (Exception e) {
      errors.add("Unknown error: " + e.getMessage());
      e.printStackTrace();
    }
    throw new IllegalArgumentException("Unable to load file.");
  }

  /**
   * Returns string of errors or warnings encountered during
   * parsing of the last file
   *
   * @return a <code>String[]</code> array of error messages in
   * chronological order
   */
  public String [] getErrors() {
    return this.errors.toArray(new String []{});
  }

  /**
   * Gets a list of warnings, yeahy!
   *
   * @return a <code>String[]</code> value
   */
  public String [] getWarnings() {
    return this.warnings.toArray(new String []{});
  }

  // Helper methods

  /**
   * Returns content of the tag rooted at the element root, converting
   * new lines and tabs into spaces.
   *
   * @param root an <code>Element</code> value
   * @param tag a <code>String</code> value
   * @return a <code>String</code> value
   */
  private String getTagContent(Element root,
			       String tag) {
    return getTagContent(root, tag, false);
  }

  /**
   * Gets the (String) content of the <code>tag</code> rooted at the
   * element <code>root</code>. Where multiple tags are found, the
   * last one is returned. Honor new lines in the content, if so
   * specified.
   *
   * @return <code>null</code> if the tag was not found as a direct
   * descendant of root.
   */
  private String getTagContent(Element root,
			       String tag,
			       boolean honorNewline) {
    NodeList nl = root.getElementsByTagName(tag);
    int len = nl.getLength();
    if (len == 0)
      return null;
    
    return getTagContent((Element)nl.item(len - 1));
  }

  private String getTagContent(Element root) {
    return getTagContent(root, false);
  }

  /**
   * Get the String content of the given tag.
   *
   * @param root an <code>Element</code> value
   * @return a <code>String</code> value
   */
  private String getTagContent(Element root,
			       boolean honorNewLine) {
    try {
      NodeList subL = root.getChildNodes();
      int len = subL.getLength();
      if (len == 0) {
	return null;
      }
      String content = subL.item(len - 1).getNodeValue().trim();
      if (!honorNewLine) {
	content = content.replaceAll("[ \t\n\f\r]+", " ");
      }
      return content;
    } catch (ClassCastException e) {
      System.err.println("Unexpected construct.");
    }
    return null;
  }

  /**
   * @return an array of the attributes for a specific tag.
   */
  private String [] getTagAttribute(Element tag,
				    String attr) {
    String attrString = tag.getAttribute(attr);
    return attrString.split("/ /");
  }

  /**
   * Looks in the root element node for (the last) child with name
   * given in tagname, and returns it. If no children with such a tag
   * name exist, creates one using the specified Document object and
   * appends it to the root element.
   *
   * @param doc a <code>Document</code> value
   * @param root an <code>Element</code> value
   * @param tagname a <code>String</code> value
   * @return an <code>Element</code> value
   */
  private Element getOrCreateElement(Document doc,
				     Element root,
				     String tagname) {
    Element newTag;
    Element oldTag = getElement(root, tagname);
    if (oldTag == null) {
      newTag = doc.createElement(tagname);
      root.appendChild(newTag);
    }
    else {
      newTag = oldTag;
    }
    return newTag;
  }

  /**
   * Removes all children from specified root, and adds those in the
   * list instead.
   *
   * @param root an <code>Element</code> value
   * @param children a <code>Node</code> value
   */
  private void replaceElementContent(Element root,
				     List<Node> children) {
    NodeList nl = root.getChildNodes();
    while (nl.getLength() > 0) {
      root.removeChild(nl.item(0));
    }
    for (Node n : children) {
      root.appendChild(n);
    }
  }

  /**
   * Removes all sub-elements rooted at this node which match the
   * given tag.
   *
   * @param root an <code>Element</code> value
   * @param tag a <code>String</code> value
   */
  private void removeSubElements(Element root,
				 String tag) {
    
    NodeList nl = root.getElementsByTagName(tag);
    while (nl.getLength() > 0) {
      root.removeChild(nl.item(0));
    }
  }

  /**
   * Overloaded method for convenience.
   *
   * @param root an <code>Element</code> value
   * @param child a <code>Node</code> value
   */
  private void replaceElementContent(Element root, Node child) {
    this.replaceElementContent(root, Arrays.asList(new Node []{child}));
  }

  // Property change listeners

  public void addPropertyChangeListener(PropertyChangeListener l) {
    this.listeners.add(l);
  }

  private void notifyListeners(String propName, Object message) {
    for (PropertyChangeListener l : this.listeners) {
      l.propertyChange(new PropertyChangeEvent(this,
					       propName,
					       null,
					       message));
    }
  }

  /**
   * Updates the internal id-team, and team-element maps with the
   * teams found underneath the root element specified.
   *
   * @param root an <code>Element</code> value
   */
  private void updateTeamMaps(Element elem) {
    this.idList = new ArrayList<String>();
    this.teamList = new ArrayList<Team>();
    this.elementList = new ArrayList<Element>();
    List<String> affiliations = new ArrayList<String>();

    if (elem == null) {
      return;
    }
    NodeList nl = elem.getElementsByTagName("Team");

    for (int i = 0; i < nl.getLength(); i++) {
      try {
	Element team = (Element)nl.item(i);
	String id    = team.getAttribute("id");
	String lname = getTagContent(team, "LongName");
	String sname = getTagContent(team, "ShortName");
	if (sname == null) {
	  sname = "";
	}
	if (id.length() == 0) {
	  throw new IllegalArgumentException("ID missing for team " + lname);
	}
	String aff   = team.getAttribute("affiliate");
	if (aff.length() == 0) {
	  aff = "_MISC";
	}

	// Add team, if unique
	Team t = new Team(lname.trim(), sname.trim());
	if (!this.teamList.contains(t) &&
	    !this.idList.contains(id)  &&
	    !this.elementList.contains(team)) {
	  this.idList.add(id);
	  this.teamList.add(t);
	  this.elementList.add(team);
	  t.setAffiliation(aff);
	}
      }
      catch (ClassCastException e) {}
      catch (IllegalArgumentException e) {
	warnings.add(e.getMessage());
      } catch (NullPointerException e) {}
    }
  }

  public static void main (String [] args) {
    RegattaIO io = new RegattaIO();
    for (String filename : args) {
      try {
	boolean success = io.readFile(new File(filename), null);
	if (success) {
	  System.out.println("Success with no problems.");
	}
	else {
	  for (String mes : io.getWarnings())
	    System.out.println(mes);
	    
	}
	Regatta reg = io.getRegatta();
	Team [] teams = reg.getTeams();
	teams[0].setLongname("PPP");
	teams[1].setShortname("QQQ");

	// Add a new team
	reg.addTeam(new Team("Dayan Paez"));
	io.getRegatta().setName("Ave Maria");

	// Change the rotation
	ICSARotationBuilder rb = new ICSARotationBuilder();
	Rotation rot = new Rotation();
	try {
	  rb.fillRotation(rot,
			  RotationType.STANDARD,
			  RotationStyle.NAVY,
			  new Race [][]{reg.getRaces()},
			  reg.getTeams(),
			  new Sail[]{new Sail("21"),
				     new Sail("22"),
				     new Sail("23"),
				     new Sail("24"),
				     new Sail("25"),
				     new Sail("26"),
				     new Sail("27"),
				     new Sail("28"),
				     new Sail("29")},
			  2);
	  reg.setRotation(rot);
	} catch (RotationBuilderException e) {
	  System.out.println("Oopsies");
	}

	// Write file
	io.writeFile(reg, new File("/tmp/file.txt"));
	
      } catch (IllegalArgumentException e) {
	System.out.println("Unable to read file: " + e.getMessage());
      }
    }
  }
}
