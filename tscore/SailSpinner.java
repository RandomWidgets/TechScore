package tscore;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import regatta.Sail;

/**
 * Sail spinners allow changing the value of a sail intelligently,
 * respecting the numerical portion of a sail, if one exists. This
 * class goes out of its way to implement the obscure JSpinner API in
 * order to make sure the editor and the model stay synced. For this
 * reason, the model and the editor are both nested classes of this
 * class, as they all work tightly together.
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
 * Created: Sun May  9 00:02:07 2010
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class SailSpinner extends TSpinner implements ChangeListener {

  SailSpinnerModel model;
  SailFormattedField field;

  /**
   * Creates a new <code>SailSpinner</code> instance.
   *
   */
  public SailSpinner(Sail s) {
    super();
    this.model = new SailSpinnerModel();
    this.model.addChangeListener(this);
    this.setModel(model);
    
    this.field = new SailFormattedField();
    this.setEditor(field);
    this.setValue(s);
  }

  /**
   * Creates an empty <code>SailSpinner</code>
   *
   */
  public SailSpinner() {
    this(null);
  }

  public void setValue(Sail s) {
    this.model.setValue(s);
  }

  // Implementation of ChangeListener

  public void stateChanged(ChangeEvent evt) {
    this.field.setText(this.model.getValue().toString());
  }

  /**
   * Sail spinner model
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
   * Created: Sat May  8 16:46:01 2010
   *
   * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
   * @version 1.0
   */
  private class SailSpinnerModel extends AbstractSpinnerModel {

    private Sail currentSail;

    /**
     * Creates a new <code>SailSpinnerModel</code> instance.
     *
     */
    public SailSpinnerModel(Sail s) {
      this.setValue(s);
    }

    /**
     * Creates a new <code>SailSpinnerModel</code> instance with no value
     *
     */
    public SailSpinnerModel() {
      this(null);
    }

    /**
     * Sync up the value of this model with that of the spinner's
     *
     */
    private void sync() {
      SailFormattedField f = (SailFormattedField)SailSpinner.this.getEditor();
      this.setValue(new Sail(f.getText()));
    }

    /**
     * Get previous sail value, if numeric. Figure out what the value is
     * from the spinner first.
     *
     */
    public Object getPreviousValue() {
      this.sync();

      Sail s1 = (Sail)this.getValue();
      if (s1.getNumber() == 1)
	return null;

      Sail s2 = new Sail(s1.toString());
      s2.add(-1);
      return s2;
    }

    /**
     * Get next sail value
     *
     */
    public Object getNextValue() {
      this.sync();

      Sail s1 = (Sail)this.getValue();
      Sail s2 = new Sail(s1.toString());
      s2.add(1);
      return s2;
    }

    /**
     * Set the current Sail value
     *
     */
    public void setValue(Object s) {
      this.currentSail = (Sail)s;
      this.fireStateChanged();
    }

    /**
     * Get the current sail
     *
     */
    public Object getValue() {return this.currentSail;}
  }

  /**
   * A regular ol' text field. In the future, this might do some more
   * damage.
   *
   * Created: Sat May  8 09:41:12 2010
   *
   * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
   * @version 1.0
   */
  public class SailFormattedField extends JTextField {

    /**
     * Creates a new <code>SailFormattedField</code> instance.
     *
     */
    public SailFormattedField() {
      super();
    }

    public SailFormattedField(Sail s) {
      super(s.toString());
    }
  }

}
