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
	public double LayerThickness, Sink, FirstLayer;
	public int BuildPlatformWidth, BuildPlatformHeight, OperatingTemp; 
	public double StepperFlowRate;
	public int ServoFlowRate;
	public int Percision; 
	private PApplet applet;

	public double RetractDistance;
	public double BacklashX, BacklashY, BacklashZ; 
	public enum ExtruderType { Servo, Stepper };
	ExtruderType Extruder; 
	public int ShellThickness; //TODO add this parameter, applies to both Shell and infill
	public double InFill; //TODO add this parameter
	public double SupportDegree; //TODO 
	public boolean AddSupport; //TODO
	public boolean ShellOnly; //TODO
	//config values of last resort
	Configuration(PApplet app) {
		Percision = 4; 
		applet = app; 
		PreScale = 1.0;
		XRotate = 0;
		FileName="";	
		PrintFeedrate = 1000;
		OrbitalFeedrate = 2000; 
		LayerThickness = 0.27;
		Sink = 0;
		OperatingTemp = 200;
		ServoFlowRate = 180;	
		StepperFlowRate = 200; 
		FirstLayer = LayerThickness/2.; //start slicing in the middle of the layer
		BuildPlatformWidth = 100; 
		BuildPlatformHeight = 100; 
		BacklashX = BacklashY = BacklashZ = 0; 
		RetractDistance = 0;
		Extruder = ExtruderType.Stepper; 
	}

	void Load(){
		String[] input = applet.loadStrings("config.txt");
		int index = 0;
		for(index = 0; index < input.length; index++) {
			String[] pieces = PApplet.split(input[index], ' ');
			if (pieces.length == 2){
				if(pieces[0].equals("SCALE")) PreScale=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("STL_FILE")) FileName=pieces[1];	
				if(pieces[0].equals("X_ROTATE")) XRotate=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("OPERATING_TEMPERATURE")) OperatingTemp=Integer.parseInt(pieces[1]);	
				if(pieces[0].equals("SINK")) Sink=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("PRINT_FEEDRATE")) PrintFeedrate=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("MACHINE_LAYERTHICKNESS")) LayerThickness=Double.parseDouble(pieces[1]);	
				if(pieces[0].equals("FIRST_LAYER_HEIGHT")) FirstLayer=Double.parseDouble(pieces[1]);
				if(pieces[0].equals("PARAMETER_PERCISION")) Percision=Math.min(15, Math.max(0,Integer.parseInt(pieces[1])));
				if(pieces[0].equals("RETRACT_DISTANCE")) RetractDistance=Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("ORBITAL_FEEDRATE")) OrbitalFeedrate=Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("EXTRUDER_TYPE")) Extruder = ExtruderType.valueOf(pieces[1]); 
			}
			if(pieces.length == 3){
				if(pieces[0].equals("FLOWRATE")){
					ServoFlowRate=Integer.parseInt(pieces[1]);	
					StepperFlowRate=Double.parseDouble(pieces[2]); 
				}
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
		output.printf("STL_FILE %s\n", FileName);
		output.printf("PARAMETER_PERCISION %d\n\n", Percision); 
		output.printf("BUILD_PLATFORM %d %d\n", BuildPlatformWidth, BuildPlatformHeight);
		output.printf("BACKLASH %."+Percision+"f %."+Percision+"f %."+Percision+"f\n", BacklashX, BacklashY, BacklashZ);
		output.printf("EXTRUDER_TYPE %s\n", Extruder.name());
		output.printf("SCALE %."+ Percision +"f\n", PreScale);
		output.printf("X_ROTATE %."+ Percision +"f\n\n", XRotate);
		
		output.printf("OPERATING_TEMPERATURE %d\n", OperatingTemp);
		output.printf("FLOWRATE %d %."+Percision+"f\n", ServoFlowRate, StepperFlowRate);
		output.printf("LAYER_THICKNESS %." + Percision + "f\n", LayerThickness);
		output.printf("PRINT_FEEDRATE %." + Percision + "f\n", PrintFeedrate);
		output.printf("ORBITAL_FEEDRATE %."+Percision+"f\n", OrbitalFeedrate); 
		output.printf("FIRST_LAYER_HEIGHT %." + Percision + "f\n", FirstLayer); 
		output.printf("SINK %." + Percision + "f\n", Sink);
		output.flush();
		output.close();
	}

}


