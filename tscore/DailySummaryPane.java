package tscore;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import regatta.Regatta;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JPanel;

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
public class DailySummaryPane extends AbstractPane implements ActionListener {

  private JSpinner daySpinner;

  /**
   * Creates a new <code>DailySummaryPane</code> instance.
   *
   */
  public DailySummaryPane(Regatta reg) {
    super ("Daily Summaries");
    this.setLayout (new GridBagLayout ());
    this.setRegatta (reg);
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
    c1 = new GridBagConstraints ();
    c2 = new GridBagConstraints ();
    c1.gridx = 0;
    c2.gridx = 1;
    c1.weighty = 0.0;
    c2.weightx = 0.5;
    c2.fill = GridBagConstraints.HORIZONTAL;

    label = Factory.label ("Day:");
    Date start = this.regatta.getStartTime ();
    Calendar calendar = Calendar.getInstance ();
    calendar.setTime (start);
    calendar.add (Calendar.DATE, this.regatta.getDuration ());
    SpinnerDateModel sm = new SpinnerDateModel(start, start, calendar.getTime (), Calendar.DATE);
    this.daySpinner = new JSpinner (sm);
    this.add (label, c1);
    this.add (this.daySpinner, c2);

    // Text area
    // spacer for now
    c1.gridy++;
    c1.gridwidth = 2;
    c1.weighty = 1.0;
    c1.fill = GridBagConstraints.BOTH;
    this.add (new JPanel (), c1);
  }

  public boolean isUsable () {
    return true;
  }

  // Implementation of java.awt.event.ActionListener

  /**
   * Describe <code>actionPerformed</code> method here.
   *
   * @param actionEvent an <code>ActionEvent</code> value
   */
  public final void actionPerformed(final ActionEvent actionEvent) {

  }

}
