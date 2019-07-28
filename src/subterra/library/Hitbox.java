package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class Hitbox extends Quad 
					implements Sprite {

	public static final int regular = 0, pushable = 1, destructible = 2;
	
	private int state, layer;
	//TODO: Destructible hitbox. Special walls that animate and remove
	//hitboxes from game world.
	
	public Hitbox(Point p, Point d, int thisState) {
		super(p.getX(), p.getY(), d.getX(), d.getY());
		state = thisState;
	}

	public void moveX(double delta) { }

	public void moveY(double delta) { }
	
	public void setState(int newState) { state = newState; }

	public int getState() {	return state; }

	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public int getLayer() { return layer; }
	
	public Quad getCollisionBox() { return this; }
	
	//Unimplemented Methods
	public int getIdentity(){ return 0; }
	public void resetState() { }
	public void animate(SpriteImageLoader loader) { }
	public BufferedImage getCurrentFrame() { return null; }

}
