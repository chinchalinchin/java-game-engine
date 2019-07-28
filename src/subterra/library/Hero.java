package subterra.library;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;


public class Hero extends Quad
				  implements Sprite {
	
	/**@Hero 
	 * 
	 * @description the Hero class contains information about the player
	 * and about the current state of the player in-game. Frames for a 
	 * given action are stored as ArrayList<BufferedImage>.
	 * 
	 * The state of the Controller is passed into state field. This 
	 * determines the current action of the Hero sprite. Each action
	 * or state has a statecount field that acts as a time flag for 
	 * that specific action. This allows the correct frame from the
	 * given ArrayList to be returned when the Hero is painted in the 
	 * View class.
	 * 
	 * For example, if the player is walking right, then the right arrow
	 * key is being pressed. This information gets passed into the state
	 * field via the View class. When the animate method is called, the Hero 
	 * class returns the current frame of walking right and then increments the 
	 * statecount field so when animate is called again the animate 
	 * method will return the next frame in the walking right animation.
	 * 
	 * Attacking and shovelling are transitory states and are controlled by
	 * separate boolean triggers. If the hero is attacking, the animate method
	 * will return the current frame of animation until the animation is 
	 * concluded. At that point, the attacking boolean trigger will be reset.
	 * Likewise, the shovelling boolean trigger is flipped when the shovelling
	 * animation is done.
	 * 
	 * Due to images being rendered from the top left, problems were encountered
	 * when rendering attacking left and shovelling left animations. This has been
	 * circumvented by only using right-facing images and then transforming parent
	 * frame in View class and drawing the right-facing images on transformed frame.
	 * 
	 * The documentation in the method paintMirroredHero in the Utilities class
	 * contains more details about how leftward facing animation is achieved by
	 * painting rightward facing animations on a mirrored image of a frame.
	 * 
	 * Also of special importance: the Hero class does not implement OneOrTwo or 
	 * OutOrIn triggers. The Hero is always rendered in-game, thus the GameWorld
	 * class holds information about the world frame to be rendered. See 
	 * GameWorld documentation for more information.
	 * 
	 */
	
		//HERO INFORMATION
	public static final int states = 25,
							types = 4,
							immutable_attributes = 6,
							variable_attributes = 7;
		//TYPES
	public final static int unarmored = 0, bronze_armor = 1, gold_armor = 2, plate_armor = 3;
		//STATES
	public final static int wlk_left = 0, wlk_right = 1, wlk_up = 2, wlk_down = 3, // Animation-Primary States
							atk_left = 4, atk_right = 5, atk_up = 6, atk_down = 7,
							shv_left = 8, shv_right = 9, shv_up = 10, shv_down = 11,
							hmr_left = 12, hmr_right= 13, hmr_up = 14, hmr_down = 15,
							shd_left = 16, shd_right = 17, shd_up = 18, shd_down = 19,
							bow_left = 20, bow_right = 21, bow_up = 22, bow_down = 23,
							die = 24,
							wlk_up_left = 25, wlk_up_right = 26, wlk_down_left = 27, // Secondary States
							wlk_down_right = 28;
		//ATTRIBUTE INDICES
	public static final int I_WLKSPD_INDEX = 0, I_RUNSPD_INDEX = 1, //Immutable Indices
							I_LANT_INDEX = 2, I_STUN_INDEX= 3,
							I_ATK_BOUNCE_INDEX = 4, I_ATK_TRIGGER_INDEX = 5;
	public static final int	V_TYPE_INDEX = 0, V_TOTHP_INDEX = 1, V_CURHP_INDEX = 2, //Variable Indices
							V_ATK_INDEX = 3, V_DEF_INDEX = 4, 
							V_LINNERRAD_INDEX = 5, V_LOUTERRAD_INDEX = 6;
		//DEFAULT FIELDS
	private static final int DEFAULT_STATE = 3, DEFAULT_TYPE = 0;
	
	private BufferedImage currentFrame;
	private int type, state, stepcount, direction,
				stunCount, lightCount, frames;
	private ArrayList<Quad> atkBoxes;
	private boolean attacking, shovelling, powered, bowing, shielding, //equipment input modulation flags
					stunned, submerged, // rendering flags
					lanterning; //static image flag
	private int AtkTrigger, AtkBounce, StunCounter,
				WalkSpeed, RunSpeed, 
				LanternCounter; //Immutable
	private int TotalHealth, CurrentHealth, Atk, Def, 
				LanternInnerRad, LanternOuterRad; //Variable
	private int x_offset, y_offset, collision_width, collision_height;
	
	public Hero(Point p){ 
		super(p.getX(), p.getY(), 0, 0);
		attacking = false; shovelling = false; powered = false; 
		lanterning = false; shielding = false; bowing = false;
		stunned = false; submerged = false;
		state = DEFAULT_STATE; type = DEFAULT_TYPE;
		stepcount = 0;
	}
	
	public void configureDimensions(SpriteImageLoader loader) {
		super.setWidth(loader.getHeroDimensions().getX());
		super.setHeight(loader.getHeroDimensions().getY());
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
	
	public void configureCollisions(int thisX, int thisY, int thisColW, int thisColH){
		x_offset = thisX; y_offset = thisY; 
		collision_width = thisColW; collision_height = thisColH;
	}
	
	public void configureVariable(int thisTotalHP, int thisCurrentHP, int thisAtk, int thisDef, 
								  int thisLanternInnerRad, int thisLanternOuterRad){
		TotalHealth = thisTotalHP; CurrentHealth = thisCurrentHP;
		Atk = thisAtk; Def = thisDef; 
		LanternOuterRad = thisLanternOuterRad; LanternInnerRad = thisLanternInnerRad;
	}
	
	public void configureImmutable(int thisWalkSpd, int thisRunSpd, int thisLanternCnt,
									 int thisStunCnt, int thisAtkBounce, int thisAtkTrigger){	
		WalkSpeed = thisWalkSpd; RunSpeed = thisRunSpd; 
		LanternCounter = thisLanternCnt; StunCounter = thisStunCnt; 
		AtkBounce = thisAtkBounce; AtkTrigger = thisAtkTrigger;
	}
	
	public Quad getAttackBox(){
		determineDirection();
		Quad mod = atkBoxes.get(direction);
		Quad atk = new Quad(mod.getMinX()+this.getMinX(), mod.getMinY() +this.getMinY(),
							mod.getWidth(), mod.getHeight());
		return atk;
	}
	
	public Quad getFrameBox(){ return this; }
	
	public Quad getCollisionBox() { 
		return new Quad( this.getMinX() + x_offset, 
						   this.getMinY() + y_offset,	
						   collision_width,
						   collision_height);
	}
	
	private void determineDirection(){  
		switch(state){
			case wlk_down: direction = DIR_down;
				break;
			case wlk_up: direction = DIR_up;
				break;
			case wlk_right: direction = DIR_right;
				break;
			case wlk_left: direction = DIR_left;
				break;
			case wlk_up_left: direction = DIR_left;
				break;
			case wlk_up_right: direction = DIR_right;
				break;
			case wlk_down_left: direction = DIR_left;
				break;
			case wlk_down_right: direction = DIR_right;
				break;
			case atk_up: direction = DIR_up;
				break;
			case atk_down: direction = DIR_down;
				break;
			case atk_right: direction = DIR_right;
				break;
			case atk_left: direction = DIR_left;
				break;
			case shv_up: direction = DIR_up;
				break;
			case shv_down: direction = DIR_down;
				break;
			case shv_right: direction = DIR_right;
				break;
			case shv_left: direction = DIR_left;
				break;
			}
	}
	
	public void recoil(int direction){
		switch(direction){
			case Hero.DIR_right: moveX(AtkBounce);
				break;
			case Hero.DIR_left: moveX(-AtkBounce);
				break;
			case Hero.DIR_up: moveY(-AtkBounce);
				break;
			case Hero.DIR_down: moveY(AtkBounce);
				break;
		}
	}
	
	public void moveX(double delta) { super.setMinX(super.getMinX()+delta); }

	public void moveY(double delta) { super.setMinY(super.getMinY()+delta); }
	
	public void animate(SpriteImageLoader loader) { 
		if(!stunned){
			int primaryState = state;
			if(state == wlk_up_left) { primaryState = wlk_left; }
			if(state == wlk_up_right) { primaryState = wlk_right; }
			if(state == wlk_down_left){ primaryState = wlk_left;}
			if(state == wlk_down_right) { primaryState = wlk_right; }
			frames = loader.getHeroFrames(type, primaryState);
			int frame = stepcount % frames;
			currentFrame = loader.getHeroFrame(type, primaryState, frame);
		
			if(!attacking && !shovelling && !powered && !bowing){
				if(state==wlk_up || state == shd_up){ moveY(-((double)(WalkSpeed))/100); stepcount++; }
				else if(state == wlk_down || state == shd_down) { moveY(((double)(WalkSpeed))/100); stepcount++;}
				else if(state ==wlk_left|| state == shd_left){ moveX(-((double)(WalkSpeed))/100); stepcount++; }
				else if(state == wlk_right || state == shd_right){ moveX(((double)(WalkSpeed))/100); stepcount++; }
				else if(state == wlk_up_right){ 
					moveX(((double)(WalkSpeed))/(100 * Math.sqrt(2))); moveY(-((double)(WalkSpeed))/(100 * Math.sqrt(2)));
					stepcount++;
				}
				else if(state == wlk_up_left){
					moveX(-((double)(WalkSpeed))/(100 * Math.sqrt(2))); moveY(-((double)(WalkSpeed))/(100 * Math.sqrt(2)));
					stepcount++;
				}
				else if(state == wlk_down_right){
					moveX(((double)(WalkSpeed))/(100 * Math.sqrt(2))); moveY(((double)(WalkSpeed))/(100 * Math.sqrt(2)));
					stepcount++;
				}
				else if(state == wlk_down_left){
					moveX(-((double)(WalkSpeed))/(100 * Math.sqrt(2))); moveY(((double)(WalkSpeed))/(100 * Math.sqrt(2)));
					stepcount++;
				}
			}
			else if(attacking && !shovelling && !powered && !bowing){
				if(state == atk_up || state == atk_down || state == atk_left || state == atk_right) {  stepcount++; }
				if(stepcount >= frames){ 
					if(state == atk_up) { state = wlk_up; }
					if(state == atk_down) { state = wlk_down; }
					if(state == atk_right) { state = wlk_right; }
					if(state == atk_left) { state = wlk_left; }
					stepcount = 0; 
					attacking = false;
				}
			}
			else if(!attacking && shovelling && !powered && !bowing){
				if(state == shv_up || state == shv_down || state == shv_left || state == shv_right) { stepcount++; }
				if(stepcount >= frames){ 
					if(state == shv_up) { state = wlk_up; }
					if(state == shv_down) { state = wlk_down; }
					if(state == shv_right) { state = wlk_right; }
					if(state == shv_left) { state = wlk_left; }
					stepcount = 0; 
					shovelling = false;
				}
			}
			else if(!attacking && !shovelling && !powered && bowing){
				if(state == bow_up || state == bow_down || state == bow_left || state == bow_right) { stepcount++; }
				if(stepcount >= frames){ 
					if(state == bow_up) { state = wlk_up; }
					if(state == bow_down) { state = wlk_down; }
					if(state == bow_right) { state = wlk_right; }
					if(state == bow_left) { state = wlk_left; }
					stepcount = 0; 
					bowing = false;
				}
			}
		}
		else{
			stunCount++;
			if(stunCount>StunCounter) { stunned = false; stunCount = 0; }
		}
	}
	
	public void setState(int setter) {  
		state = setter; 
		determineDirection();
	}
	
	public void setType(int setter) { type = setter; }
	
	public void resetState() { stepcount = 0; }
	
	public void setSubmerged(boolean whichSubmerge) { submerged = whichSubmerge; }
	
	public void setPowered(boolean whichPower) { powered = whichPower; }
	
	public void setAttacking(boolean whichAttack) { attacking = whichAttack; }
	
	public void setShovelling(boolean whichShovel) { shovelling = whichShovel; }
	
	public void setLanterning(boolean whichLantern) { 
		if(!lanterning && whichLantern) { lightCount = 0 ;}
		lanterning = whichLantern; 
	}
	
	public void setShielding(boolean whichShield) { 
		if(!whichShield && shielding){
			switch(direction){
				case DIR_up: state = wlk_up;
					break;
				case DIR_down: state = wlk_down;
					break;
				case DIR_right: state = wlk_right;
					break; 
				case DIR_left: state = wlk_left;
					break;
			}
		}
		shielding = whichShield; 
	}
	
	public void setBowing(boolean whichBow) { bowing = whichBow; }
	
	public void setPoint(Point p) {	super.setMin(p); }
	
	public void incrementLantern() { lightCount ++; }
	
	public void damage(int amt){
		CurrentHealth = CurrentHealth - amt;
		if(CurrentHealth< 0) { CurrentHealth = 0; }
	}
	
	public void stun() { stunned = true; } 
	
	public void flushState() { state = DEFAULT_STATE; stepcount = 0; }
	
	public boolean isAttacking(){
		if(stepcount > AtkTrigger && attacking) { return true; }
		else { return false; }
	}
	public int getType() { return type; }
	
	public int getState() { return state; }
	
	public int getDirection() { determineDirection(); return direction; }
	
	public boolean getPowered() { return powered; }
	
	public boolean getAttacking() { return attacking; }
	
	public boolean getShovelling() { return shovelling; }
	
	public boolean getSubmerged() { return submerged; }
	
	public boolean getLanterning() { 
		if(lightCount >= LanternCounter && lanterning){ lanterning = false; }
		return lanterning; 
	}
	
	public boolean getShielding() { return shielding; }
	
	public boolean getBowing() { return bowing; }
	
	public boolean getStunned() { return stunned; }
	
	public int getStunCount() { return stunCount; }
	
	public int getStunCounter() { return StunCounter; }
	
	public int getCurrentHP() { return CurrentHealth; }
	
	public int getTotalHP() { return TotalHealth; }
	
	public int getATK() { return Atk; }
	
	public int getDEF() { return Def; } 
	
	public int getWlkSPD() { return WalkSpeed; }
	
	public int getRunSPD() { return RunSpeed; }
	
	public int getInnerLanternRad() { return LanternInnerRad; }
	
	public int getOuterLanternRad() { return LanternOuterRad; }
	
	public BufferedImage getCurrentFrame() { return currentFrame; }
	
	public int getIdentity() { return HERO_IDENTITY; }
	
	//Unimplemented methods
	public void setLayer(int whichLayer) { }
	public int getLayer() { return 0; }
	
}
