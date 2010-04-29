package nscore;

import regatta.Regatta;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Date;
import regatta.RP;
import regatta.Team;
import regatta.RP.BoatRole;
import regatta.Sailor;
import regatta.Regatta.Division;
import java.util.Arrays;
import java.util.List;
import regatta.Race;
import regatta.Rotation;
import java.util.Calendar;
import regatta.Sail;
import regatta.Finish;
import regatta.FinishAdjustment;
import regatta.Penalty.PenaltyType;
import regatta.Penalty;
import regatta.Breakdown;
import regatta.Breakdown.BreakdownType;
import tscore.RegattaIO;

/**
 * Reads a NavyScoring (v3.92) SCO file and creates a regatta from
 * it.
 *
 *
 * Created: Tue Sep 22 18:49:41 2009
 *
 * @author <a href="mailto:dayan@localhost">Dayan Paez</a>
 * @version 1.0
 */
public class NavyScoreLoader {

  /**
   * Loads an SCO file and generates a regatta object with the
   * parameters in it.
   *
   * @param f a <code>File</code> value
   * @return a <code>Regatta</code> value
   */
  public static Regatta loadFile(File f)
    throws FileNotFoundException,
	   IOException,
	   SecurityException,
	   NumberFormatException {
    char [] divs = {'A', 'B', 'C', 'D'};

    Regatta reg;

    FileInputStream into = new FileInputStream(f);
    readStr(into, 4);
    String name = readStr(into, 35);
    String host = readStr(into, 35);
    String dateString = readStr(into, 35);

    reg = new Regatta(name);
    reg.setStartTime(getDateFromString(dateString));

    int fleet = into.read();
    int num_divs  = into.read();
    int num_races = into.read();
    reg.createRaces(num_divs, num_races);

    // Skip 5: "current race", "print style", "lpt style"...
    into.skip(5);
    // m("Rules", into.read());

    // Filler
    into.skip(2);

    // There seems to be a slight bug in the way the SCO files are saved,
    // where after 16 schools, the byte that would begin a new school
    // record, which should have the value "4" for the number of bytes in
    // the version number ("3.92"), is instead a "0", so the consume
    // function gobbles up more than it should.
    //
    // To fix this, ignore the two bytes from version at school index
    // 16. This seems to be more reliable, if unfortunate.

    // School nodes
    Team team;
    RP rp = new RP();
    Rotation rot = new Rotation();
    reg.setRP(rp);
    reg.setRotation(rot);
    // For re-mapping finishes
    Calendar cal = Calendar.getInstance();
    Date now     = cal.getTime();
    for (int school = 0; school < fleet; school++) {

      if (school == 16) {
	consume(into);
	into.skip(2);
      }
      else {
	readStr(into, 4);
      }
      String name1 = readStr(into, 20);
      String name2 = readStr(into, 20);
      reg.addTeam(team = new Team(name1, name2));
      
      into.read();
      // Ignore total
      into.skip(3);
      for (int d = 0; d < num_divs; d++) {
	Division div = Division.values()[d];
	into.skip(7);
	for (BoatRole role : BoatRole.values()) {
	  for (Sailor s : getSailors(readStr(into, 48))) {
	    rp.addSailorRaces(team,
			      s,
			      Arrays.asList(new Race[]{reg.getRace(div, 1)}),
			      role);
	  }
	}

	for (int i = 0; i < num_races; i++) {
	  // finish
	  int finInt = into.read();

	  cal.setTime(now);
	  cal.add(Calendar.HOUR,   d * num_races + i);
	  cal.add(Calendar.SECOND, 2 * finInt);
	  Date finHour = cal.getTime();
	  Race race  = reg.getRace(div, (i + 1));
	  Finish finish = new Finish(race, team, finHour);
	  finish.setAdjustment(getAdjustmentFromInt(finInt));
	  reg.setFinish(finish);

	  // rotation
	  Sail sail = new Sail(readStr(into, 8).trim());
	  rot.setSail(race, team, sail);
	}
	consume(into);
      }

      // Skip pointers
      into.skip(8);
    }

    into.close();

    return reg;
  }

  /**
   * Reads the string at point in file 'f' with maximum size as
   * specified by bucket
   *
   * @param f a <code>File</code> value
   * @param bucket an <code>int</code> value
   * @return a <code>String</code> value
   */
  private static String readStr(FileInputStream f, int bucket)
    throws IOException, NumberFormatException {

    int size = f.read();
    while (size == 0) {
      size = f.read();
    }
    byte [] data = new byte[size];
    f.read(data);
    f.skip(bucket - size);
    return new String(data);
  }

  /**
   * Converts decimal number to binary, return string
   *
   * @param num an <code>int</code> value
   * @return a <code>String</code> value
   */
  private static String dec2bin(int num) {
    if (num < 2) {
      return String.valueOf(num);
    }
    return String.valueOf(dec2bin(num / 2)) +
      String.valueOf(num % 2);
  }

  /**
   * Reads the 2-byte integer at point in file
   *
   * @param f a <code>File</code> value
   * @return an <code>int</code> value
   */
  private static int readInt(FileInputStream f) throws IOException {
    byte [] bits = new byte[2];
    f.read(bits);

    // step backwards and assemble integer string
    String strp = "";
    for (int i = bits.length - 1; i >= 0 ; i--) {
      String bin = "00000000" + dec2bin(bits[i] + 128);
      System.out.println((bits[i] + 128) + " = " + bin);
      
      strp += bin.substring(bin.length() - 9);
    }
    return Integer.parseInt(strp, 2);
  }

  /**
   * Convenience method: pretty-prints the title and value
   *
   * @param t a <code>String</code> value
   * @param v a <code>String</code> value
   */
  private static void m(String t, String v) {
    System.out.println(String.format("%15s: %s", t, v));
  }
  private static void m(String t, int v) {m(t, String.valueOf(v));}

  /**
   * Advances the file pointer to the first non-null byte and returns
   * it
   *
   * @param f a <code>File</code> value
   * @return an <code>int</code> value
   */
  private static int consume(FileInputStream f) throws IOException {
    int b = f.read();
    while (b == 0) {
      b = f.read();
    }
    return b;
  }

  private static Date getDateFromString(String str) {
    return new Date();
  }

  private static FinishAdjustment getAdjustmentFromInt(int code) {
    switch (code) {
    case 247:
    case 255:
      return new Penalty(PenaltyType.DSQ, "");

    case 248:
      return new Penalty(PenaltyType.RAF, "");

    case 249:
      return new Breakdown(BreakdownType.BKD, "");

    case 250:
      return new Breakdown(BreakdownType.BYE, "");

    case 251:
      return new Penalty(PenaltyType.OCS, "");

    case 252:
      return new Breakdown(BreakdownType.RDG, "");

    case 253:
      return new Penalty(PenaltyType.DNF, "");

    case 254:
      return new Penalty(PenaltyType.DNS, "");
      
    default:
      return null;
    }
  }

  private static List<Sailor> getSailors(String str) {
    return Arrays.asList(new Sailor[]{new Sailor("", str, new Date())});
  }

  public static void main (String [] args) {
    final String F_NAME = "/mit/paez/projects/ICSA/sco_files/OBERG08F.SCO";
    File f = new File(F_NAME);

    try {
      Regatta reg = loadFile(f);
      RegattaIO regIO = new RegattaIO();
      regIO.writeFile(reg, new File("/tmp/OBERG08F.tsr"));
    } catch (Exception e) {
      System.err.println("Error! " + e.getMessage());
    }

  }
}
