import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MyFilesystemParser {
  
  File logFile;
  private String curLine;
  private String ignoreDir;
  private long counter;
  
  public MyFilesystemParser(String logfile) {
    logFile = new File(logfile+".gz");
    ignoreDir = System.getProperty("user.dir");
    while (ignoreDir.charAt(ignoreDir.length()-1)=='\\') ignoreDir = ignoreDir.substring(0, ignoreDir.length()-1);
  }
  
  public boolean driveIsWritable(char drive) {
    File flTest = new File (drive+":\\");
    return flTest.canWrite();
  }
  
  public boolean driveExists(char drive) {
    File flTest = new File(drive+":\\");
    return flTest.exists();
  }
  
  public boolean driveIsReadable(char drive) {
    File flTest = new File(drive+":\\");
    return flTest.canRead();
  }
  
/*  public File[] listRoots() {
    File[] result = new File[1];
    result[0] = new File("E:\\");
    return result;
  } */
  
  public File[] listRoots() {
    int count = 0;
    for (char d='A';d<='Z';d++) {
      if (driveExists(d)) count++;
    }
    File[] result = new File[count];
    count = 0;
    for (char d='A';d<='Z';d++) {
      if (driveExists(d)) result[count++] = new File(d+":\\"); 
    }
    return result;
  }
  
  /** Returns all files of a specified directory
      ATTENTION! ignoreDir has to contain a directory which should be ignored (temp, etc.).
      The String must have no trailing backslash or slash. Also keep in mind that we work with
      equalsIgnoreCase - so this won't work correctly on UNIXish machines
      @param dir Directory to get contents of
      @param noIgnoreDir If false, the startup-directory is ignored (because there are temporary files, data files, etc.)
      @return File[] with File-Objects of all files in that directory
      */
  public File[] listFiles(File dir, boolean noIgnoreDir) {
    String[] files = dir.list();
    File[] result;
    if ( files != null && ( !dir.getAbsolutePath().equalsIgnoreCase(ignoreDir) || noIgnoreDir )) {
      result = new File[files.length];
      for (int i=0; i<files.length; i++) {
        String lastItem = dir.getAbsolutePath();
        result[i] = new File(lastItem+((lastItem.charAt(lastItem.length()-1)=='\\')?"":"\\")+files[i]);
      }
    } else {
      result = new File[0];
    }
    return result;
  }
  
  /** Swaps two items of the files array */
  public void swap(File[] files, int i1, int i2) {
    File tmp = files[i2];
    files[i2] = files[i1];
    files[i1] = tmp;
  }
  
  /** String.compareToIgnoreCase() */
  public int lge(String s1, String s2) {
    return s1.toLowerCase().compareTo(s2.toLowerCase());
  }
  
  /** Helper method to partition QuickSort-areas */
  public int sortQuickPart(File[] files, int lo, int hi) {
    String p = files[(lo+hi)/2].getName();  // Pivot-Element

    while (lo <= hi) {
      while (lge(files[hi].getName(), p) > 0) hi--;
      while (lge(files[lo].getName(), p) < 0) lo++;
      if (lo < hi) {
        swap(files, lo++, hi--);
      } else {
        return hi;
      }
    }
    return hi;
  }
  
  /** QuickSort implementation */
  public void sortQuick(File[] files, int lo, int hi) {
    if (lo<hi) {
      int p = sortQuickPart(files, lo, hi);
      sortQuick(files, p+1, hi);
      sortQuick(files, lo, p);
    }
  }
  
  /** Arranges all directory-entries of the list at top */
  public void sortDirsFirst(File[] files) {
    int x;
    if (files.length>0 && files[0].isDirectory()) x=1; else x=0;
    File tmp;
    for (int i=1;i<files.length;i++) {
      if (files[i].isDirectory()) {
        tmp = files[i];
        for (int j=i;j>x;j--) {
          files[j] = files[j-1];
        }
        files[x] = tmp;
        x++;
      }
    }
  }
  
  /** Returns the sorted files-array */
  public File[] sortDir(File[] files) {
    if (files.length>=2) {
      sortQuick(files, 0, files.length-1);
      // sortDirsFirst(files);
    }
    return files;
  }
  
  /** Writes all file-entries incl. size and time of dir and sub-dirs to fw */
  public void dumpFiles(BufferedOutputStream fw, File dir) throws IOException {
    File[] entries = sortDir(listFiles(dir, false));
    for (int i=0; i<entries.length; i++) {
      if (entries[i].isDirectory() && entries[i].canRead()) {
        fw.flush();
        dumpFiles(fw, entries[i]);
      } else if (entries[i].isFile()) {
        fw.write((entries[i].getPath()+"\t"+entries[i].length()+"\t"+entries[i].lastModified()+"\r\n").getBytes());
      }
    }
    fw.flush();
  }
  
  /** Walks the root-directories of all drives and calls dumpFiles() */
  public void dumpAllDrives() throws IOException {
      BufferedOutputStream fw = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(logFile)));
      File[] roots = listRoots();
      for (int i=0;i<roots.length;i++) {
        if (roots[i].canWrite()) {
          dumpFiles(fw, roots[i]);
        }
      }
      fw.close();
  }
  
  /** Compares file-liste from cmp to actual files on disk and writes changes to out */
  public void cmpFiles(BufferedReader cmp, BufferedOutputStream out, File dir, BufferedOutputStream dout) throws IOException {
    File[] entries = sortDir(listFiles(dir, false));
    String myLine = "";
    for (int i=0; i<entries.length; i++) {
      if (entries[i].isDirectory() && entries[i].canRead()) {
        // readable directory ... submerge
        out.flush();
        if (dout != null) dout.flush();
        cmpFiles(cmp, out, entries[i], dout);
      } else if (entries[i].isFile()) {
        // it's a file
        myLine = entries[i].getPath()+"\t"+entries[i].length()+"\t"+entries[i].lastModified();
        if (dout != null) dout.write((myLine+"\r\n").getBytes());
        String fn1 = curLine.substring(0, curLine.indexOf("\t"));
        String fn2 = myLine.substring(0, myLine.indexOf("\t"));
        while (lge(fn1, fn2) < 0) {
          // file from dump is behind our current file on disk.
          // i.e.: a file got deleted - never mind, do not log
          out.write(("-\t"+curLine+"\r\n").getBytes());
          counter++;
          curLine = cmp.readLine();
          fn1 = curLine.substring(0, curLine.indexOf("\t"));
        }
        if (!myLine.equals(curLine)) {
          // something's different - name (new file) or just size or date - log it!
          if (fn1.equals(fn2)) {
            // both entries are about the same file, but size or date differs
            out.write(("*\t"+myLine+"\r\n").getBytes());
            counter++;
            curLine = cmp.readLine();
          } else {
            // there's a new file on disk
            out.write(("+\t"+myLine+"\r\n").getBytes());
            counter++;
          }
        } else {
          // both entries are equal - never mind, advance to next
          curLine = cmp.readLine();
        }
      } // else if (entries[i].isFile())
    } // for (int i=0; i<entries.length; i++)
    out.flush();
  }

  /** Walks all root-directories and calls cmpFiles() */
  public long dumpDifferences(String outfile, boolean redump) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outfile)));
    BufferedOutputStream dout;
    if (redump) dout = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream("$$mfs$$.$$$"))); else dout = null;
    BufferedReader cmp = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile))));
    counter = 0;
    File[] roots = listRoots();
    curLine = cmp.readLine();
    for (int i=0;i<roots.length;i++) {
      if (roots[i].canWrite()) {
        cmpFiles(cmp, out, roots[i], dout);
      }
    }
    cmp.close();
    out.close();
    if (dout != null) {
      // redumped! Try to delete old file and rename new one.
      dout.close();
      File dump = new File("$$mfs$$.$$$");
      if (logFile.delete()) {
        if (!dump.renameTo(logFile)) {
          System.out.println("ERROR: Could not rename new log to "+logFile.getName()+".");
        }
      } else {
        System.out.println("ERROR: Could not delete old logfile. Redump is still there.");
      }
    }
    return counter;
  }
  
  public long dumpDifferences(String outfile) throws IOException {
    return dumpDifferences(outfile, false);
  }
  
  /** Deletes dump */
  public boolean delDump() {
    return logFile.delete();
  }
  
  /** Fills a string with preceding zeroes until its length is digits */
  private String formatNumber(int value, int digits) {
    String result = String.valueOf(value);
    while (result.length() < digits) {
      result = "0" + result;
    }
    return result;
  }
  
  /** Returns the formatted date of the dump file as String */
  public String getDumpDate(File flLog) {
    String result = "";
    if (flLog.exists()) {
      Date dtTime = new Date(flLog.lastModified());
      Calendar clTime = Calendar.getInstance();
      clTime.setTime(dtTime);
      
      result += formatNumber(clTime.get(Calendar.YEAR), 4) + "-";
      result += formatNumber(clTime.get(Calendar.MONTH), 2) + "-";
      result += formatNumber(clTime.get(Calendar.DATE), 2) + " ";
      result += formatNumber(clTime.get(Calendar.HOUR_OF_DAY), 2) + ":";
      result += formatNumber(clTime.get(Calendar.MINUTE), 2) + ".";
      result += formatNumber(clTime.get(Calendar.SECOND), 2);
    } else {
      result = "<DOES NOT EXIST>";
    }
    return result;
  }
  
  public String getDumpDate() {
    return getDumpDate(logFile);
  }
  
  public String getDumpDate(String name) {
    File flLog = new File(name);
    return getDumpDate(flLog);
  }
  
  /** Returns a list of data files */
  public String[] getMonitored(String mask) {
    File[] files = sortDir(listFiles(new File("."), true));
    String fn = "";
    String[] result = new String[0];
    String[] tmp;
    for (int i=0;i<files.length;i++) {
      fn = files[i].getName();
      if (fn.length()-mask.length()>0 && fn.substring(fn.length()-mask.length()).equals(mask)) {
        tmp = new String[result.length+1];
        for (int j=0;j<result.length;j++) {
          tmp[j] = result[j];
        }
        tmp[tmp.length-1] = fn.substring(0, fn.length()-mask.length());
        result = tmp;
      }
    }
    return result;
  }
  
  /** Returns all entries of specified file */
  public String[] getEntries(String fname) {
    File infile = new File(fname);
    String[] result = new String[0];
    String[] tmp;
    String ln;
    int tab;
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(infile))));
      ln = in.readLine();
      while (ln != null) {
        tmp = new String[result.length+1];
        for (int i=0;i<result.length;i++) {
          tmp[i] = result[i];
        }
        tab = ln.indexOf("\t");
        String fn = ln.substring(tab+1, ln.indexOf("\t", tab+1));
        File fl = new File(fn);
        String status = ln.substring(0, tab);
        if (!fl.exists()) {
          status += "!";
        }
        tmp[tmp.length-1] = status+" "+fn;
        result = tmp;
        ln = in.readLine();
      }
      in.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }
  
}