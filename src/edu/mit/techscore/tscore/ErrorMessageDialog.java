package edu.mit.techscore.tscore;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Container;

import java.awt.Component;
import javax.swing.JEditorPane;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JToggleButton;
import javax.swing.AbstractButton;
import edu.mit.techscore.dpxml.XMLTag;
import edu.mit.techscore.dpxml.XMLTextTag;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * A dialog similar to JOptionPane's message dialogs that can display
 * a JEditorPane with a set of bullet points for an extended message
 * of "what went wrong".
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
 * Created: Sat Aug 29 11:32:24 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class ErrorMessageDialog extends JDialog {

  public ErrorMessageDialog(JFrame parent,
			    String mainMessage,
			    String title,
			    String [] notes) {
    this(parent, JOptionPane.ERROR_MESSAGE, mainMessage, title, notes);
  }

  /**
   * Creates a new <code>ErrorMessageDialog</code> instance.
   *
   */
  public ErrorMessageDialog(JFrame parent,
			    int type,
			    String mainMessage,
			    String title,
			    String [] notes) {
    super(parent, true);
    this.setLocationRelativeTo(parent);
    this.setTitle(title);

    // Icon
    String key;
    switch (type) {
    case JOptionPane.WARNING_MESSAGE:
      key = "OptionPane.warningIcon";
      break;
    default:
      key = "OptionPane.errorIcon";
      break;
    }
    Icon icon = UIManager.getIcon(key);
    System.out.println("Icon: " + icon);
    this.setPreferredSize(new Dimension(250, 250));

    // Fill
    JPanel cp = new JPanel(new GridBagLayout());
    GridBagConstraints c1, c2;
    c1 = new GridBagConstraints();
    c2 = new GridBagConstraints();
    c1.gridx = 0;
    c1.gridy = 0;
    c1.weightx = 1.0;
    c1.weighty = 0.0;
    c1.insets = new Insets(2,2,2,2);
    c1.fill = GridBagConstraints.HORIZONTAL;
    JLabel label = new JLabel(mainMessage, icon, SwingConstants.CENTER);
    cp.add(label, c1);

    // Message pane
    c1.gridy++;
    c1.fill = GridBagConstraints.BOTH;
    c1.weighty = 1.0;
    XMLTag root = new XMLTag("html");
    XMLTag tag;
    root.add(tag = new XMLTag("style"));
    tag.addAttr("type", "text/css");
    tag.add(new XMLTextTag("body: {font-size: 0.8em}"));
    XMLTag list = new XMLTag("ul");
    root.add(list);
    XMLTag entry;
    for (String s : notes) {
      list.add(entry = new XMLTag("li"));
      entry.add(new XMLTextTag(s));
    }
    JEditorPane editorPane = new JEditorPane("text/html", root.toXMLString());
    editorPane.setEditable(false);
    cp.add(new JScrollPane(editorPane), c1);

    // Expand/collapse button
    c1.gridy++;
    c1.anchor = GridBagConstraints.CENTER;
    c1.weighty = 0.0;
    JPanel buttonPanel = new JPanel();
    cp.add(buttonPanel, c1);
    buttonPanel.add(new JButton(new AbstractAction("Close") {
	public void actionPerformed(ActionEvent e) {
	  dispose();
	}
      }));

    this.setContentPane(cp);
    this.pack();
  }

}
