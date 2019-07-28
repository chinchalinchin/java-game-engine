package subterra.geometry;

public class Line {

	public static final String top = "TOP";
	public static final String bottom = "BOTTOM";
	public static final String coll = "COLLINEAR";
	public static final String unk = "UNKNOWN";
	private Point p1; //smallest x
	private Point p2; //largest x
	
	public Line(double x1, double y1, double x2, double y2){
		if(x1 < x2){ p1 = new Point (x1, y1); p2 = new Point (x2, y2); } 
		else{ p1 = new Point(x2, y2); p2 = new Point(x1, y1); }
	}
	
	public Line(Point o1, Point o2){
		if(o2.getX() > o1.getX()){ p1 = o1; p2 = o2; } 
		else { p1 = o2; p2 = o1; }
	}
	
	public Point getP1(){ return p1; }
	
	public Point getP2(){ return p2; }
	
	public double length(){
		double difX = (p1.getX()-p2.getX());
		difX = difX*difX;
		double difY = (p1.getY()-p2.getY());
		difY = difY*difY;
		double dist = Math.sqrt(difX+difY);
		return dist;
	}
	
	public double getSlope(){
		double difX = (p1.getX()-p2.getX());
		double difY = (p1.getY()-p2.getY());
		double m;
		if(difX != 0) { m = difY/difX; }
		else { m = 0; }
		return m;
	}
	
	public double getIntercept(){
		double b = p1.getY()-getSlope()*p1.getX();
		return b;
		//vertical lines, fool.
	}
	
	public boolean containsPoint(Point p){
		double x = p.getX();
		double dx = p1.getX() - p2.getX();
		if( x < p1.getX() || x > p2.getX() ) { return false; }
			//Point is outside of domain, cannot be on line.
		else {
			//Point is inside of domain, possible containment.
			//Check conditions on line.
			double m = getSlope();
			if(m != 0){
				//Line has slope. See if actual value 
				//matches predicted value.
				double actY = p.getY();
				double preY = m * (p.getX()) + getIntercept();
				if(preY == actY) { return true; }
				else { return false; }
			} 
			else if (m == 0 && dx != 0){
				//Line is horizontal. See if y-value of point
				//falls in constant range.
				if(p.getY() == p1.getY()) { return true;}
				else { return false; }
			}
			else if(m==0 && dx ==0 ){
				//Line is vertical. See if y-value of point 
				//falls within ordered range. Since lines 
				//are monotonic, they are constantly increasing
				//or decreasing. Thus it suffices to check if y-value 
				//falls between the ordered end points, since the direction
				//of increase is arbitrarily defined.
				double py = p.getY(); double ly1, ly2;
				if(p2.getY() < p1.getY()) { ly1 = p2.getY(); ly2 = p1.getY(); }
				else { ly1 = p1.getY(); ly2 = p2.getY(); }
					//Order End Points
				boolean lower = (py > ly1) || (py == ly1);
				boolean upper = (py < ly2) || (py == ly2);
					//Point's y-value is bounded by line's range.
				return (lower && upper);
			} else { return false; }
		}
	}
	
	public boolean intersectsLine(Line l){
		Point thatP1 = l.getP1();
		Point thatP2 = l.getP2();
		if(thatP1.isPoint(this.p1) || thatP2.isPoint(this.p2) ||
				thatP1.isPoint(this.p2) || thatP2.isPoint(this.p1)) { return true; } 
			//If start and ending points are equal, lines are redundant.
		String thislThatP1 = orientationOf(thatP1);
		String thislThatP2 = orientationOf(thatP2);
		String thatlThisP1 = l.orientationOf(this.p1);
		String thatlThisP2 = l.orientationOf(this.p2);
		boolean thisCase = !thislThatP1.equals(thislThatP2);
			//Evaluted true if points from input line are on opposing sides
		boolean thatCase = !thatlThisP1.equals(thatlThisP2);
			//Evaluated true if points from this line are on opposing sides of input line
		if(thisCase && thatCase) { return true; }
			//Case 1: Intersection if and only orientations are different
			//for both sets of point with respect to the opposing line.
		else{
			//Case 2: Intersection if special degenerate case of 
			//collinearity. In case of universal collinearity, domains 
			//and ranges must overlap.
			boolean collCase = thislThatP1.equals(Line.coll) &&
							   thislThatP2.equals(Line.coll) &&
							   thatlThisP1.equals(Line.coll) &&
							   thatlThisP2.equals(Line.coll);
				//Determine collinearity
			if(collCase){
				//DOES ORDERING OF Y'S AFFECT ALGORITHM?
					//Hypothesis/Conjecture: NO. Lines are monotonic. If
					//end points fall within the ordered range, then 
					//algorithm should be unaffected.
				
				double thatX1 = thatP1.getX(); double thatX2 = thatP2.getX();
					//X's ordered by default
				double thatY1 = thatP1.getY(); double thatY2 = thatP2.getY();
				if(thatY2 < thatY1){
					//Order That's Range End Points
					double buf = thatY2;
					thatY2 = thatY1;
					thatY1 = buf;
				}
				double thisX1 = p1.getX(); double thisX2 = p2.getX();
					//X's ordered by default.
				double thisY1 = p1.getY(); double thisY2 = p2.getY();
				if(thisY2 < thisY1){
					//Order This's Range End Points
					double buf = thisY2;
					thisY2 = thisY1;
					thisY1 = buf;
				}
				boolean thatX1Overlap = ((thatX1 > thisX1) || (thatX1 == thisX1)) &&
										((thatX1 < thisX2) || (thatX1 == thisX2));
					//thatX1 bounded by thisX1 and thisX2
				boolean thatX2Overlap = ((thatX2 > thisX1) || (thatX2 == thisX1)) &&
										((thatX2 < thisX2) || (thatX2 == thisX2));
					//thatX2 bounded by thisX1 and thisX2
				boolean thisX1Overlap = ((thisX1 > thatX1) || (thisX1 == thatX1)) &&
										((thisX1 < thatX2) || (thisX1 == thatX2));
					//thisX1 bounded by thatX1 and thatX2
				boolean thisX2Overlap = ((thisX2 > thatX1) || (thisX2 == thatX1)) &&
										((thisX2 < thatX2) || (thisX2 == thatX2));
					//thisX2 bounded by thatX1 and thatX2
				boolean totalThisXOverlap = thisX1Overlap && thisX2Overlap;
					//thisX1 AND thisX2 bounded by thatX1 and thatX2
				boolean XOverlap = thatX1Overlap || thatX2Overlap || totalThisXOverlap;
		
				boolean thatY1Overlap = ((thatY1 > thisY1) || (thatY1 == thisY1)) &&
										((thatY1 < thisY2) || (thatY1 == thisY2));
				boolean thatY2Overlap = ((thatX2 > thisY1) || (thatY2 == thisY1)) &&
										((thatY2 < thisY2) || (thatY2 == thisY2));
				boolean thisY1Overlap = ((thisY1 > thatY1) || (thisY1 == thatY1)) &&
										((thisY1 < thatY2) || (thisY1 == thatY2));
				boolean thisY2Overlap = ((thisY2 > thatY1) || (thisY2 == thatY1)) &&
										((thisY2 < thatY2) || (thisY2 == thatY2));
				boolean totalThisYOverlap = thisY1Overlap && thisY2Overlap;
				boolean YOverlap = thatY1Overlap || thatY2Overlap || totalThisYOverlap;
				
				return (XOverlap && YOverlap);
			} else return false;
		}
	}
	
	public String orientationOf(Point p){
		//Determines which side of the plane the inputted point
		//falls on with respect to the object line.
		double actY = p.getY();
		double slope = getSlope();
		double dx = p1.getX() - p2.getX();
		if(slope != 0){ 
			double preY = slope*p.getX() + getIntercept();
			if(actY > preY) { return top; }
			else if (actY == preY) { return coll; }
			else { return bottom; }
		}
		else if(slope == 0 && dx != 0) { 
			double preY = p1.getY(); 
			double startX = p1.getX(); double endX = p2.getX();
				//Make sure point is in between end points.
			boolean inside = (p.getX() == startX || p.getX() > startX)
							&& (p.getX() == endX || p.getX() < endX);
			if(actY > preY) { return top; }
			else if (actY == preY & inside) { return coll; }
			else { return bottom; }
		}
		else if(slope == 0 && dx == 0){
			double preX = p1.getX();
			double actX = p.getX();
			
			if(actX > preX) { return top; }
			else if(actX == preX) {
					//Make sure point is in between end points. Y's are not ordered like X's!
				double startY, endY;
				if(p1.getY() > p2.getY()){ startY = p2.getY(); endY = p1.getY(); }
				else{ startY = p1.getY(); endY = p2.getY(); }
				boolean inside = (p.getX() == startY || p.getX() > startY)
									&& (p.getX() == endY || p.getX() < endY);
				if( inside) { return coll; }
			}
			else { return bottom; }
		}
		return unk;
	}
	
	public boolean intersectsQuad(Quad q){
		if(q.containsPoint(p1)) { return true; }
		else if(q.containsPoint(p2)) { return true;} 
			//TODO: What if quad contains both points?
			//Then there is no intersection!
		else if(intersectsLine(q.getRightBound())) { return true; }
		else if(intersectsLine(q.getLeftBound())) { return true;}
		else if(intersectsLine(q.getBottomBound())) { return true;}
		else if(intersectsLine(q.getTopBound())) { return true; }
		else { return false; }
	}
}
