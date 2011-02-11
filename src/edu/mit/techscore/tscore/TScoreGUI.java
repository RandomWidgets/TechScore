package edu.mit.techscore.tscore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import edu.mit.techscore.regatta.Regatta;
import edu.mit.techscore.regatta.RegattaEvent;
import edu.mit.techscore.regatta.RegattaEvent.RegattaEventType;
import edu.mit.techscore.regatta.RegattaListener;
import edu.mit.techscore.regatta.Scorer;
import edu.mit.techscore.tscore.PaneChangeEvent.About;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.io.IOException;
import edu.mit.techscore.nscore.NavyScoreLoader;
import javax.swing.SwingUtilities;

/**
 * Main GUI class for TechScore desktop program
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
 * Created: Wed Jun  3 12:09:22 2009
 *
 * @author Dayan Paez
 * @version 1.3.4
 */

public class TScoreGUI
  extends JFrame
  implements RegattaListener, WindowListener, PropertyChangeListener {
  
  // Static variables
  public static String VERSION = "1.5";
  public static String FILE_EXTENSION = "tsr";
  public static String NAVY_EXTENSION = "sco";

  // Variables
  private JScrollPane navPanel;
  private Regatta regatta;
  private File regattaFile;
  private JFileChooser fc;
  private RegattaFileFilter fileFilter;
  private RegattaIO regIO;
  private boolean hasUnsaved;

  // Windows and panes
  private JToolBar toolBar;
  private JPanel contentPanel;
  private AbstractFrame rotationDialog, scoreDialog;
  private IntroductionDialog helpDialog;
  private AbstractPane [] editPanes;
  private EditPaneActionGroup.EditPaneAction defaultPaneAction;
  private final JDialog aboutDialog;
  private final JComponent statusBar;
  private JProgressBar progressBar;

  // Actions
  private SaveRegattaAction srAction;
  private SaveAsRegattaAction saAction;
  private CloseRegattaAction crAction;
  private QuitAction quAction;
  private ToggleDialogAction sRotAction, sScoreAction, sHelpAction;

  // Preferences
  private Scorer scorer;

  /**
   * Creates a new <code>TScoreGUI</code> instance.
   *
   */
  public TScoreGUI() {
    super("TechScore");

    this.scorer = new ICSAScorer();

    this.setMinimumSize(new Dimension(300, 300));
    this.loadPreferences();
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.fileFilter = new RegattaFileFilter();
    this.regIO = new RegattaIO();
    this.regIO.addPropertyChangeListener(this);
    this.fc = new JFileChooser();
    this.fc.setFileFilter(fileFilter);
    // this.fc.addChoosableFileFilter(new NavyFileFilter());
    this.setIconImage(new ImageIcon(getClass().getResource("img/icon.png")).getImage());

    // INIT COMPONENTS

    // Windows
    this.rotationDialog = new RotationsDialog(this, regatta);
    this.scoreDialog    = new ScoresDialog(this, regatta);
    this.sRotAction     = new ToggleDialogAction("View rotations",
						 this.rotationDialog);
    this.sScoreAction   = new ToggleDialogAction("View scores",
						 this.scoreDialog);

    // Help Window
    this.helpDialog     = new IntroductionDialog(this);
    this.sHelpAction    = new ToggleDialogAction("Quick guide", this.helpDialog);

    this.setContentPane(new JPanel(new BorderLayout()));

    // Menu
    toolBar = new JToolBar("Edit");
    JMenuBar mb = new JMenuBar();
    JMenu menu;
    AbstractPane   pane;
    EditPaneActionGroup.EditPaneAction act;
    Action [] mAndT = {new NewRegattaAction(),
		       new OpenRegattaAction(),
		       new SaveRegattaAction()};

    // -File
    menu = new JMenu("File", false);
    menu.setMnemonic(KeyEvent.VK_F);
    menu.add(new JMenuItem(mAndT[0]));
    menu.add(new JMenuItem(mAndT[1]));
    menu.addSeparator();
    menu.add(new JMenuItem(mAndT[2]));
    this.srAction = (SaveRegattaAction)mAndT[2];
    menu.add(new JMenuItem(this.saAction = new SaveAsRegattaAction()));
    menu.add(new JMenuItem(this.crAction = new CloseRegattaAction()));
    menu.addSeparator();
    menu.add(new JMenuItem(this.quAction = new QuitAction()));
    mb.add(menu);

    JButton button;
    for (int i = 0; i < mAndT.length; i++) {
      toolBar.add(button = new JButton(mAndT[i]));
      button.setText("");
    }
    toolBar.addSeparator();

    // -Edit
    menu = new JMenu("Edit", false);
    menu.setMnemonic(KeyEvent.VK_E);
    mb.add(menu);
    RPFormPane rpPane = new RPFormPane(this.regatta);
    RPDatabasePane dbPane = new RPDatabasePane(this.regatta);

    editPanes = new AbstractPane [] {new DetailsPane(this.regatta),
				     new DailySummaryPane(this.regatta),
				     new RacesPane(this.regatta),
				     new RotationsPane(this.regatta),
				     new TweakRotationsPane(this.regatta),
				     new FinishesPane(this.regatta),
				     new PenaltiesPane(this.regatta),
				     new TeamPenaltiesPane(this.regatta),
				     new BreakdownsPane(this.regatta),
				     rpPane, dbPane};
    KeyStroke [] keys = {KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0),
			 KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)};
    
    EditPaneActionGroup group = new EditPaneActionGroup();
    
    EditPaneActionGroup. EditPaneAction [] paneActions =
      new EditPaneActionGroup.EditPaneAction[editPanes.length];
    for (int i = 0; i < editPanes.length; i++) {
      paneActions[i] = group.new EditPaneAction(editPanes[i], keys[i]);
    }

    // Menu
    for	(EditPaneActionGroup.EditPaneAction a: paneActions) {
      if (a.getPane() instanceof RotationsPane ||
	  a.getPane() instanceof FinishesPane  ||
	  a.getPane() instanceof RPFormPane) {
	menu.addSeparator();
      }

      JRadioButtonMenuItem tmenu;
      menu.add(tmenu = new JRadioButtonMenuItem(a));
      a.register(tmenu);
    }

    // Toolbar
    EditPaneActionGroup.
      EditPaneAction [] toolbarActs = {paneActions[0],
				       paneActions[2],
				       paneActions[3],
				       paneActions[5],
				       paneActions[6],
				       paneActions[8]};
    for (EditPaneActionGroup.EditPaneAction a: toolbarActs) {
      JToggleButton tbutton;
      toolBar.add(tbutton = new JToggleButton(a));
      tbutton.setText("");
      a.register(tbutton);
    }

    // Set the editing pane as the default
    this.defaultPaneAction = paneActions[0];


    // -View
    JCheckBoxMenuItem cbItem;
    menu = new JMenu("View", false);
    menu.setMnemonic(KeyEvent.VK_V);
    cbItem = new JCheckBoxMenuItem(this.sRotAction);
    this.sRotAction.addButton(cbItem);
    menu.add(cbItem);
    cbItem = new JCheckBoxMenuItem(this.sScoreAction);
    this.sScoreAction.addButton(cbItem);
    menu.add(cbItem);

    menu.addSeparator();
    cbItem = new JCheckBoxMenuItem(new ToggleToolBar());
    cbItem.setSelected(toolBar.isVisible());
    menu.add(cbItem);
    mb.add(menu);

    // -Help
    menu = new JMenu("Help", false);
    menu.setMnemonic(KeyEvent.VK_H);
    //   -Quick guide
    cbItem = new JCheckBoxMenuItem(this.sHelpAction);
    this.sHelpAction.addButton(cbItem);
    menu.add(cbItem);
    //   -About
    this.aboutDialog = new AboutDialog(this);
    menu.add(new JMenuItem(new AbstractAction("About...") {
	public void actionPerformed(ActionEvent evt) {
	  aboutDialog.setVisible(true);
	}
      }));
    mb.add(menu);

    this.setJMenuBar(mb);
    this.getContentPane().add(toolBar, BorderLayout.PAGE_START);
    this.toolBar.setOrientation(JToolBar.HORIZONTAL);

    // Statusbar
    this.statusBar = new JPanel(new BorderLayout());
    this.statusBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    this.statusBar.setPreferredSize(new Dimension(100, 18));
    this.getContentPane().add(statusBar, BorderLayout.PAGE_END);

    // Ready for viewing
    this.addWindowListener(this);
    this.pack();
  }

  /**
   * Opens regatta file and loads regatta.
   *
   * @param f a <code>File</code> describing a regatta
   */
  public void loadFile(final File f) {
    final Runnable thd = new Runnable() {
	public void run() {
	  try {
	    boolean success = regIO.readFile(f);

	    // Save open regatta?
	    if (TScoreGUI.this.regatta != null) {
	      TScoreGUI.this.crAction.
		actionPerformed(new ActionEvent(this,
						ActionEvent.ACTION_PERFORMED,
						"Open regatta"));
	    }

	    TScoreGUI.this.hasUnsaved = false;
	    TScoreGUI.this.regattaFile = f;
	    TScoreGUI.this.saAction.setEnabled(true);
	    TScoreGUI.this.crAction.setEnabled(true);
	    TScoreGUI.this.setRegatta(regIO.getRegatta());
	  
	    if (!success) {
	      String mes = "There were problems while loading file.\n" +
		"You should inspect the regatta for corrupt data.";
	      JDialog errorDialog =
		new ErrorMessageDialog(TScoreGUI.this,
				       JOptionPane.WARNING_MESSAGE,
				       mes,
				       "Warning",
				       regIO.getWarnings());

	      errorDialog.setVisible(true);
	    }    
	  } catch (IllegalArgumentException e) {
	    JDialog errorDialog = new ErrorMessageDialog(TScoreGUI.this,
							 "Unable to load file.",
							 "File error",
							 regIO.getErrors());
	    errorDialog.setVisible(true);
	  }
	}
      };
    SwingUtilities.invokeLater(thd);
  }

  /**
   * Sets the current regatta
   */
  private void setRegatta(Regatta r) {
    this.regatta = r;
    this.updateTitle();
    if (this.regatta != null) {
      this.regatta.addRegattaListener(this);
      this.saAction.setEnabled(true);
      this.crAction.setEnabled(true);
    }
    else {
      this.setContent(new JPanel());
      this.hasUnsaved = false;
      this.srAction.setEnabled(false);
      this.saAction.setEnabled(false);
    }

    // Update panes
    for (AbstractPane pane: editPanes) {
      pane.setRegatta(r);
    }

    // Update dialogs
    this.rotationDialog.setRegatta(r);
    this.scoreDialog.setRegatta(r);

    // Select default pane
    if (r != null)
      this.defaultPaneAction.
	actionPerformed(new ActionEvent(TScoreGUI.this.defaultPaneAction,
					ActionEvent.ACTION_PERFORMED,
					"Open regatta"));
  }

  /**
   * Sets the content of this frame
   */
  public void setContent(JPanel comp) {
    if (contentPanel != null) {
      this.getContentPane().remove(contentPanel);
    }
    contentPanel = comp;
    JPanel c = (JPanel)this.getContentPane();
    c.add(contentPanel, BorderLayout.CENTER);
    c.revalidate();
    c.repaint();
  }

  /**
   * Updates the title bar for the frame
   */
  private void updateTitle() {
    if (this.regatta == null) {
      this.setTitle("TechScore");
      return;
    }
    String rep = this.regatta.getName();
    if (this.regattaFile != null) {
      rep += " (" + this.regattaFile.getName() + ")";
    }
    if (this.hasUnsaved) {
      rep = "* " + rep;
    }
    this.setTitle(rep + " | TechScore");
  }

  /**
   * Creates the rotation dialog
   */
  private JDialog createRotationDialog() {
    String title = (this.regatta == null) ?
      "No regatta" : this.regatta.getName();
    title += " | Rotations";

    JDialog dialog = new JDialog(this, title);
    dialog.setMinimumSize(new Dimension(400, 600));
    dialog.setLocationRelativeTo(this);

    return dialog;
  }

  /**
   * Regatta change listener
   */
  public void regattaChange(RegattaEvent e) {
    RegattaEventType type = e.getChangeType();
    this.hasUnsaved = true;
    this.srAction.setEnabled(true);
    this.updateTitle();
  }

  /**
   * Class for filtering TechScore files
   */
  class RegattaFileFilter extends FileFilter {
    public boolean accept(File f) {
      if (f.isDirectory()) {
	return true;
      }

      String ext = Factory.getExtension(f);
      if (ext != null) {
	if (ext.equals(TScoreGUI.FILE_EXTENSION)) {
	  return true;
	}
      }

      return false;
    }
    public String getDescription() {
      return "TechScore Regatta";
    }
  }

  class NavyFileFilter extends FileFilter {
    public boolean accept(File f) {
      if (f.isDirectory()) {
	return true;
      }

      String ext = Factory.getExtension(f);
      if (ext != null) {
	return ext.equals(TScoreGUI.NAVY_EXTENSION);
      }
      return false;
    }

    public String getDescription() {
      return "NavyScoring Regatta";
    }
  }

  /**
   * Uses the program's scorer to score the regatta
   *
   */
  public void doScore() {
    if (this.regatta == null) {
      return;
    }

    this.scorer.score(this.regatta);
  }

  /**
   * Gets the scorer currently in use for the program.
   *
   * @return a <code>Scorer</code> value
   */
  public Scorer getScorer() {
    return this.scorer;
  }

  
  /* Action classes */
  
  /**
   * Save As regatta action: unconditional asks for a file to save
   */
  class SaveAsRegattaAction extends AbstractAction {
    SaveAsRegattaAction() {
      super("Save As...");
      putValue(SHORT_DESCRIPTION, "Save regatta to file.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
						       InputEvent.CTRL_MASK +
						       InputEvent.SHIFT_MASK));
      if (TScoreGUI.this.regatta == null)
	this.setEnabled(false);
    }
    public void actionPerformed(ActionEvent ev) {
      
      int returnVal = fc.showSaveDialog(TScoreGUI.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
	File f = fc.getSelectedFile();
	String ext = Factory.getExtension(f);
	if (ext == null || !ext.equalsIgnoreCase(TScoreGUI.FILE_EXTENSION)) {
	  f = new File(f.getPath() + "." + TScoreGUI.FILE_EXTENSION);
	}

	if (regattaFile == null) {
	  if (regIO.writeFile(regatta, f)) {
	    TScoreGUI.this.regattaFile = f;
	    TScoreGUI.this.hasUnsaved = false;
	    TScoreGUI.this.srAction.setEnabled(false);
	    TScoreGUI.this.updateTitle();
	  }
	  else {
	    JOptionPane.showMessageDialog(TScoreGUI.this,
					  "Unable to save file.",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	    System.err.println(Factory.implode(regIO.getErrors(), "\n"));
	  }
	}
	else {
	  // Rewrite
	  if (regIO.rewriteFile(regatta, regattaFile, f)) {
	    TScoreGUI.this.regattaFile = f;
	    TScoreGUI.this.hasUnsaved = false;
	    TScoreGUI.this.srAction.setEnabled(false);
	    TScoreGUI.this.updateTitle();
	  }
	  else {
	    JOptionPane.showMessageDialog(TScoreGUI.this,
					  "Unable to save file.",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	    System.err.println(Factory.implode(regIO.getErrors(), "\n"));
	  }
	}

      }
    }
  }

  /**
   * Save a regatta action: if there is a file associated with the
   * current regatta, use that file, otherwise prompt for one
   */
  class SaveRegattaAction extends AbstractAction {
    SaveRegattaAction() {
      super("Save");
      putValue(SMALL_ICON, Factory.getImageIcon("img/Save16.gif", "Save"));
      putValue(SHORT_DESCRIPTION, "Save regatta.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
						       InputEvent.CTRL_MASK));
      if (TScoreGUI.this.regatta == null ||
	  !TScoreGUI.this.hasUnsaved) {
	this.setEnabled(false);
      }
    }
    public void actionPerformed(ActionEvent ev) {

      if (TScoreGUI.this.regattaFile == null) {
	// Revert to Save As...
	TScoreGUI.this.saAction.
	  actionPerformed(new ActionEvent(this,
					  ActionEvent.ACTION_PERFORMED,
					  "No file to save"));
      }
      else {
	regIO.setRegatta(regatta);
	if (regIO.rewriteFile(regatta,
			      TScoreGUI.this.regattaFile,
			      TScoreGUI.this.regattaFile)) {
	  TScoreGUI.this.hasUnsaved = false;
	  this.setEnabled(false);
	  TScoreGUI.this.updateTitle();
	}
	else {
	  System.err.println("could not save.");
	}
      }
    }
  }

  /**
   * Open regatta action
   */
  class OpenRegattaAction extends AbstractAction {
    OpenRegattaAction() {
      super("Open...");
      putValue(SMALL_ICON, Factory.getImageIcon("img/Open16.gif", "Open"));
      putValue(SHORT_DESCRIPTION, "Open saved regatta.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
						       InputEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent ev) {
      int returnVal = fc.showOpenDialog(TScoreGUI.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {

	File f = fc.getSelectedFile();
	String ext = Factory.getExtension(f);
	System.out.println("Extension: " + ext);
	// If no extension, assume TechScore file
	if (ext == null) {
	  f = new File(f.getPath() + TScoreGUI.FILE_EXTENSION);
	  TScoreGUI.this.loadFile(f);
	}
	else if (ext.equalsIgnoreCase(TScoreGUI.NAVY_EXTENSION)) {
	  try {
	    NavyScoreLoader loader = new NavyScoreLoader();
	    TScoreGUI.this.setRegatta(loader.loadFile(f));
	  } catch (Exception e) {
	    JOptionPane.showMessageDialog(TScoreGUI.this,
					  "Unable to open NavyScoring file.",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	  } finally {
	    return;
	  }
	}
	// Assume TechScore
	TScoreGUI.this.loadFile(f);
      }
    }
  }

  /**
   * New regatta action
   */
  class NewRegattaAction extends AbstractAction {
    NewRegattaAction() {
      super("New");
      putValue(SMALL_ICON, Factory.getImageIcon("img/New16.gif", "New"));
      putValue(SHORT_DESCRIPTION, "Create a new regatta");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
						       InputEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent ev) {
      TScoreGUI.this.crAction.
	actionPerformed(new ActionEvent(this,
					ActionEvent.ACTION_PERFORMED,
					"New regatta"));
      TScoreGUI.this.setRegatta(Factory.newRegatta());
      TScoreGUI.this.regattaFile = null;
      	  
      // Select details pane
      TScoreGUI.this.defaultPaneAction.
	actionPerformed(new ActionEvent(TScoreGUI.this.defaultPaneAction,
					ActionEvent.ACTION_PERFORMED,
					"New regatta"));
    }
  }

  /**
   * Close action
   */
  class CloseRegattaAction extends AbstractAction {
    CloseRegattaAction() {
      super("Close");
      putValue(SHORT_DESCRIPTION, "Close this regatta.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W,
						       InputEvent.CTRL_MASK));
      if (TScoreGUI.this.regatta == null) {
	this.setEnabled(false);
      }

    }
    public void actionPerformed(ActionEvent ev) {
      if (TScoreGUI.this.hasUnsaved) {
	String mes = "Save the current regatta?";
	int n = JOptionPane.showConfirmDialog(TScoreGUI.this,
					      mes,
					      "Unsaved changes",
					      JOptionPane.YES_NO_OPTION);
	if (n == JOptionPane.YES_OPTION) {
	  TScoreGUI.this.srAction.
	    actionPerformed(new ActionEvent(this,
					    ActionEvent.ACTION_PERFORMED,
					    "Save"));
	}
      }
      // Close regatta
      this.setEnabled(false);
      TScoreGUI.this.setRegatta(null);
    }
  }

  /**
   * Quit action
   */
  class QuitAction extends AbstractAction {
    QuitAction() {
      super("Quit");
      putValue(SHORT_DESCRIPTION, "Quit TechScore.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						       InputEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent ev) {
      TScoreGUI.this.crAction.
	actionPerformed(new ActionEvent(this,
					ActionEvent.ACTION_PERFORMED,
					"Close"));
      TScoreGUI.this.savePreferences();
      // Ask the windows to save preferences, too
      TScoreGUI.this.sRotAction.savePreferences();
      TScoreGUI.this.sScoreAction.savePreferences();

      // Exit
      System.exit(0);
    }
  }

  /**
   * Show rotation dialog
   */
  public class ToggleDialogAction
    extends AbstractAction
    implements WindowListener {

    private AbstractFrame window;
    private ArrayList<AbstractButton> toggleButtons;

    // Preferences
    private Preferences prefs;
    private Point location;
    private Dimension size;
    private boolean isShowing;
    
    public ToggleDialogAction(String name, AbstractFrame w) {
      super(name);
      this.toggleButtons = new ArrayList<AbstractButton>();
      this.window = w;

      // Open prefrences for this window
      this.prefs = Preferences.userNodeForPackage(this.getClass());
      this.loadPreferences(true);
      
      this.window.addWindowListener(this);
    }
    public void addButton(AbstractButton button) {
      this.toggleButtons.add(button);
      button.setSelected(this.window.isVisible());
    }
    public void actionPerformed(ActionEvent ev) {
      AbstractButton button = (AbstractButton)ev.getSource();
      if (button.isSelected())
	this.window.setVisible(true);
      else {
	this.savePreferences(false);
	this.window.dispose();
      }
    }

    private void notifyButtons(boolean state) {
      for (AbstractButton but : toggleButtons) {
	but.setSelected(state);
      }
    }
    
    /*
     * Window listener events
     */
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {};
    public void windowClosing(WindowEvent e) {
      this.notifyButtons(false);
      this.savePreferences(false);
    }
    public void windowOpened(WindowEvent e) {
      this.notifyButtons(true);
      this.loadPreferences();
    }
    public void windowClosed(WindowEvent e) {};
    public void windowIconified(WindowEvent e) {};
    public void windowDeiconified(WindowEvent e) {};

    private void savePreferences() {
      this.savePreferences(this.window.isVisible());
    }

    private void savePreferences(boolean isVis) {
      if (this.window.isVisible()) {
	// Override using current location's setting
	this.location = this.window.getLocationOnScreen();
	this.size     = this.window.getSize();
      }
      this.isShowing = isVis;

      String p = (int)location.getX() + "," +
	(int)location.getY() + "," +
	(int)size.getWidth() + "," +
	(int)size.getHeight() + "," + isVis;
      prefs.put(this.window.getTitle().toUpperCase(), p);
    }

    public void loadPreferences() {
      this.loadPreferences(false);
    }

    public void loadPreferences(boolean overrideVisibility) {
      String def = "400,0,400,600,false";
      String p = prefs.get(this.window.getTitle().toUpperCase(), def);

      try {
	parsePref(p);
      } catch (IllegalArgumentException e) {
	parsePref(def);
      } finally {
	this.window.setLocation(this.location);
	this.window.setPreferredSize(this.size);
	if (overrideVisibility) {
	  this.window.setVisible(this.isShowing);
	  this.notifyButtons(this.isShowing);
	}

	this.window.pack();
      }
      
    }

    /**
     * Updates the location, dimension, and showing variables for this
     * window so that they reflect the values in 'def'
     *
     * @param def a <code>String</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    private void parsePref(String def)
      throws IllegalArgumentException {

      // First two are locations
      String [] tokens = def.split(",");
      if (tokens.length < 5) {
	throw new IllegalArgumentException("Preference expects 5 params.");
      }
      try {
	this.location = new Point(Integer.parseInt(tokens[0]),
				  Integer.parseInt(tokens[1]));
	this.size     = new Dimension(Integer.parseInt(tokens[2]),
				      Integer.parseInt(tokens[3]));
	this.isShowing= Boolean.parseBoolean(tokens[4]);
      } catch (Exception e) {
	throw new IllegalArgumentException(e.getMessage());
      }
    }
  }

  /**
   * Show toolbar
   */
  class ToggleToolBar extends AbstractAction {
    ToggleToolBar() {
      super("Show toolbar");
      putValue(SHORT_DESCRIPTION, "Show/hide edit toolbar.");
    }
    public void actionPerformed(ActionEvent ev) {
      AbstractButton button = (AbstractButton)ev.getSource();
      TScoreGUI.this.toolBar.setVisible(button.isSelected());
    }
  }

  class EditPaneActionGroup {
    private EditPaneAction currentAction;
    private ArrayList<EditPaneAction> actions;
    
    public EditPaneActionGroup() {
      this.currentAction = null;
      this.actions = new ArrayList<EditPaneAction>();
    }

    /**
     * Selects the specified action found in this group, while
     * deselecting all others. But first asks the current one if
     * it's alright to do so.
     *
     * @param act an <code>EditPaneAction</code> value
     */
    public void select(EditPaneAction selectedAction) {
      if (this.currentAction != null) {
	// should continue with the selection process?
	if (!this.currentAction.getPane().empty()) {
	  for (EditPaneAction act : this.actions) {
	    if (act != this.currentAction) {
	      act.setSelected(false);
	    }
	  }
	  return;
	}
      }

      for (EditPaneAction act : this.actions) {
	if (act == selectedAction) {
	  act.setSelected(true);
	}
	else {
	  act.setSelected(false);
	}
      }
      this.currentAction = selectedAction;
      TScoreGUI.this.
	helpDialog.paneChanged(new PaneChangeEvent(this.currentAction.getPane(),
						   PaneChangeEvent.About.VISIBILITY,
						   true));
    }

    /**
     * Edit pane action. Each of these actions takes one AbstractPane
     * and its action is to set that pane as the content pane of the
     * main frame.
     * <p>
     *
     * Meanwhile, if in so doing an existing pane is to be replaced,
     * then first prompt for that pane's <code>empty</code> status.
     */
    class EditPaneAction extends AbstractAction
      implements PaneChangeListener {
    
      private AbstractPane editPane;
      private ArrayList<AbstractButton> components;
      EditPaneAction(AbstractPane p) {
	this(p, null);
      }
      EditPaneAction(AbstractPane p, KeyStroke k) {
	super(p.toString());
	this.editPane = p;
	this.editPane.addPaneChangeListener(this);
	this.setEnabled(this.editPane.isEnabled());

	putValue(SHORT_DESCRIPTION, p.toString());
	putValue(SMALL_ICON, p.getIcon());
	putValue(Factory.SELECTED_KEY, new Boolean(false));
	putValue(ACCELERATOR_KEY, k);

	// Register this action with daddy
	EditPaneActionGroup.this.actions.add(this);
	this.components = new ArrayList<AbstractButton>();
      }
      
      protected final void register(AbstractButton button) {
	this.components.add(button);
      }

      public void setSelected(boolean isSelected) {
	this.putValue(Factory.SELECTED_KEY, new Boolean(isSelected));
	for (int i = 0; i < this.components.size(); i++) {
	  this.components.get(i).setSelected(isSelected);
	}
	if (isSelected) {
	  this.editPane.fill();
	  TScoreGUI.this.setContent(this.editPane);
	}
      }

      public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	if (source instanceof Action) {
	  EditPaneActionGroup.this.select(this);
	}
	else {
	  AbstractButton button = (AbstractButton)source;
	  boolean isSelected = button.isSelected();

	  if (isSelected) {
	    EditPaneActionGroup.this.select(this);
	  }
	  else {
	    button.setSelected(true);
	  }
	}
      }
    
      /**
       * Listen to changes from the pane
       */
      public void paneChanged(PaneChangeEvent e) {
	if (e.getAbout() == PaneChangeEvent.About.AVAILABILITY) {
	  this.setEnabled(e.getValue());
	}
      }

      public AbstractPane getPane() {
	return this.editPane;
      }
    }

  }

  private void savePreferences() {
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    // Location
    Point loc = this.getLocationOnScreen();
    prefs.putInt("LOCX", (int)loc.getX());
    prefs.putInt("LOCY", (int)loc.getY());

    // Window size
    Dimension dim = this.getSize();
    prefs.putInt("WIDTH", (int)dim.getWidth());
    prefs.putInt("HEIGHT", (int)dim.getHeight());
  }

  private void loadPreferences() {
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    // Location
    int x = prefs.getInt("LOCX", 5);
    int y = prefs.getInt("LOCY", 5);
    this.setLocation(x, y);

    // Window size
    int w = prefs.getInt("WIDTH", 400);
    int h = prefs.getInt("HEIGHT", 600);
    this.setPreferredSize(new Dimension(w, h));
  }

  // Implementation of WindowListener
  
  public void windowActivated(WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {};
  public void windowClosing(WindowEvent e) {
    // Proceed with closing sequence
    this.quAction.actionPerformed(new ActionEvent(this,
						  ActionEvent.ACTION_PERFORMED,
						  "Closing the window."));
  }
  public void windowOpened(WindowEvent e) {};
  public void windowClosed(WindowEvent e) {};
  public void windowIconified(WindowEvent e) {};
  public void windowDeiconified(WindowEvent e) {};

  // Implementation of property change listener

  /**
   * Listens to progress made by regatta loaders, scorers, etc.
   *
   * @param e a <code>PropertyChangeEvent</code> value
   */
  private static int progValue = 0;
  public void propertyChange(PropertyChangeEvent evt) {

    String propName = evt.getPropertyName();
    try {
      this.statusBar.removeAll();
      this.statusBar.setLayout(new BorderLayout());
      if (propName.equalsIgnoreCase("done")) {
	// Start/end the progress bar in the status bar
	if ((Boolean)evt.getNewValue()) {
	  this.statusBar.add(new JLabel("Ready"),
			     BorderLayout.CENTER);
	}
      }
      else {
	// Update the progress bar
	this.statusBar.add(new JLabel(String.valueOf(evt.getNewValue())),
			   BorderLayout.CENTER);
      }
    } catch (NullPointerException e) {
      System.err.println("Missing progress bar");
    } catch (ClassCastException e) {
      System.err.println("Expected a boolean value");
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    final TScoreGUI gui = new TScoreGUI();
    if (args.length > 0) {
      File f = new File(args[0]);
      if (f.canRead())
	gui.loadFile(f);
    }

    java.awt.EventQueue.invokeLater(new Runnable() {
	public void run() {
	  gui.setVisible(true);
	}
      });
  }
}
