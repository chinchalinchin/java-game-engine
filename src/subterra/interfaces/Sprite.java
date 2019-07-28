package subterra.interfaces;

import java.awt.image.BufferedImage; 

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;


public interface Sprite {
	
	/**@Sprite
	 * 
	 * @description interface for general methods associated with in-game objects.
	 *
	 */
	
	//FILE DIRECTORY INFORMATION
	public static final String cdir = System.getProperty("user.dir"); 
	public static final String sep = System.getProperty("file.separator");
	public static final String imgpath = cdir + sep + "imgs" + sep;
	//DIRECTION INDICES
	public static final int DIR_up = 0, DIR_down = 1, DIR_right = 2, DIR_left = 3,
							DIRECTIONS = 4;
	//SPECIAL PROPERTIES
	public static final int NO_DIM = -1, NO_ANCHOR = -1, NO_MSG = -1;
	public static final int HERO_IDENTITY = 0, NO_IDENTITY = -1;
	public static final int SHEET_SPRITE_DIM = 64;
	//COLLISION BOX ATTRIBUTE INDICES
	public static final int OFFSET_X = 0, OFFSET_Y = 1, COLLISION_WIDTH = 2, COLLISION_HEIGHT = 3,
						    COLLISION_ATTRIBUTES = 4;
	//ATTACK BOX ATTRIBUTE INDICES
	public static final int X_ATK = 0, Y_ATK = 1, ATK_WIDTH = 2, ATK_HEIGHT = 3,
						    ATTACK_ATTRIBUTES = 4;
	//SPRITE GROUPS
	public static final int SINGLETON = 0, BINARY = 1, SHEET = 2, LIGHTSOURCE = 3, EFFECT =4;
	public static final int SPRITE_GROUPS = 5;
	
	//General Methods
	public abstract void moveX(double delta);
	public abstract void moveY(double delta);
	public abstract BufferedImage getCurrentFrame();
	//public abstract SOUNDTYPE getCurrentSound()
	public abstract void animate(SpriteImageLoader loader);
	public abstract void resetState();
	//TODO:
	//public abstract void configureCollisionBox(Point p, Point d)
		//Point p always relative to Sprite Min!!
	
	//Setter Methods
	public abstract void setMin(Point p);
	public abstract void setMinX(double x);
	public abstract void setMinY(double y);
	public abstract void setQuad(Quad q);
	public abstract void setWidth(double w);
	public abstract void setHeight(double h);
	public abstract void setState(int newState);
	public abstract void setLayer(int newLayer);
	
	//Getter Methods
	public abstract int getIdentity();
	public abstract Point getMin();
	public abstract Point getMax();
	public abstract Point getCenter();
	public abstract double getMinX();
	public abstract double getMinY();
	public abstract double getMaxX();
	public abstract double getMaxY();
	public abstract double getWidth();
	public abstract double getHeight();
	public abstract int getState();
	public abstract Quad getCollisionBox();
	public abstract int getLayer();
	
}
