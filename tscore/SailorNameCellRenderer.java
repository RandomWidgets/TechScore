package tscore;

import javax.swing.DefaultListCellRenderer;
import java.awt.Component;
import javax.swing.JList;
import regatta.Sailor;

/**
 * Describe class SailorNameCellRenderer here.
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
 * Created: Fri Sep 11 10:44:06 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class SailorNameCellRenderer extends DefaultListCellRenderer {

  /**
   * Creates a new <code>SailorNameCellRenderer</code> instance.
   *
   */
  public SailorNameCellRenderer() {
    super();
  }

  @Override
  public Component getListCellRendererComponent(JList list,
						Object value,
						int index,
						boolean isSelected,
						boolean cellHasFocus) {
    if (!(value instanceof Sailor)) {
      throw new IllegalArgumentException("Object must be sailor.");
    }
    Sailor s = (Sailor)value;
    return super.getListCellRendererComponent(list,
					      s.getName(),
					      index,
					      isSelected,
					      cellHasFocus);
  }
}
