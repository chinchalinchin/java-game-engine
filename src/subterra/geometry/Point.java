package subterra.geometry;

public class Point {

	private double x, y;
	
	public Point( double ix, double iy){
		x = ix; y = iy;
	}
	
	public double getX(){ return x;}
	
	public double getY(){ return y; }
	
	public void setX(double newX){ x = newX; }
	
	public void setY(double newY){ y = newY; }
	
	public double distanceTo(Point p){
		double dx = p.getX()-x;
		double dy = p.getY()-y;
		dx = dx*dx; dy = dy*dy;
		double dist = Math.sqrt(dx+dy);
		return dist;
	}
	
	public boolean isPoint(Point p){
		double checkx = p.getX();
		double checky = p.getY();
		if(checkx == this.x && checky == this.y) { return true; }
		else { return false; }
	}
	
}
