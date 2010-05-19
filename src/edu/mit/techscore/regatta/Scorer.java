package regatta;

import java.util.Map;

/**
 * Objects which can score regatta. Classes that implement this method
 * are required to implement four methods: <code>score</code> which
 * takes a regatta as argument and update its finishes to reflect the
 * scoring; <code>rules</code> which returns a <code>String</code>
 * summarizing the rules used; and two <code>rank</code> methods that
 * take a regatta and possibly a division and returns a list of that
 * regatta's team in order. This is also a tiebreaker.
 *
 *
 * Created: Sun Jun 28 21:48:37 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public interface Scorer {
  public void score(Regatta reg);
  public String rules();
  public Team [] rank(Regatta reg);
  public Team [] rank(Regatta reg, Regatta.Division div);
  public Map<Team, String> getRankExplanations();
}
