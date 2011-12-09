import processing.core.PApplet;
// Material Class

class Material {
	private String TypeName;
	private double MeltTemp;
	private double ExtrudeTemp;
	Material(String mname,double Etemp, double BTemp){
		TypeName = mname; 
		ExtrudeTemp = Etemp;
		MeltTemp = BTemp; 
	}
	Material(String aString) {
		if(aString == "ABS") {
			TypeName=aString;
			MeltTemp=100.0;
			ExtrudeTemp=220.0;
		} else if(aString == "PLA"){
			TypeName=aString;
			MeltTemp=60;
			ExtrudeTemp=200.0;
		}
	}
	
	String getName(){ return TypeName; }
	double getMeltTemp() { return MeltTemp; }
	double getExtrudeTemp() { return ExtrudeTemp; }
	public int parse(String[] input, int index) {
		for(; index < input.length; index++){
			String[] pieces = PApplet.split(input[index], ' ');
			if(pieces.length == 1){//switch modes
				if(pieces[0].equals("[MATERIAL]"));
				else if(pieces[0].startsWith("[") && pieces[0].endsWith("]")) return index; 
			}
			if (pieces.length == 2){
				if(pieces[0].equals("TOOL_TYPE")) TypeName = pieces[1];
				if(pieces[0].equals("MELTING_TEMPERATURE")) MeltTemp=Double.parseDouble(pieces[1]);
				if(pieces[0].equals("EXTRUDE_TEMPERATURE")) ExtrudeTemp=Double.parseDouble(pieces[1]);
			}
		}
		return index;
	}
	public String getParameters(int perc) {
		String us = "[MATERIAL]\n"; 
		us += String.format("TOOL_TYPE %s\n", TypeName); 
		us += String.format("MELTING_TEMPERATURE %."+perc+"f", MeltTemp); 
		us += String.format("EXTRUDE_TEMPERATURE %."+perc+"f", ExtrudeTemp);		
		return us + "\n";
	}

}
