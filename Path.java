// Path Class
import java.awt.geom.Path2D;

import processing.core.PVector;

class SSPath extends Path2D.Float {

	double HeadSpeed;
	double Flowrate;
	
	// constructor.
	// Initializes a new path object, with no path points.
	SSPath(){
		HeadSpeed=1000;
		Flowrate=0;
	}

	SSPath(SSLine firstLine) {
		super(firstLine);
		HeadSpeed = firstLine.HeadSpeed;
		Flowrate = firstLine.Flowrate;
	}

	// Adds a point to the end of the path.
	void addPoint(double X, double Y){
		super.lineTo(X,Y);
	}


	// Adds a point to the end of the path.
	void addPoint(PVector pt){
		super.lineTo(pt.x,pt.y);
	}

	// Closes the path if it is not already closed.
	// A closed path is one that ends where it started.
//	void close()
//	{
//		super.closePath();
//	}

	// If the point is inside the closed path, returns true.	False otherwise.
	// If the path is not closed, then returns false.
	boolean containsPoint(PVector testpoint){
		return( super.contains(testpoint.x,testpoint.y));
	}

	// If the point is inside the closed path, returns true.	False otherwise.
	// If the path is not closed, then returns false.
	boolean containsPoint(double X, double Y){
		return( super.contains(X,Y) );
	}
}

