package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Gate extends Quad implements Sprite {

	public final static int impassable = 0, passable = 1;
	
	private BufferedImage frame; 
	private int state, layer, anchor;
	private int x_offset, y_offset, col_width, col_height;
	
	public Gate(Point p, int anc) {
		super(p.getX(), p.getY(), 0, 0);
		anchor = anc; state = impassable;
	}

	public void configureAttributes(int thisWidth, int thisHeight){
		super.setWidth(thisWidth); super.setHeight(thisHeight);
	}
	
	public void configureCollisions(int thisX, int thisY, int thisColW, int thisColH){
		x_offset = thisX; y_offset = thisY;
		col_width = thisColW; col_height = thisColH;
	}
	
	public void init(SpriteImageLoader loader){
		frame = loader.getBinaryFrame(SpriteImageLoader.Gate_INDEX, state);
	}

	public BufferedImage getCurrentFrame() { return frame; }

	public void animate(SpriteImageLoader loader) {
		if(state == impassable) { state = passable; }
		else if (state == passable) { state = impassable; }
		frame = loader.getBinaryFrame(SpriteImageLoader.Gate_INDEX, state);
	}

	public void setState(int newState) { state = newState; }

	public void setLayer(int newLayer) { layer = newLayer; }

	public int getState() { return state; }

	public int getLayer() { return layer;  }
	
	public int getAnchor() { return anchor; }
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}
	
	//Unimplemented Methods
	public void resetState() {	}
	public int getIdentity() { return 0; }
	public void moveX(double delta) { }
	public void moveY(double delta) { }

}
