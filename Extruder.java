// Extruder Class
import java.lang.Math;

class Extruder {
	int ToolNum;
	Material Filament;
	double ZThick;
	double Diameter;
	double FlowRate;

	Extruder() {
		ToolNum=0;
		Filament=new Material("ABS");
		Diameter=0.6;
		ZThick=0.37;
	}

	Extruder(int anInt) {
		ToolNum=anInt;
		Filament=new Material("ABS");
		Diameter=0.6;
		ZThick=0.37;
	}

	Extruder(int anInt, String aString) {
		ToolNum=anInt;
		Filament=new Material(aString);
		Diameter=0.6;
		ZThick=0.37;
	}

	void setDiameter(double aFloat) {
		Diameter=aFloat;
		if(ZThick>Diameter) {
			System.out.println("Z thickness greater than extruded diameter. Setting Z thickness to half diameter.");
			ZThick=Diameter/2;
		}
	}

	void setZThick(double aFloat) {
		if(aFloat<Diameter) {
			ZThick=aFloat;
		} else {
			System.out.println("Z thickness greater than extruded diameter. Setting Z thickness to half diameter.");
			ZThick=Diameter/2;
		}
	}

	void setFlowRate(double aFloat) {
		FlowRate=aFloat;
	}

	double calcWallWidth() {
		double freespace_area=Math.PI*Math.pow(Diameter/2,2);
		return freespace_area/ZThick;
	}
}

