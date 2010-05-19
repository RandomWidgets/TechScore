package edu.mit.techscore.tscore;

import regatta.Regatta;
import java.io.File;
import javax.swing.AbstractAction;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.Action;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import regatta.RegattaEvent;
import regatta.RegattaEvent.RegattaEventType;
import regatta.Team;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import java.util.Date;
import javax.swing.JComponent;
import java.io.FileWriter;
import java.io.BufferedWriter;
import javax.swing.JPanel;
import javax.swing.JSpinner.DateEditor;

/**
 * Manages the database of registered sailors
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
 * Created: Fri Sep 11 10:56:11 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class RPDatabasePane
  extends AbstractPane
  implements ActionListener {

  // Private variables
  private JComboBox teamCombo, affCombo;
  private JScrollPane form;
  private JPanel table;

  private Action clearDataAction;
  private File databaseDir;

  private final static String NO_AFFILIATION = "";

  /**
   * Creates a new <code>RPDatabasePane</code> instance.
   *
   */
  public RPDatabasePane(Regatta reg) {
    super("Sailor Database");
    setRegatta(reg);
    this.databaseDir = null;
    this.clearDataAction = new ClearDataAction();
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

  public boolean isUsable() {
    return this.regatta.getTeams().length > 0;
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
    this.teamCombo = new JComboBox(regatta.getTeams());
    this.teamCombo.addActionListener(this);
    this.add(label, c1);
    this.add(teamCombo, c2);

    //- Affiliation
    c1.gridy++; c2.gridy++;
    label = Factory.label("Set affiliation:");
    this.affCombo = new JComboBox(this.getActiveAffiliations());
    this.affCombo.addActionListener(this);
    this.affCombo.setEditable(true);
    this.add(label, c1);
    this.add(affCombo, c2);

    //- Form
    c1.gridy++; c2.gridy++;
    c1.gridwidth = 2;
    c1.weightx = 1.0;
    c1.weighty = 1.0;
    c1.fill = GridBagConstraints.BOTH;
    this.form = new JScrollPane();
    this.add(form, c1);

    //- Clear data button
    c1.gridy++; c2.gridy++;
    c1.gridwidth = 1;
    c1.weightx = 0.0;
    c1.weighty = 0.0;
    c1.fill = GridBagConstraints.NONE;
    c2.fill = GridBagConstraints.NONE;
    c1.anchor = GridBagConstraints.LINE_START;
    c2.anchor = GridBagConstraints.LINE_END;
    this.add(new JButton(this.clearDataAction), c1);

    this.updateForm();
  }

  /**
   * Gets the set of affiliations currently in use by all the teams in
   * this regatta, plus the "_MISC" affiliation. Also, delete all
   * affiliation files from database which are empty and not in use by
   * any team (except, of course, for "_MISC")
   *
   * @return a <code>String[]</code> value
   */
  private String [] getActiveAffiliations() {
    Set<String> affs = new TreeSet<String>();
    for (Team t : this.regatta.getTeams()) {
      affs.add(this.translateAffiliation(t.getAffiliation()));
    }
    // Add empty affiliation ("_MISC")
    affs.add(NO_AFFILIATION);

    // TODO: remove empty affiliations
    return affs.toArray(new String[]{});
  }

  private String translateAffiliation(String aff) {
    return (aff.equals("_MISC")) ? NO_AFFILIATION : aff;
  }

  /**
   * Updates the affiliation combo and rewrites the list of sailors
   * from database for the given affiliation.
   *
   */
  private void updateForm() {
    Team team = (Team)this.teamCombo.getSelectedItem();
    String aff= team.getAffiliation();
    this.affCombo.setSelectedItem(this.translateAffiliation(aff));
  }

  /**
   * Updates the list of sailors for the given affiliation
   *
   */
  private void updateList(String aff) {
    List<DatabaseRow> rows = new ArrayList<DatabaseRow>();
    File file = new File(this.databaseDir, aff);
    try {
      if (!file.exists()) {
	file.createNewFile();
      }
      BufferedReader reader =
	new BufferedReader(new FileReader(file));
      String line;
      Calendar cal;
      while ((line = reader.readLine()) != null) {
	String [] fields = line.split("\t");
	try {
	  // Check for validity of year
	  cal = Calendar.getInstance();
	  cal.set(Calendar.YEAR, Integer.parseInt(fields[2]));
	  boolean isNew = false;
	  try {
	    isNew = Boolean.parseBoolean(fields[3]);
	  } catch (Exception e) {}

	  rows.add(new DatabaseRow(aff,
				   fields[0],
				   fields[1],
				   cal.getTime(),
				   isNew));
	} catch (IndexOutOfBoundsException e) {
	  System.err.println("Missing/invalid fields for sailor: " + line);
	} catch (NumberFormatException e) {
	  System.err.println("Invalid value for year: " + fields[2]);
	}

      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("Unknown affiliation file: " + file);
    } catch (IOException e) {
      System.err.println("I/O error: " + e.getMessage());
    } catch (SecurityException e) {
      System.err.println("Writing/creating file not allowed: " + e.getMessage());
    }
    
    // Create table
    this.table = new JPanel(new GridBagLayout());
    GridBagConstraints [] c = new GridBagConstraints[4];
    c[0] = new GridBagConstraints();
    c[1] = new GridBagConstraints();
    c[2] = new GridBagConstraints();
    c[3] = new GridBagConstraints();
    c[0].gridx = 0; c[1].gridx = 1; c[2].gridx = 2; c[3].gridx = 3;
    c[0].gridy = 0; c[1].gridy = 0; c[2].gridy = 0; c[3].gridy = 0;
    c[0].fill = GridBagConstraints.HORIZONTAL;
    c[1].fill = GridBagConstraints.HORIZONTAL;
    c[2].fill = GridBagConstraints.HORIZONTAL;
    c[3].fill = GridBagConstraints.HORIZONTAL;
    Insets insts = new Insets(2,2,2,2);
    c[0].insets = insts;
    c[1].insets = insts;
    c[2].insets = insts;
    c[3].insets = insts;
    c[0].weightx = 1.0;
    c[1].weightx = 0.5;
    JComponent [] comps;
    // Header
    comps = new JComponent [] {new JLabel("Name"),
			       new JLabel("Year"),
			       new JLabel(""),
			       new JLabel("")};
    for (int i = 0; i < comps.length; i++) {
      this.table.add(comps[i], c[i]);
      c[i].gridy++;
    }
    for (DatabaseRow row : rows) {
      comps = row.getComponentArray();
      for (int i = 0; i < comps.length; i++) {
	this.table.add(comps[i], c[i]);
	c[i].gridy++;
      }
    }
    // add empty row
    c[2].gridwidth = 2;
    comps = (new DatabaseRow(aff)).getComponentArray();
    for (int i = 0; i < comps.length; i++) {
      this.table.add(comps[i], c[i]);
    }
    
    // add spacer
    c[0].gridy++;
    c[0].gridwidth = 4;
    c[0].weighty = 1.0;
    this.table.add(new JLabel(), c[0]);
    this.form.setViewportView(this.table);

    comps[0].requestFocusInWindow();
  }

  // Implementation of action listener
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == this.teamCombo) {
      this.updateForm();
    }
    else if (source == this.affCombo) {
      // Differentiate between the creation of a new affiliation file
      // and the selection of an existing affiliation
      String aff = (String)this.affCombo.getSelectedItem();
      if (aff == NO_AFFILIATION) {
	aff = "_MISC";
      }
      if (evt.getActionCommand().equals("comboBoxEdited")) {
	aff = aff.replaceAll(" ", "");
	try {
	  File file = new File(this.databaseDir, aff);
	  if (file.createNewFile()) {
	    System.out.println("Created a new affiliation file: " + aff);
	  }
	} catch (IOException e) {
	  System.err.println("Unable to create new affiliatio file: " + aff);
	} catch (SecurityException e) {
	  System.err.println("Operation not allowed: " + e.getMessage());
	}
	affCombo.setSelectedItem(this.translateAffiliation(aff));
      }
      else {
	// Set the affiliation for the chosen team
	Team team = (Team)this.teamCombo.getSelectedItem();
	team.setAffiliation(aff);
	this.updateList(aff);
      }
    }

  }

  /**
   * A row in the database entry, with widgets to edit the sailor's
   * name and year (using spinner) and buttons to edit, delete and
   * add.
   *
   */
  class DatabaseRow {
    private String affiliation;
    private JTextField nameField;
    private JSpinner yearSpinner;
    private JButton addBut, editBut, delBut;
    private boolean editable;
    private String id;

    public DatabaseRow(String aff,
		       String id,
		       String name,
		       Date year,
		       boolean editable) {
      this.id = id;
      this.editable    = editable;
      this.affiliation = aff;
      this.nameField = new JTextField(name);
      // Year
      SpinnerDateModel sm = new SpinnerDateModel(year,
						 null,
						 null,
						 Calendar.YEAR);
      this.yearSpinner = new JSpinner(sm);
      yearSpinner.setEditor(new JSpinner.DateEditor(yearSpinner,
						    "yyyy"));
      // Edit and delete buttons
      this.delBut = Factory.tightButton(new DelSailorAction());
      this.editBut= Factory.tightButton(new EditSailorAction());
    }

    /**
     * Creates a new empty <code>DatabaseRow</code> instance, perfect
     * to add to the database.
     *
     */
    public DatabaseRow(String aff) {
      this.affiliation = aff;
      this.nameField = new JTextField();
      this.editable    = true;
      // Year
      SpinnerDateModel sm = new SpinnerDateModel(new Date(),
						 null,
						 null,
						 Calendar.YEAR);
      this.yearSpinner = new JSpinner(sm);
      yearSpinner.setEditor(new JSpinner.DateEditor(yearSpinner,
						    "yyyy"));
      // Add Button
      this.addBut = Factory.tightButton(new AddSailorAction());
    }

    JComponent [] getComponentArray() {
      if (this.addBut == null) {
	return new JComponent [] {this.nameField, this.yearSpinner,
				  this.editBut,   this.delBut};
      }
      return new JComponent [] {this.nameField, this.yearSpinner,
				this.addBut};
    }

    // Actions
    class AddSailorAction extends AbstractAction {
      public AddSailorAction() {
	super("Add");
	this.setEnabled(DatabaseRow.this.editable);
      }
      public void actionPerformed(ActionEvent evt) {
	// Add new sailor
	String name = nameField.getText().trim();
	if (name.length() == 0) {
	  System.out.println("Sailor must have a name.");
	  return;
	}

	Calendar cal = Calendar.getInstance();
	cal.setTime((Date)yearSpinner.getValue());
	int year = cal.get(Calendar.YEAR);
	String line = Factory.getNextRpId() + "\t" +
	  name + "\t" + year + "\ttrue";

	try {
	  FileWriter stream =
	    new FileWriter(new File(databaseDir, affiliation),
			   true);
	  BufferedWriter out = new BufferedWriter(stream);
	  out.write(line);
	  out.newLine();
	  out.close();

	  regatta.fireRegattaChange(new RegattaEvent(regatta,
						     RegattaEventType.RP_DATA,
						     RPDatabasePane.this));
	} catch (IOException e) {
	  System.err.println("Unable to add sailor to file.");
	}

	// Redraw membership list
	updateList(affiliation);
	// Scroll to bottom
	int max = form.getVerticalScrollBar().getMaximum();
	form.getVerticalScrollBar().setValue(max);
      }
    }
    
    class DelSailorAction extends AbstractAction {
      public DelSailorAction() {
	super(null, Factory.getImageIcon("img/Delete16.gif"));
	putValue(Action.SHORT_DESCRIPTION, "Delete sailor.");
	this.setEnabled(DatabaseRow.this.editable);
      }
      public void actionPerformed(ActionEvent evt) {
	// Create temporary file
	Calendar cal = Calendar.getInstance();
	cal.setTime((Date)yearSpinner.getValue());
	try {
	  File temp = File.createTempFile("tsr" + affiliation, "");
	  File affFile = new File(databaseDir, affiliation);
	  BufferedWriter out =
	    new BufferedWriter(new FileWriter(temp));
	  BufferedReader in  =
	    new BufferedReader(new FileReader(affFile));
	  // Pass each line from one file to another
	  String line;
	  while ((line = in.readLine()) != null) {
	    String [] fields = line.split("\t");
	    String theId = fields[0];
	    if (!DatabaseRow.this.id.equals(theId)) {
	      out.write(line);
	      out.newLine();
	    }
	  }
	  // Close both
	  in.close();
	  out.close();

	  // Replace the affiliation file with the temp file.
	  affFile.delete();
	  if (!temp.renameTo(affFile))
	    throw new IOException("Unable to commit changes.");

	} catch (IOException e) {
	  System.err.println("Error while editing: ");
	  e.printStackTrace();
	} catch (SecurityException e) {
	  System.err.println("Writing not allowed.");
	}

	// Redraw the entire list (why?)
	updateList(affiliation);
      }
    }

    class EditSailorAction extends AbstractAction {
      public EditSailorAction() {
	super(null, Factory.getImageIcon("img/Edit16.gif"));
	putValue(Action.SHORT_DESCRIPTION, "Edit sailor.");
	this.setEnabled(DatabaseRow.this.editable);
      }
      public void actionPerformed(ActionEvent evt) {
	// Create temporary file
	Calendar cal = Calendar.getInstance();
	cal.setTime((Date)yearSpinner.getValue());
	try {
	  File temp = File.createTempFile("tsr" + affiliation, "");
	  File affFile = new File(databaseDir, affiliation);
	  BufferedWriter out =
	    new BufferedWriter(new FileWriter(temp));
	  BufferedReader in  =
	    new BufferedReader(new FileReader(affFile));
	  // Pass each line from one file to another
	  String line;
	  while ((line = in.readLine()) != null) {
	    String [] fields = line.split("\t");
	    String theId = fields[0];
	    if (DatabaseRow.this.id.equals(theId)) {
	      // Edit the sailor
	      fields[1] = nameField.getText();
	      fields[2] = String.valueOf(cal.get(Calendar.YEAR));
	      line = String.format("%s\t%s\t%s\t%s",
				   fields[0],
				   fields[1],
				   fields[2],
				   fields[3]);
	    }
	    out.write(line);
	    out.newLine();
	  }
	  // Close both
	  in.close();
	  out.close();

	  // Replace the affiliation file with the temp file.
	  affFile.delete();
	  if (!temp.renameTo(affFile))
	    throw new IOException("Unable to commit changes.");

	} catch (IOException e) {
	  System.err.println("Error while editing: ");
	  e.printStackTrace();
	} catch (SecurityException e) {
	  System.err.println("Writing not allowed.");
	}

	// Redraw the entire list (why?)
	updateList(affiliation);
      }
    }
    
  }
  

  // Actions
  class ClearDataAction extends AbstractAction {
    public ClearDataAction() {
      super("Remove all");
    }
    
    public void actionPerformed(ActionEvent evt) {
      int returnVal = JOptionPane.showConfirmDialog(RPDatabasePane.this,
						    "Delete all entries from database for this team?",
						    "Warning",
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.WARNING_MESSAGE);
      if (returnVal == JOptionPane.NO_OPTION) {
	return;
      }

      // TODO
      regatta.fireRegattaChange(new RegattaEvent(regatta,
						 RegattaEventType.RP_DATA,
						 this));
      RPDatabasePane.this.updateForm();
    }
  }
}
