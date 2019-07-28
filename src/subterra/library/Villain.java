package subterra.library;

import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Functions;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class Villain extends Quad implements Sprite {

	/**
	 * @description the Villain class is broken into a hierarchy: types > states > frames.
	 * This is for purposes of animation. Due to replication of enemy types in-game and the
	 * possible duplication of image storage in the RAM while game is running, the 
	 * SpriteImageLoader modularizes image loading, while each Villain contains a pointer
	 * to the location of its current frame encoded into its type, state and frame count. 
	 * These properties are passed into the SpriteImageLoader and it internally returns
	 * the appropriate frame.
	 * 
	 * A Villain has attributes. This can be configured through the method and determine 
	 * that Villain type's in-game behavior. Attributes for a specific type are read in 
	 * from file and configured in the FileHandler class.
	 * 
	 */
	
	//TODO: KILL GOLEMS THAT HAVE BEEN LEFT OUT IN THE LIGHT!
	
	//PROPERTIES
	public final static int attributes = 13;
	public final static int types = 13;
	public final static int states = 13;
		//TYPES
	public final static int hollow_male_fresh = 0, hollow_female_fresh = 1,
							hollow_male_spoiled = 2, hollow_female_spoiled =3,
							hollow_rotten = 4, hollow_shambler = 5,
							cretin_male = 7, cretin_female = 8,
							cretin_chieftain = 9, solokai_foot_soldier=10,
							solokai_general = 11, machinarium_grunt = 12;
		//STATES	
	public final static int wlk_left = 0, wlk_right = 1, wlk_up = 2, wlk_down = 3,
							 atk_left = 4, atk_right = 5, atk_up = 6, atk_down = 7,
							 bow_left = 8, bow_right = 9, bow_up = 10, bow_down = 11,
							 die = 12;
		//ATTRIBUTE INDICES
	public final static int ATKRAD_INDEX = 0, AWARERAD_INDEX = 1, 
							HEALTH_INDEX = 2, PERIMETER_INDEX = 3, 
							WALKSPEED_INDEX = 4, RUNSPEED_INDEX = 5,
							ATKBOUNCE_INDEX = 6, STUN_INDEX = 7, SYNCH_INDEX = 8, 
							ATK_INDEX = 9, DEF_INDEX = 10, ATK_TRIGGER_INDEX = 11, 
							ATK_LENGTH_INDEX = 12;
		//DEFAULT FIELDS
	public final static int WALK_STATES = 4;
	
		//Class Fields
	private BufferedImage currentFrame;
	@SuppressWarnings("unused")
	private boolean stunned, attacking, dead, aware, scared;
	private int state, type, stepcount, stunCounter, 
				identity, currentSpd, layer, frames;
	private ArrayList<Quad> atkBoxes;
	private Point heroLocation;
	private Point scarePoint; private double scareDist;
	private int x_offset, y_offset, col_width, col_height;
	private int[] myAttributes = new int[attributes];
	
	public Villain(Point p, int thisType, int iDent) {
		super(p.getX(), p.getY(), 0, 0);
		type = thisType;  identity = iDent;
		stunned = false; attacking = false;
		aware = false; dead = false; scared = false;
		stepcount = 0; state = 0;
	}
	
	public void configureCombat(int[] left, int[] right, int[] up, int[] down){
		atkBoxes = new ArrayList<Quad>();
		Quad l = new Quad(left[Sprite.X_ATK], left[Sprite.Y_ATK], 
						  left[Sprite.ATK_WIDTH], left[Sprite.ATK_HEIGHT]);
		Quad r = new Quad(right[Sprite.X_ATK], right[Sprite.Y_ATK], 
				  			right[ATK_WIDTH], right[Sprite.ATK_HEIGHT]);
		Quad u = new Quad(up[Sprite.X_ATK], up[Sprite.Y_ATK], 
	  						up[ATK_WIDTH], up[Sprite.ATK_HEIGHT]);
		Quad d = new Quad(down[Sprite.X_ATK], down[Sprite.Y_ATK], 
	  						down[ATK_WIDTH], down[Sprite.ATK_HEIGHT]);
		atkBoxes.add(u); atkBoxes.add(d); atkBoxes.add(r); atkBoxes.add(l);
	}
	
	public void configureDimensions(SpriteImageLoader loader){
		super.setWidth(loader.getVillainDimensions().getX());
		super.setHeight(loader.getVillainDimensions().getY());
	}
	
	public void configureCollisions(int thisX, int thisY, int ColWd, int ColHgt){
		x_offset = thisX; y_offset = thisY; col_width = ColWd; col_height = ColHgt;
	}
	
	public void configureAttributes(int thisAtkRadius, int thisAwareRadius, int thisHealth,
									int thisPerimeter,int thisWalkSpeed, int thisRunSpeed, int thisAtkBounce, int thisStun, 
									int thisSynch, int thisAtk, int thisDef,
									int thisAtkTrigger, int thisAtkLength){
		myAttributes[ATKRAD_INDEX]= thisAtkRadius; myAttributes[AWARERAD_INDEX] = thisAwareRadius; 
		myAttributes[HEALTH_INDEX] = thisHealth; myAttributes[PERIMETER_INDEX] = thisPerimeter;
		myAttributes[WALKSPEED_INDEX] = thisWalkSpeed; myAttributes[RUNSPEED_INDEX] = thisRunSpeed;
		myAttributes[ATKBOUNCE_INDEX] = thisAtkBounce; myAttributes[STUN_INDEX] = thisStun;
		myAttributes[SYNCH_INDEX] = thisSynch; myAttributes[ATK_INDEX] = thisAtk;
		myAttributes[DEF_INDEX] = thisDef; myAttributes[ATK_TRIGGER_INDEX] = thisAtkTrigger;
		myAttributes[ATK_LENGTH_INDEX] = thisAtkLength; 
		currentSpd = myAttributes[WALKSPEED_INDEX];
	}

	public void inform(Point hero){
		heroLocation = hero;
		double d = Functions.distanceBetween(hero, this.getCenter());
		double xDir = (this.getCenter().getX() - hero.getX())/d;
		double yDir = (this.getCenter().getY() - hero.getY())/d;
		double absX = Functions.absolute(xDir);
		double absY = Functions.absolute(yDir);
		double theta  = Math.atan(absY/absX);
		switch(type){
			case hollow_male_fresh:
				if(!dead && !scared){
					if( d < myAttributes[AWARERAD_INDEX] && d > myAttributes[ATKRAD_INDEX]){
						aware = true;
						currentSpd = myAttributes[RUNSPEED_INDEX];
						if(xDir > 0 && yDir > 0) {
							if(theta>Math.PI/4){ if(state != wlk_up) { state = wlk_up; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != wlk_left) { state = wlk_left; stepcount = 0; } }
						}
						else if(xDir > 0 && yDir < 0){
							if(theta>Math.PI/4){ if(state != wlk_down) { state = wlk_down; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != wlk_left) { state = wlk_left; stepcount = 0; } }
						}
						else if(xDir < 0 && yDir > 0){
						if(theta>Math.PI/4){ if(state != wlk_up) { state = wlk_up; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != wlk_right) { state = wlk_right; stepcount = 0; } }
						}
						else if(xDir < 0 && yDir < 0){
							if(theta>Math.PI/4){ if(state != wlk_down) { state = wlk_down; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != wlk_right) { state = wlk_right; stepcount = 0; } }
						}
					}
					else if( d < myAttributes[ATKRAD_INDEX]){
						aware = true;
						currentSpd = myAttributes[RUNSPEED_INDEX];
						if(xDir > 0 && yDir > 0) { 
							if(theta>Math.PI/4){ if(state != atk_up) { state = atk_up; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != atk_left) { state = atk_left; stepcount = 0; }}
						}
						else if(xDir > 0 && yDir < 0){
							if(theta>Math.PI/4){ if(state != atk_down) { state = atk_down; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != atk_left) { state = atk_left; stepcount = 0; } }
						}
						else if(xDir < 0 && yDir > 0){
							if(theta>Math.PI/4){ if(state != atk_up) { state = atk_up; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != atk_right) { state = atk_right; stepcount = 0; } }
						}
						else if(xDir < 0 && yDir < 0){
							if(theta>Math.PI/4){ if(state != atk_down) { state = atk_down; stepcount = 0; } }
							else if(theta<Math.PI/4){ if(state != atk_right) { state = atk_right; stepcount = 0; } }
						}	
					}
					else if(d > myAttributes[AWARERAD_INDEX]){
						currentSpd = myAttributes[WALKSPEED_INDEX];
						aware = false;
					}
				}
				else if(scared && !dead){
				}
				else if(dead && !scared){
				}
				break;
			
			case cretin_male:
				break;
		}
		
	}
	
	public int getDirection(){
		if(state == wlk_left || state == atk_left) { return DIR_left; }
		else if (state == wlk_right || state == atk_right) { return DIR_right; }
		else if (state == wlk_up || state == atk_up) { return DIR_up; }
		else if (state == wlk_down || state == atk_down) { return DIR_down; }
		else { return DIR_up;}
	}
	
	public int getOppositeDirection() {
		if(state == wlk_left || state == atk_left) { return DIR_right; }
		else if (state == wlk_right || state == atk_right) { return DIR_left; }
		else if (state == wlk_up || state == atk_up) { return DIR_down; }
		else if (state == wlk_down || state == atk_down) { return DIR_up; }
		else { return DIR_up;}

	}
	
	public void reverseWalkDirection(){
		if(state == wlk_up){ state = wlk_down; stepcount = 0; }
		else if(state == wlk_down) { state = wlk_up; stepcount = 0; }
		else if (state == wlk_right) { state = wlk_left; stepcount = 0; }
		else if (state == wlk_left) { state = wlk_right; stepcount = 0; } 
	}
	
	public void recoil(int direction){
		switch(direction){
			case DIR_right:
				moveX(myAttributes[ATKBOUNCE_INDEX]);
				break;
			case DIR_left:
				moveX(-myAttributes[ATKBOUNCE_INDEX]);
				break;
			case DIR_up:
				moveY(-myAttributes[ATKBOUNCE_INDEX]);
				break;
			case DIR_down:
				moveY(myAttributes[ATKBOUNCE_INDEX]);
				break;
		}
	}
	
	/**
	 * @Method frighten
	 * 
	 * @param p: The point of fear! The point the Villain moves away from!
	 * @param armsLength: How far away to keep the villain!
	 * 
	 * @description: Called in View Class. View will decided based on Villain
	 * type where the Point originates from. Golems, for instance, are frightened
	 * of light. 
	 * 
	 */
	public void frighten(Point p, double armsLength){
		double dist = Functions.distanceBetween(p, this.getMin());
		if(dist <= armsLength && !scared) {  
			currentSpd = myAttributes[RUNSPEED_INDEX];
			scared = true; scarePoint = p; scareDist = armsLength;
			double xDir = (this.getMin().getX() - p.getX())/dist;
			double yDir = (this.getMin().getY() - p.getY())/dist;
			double absX = Functions.absolute(xDir); double absY = Functions.absolute(yDir);
			double theta  = Math.atan(absY/absX);
			if(xDir > 0 && yDir > 0){
				if(theta>Math.PI/4){ if(state != wlk_down) { state = wlk_down; stepcount = 0; }}
				else if (theta<Math.PI/4){ if(state != wlk_right) { state = wlk_right; stepcount = 0; } }
			} 
			else if( xDir > 0 && yDir<0){
				if(theta>Math.PI/4){ if(state != wlk_up) { state = wlk_up; stepcount = 0; } }
				else if (theta<Math.PI/4){ if( state != wlk_right) { state = wlk_right; stepcount = 0 ; } }
			} 
			else if( xDir < 0 && yDir >0){
				if(theta>Math.PI/4){ if (state != wlk_down) { state = wlk_down; stepcount = 0; }}
				else if (theta<Math.PI/4){ if(state != wlk_left) { state = wlk_left; stepcount = 0; } }
			} 
			else if( xDir < 0 && yDir<0){
				if(theta>Math.PI/4){ if(state != wlk_up) { state = wlk_up; stepcount = 0; } }
				else if (theta<Math.PI/4){ if(state != wlk_left) { state = wlk_left; stepcount = 0; } }
			}
		}
	}
	
	/**
	 * @Method animate
	 * 
	 * @description  this method contains the path idle procedure for on-screen 
	 * villains. The Villain moves in a predetermined way until it is informed
	 * of the hero's whereabouts and passes a distance check between the Villain
	 * and Hero center. At that point, the attack state is triggered and the attack
	 * method is called. 
	 */ 
	public void animate(SpriteImageLoader loader){
		frames = loader.getVillainFrames(type, state);
		int frame = stepcount % frames;
		currentFrame = loader.getVillainFrame(type,state, frame);
		//currentFrame = loader.getVillainFrame(type, state, frame, SynchDelay);
		if(!stunned && !dead){
			if(state == wlk_left){
				moveX(-((double)(currentSpd))/100); 
				stepcount++;
				if(stepcount>myAttributes[PERIMETER_INDEX] && !scared){ randomizeWalkState(); stepcount = 0; }
			}
			else if (state == wlk_right){
				moveX(((double)(currentSpd))/100); 
				stepcount++;
				if(stepcount>myAttributes[PERIMETER_INDEX]&& !scared){ randomizeWalkState(); stepcount = 0; }
			}
			else if (state == wlk_up){
				moveY(-((double)(currentSpd))/100); 
				stepcount++;
				if(stepcount>myAttributes[PERIMETER_INDEX]&& !scared){ randomizeWalkState(); stepcount = 0; }
			}
			else if (state == wlk_down){
				moveY(((double)(currentSpd))/100);
				stepcount++;
				if(stepcount>myAttributes[PERIMETER_INDEX]&& !scared){ randomizeWalkState(); stepcount = 0; }
			}
			else if( state == atk_right || state == atk_left 
					  || state == atk_up || state == atk_down){ stepcount++; }
			else if ( state == die){ 
				if(stepcount < frames) { stepcount++;}
				if(stepcount == (frames-1)) { dead = true; } 
			}
		}
		else if (stunned && !dead){
			stunCounter++; 
			if(stunCounter > myAttributes[STUN_INDEX]){ stunned = false; stunCounter = 0;}
		}
		else if(dead){
			
		}
		//if(Functions.pointEqualspoint(start, this.getMin())){ pathCorrect(); }
		if(scared) { stillScared(); }

	}
	
	public void pathCorrect(){
		if(aware){
			double d = Functions.distanceBetween(heroLocation, this.getCenter());
			double xDir = (this.getCenter().getX() - heroLocation.getX())/d;
			double yDir = (this.getCenter().getY() - heroLocation.getY())/d;
			double absX = Functions.absolute(xDir);
			double absY = Functions.absolute(yDir);
			double theta  = Math.atan(absY/absX);
			if(xDir > 0 && yDir > 0) {
				if(theta>Math.PI/4){ if(state != wlk_left) { state = wlk_left; stepcount = 0; } }
				else if(theta<Math.PI/4){ if(state != wlk_up) { state = wlk_up; stepcount = 0; } }
			}
			else if(xDir > 0 && yDir < 0){
				if(theta>Math.PI/4){ if(state != wlk_left) { state = wlk_left; stepcount = 0; } }
				else if(theta<Math.PI/4){ if(state != wlk_down) { state = wlk_down; stepcount = 0; } }
			}
			else if(xDir < 0 && yDir > 0){
			if(theta>Math.PI/4){ if(state != wlk_right) { state = wlk_right; stepcount = 0; } }
				else if(theta<Math.PI/4){ if(state != wlk_up) { state = wlk_up; stepcount = 0; } }
			}
			else if(xDir < 0 && yDir < 0){
				if(theta>Math.PI/4){ if(state != wlk_right) { state = wlk_right; stepcount = 0; } }
				else if(theta<Math.PI/4){ if(state != wlk_down) { state = wlk_down; stepcount = 0; } }
			}
		}
	}
	
	public void moveX(double delta) { super.setMinX(super.getMinX()+delta); }

	public void moveY(double delta) { super.setMinY(super.getMinY()+delta); }

	public Quad getAttackBox(){
		Quad mod = atkBoxes.get(getDirection());
		Quad atk = new Quad(mod.getMinX()+this.getMinX(), mod.getMinY() +this.getMinY(),
							mod.getWidth(), mod.getHeight());
		return atk;
	}
	
	public int getIdentity() { return identity; }
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   col_width,
						   col_height);
	}

	public BufferedImage getCurrentFrame() { return currentFrame; }
	
	public void stun() { stunCounter = 0; stunned = true; }
	
	public boolean isStunned(){  return stunned; }
	
	public boolean isAware() { return aware; }
	
	public boolean isScared() { return scared; }
	
	public boolean isDead() { return dead; }
	
	public boolean isDying() {
		if(!dead && state == die) { return true; }
		else { return false; } 
	}
	
	public boolean isAttacking(){
		if(state == atk_up || state == atk_down|| state == atk_left || state == atk_right){
			int start = myAttributes[ATK_TRIGGER_INDEX]; int end = myAttributes[ATK_TRIGGER_INDEX] + myAttributes[ATK_LENGTH_INDEX];
			int current = stepcount % frames;
			if(current>=start && current <end){ return true; }
			else { return false; }
		}
		else { return false; }
	}
	
	public int getState() { return state; }
	
	public int getType() { return type; }
	
	public int getLayer() { return layer; }
	
	public int getStunCounter() { return stunCounter; }
	
	public int getSynchDelay() { return myAttributes[SYNCH_INDEX]; }
	
	public int getStun() { return myAttributes[STUN_INDEX]; }
	
	public int getAtk() { return myAttributes[ATK_INDEX]; }
	
	public int getDef() { return myAttributes[DEF_INDEX]; }
	
	public void kill() { stepcount = 0; state = die; }
	
	public int getHealth() { return myAttributes[HEALTH_INDEX]; }		
	
	public void damage(int amount){ 
		if(myAttributes[HEALTH_INDEX] != 0 ) { myAttributes[HEALTH_INDEX] = myAttributes[HEALTH_INDEX] - amount; } 
		if(myAttributes[HEALTH_INDEX] <= 0) { myAttributes[HEALTH_INDEX] = 0; kill(); }
	}
	
	public void resetState() { }
	
	public void setState(int newState) { state = newState; }

	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public void setScared(boolean whichScared) { scared = whichScared; }
	
	private void randomizeWalkState() { state = ThreadLocalRandom.current().nextInt(0, WALK_STATES - 1);}
	
	private void stillScared() {
		double dist = Functions.distanceBetween(scarePoint, this.getMin());
		if(dist>scareDist) { scared = false; currentSpd =  myAttributes[WALKSPEED_INDEX];}
	}
	
	public boolean isScareable(){
		if(type == hollow_male_fresh || type == hollow_female_fresh) { return true;}
		else { return false; }
	}
}
