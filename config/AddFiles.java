import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AddFiles {
  
  static String pdir;
  
  private static void addEntry(String path, BufferedWriter out) throws IOException {
    System.out.print(path);
    out.write("\""+path+"\"-\"");
    path = path.substring(pdir.length()+1);
    path = path.substring(0,1)+":"+path.substring(1);
    System.out.println(" -> "+path);
    out.write(path+"\"");
    out.newLine();
  }
  
  private static void addRemovalEntry(String path, BufferedWriter out) throws IOException {
    out.write("\"\"-\"");
    path = path.substring(pdir.length()+1);
    path = path.substring(0,1)+":"+path.substring(1);
    System.out.println(path+" (created later)");
    out.write(path+"\",FN");
    out.newLine();
  }

  private static void parseDir(File dir, BufferedWriter out) throws IOException {
    String[] files = dir.list();
    if (files != null) {
      for (int i=0;i<files.length;i++) {
        File tmp = new File(dir.getPath()+"\\"+files[i]);
        if (tmp.isDirectory() && tmp.canRead() && !tmp.isHidden()) {
          out.flush();
          parseDir(tmp, out);
        } else if (tmp.isFile() && !tmp.isHidden()) {
          addEntry(tmp.getPath(), out);
        } else if (tmp.isFile() && tmp.isHidden()) {
          addRemovalEntry(tmp.getPath(), out);
        }
      }
    }
    out.flush();
  }

  public static void main(String[] args) {
    if (args.length<1) {
      System.out.println("Syntax: AddFiles [directory]");
      System.exit(3);
    }
    pdir = args[0];
    File bld = new File("config\\makesis.pkg");
    if (!bld.exists()) {
      System.out.println("config\\makesis.pkg doesn't exist. Exiting...");
      System.exit(1);
    }
    File dir = new File(pdir);
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(bld, true));
      
      parseDir(dir, out);
      
      out.close();
    } catch (IOException ioe) {
      System.out.println("Error while adding data!");
      ioe.printStackTrace();
      System.exit(2);
    }
  }


}