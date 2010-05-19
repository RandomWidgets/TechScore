package edu.mit.techscore.tscore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import regatta.Regatta;

/**
 * Since regattas can be multi-day affairs, allow for daily
 * summaries. With this pane, the user can select the day of the
 * regatta and add/edit the associated summary.
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
 *
 * Created: Mon May  3 19:22:36 2010
 *
 * @author <a href="mailto:dayan@marti">dpv</a>
 * @version 1.0
 */
public class DailySummaryPane extends AbstractPane implements ChangeListener {

  private JSpinner daySpinner;
  private JTextArea blurbField;
  
  private Date previousDate;    // track the date displayed
  private DateFormat df;        // ...and its translator

  /**
   * Creates a new <code>DailySummaryPane</code> instance.
   *
   */
  public DailySummaryPane(Regatta reg) {
    super ("Daily Summaries");
    this.setLayout(new GridBagLayout());
    this.setRegatta(reg);
  }

  /**
   * Creates the layout for the page: two fieldsets aligned
   * vertically.  The first to choose the date of the regatta and the
   * latter to enter the summary text.
   *
   */
  public void fill () {
    this.removeAll ();
    
    // Day selector: spinner
    JLabel label;
    GridBagConstraints c1, c2;
    Insets insets = new Insets(2,2,2,2);
    c1 = new GridBagConstraints ();
    c2 = new GridBagConstraints ();
    c1.gridx = 0;
    c2.gridx = 1;
    c1.gridy = 0;
    c2.gridy = 0;
    c1.weighty = 0.0;
    c2.weightx = 0.5;
    c2.fill = GridBagConstraints.HORIZONTAL;
    c1.insets = insets;
    c2.insets = insets;

    // list of possible dates
    this.df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    Date start = this.regatta.getStartTime();
    String [] dates = new String[this.regatta.getDuration()];
    dates[0] = df.format(start);
    this.previousDate = start;
    
    Calendar calendar = Calendar.getInstance ();
    calendar.setTime(start);
    for (int i = 1; i < dates.length; i++) {
      calendar.add(Calendar.DATE, 1);
      dates[i] = df.format(calendar.getTime());
    }
    
    label = Factory.label ("Racing Day:");
    SpinnerModel sm = new SpinnerListModel(dates);
    this.daySpinner = new JSpinner(sm);
    this.daySpinner.addChangeListener(this);
    this.add (label, c1);
    this.add (this.daySpinner, c2);

    // Commit button
    c1.gridy++;
    c1.gridwidth = 2;
    c1.fill = GridBagConstraints.BOTH;
    this.add(new JButton(new AbstractAction("Set Summary") {
	public void actionPerformed(ActionEvent evt) {
	  commitBlurb();
	}
      }), c1);

    // Text area
    // spacer for now
    c1.gridy++;
    c1.weighty = 1.0;
    this.blurbField = new JTextArea(regatta.getBlurb(start));
    this.blurbField.setLineWrap(true);
    this.add(new JScrollPane(blurbField), c1);
  }

  public boolean isUsable () {
    return true;
  }

  /**
   * Commits the blurb shown for the current date
   *
   */
  private void commitBlurb() {
    System.out.println(this.blurbField.getText());
    this.regatta.setBlurb(this.previousDate, this.blurbField.getText().trim());
  }

  // Implementation of java.awt.event.ChangeListener

  /**
   * Loads (and saves) the summary when user changes date
   *
   * @param actionEvent an <code>ActionEvent</code> value
   */
  public final void stateChanged(final ChangeEvent evt) {

    // update previous date to the new value
    try {
      this.previousDate = this.df.parse((String)this.daySpinner.getValue());
      this.blurbField.setText(this.regatta.getBlurb(this.previousDate));
    } catch (Exception e) {
      System.err.println("Unable to parse my own date? Please file bug report.");
    }
  }

  /**
   * Commits the changes to the current blurb.
   *
   * @return true
   */
  public boolean empty() {
    this.commitBlurb();
    return true;
  }
}
