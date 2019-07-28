package subterra.library;

import java.awt.image.BufferedImage;

import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Portal extends Quad implements Sprite{
	
	private String connection;
	private Point connectorInsertion;
	private int layer, nextlayer;
	
	public Portal(Point p, Point d, Point insertion, String c, int nextL) {
		super(p.getX(), p.getY(), d.getX(), d.getY());
		connection = c;
		connectorInsertion = insertion;
		nextlayer = nextL;
	}
	
	public void moveX(double delta) { super.setMinX(super.getMinX()+delta); }

	public void moveY(double delta) { super.setMinY(super.getMinY()+delta); }
	
	public String getConnection() { return connection; }
	
	public Point getNextInsertion() { return connectorInsertion; }

	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public int getLayer() { return layer; }

	public int getNextLayer() { return nextlayer; }
	
	public Quad getCollisionBox() { return this; }
	
	//Unimplemented methods.
	public int getIdentity(){ return 0; }
	public void resetState() {	}
	public BufferedImage getCurrentFrame() { return null; }
	public void animate(SpriteImageLoader loader) { }
	public void setState(int newState) { }
	public int getState() { return 0; }
}
