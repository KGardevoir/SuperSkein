import java.util.ArrayList;
import processing.core.PApplet;

// Slice Class
//
class Slice {
	public SliceTree slice;
	PApplet applet; 
	@SuppressWarnings("unused")
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
		while(!UnsortedLines.isEmpty()){
			SSLine CLine = (SSLine) Lines.get(0);//Get First
			iNextLine = 0;
			mindist = 10000;
			boolean doflip = false;
			for(int i = UnsortedLines.size()-1; i > 0; i++){//go backwards for constant time remove function
				SSLine LineCandidate = UnsortedLines.get(i);
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
			SSLine LineToMove = UnsortedLines.get(iNextLine);
			if(doflip) LineToMove.Flip();
			Lines.add(LineToMove);
			UnsortedLines.remove(iNextLine);
		}//this "sorts" the lines, it actually just pairs them with their next neighbor to create paths
//		Paths = new ArrayList();
		
		//first we must determine if is inner or outer to do this we have to separate the paths
		ArrayList<SSPath> paths = new ArrayList<SSPath>(); 
		int k = 0, i = 0; 
		while(k < Lines.size()){
			paths.add(new SSPath());
			SSLine plines = Lines.get(k++);
			double sx, sy; 
			paths.get(i).addPoint(sx = plines.x1, sy = plines.y1); 
			boolean notconnected = true; 
			while(notconnected && k < Lines.size()){
				SSLine npline = Lines.get(k++); 
				double x, y; 
				paths.get(i).addPoint(x = npline.x2, y = npline.y2); 
				if(Math.abs(sx-x) > 1e6 && Math.abs(sy-y) > 1e6){
					notconnected = true; 
					break;
				}
			}
			i++; 
		}
		//turn path into a bunch of other paths by using a tree (so we know who falls where in relation to whom)
		slice = new SliceTree(paths); 
	}
	private double mag(double d, double e) {
		return Math.sqrt(d*d+e*e);
	}
} 

