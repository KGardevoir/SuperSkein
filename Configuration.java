import java.io.PrintWriter;

import processing.core.PApplet;

//Configuration
//This class acts both as a writer for config.txt
//and as a storage space for all configuration variables.
//Ideally, if you create a new user-settable variable,
//it should be a member of this class and get a line in
//the configuration file.

//~config.txt, obviously

class Configuration {
	
	public String FileName;
	
	public double PreScale, XRotate, PrintFeedrate, OrbitalFeedrate;
	public double Sink, FirstLayer;
	public int BuildPlatformWidth, BuildPlatformHeight; 
	public double BridgeFeedrateModifier;
	public Extruder extruder; 
	public int Percision; 
	private PApplet applet;

	public double BacklashX, BacklashY, BacklashZ; 
 
	public int ShellThickness; 
	public double InFill;
	public double SupportDegree; 
	public boolean AddSupport; 
	public double MachinePercision; //this is the so called "grid scale"
	//config values of last resort
	Configuration(PApplet app) {
		MachinePercision = 0.01; //1/100th mm percision, Z is naturally much higher
		Percision = 4; 
		applet = app; 
		PreScale = 1.0;
		XRotate = 0;
		FileName="";	
		PrintFeedrate = 1000;
		OrbitalFeedrate = 2000; 
		extruder = new Extruder(0.27, 0.28, 0.35);
		Sink = 0; 
		FirstLayer = extruder.ZThickness/2.; //start slicing in the middle of the layer
		BuildPlatformWidth = 100; 
		BuildPlatformHeight = 100; 
		BacklashX = BacklashY = BacklashZ = 0; 
		
		ShellThickness = 2; 
		InFill = 0.3; 
		SupportDegree = 0.35; 
		BridgeFeedrateModifier = 1.4; 
		AddSupport = true; 
	}

	void Load(){
		String[] input = applet.loadStrings("config.txt");
		int index = 0;
		
		for(index = 0; index < input.length; index++) {
			String[] pieces = PApplet.split(input[index], ' ');
			if(pieces.length == 1){//switch modes
				if(pieces[0].equals("[MATERIAL]")) index = extruder.Filament.parse(input, index); 
				if(pieces[0].equals("[TOOL]")) index = extruder.parse(input, index);
				if(pieces[0].equals("[MACHINE]")); //continue
			}
			if (pieces.length == 2){
				if(pieces[0].equals("STL_FILE")) FileName=pieces[1];	
				if(pieces[0].equals("MACHINE_PERCISION")) MachinePercision = Math.abs(Double.parseDouble(pieces[1]));
				if(pieces[0].equals("PARAMETER_PERCISION")) Percision = Math.min(15, Math.max(0,Integer.parseInt(pieces[1])));
				if(pieces[0].equals("PRINT_FEEDRATE")) PrintFeedrate = Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("ORBITAL_FEEDRATE")) OrbitalFeedrate = Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("BRIDGE_FEEDRATE_MODIFIER")) BridgeFeedrateModifier = Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("FIRST_LAYER_HEIGHT")) FirstLayer = Double.parseDouble(pieces[1]);
				if(pieces[0].equals("ADD_SUPPORT_DEGREE")) SupportDegree = Double.parseDouble(pieces[1])/100.; 
				
				if(pieces[0].equals("SHELL_THICKNESS")) ShellThickness = Integer.parseInt(pieces[1]); 
				if(pieces[0].equals("ADD_SUPPORT")) AddSupport = Boolean.parseBoolean(pieces[1]); 
				if(pieces[0].equals("INFILL")) InFill = Double.parseDouble(pieces[1])/100.;
				if(pieces[0].equals("SCALE")) PreScale=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("X_ROTATE")) XRotate=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("SINK")) Sink=Double.parseDouble(pieces[1]);	
				
			}
			if(pieces.length == 3){
				if(pieces[0].equals("BUILD_PLATFORM")){
					BuildPlatformWidth = Integer.parseInt(pieces[1]);
					BuildPlatformHeight = Integer.parseInt(pieces[2]);
				}
			}
			if(pieces.length == 4){
				if(pieces[0].equals("BACKLASH")){
					BacklashX = Double.parseDouble(pieces[1]); 
					BacklashY = Double.parseDouble(pieces[2]);
					BacklashZ = Double.parseDouble(pieces[3]); 
				}
			}
		}
	}
	
	void Save(){
		PrintWriter output = applet.createWriter("config.txt");
		output.println("[MACHINE]");
		output.printf("STL_FILE %s\n", FileName);
		output.printf("MACHINE_PERCISION %."+Percision+"f\n", MachinePercision); 
		output.printf("PARAMETER_PERCISION %d\n", Percision); 
		output.printf("BUILD_PLATFORM %d %d\n", BuildPlatformWidth, BuildPlatformHeight);
		//output.printf("OPERATING_TEMPERATURE %d\n", OperatingTemp);
		//output.printf("TOOL_TYPE %s\n", Tool.name());
		output.printf("PRINT_FEEDRATE %." + Percision + "f\n", PrintFeedrate);
		output.printf("ORBITAL_FEEDRATE %."+Percision+"f\n", OrbitalFeedrate); 
		output.printf("BRIDGE_FEEDRATE_MODIFIER %."+Percision+"f\n", BridgeFeedrateModifier);
		output.printf("BACKLASH %."+Percision+"f %."+Percision+"f %."+Percision+"f\n", BacklashX, BacklashY, BacklashZ);
		output.printf("SHELL_THICKNESS %d\n", ShellThickness); 
		//output.printf("LAYER_XYEXTRUSION %."+Percision+"4f\n", XYExtrusionThickness); 
		//output.printf("LAYER_THICKNESS %." + Percision + "f\n", LayerThickness);
		output.printf("FIRST_LAYER_HEIGHT %." + Percision + "f\n", FirstLayer); 
		output.printf("ADD_SUPPORT_DEGREE %."+ Percision + "f\n\n", SupportDegree*100); 
		
		output.printf("ADD_SUPPORT %s\n", AddSupport);
		output.printf("INFILL %."+Percision+"f\n", InFill*100);
		output.printf("SCALE %."+ Percision +"f\n", PreScale);
		output.printf("X_ROTATE %."+ Percision +"f\n", XRotate);
		output.printf("SINK %." + Percision + "f\n\n", Sink);
		output.println(extruder.getParameters(Percision));
		output.println(extruder.Filament.getParameters(Percision));
		output.flush();
		output.close();
	}

}


