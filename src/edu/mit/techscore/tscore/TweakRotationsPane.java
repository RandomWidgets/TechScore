package tscore;

import regatta.Regatta;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import regatta.Regatta.Division;
import java.awt.Component;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import regatta.Race;
import javax.swing.JSpinner;
import java.awt.event.ActionListener;
import regatta.Sail;
import java.util.Collections;
import java.util.Arrays;
import tscore.TweakRotationsPane.SailChange;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import regatta.Rotation;
import regatta.RegattaEvent;
import regatta.RegattaEvent.RegattaEventType;
import javax.swing.JOptionPane;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.Map;

/**
 * Describe class TweakRotationsPane here.
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
 * Created: Thu Aug 27 10:46:02 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class TweakRotationsPane extends AbstractPane {

  public static enum SailChange {
    ADD("Add/Subtract amount to sails"),
      REPLACE("Replace sail with new one");
    private String name;
    SailChange(String n) {
      this.name = n;
    }
    public String toString() {
      return this.name;
    }
  }

  // Private variables
  private JComboBox changeField, sailField;
  private JList divisionList;
  private JRangeTextField raceField;
  private SpinnerNumberModel amountField, newSailField;
  private List<Component> changeComponents;

  // Layout
  private GridBagConstraints p1, p2;

  /**
   * Creates a new <code>TweakRotationsPane</code> instance.
   *
   */
  public TweakRotationsPane(Regatta reg) {
    super("Tweak rotations");
    this.setRegatta(reg);
  }

  /**
   * Adds the components to this pane
   *
   */
  public void fill() {
    this.removeAll();
    this.setLayout(new GridBagLayout());

    p1 = new GridBagConstraints();
    p2 = new GridBagConstraints();
    p1.gridx = 0; p2.gridx = 1;
    p1.gridy = 0; p2.gridy = 0;
    p2.weightx = 1.0;
    p1.weighty = 0.0;
    p1.fill = GridBagConstraints.HORIZONTAL;
    p2.fill = GridBagConstraints.HORIZONTAL;
    p1.insets = new Insets(3,3,3,3);
    p2.insets = new Insets(3,3,3,3);

    //- Sail change
    JLabel label;
    label = Factory.label("Sail change:");
    this.changeField = new JComboBox(SailChange.values());
    this.changeField.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  updateChangeComponents();
	}
      });
    this.add(label, p1);
    this.add(changeField, p2);

    //- Divisions
    p1.gridy++; p2.gridy++;
    label = Factory.label("Division(s):");
    Division [] divs = regatta.getDivisions();
    int [] selIndices = new int[divs.length];
    for (int i = 0; i < divs.length; i++) {
      selIndices[i] = i;
    }
    this.divisionList = new JNoneUnselectedList(divs);
    this.divisionList.setSelectedIndices(selIndices);
    this.divisionList.setBorder(BorderFactory.createEtchedBorder());
    this.add(label, p1);
    this.add(divisionList, p2);

    //- Races
    p1.gridy++; p2.gridy++;
    label = Factory.label("Races:");
    this.raceField =
      new JRangeTextField(Factory.raceToInteger(regatta.getRaces(Division.A)),
			  regatta.getRaces(Division.A));
    this.add(label, p1);
    this.add(raceField, p2);

    //- Change components, updated according to preceding values
    this.changeComponents = new ArrayList<Component>();
    this.updateChangeComponents();
  }

  public boolean isUsable() {
    return (this.regatta.getRotation() != null);
  }

  private void updateChangeComponents() {
    // Remove old components
    for (Component c : this.changeComponents) {
      this.remove(c);
    }

    // Add new ones
    p1.gridy = 3; p2.gridy = 3;
    this.changeComponents = new ArrayList<Component>();
    SailChange changeType =
      (SailChange) this.changeField.getSelectedItem();
    JLabel label;
    JSpinner spinner;
    final JButton button;
    if (changeType == SailChange.ADD) {
      
      //- Amount to add/subtract
      label = Factory.label("Amount:");
      this.amountField = new SpinnerNumberModel(0, null, null, 1);
      this.add(label, p1);
      this.add(spinner = new JSpinner(amountField), p2);
      this.changeComponents.add(label);
      this.changeComponents.add(spinner);

      //- Spacer
      p1.gridy++; p2.gridy++;
      p1.weighty = 1.0;
      this.add(label = new JLabel(), p1);
      this.changeComponents.add(label);

      //- Submit button
      p1.gridy++; p2.gridy++;
      p1.weighty = 0.0;
      p2.fill   = GridBagConstraints.NONE;
      p2.anchor = GridBagConstraints.LINE_END;
      this.add(button = new JButton(new AbstractAction("Add amount") {
	  public void actionPerformed(ActionEvent e) {
	    // Get amount
	    int amount = amountField.getNumber().intValue();

	    // Get the races
	    Rotation rot = regatta.getRotation();
	    Map<Division, List<Race>> problemRaces = new LinkedHashMap<Division, List<Race>>();
	    for (Object d : divisionList.getSelectedValues()) {
	      for (Integer i : raceField.getNumbers()) {
		Race race = regatta.getRace((Division)d, i);
		Sail [] sails = rot.getSails(race);
		int intValue = Integer.valueOf(Collections.min(Arrays.asList(sails)).getSail());
		if (intValue + amount <= 0) {
		  List<Race> list = problemRaces.get((Division)d);
		  if (list == null) {
		    list = new ArrayList<Race>(1);
		    problemRaces.put((Division)d, list);
		  }
		  list.add(race);
		}
		else {
		  for (Sail s : sails) {
		    s.setSail(String.valueOf(Integer.valueOf(s.getSail()) + amount));
		  }
		}
	      }
	    }
	    regatta.fireRegattaChange(new RegattaEvent(regatta,
						       RegattaEventType.ROTATION,
						       TweakRotationsPane.this));
	    if (problemRaces.keySet().size() > 0) {
	      String sailString = "";
	      for (Division d : problemRaces.keySet()) {
		sailString += "Division " + d + ": ";
		sailString +=
		  Factory.formatList(problemRaces.get(d).toArray(new Race[]{}));
		sailString += "\n";
	      }

	      JOptionPane.showMessageDialog(TweakRotationsPane.this,
					    "Sail values must be positive.\n" +
					    "Ignored races\n" + sailString,
					    "Error",
					    JOptionPane.WARNING_MESSAGE);
	    }
	    updateChangeComponents();
	  }
	}), p2);
      this.changeComponents.add(button);

      p2.fill = GridBagConstraints.HORIZONTAL;
    }
    else {
      // Create submit button
      button = new JButton(new AbstractAction("Replace sail") {
	  public void actionPerformed(ActionEvent e) {
	    // Get old and new sails
	    String oldSailNum = ((Sail)sailField.getSelectedItem()).getSail();
	    String newSailNum = String.valueOf(newSailField.getNumber());

	    // Get the races
	    Rotation rot = regatta.getRotation();
	    for (Object d : divisionList.getSelectedValues()) {
	      for (Integer i : raceField.getNumbers()) {
		Race race = regatta.getRace((Division)d, i);
		Sail [] sails = rot.getSails(race);
		
		for (Sail s : sails) {
		  if (s.getSail() == oldSailNum) {
		    s.setSail(newSailNum);
		  }
		}
	      }
	    }
	    regatta.fireRegattaChange(new RegattaEvent(regatta,
						       RegattaEventType.ROTATION,
						       TweakRotationsPane.this));
	    updateChangeComponents();
	  }
	});
	
      //- Sail to replace
      label = Factory.label("Replace sail:");
      List<Race> races = new ArrayList<Race>();
      Object [] divs = this.divisionList.getSelectedValues();
      for (Integer i : this.raceField.getNumbers()) {
	for (Object d : divs) {
	  races.add(regatta.getRace((Division)d, i));
	}
      }
      final Sail [] curSails = regatta.getRotation().getSails(races.toArray(new Race[]{}));
      this.sailField = new JComboBox(curSails);
      this.add(label, p1);
      this.add(sailField, p2);
      this.changeComponents.add(label);
      this.changeComponents.add(sailField);

      //- Replacement
      p1.gridy++; p2.gridy++;
      label = Factory.label("With new sail:");
      int nextSail = Integer.valueOf(Collections.max(Arrays.asList(curSails)).getSail());
      this.newSailField =
	new SpinnerNumberModel(nextSail + 1,
			       1, null, 1);
      this.newSailField.addChangeListener(new ChangeListener() {
	  public void stateChanged(ChangeEvent e) {
	    Sail newSail = new Sail(String.valueOf(newSailField.getNumber()));
	    button.setEnabled(!Arrays.asList(curSails).contains(newSail));
	  }
	});
      this.add(label, p1);
      this.add(spinner = new JSpinner(newSailField), p2);
      this.changeComponents.add(label);
      this.changeComponents.add(spinner);

      //- Spacer
      p1.gridy++; p2.gridy++;
      p1.weighty = 1.0;
      this.add(label = new JLabel(), p1);
      this.changeComponents.add(label);

      //- Submit button
      p1.gridy++; p2.gridy++;
      p1.weighty = 0.0;
      p2.fill   = GridBagConstraints.NONE;
      p2.anchor = GridBagConstraints.LINE_END;
      this.add(button, p2);
      this.changeComponents.add(button);

      p2.fill = GridBagConstraints.HORIZONTAL;
    }

    this.revalidate();
    this.repaint();
  }
}
