package subterra.library;

import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Effect extends Quad implements Sprite {

	public final static int frames = 6, 
					  		types = 8,
					  		attributes = 3;
	public final static boolean IN_GAME = true, FILE = false,
							    OVER = true, UNDER = false;
	public final static int WIDTH_INDEX = 0, HEIGHT_INDEX = 1,
							SUB_DEP_INDEX = 2;//attributes
	public final static int concussion = 0, clock = 1, knockback = 2,
							water = 3, waterfall =4, waistwater =5, 
							splash = 6, hackcloud = 7;//types
	public final static int reactive = 0, inert = 1; //states
	
	private final static int CONCUSSION_WIDTH = 21, CONCUSSION_HEIGHT = 17,
				       		 KNOCKBACK_WIDTH=10, KNOCKBACK_HEIGHT = 8,
				       		 SPLASH_WIDTH = 33, SPLASH_HEIGHT = 14,
				       		 HACK_WIDTH = 30, HACK_HEIGHT = 30;
	
	private BufferedImage currentFrame;
	private int counter, state, type, submergedepth, layer;
	private int anchorIdentity;
	private boolean OverUnder, Anchored;
	
	public Effect(Point p, int thisType, boolean inGame) {
		super(p.getX(), p.getY(), 0, 0);
		type = thisType;
		counter = 0;
		state = reactive;
		Anchored = false;
		if(inGame){ inGameConfigure(); }
		setOverUnder();
	}
	
	public void init(SpriteImageLoader loader){ currentFrame = loader.getEffectFrame(type, counter); }
	
	private void setOverUnder(){
		if(type == concussion || type == knockback || type == hackcloud) { OverUnder = OVER; }
		else { OverUnder = UNDER; }
	}
	
	public void setAnchored(boolean anchor) { Anchored = anchor; }
	
	public void setAnchorIdentity(int anIdent) { 
		anchorIdentity = anIdent; 
		if(anchorIdentity != NO_ANCHOR) { Anchored = true; } 
	}
	
	private void inGameConfigure(){
		switch(type){
			case concussion:
				super.setWidth(CONCUSSION_WIDTH); super.setHeight(CONCUSSION_HEIGHT);
				break;
			case knockback:
				super.setWidth(KNOCKBACK_WIDTH); super.setHeight(KNOCKBACK_HEIGHT);
				break;
			case splash:
				super.setWidth(SPLASH_WIDTH); super.setHeight(SPLASH_HEIGHT);
				break;
			case hackcloud:
				super.setWidth(HACK_WIDTH); super.setHeight(HACK_HEIGHT);
				break;
		}
	}
	
	public void handlerConfigure(int thisWidth, int thisHeight, int subDep){
		super.setWidth(thisWidth); super.setHeight(thisHeight); 
		submergedepth = subDep;
	}

	public void moveX(double delta) { super.setMinX(super.getMinX() + delta);	}

	public void moveY(double delta) { super.setMinY(super.getMinY() + delta); }

	public BufferedImage getCurrentFrame() { return currentFrame; }
	
	public void randomizeFrame(){ counter = ThreadLocalRandom.current().nextInt(0, frames); }

	public void animate(SpriteImageLoader loader) { 
		int frame;
		frame = counter%frames;
		currentFrame = loader.getEffectFrame(type, frame);
		if(state == reactive) {  counter++; }
		if(isPerpetual()){ if(counter>=frames) { counter = 0; } }
		else{ if(counter>=frames) {  state = inert; } }
		
	}

	public int getType() { return type; }
	
	public int getState() { return state; }
	
	public int getSubmergDepth() { return submergedepth; }
	
	public int getAnchorIdentity() { return anchorIdentity; }
	
	public boolean getOverUnder() { return OverUnder; }
	
	public boolean getAnchored() { return Anchored; }
	
	
	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public int getLayer() { return layer; }

	private boolean isPerpetual() {
		if(type == water || type == waterfall || type == waistwater ||
				type == splash || type == clock){ return true; } 
		else { return false; }
	}
	
	public Quad getCollisionBox() {return this;}
	
	//Unimplemented Methods
	public int getIdentity(){ return 0; }
	public void resetState() { }
	public void setState(int newState) { }
}
