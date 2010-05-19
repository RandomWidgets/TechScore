package edu.mit.techscore.regatta;

import java.util.Comparator;
import java.util.Date;

import regatta.Finish.FinishType;
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
 * inner class PlaceComparator is provided. A variation of this
 * comparator called CleanPlaceComparator places those non-penalized
 * finishes ahead of penalized ones in the same race.
 *
 * Created: Sun Jun 28 21:57:04 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 2.0
 */
public class FinishWithType implements Comparable<Finish> {

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
   * The kind of finish
   */
  private FinishType type;

  /**
   * The numerical score for this regatta.
   */
  private int score = 0;

  /**
   * The penalty or breakdown, if any
   */
  private FinishAdjustment adjust = null;

  public static enum FinishType {
    REGULAR("", "Regular"),
      DNF("DNF", "Did not finish"),
      DNS("DNS", "Did not start"),
      BYE("BYE", "Bye");
    private String name, desc;
    FinishType(String n, String d) {
      this.name = n;
      this.desc = d;
    }
    public String toString() {return this.name;}
    public String desc()     {return this.desc;}
  };


  // Constructors
  /**
   * Creates a new <code>Finish</code> instance using the current time
   * for the timestamp and the default finish type
   * <code>REGULAR</code>.
   *
   * @param r a <code>Race</code> value
   * @param t a <code>Team</code> value
   */
  public FinishWithType(Race r, Team t) {
    this(r, t, new Date(), FinishType.REGULAR);
  }

  /**
   * Creates a new <code>Finish</code> instance using the current time
   * for the timestamp.
   *
   * @param r a <code>Race</code> value
   * @param t a <code>Team</code> value
   * @param type a <code>FinishType</code> value
   */
  public FinishWithType(Race r, Team t, FinishType type) {
    this(r, t, new Date(), type);
  }
  
  /**
   * Creates a new <code>Finish</code> instance with
   * <code>REGULAR</code> finish by default.
   *
   * @param r a <code>Race</code> value
   * @param t a <code>Team</code> value
   * @param time a <code>Date</code> value
   */
  public FinishWithType(Race r, Team t, Date time) {
    this(r, t, time, FinishType.REGULAR);
  }

  /**
   * Creates a new <code>Finish</code> instance.
   *
   * @param r <code>Race</code> the race
   * @param t <code>Team</code> the team
   * @param time a <code>Date</code> value for the timestamp
   * @param type a <code>FinishType</code> value
   */
  public FinishWithType(Race r, Team t, Date time, FinishType type) {
    this.race = r;
    this.team = t;
    this.timestamp = time;
    this.type = type;
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
   * Get the <code>Type</code> value.
   *
   * @return a <code>FinishType</code> value
   */
  public final FinishType getType() {
    return type;
  }

  /**
   * Set the <code>Type</code> of finish.
   *
   * @param newType The new Type value.
   */
  public final void setType(final FinishType newType) {
    this.type = newType;
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

  public String toString() {
    if (this.adjust != null) {
      return this.adjust.toString();
    }
    if (this.type != FinishType.REGULAR) {
      return this.type.toString();
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

      return f1.getTimestamp().compareTo(f2.getTimestamp());
    }
  }

  public static class CleanPlaceComparator implements Comparator<Finish> {
    // Implementation of java.util.Comparator
    public int compare(Finish f1, Finish f2) {
      int rel = f1.getRace().compareTo(f2.getRace());
      if (rel != 0) return rel;

      if (f2.getPenalty() != null) {
	return -1;
      }
      else if (f1.getPenalty() != null) {
	return 1;
      }
      return f1.getTimestamp().compareTo(f2.getTimestamp());
    }
  }
}