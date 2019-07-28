package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class ShovelTile extends Quad implements Sprite {

	public final static int cover = 0, uncover = 1;
	
	private BufferedImage currentFrame;
	private int contents, state, layer;
	private int x_offset, y_offset, col_width, col_height;

	public ShovelTile(Point p, int thisState, int theseContents) {
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
	
	public void init(SpriteImageLoader loader){
		currentFrame = loader.getBinaryFrame(SpriteImageLoader.ShovelTile_INDEX, state);
	}

	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}
	
	public BufferedImage getCurrentFrame() { return currentFrame; }

	public void animate(SpriteImageLoader loader) { 
		state = uncover;
		currentFrame = loader.getBinaryFrame(SpriteImageLoader.ShovelTile_INDEX, state);
	}
	
	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public int getLayer() { return layer; }
	
	public int getContents() { return contents; }
	
	//Unimplemented Methods
	public int getIdentity(){ return 0; }
	public void moveY(double delta) { }
	public void moveX(double delta) {	}
	public void resetState() {	}
	public void setState(int newState) {	}
	public int getState() { return 0; }
}
