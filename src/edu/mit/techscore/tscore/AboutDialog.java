package edu.mit.techscore.tscore;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Displays a dialog which displays information about the program.
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
 * Created: Sun Aug 23 18:51:16 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class AboutDialog extends JDialog {

  /**
   * Creates a new <code>AboutDialog</code> instance.
   *
   */
  public AboutDialog(JFrame owner) {
    super(owner, true);

    // Fill
    this.setLocationRelativeTo(owner);
    this.setPreferredSize(new Dimension(250, 200));
    this.setResizable(false);
    this.setLayout(new GridBagLayout());
    GridBagConstraints c1 = new GridBagConstraints();
    c1.gridx = 0;
    c1.gridy = 0;
    c1.insets = new Insets(2,2,2,2);

    // Name
    Icon tsIcon = Factory.getImageIcon("img/techscore.png",
				       "TechScore");
    String name = "Version " + TScoreGUI.VERSION;
    JLabel label = new JLabel(name, tsIcon, JLabel.CENTER);
    label.setVerticalTextPosition(JLabel.BOTTOM);
    label.setHorizontalTextPosition(JLabel.CENTER);
    this.add(label, c1);

    // Authors
    c1.gridy++;
    c1.weighty = 1.0;
    c1.fill = GridBagConstraints.BOTH;
    this.add(new JScrollPane(new JLabel(getAuthors(), JLabel.CENTER)), c1);

    // Close button
    c1.gridy++;
    c1.weighty = 0.0;
    c1.fill = GridBagConstraints.NONE;
    JButton button = new JButton(new AbstractAction() {
	public void actionPerformed(ActionEvent evt) {
	  AboutDialog.this.setVisible(false);
	}
      });
    button.setText("Close");
    this.add(button, c1);
    
    this.pack();
  }

  private String getAuthors() {
    return "<html>Day&aacute;n P&aacute;ez</html>";
  }
}
