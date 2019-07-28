package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Hackbox extends Quad implements Sprite {

	public final static int unhacked = 0, hacked = 1;
	
	private BufferedImage frame;
	private int state, layer;
	private int x_offset, y_offset, col_width, col_height;
	
	public Hackbox(Point p, int st) {
		super(p.getX(), p.getY(), 0, 0);
		state = st;
	}
	
	public void configureAttributes(int thisWidth, int thisHeight){
		super.setWidth(thisWidth); super.setHeight(thisHeight);
	}
	
	public void configureCollisions(int thisX, int thisY, int thisColW, int thisColH){
		x_offset = thisX; y_offset = thisY;
		col_width = thisColW; col_height = thisColH;
	}
	
	public void init(SpriteImageLoader loader){ frame = loader.getBinaryFrame(SpriteImageLoader.Hack_INDEX, state); }

	public BufferedImage getCurrentFrame() { return frame; }

	public void animate(SpriteImageLoader loader) {
		state = hacked;
		frame = loader.getBinaryFrame(SpriteImageLoader.Hack_INDEX, state);
	}

	public void resetState() { state = unhacked; }
	
	public void setState(int newState) { state = newState; }

	public void setLayer(int newLayer) { layer = newLayer; }

	public int getState() { return state; }

	public int getLayer() { return layer; }
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}

	//Unimplemented Methods
	public void moveX(double delta) { }
	public void moveY(double delta) {	}
	public int getIdentity() {	return 0; }

}
