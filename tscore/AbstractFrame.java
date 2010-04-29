package tscore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import regatta.Regatta;
import regatta.RegattaEvent;
import regatta.RegattaEvent.RegattaEventType;
import regatta.RegattaListener;
import javax.swing.ImageIcon;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.awt.Component;

/**
 * Parent of all display frames in TechScore such as those that
 * display rotations or scores. This class extends JFrame and provides
 * a mechanism for printing and saving the dialog's content, which is
 * defined in <code>getDialogContent</code>, and is expected to be in
 * text/html form.
 *
 * The dialogs consist of a toolbar which is located at the top of the
 * frame's content pane. Children of this class should implement the
 * <code>getContentComponent</code> method which returns the component
 * to be added to the center of the frame's content pane.
 *
 * In addition, children of this class must implement the regatta
 * listener interface.
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
 * Created: Thu Jul 16 11:46:49 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public abstract class AbstractFrame
  extends JFrame implements Printable, RegattaListener {

  protected Regatta regatta;
  protected AbstractAction saveAction, printAction;

  /**
   * The style string (CSS) to include in HTML files.
   *
   */
  protected String styleString;

  /**
   * Creates a new <code>AbstractFrame</code> instance.
   *
   */
  public AbstractFrame(JFrame parent, String name) {
    super(name);
    
    // Toolbar
    JToolBar tb = new JToolBar("Rotations toolbar");
    tb.add(new JButton(this.printAction = new PrintAction()));
    tb.add(new JButton(this.saveAction = new SaveAction()));
    this.getContentPane().add(tb, BorderLayout.PAGE_START);
    this.setIconImage(new ImageIcon(getClass().getResource("img/" + name + "Icon.png")).getImage());

    // Initialize components
    this.getContentPane().add(this.getContentComponent(), BorderLayout.CENTER);
    this.pack();

    // Stylesheet
    StringBuilder contents = new StringBuilder();
    String sep = System.getProperty("line.separator");
    try {
      InputStream is = this.getClass().getResourceAsStream("inc/report.css");
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      try {
	String line = null;
	while ((line = reader.readLine()) != null) {
	  contents.append(line);
	  contents.append(sep);
	}
      }
      finally {
	reader.close();
      }
    } catch (IOException e) {
      System.err.println(e);
    } catch (Exception e) {
      System.err.println(e);
    }

    this.styleString = contents.toString();
  }

  /**
   * Method to be implemented by children of this class. The returned
   * String is used to save the dialog content to file, among other
   * actions.
   *
   * @return a <code>String</code> value
   */
  protected abstract String getDialogContent();

  /**
   * Returns the component to add to this fram's content pane.
   *
   * @return a <code>Component</code> value
   */
  protected abstract Component getContentComponent();

  /**
   * Initializes the content of the dialog. This method is called
   * whenever the regatta for this frame is changed.
   *
   */
  protected abstract void fill();

  /**
   * Sets the regatta that this dialog will use.
   *
   * @param reg a <code>Regatta</code> value
   */
  public void setRegatta(Regatta reg) {
    this.regatta = reg;
    boolean nonNull = this.regatta != null;
    if (nonNull) {
      this.regatta.addRegattaListener(this);
    }
    this.printAction.setEnabled(nonNull);
    this.saveAction.setEnabled(nonNull);
    this.fill();
  }

  /**
   * Updates the content of this frame
   *
   */
  protected abstract void update();

  // Implementation of tscore.RegattaListener
  public abstract void regattaChange(RegattaEvent e);

  /**
   * Prints the <code>content</code> pane on one page.
   *
   * @param graphics a <code>Graphics</code> value
   * @param pageFormat a <code>PageFormat</code> value
   * @param n an <code>int</code> value
   * @return an <code>int</code> value
   * @exception PrinterException if an error occurs
   */
  public final int print(final Graphics graphics,
			 final PageFormat pageFormat,
			 final int n) throws PrinterException {
    if (n > 0)
      return NO_SUCH_PAGE;

    // Translate imageable area
    Graphics2D g2d = (Graphics2D)graphics;
    g2d.translate(pageFormat.getImageableX(),
		  pageFormat.getImageableY());

    // Render
    this.getContentComponent().paint(graphics);

    return PAGE_EXISTS;
  }

  public void setVisible(boolean flag) {
    super.setVisible(flag);
    if (flag) {
      this.fill();
    }
  }

  /**
   * Print the dialog
   */
  protected class PrintAction extends AbstractAction {
    PrintAction() {
      super("Print");
    }
    public void actionPerformed(ActionEvent evt) {
      // printing
      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPrintable(AbstractFrame.this);

      boolean doPrint = job.printDialog();
      if (doPrint) {
	try {
	  job.print();
	}
	catch (PrinterException e) {
	  System.err.println("Could not print.");
	}
      }
    }
  }

  class SaveAction extends AbstractAction {
    SaveAction() {
      super("Save to file");
    }
    public void actionPerformed(ActionEvent evt) {
      JFileChooser fc = new JFileChooser();
      int returnVal = fc.showSaveDialog(AbstractFrame.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
	File f = fc.getSelectedFile();

	try {
	  FileWriter fstream = new FileWriter(f);
	  BufferedWriter out = new BufferedWriter(fstream);
	  out.write(getDialogContent());
	  out.close();
	} catch (IOException e) {
	  System.err.println("Unable to save to file.");
	}
      }
    }
  }
}
