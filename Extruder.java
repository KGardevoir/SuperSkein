// Extruder Class
import java.lang.Math;

import processing.core.PApplet;


class Extruder {
	public int ToolNum;
	public enum ToolType { Servo, Stepper };
	public ToolType Tool;
	public Material Filament;
	public double ZThickness;
	private double NozzleDiameter;
	public double StepperFlowRate;
	public int ServoFlowRate;
	public double XYThickness; 
	public double RetractDistance; 

	Extruder(int tnum, double lThick, double dThick, double nDiam, String mater, ToolType tool) {
		ToolNum=tnum;
		Tool = tool; 
		Filament=new Material(mater);
		NozzleDiameter=nDiam;
		ZThickness=lThick;
		XYThickness = dThick; 
	}
	Extruder(double lThick, double dThick, double nDiam, String mater, ToolType tool) {
		this(0, lThick, dThick, nDiam, mater, tool);
	}
	Extruder(double lThick, double dThick, double nDiam, String mater){
		this(0, lThick, dThick, nDiam, mater, ToolType.Stepper);
	}
	Extruder(double lThick, double dThick, double nDiam){
		this(lThick, dThick, nDiam, "PLA");
	}
	Extruder(double lThick, double nDiam){//guess the XYThickness
		this(lThick, Math.PI*Math.pow(lThick/2,2)/nDiam, nDiam);
	}
	
	double calcWallWidth() {
		double freespace_area=Math.PI*Math.pow(NozzleDiameter/2,2);
		XYThickness = freespace_area/ZThickness;
		return XYThickness; 
	}
	public int parse(String[] input, int index) {
		for(; index < input.length; index++){
			String[] pieces = PApplet.split(input[index], ' ');
			if(pieces.length == 1){//switch modes
				if(pieces[0].equals("[TOOL]"));
				else if(pieces[0].startsWith("[") && pieces[0].endsWith("]")) return index; 
			}
			if (pieces.length == 2){
				if(pieces[0].equals("TOOL_NUMBER")) ToolNum = Integer.parseInt(pieces[1]);	
				if(pieces[0].equals("TOOL_TYPE")) Tool = ToolType.valueOf(pieces[1]);
				if(pieces[0].equals("LAYER_THICKNESS")) ZThickness = Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("NOZZLE_DIAMETER")) NozzleDiameter = Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("STEPPER_FLOWRATE")) StepperFlowRate = Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("SERVO_FLOWRATE")) ServoFlowRate = Integer.parseInt(pieces[1]); 
				if(pieces[0].equals("EXTRUSION_WIDTH")) XYThickness = Double.parseDouble(pieces[1]); 
				if(pieces[0].equals("RETRACT_DISTANCE")) RetractDistance = Double.parseDouble(pieces[1]); 	
			}
		}
		return index;
	}
	public String getParameters(int perc) {
		String us = "[TOOL]\n"; 
		us += String.format("TOOL_NUMBER %d\n", ToolNum);
		us += String.format("TOOL_TYPE %s\n", Tool.toString());
		us += String.format("LAYER_THICKNESS %."+perc+"f\n", ZThickness);
		us += String.format("NOZZLE_DIAMETER %."+perc+"f\n", NozzleDiameter);
		us += String.format("STEPPER_FLOWRATE %."+perc+"f\n", StepperFlowRate);
		us += String.format("SERVO_FLOWRATE %d\n", ServoFlowRate);
		us += String.format("EXTRUSION_WIDTH %."+perc+"f\n", XYThickness);
		us += String.format("RETRACT_DISTANCE %."+perc+"f\n", XYThickness);
		return us + "\n";
	}
}

