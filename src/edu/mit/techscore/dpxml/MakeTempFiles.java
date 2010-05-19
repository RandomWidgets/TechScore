package edu.mit.techscore.dpxml;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Describe class MakeTempFiles here.
 *
 *
 * Created: Thu Sep  3 18:23:21 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class MakeTempFiles {

  public static void main (String [] args) {
    String prefix = (args.length == 0) ? "jug" : args[0];
    try {
      File dir = File.createTempFile(prefix, "");
      File tempdir = new File(dir.getPath());
      dir.delete();
      if (tempdir.mkdir()) {
	System.out.println("Temp directory: " + dir.getPath());
      }
      else {
	throw new IllegalArgumentException("Could not create directory.");
      }

      File newFile;
      FileWriter writer;
      BufferedWriter out;
      for (char i = 'A'; i < 'F'; i++) {
	writer = new FileWriter(new File(tempdir, String.valueOf(i)));
	out = new BufferedWriter(writer);
	out.write("Hello World\tFile " + i + "\n");
	out.close();
      }

      Thread.currentThread().sleep(10000);

      // Delete each file
      System.out.println("Deleting files");
      for (File file : tempdir.listFiles()) {
	System.out.println("Deleting " + file);
	file.delete();
      }
      System.out.println("Deleting folder.");
      tempdir.delete();

    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
    } catch (IOException e) {
      System.err.println("Unable to create temp dir: " + e.getMessage());
    } catch (SecurityException e) {
      System.err.println("Do not have priviledges to create directory: " +
			 e.getMessage());
    } catch (InterruptedException e) {
      System.out.println("Interrupted before deletion.");
    }

  }

}
