package subterra.library;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

/**
 * 
 * @author chinchalinchin
 *
 *@description the Switch acts as an anchor to a LightSource. It's state determines whether 
 *an anchored LightSource contributes to the clipping done in the View class.
 *
 */
public class Switch extends Quad implements Sprite {

	public static final int on = 0, off = 1;
	
	private BufferedImage currentFrame;
	private int state, switchAnchor, layer;
	private int x_offset, y_offset, col_width, col_height;
	
	/**
	 * 
	 * @param p
	 * @param d
	 * @param thisType
	 * @param thisState
	 * @param anchor
	 */
	public Switch(Point p, int thisState, int anchor) {
		super(p.getX(), p.getY(), 0, 0);
		state = thisState;
		switchAnchor = anchor;
	}
	
	public void configureAttributes(int thisWidth, int thisHeight){
		super.setWidth(thisWidth); super.setHeight(thisHeight);
	}
	
	public void configureCollisions(int thisX, int thisY, int thisColW, int thisColH){
		x_offset = thisX; y_offset = thisY;
		col_width = thisColW; col_height = thisColH;
	}
	
	public void init(SpriteImageLoader loader){
		currentFrame = loader.getBinaryFrame(SpriteImageLoader.Switch_INDEX, state);
	}

	public BufferedImage getCurrentFrame() { return currentFrame; }

	public void animate(SpriteImageLoader loader) {
		if(state == on) { state =off; }
		else if (state ==off) { state = on; }
		currentFrame = loader.getBinaryFrame(SpriteImageLoader.Switch_INDEX, state);
	}

	public void setState(int newState) {state = newState; }

	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public int getState() {return state; }
	
	public int getLayer() { return layer; }
	
	public int getAnchor() { return switchAnchor; }
	
	public ArrayList<Line> getBounds(){
		ArrayList<Line> theseLines = new ArrayList<Line>();
		theseLines.add(super.getTopBound());
		theseLines.add(super.getBottomBound());
		theseLines.add(super.getRightBound());
		theseLines.add(super.getLeftBound());
		return theseLines;
	}
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}
	
	//Unimplemented methods
	public int getIdentity(){ return 0; }
	public void moveX(double delta) {}
	public void moveY(double delta) {}
	public void resetState() { }

}
