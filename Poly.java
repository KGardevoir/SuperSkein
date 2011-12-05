// SSPoly extends Polygon

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

class SSPoly extends Polygon {
	double GridScale;
	double HeadSpeed;
	double Flowrate;
	boolean debugFlag;

	SSPoly(){
		GridScale=0.01;
		HeadSpeed=1000;
		Flowrate=0;
		debugFlag=false;
	}

	void setGridScale(double adouble){ GridScale=adouble; }
	double getGridScale() { return(GridScale); }

	void setHeadSpeed(double adouble){ HeadSpeed=adouble; }
	double getHeadSpeed(){ return(HeadSpeed); }

	void setFlowrate(double adouble) { Flowrate=adouble; }
	double getFlowrate() { return(Flowrate); }

	void setDebugFlag(boolean aBool) { debugFlag = aBool; }
	boolean getDebugFlag() { return(debugFlag); }

	void addPoint(double fx, double fy) {
		double scalefx = Math.round(fx/GridScale);
		double scalefy = Math.round(fy/GridScale);
		addPoint((int)scalefx,(int)scalefy);
	}

	public boolean contains(double fx, double fy) {
		double scalefx = Math.round(fx/GridScale);
		double scalefy = Math.round(fy/GridScale);
		boolean bContains = contains((int)scalefx,(int)scalefy);
		return(bContains);
	}

	void translate(double fx, double fy) {
		double scalefx = Math.round(fx/GridScale);
		double scalefy = Math.round(fy/GridScale);
		translate((int)scalefx,(int)scalefy);
	}

	ArrayList<SSPoly> Path2Polys(SSPath thisPath) {
		ArrayList<SSPoly> returnList = new ArrayList<SSPoly>();
		SSPoly thisPoly = new SSPoly();
		thisPoly.setDebugFlag(debugFlag);
		thisPoly.setGridScale(GridScale);
		returnList.add(thisPoly);
		PathIterator pathIter = thisPath.getPathIterator(new AffineTransform());
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
				thisPoly = new SSPoly();
				thisPoly.setDebugFlag(debugFlag);
				thisPoly.setGridScale(GridScale);
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
