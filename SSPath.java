// Path Class
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import processing.core.PVector;

class SSPath extends Path2D.Double {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8034991198131925267L;
	private Configuration config; 
	
	// constructor.
	// Initializes a new path object, with no path points.
	SSPath(Configuration conf){
		config = conf; 
	}

	SSPath(Configuration conf, SSLine firstLine) {
		super(firstLine);
		config = conf; 
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
	void reverse(){
		PathIterator iter = this.getPathIterator(new AffineTransform()); 
		ArrayList<java.lang.Double> list = new ArrayList<java.lang.Double>(); 
		ArrayList<Integer> olist = new ArrayList<Integer>(); 
		for(; !iter.isDone(); iter.next()){
			double[] d = new double[6]; 
			olist.add(iter.currentSegment(d));
			list.add(d[0]);
			list.add(d[1]);
		}
		this.reset(); 
		for(int i = 0, j = 0; i < olist.size(); i++, j+=2){
			if(olist.get(i) == PathIterator.SEG_MOVETO || i == olist.size()-1){
				int k = j; 
				for(; i < olist.size(); i++, j+=2){
					if(olist.get(i) == PathIterator.SEG_CLOSE){
						list.set(j, list.get(k)); 
						list.set(j+1, list.get(k+1)); 
						break; 
					}
				}
			}
		}
		for(int i = olist.size()-1, j = list.size()-1; i >= 0; i--, j-=2){
			double s2 = list.get(j); 
			double s1 = list.get(j-1); 
			try{
				if(olist.get(i) == PathIterator.SEG_MOVETO) this.closePath(); 
				else if(olist.get(i) == PathIterator.SEG_CLOSE) this.moveTo(s1, s2);
				else this.lineTo(s1, s2);
			} catch (java.awt.geom.IllegalPathStateException e){
				System.out.println("Something happened"); 
			}
		}
	}
	// If the point is inside the closed path, returns true.	False otherwise.
	// If the path is not closed, then returns false.
	boolean containsPoint(double X, double Y){
		return( super.contains(X,Y) );
	}
}

