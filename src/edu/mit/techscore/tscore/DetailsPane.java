package edu.mit.techscore.tscore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;

import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.Regatta.RegattaScoring;
import edu.mit.techscore.regatta.Regatta.RegattaType;
import org.sourceforge.jcalendarbutton.*;

/**
 * The main pane for editing regatta, this pane contains the details
 * such as name, date, duration, type, and blurb. As of 2009-08-23,
 * the details are edited by actively pressing a button to do so
 * (rather than using the confusing focus mechanism).
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
 * Created: Sun May 31 22:45:10 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class DetailsPane
  extends AbstractPane {
  
  private DateFormatter df, tf;

  // Fields
  private JTextField nameField;
  private JFormattedTextField dateField, timeField;
  private JCalendarButton dateButton;
  private JTimeButton timeButton;
  private SpinnerNumberModel durationModel;
  private JComboBox typeField, scoringField;
  private JTextArea blurbField;

  /**
   * Creates a new <code>DetailsPane</code> instance.
   *
   */
  public DetailsPane(Regatta reg) {
    super("Details");
    this.setIcon(Factory.getImageIcon("img/Edit16.gif", this.toString()));
    this.df = new DateFormatter(DateFormat.getDateInstance(DateFormat.SHORT,
							   Locale.US));
    this.tf = new DateFormatter(DateFormat.getTimeInstance(DateFormat.SHORT,
							   Locale.US));
    this.setLayout(new GridBagLayout());
    this.setRegatta(reg);
  }
  
  public void fill() {
    this.removeAll();

    GridBagConstraints p = new GridBagConstraints();
    p.gridx = 0;
    p.gridy = 0;
    p.insets = new Insets(2,2,2,2);
    p.fill = GridBagConstraints.BOTH;
    p.weightx = 1.0;
    p.weighty = 0.0;
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Settings"));
    this.add(panel, p);

    GridBagConstraints c1, c2, c3;
    Insets insts = new Insets(2,2,2,2);
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c3 = new GridBagConstraints();
    c1.anchor = GridBagConstraints.PAGE_START;
    c1.gridx = 0; c2.gridx = 1; c3.gridx = 2;
    c1.gridy = 0; c2.gridy = 0; c3.gridy = 0;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c2.fill = GridBagConstraints.BOTH;
    c3.fill = GridBagConstraints.HORIZONTAL;
    c1.insets = insts;
    c2.insets = insts;
    c3.insets = insts;
    c2.weightx = 1.0;
    
    //- Name
    c2.gridwidth = 2;
    JLabel label;
    label = Factory.label("Name:");
    nameField = Factory.editField(regatta.getName());
    nameField.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  DetailsPane.this.regatta.
	    setName(DetailsPane.this.nameField.getText());
	}
      });
    panel.add(label, c1);
    panel.add(nameField, c2);

    //- Date
    c1.gridy++; c2.gridy++; c3.gridy++;
    c2.gridwidth = 1;
    label = Factory.label("Date:");
    dateField = new DateEditField(this.df);
    dateField.setValue(regatta.getStartTime());
    dateField.addPropertyChangeListener(new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent evt) {
	  if (evt.getPropertyName().equals("value")) {
	    // Notify the dateButton
	    Date newValue = (Date)dateField.getValue();
	    dateButton.setTargetDate(newValue);
	    
	    // Set the date in the regatta
	    regatta.setStartTime(newValue);
	  }
	}
      });
    panel.add(label, c1);
    panel.add(dateField, c2);

    //- Calendar button
    dateButton = new JCalendarButton(regatta.getStartTime());
    dateButton.addPropertyChangeListener(new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent evt) {
	  if (evt.getNewValue() instanceof Date) {
	    Date newValue = (Date)evt.getNewValue();
	    // Notify the dateField of the change. Unforunately, this
	    // means that the dateField will in turn reset the value
	    // of calendar button.
	    dateField.setValue(newValue);
	  }
	}
      });
    panel.add(dateButton, c3);

    //- Time
    c1.gridy++; c2.gridy++; c3.gridy++;
    label = Factory.label("Time:");
    timeField = new DateEditField(this.tf);
    timeField.setValue(regatta.getStartTime());
    timeField.addPropertyChangeListener(new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent evt) {
	  if (evt.getPropertyName().equals("value")) {
	    // Notify the dateButton
	    Date newValue = (Date)timeField.getValue();
	    timeButton.setTargetDate(newValue);
	    
	    // Set the date in the regatta
	    regatta.setStartTime(newValue);
	  }
	}
      });
    panel.add(label, c1);
    panel.add(timeField, c2);

    //- Time button
    timeButton = new JTimeButton(regatta.getStartTime());
    timeButton.addPropertyChangeListener(new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent evt) {
	  if (evt.getNewValue() instanceof Date) {
	    // Notify the timeField of the change
	    timeField.setValue((Date)evt.getNewValue());
	  }
	}
      });
    panel.add(timeButton, c3);

    //- Duration
    c1.gridy++; c2.gridy++; c3.gridy++;
    label = Factory.label("Duration:");
    durationModel = new SpinnerNumberModel(regatta.getDuration(),
					   1, null, 1);
    durationModel.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent evt) {
	  regatta.setDuration(durationModel.getNumber().intValue());
	}
      });
    JSpinner durationField = Factory.spinner(durationModel);
    JLabel days = new JLabel("day(s)");
    panel.add(label, c1);
    panel.add(durationField, c2);
    panel.add(days, c3);

    //- Type
    c1.gridy++; c2.gridy++; c3.gridy++;
    c2.weightx = 1.0;
    c2.gridwidth = 2;
    label = Factory.label("Type:");
    typeField = new JComboBox(RegattaType.values());
    typeField.setSelectedItem(regatta.getType());
    typeField.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  regatta.setType((RegattaType)typeField.getSelectedItem());
	}
      });
    panel.add(label, c1);
    panel.add(typeField, c2);

    //- Scoring
    c1.gridy++; c2.gridy++; c3.gridy++;
    c2.weightx = 1.0;
    c2.gridwidth = 2;
    label = Factory.label("Scoring:");
    scoringField = new JComboBox(RegattaScoring.values());
    scoringField.setSelectedItem(regatta.getScoring());
    scoringField.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  regatta.setScoring((RegattaScoring)scoringField.getSelectedItem());
	}
      });
    panel.add(label, c1);
    panel.add(scoringField, c2);

    //-  Spacer and messages pane
    p.gridy++;
    p.weighty = 1.0;
    JPanel infoLabel = new JPanel();
    JScrollPane pane = new JScrollPane(infoLabel);
    this.add(pane, p);
    pane.setBorder(BorderFactory.createTitledBorder("Overview"));
    this.getRegattaInfo(infoLabel);
  }

  private void getRegattaInfo(JPanel p) {
    p.setLayout(new GridBagLayout());
    GridBagConstraints c0, c1;
    c0 = new GridBagConstraints();
    c1 = new GridBagConstraints();
    c0.gridx = 0; c1.gridx = 1;
    c0.gridy = 0; c1.gridy = 0;
    c0.insets = new Insets(2,2,2,2);
    c1.insets = new Insets(2,2,2,2);
    c0.fill = GridBagConstraints.HORIZONTAL;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c0.weightx = 0.5;
    c1.weightx = 1.0;
    
    newRow(p, c0, c1, "No. teams:",     this.regatta.getTeams().length);
    newRow(p, c0, c1, "No. divisions:", this.regatta.getDivisions().length);
    newRow(p, c0, c1, "No. races:",     this.regatta.getNumRaces());
    newRow(p, c0, c1, "","");
    newRow(p, c0, c1, "Using rotation:", (this.regatta.getRotation() == null) ? "No" : "Yes");
    newRow(p, c0, c1, "","");
    newRow(p, c0, c1, "Scored races:",  this.regatta.getFinishedRaces().length);

    // spacer
    c0.weighty = 1.0;
    c1.weighty = 1.0;
    newRow(p, c0, c1, "", "");
  }

  /**
   * Helper method returns a row in a table with the header and
   * content as given
   *
   * @param header the header text
   * @param content the content
   */
  private void newRow(JPanel p,
		      GridBagConstraints c0,
		      GridBagConstraints c1,
		      String header,
		      String content) {
    p.add(new JLabel(header,  SwingConstants.LEFT), c0);
    p.add(new JLabel(content, SwingConstants.RIGHT),c1);
    c0.gridy++;
    c1.gridy++;
  }
  private void newRow(JPanel p,
		      GridBagConstraints c0,
		      GridBagConstraints c1,
		      String header,
		      int content) {
    newRow(p, c0, c1, header, String.valueOf(content));
  }

  /**
   * Overrides AbstractPane method.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isUsable() {
    return true;
  }
}
