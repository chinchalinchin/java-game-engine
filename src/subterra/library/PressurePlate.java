package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class PressurePlate extends Quad implements Sprite {

	public final static int unpressed = 0, pressed = 1;
	private BufferedImage frame;
	private int layer, state, anchor, identity;
	private int x_offset, y_offset, col_width, col_height;
	
	public PressurePlate(Point p, int link) {
		super(p.getX(), p.getY(), 0, 0);
		state = unpressed; anchor = link;
		identity = NO_IDENTITY; 
	}
	
	public void configureAttributes(int thisWidth, int thisHeight){
		super.setWidth(thisWidth); super.setHeight(thisHeight);
	}
	
	public void configureCollisions(int thisX, int thisY, int thisColW, int thisColH){
		x_offset = thisX; y_offset = thisY;
		col_width = thisColW; col_height = thisColH;
	}
	
	public void init(SpriteImageLoader loader) { frame = loader.getBinaryFrame(SpriteImageLoader.Plate_INDEX, state); }

	public BufferedImage getCurrentFrame() {  return frame; }

	public void animate(SpriteImageLoader loader) { 
		if(state == pressed) { state = unpressed; }
		else if (state == unpressed) { state = pressed; }
		frame = loader.getBinaryFrame(SpriteImageLoader.Plate_INDEX, state); 
	}

	public void resetState() { state = unpressed; }

	public void setState(int newState) { state = newState; }

	public void setLayer(int newLayer) { layer = newLayer; }
	
	public void setIdentity(int newIdent) { identity = newIdent; }

	public int getState() { return state; }
	
	public int getLayer() { return layer; }
	
	public int getAnchor() { return anchor; }
	
	public int getIdentity() { return identity; }

	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}
	
	//Unimplemented Methods
	public void moveX(double delta) { }
	public void moveY(double delta) {	}

}
