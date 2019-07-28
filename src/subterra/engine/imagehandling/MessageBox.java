package subterra.engine.imagehandling;

import java.util.ArrayList;

	//Unportable libaries
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class MessageBox extends Quad implements Sprite {

	public static final double defaultWidth = 159, defaultHeight = 99;
	private static final int horbuff = 8, vertbuff = 5;
	private static final String BLANK_CHAR = "~~";
	
	private BufferedImage msgFrame;
	private String message;
	private ArrayList<String> formatMsg;
	private boolean finished;
	private int linesPerBox, currentLine, linesLeft, linesRead;

	public MessageBox(Point p, String msg) {
		super(p.getX(), p.getY(), defaultWidth, defaultHeight); 
		message = msg;
	}
	
	public void init(SpriteImageLoader loader){
		formatMsg = new ArrayList<String>();
		loader.loadMessage();
		msgFrame = loader.getMessageFrame();
		finished = false; currentLine = 0;
		formatMessage(); determineLinesPerBox();
		drawMessage();
	}
	
	private void formatMessage(){
		String[] msg = message.split("\\s");
		int words = msg.length;
		double linelength = defaultWidth - 2*horbuff;
		formatMsg = new ArrayList<String>();
		Graphics2D g2 = (Graphics2D) msgFrame.getGraphics();
		FontMetrics fm = g2.getFontMetrics();
		String line = null, previousline = null;
		for(int i = 0; i < words; i++){
			previousline = line;
			if(!msg[i].equals(BLANK_CHAR)) {
				if(line == null) { line = msg[i]; }
				else{line = line + " " + msg[i]; }
				if(fm.stringWidth(line) >= linelength){
					if(previousline!=null){  formatMsg.add(previousline); line = msg[i]; }
				}
			}
			else { formatMsg.add(previousline); formatMsg.add(" "); line = null; }
		}
		if(line != null) { formatMsg.add(line); }
	}
	
	
	private void determineLinesPerBox(){
		double h = this.getHeight();
		double wh = msgFrame.getGraphics().getFontMetrics().getHeight() + vertbuff;
		linesPerBox = (int) Math.floor((h / wh));
		if(formatMsg.size() - linesPerBox > 0){ linesLeft = formatMsg.size() - linesPerBox;	}
		else { linesLeft = 0;}
		linesRead = 0;
	}
	
	private void drawMessage(){
		Graphics2D g2 = (Graphics2D) msgFrame.getGraphics();
		double wh = g2.getFontMetrics().getHeight()+vertbuff;
		if(formatMsg.size() <= linesPerBox) {
			int i = 0;
			for(String line : formatMsg){
				g2.drawString(line, horbuff, (int) ((i+1)*wh));
				i++;
			}
		}
		else{
			if(formatMsg.size() - currentLine <= linesPerBox){
				int counter = 0;
				for(int i = currentLine; i <= (formatMsg.size()-1); i++){
					String text = formatMsg.get(i);
					g2.drawString(text, horbuff, (int) ((counter+1)*wh));
					counter++;
				}
			}
			else{
				int counter = 0;
				for(int i = currentLine; i <= (currentLine + linesPerBox-1); i++){
					String text = formatMsg.get(i);
					g2.drawString(text, horbuff, (int) ((counter+1)*wh));
					counter++;
				}
			}
		}
	}
	
	public int getLinesLeft() { return linesLeft; }
	
	public int getLinesRead() { return linesRead; } 
	
	public int getLines() { return formatMsg.size(); }
	
	public void nextLine(SpriteImageLoader l){ 
		if(linesLeft >= 1 ){
			linesRead ++;
			currentLine++; 
			linesLeft--;
			l.loadMessage(); // need to reload every time so message is blank
			msgFrame = l.getMessageFrame();
			drawMessage();
		} else { finished = true; }
	}
	
	public void previousLine(SpriteImageLoader l){
		if(formatMsg.size()>linesPerBox){
			if(currentLine > 0){
				currentLine--;
				linesRead--;
				linesLeft++;
				l.loadMessage(); // need to reload every time so message is blank
				msgFrame = l.getMessageFrame();
				drawMessage();
			}
		}
	}
	
	public void overrideHeight(double h){
		super.setHeight(h);
		BufferedImage newBox = new BufferedImage((int) this.getWidth(), 
												 (int) h, 
												 BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = newBox.getGraphics();
		g.drawImage(msgFrame, 0, 0, (int) this.getWidth(), (int) this.getHeight(), 
				    0, 0, msgFrame.getWidth(), msgFrame.getHeight(), null );
		msgFrame = newBox; formatMessage();
	}

	public void overrideWidth(double w){
		super.setWidth(w);
		super.setHeight(h);
		BufferedImage newBox = new BufferedImage((int) w, 
												 (int) this.getHeight(), 
												 BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = newBox.getGraphics();
		g.drawImage(msgFrame, 0, 0, (int) this.getWidth(), (int) this.getHeight(), 
				    0, 0, msgFrame.getWidth(), msgFrame.getHeight(), null );
		msgFrame = newBox; formatMessage();
	}
	
	public boolean isFinished() { return finished; }
	
	public BufferedImage getCurrentFrame() { return msgFrame; }
	
	//Unimplemented methods: 
	public int getIdentity(){ return 0; }
	public void resetState() {	}
	public void setState(int newState) { }
	public int getState() { return 0; }
	public ArrayList<Line> getBounds() { return null; }
	public Quad getArea() { return this;}
	public void moveX(double delta) { }
	public void moveY(double delta) { }
	public boolean intersectsSprite(Sprite s) { return false; }
	public void animate(SpriteImageLoader loader) {	}
	public Quad getCollisionBox() { return null; }
	public void setLayer(int newLayer) { }
	public int getLayer() { return 0; }

}
