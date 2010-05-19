package regatta;

import java.util.EventListener;

/**
 * Interface for listening to regatta events.
 *
 *
 * Created: Sun Jun  7 11:47:09 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public interface RegattaListener extends EventListener {
  public void regattaChange(RegattaEvent evt);
}
