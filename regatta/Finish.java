package regatta;

import java.util.Comparator;
import java.util.Date;
import regatta.Finish.PlaceComparator;

/**
 * Encapsulates a regatta finish, including the score (to be filled in
 * by a <code>Scorer</code>) and any penalties or breakdowns
 * associated with the finish. Finishes are stored with a timestamp
 * value corresponding to their moment of creation, or as specified.
 *
 * Class implements the Comparable interface. Two finishes are the
 * same if they both reference the same team in the same
 * race. Otherwise, finishes are organized first by race, and then by
 * team, using <code>Race</code>'s and <code>Team</code>'s Comparable
 * methods.
 *
 * To organize or compare finishes in terms of place finishes, the
 * inner class PlaceComparator is provided.
 *
 * Created: Sun Jun 28 21:57:04 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 2.0
 */
public class Finish implements Comparable<Finish> {

  /**
   * The race associated with this finish.
   */
  private Race race;

  /**
   * The team associated with this finish.
   */
  private Team team;

  /**
   * The moment in time at which the team crossed the finish line in
   * this race
   */
  private Date timestamp;

  /**
   * The numerical score for this regatta.
   */
  private int score = 0;

  /**
   * The penalty or breakdown, if any
   */
  private FinishAdjustment adjust = null;

  // Constructors
  /**
   * Creates a new <code>Finish</code> instance using the current time
   * for the timestamp.
   *
   * @param r a <code>Race</code> value
   * @param t a <code>Team</code> value
   */
  public Finish(Race r, Team t) {
    this(r, t, new Date());
  }
  
  /**
   * Creates a new <code>Finish</code> instance.
   *
   * @param r <code>Race</code> the race
   * @param t <code>Team</code> the team
   * @param time a <code>Date</code> value for the timestamp
   */
  public Finish(Race r, Team t, Date time) {
    this.race = r;
    this.team = t;
    this.timestamp = time;
  }

  /**
   * Get the <code>Race</code> value.
   *
   * @return a <code>Race</code> value
   */
  public final Race getRace() {
    return race;
  }

  /**
   * Get the <code>Team</code> value.
   *
   * @return a <code>Team</code> value
   */
  public final Team getTeam() {
    return team;
  }

  /**
   * Get the <code>Timestamp</code> value.
   *
   * @return a <code>Date</code> value
   */
  public final Date getTimestamp() {
    return timestamp;
  }

  /**
   * Set the <code>Timestamp</code> value for this finish.
   *
   * @param newTimestamp The new Timestamp value.
   */
  public final void setTimestamp(final Date newTimestamp) {
    this.timestamp = newTimestamp;
  }

  /**
   * Get the <code>Score</code>.
   *
   * @return an <code>int</code> value representing the score.
   */
  public final int getScore() {
    return score;
  }

  /**
   * Set the <code>Score</code> value.
   *
   * @param newScore The new Score value.
   */
  public final void setScore(final int newScore) {
    this.score = newScore;
  }

  /**
   * Returns the finish adjustment, if any. A finish adjustment can be
   * either a penalty or a breakdown, only one of which is allowed for
   * a given finish.
   *
   * @return a <code>Adjustment</code> object if one exists for this
   * finish
   * @return <code>null</code> if no adjustment exists.
   */
  public final FinishAdjustment getAdjustment() {
    return this.adjust;
  }

  public final Penalty getPenalty() {
    if (this.adjust == null) {
      return null;
    }
    if (this.adjust instanceof Penalty) {
      return (Penalty)this.adjust;
    }
    return null;
  }
  public final Breakdown getBreakdown() {
    if (this.adjust == null) {
      return null;
    }
    if (this.adjust instanceof Breakdown) {
      return (Breakdown)this.adjust;
    }
    return null;
  }

  public final void setPenalty(Penalty penalty) {
    this.adjust = penalty;
  }

  public final void setBreakdown(Breakdown breakdown) {
    this.adjust = breakdown;
  }

  public final void setAdjustment(FinishAdjustment a) {
    this.adjust = a;
  }

  public String toString() {
    if (this.adjust != null) {
      return this.adjust.toString();
    }
    if (this.score > 0) {
      return String.valueOf(this.score);
    }
    return "";
  }

  // Implementation of Comparable interface
  public int compareTo(Finish other) {
    int rel = this.getRace().compareTo(other.getRace());
    if (rel != 0)
      return rel;
    return this.getTeam().compareTo(other.getTeam());
  }

  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other instanceof Finish) {
      return this.compareTo((Finish)other) == 0;
    }
    return false;
  }

  public int hashCode() {
    return this.getRace().hashCode() + this.getTeam().hashCode();
  }

  public static class PlaceComparator implements Comparator<Finish> {
    // Implementation of java.util.Comparator
    public int compare(Finish f1, Finish f2) {
      int rel = f1.getRace().compareTo(f2.getRace());
      if (rel != 0) return rel;

      rel = f1.getTimestamp().compareTo(f2.getTimestamp());
      if (rel != 0) {
	return rel;
      }

      return f1.getTeam().compareTo(f2.getTeam());
    }
  }
}