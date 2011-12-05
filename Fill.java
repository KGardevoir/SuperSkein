// Fill Generation
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

class Fill {
	boolean debugFlag;
	int Width;
	int Height; 
	Extruder ExtruderProperties;
	double SparseFillDensity;
	double RotateFillAngle;
	SSArea SparseFill;
	SSArea BridgeFill;

	Fill(boolean bFlag, int iWidth, int iHeight, double fillDensity) {
		RotateFillAngle=45.0;
		debugFlag=bFlag;
		Width=iWidth;
		Height=iHeight;
		if(fillDensity<0 || fillDensity>1.0) {
			System.out.println("Sparse Fill Density out of 0 to 1.0 range. Setting to 0.5");
			SparseFillDensity=0.5;
		} else SparseFillDensity=fillDensity;
		ExtruderProperties = new Extruder();
		SparseFill = new SSArea();
		SparseFill.setGridScale(0.01);
		double wallWidth=ExtruderProperties.calcWallWidth();
		for(double dx=0;dx<2*Width; dx+=2*wallWidth/fillDensity) {
			Rectangle2D thisRect = new Rectangle2D.Double(dx,0,wallWidth/fillDensity,2*Height);
			Area thisRectArea = new Area(thisRect);
			AffineTransform centerAreaTransform = new AffineTransform();
			centerAreaTransform.setToTranslation(-Width,-Height);
			thisRectArea.transform(centerAreaTransform);
			SparseFill.add(thisRectArea);
		}
		BridgeFill = new SSArea();
		BridgeFill.setGridScale(0.01);
		for(double dx=0;dx<2*Width; dx+=2*wallWidth) {
			Rectangle2D thisRect = new Rectangle2D.Double(dx,0,wallWidth,2*Height);
			Area thisRectArea = new Area(thisRect);
			AffineTransform centerAreaTransform = new AffineTransform();
			centerAreaTransform.setToTranslation(-Width,-Height);
			thisRectArea.transform(centerAreaTransform);
			BridgeFill.add(thisRectArea);
		}
	}

	ArrayList<SSArea> GenerateFill(ArrayList<SSArea> SliceList) {
		ArrayList<SSArea> FillAreaList = new ArrayList<SSArea>();
		double wallWidth=ExtruderProperties.calcWallWidth();
		for(int LayerNum=0; LayerNum<SliceList.size();LayerNum++) {
			SSArea thisArea = SliceList.get(LayerNum);
			// Shell area to subtract off slice.
			SSArea thisShell = new SSArea();
			thisShell.setGridScale(thisArea.getGridScale());
			thisShell.add(thisArea);
			thisShell.makeShell(wallWidth,8);
			// Fill mask area
			SSArea thisFill = new SSArea();
			thisFill.setGridScale(thisArea.getGridScale());
			thisFill.add(thisArea);
			thisFill.subtract(thisShell);
			// Identify bridge areas for special treatment.
			SSArea thisBridge = new SSArea();
			thisBridge.setGridScale(thisFill.getGridScale());
			thisBridge.add(thisFill);
			AffineTransform rotateFill = new AffineTransform();
			rotateFill.setToRotation(2*Math.PI*RotateFillAngle/360.0);
			BridgeFill.transform(rotateFill);
			SparseFill.transform(rotateFill);
			if(LayerNum==0 || LayerNum==SliceList.size()-1) thisFill.intersect(BridgeFill); // Bottom and Top layer special case.
			else {
				SSArea prevArea = SliceList.get(LayerNum-1);
				thisBridge.subtract(prevArea);
				// Identify cap areas for special treatment.
				SSArea nextArea = SliceList.get(LayerNum+1);
				SSArea thisCap = new SSArea();
				thisCap.setGridScale(thisFill.getGridScale());
				thisCap.add(thisArea);
				thisCap.subtract(nextArea);
				thisBridge.add(thisCap);
				if(!thisBridge.isEmpty()) {
					thisFill.subtract(thisBridge);
					thisBridge.intersect(BridgeFill);
					thisFill.intersect(SparseFill);
					thisFill.add(thisBridge);
				} else thisFill.intersect(SparseFill);
			}
			// Subtract bridge areas from fill mask
			FillAreaList.add(LayerNum,thisFill);
		}
		return FillAreaList;
	}
}

