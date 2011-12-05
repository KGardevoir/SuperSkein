import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;

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
			SSPoly thisPoly = (SSPoly) PolyList.get(i);
			this.exclusiveOr(new Area((Shape) thisPoly));
		}
		AffineTransform scaleAreaTransform = new AffineTransform();
		scaleAreaTransform.setToScale(GridScale,GridScale);
		this.transform(scaleAreaTransform);
	}

	void makeShell(double wallWidth, int dirCount){
		//double sqt2 = Math.sqrt(2);
		//TODO fix implementation (wallWidth really should be in layers)
		AffineTransform shiftTrans = new AffineTransform();
		Area innerArea = new Area(this);
		for(int i=0;i<dirCount;i++) {
			double dx = wallWidth*Math.cos(i*360/dirCount);
			double dy = wallWidth*Math.sin(i*360/dirCount);
			shiftTrans.setToTranslation(dx,dy);
			Area shiftCopy = this.createTransformedArea(shiftTrans);
			shiftCopy.subtract(this);
			shiftTrans.setToTranslation(-dx,-dy); shiftCopy.transform(shiftTrans);
			if(shiftCopy.isEmpty()) System.out.println("	makeShell: shiftCopy is Empty");
			innerArea.subtract(shiftCopy);
		}
		if(innerArea.isEmpty()){
			System.out.println(" makeShell: innerArea is Empty");
		} else {
			this.subtract(innerArea);
		}
	}
}
