import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.io.File;

public class JUninstaller extends Frame implements ActionListener {

  final int WND_W=208, WND_H=276;  // initial window size
  final String APPNAME="jUninstaller";
  final String APPVERSION="1.0";
  final String DATAEXT=".jun.gz";
  
  private static JUninstaller jUninstaller = null;
  
  final Font ftPlain8 = new Font("Dialog", Font.PLAIN, 8);
  final Font ftPlain10 = new Font("Dialog", Font.PLAIN, 10);
  final Font ftBold12 = new Font("Dialog", Font.BOLD, 12);
  
  CardLayout clMain = new CardLayout();
  Panel pnMain = new Panel(clMain);
  
  Panel pnUnin = new Panel(new BorderLayout());
  Panel pnUnin2 = new Panel(new GridLayout(0,1,0,0));

  Button btAbout = new Button(APPNAME);

  Dialog diAbout = new Dialog(this, "About...", true);
  Panel pnAboutText = new Panel(new GridLayout(0,1,0,0));
  Panel pnAboutButt = new Panel(new FlowLayout(FlowLayout.RIGHT));
  Label lbAbout1 = new Label(APPNAME, Label.CENTER);
  Label lbAbout2 = new Label("by Markus Birth", Label.CENTER);
  Label lbAbout3 = new Label("mbirth@webwriters.de", Label.CENTER);
  Label lbAbout4 = new Label("http://www.webwriters.de/mbirth/", Label.CENTER);
  Button btOk = new Button("OK");
  
  List ltApps = new List();
  Panel pnMain1 = new Panel(new GridLayout(1,0,0,0));
  Panel pnMain2 = new Panel(new GridLayout(1,0,0,0));
  Button btUnin = new Button("Uninstall");
  Button btView = new Button("View");
  Button btMoni = new Button("Monitor new");
  Button btRena = new Button("Rename");
  Button btDele = new Button("Delete");
  Button btQuit = new Button("Exit");
  
  Panel pnMoni = new Panel(new GridLayout(0,1,0,15));
  Label lbLast = new Label("Last scan:");
  Button btScan = new Button("Scan disks");
  Button btFind = new Button("Find changes");
  Button btFUp = new Button("Find changes & \nUpdate Scan");
  Button btBack = new Button("Back");
  
  Panel pnScan = new Panel(new GridLayout(0,1,0,15));
  Label lbSIP = new Label("SCAN IN PROGRESS", Label.CENTER);
  Label lbPW = new Label("Please wait...", Label.CENTER);
  Label lbTime = new Label("--:--:--", Label.CENTER);
  
  Panel pnView = new Panel(new BorderLayout());
  Panel pnDel = new Panel(new BorderLayout());
  Panel pnViewB = new Panel(new BorderLayout());
  Panel pnViewC = new Panel(new GridLayout(0,1,0,0));
  Panel pnViewD = new Panel(new GridLayout(0,1,0,0));
  Label lbView = new Label("---");
  List ltView = new List();
  Button btDetails = new Button("Details");
  Button btRefresh = new Button("Refresh");
  Button btRemove = new Button("Remove");
  Button btDelFile = new Button("Del");
  Button btBack2 = new Button("Back");
  
  Dimension dmScreen = Toolkit.getDefaultToolkit().getScreenSize();  // get Screen dimensions
  MyInfoPrint MIP = new MyInfoPrint(this);
  MyFilesystemParser mfs = new MyFilesystemParser("JUninstaller.dat");
  MyQuestions MQ = new MyQuestions(this);
  
  private class MainWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent we) {
      // TODO: Insert commands to execute upon shutdown
      MIP.infoPrint("Goodbye...");
      MIP.hide();
      System.exit(0);
    }
  }
  
  private class DummyWindowAdapter extends WindowAdapter {
    public void windowActivated(WindowEvent we) {
    }
    
    public void windowDeactivated(WindowEvent we) {
      if (we.getSource().equals(diAbout)) {
        diAbout.setVisible(false);
      }
    }
    
    public void windowClosing(WindowEvent we) {
      if (we.getSource().equals(diAbout)) {
        // System.out.println("About manually closed.");
        diAbout.setVisible(false);
        jUninstaller.dispatchEvent(new WindowEvent(jUninstaller, WindowEvent.WINDOW_CLOSING));
      }
    }
  }
  
  // handler for ActionListener
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource().equals(btOk)) {     // OK button on AboutScreen
      diAbout.setVisible(false);
    } else if (ae.getSource().equals(btAbout)) {  // About-Button
      diAbout.setVisible(true);
      btOk.requestFocus();
    } else if (ae.getSource().equals(btQuit)) { // Quit-Button
      dispatchEvent(new WindowEvent(this, 201));
    } else if (ae.getSource().equals(btMoni)) { // Monitor new-Button
      clMain.show(pnMain, "2");
    } else if (ae.getSource().equals(btDele)) { // Delete-Button
      String selItem = ltApps.getSelectedItem();
      if (selItem != null) {
        if (MQ.yesnoBox("Are you sure?", "Do you really want to delete this entry without uninstall?") == MQ.YES) {
          File flDelMe = new File(selItem+DATAEXT);
          if (flDelMe.delete()) {
            MIP.infoPrint("Deleted!");
          } else {
            MIP.infoPrint("Can't delete!");
          }
          updateList(selItem);
        }
      } else {
        MIP.infoPrint("Select one entry!");
      }
    } else if (ae.getSource().equals(btBack)) { // Go Back to main card
      clMain.show(pnMain, "1");
    } else if (ae.getSource().equals(btScan)) {  // Scan-Button
      parseFilesys();
      MQ.msgBox("Scan complete.", "Now install your desired application and RUN IT at least once.\n\nDO NOT INSTALL OR CHANGE ANYTHING ELSE.\n\nIf done, perform one of the two 'Find Changes'.");
    } else if (ae.getSource().equals(btFind)) {  // Find changes-Button
      compareFilesys(false);
    } else if (ae.getSource().equals(btFUp)) {  // Find changes + Update-Button
      compareFilesys(true);
      MQ.msgBox("Log complete.", "You can now install+run the next application.");
    } else if (ae.getSource().equals(btUnin)) {  // Uninstall-Button
      String selItem = ltApps.getSelectedItem();
      if (selItem != null) {
        if (MQ.yesnoBox("Are you sure?", "Do you really want to uninstall?") == MQ.YES) {
          MIP.infoPrint("Uninstalling...");
        }
      } else {
        MIP.infoPrint("Select one entry!");
      }
    } else if (ae.getSource().equals(btView)) {  // View-Button (show list of entries in log)
      String selItem = ltApps.getSelectedItem();
      if (selItem != null) {
        lbView.setText("Details of "+selItem);
        updateDetList(selItem+DATAEXT, null);
        clMain.show(pnMain, "4");
      } else {
        MIP.infoPrint("Select one entry!");
      }
    } else if (ae.getSource().equals(btRena)) {  // Rename-Button (rename log)
      String selItem = ltApps.getSelectedItem();
      if (selItem != null) {
        boolean okay;
        File flNew;
        File flTemp = new File(selItem+DATAEXT);
        String newname = "";
        do {
          okay = false;
          newname = MQ.inputBox("Enter name", "Enter new name for this entry:", ((newname!="")?newname:selItem));
          if (newname != null) {
            flNew = new File(newname+DATAEXT);
            if (newname.equals("")) {
              MIP.infoPrint("Enter a name");
            } else if (flNew.exists()) {
              MIP.infoPrint("Already exists!");
            } else if (!flTemp.renameTo(flNew)) {
              MIP.infoPrint("Invalid name!");
            } else {
              okay = true;
            }
          }
        } while (!okay && newname!=null);
        updateList(newname);
      } else {
        MIP.infoPrint("Select one entry!"); 
      }
    } else if (ae.getSource().equals(btBack2)) { // Back
      clMain.show(pnMain, "1");
    } else if (ae.getSource().equals(btDetails)) {  // Details of log-entry
      String selItem = ltView.getSelectedItem();
      if (selItem != null) {
        String mbt = selItem.substring(selItem.indexOf(" ")+1); 
        File flDet = new File(mbt);
        if (flDet.exists()) {
          mbt += "\n\nSize: "+flDet.length()+" Bytes";
          mbt += "\nLast Modified: "+mfs.getDumpDate(flDet);
        } else {
          mbt += "\n\nFile doesn't exist anymore.";
        }
        MQ.msgBox("File Details", mbt);
      } else {
        MIP.infoPrint("Select one entry!");
      }
    } else if (ae.getSource().equals(btRemove)) {  // Remove (single log-entry)
      String selItem = ltView.getSelectedItem();
      String selFile = lbView.getText().substring(11);
      if (selItem != null && selFile != null) {
        String mbt = selItem.substring(selItem.indexOf(" ")+1); 
        if (mfs.removeLine(selFile+DATAEXT, mbt)) {
          int idx = ltView.getSelectedIndex();
          ltView.remove(selItem);
          ltView.select(idx);
        } else {
          MQ.msgBox("Error", "For some reason there were problems removing this line.");
        }
//        updateDetList(selFile+DATAEXT, selItem);
      } else {
        MIP.infoPrint("Select one entry!");
      }
    } else if (ae.getSource().equals(btDelFile)) {  // Delete file (log-details)
      String selItem = ltView.getSelectedItem();
      String selFile = lbView.getText().substring(11);
      char status = selItem.charAt(0);  // for addition question later
      if (selItem != null && selFile != null) {
        selItem = selItem.substring(selItem.indexOf(" ")+1);
        File flDelMe = new File(selItem);
        if (flDelMe.exists()) {
          if (MQ.yesnoBox("Are you sure?", "Do you really want to delete this file?\n\n"+selItem+"\n\nDeleting the wrong file may render your phone unusable!") == MQ.YES) {
            
            boolean doIt = false;
            if (status == '*') {
              doIt = (MQ.yesnoBox("Are you really sure?", "This file has already been there before the logged installation occurred.\nDeleting it may harm your phone.\n\nDo you still want to delete it?") == MQ.YES); 
            } else {
              doIt = true;
            }
            
            if (doIt) {
              if (flDelMe.delete()) {
                MIP.infoPrint("Deleted.");
                ltView.replaceItem(status+"! "+selItem, ltView.getSelectedIndex());
              } else {
                MIP.infoPrint("Error!");
              }
//              updateDetList(selFile+DATAEXT, selItem);
            } else {
              MIP.infoPrint("Deletion aborted.");
            }
          }
        } else {
          MIP.infoPrint("File does not exist!"); 
        }
      } else {
        MIP.infoPrint("Select one entry!");
      }
    } else if (ae.getSource().equals(btRefresh)) {  // Refresh list (log-details)
      String selFile = ltApps.getSelectedItem();
      String selItem = ltView.getSelectedItem();
      if (selItem != null) selItem = selItem.substring(selItem.indexOf(" ")+1);
      if (selFile != null) {
        updateDetList(selFile+DATAEXT, selItem);
      } else {
        MIP.infoPrint("Error while refreshing!");
      }
    }
    // TODO: more events
  }
  
  public JUninstaller() {                // Constructor
    addWindowListener(new MainWindowAdapter());
    setTitle(APPNAME+" "+APPVERSION+" by Markus Birth");  // set Frame title
    //setResizable(false);
    setSize(WND_W, WND_H);   // set Frame size
    setLocation((dmScreen.width-WND_W)/2, (dmScreen.height-WND_H)/2); // center Frame
    MIP.busy(APPNAME+" loading...");
    doAbout();
    
    btAbout.addActionListener(this);
    btQuit.addActionListener(this);
    btMoni.addActionListener(this);
    btDele.addActionListener(this);
    btUnin.addActionListener(this);
    btRena.addActionListener(this);
    btView.addActionListener(this);
    
    updateList(null);
    
    pnMain.setFont(ftPlain8);
    btAbout.setFont(ftBold12);
    
    pnMain1.add(btUnin);
    pnMain1.add(btView);

    pnMain2.add(btDele);
    pnMain2.add(btRena);

    pnUnin2.add(pnMain1);
    pnUnin2.add(btMoni);
    pnUnin2.add(pnMain2);
    pnUnin2.add(btQuit);
    
    pnUnin.add(btAbout, BorderLayout.NORTH);
    pnUnin.add(ltApps, BorderLayout.CENTER);
    pnUnin.add(pnUnin2, BorderLayout.SOUTH);

    btScan.addActionListener(this);
    btFind.addActionListener(this);
    btFUp.addActionListener(this);
    btBack.addActionListener(this);
    
    lbLast.setText("Last scan: "+mfs.getDumpDate());
    pnMoni.add(lbLast);
    pnMoni.add(btScan);
    pnMoni.add(btFind);
    pnMoni.add(btFUp);
    pnMoni.add(btBack);
    
    lbSIP.setFont(ftBold12);
    lbPW.setFont(ftBold12);
    lbTime.setFont(ftBold12);
    pnScan.add(lbSIP);
    pnScan.add(lbPW);
    pnScan.add(lbTime);

    btDetails.addActionListener(this);
    btRemove.addActionListener(this);
    btDelFile.addActionListener(this);
    btBack2.addActionListener(this);
    btRefresh.addActionListener(this);
    lbView.setFont(ftBold12);
    pnView.add(lbView, BorderLayout.NORTH);
    pnView.add(ltView, BorderLayout.CENTER);
    pnViewC.add(btDetails);
    pnViewC.add(btRemove);
    pnViewD.add(btRefresh);
    pnViewD.add(btDelFile);
    pnDel.add(pnViewC, BorderLayout.CENTER);
    pnDel.add(pnViewD, BorderLayout.EAST);
    pnViewB.add(pnDel, BorderLayout.CENTER);
    pnViewB.add(btBack2, BorderLayout.SOUTH);
    pnView.add(pnViewB, BorderLayout.SOUTH);

    pnMain.add(pnUnin, "1"); // Main view
    pnMain.add(pnMoni, "2"); // "Monitor new" view
    pnMain.add(pnScan, "3"); // Progress view (Scanning ... please wait ...)
    pnMain.add(pnView, "4"); // Details-view
    
    add(pnMain);
    
    // TODO: more initialization commands
    show(); // automagically calls paint(Graphics g)
    MIP.hide(); // init done, hide infoPrint
  }
  
  private void parseFilesys() {
    // parse filesystem (all readable drives) and store
    // filenames, sizes, date/times in a datafile
    
    lbSIP.setText("=== SCAN IN PROGRESS ===");
    clMain.show(pnMain, "3");
    MIP.busy("Scan in progress");
    TimerThread thTimer = new TimerThread();
    thTimer.start();
    try {
      mfs.dumpAllDrives();
      MIP.infoPrint("Scan complete.");
    } catch (Exception ex) {
      MIP.infoPrint("Exception!");
      ex.printStackTrace();
      if (!mfs.delDump()) { MIP.infoPrint("Delete log failed"); }
    }
    thTimer.setStop(true);
    clMain.show(pnMain, "2");
    lbLast.setText("Last scan: "+mfs.getDumpDate());
  }
  
  private void updateList(String selItem) {
    pnUnin.remove(ltApps);
    pnUnin.validate();
    String[] apps = mfs.getMonitored(DATAEXT);
    ltApps.removeAll();
    int selIdx = 0;
    if (selItem != null) selItem = selItem.toLowerCase();
    for (int i=0;i<apps.length;i++) {
      ltApps.add(apps[i]);
      if (selItem != null && apps[i].toLowerCase().compareTo(selItem)>=0) {
        selIdx = ltApps.getItemCount()-1;
        selItem = null;
      }
    }
    ltApps.makeVisible(selIdx);
    ltApps.select(selIdx);
    pnUnin.add(ltApps, BorderLayout.CENTER);
    pnUnin.validate();
    MIP.infoPrint(apps.length+" logs");
  }
  
  private void updateDetList(String log, String selItem) {
    pnView.remove(ltView);
    pnView.validate();
    ltView.removeAll();
    MIP.busy("Reading...");
    String[] entries = mfs.getEntries(log);
    MIP.busy("Building list (" + entries.length + ")...");
    int selIdx = 0;
    if (selItem!=null) selItem = selItem.substring(selItem.indexOf(" ")+1).toLowerCase();
    String cmpItem;
    for (int i=0;i<entries.length;i++) {
      ltView.add(entries[i]);
      cmpItem = entries[i].substring(entries[i].indexOf(" ")+1).toLowerCase();
      if (selItem != null && cmpItem.compareTo(selItem)>=0) {
        selIdx = ltView.getItemCount()-1;
        selItem = null;
      }
    }
    ltView.makeVisible(selIdx);
    ltView.select(selIdx);
    pnView.add(ltView, 1);
    pnView.validate();
    MIP.infoPrint(entries.length+" entries");
  }

  private void compareFilesys(boolean redump) {
    // parse filsystem and compare with
    // previously generated info

    long diffs = 0;
    boolean okay = false;
    String newname = "";
    File flTemp = new File("JUninstaller.$$$");
    File flNew;
    
    lbSIP.setText("=== COMPARING FILES ===");
    clMain.show(pnMain, "3");
    MIP.busy("Comparison in progress");
    TimerThread thTimer = new TimerThread();
    thTimer.start();
    try {
      diffs = mfs.dumpDifferences("JUninstaller.$$$", redump);
      MIP.infoPrint("Comparison complete.");
    } catch (Exception ex) {
      MIP.infoPrint("Exception!");
      ex.printStackTrace();
    }
    thTimer.setStop(true);
    if (diffs > 0) {
      MIP.infoPrint(diffs+" changes found.");
      do {
        okay = false;
        newname = MQ.inputBox("Enter name", "Enter a name for this entry:", newname);
        if (newname != null) {
          flNew = new File(newname+DATAEXT);
          if (newname.equals("")) {
            MIP.infoPrint("Enter a name");
          } else if (flNew.exists()) {
            MIP.infoPrint("Already exists!");
          } else if (!flTemp.renameTo(flNew)) {
            MIP.infoPrint("Invalid name!");
          } else {
            okay = true;
          }
        }
      } while (!okay && newname!=null);

      if (!okay || newname==null) {
        if (!flTemp.delete()) {
          MIP.infoPrint("Could not delete!");
        }
      }
      updateList(newname);
      clMain.show(pnMain, "1");
    } else {
      MIP.infoPrint("No diffs found.");
      File flDiff = new File("JUninstaller.$$$");
      if (!flDiff.delete()) { MIP.infoPrint("Delete failed"); }
      clMain.show(pnMain, "2");
    }
  }
  
  class TimerThread extends Thread {
    private boolean stopNow;
    private long startTime;
    
    public void run() {
      stopNow = false;
      startTime = System.currentTimeMillis();
      long diffTime;
      long min;
      long sec;
      long mil;
      do {
        diffTime = System.currentTimeMillis()-startTime;
        min = diffTime/60000;
        sec = (diffTime/1000)%60;
        mil = (diffTime/10)%100;
        lbTime.setText(min+":"+((sec<10)?"0":"")+sec+"."+((mil<10)?"0":"")+mil);
        try {
          Thread.sleep(500);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      } while (!stopNow);
    }
    
    public void setStop(boolean n) {
      stopNow = n;
    }
    
  }
  
  public static void main(String args[]) {
    try {
      jUninstaller = new JUninstaller();
    } catch (Exception ex) {
      System.out.println("Caught exception: "+ex.toString());
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  public final void doAbout() {
    btOk.addActionListener(this);
    pnAboutButt.add(btOk);
    lbAbout1.setFont(ftBold12);
    lbAbout2.setFont(ftPlain10);
    lbAbout3.setFont(ftPlain10);
    lbAbout4.setFont(ftPlain10);
    diAbout.setLayout(new BorderLayout());
    diAbout.setBackground(SystemColor.control);
    pnAboutText.add(lbAbout1);
    pnAboutText.add(lbAbout2);
    pnAboutText.add(lbAbout3);
    pnAboutText.add(lbAbout4);
    diAbout.add(pnAboutText, BorderLayout.CENTER);
    diAbout.add(pnAboutButt, BorderLayout.SOUTH);
    diAbout.addWindowListener(new DummyWindowAdapter());
    diAbout.pack(); // without it, the Dialog won't get displayed!!
    Dimension dmAboutBox = diAbout.getSize();
    Dimension dmWindow = this.getSize();
    Point ptWindow = this.getLocation();
    diAbout.setLocation(ptWindow.x+(dmWindow.width-dmAboutBox.width)/2, ptWindow.y+dmWindow.height-dmAboutBox.height);
  }
  
}
