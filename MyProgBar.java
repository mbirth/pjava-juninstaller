import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class MyProgBar extends Component {
  int minVal;
  int maxVal;
  int curVal;
  Color fgColor = new Color(0,0,128);
  Color bgColor = Color.lightGray;
  Color txColor = Color.white;
  int Height = 18;
  int Width = 40;
  
  public MyProgBar(int min, int max, int pos) {
    // super();
    this.minVal = min;
    this.maxVal = max;
    this.curVal = pos;
  }
    
  public MyProgBar(int min, int max) {
    // super();
    this(min, max, 0);
  }
  
  public MyProgBar() {
    // super();
    this(0, 100, 0);
  }
    
  public void paint(Graphics g) {
    System.out.println("Object is "+getSize().width+"x"+getSize().height);
    double percentage = (double)(curVal-minVal)/(double)(maxVal-minVal);
    String percString = String.valueOf((int)(percentage*100)) + "%";
    g.setFont(new Font("Dialog", Font.PLAIN, 10));
    FontMetrics fm = getFontMetrics(g.getFont());
    g.setColor(bgColor);
    g.fill3DRect(0,0,getSize().width-1,getSize().height-1, false);
    g.setColor(fgColor);
    g.fill3DRect(0,0,(int)((double)(getSize().width-1)/(maxVal-minVal)*(curVal-minVal)),getSize().height-1, true);
    g.setXORMode(txColor);
    g.drawString(percString, getSize().width/2-fm.stringWidth(percString)/2, getSize().height/2-fm.getHeight()/2+fm.getAscent());
    g.setPaintMode();
    g.setColor(Color.black);
    g.drawRect(0,0,getSize().width-1,getSize().height-1);
  }
  
  public void setMinValue(int v) {
    minVal = v;
  }
    
  public void setMaxValue(int v) {
    maxVal = v;
  }
    
  public void setPos(int p) {
    curVal = p;
    paint(getGraphics());
  }
  
  public Dimension getPreferredSize() {
    return new Dimension(Width, Height);
  }
  
  public Dimension getMinimumSize() {
    return new Dimension(Width, Height);
  }
  
}
