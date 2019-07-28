package subterra.geometry;

public class Functions {

	public static double absolute(double num){
		if(num>0) return num;
		else return (-num);
	}
	
	public static double distanceBetween(Point p1, Point p2){
		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY();
		dx = dx*dx;
		dy = dy*dy;
		double dist = Math.sqrt(dx+dy);
		return dist;
	}
	
	public static boolean pointEqualspoint(Point p1, Point p2){
		if((p1.getX() == p2.getX()) && (p1.getY() == p2.getY())){ return true; }
		else { return false; }
	}
}
