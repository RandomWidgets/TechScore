/**
 * This file is part of TechScore.<p>
 * 
 * TechScore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<p>
 * 
 * TechScore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<p>
 * 
 * You should have received a copy of the GNU General Public License
 * along with TechScore.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edu.mit.techscore.regatta;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Tracks the membership of different sailors by using their
 * affiliation as a <code>String</code> value.<p>
 *
 * Affiliations can be strict or not. Client code is responsible for
 * giving meaning to this flag. Presumably, a strict membership
 * database will not allow new entries, or provide for a drop-down
 * list of current entries prior to entering new informtation, etc.<p>
 *
 * The database is stored as files in a directory, which can be
 * specified with {@link setDatabaseDir}. The files are named after
 * their affiliation code, and are ordered according to the following
 * scheme:<p>
 *
 * <table>
 * <tr>
 *     <th>ID</th>
 *     <th>Name</th>
 *     <th>Year</th>
 *     <th>New?</th>
 * </tr>
 * </table>
 *
 * The ID is used as a primary key. The name and year are
 * self-explanatory and reflect the needs of college sailing. It is
 * perfectly legal for the latter to be left blank. The last field
 * (New?) is used to track additions to the database should it ever
 * need to be synchronized with an external source. Presumably, this
 * field is always 'true' for every regatta created from
 * scratch. Note, however, that the default value for that field is
 * true.<p>
 *
 * Each record is stored in the file in one line. Each field is
 * tab-delimited, and each line is separated by the OS's native
 * newline delimiter.<p>
 *
 * Created: Sun Jun 20 22:02:29 2010
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 * @see {link Sailor}
 */
public class MembershipDatabase {

  /**
   * Describe databaseDir here.
   */
  private File databaseDir;

  /**
   * Describe strict here.
   */
  private boolean strict;

  // Determines whether the database can be used at all
  private boolean isUsable;

  /**
   * Creates a new <code>MembershipDatabase</code> instance.
   *
   */
  public MembershipDatabase() {
    try {
      File dir = File.createTempFile("tsr", "");
      this.databaseDir = new File(dir.getPath());
      dir.delete();
      this.databaseDir.mkdir();
      this.databaseDir.deleteOnExit();
      System.out.println("Created directory " + this.databaseDir);
      
      this.isUsable = true;
    } catch (IOException e) {
      this.isUsable = false;
    }
    this.strict = false;
  }

  /**
   * Get the <code>DatabaseDir</code> value.
   *
   * @return a <code>File</code> value
   */
  public final File getDatabaseDir() {
    return databaseDir;
  }

  /**
   * Set the <code>DatabaseDir</code> value.
   *
   * @param newDatabaseDir The new DatabaseDir value.
   * @throws IllegalArgumentException if not a directory
   */
  public final void setDatabaseDir(final File newDatabaseDir)
    throws IOException {

    if (!newDatabaseDir.isDirectory())
      throw new IllegalArgumentException(newDatabaseDir + " is not a directory.");

    // Move all files to new directory
    if (newDatabaseDir.canWrite()) {
      for (File f : databaseDir.listFiles()) {
	if (!f.renameTo(new File(newDatabaseDir, f.getName())))
	  throw new IOException("Unable to move database file to new directory");
      }
    }

    this.databaseDir = newDatabaseDir;
  }

  /**
   * Get the <code>Strict</code> value.
   *
   * @return a <code>boolean</code> value
   */
  public final boolean isStrict() {
    return strict;
  }

  /**
   * Set the <code>Strict</code> value.
   *
   * @param newStrict The new Strict value.
   */
  public final void setStrict(final boolean newStrict) {
    this.strict = newStrict;
  }
  
  /**
   * Gets the set of affiliations currently in use by all the teams in
   * this regatta, plus the "_MISC" affiliation. Also, delete all
   * affiliation files from database which are empty and not in use by
   * any team (except, of course, for "_MISC")
   *
   * @return a <code>String[]</code> value, or null if no such value
   * exists
   */
  public String [] getAffiliations() {
    return this.databaseDir.list();
  }

  /**
   * Releases all resources and removes the database from file.
   *
   */
  public void close() {
    if (this.databaseDir == null)
      return;

    for (File f : this.databaseDir.listFiles()) {
      f.delete();
    }
  }

  /**
   * Creates the given affiliation if it does not already exist
   *
   * @param aff the affiliation to set
   * @return true if it worked, false otherwise
   */
  public boolean addAffiliation(String aff) {
    File affFile = new File(databaseDir, aff.toUpperCase());
    try {
      affFile.createNewFile();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Adds this member to the database for the given affiliation.
   *
   * @param aff the affiliation code
   * @param sailor the sailor to add
   * @param isNew whether this is a new sailor
   */
  public boolean setMember(String aff, Membership sailor) {
    File affFile = new File(databaseDir, aff.toUpperCase());
    File temp;
    try {
      affFile.createNewFile();
      temp = File.createTempFile("tsr" + aff, "");

      // Transfer memberships from one this file to a temp file
      BufferedReader in  = new BufferedReader(new FileReader(affFile));
      BufferedWriter out = new BufferedWriter(new FileWriter(temp));
      String line, theID;
      while ((line = in.readLine()) != null) {
	String [] fields = line.split("\t");
	theID = fields[0];
	if (!sailor.getID().equals(theID)) {
	  out.write(line);
	  out.newLine();
	}
      }
      in.close();
      // Write this new membership
      Calendar cal = Calendar.getInstance();
      cal.setTime(sailor.getYear());
      int year = cal.get(Calendar.YEAR);
      out.write(String.format("%s\t%s\t%s\t%s",
			      sailor.getID(),
			      sailor.getName(),
			      year,
			      sailor.isNew()));
      out.close();

      // Copy the temp file to the old file
      affFile.delete();
      temp.renameTo(affFile);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * Removes the given sailor from the database
   *
   * @param aff the affiliation
   * @param sailor the sailor to remove
   * @return true on success, false otherwise
   */
  public boolean unsetMember(String aff, Sailor sailor) {
    File affFile = new File(databaseDir, aff.toUpperCase());
    if (!affFile.exists()) return false;
    
    File temp;
    try {
      affFile.createNewFile();
      temp = File.createTempFile("tsr" + aff, "");

      // Transfer memberships from one this file to a temp file
      BufferedReader in  = new BufferedReader(new FileReader(affFile));
      BufferedWriter out = new BufferedWriter(new FileWriter(temp));
      String line, theID;
      while ((line = in.readLine()) != null) {
	String [] fields = line.split("\t");
	theID = fields[0];
	if (!sailor.getID().equals(theID)) {
	  out.write(line);
	  out.newLine();
	}
      }
      in.close();
      out.close();

      // Copy the temp file to the old file
      affFile.delete();
      temp.renameTo(affFile);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * Returns all the members for the given affiliation
   *
   * @param aff the affiliation code
   * @return list of members, empty if no such affiliation exists or
   * is unreadable
   */
  public Membership [] getMembers(String aff) {
    Calendar cal = Calendar.getInstance();
    File affFile = new File(databaseDir, aff.toUpperCase());
    if (!affFile.exists()) return new Membership [] {};

    try {
      // Transfer memberships from one this file to a temp file
      List<Membership> members = new ArrayList<Membership>();
      BufferedReader in  = new BufferedReader(new FileReader(affFile));
      String line, theID;
      while ((line = in.readLine()) != null) {
	String [] fields = line.split("\t");
	Date year;
	try {
	  cal.set(Calendar.YEAR, Integer.parseInt(fields[2]));
	  year = cal.getTime();
	} catch (Exception e) {
	  year = new Date();
	}
	boolean isNew = true;
	try {
	  isNew = Boolean.parseBoolean(fields[3]);
	} catch (Exception e) {}

	members.add(new Membership(fields[0], fields[1], year, isNew));
      }
      in.close();
      return members.toArray(new Membership[]{});
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Tracks new sailors with new field <code>isNew</code>
   *
   *
   */
  public static class Membership extends Sailor {
    private boolean isNew;

    public Membership(String id, String name, Date year) {
      this(id, name, year, true);
    }
    public Membership(String id, String name, Date year, boolean isNew) {
      super(id, name, year);
      this.setNew(isNew);
    }
    
    public boolean isNew() { return this.isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
  }

  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String</code> value
   */
  public static final void main(final String[] args) {
    MembershipDatabase db = new MembershipDatabase();
    db.setMember("_MISC", new Membership("t01", "Dayan Paez", new Date()));
    db.setMember("HAR", new Membership("t02", "Alexis Dale", new Date()));

    try {
      System.in.read();
    } catch (IOException e) {
      System.err.println("Unable to read from command line.");
    }

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2008);
    db.setMember("HAR", new Membership("t02", "Alexis Dalee", cal.getTime()));
    try {
      System.in.read();
    } catch (IOException e) {
      System.err.println("Unable to read from command line.");
    }
    for (Membership m : db.getMembers("HAR")) {
      System.out.println(m);
    }


    db.close();
  }

}
