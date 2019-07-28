package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Barrel extends Quad implements Sprite {

	private BufferedImage frame;
	private int layer, identity;
	private int CollisionWidthMod, CollisionHeightMod;
	
	public Barrel(Point p, int id) {
		super(p.getX(), p.getY(), 0, 0);
		identity = id;
	}

	public void configure(int thisWidth, int thisHeight, int thisColWidth, int thisColHeight){
		super.setWidth(thisWidth); super.setHeight(thisHeight);
		CollisionWidthMod = thisColWidth; CollisionHeightMod = thisColHeight;
	}
	
	public void moveX(double delta) { super.setMinX(super.getMinX()+delta);	}

	public void moveY(double delta) { super.setMinY(super.getMinY()+delta); }

	public BufferedImage getCurrentFrame() { return frame; }

	public void animate(SpriteImageLoader loader) { frame = loader.getSingletonFrame(SpriteImageLoader.Barrel_INDEX); }

	public void setLayer(int newLayer) { layer = newLayer; }

	public int getIdentity() { return identity; }
	
	public int getLayer() { return layer; }
	
	public Quad getCollisionBox() {
		double newX = this.getMinX() + CollisionWidthMod/2;
		double newY = this.getMinY() + CollisionHeightMod/2;
		double newWidth = this.getWidth() - CollisionWidthMod;
		double newHeight = this.getHeight() - CollisionHeightMod;
		return new Quad(newX, newY, newWidth, newHeight);
	}
	
	//Unimplemented Methods
	public void resetState() { }
	public void setState(int newState) {	}
	public int getState() { return 0; }

}
