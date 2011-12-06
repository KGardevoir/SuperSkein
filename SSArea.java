import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import processing.core.PVector;

class SSArea extends Area {
	double GridScale;
	double HeadSpeed;
	double FlowRate;

	SSArea(){
		GridScale=0.01;
		HeadSpeed=1000.;
		FlowRate=0.;
	}
	SSArea(Slice aSlice){
		this(); 
		Slice2Area(aSlice); 
	}
	
	void setGridScale(double d){ GridScale = d; }
	double getGridScale(){ return GridScale; }

	void setHeadSpeed(double aHeadSpeed){ HeadSpeed = aHeadSpeed; }
	double getHeadSpeed(){ return HeadSpeed; }

	void setFlowRate(double aFlowRate){ FlowRate = aFlowRate; }
	double getFlowRate(){ return FlowRate; }

	void Slice2Area(Slice thisSlice){
		SSPoly path2polys = new SSPoly();
		path2polys.setGridScale(GridScale);
		ArrayList<SSPoly> PolyList = path2polys.Path2Polys(thisSlice.SlicePath);
		for(int i=0;i<PolyList.size();i++) {
			SSPoly thisPoly = PolyList.get(i);
			this.exclusiveOr(new Area((Shape) thisPoly));
		}
		AffineTransform scaleAreaTransform = new AffineTransform();
		scaleAreaTransform.setToScale(GridScale,GridScale);
		this.transform(scaleAreaTransform);
	}

	void makeShell(double wallWidth, int walls, int dirCount){
		//double sqt2 = Math.sqrt(2);
		//TODO fix implementation (wallWidth really should be in layers)
		PathIterator path = this.getPathIterator(new AffineTransform()); 
		//first we must determine if is inner or outer to do this we have to separate the paths
		ArrayList<SSPath> paths = new ArrayList<SSPath>(); 
		double sx[] = new double[6], x[] = new double[6]; 
		for(int i = 0;!path.isDone(); i++, path.next()){
			paths.add(new SSPath());
			int num = path.currentSegment(sx);
			paths.get(i).addPoint(sx[0], sx[1]); 
			boolean pflag = true; 
			while(pflag && !path.isDone()){
				path.next(); 
				path.currentSegment(x);
				paths.get(i).addPoint(x[0], x[1]); 
				for(int j = 0; j < 6; j++){
					pflag = false; 
					if(Math.abs(x[j]-sx[j]) > 1e6){
						pflag = true; 
						break;
					}
				}
			}
		}
		//next we determine if the paths are contained within each other
		boolean pathtype[][] = new boolean[paths.size()][paths.size()]; 
		for(int i = 0; i < paths.size(); i++)
			for(int j = 0; j < paths.size(); j++)
				if(i == j) pathtype[j][i] = false; //the diagonal should be false (path doesn't contain itself)
				else pathtype[j][i] = paths.get(j).contains(paths.get(i).getCurrentPoint());//assume that we don't have intersecting paths
		//Now we have all the information we need, start with false and xor along the row until we have wither the node is an external or internal node
		boolean[] fptypes = new boolean[paths.size()]; 
		for(int i = 0; i < paths.size(); i++){
			boolean ptype = false; 
			for(int j = 0; j < paths.size(); j++)
				ptype ^= pathtype[j][i]; 
			fptypes[i] = ptype; 
		}
		//now we have the last bit of information we need, the "path types", either inner or outer. 
		
		/*for(int i=0;i<dirCount;i++) {
			double dx = wallWidth*Math.cos(i*2*Math.PI/dirCount);
			double dy = wallWidth*Math.sin(i*2*Math.PI/dirCount);
			shiftTrans.setToTranslation(dx,dy);
			Area shiftCopy = this.createTransformedArea(shiftTrans);
			shiftCopy.subtract(this);
			shiftTrans.setToTranslation(-dx,-dy); shiftCopy.transform(shiftTrans);
			if(shiftCopy.isEmpty()) System.out.println("	makeShell: shiftCopy is Empty");
			innerArea.subtract(shiftCopy);
		}*/
		
		if(innerArea.isEmpty()){
			System.out.println(" makeShell: innerArea is Empty");
		} else {
			this.subtract(innerArea);
		}
	}
}
