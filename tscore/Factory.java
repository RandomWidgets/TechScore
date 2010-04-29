package tscore;

import java.awt.Dimension;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import regatta.Race;
import regatta.Regatta;
import regatta.Regatta.Division;
import javax.swing.JButton;
import javax.swing.Action;
import java.awt.Insets;
import java.io.File;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

/**
 * Provides static methods for creating components
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
 * Created: Wed Jun  3 21:50:32 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class Factory {
  public static final String SELECTED_KEY = "actionConstants.selected";

  // Private variables
  private static String [] DATE_TAG_NAMES = {"TS:Year",
					     "TS:Month",
					     "TS:Day",
					     "TS:Hour",
					     "TS:Minute"};
  private static int [] DATE_INT = {Calendar.YEAR,
				    Calendar.MONTH,
				    Calendar.DAY_OF_MONTH,
				    Calendar.HOUR_OF_DAY,
				    Calendar.MINUTE};


  /**
   * Creates and returns a <code>JLabel</code> with the given name and
   * default size
   *
   * @param value the value for the label
   * @return a JLabel label, with default formatting
   */
  public static JLabel label(String value) {
    return Factory.label(value, 100);
  }
  
  /**
   * Creates and returns a <code>JLabel</code> with the given name and
   * specified width
   *
   * @param value the value for the label
   * @param width the width of the label
   * @return a JLabel label, with default formatting
   */
  public static JLabel label(String value, int width) {
    JLabel l = new JLabel(value, SwingConstants.RIGHT);
    l.setPreferredSize(new Dimension(width, 20));
    return l;
  }

  public static JButton tightButton(Action act) {
    JButton but = new TButton(act);
    but.setMargin(new Insets(1,1,1,1));
    return but;
  }

  /**
   * Returns a Spinner object created with the specified SpinnerModel
   *
   * @param m a <code>SpinnerModel</code> value
   * @return a <code>JSpinner</code> value
   */
  public static JSpinner spinner(SpinnerModel m) {
    return new TSpinner(m);
  }
  public static JSpinner spinner() {
    return new TSpinner();
  }
  
  /**
   * Creates and returns a text field with the given value and default
   * number of columns, and binding the escape action with the
   * keystroke "ESCAPE"
   * 
   * @param  value the value for the textfield
   * @return a JTextField text field, with default formatting
   */
  public static JTextField editField(String value) {
    JTextField t = new EditField(value, 20);
    t.setMinimumSize(new Dimension(30, 20));
    return t;
  }

  /**
   * Creates and returns a formatted text field with the formatter
   * specified and the original text specified, and with the default
   * parameters. Registers the focus listener specified with the
   * JFormattedTextField created.
   *
   * @param value  the formatter to use
   * @param format the original value
   * @param flt    the FocusListener object
   */
  public static JFormattedTextField formattedField(Object value,
						   AbstractFormatter format,
						   FocusListener flt) {
    JFormattedTextField t = new JFormattedTextField(format);
    t.addFocusListener(flt);
    t.setValue(value);
    t.setColumns(20);
    t.setMinimumSize(new Dimension(30, 20));

    return t;
  }

  /**
   * Creates and returns a standard Regatta, with name "Untitled",
   * starting time set to the current time, with 2 day duration,
   * "Personal" type, two divisions, and 10 races
   */
  public static Regatta newRegatta() {
    Regatta reg = new Regatta("Untitled");
    reg.setDuration(2);
    reg.createRaces(2, 10);

    return reg;
  }

  /**
   * Parses the character and returns a Division value, case
   * insensitive.
   *
   * @param div a <code>char</code> value for the division
   * @return a <code>Division</code>
   * @exception IllegalArgumentException if not a valid division code
   */
  public static Division parseDivision(char div)
    throws IllegalArgumentException {
    String test = "" + div;
    for (Division d: Division.values()) {
      if (d.toString().equalsIgnoreCase(test)) {
	return d;
      }
    }
    throw new IllegalArgumentException("Illegal division value: " + div);
  }

  /**
   * Returns an ImageIcon located in the given path
   *
   * @param path a <code>String</code> value
   * @param desc a <code>String</code> description
   * @return an <code>ImageIcon</code> value
   */
  public static ImageIcon getImageIcon(String path, String desc) {
    return new ImageIcon(Factory.class.getResource(path), desc);
  }
  
  /**
   * Creates an ImageIcon with no description.
   *
   * @param path a <code>String</code> value
   * @return an <code>ImageIcon</code> value
   */
  public static ImageIcon getImageIcon(String path) {
    return getImageIcon(path, null);
  }

  /**
   * Sorts a list according to the order of the items in the first
   * list
   *
   * @param list1 an <code>Comparable</code> value
   * @param list2 an <code>Object</code> value
   */
  public static <T extends Comparable, E>
			   void multiSort(List<T> list1,
					  List<E> list2) {

    if (list1.size() == 1) {
      return;
    }

    int end   = list1.size();
    int limit = end / 2;
    List<T> half11, half12;
    List<E> half21, half22;
    half11 = list1.subList(0, limit);
    half12 = list1.subList(limit, end);
    half21 = list2.subList(0, limit);
    half22 = list2.subList(limit, end);

    Factory.multiSort(half11, half21);
    Factory.multiSort(half12, half22);

    // Merge back together
    int i = 0;
    int j = 0;
    List<T> copy1 = new ArrayList<T>(end);
    List<E> copy2 = new ArrayList<E>(end);
    while (i < half11.size() && j < half12.size()) {
      T obj1 = half11.get(i);
      T obj2 = half12.get(j);

      if (obj1.compareTo(obj2) <= 0) {
	copy1.add(obj1);
	copy2.add(half21.get(i));
	i++;
      }
      else {
	copy1.add(obj2);
	copy2.add(half22.get(j));
	j++;
      }
    }
    // Add remaining ones
    while (i < half11.size()) {
      copy1.add(half11.get(i));
      copy2.add(half21.get(i));
      i++;
    }
    while (j < half12.size()) {
      copy1.add(half12.get(j));
      copy2.add(half22.get(j));
      j++;
    }

    // Copy back
    for (int r = 0; r < end; r++) {
      list1.set(r, copy1.get(r));
      list2.set(r, copy2.get(r));
    }
  }

  public static Integer [] raceToInteger(Race [] races) {
    Integer [] nums = new Integer[races.length];
    for (int i = 0; i < races.length; i++) {
      nums[i] = races[i].getNumber();
    }
    return nums;
  }

  /**
   * Convenience method for formatting races using their numerical
   * values.
   *
   * @param nums a <code>Sail</code> value
   * @return a <code>String</code> value
   */
  public static String formatList(Race [] races) {
    Integer [] nums = Factory.raceToInteger(races);
    Arrays.sort(nums);
    return formatList(nums);
  }

  /**
   * Returns a string representation of the list of integers, with
   * consecutive races represented as ranges, e.g. 1-3,5,8
   *
   * The integers must be in consecutive order already.
   *
   * @param races a <code>Race</code> value
   * @return a <code>String</code> value
   */
  public static String formatList(Integer [] nums) {
    if (nums.length == 0) {
      return "";
    }
    int next, last, curRange;
    last = nums[0];
    curRange = 0;
    String str = String.valueOf(last);
    int i = 1;
    while (i < nums.length) {
      next = nums[i];
      if (next - last == 1) {
	curRange++;
      }
      else {
	if (curRange > 0) {
	  str += "-" + last;
	}
	str += "," + next;
	curRange = 0;
      }
      last = next;
      i++;
    }
    if (curRange > 0) {
      str += "-" + last;
    }

    // Squeeze the dashes
    return str;
  }

  /**
   * Parses the string into a list of unique integers. Ranges can be
   * included as a dash (-). This method is nice: it will return only
   * what it understands without complaining.
   *
   * @param str a <code>String</code> value
   * @return an <code>Integer[]</code> value
   */
  public static Integer [] parseList(String str) {
    if (str.length() == 0) {
      return new Integer []{};
    }

    str = str.replaceAll("[^0-9 -]", " "); // whitelist
    str = str.replaceAll("^[^0-9]+", "");   // non-numerical start
    str = str.replaceAll("[^0-9]+$", "");   // non-numerical end
    str = str.replaceAll("[^0-9]+-[^0-9]+", "-"); // number-number
    str = str.replaceAll(" +", " "); // squeeze spaces

    String [] ranges = str.split(" ");
    Set<Integer> list = new TreeSet<Integer>();
    for (String range : ranges) {
      String [] subranges = range.split("-");

      // Get first and last
      int first = Integer.valueOf(subranges[0]);
      int last  = Integer.valueOf(subranges[subranges.length - 1]);

      for (int i = first; i <= last; i++) {
	list.add(new Integer(i));
      }
    }

    // Print away
    return list.toArray(new Integer []{});
  }

  public static String implode(Object [] obj, String delim) {
    StringBuilder sb = new StringBuilder();
    for (Object o : obj) {
      if (sb.length() > 0) {
	sb.append(delim);
      }
      sb.append(o);
    }
    return sb.toString();
  }

  // Database info
  
  private static int rpID = 0;
  public final static String RP_PREFIX = "ts";
  /**
   * Get the next RP ID
   *
   * @return a <code>String</code> value
   */
  public static String getNextRpId() {
    return RP_PREFIX + rpID++;
  }
  public static void setLastRpId(int i) {
    rpID = Math.max(rpID, (i + 1));
  }

  /**
   * Gets the extension for a file
   *
   * @param f a <code>File</code> value
   * @return a <code>String</code> value
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() -1) {
      ext = s.substring(i+1).toLowerCase();
    }

    return ext;
  }

  // Main method
  public static void main (String [] args) {
    Integer [] intList = {new Integer(4),
			  new Integer(3),
			  new Integer(5),
			  new Integer(1),
			  new Integer(6),
			  new Integer(9)};

    String []  enuList = {"Fourth", "Third", "Fifth", "First",
			  "Sixth",  "Tenth"};
    
    List<Integer> ints = Arrays.asList(intList);
    List<String>  strs = Arrays.asList(enuList);

    Factory.multiSort(ints, strs);
    for (int i = 0; i < ints.size(); i++) {
      System.out.println(ints.get(i) + "\t" + strs.get(i));
    }

    Integer num = 3;
    System.out.println(num);
  }
}
