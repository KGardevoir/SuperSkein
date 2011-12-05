// Line Class
// Once we have the slices as 2D lines,
// we never look back.
// From this version (1.3ish?) SSLine will
// have both SPEED and PLASTIC FLOW RATE.
import java.awt.geom.Line2D;
import processing.core.PConstants;
import processing.core.PVector;


class SSLine extends Line2D.Double {
	
//	double x1,y1,x2,y2;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9161900654120601922L;
	double HeadSpeed;
	double Flowrate;
	
	final double epsilon = 1e-6;
	
	SSLine(double nx1, double ny1, double nx2, double ny2) {
		super(nx1,ny1,nx2,ny2);
		HeadSpeed = 1000; //By default it at least does move.
		Flowrate = 0; //By default the plastic does not flow.
	}


	SSLine(PVector pt1, PVector pt2) {
		super(pt1.x,pt1.y,pt2.x,pt2.y);
		HeadSpeed = 1000; //By default it at least does move.
		Flowrate = 0; //By default the plastic does not flow.
	}
	
	
	void setPoint1(PVector pt) {
		x1 = pt.x;
		y1 = pt.y;
	}


	void setPoint2(PVector pt) {
		x2 = pt.x;
		y2 = pt.y;
	}

	PVector wrapPVector(double x, double y){return new PVector((float)x, (float)y);}
	PVector getPoint1() { return wrapPVector(x1,y1); }
	PVector getPoint2() { return wrapPVector(x2,y2); }


	void Scale(double Factor){
		x1=x1*Factor;
		x2=x2*Factor;
		y1=y1*Factor;
		y2=y2*Factor;
	}


	void Flip(){
		double xn, yn;
		xn = x1;
		yn = y1;
		x1 = x2;
		y1 = y2;
		x2 = xn;
		y2 = yn;
	}

	
	void Rotate(double Angle){
		double xn,yn;
		xn = x1*Math.cos(Angle) - y1*Math.sin(Angle);
		yn = x1*Math.sin(Angle) + y1*Math.cos(Angle);
		x1 = xn;
		y1 = yn;
		xn = x2*Math.cos(Angle) - y2*Math.sin(Angle);
		yn = x2*Math.sin(Angle) + y2*Math.cos(Angle);
		x2 = xn;
		y2 = yn;
	}

	void Translate(double tX, double tY){
		x1 = x1 + tX;
		x2 = x2 + tX;
		y1 = y1 + tY;
		y2 = y2 + tY;
	}
	
	double Length(){ return mag(y2-y1, x2-x1); }
	private double mag(double d, double e) {
		return Math.sqrt(d*d+e*e);
	}


	double RadianAngle(){ return Math.atan2(y2-y1, x2-x1); }
	
	
	PVector ClosestSegmentPointToXY(double px, double py){
		double xd, yd, u;
		PVector pt;

		xd = x2 - x1;
		yd = y2 - y1;
		if (Math.abs(xd) < epsilon && Math.abs(yd) < epsilon) {
			pt = wrapPVector(x1, y1);
			return pt;
		}
		u = ((px - x1) * xd + (py - y1) * yd) / (xd * xd + yd * yd);
		
		if (u < 0.0) pt = wrapPVector(x1, y1);
		else if (u > 1.0) pt = wrapPVector(x2, y2);
		else pt = wrapPVector(x1 + u * xd, y1 + u * yd);
		
		return pt;
	}


	PVector ClosestExtendedLinePointToXY(double px, double py){
		double xd, yd, u;
		PVector pt;

		xd = x2 - x1;
		yd = y2 - y1;
		if (Math.abs(xd) < epsilon && Math.abs(yd) < epsilon){
			pt = wrapPVector(x1, y1);
			return pt;
		}
		u = ((px - x1) * xd + (py - y1) * yd) / (xd * xd + yd * yd);
		pt = wrapPVector(x1 + u * xd, y1 + u * yd);
		return pt;
	}


	double MinimumSegmentDistanceFromXY(double x, double y){
		PVector pt = ClosestSegmentPointToXY(x, y);
		return mag(pt.y-y, pt.x-x);
	}


	// Returns the distance from the given XY point to the closest
	double MinimumExtendedLineDistanceFromXY(double x, double y){
		PVector pt = ClosestExtendedLinePointToXY(x, y);
		return mag(pt.y-y, pt.x-x);
	}
	
	
	// Returns null if the two line segments don't intersect each other.
	// Otherwise returns intersection point as a PVector.
	// NOTE: coincident lines don't count as intersecting.
	PVector FindSegmentsIntersection(SSLine line2){
		double dx1 = x2 - x1;
		double dy1 = y2 - y1;
		
		double dx2 = line2.x2 - line2.x1;
		double dy2 = line2.y2 - line2.y1;
		
		double dx3 = x1 - line2.x1;
		double dy3 = y1 - line2.y1;
		
		double d	= dy2 * dx1 - dx2 * dy1;
		double na = dx2 * dy3 - dy2 * dx3;		
		double nb = dx1 * dy3 - dy1 * dx3;
		
		if(d == 0) {
			//if (na == 0.0 && nb == 0.0) {
			//	return null;	// Lines are coincident.
			//}
			return null;	// No intersection; lines are parallel
		}
		
		double ua = na / d;
		double ub = nb / d;
		
		if(ua < 0.0 || ua > 1.0) return null; // Intersection wouldn't be inside first segment

		if(ub < 0.0 || ub > 1.0) return null; // Intersection wouldn't be inside second segment

		double xi = x1 + ua * dx1;
		double yi = y1 + ua * dy1;
		return wrapPVector(xi, yi);
	}


	// Returns null if the two extended lines are parallel.
	// Otherwise returns intersection point as a PVector.
	// NOTE: coincident lines don't count as intersecting.
	PVector FindExtendedLinesIntersection(SSLine line2){
		double dx1 = x2 - x1;
		double dy1 = y2 - y1;
		
		double dx2 = line2.x2 - line2.x1;
		double dy2 = line2.y2 - line2.y1;
		
		double dx3 = x1 - line2.x1;
		double dy3 = y1 - line2.y1;
		
		double d	= dy2 * dx1 - dx2 * dy1;
		double na = dx2 * dy3 - dy2 * dx3;		
		//TODO fix? double nb = dx1 * dy3 - dy1 * dx3;
		
		if (d == 0) {
			//if (na == 0.0 && nb == 0.0) {
			//	return null;	// Lines are coincident.
			//}
			return null;	// No intersection; lines are parallel
		}
		
		double ua = na / d;
		//TODO fix? double ub = nb / d;
		
		double xi = x1 + ua * dx1;
		double yi = y1 + ua * dy1;
		return wrapPVector(xi, yi);
	}


	// Returns a SSLine of the current segment, if it were shifted
	// to the right by the given amount.
	SSLine Offset(double offsetby){
		double ang = this.RadianAngle();
		double perpang = ang - PConstants.HALF_PI;
		double nux1 = x1 + offsetby * Math.cos(perpang);
		double nuy1 = y1 + offsetby * Math.sin(perpang);
		double nux2 = x2 + offsetby * Math.cos(perpang);
		double nuy2 = y2 + offsetby * Math.sin(perpang);
		return new SSLine(nux1, nuy1, nux2, nuy2);
	}
	
	
	// Returns a PVector with the point where two joined lines would
	// intersect if both lines were offset to the right by the given
	// amount.	The end point of the first line segment must be the
	// exact same point as the start point of the second line.
	// Returns null if the two lines aren't joined.
	PVector FindOffsetIntersectionByBisection(SSLine line2, double offsetby){
		if (x2 != line2.x1 || y2 != line2.y1) return null;
		
		SSLine offline1 = this.Offset(offsetby);
		SSLine offline2 = line2.Offset(offsetby);
		return offline1.FindExtendedLinesIntersection(offline2);
	}
}