package subterra.library;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;
import subterra.engine.imagehandling.SpriteImageLoader;

public class Door extends Quad	
				  implements Sprite {

	private int layer, connectionlayer;
	private Point connectionInsert;
	
	public Door(Point p, Point d){
		super(p.getX(), p.getY(), d.getX(), d.getY());
	}

	public void moveX(double delta) { super.setMinX(super.getMinX()+delta); }

	public void moveY(double delta) { super.setMinY(super.getMinY()+delta); }
	
	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public void setConnectionLayer(int whichConnect) { connectionlayer = whichConnect; }
	
	public void setConnectionInsert(Point insert) { connectionInsert = insert; }
	
	public int getLayer() { return layer; }
	
	public int getConnectionLayer() { return connectionlayer; }
	
	public Point getConnectionInsert() { return connectionInsert; }
	
	public ArrayList<Line> getBounds(){
		ArrayList<Line> theseLines = new ArrayList<Line>();
		theseLines.add(super.getTopBound());
		theseLines.add(super.getBottomBound());
		theseLines.add(super.getRightBound());
		theseLines.add(super.getLeftBound());
		return theseLines;
	}
	
	public Quad getCollisionBox() { return this; }
	
	//Unimplemented methods.
	public int getIdentity(){ return 0; }
	public void resetState() {	}
	public BufferedImage getCurrentFrame() { return null; }
	public void animate(SpriteImageLoader loader) { }
	public void setState(int newState) { }
	public int getState() { return 0;}
}
