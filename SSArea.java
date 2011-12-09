import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;

class SSArea extends Area {
	Configuration config; 

	SSArea(Configuration conf){
		config = conf; 
	}
	SSArea(Configuration conf, Slice aSlice){
		this(conf); 
		Slice2Area(aSlice); 
	}
	
	void Slice2Area(Slice thisSlice){
		SSPoly path2polys = new SSPoly(config);
		ArrayList<SSPoly> PolyList = path2polys.Path2Polys(thisSlice.slice);
		for(int i=0;i<PolyList.size();i++) {
			SSPoly thisPoly = PolyList.get(i);
			this.exclusiveOr(new Area((Shape) thisPoly));
		}
		AffineTransform scaleAreaTransform = new AffineTransform();
		scaleAreaTransform.setToScale(config.MachinePercision,config.MachinePercision);
		this.transform(scaleAreaTransform);
	}

	void makeShell(int num_walls, int dirCount){
		//double sqt2 = Math.sqrt(2);
		//TODO fix implementation (wallWidth really should be in layers)
		AffineTransform shiftTrans = new AffineTransform();
		Area innerArea = new Area(this);
		for(int i=0;i<dirCount;i++) {
			double dx = config.extruder.XYThickness*num_walls*Math.cos(i*2*Math.PI/dirCount);
			double dy = config.extruder.XYThickness*num_walls*Math.sin(i*2*Math.PI/dirCount);
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
