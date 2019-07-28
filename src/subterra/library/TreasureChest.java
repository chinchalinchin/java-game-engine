package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class TreasureChest extends Quad implements Sprite {

	public final static int closed = 0, open = 1;
	
	private int contents, state, layer;
	private int x_offset, y_offset, col_width, col_height;
	private BufferedImage currentFrame;

	public TreasureChest(Point p, int theseContents, int thisState) {
		super(p.getX(), p.getY(), 0, 0);
		contents = theseContents;
		state = thisState;
	}
	
	public void configureAttributes(int thisWidth, int thisHeight){
		super.setWidth(thisWidth); super.setHeight(thisHeight);
	}
	
	public void configureCollisions(int thisX, int thisY, int thisColW, int thisColH){
		x_offset = thisX; y_offset = thisY;
		col_width = thisColW; col_height = thisColH;
	}
	
	public void init(SpriteImageLoader loader){ currentFrame = loader.getBinaryFrame(SpriteImageLoader.TreasureChest_INDEX, state); }
	
	public int getContents(){ return contents; }

	public BufferedImage getCurrentFrame() { return currentFrame; }

	public void animate(SpriteImageLoader loader) {  
		state = open; 
		currentFrame = loader.getBinaryFrame(SpriteImageLoader.TreasureChest_INDEX, state);
	}
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}
	
	public int getState() { return state; }
	
	public void setLayer(int whichLayer) { layer = whichLayer; }

	public int getLayer() { return layer; }
	
	//Unimplemented methods
	public void moveX(double delta) { }
	public void moveY(double delta) { }
	public void resetState() {	}
	public void setState(int newState) { }
	public int getIdentity(){ return 0; }

}
