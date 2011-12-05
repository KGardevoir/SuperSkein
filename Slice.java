import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

// Slice Class
//
class Slice {
	public SSPath SlicePath;
	PApplet applet; 
	//Right now this is all in the constructor.
	//It might make more sense to split these
	//out but that's a pretty minor difference
	//at the moment.
	Slice(PApplet app, Mesh InMesh, double ZLevel) throws EmptyLineException{
		applet = app; 
		ArrayList<SSLine> UnsortedLines;
		ArrayList<SSLine> Lines;
		SSLine Intersection;
		UnsortedLines = new ArrayList<SSLine>();
		for(int i = InMesh.Triangles.size()-1;i>=0;i--){
			Triangle tri = (Triangle) InMesh.Triangles.get(i);
			Intersection = tri.GetZIntersect(ZLevel);
			if(Intersection != null) UnsortedLines.add(Intersection);
		}
		
		
		if(UnsortedLines == null) throw new EmptyLineException();
				
		//Slice Sort: arrange the line segments so that
		//each segment leads to the nearest available
		//segment. This is accomplished by using two
		//arraylists of lines, and at each step moving
		//the nearest available line segment from the
		//unsorted pile to the next slot in the sorted pile.
		Lines = new ArrayList<SSLine>();
		Lines.add(UnsortedLines.get(0));
		int FinalSize = UnsortedLines.size();
		UnsortedLines.remove(0);

		//ratchets for distance
		//dist_flipped exists to catch flipped lines
		double dist, dist_flipped;
		double mindist = 10000;

		int iNextLine;

		double epsilon = 1e-6;
		
		//while(Lines.size()<FinalSize)
		while(UnsortedLines.size()>0){
			SSLine CLine = (SSLine) Lines.get(Lines.size()-1);//Get last
			iNextLine = (Lines.size()-1);
			mindist = 10000;
			boolean doflip = false;
			for(int i = UnsortedLines.size()-1;i>=0;i--){
				SSLine LineCandidate = (SSLine) UnsortedLines.get(i);
				dist				 = mag(LineCandidate.x1-CLine.x2, LineCandidate.y1-CLine.y2);
				dist_flipped 		 = mag(LineCandidate.x2-CLine.x2, LineCandidate.y2-CLine.y2); // flipped
					
				if(dist<epsilon){
					// We found exact match.	Break out early.
					doflip = false;
					iNextLine = i;
					mindist = 0;
					break;
				}
				if(dist_flipped<epsilon){
					// We found exact flipped match.	Break out early.
					doflip = true;
					iNextLine = i;
					mindist = 0;
					break;
				}
				if(dist<mindist){
					// remember closest nonexact matches to end
					doflip = false;
					iNextLine = i;
					mindist = dist;
				}
				if(dist_flipped<mindist){
					// remember closest flipped nonexact matches to end
					doflip = true;
					iNextLine = i;
					mindist = dist_flipped;
				}
			}
			SSLine LineToMove = (SSLine) UnsortedLines.get(iNextLine);
			if(doflip) LineToMove.Flip();
			Lines.add(LineToMove);
			UnsortedLines.remove(iNextLine);
		}
//		Paths = new ArrayList();
		SSLine thisLine = (SSLine) Lines.get(0);	 
		SlicePath = new SSPath(thisLine);
		// Paths.add(thisPath);
		PVector prevPt = wrapPVector(thisLine.x2,thisLine.y2);
		for(int i=1;i<Lines.size();i++){
			SSLine newLine = (SSLine) Lines.get(i);
			boolean connectFlag = true;
			if(newLine.x1 != prevPt.x || newLine.y1 != prevPt.y){
				SlicePath.closePath();
				connectFlag = false;
			}
			SlicePath.append(newLine,connectFlag);
			prevPt = wrapPVector(newLine.x2,newLine.y2);
		}
		SlicePath.closePath();
	}
	private double mag(double d, double e) {
		return Math.sqrt(d*d+e*e);
	}
	private PVector wrapPVector(double x, double y){ return new PVector((float)x, (float)y);}
} 

