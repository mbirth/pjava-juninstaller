import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MyQuestions implements ActionListener {
  public final static int YES = 1;
  public final static int NO = -1;
  
  Frame parent;
  Font ftDialog;
  Dialog diDialog;
  Button btOK = new Button();
  Button btCancel = new Button();
  Panel pnButtons = new Panel(new FlowLayout(FlowLayout.RIGHT));
  TextField tfInput = new TextField();
  int maxWidth = 200;
  int result = 0;
  String userinput = null;
  WrapperDefs[] wd;
  
  public MyQuestions(Frame p, Font ft) {
    parent = p;
    ftDialog = ft;
    btOK.addActionListener(this);
    btCancel.addActionListener(this);
    wd = new WrapperDefs[3];
    wd[0] = new WrapperDefs(" ", true);
    wd[1] = new WrapperDefs("\\", false, true);
    wd[2] = new WrapperDefs("-", false, false);
  }
  
  public MyQuestions(Frame p) {
    this(p, new Font("Dialog", Font.PLAIN, 8));
  }
  
  private int getLastWrap(String text) {
    for (int i=text.length()-1;i>0;i--) {
      for (int j=0;j<wd.length;j++) {
        if (text.substring(i,i+wd[j].w.length()).equals(wd[j].w)) {
          return i;
        }
      }
    }
    return -1;
  }
  
  private Rectangle getBnds(Dialog di) {
    Rectangle rtWindow = parent.getBounds();
    Rectangle rtResult = di.getBounds();
    rtResult.x = rtWindow.x+(rtWindow.width-rtResult.width)/2;
    rtResult.y = rtWindow.y+rtWindow.height-rtResult.height;
    return rtResult;
  }
  
  private void makeText(Dialog di, String text) {
    String[] txts = new String[1];
    String[] tmps;
    txts[0] = text;

    // convert newline character
    while (txts[txts.length-1].indexOf("\n") != -1) {
      tmps = new String[txts.length+1];
      for (int i=0;i<txts.length;i++) {
        tmps[i] = txts[i];
      }
      tmps[tmps.length-2] = txts[txts.length-1].substring(0,txts[txts.length-1].indexOf("\n"));
      tmps[tmps.length-1] = txts[txts.length-1].substring(txts[txts.length-1].indexOf("\n")+1);
      txts = tmps;
    }

    // make each line below maxWidth as long as there is a whitespace in it
    int idx;
    FontMetrics fm = di.getFontMetrics(ftDialog);
    boolean addedLine;
    for (int i=0;i<txts.length;i++) {
      txts[i] = txts[i].trim();
      addedLine = false;
      while (fm.stringWidth(txts[i]) > maxWidth && txts[i].indexOf(" ") != -1) {
        if (!addedLine) {
          tmps = new String[txts.length+1];
          for (int j=0;j<txts.length;j++) {
            if (j<=i) tmps[j] = txts[j];
              else tmps[j+1] = txts[j];
          }
          tmps[i+1] = "";
          txts = tmps;
          addedLine = true;
        }
        // auto-lengthen txts, if there is more space needed
        if (i+1>=txts.length) {
          tmps = new String[txts.length+1];
          for (int j=0;j<txts.length;j++) {
            tmps[j] = txts[j];
          }
          tmps[tmps.length-1] = "";
          txts = tmps;
        }
        // wrap word to next line
        idx = txts[i].lastIndexOf(" ");
        txts[i+1] = txts[i].substring(idx+1) + " " + txts[i+1];
        txts[i] = txts[i].substring(0, idx);
      }
    }
    
    // create Panel with Labels and add to Dialog
    Panel pn = new Panel(new GridLayout(0,1,0,0));
    Label[] lbs = new Label[txts.length];
    for (int i=0;i<lbs.length;i++) {
      lbs[i] = new Label(txts[i]);
      pn.add(lbs[i]);
    }
    di.add(pn, BorderLayout.NORTH);
  }

  public void msgBox(String title, String text, String buttext) {
    btOK.setLabel(buttext);
    diDialog = new Dialog(parent, title, true);
    diDialog.setFont(ftDialog);
    diDialog.setLayout(new BorderLayout());
    diDialog.setBackground(SystemColor.control);
    pnButtons.removeAll();
    pnButtons.add(btOK);
    makeText(diDialog, text);
    diDialog.add(pnButtons, BorderLayout.SOUTH);
    diDialog.pack();
    diDialog.setBounds(getBnds(diDialog));
    diDialog.show();
  }
  
  public void msgBox(String title, String text) {
    msgBox(title, text, "OK");
  }
  
  public String inputBox(String title, String text, String definput, String yestext, String notext) {
    btOK.setLabel(yestext);
    btCancel.setLabel(notext);
    tfInput.setText(definput);
    diDialog = new Dialog(parent, title, true);
    diDialog.setFont(ftDialog);
    diDialog.setLayout(new BorderLayout());
    diDialog.setBackground(SystemColor.control);
    pnButtons.removeAll();
    pnButtons.add(btCancel);
    pnButtons.add(btOK);
    makeText(diDialog, text);
    diDialog.add(tfInput, BorderLayout.CENTER);
    diDialog.add(pnButtons, BorderLayout.SOUTH);
    diDialog.pack();
    diDialog.setBounds(getBnds(diDialog));
    diDialog.show();
    if (result == YES) {
      return tfInput.getText();
    } else {
      return null;
    }
  }
  
  public String inputBox(String title, String text, String definput) {
    return inputBox(title, text, definput, "OK", "Cancel");
  }
  
  public String inputBox(String title, String text) {
    return inputBox(title, text, "");
  }
  
  public int yesnoBox(String title, String text, String yestext, String notext) {
    btOK.setLabel(yestext);
    btCancel.setLabel(notext);
    diDialog = new Dialog(parent, title, true);
    diDialog.setFont(ftDialog);
    diDialog.setLayout(new BorderLayout());
    diDialog.setBackground(SystemColor.control);
    pnButtons.removeAll();
    pnButtons.add(btCancel);
    pnButtons.add(btOK);
    makeText(diDialog, text);
    diDialog.add(pnButtons, BorderLayout.SOUTH);
    diDialog.pack();
    diDialog.setBounds(getBnds(diDialog));
    diDialog.show();
    return result;
  }
  
  public int yesnoBox(String title, String text) {
    return yesnoBox(title, text, "Yes", "No");
  }
  
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource().equals(btOK)) {
      result = YES;
      diDialog.setVisible(false);
    } else if (ae.getSource().equals(btCancel)) {
      result = NO;
      diDialog.setVisible(false);
    }
  }


  class WrapperDefs {
    String w;
    boolean remove;
    boolean before;
    
    public WrapperDefs(String w) {
      this(w, false, false);
    }
    
    public WrapperDefs(String w, boolean r) {
      this(w, r, false);
    }
    
    public WrapperDefs(String w, boolean r, boolean b) {
      this.w = w;
      remove = r;
      before = b;
    }
  }
  
}