// SSPoly extends Polygon

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

class SSPoly extends Polygon {
	/**
	 * 
	 */
	private static final long serialVersionUID = -119460655901776709L;
	private Configuration config;
	boolean debugFlag;

	SSPoly(Configuration conf){
		config = conf; 
	}
	/**
	 * Create a Polygon based on a path. It will use up until the first SEG_CLOSE
	 */
	SSPoly(SSPath path){
		PathIterator ip = path.getPathIterator(new AffineTransform()); 
		boolean stop_flag = true; 
		for(; !ip.isDone() && stop_flag; ip.next()){
			double[] c = new double[6]; 
			int type = ip.currentSegment(c); 
			switch(type){
			case PathIterator.SEG_MOVETO:
				this.addPoint(c[0], c[1]); 
				break; 
			case PathIterator.SEG_LINETO: 
				this.addPoint(c[0], c[1]);
				break; 
			case PathIterator.SEG_CLOSE: 
				stop_flag = false; 
				break; 
			}
		}
		
	}
	void setDebugFlag(boolean aBool) { debugFlag = aBool; }
	boolean getDebugFlag() { return(debugFlag); }

	void addPoint(double fx, double fy) {
		double scalefx = Math.round(fx/config.MachinePercision);
		double scalefy = Math.round(fy/config.MachinePercision);
		addPoint((int)scalefx,(int)scalefy);
	}

	public boolean contains(double fx, double fy) {
		double scalefx = Math.round(fx/config.MachinePercision);
		double scalefy = Math.round(fy/config.MachinePercision);
		boolean bContains = contains((int)scalefx,(int)scalefy);
		return(bContains);
	}

	void translate(double fx, double fy) {
		double scalefx = Math.round(fx/config.MachinePercision);
		double scalefy = Math.round(fy/config.MachinePercision);
		translate((int)scalefx,(int)scalefy);
	}

	ArrayList<SSPoly> Path2Polys(SliceTree slice) {
		ArrayList<SSPoly> returnList = new ArrayList<SSPoly>();
		SSPoly thisPoly = new SSPoly(config);
		thisPoly.setDebugFlag(debugFlag);
		returnList.add(thisPoly);
		PathIterator pathIter = slice.flatten(config).getPathIterator(new AffineTransform());
		double[] newCoords = {0.0,0.0,0.0,0.0,0.0,0.0};
		double[] prevCoords = {0.0,0.0,0.0,0.0,0.0,0.0};
		double[] startCoords = {0.0,0.0,0.0,0.0,0.0,0.0};
		int segType = pathIter.currentSegment(prevCoords);
		segType = pathIter.currentSegment(startCoords);
		pathIter.next();
		while(!pathIter.isDone()){
			segType = pathIter.currentSegment(newCoords);
			if(segType == PathIterator.SEG_LINETO) {
				if(debugFlag) System.out.print(".");
				thisPoly.addPoint(prevCoords[0],prevCoords[1]);
				segType = pathIter.currentSegment(prevCoords);
			} else if(segType == PathIterator.SEG_CLOSE) {
				if(debugFlag) System.out.println("\n	Polygon: "+returnList.size()+"	SEG_CLOSE: "+newCoords[0]+" "+newCoords[1]);
				thisPoly.addPoint(prevCoords[0],prevCoords[1]);
				segType = pathIter.currentSegment(prevCoords);
			} else if(segType == PathIterator.SEG_MOVETO) {
				if(debugFlag) System.out.println("\n	Polygon: "+returnList.size()+"	SEG_MOVETO: "+newCoords[0]+" "+newCoords[1]);
				thisPoly = new SSPoly(config);
				thisPoly.setDebugFlag(debugFlag);
				returnList.add(thisPoly);
				segType = pathIter.currentSegment(prevCoords);
				segType = pathIter.currentSegment(startCoords);
			} else {
				System.out.println("	Polygon: " + returnList.size() + "	segType: " + segType + "\n");
				segType = pathIter.currentSegment(prevCoords);
			}
			pathIter.next();
		}
		if(debugFlag) System.out.println(" SSPoly Count: " + returnList.size() + "\n");
		return(returnList);
	}

}
