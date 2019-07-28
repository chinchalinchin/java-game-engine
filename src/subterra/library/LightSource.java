package subterra.library;

import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class LightSource extends Quad implements Sprite {

	
	/**
	 * @LightSource:
	 * 
	 * @description: LightSources affect the clipping area of the frame rendered
	 * in the View Class. After all GameWorld objects have been accumulated on a frame
	 * during the rendering process, the View class iterates through the LightSources
	 * on screen and then clips the resultant according the radius of the onscreen 
	 * LightSources. The frame is clipped in two phases, according to the LightRadius 
	 * and InnerLightRadius. 
	 * 
	 */
	public static final int on = 0, off = 1,
							attributes = 6,
							types = 13,
							frames = 5,
							STATIONARY_TYPES = 7;
	public static final int WIDTH_INDEX = 0, HEIGHT_INDEX = 1, OUTERRAD_INDEX = 2,
							INNERRAD_INDEX=3, COLLISION_WIDTH_INDEX=4, COLLISION_HEIGHT_INDEX = 5;
	public static final int lamppost = 0, //types
							beacon = 1,
							nissa_tree = 2,
							candle = 3,
							shaded_lamp = 4,
							hanging_lantern = 5,
							ceiling_light = 6,
							campfire = 7,
							vertical_lava = 8,
							horizontal_lava = 9,
							lava_bubble = 10,
							stove = 11,
							torch = 12;

	private BufferedImage currentFrame;
	private int state, type, switchAnchor, counter, layer;
	private int OuterRadius, InnerRadius;
	private double CollisionWidthMod, CollisionHeightMod;

	public LightSource(Point p, int thisType, int thisState, int anchor) {
		super(p.getX(), p.getY(), 0, 0);
		type = thisType;
		state = thisState;
		switchAnchor = anchor;
		counter = 0;
	}
	
	public void init(SpriteImageLoader loader){ currentFrame = loader.getLightSource(type, counter, isStationary()); }
	
	public void randomizeFrame(){ counter = ThreadLocalRandom.current().nextInt(0, frames); }
	
	public void configure(int thisWidth, int thisHeight, int thisOuterRad, int thisInnerRad, int thisColWidth,
						  int thisColHeight){
		super.setWidth((double)thisWidth); super.setHeight((double)thisHeight);
		OuterRadius = thisOuterRad; InnerRadius = thisInnerRad; 
		CollisionWidthMod = thisColWidth; CollisionHeightMod = thisColHeight;
	}
	
	public BufferedImage getCurrentFrame() {  return currentFrame; }
	
	public void animate(SpriteImageLoader loader) {
		if(!isStationary()){
			currentFrame = loader.getLightSource(type, counter, isStationary());
			counter++;
			if(counter>=frames){ counter = 0; }
		}
	}

	public void setState(int newState) { state = newState;	}

	public void setLayer(int whichLayer) { layer = whichLayer; }

	public int getState() { return state; }
	
	public int getType() { return type; }

	public int getAnchor() { return switchAnchor; }
	
	/**
	 * @description the light radius determines the radius of the outermost circle
	 * of light cast by a given light source.
	 * 
	 * @return returns integer representing the light radius of the LightSource.
	 */
	public int getLightRadius(){ return OuterRadius; }
	
	/**
	 * @description the inner light radius determines the radius of the innermost 
	 * circle of light cast by a given source of Light.
	 * 
	 * @return returns integer represent the light radius of the LightSource.
	 */
	public int getInnerLightRadius(){ return InnerRadius; }
	
	//Light without dimensions acts like a point, can still collide with it.
		//need some way to have no dimensions! 
	public Quad getCollisionBox(){
		double newX, newY, newWidth, newHeight;
		if(CollisionWidthMod == NO_DIM){
			newX = -200; newWidth = 0;
		} 
		else{
			newX = this.getMinX() + CollisionWidthMod/2;
			newWidth = this.getWidth() - CollisionWidthMod;
		}
		if(CollisionHeightMod == NO_DIM){
			newY =-200; newHeight = 0;
		}
		else{
			newY = this.getMinY() + CollisionHeightMod/2;
			newHeight = this.getHeight() - CollisionHeightMod;
		}
		return new Quad(newX, newY, newWidth, newHeight);
	}

	public int getLayer() { return layer; }
	
	private boolean isStationary() {
		if(type == lamppost || type == beacon || type == nissa_tree || type == candle ||
				type == shaded_lamp || type == hanging_lantern || type == ceiling_light) { return true; }
		else { return false; }
	}
	
	public boolean isLava(){
		if(type == vertical_lava || type == horizontal_lava || type == lava_bubble) { return true; }
		else { return false; }
	}
	//Unimplemented methods
	public int getIdentity(){ return 0; }
	public void moveX(double delta) {	}
	public void moveY(double delta) {   }
	public void resetState() {	}
}

