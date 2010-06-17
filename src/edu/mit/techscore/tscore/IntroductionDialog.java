/**
 * This file is part of TechScore.<p>
 * 
 * TechScore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<p>
 * 
 * TechScore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<p>
 * 
 * You should have received a copy of the GNU General Public License
 * along with TechScore.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edu.mit.techscore.tscore;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.mit.techscore.dpxml.XMLTag;
import edu.mit.techscore.dpxml.XMLTextTag;
import edu.mit.techscore.regatta.RegattaEvent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * Describe class IntroductionDialog here.
 *
 *
 * Created: Sat Jun 12 14:26:09 2010
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class IntroductionDialog extends AbstractFrame implements PaneChangeListener {

  /**
   * Creates a new <code>IntroductionDialog</code> instance.
   *
   */
  public IntroductionDialog(JFrame owner) {
    super(owner, "Quick Help");
    
    XMLTag root = new XMLTag("html");
    root.add(new XMLTag("h1", new XMLTag[] {new XMLTextTag("Welcome to TechScore")}));
    this.text = root.toXMLString();
  }

  protected Component getContentComponent() {
    if (this.scrollpane == null) {
      this.content = new JEditorPane("text/html", "");;
      this.content.setEditable(false);
      this.scrollpane = new JScrollPane(content);
    }
    return this.scrollpane;
  }
  
  protected String getDialogContent() {
    return this.text;
  }
  public void regattaChange(RegattaEvent evt) {}
  public void update() {}
  public void fill() {
    this.content.setText(this.text);
  }

  /**
   * Use to notify of which editing pane's content to include. If the
   * pane is not supported, no change occurs
   *
   * @param pane the pane whose content to add
   */
  public void paneChanged(PaneChangeEvent evt) {
    if (!this.isVisible())
      return;
    
    AbstractPane pane = evt.getSource();
    if (pane == this.curPane)
      return;

    // Get content of the file in question
    String filename = null;
    if      (pane instanceof DetailsPane)        filename = "details";
    else if (pane instanceof DailySummaryPane)   filename = "summaries";
    else if (pane instanceof RacesPane)          filename = "races";
    else if (pane instanceof RotationsPane)      filename = "rotations";
    else if (pane instanceof TweakRotationsPane) filename = "tweak";
    else if (pane instanceof FinishesPane)       filename = "finishes";
    else if (pane instanceof PenaltiesPane)      filename = "penalties";
    else if (pane instanceof TeamPenaltiesPane)  filename = "team-penalties";
    else if (pane instanceof BreakdownsPane)     filename = "breakdowns";
    else if (pane instanceof RPFormPane)         filename = "rp";
    else if (pane instanceof RPDatabasePane)     filename = "rp-database";

    if (filename == null)
      return;

    try {
      filename = String.format("help/%s.html", filename);
      URL url = this.getClass().getResource(filename);
      BufferedReader reader = new BufferedReader(new FileReader(url.getPath()));
      StringBuilder builder = new StringBuilder();
      while (reader.ready())
	builder.append(reader.readLine());
      reader.close();
      this.text = builder.toString();
      this.fill();
    } catch (Exception e) {
      System.err.println("Unable to find help file for pane " + pane);
    }

    this.curPane = pane;
  }

  private AbstractPane curPane;
  private String text;
  private JScrollPane scrollpane;
  private JEditorPane content;
}
