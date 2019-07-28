package subterra.engine;

import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;

public class Physics {
	
	/**
	 * @Method spriteCollision
	 * 
	 * @param h: The hero sprite
	 * @param s: The sprite with which the hero collided
	 * 
	 * @description: 
	 * Divides GameWorld into four quadrants, as if the Cartesian plane
	 * had been rotated 45 degrees. Then the method uses the center of 
	 * each sprite to determine which determine the direction the collision
	 * took place before deciding on the appropriate action.
	 * 
	 * This class actually moves the Sprites. This is done statically through 
	 * the use of interfaces. 
	 * 
	 * The hero sprite can be substituted for any Sprite with a move method. 
	 * Used in conjunction with Villain class to determine if Villain collides 
	 * with anything during idle method. See documentation on Villain class and 
	 * View class, VillainUpdate method for more information.
	 */
	public static void spriteCollision(Sprite h, Quad s){
		Quad hcb = h.getCollisionBox();
		Point hc = hcb.getCenter();
		Point sc = s.getCenter();
		
		Line q1 = new Line(sc, new Point(s.getMinX(), s.getMinY()));
		Line q2 = new Line(sc, new Point(s.getMaxX(), s.getMinY()));
		Line q3 = new Line(sc, new Point(s.getMaxX(), s.getMaxY()));
		Line q4 = new Line(sc, new Point(s.getMinX(), s.getMaxY()));
		
		if(hc.getX()<sc.getX()){
			double quad1bound = q1.getSlope()*hc.getX() + q1.getIntercept();
			double quad4bound = q4.getSlope()*hc.getX() + q4.getIntercept();
			if(hc.getY() < quad4bound && hc.getY() > quad1bound){
				h.moveX(-1*(hcb.getMaxX() - s.getMinX()));
			}
			else if(hc.getY()<quad1bound){
				 h.moveY(-1*(hcb.getMaxY() - s.getMinY()));
			}
			else if(hc.getY()>quad4bound){
				h.moveY(s.getMaxY() - hcb.getMinY());
			}
		}
		else{
			double quad2bound = q2.getSlope()*hc.getX() + q2.getIntercept();
			double quad3bound = q3.getSlope()*hc.getX() + q3.getIntercept();
			if(hc.getY() < quad3bound && hc.getY() > quad2bound){
				h.moveX(s.getMaxX() - hcb.getMinX());
			}
			else if(hc.getY()<quad2bound){
				 h.moveY(-1*(hcb.getMaxY() - s.getMinY()));
			}
			else if(hc.getY()>quad3bound){
				h.moveY(s.getMaxY() - hcb.getMinY());
			}
		}
	}
}
