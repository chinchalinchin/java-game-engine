package subterra.engine.imagehandling.menu;

import java.awt.Color; 
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class MainButton extends Quad implements Sprite {

	/**
	 * @description buttons used in the main menu. holds an internal state called active
	 * that when flip changes the Frame returned by the getCurrentFrame method.
	 * 
	 * MainButton Sprites must load their own images since the menu is created 
	 * before the GameWorld has been initialized. See Cradle for more details. 
	 */
	//Button States
	public static final int active = 0;
	public static final int inactive = 1;
	public static final String menupath = imgpath + "menu" + sep;
	public static final String activepath = menupath + "game_button_active.png";
	public static final String inactivepath = menupath + "game_button_inactive.png";
	
	//DefaultDimensions
	public static final int defaultWidth = 114;
	public static final int defaultHeight = 26;
	
	//Class Fields
	private BufferedImage activeFrame;
	private BufferedImage inactiveFrame;
	private int state;
	private String displayText;

	public MainButton(Point p, String dText) {
		super(p.getX(), p.getY(), defaultWidth, defaultHeight); 
		state = 1; displayText = dText;
		makeFrames(); 
		}

	private void makeFrames(){
		BufferedImage bfa = null, bfi = null;
		try{ 
			bfa = ImageIO.read(new File(activepath));
			bfi = ImageIO.read(new File(inactivepath));
			if(bfa != null) { 
				activeFrame = bfa;
				Graphics2D g2 = (Graphics2D) bfa.getGraphics();
				int boundY = (defaultHeight - g2.getFontMetrics().getHeight())/2;
				int boundX = (defaultWidth - g2.getFontMetrics().stringWidth(displayText))/2;
				g2.setColor(Color.white);
				g2.drawString(displayText, boundX, boundY + defaultHeight/2);
				g2.dispose();
			}
			if(bfi != null) { 
				inactiveFrame = bfi; 
				Graphics2D g2 = (Graphics2D) bfi.getGraphics();
				int boundY = (defaultHeight - g2.getFontMetrics().getHeight())/2;
				int boundX = (defaultWidth - g2.getFontMetrics().stringWidth(displayText))/2;
				g2 = (Graphics2D) bfi.getGraphics();
				g2.setColor(Color.black);
				g2.drawString(displayText, boundX, boundY + defaultHeight/2);
				g2.dispose();
			}
		} catch(IOException ie) { System.out.println("Error loading: " + activepath + " " + inactivepath);}
	}

	public BufferedImage getCurrentFrame() { 
		if(state == active){ return activeFrame; }
		else if(state == inactive) { return inactiveFrame; }
		else { return null; }
	}

	public void resetState() {	state = 0; }

	public void setState(int newState) { state = newState; }

	public int getState() { return state; } 
	
	public ArrayList<Line> getBounds() { 
		ArrayList<Line> theseLines = new ArrayList<Line>();
		theseLines.add(super.getTopBound());
		theseLines.add(super.getBottomBound());
		theseLines.add(super.getRightBound());
		theseLines.add(super.getLeftBound());
		return theseLines;
	}

	public Quad getArea() {
		return new Quad(this.getMinX(), this.getMinY(), 
				this.getWidth(), this.getHeight());
	}

	//Unimplemented methods
	public int getIdentity(){ return 0; }
	public void animate(SpriteImageLoader loader) {	}
	public Quad getCollisionBox() { return null; }
	public void setLayer(int newLayer) { }
	public int getLayer() { return 0; }
	public void moveX(double delta) {	}
	public void moveY(double delta) {	}
	public boolean intersectsSprite(Sprite s) { return false; }

}
