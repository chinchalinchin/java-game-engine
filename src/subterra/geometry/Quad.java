package subterra.geometry;

public class Quad {

	public double x, y, w, h;
	private Line right, left, top, bottom;
	
	public Quad(double ix, double iy, double iw, double ih){
		x = ix; y = iy; w = iw; h = ih;
		makeBounds();
	}
	
	public void makeBounds(){
		left = new Line(x,y,x,y+h); right = new Line(x+w,y,x+w,y+h);
		top = new Line(x, y, x+w, y); bottom = new Line(x, y+h, x+w, y+h);
	}
	
	public double getMaxX(){ return (x+w);}
	
	public double getMinX(){ return x; }
	
	public double getMaxY(){ return (y+h); }
	
	public double getMinY(){ return y; }
	
	public Point getMin(){ return new Point(getMinX(), getMinY()); }
	
	public Point getMax(){ return new Point(getMaxX(), getMaxY()); }
	
	public double getHeight(){ return h; }
	
	public double getWidth(){ return w; }
	
	public Point getCenter(){ return (new Point(x+w/2, y+h/2)); }
	
	public Line getRightBound(){ return right; }
	
	public Line getLeftBound() { return left; }
	
	public Line getTopBound() { return top; }
	
	public Line getBottomBound(){ return bottom; }

	public void setCenter(double x, double y){
		double w = getWidth();
		double h = getHeight();
		Quad q = new Quad(x-w/2, y - h/2, w, h);
		this.setQuad(q);
	}
	
	public void setQuad(Quad newQ){
		double newX = newQ.getMinX();
		double newY = newQ.getMinY();
		double newW = newQ.getWidth();
		double newH = newQ.getHeight();
		x = newX; y = newY;
		w = newW; h = newH;
		makeBounds();
	}
	
	public void setDimensions(double nW, double nH){ setWidth(nW); setHeight(nH); }
	
	public void setWidth(double nW){ w = nW; makeBounds(); }
	
	public void setHeight(double nH){ h = nH; makeBounds();}
	
	public void setMinX(double newX) { x = newX; makeBounds(); }
	
	public void setMinY(double newY) { y = newY; makeBounds(); }
	
	public void setMin(Point p) { x = p.getX(); y = p.getY(); makeBounds(); }
	
	public boolean containsPoint(Point p){
		if(right.containsPoint(p)) { return true; }
		else if(left.containsPoint(p)) { return true; }
		else if(top.containsPoint(p)) { return true; }
		else if(bottom.containsPoint(p)){ return true; }
		else {
			boolean xcheck, ycheck;
			double px = p.getX();
			double py = p.getY();
			xcheck = ((px > x) && ( px < x + w));
			ycheck = ((py > y) && (py< y + h));
			if(xcheck && ycheck) { return true; }
			else { return false; }
		}
	}
	
	public boolean intersectsLine(Line l){
		if(containsPoint(l.getP1()) 
		   || containsPoint(l.getP2())) { return true; }
		else if(l.intersectsLine(top)) { return true; }
		else if(l.intersectsLine(bottom)) { return true; }
		else if(l.intersectsLine(right)) { return true; }
		else if(l.intersectsLine(left)) { return true;}
		else return false;
	}
	
	public boolean intersectsQuad(Quad q){
		Line thatRight = q.getRightBound();
		Line thatLeft = q.getLeftBound();
		Line thatTop = q.getTopBound();
		Line thatBottom = q.getBottomBound();
		
		//Boundary Checks
			//Right Bound Check
		if (thatTop.intersectsLine(right) || thatBottom.intersectsLine(right)
				|| thatRight.intersectsLine(right) || thatLeft.intersectsLine(right)){
			return true;
		}
			//Left Bound Check
		else if (thatTop.intersectsLine(left) || thatBottom.intersectsLine(left)
				|| thatRight.intersectsLine(left) || thatLeft.intersectsLine(left)){
			return true;
		}
			//Top Bound Check
		else if(thatRight.intersectsLine(top) || thatLeft.intersectsLine(top)
				|| thatTop.intersectsLine(top) || thatBottom.intersectsLine(top)){
			return true;
		}
			//Bottom Bound Check
		else if(thatRight.intersectsLine(bottom) || thatLeft.intersectsLine(bottom)
				|| thatTop.intersectsLine(bottom) || thatBottom.intersectsLine(bottom)){
			return true;
		} 
		else {
			//Containment Check, i.e. if the argument q is wholly contained 
			//in the object quad or visa versa.
			boolean ccheck1 = containsPoint(q.getCenter());
			boolean ccheck2 = q.containsPoint(getCenter());
			boolean containcheck = ccheck1 || ccheck2;
			return containcheck; 
		}
	}
	
}
