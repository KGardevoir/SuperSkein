import java.util.ArrayList;
import processing.core.PApplet;

// Slice Class
//
class Slice {
	public SliceTree slice;
	PApplet applet; 
	Configuration config; 
	@SuppressWarnings("unused")
	//Right now this is all in the constructor.
	//It might make more sense to split these
	//out but that's a pretty minor difference
	//at the moment.
	Slice(PApplet app, Configuration conf, Mesh InMesh, double ZLevel) throws EmptyLineException{
		applet = app; 
		config = conf; 
		ArrayList<SSLine> UnsortedLines;
		ArrayList<SSLine> Lines;
		SSLine Intersection;
		UnsortedLines = new ArrayList<SSLine>();
		for(int i = InMesh.Triangles.size()-1;i>=0;i--){
			Triangle tri = (Triangle) InMesh.Triangles.get(i);
			Intersection = tri.GetZIntersect(conf, ZLevel);
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
		ArrayList<SSPath> paths = new ArrayList<SSPath>(); 
		int k = 0; 
		double sx, sy; 
		for(int i = 0; k < Lines.size(); i++){
			SSPath path = new SSPath(config); 
			paths.add(path);
			SSLine line = Lines.get(k++);
			path.moveTo(line.x1, line.y1); 
			path.lineTo(line.x2, line.y2); 
			sx = line.x1; sy = line.y1; 
			double lx = line.x2, ly = line.y2; 
			while(k < Lines.size()){
				line = Lines.get(k++); 
				if(Math.abs(line.x2 - sx) < 1e-6 && Math.abs(line.y2-sy) < 1e-6){
					//end of loop
					path.closePath(); 
					break; 
				} else if(Math.abs(lx - line.x1) > 1e-6 || Math.abs(ly - line.y1) > 1e-6) {//check to see if we need to force the end of loop
					path.closePath(); 
					break; 
				} else {
					path.addPoint(lx = line.x2, ly = line.y2); 
				}
			}
		}
		slice = new SliceTree(paths); 
	}
	private double mag(double d, double e) {
		return Math.sqrt(d*d+e*e);
	}
} 

