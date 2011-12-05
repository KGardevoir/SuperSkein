// Material Class

class Material {
	String TypeName;
	double MeltTemp;
	double ExtrudeTemp;

	Material(String aString) {
		if(aString == "ABS") {
			TypeName=aString;
			MeltTemp=100.0;
			ExtrudeTemp=220.0;
		} else {
			TypeName=aString;
			MeltTemp=0;
			ExtrudeTemp=0;
		}
	}

	void setMeltTemp(double aFloat) {
		MeltTemp=aFloat;
	}
	void setExtrudeTemp(double aFloat) {
		ExtrudeTemp=aFloat;
	}

}
