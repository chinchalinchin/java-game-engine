package subterra.library;

import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class NPC extends Quad implements Sprite {
	
		//NPC INFORMATION
	public static final int types= 12,
							states = 9,
							attributes = 3;
		//TYPES
	public static final int belmont = 0, harry = 1, drake = 2, scholar = 3,
						    abbott = 4, matilda = 5, pyat = 6, pree = 7, squalax = 8;
		//STATES
	public static final int wlk_left = 0, wlk_right = 1, wlk_up = 2, wlk_down = 3,
							atk_left = 4, atk_right = 5, atk_up = 6, atk_down = 7,
							die = 8; //states
		//ATTRIBUTE INDICES
	public static final int SPD_INDEX = 0, PER_INDEX = 1, WEIGHT_INDEX = 2; //attributes
		//DEFAULT FILEDS
	private static final int DEFAULT_STATE = 2, WALK_STATES = 4;
	
	private BufferedImage frame;
	private int state, type, stepcount, 
				msgLookUp, identity, layer,
				frames;
	private boolean moving;
	private int Perimeter, Speed, Weight; // attributes
	private int x_offset, y_offset, col_width, col_height; //collision attributes

	public NPC(Point p, int thisType, int msg, int iDent) {
		super(p.getX(), p.getY(),0, 0);
		type = thisType; msgLookUp = msg;
		state = DEFAULT_STATE;
		moving = true; identity = iDent;
		stepcount = 0;
	}
	
	public void configureDimensions(SpriteImageLoader loader){
		super.setWidth(loader.getVillainDimensions().getX());
		super.setHeight(loader.getVillainDimensions().getY());
	}
	
	public void configureCollisions(int thisX, int thisY, int ColWd, int ColHgt){
		x_offset = thisX; y_offset = thisY; col_width = ColWd; col_height = ColHgt;
	}
	
	public void configure(int spd, int per, int wgt){
		Speed = spd; Perimeter = per; Weight = wgt;
	}
	
	public void moveX(double delta) { super.setMinX(super.getMinX()+delta);	}

	public void moveY(double delta) { super.setMinY(super.getMinY()+delta); }

	public void setState(int newState) { state = newState;	}

	public int getState() { return state; }

	public void resetState() {	}
	
	public void randomizeWalkState() { state = ThreadLocalRandom.current().nextInt(0, WALK_STATES-1);}
	
	public void reverseDirection() {
		if(state == wlk_up ) { state = wlk_down; stepcount = 0; }
		else if(state == wlk_down) { state = wlk_up; stepcount = 0; }
		else if(state == wlk_right) { state = wlk_left; stepcount = 0; }
		else if(state == wlk_left) { state = wlk_right; stepcount = 0; }
	}
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}
	
	public int getIdentity()  { return identity; }

	public BufferedImage getCurrentFrame() { return frame; }

	public void animate(SpriteImageLoader loader) {
		if(moving){
			frames = loader.getNPCFrames(type, state);
			//call to loader to get frames based on state and npc
			int check = stepcount%frames;
			frame = loader.getNPCFrame(type, state, check);
			if(state == wlk_right){
				moveX(((double)Speed)/100);
				stepcount++;
				if(stepcount>Perimeter){ stepcount = 0; randomizeWalkState();}
			}
			else if(state == wlk_left){
				moveX(-((double)Speed)/100);
				stepcount++;
				if(stepcount>Perimeter){ stepcount = 0; randomizeWalkState();}
			}
			else if(state == wlk_up){
				moveY(-((double)Speed)/100);
				stepcount++;
				if(stepcount>Perimeter) { stepcount = 0; randomizeWalkState(); }
			}
			else if(state == wlk_down){
				moveY(((double)Speed)/100);
				stepcount++;
				if(stepcount>Perimeter) { stepcount = 0; randomizeWalkState(); }
			}
		}
	}
	
	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public int getLayer() { return layer; }

	public int getMsgLookUp() { return msgLookUp; }
	
	public boolean isMoving() { return moving; }
	
	public void setMoving(boolean move) { moving = move; }
	
	public int getType() { return type; }
	
	public int getWeight() { return Weight; }

}
