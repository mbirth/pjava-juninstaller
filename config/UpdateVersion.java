import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UpdateVersion {

  private static String[] addString(String[] arr, String txt) {
    String[] tmp;
    tmp = new String[arr.length+1];
    for (int i=0;i<arr.length;i++) {
      tmp[i] = arr[i];
    }
    tmp[arr.length] = txt;
    return tmp;
  }

  private static String[] explode(String txt, String sep) {
    int i=0;
    int idx;
    String[] result = new String[0];
    while ((idx = txt.indexOf(sep, i)) >= 0) {
      result = addString(result, txt.substring(i,idx));
      i = idx+sep.length();
    }
    result = addString(result, txt.substring(i));
    return result;
  }

  public static void main(String[] args) {
    if (args.length<4) {
      System.out.println("Syntax: java UpdateVersion [Action] [Major] [Minor] [Build]\n");
      System.out.println("        [Action] is one of:");
      System.out.println("           SET   to set new values");
      System.out.println("           ADD   to add values to current values, use +x or -x");
      System.exit(1);
    }
    args[0] = args[0].toLowerCase();
    if (!args[0].equals("set") && !args[0].equals("add")) {
      System.out.println("Incorrect Action-switch. See Syntax.");
      System.exit(4);
    }
    File bld = new File("build.cmd");
    File bld2 = new File("build.$$$");
    if (bld2.exists()) {
      if (!bld2.delete()) {
        System.out.println("Could not delete temporary file build.$$$. Exiting...");
        System.exit(2);
      }
    }
    String tmp;
    String ntmp = "set VER=";
    int itmp;
    int[] newi = { 0, 0, 0 };
    try {
      BufferedReader in = new BufferedReader(new FileReader(bld));
      BufferedWriter out = new BufferedWriter(new FileWriter(bld2));
      while ((tmp = in.readLine()) != null) {
        tmp = tmp.trim();
        if (tmp.toLowerCase().indexOf("set ver=") >= 0) {
          System.out.println("Old: "+tmp);
          if (args[0].equals("set")) {
            for (int i=0;i<3;i++) {
              try {
                newi[i] = Integer.parseInt(args[i+1]);
              } catch (NumberFormatException nfe) {
                System.out.println("Error while parsing specified numbers!");
                System.exit(5);
              }
            }
          } else if (args[0].equals("add")) {
            String[] a1 = explode(tmp, "=");
            String[] a2 = explode(a1[1], ",");
          
            for (int i=0;i<3;i++) {
              try {
                newi[i] = Integer.parseInt(a2[i]);
              } catch (NumberFormatException nfe) {
                System.out.println("Error while parsing old numbers!");
                nfe.printStackTrace();
                System.exit(6);
              }
              try {
                newi[i] += Integer.parseInt(args[i+1]);
              } catch (NumberFormatException nfe) {
                System.out.println("Error while parsing specified numbers!");
                nfe.printStackTrace();
                System.exit(5);
              }
            }
          }
          for (int i=0;i<3;i++) {
            ntmp += Integer.toString(newi[i])+((i<=1)?",":"");
          }
          System.out.println("New: "+ntmp);
          out.write(ntmp);
          out.newLine();
        } else {
          out.write(tmp);
          out.newLine();
        }
      }
      in.close();
      out.close();
    } catch (IOException ioe) {
      System.out.println("Error while copying!");
      ioe.printStackTrace();
      System.exit(3);
    }
    if (bld.delete()) {
      bld2.renameTo(bld);
    } else {
      System.out.println("Could not delete old build.cmd. New file is build.$$$ - rename manually!");
    }
  }


}