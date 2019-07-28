package subterra.library;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import subterra.engine.GameWorld;
import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Projectile extends Quad implements Sprite {

	public final static int ALIVE = 0, DEAD = 1;
	public final static int types = 1;
	public final static int bolt = 0;
	public final static boolean FRIEND = true, FOE = false;
	
	private final static int BOLT_WIDTH = 41, BOLT_HEIGHT = 10, BOLT_SPEED = 15, BOLT_DAMAGE = 5; //default direction left
	
	private BufferedImage frame;
	private int state, layer, type, direction, relativeVelocity;
	private boolean FriendorFoe;
	
	public Projectile(Point p, int ty, int dir, boolean ForF) {
		super(p.getX(), p.getY(), 0, 0);
		state = ALIVE; type = ty; direction = dir;
		frame = null; FriendorFoe = ForF; relativeVelocity = 0;
		switch(ty){
			case bolt:
				if(direction == DIR_left || direction == DIR_right) {
					super.setWidth(BOLT_WIDTH); super.setHeight(BOLT_HEIGHT);
				} 
				else{ super.setWidth(BOLT_HEIGHT); super.setHeight(BOLT_WIDTH); }
			break;
		}
	}

	public void moveX(double delta) { super.setMinX(super.getMinX() + delta); }

	public void moveY(double delta) { super.setMinY(super.getMinY() + delta); }

	public BufferedImage getCurrentFrame() { return frame; }

	public void animate(SpriteImageLoader loader) { 
		if(frame == null) { frame = loader.getProjectileFrame(type, direction); }
		else{
			switch(direction){
				case DIR_up: moveY(-(BOLT_SPEED+relativeVelocity));
					break;
				case DIR_down: moveY((BOLT_SPEED+relativeVelocity));
					break;
				case DIR_left: moveX(-(BOLT_SPEED+relativeVelocity));
					break;
				case DIR_right: moveX((BOLT_SPEED+relativeVelocity));
					break;
			}
		}
	}
	
	public void determineAlive(ArrayList<Line> bounds, ArrayList<Sprite> hbx){
		boolean check1 = super.getMinX()  - bounds.get(GameWorld.RIGHT_INDEX).getP1().getX() > 0;
		boolean check2 = super.getMinX() + this.getWidth() < 0; //LEFT CHECK
		boolean check3 = super.getMinY() + this.getHeight() < 0; //TOP CHECK
		boolean check4 = super.getMinY() - bounds.get(GameWorld.BOTTOM_INDEX).getP1().getY() > 0;
		if(check1||check2||check3||check4) { state = DEAD; }
		for(Sprite h : hbx){
			if(h.getCollisionBox().intersectsQuad(this)){ state = DEAD; }
		}
	}

	public void setState(int newState) { state = newState; }

	public void setLayer(int newLayer) { layer = newLayer; }
	
	public void setRelativeVelocity(int newVelocity) { relativeVelocity = newVelocity; }

	public int getState() { return state; }
	
	public int getDirection() { return direction; }

	public int getLayer() { return layer; }
	
	public int getType() { return type; }
	
	public int getRelativeVelocity() { return relativeVelocity; }
	
	public int getDamage() {  return BOLT_DAMAGE; }
		//TODO: Switch on type and return appropriate damage.
	
	public boolean isFriendOrFoe() { return FriendorFoe; }
	
	public Quad getCollisionBox() { return this; }
	
	//UNIMPLEMENTED METHODS
	public int getIdentity() { return 0; }
	public void resetState() { }

}
