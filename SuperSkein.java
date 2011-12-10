import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import processing.core.*; 

public class SuperSkein extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3965090328975349612L;
	SuperSkein thisapplet = this; 
	public static void main(String args[]) {
		PApplet.main(new String[] { PApplet.ARGS_PRESENT, "SuperSkein" });
	}
	//The config file takes precedence over these parameters!
	
	float PreScale = 1;
	String FileName = "sculpt_dragon.stl";
	float XRotate = 0;
	boolean debugFlag = false;

	String DXFSliceFilePrefix = "dxf_slice";

	// Set DXFExportMode=1 to switch render and enable dependent code.
	int DXFExportMode = 1;
	// Set OpenSCADTestMode=1 to enable OpenSCAD test code.
	int OpenSCADTestMode=0;



	//Display Properties
	float GridSpacing = 10;
	float DisplayScale = 5;


	//End of "easy" modifications you can make...
	//Naturally I encourage everyone to learn and
	//alter the code that follows!

	ArrayList<Slice> Slice;
	Mesh STLFile;
	PrintWriter output;
	double MeshHeight;
	// RawDXF pgDxf;

	//Configuration File Object
	//Hijacks the above variables
	//We'll ditch 'em once this works.
	Configuration MyConfig = new Configuration(this);


	//Thread Objects
	Runnable STLLoad = new STLLoadProc();
	Runnable FileWrite = new FileWriteProc();
	Runnable DXFWrite = new DXFWriteProc();
	Thread DXFWriteThread;
	Thread FileWriteThread, STLLoadThread;
	boolean DXFWriteTrigger = false;
	boolean FileWriteTrigger = false;
	boolean STLLoadTrigger = false;
	double DXFWriteFraction = 0;
	double FileWriteFraction = 0;
	double STLLoadFraction = 0;
	//Flags
	boolean STLLoadedFlag = false;
	boolean FileWrittenFlag = false;


	int AppWidth = (int)(MyConfig.BuildPlatformWidth*DisplayScale);
	int AppHeight = (int)(MyConfig.BuildPlatformHeight*DisplayScale);

	//GUI Page Select
	int GUIPage = 0;

	//Page 0 GUI Widgets
	GUI STLLoadButton, FileWriteButton, DXFWriteButton;
	GUIProgressBar STLLoadProgress, FileWriteProgress, DXFWriteProgress;
	GUITextBox STLName;
	GUIFloatBox STLScale, STLXRotate;
	GUICheckBox useRealLayers; 
	//Page 1 GUI Widgets
	VScrollbar layerHeight; 
	//AllPage GUI Widgets
	GUI RightButton, LeftButton;


	public void setup(){
//		if(DXFExportMode != 0) size(AppWidth,AppHeight,P3D);
//		if(DXFExportMode == 0) size(AppWidth,AppHeight,JAVA2D);
		STLLoadButton = new GUI(thisapplet,10,125,100,15, "Load STL");
		STLLoadProgress = new GUIProgressBar(thisapplet, 120,125,AppWidth-130,15);
		
		FileWriteButton = new GUI(thisapplet, 10,150,100,15, "Write GCode");
		FileWriteProgress = new GUIProgressBar(thisapplet, 120,150,AppWidth-130,15);
		
		DXFWriteButton = new GUI(thisapplet, 10,175,100,15, "Write DXF Slices");
		DXFWriteProgress = new GUIProgressBar(thisapplet, 120,175,AppWidth-130,15);
		
		STLName = new GUITextBox(thisapplet, 120,25,AppWidth-130,15,"sculpt_dragon.stl");
		STLScale = new GUIFloatBox(thisapplet, 120,50,100,15, "1.0");
		STLXRotate = new GUIFloatBox(thisapplet, AppWidth-130,50,100,15, "0.0");
		
		layerHeight = new VScrollbar(thisapplet, 0, 0, 10, AppHeight, 10); 
		RightButton = new GUI(thisapplet, AppWidth-90,AppHeight-20,80,15, "Right");
		LeftButton = new GUI(thisapplet, 10,AppHeight-20,80,15, "Left");
		useRealLayers = new GUICheckBox(thisapplet, 10, 200, true, "Use Real Slices?");
		size(AppWidth,AppHeight,P2D);

		Slice = new ArrayList<Slice>();
		
		if(DXFExportMode != 0) DXFWriteThread = new Thread(DXFWrite);
		if(DXFExportMode != 0) DXFWriteThread.start();
		FileWriteThread = new Thread(FileWrite);
		FileWriteThread.start();
		STLLoadThread = new Thread(STLLoad);
		STLLoadThread.start();

		//For initialization
		//~config.txt
		MyConfig.Load();
		STLScale.setFloat((float) MyConfig.PreScale);
		STLName.Text = MyConfig.FileName;
		STLXRotate.setFloat((float) MyConfig.XRotate);
		noLoop();
	}

	//This executes on Exit
	//Autosave for ~config.txt
	public void stop(){
		if(STLScale.Valid) MyConfig.PreScale = STLScale.getFloat();
		if(STLXRotate.Valid) MyConfig.XRotate = STLXRotate.getFloat();
		MyConfig.FileName = STLName.Text;
		
		MyConfig.Save();
		super.stop();
	}

	public void draw(){
		background(0);
		stroke(0);
		strokeWeight(2);
		PFont font;
		font = loadFont("data/ArialMT-12.vlw");
		

		//GUI Pages
		RightButton.x = this.width-90; 
		RightButton.y = this.height-20; 
		LeftButton.y = this.height-20;
		//Interface Page
		if(GUIPage == 0){
			STLLoadProgress.w = this.width - 130; 
			FileWriteProgress.w = this.width - 130; 
			DXFWriteProgress.w = this.width - 130;  

			STLName.w = this.width - 130;
			STLXRotate.x = this.width - 130; 
			
			textAlign(CENTER);
			textFont(font);
			textMode(SCREEN);
			fill(255);
			text("GCODE Write",width/2,15);
			
			textAlign(LEFT);
			text("STL File Name",10,37);
			STLName.display();
			text("Scale Factor",10,62);
			STLScale.display();
			
			text("X-Rotation",this.width - 200,62);
			STLXRotate.display();

			useRealLayers.display(); 
			if(DXFExportMode != 0) DXFWriteProgress.update(DXFWriteFraction);
			if(DXFExportMode != 0) DXFWriteButton.display();
			if(DXFExportMode != 0) DXFWriteProgress.display();
			FileWriteProgress.update(FileWriteFraction);
			FileWriteButton.display();
			FileWriteProgress.display();
			STLLoadProgress.update(STLLoadFraction);
			STLLoadButton.display();
			STLLoadProgress.display();
		}

		//MeshMRI
		//Only relates to the final gcode in that
		//it shows you 2D sections of the mesh.
		if(GUIPage == 1){
			textFont(font);
			textMode(SCREEN);
			fill(255);
			textAlign(RIGHT); 
			if(useRealLayers.checked) text("Slice Number: " + (int)Math.floor((1.-layerHeight.getPos())*(Slice.size()-1)), width, 15);
			else text("Z-Height: " +  String.format("%."+MyConfig.Percision+"f", MeshHeight*(1.-layerHeight.getPos())+STLFile.bz1) + " mm", width, 15);
			DisplayScale = Math.min(this.width/MyConfig.BuildPlatformWidth, this.height/MyConfig.BuildPlatformHeight);
			layerHeight.setScrollHeight(this.height);
			
			textAlign(CENTER);
			fill(255); 
			
			if(useRealLayers.checked) text("SliceMRI",width/2,15);
			else text("MeshMRI", width/2, 15); 

			//Slice = new ArrayList<Slice>();
			//Draw the grid
			stroke(80);
			strokeWeight(1);
			for(float px = 0; px<(MyConfig.BuildPlatformWidth*DisplayScale+1);px=px+GridSpacing*DisplayScale) line(px,0,px,MyConfig.BuildPlatformHeight*DisplayScale);
			for(float py = 0; py<(MyConfig.BuildPlatformHeight*DisplayScale+1);py=py+GridSpacing*DisplayScale) line(0,py,MyConfig.BuildPlatformWidth*DisplayScale,py);
			 
			if(STLLoadedFlag){
				stroke(255);
				strokeWeight(2);
				if(!useRealLayers.checked){
					SSLine Intersection;
					for(int i = 0; i < STLFile.Triangles.size(); i++){
						Triangle tri = STLFile.Triangles.get(i);
						if((Intersection = tri.GetZIntersect(MyConfig, MeshHeight*(1.-layerHeight.getPos())+STLFile.bz1)) != null){
							Intersection.Scale(DisplayScale);
							Intersection.Rotate(PI);
							Intersection.Translate(MyConfig.BuildPlatformWidth*DisplayScale/2, MyConfig.BuildPlatformHeight*DisplayScale/2);				
							line(Intersection.x1,Intersection.y1,Intersection.x2,Intersection.y2);
						}
					}//slice the mesh correctly and draw profile
				} else {//XXX use real slices
					redraw(); 
					SSPath cslice = Slice.get(Math.min(Slice.size()-1, Math.max(0, (int)Math.floor((1.-layerHeight.getPos())*(Slice.size()-1))))).slice.flatten(MyConfig); 
					PathIterator iter = cslice.getPathIterator(new AffineTransform()); 
					pushMatrix(); 
					translate(MyConfig.BuildPlatformWidth/2*DisplayScale, MyConfig.BuildPlatformHeight/2*DisplayScale);
					scale(DisplayScale);
					rotate((float) Math.PI);
					fill(204, 102, 0);
					noFill(); 
					double[][] p = new double[3][6]; double[][] fp = new double[3][2]; 
					
					for(; !iter.isDone(); iter.next()){
						int type = iter.currentSegment(p[2]);
						if(type == PathIterator.SEG_MOVETO){//TODO handle if path is not a complete object with n>3 vertexes
							iter.currentSegment(p[0]); 
							iter.next(); 
							iter.currentSegment(p[1]); //we have to flush the other two points...
							iter.next(); 
							iter.currentSegment(p[2]); 
							fp[0][0] = p[0][0]; 
							fp[0][1] = p[0][1];
							fp[1][0] = p[1][0]; 
							fp[1][1] = p[1][1]; 
							line(p[0][0], p[0][1], p[1][0], p[1][1]);
							ellipse((float)p[0][0], (float)p[0][1],1, 1);
							ellipse((float)p[1][0], (float)p[1][1],1, 1);
							ellipse((float)p[0][0], (float)p[0][1], 2, 2); 
						} else if(type == PathIterator.SEG_CLOSE) {
							p[2][0] = fp[0][0]; 
							p[2][1] = fp[0][1]; 
							line(p[1][0], p[1][1], p[2][0], p[2][1]); 
							ellipse((float)p[1][0], (float)p[1][1],1, 1);
							p[0][0] = fp[1][0]; 
							p[0][1] = fp[1][1]; 
							continue; 
						}
						line(p[1][0], p[1][1], p[2][0], p[2][1]); 

						ellipse((float)p[1][0], (float)p[1][1],1, 1);
						p[0][0] = p[1][0]; //shift parameters down
						p[0][1] = p[1][1]; 
						p[1][0] = p[2][0]; 
						p[1][1] = p[2][1]; 
					}
					popMatrix(); 
				}
			} else text("STL File Not Loaded",width/2,height/2);
			strokeWeight(1);
			layerHeight.display(mousePressed); 
		}
		if( GUIPage != 2 ) {
			//Always On Top, so last in order
			LeftButton.display();
			RightButton.display();
		}
	}
	private void line(double x1, double y1, double x2, double y2) {
		line((float)x1,(float)y1,(float)x2,(float)y2);
	}

	//Save file on click
	public void mousePressed(){
		if(GUIPage == 0){
			if( (DXFExportMode != 0) && DXFWriteButton.over(mouseX,mouseY)) DXFWriteTrigger = true;
			if( FileWriteButton.over(mouseX,mouseY)) FileWriteTrigger = true;
			if( STLLoadButton.over(mouseX,mouseY)) STLLoadTrigger = true;
			if( useRealLayers.over(mouseX, mouseY)) useRealLayers.toggle(); 
			STLName.checkFocus(mouseX,mouseY);
			STLScale.checkFocus(mouseX,mouseY);
			STLXRotate.checkFocus(mouseX,mouseY);
		}

		//if(GUIPage == 1) layerHeight.update(mouseX, mouseY, true); 
		
		if( LeftButton.over(mouseX,mouseY) ) GUIPage--;
		if( RightButton.over(mouseX,mouseY) ) GUIPage++;
		if( GUIPage == 2) GUIPage=0;
		if( GUIPage == -1) GUIPage=1;
		redraw();
	}

	public void mouseDragged(){
		if(GUIPage == 1 && mouseButton == LEFT){
			layerHeight.update(mouseX, mouseY, true); 
			redraw(); 
		}
	}

	public void mouseMoved(){
		 redraw();
	}

	public void mouseReleased(){
		redraw(); 
	}


	public void keyTyped(){
		if(GUIPage == 0){
			STLName.doKeystroke(key);
			STLScale.doKeystroke(key);
			STLXRotate.doKeystroke(key);
		}
		redraw();
	}

	class STLLoadProc implements Runnable {
		public void run(){
			while(true){
				while(!STLLoadTrigger)delay(300);
				STLLoadTrigger = false;
				STLLoadFraction = 0;
				STLLoadProgress.message("STL Load May Take a Minute or more...");
				String newName=selectInput("Select STL to Load");
				
				if(newName != null){
					STLName.Text=newName;
					STLFile = new Mesh(thisapplet, STLName.Text);

					//Scale and locate the mesh
					//These will do nothing if these methods return NaN
					STLFile.Scale(STLScale.getFloat());
					STLFile.RotateX(STLXRotate.getFloat()*180/PI);
					//Put the mesh in the middle of the platform:
					STLFile.Translate(-STLFile.bx1,-STLFile.by1,-STLFile.bz1);
					STLFile.Translate(-STLFile.bx2/2, -STLFile.by2/2, 0);
					//STLFile.Translate(0,0,-MyConfig.LayerThickness);	
					STLFile.Translate(0,0,-MyConfig.Sink);
					MeshHeight = STLFile.bz2-STLFile.bz1;
					STLLoadedFlag = true;
					//XXX now slice mesh so the preview means more (and are tremendously more useful). 
					Slice.clear(); 
					for(double ZLevel = MyConfig.FirstLayer;ZLevel < STLFile.bz2; ZLevel+=MyConfig.extruder.ZThickness){//slice at the center of the layer
						//int SliceNum = (int) Math.floor((ZLevel-MyConfig.FirstLayer)/MyConfig.extruder.ZThickness);
						//SSArea thisArea = new SSArea(MyConfig);
						if(debugFlag) println("\n	GridScale: "+MyConfig.MachinePercision);
						//thisArea.Slice2Area(ThisSlice);
						Slice.add(new Slice(thisapplet, MyConfig, STLFile, ZLevel));
						STLLoadFraction = .5 + ((ZLevel-MyConfig.FirstLayer)/(STLFile.bz2-MyConfig.FirstLayer))/2.; 
						redraw(); 
					}
					STLLoadFraction = 1.1;
					System.out.println("MeshHeight = " + MeshHeight);
					System.out.println("Number of Slices: " + (int)Math.floor((MeshHeight-MyConfig.FirstLayer)/MyConfig.extruder.ZThickness));
					redraw();
				} else println("No STL File selected. Aborting");
				
				

			}
		}
	}


	class FileWriteProc implements Runnable{
		public void run(){
			while(true){
				while(!FileWriteTrigger)delay(300);
				String GCodeFileName = selectOutput("Save G-Code to This File");
				if(GCodeFileName == null) {
					println("No file was selected; using STL File as G-Code file prefix.");
					GCodeFileName=STLName.Text+".gcode";
				}

				FileWriteTrigger=false;//Only do this once per command.
				FileWriteFraction=(float) 0.1;
				redraw();

				ArrayList<SSArea> SliceAreaList = new ArrayList<SSArea>();
				for(double ZLevel = MyConfig.FirstLayer;ZLevel < STLFile.bz2; ZLevel+=MyConfig.extruder.ZThickness){//slice at the center of the layer
					Slice ThisSlice;
					ThisSlice = new Slice(thisapplet, MyConfig, STLFile, ZLevel);
					int SliceNum = (int) Math.floor(ZLevel / MyConfig.extruder.ZThickness);
					SSArea thisArea = new SSArea(MyConfig);
					if(debugFlag) println("\n	GridScale: "+MyConfig.MachinePercision);
					thisArea.Slice2Area(ThisSlice);
					SliceAreaList.add(SliceNum, thisArea);
					FileWriteFraction = ((ZLevel-MyConfig.FirstLayer)/(STLFile.bz2-MyConfig.FirstLayer))/10.; 
					redraw(); 
				}
				FileWriteFraction= 0.2;
				redraw();
				ArrayList<SSArea> ShellAreaList = new ArrayList<SSArea>();
				for(int ShellNum=0; ShellNum < SliceAreaList.size(); ShellNum++) {
					SSArea thisArea = SliceAreaList.get(ShellNum);
					SSArea thisShell = new SSArea(MyConfig);
					thisShell.add(thisArea);
					thisShell.makeShell(MyConfig.ShellThickness,8);
					SSArea thisSubArea = new SSArea(MyConfig);
					thisSubArea.add(thisArea);
					thisSubArea.subtract(thisShell);
					ShellAreaList.add(ShellNum,thisSubArea);
				}
				FileWriteFraction=(float) 0.3;
				redraw();
				Fill areaFill=new Fill(MyConfig, true,(int)Math.floor(MyConfig.BuildPlatformWidth),(int)Math.floor(MyConfig.BuildPlatformHeight),0.2);
				ArrayList<SSArea> FillAreaList = areaFill.GenerateFill(ShellAreaList);

				FileWriteFraction=(float) 0.5;
				redraw();
				AreaWriter gcodeOut = new AreaWriter(thisapplet, debugFlag,(int)Math.floor(MyConfig.BuildPlatformWidth),(int)Math.floor(MyConfig.BuildPlatformHeight));
				gcodeOut.setOperatingTemp(MyConfig.extruder.Filament.getExtrudeTemp());
				gcodeOut.setFlowRate(MyConfig.extruder.ServoFlowRate);
				gcodeOut.setLayerThickness(MyConfig.extruder.ZThickness);
				gcodeOut.setPrintHeadSpeed(MyConfig.PrintFeedrate);
				FileWriteFraction=(float) 0.7;
				redraw();

				gcodeOut.ArrayList2GCode(GCodeFileName,SliceAreaList,ShellAreaList,FillAreaList);

				FileWriteFraction=1.5;
				System.out.print("\nFinished Slicing!	Bounding Box is:\n");
				System.out.printf("X: %.4f - %.4f + 	 ", STLFile.bx1, STLFile.bx2);
				System.out.printf("Y: %.4f - %.4f + 	 ", STLFile.by1, STLFile.by2);
				System.out.printf("Z: %.4f - %.4f + 	 ", STLFile.bz1, STLFile.bz2);
				if(STLFile.bz1<0) System.out.print("\n(Values below z=0 not exported.)");

				MeshHeight=STLFile.bz2-STLFile.bz1;
				STLLoadedFlag = true;
				redraw();
			}
		}
	}


	class DXFWriteProc implements Runnable{
		public void run(){
			while(true){
				while(!DXFWriteTrigger)delay(300);
				DXFWriteTrigger=false;//Only do this once per command.
				// GUIPage=2;
				DXFWriteFraction=0.1;
				redraw();
				
				String DXFSliceFilePrefix = selectOutput("Save Results to This File Path and Prefix");
				if(DXFSliceFilePrefix == null) {
					println("No file was selected; using STL File location as path+prefix.");
					DXFSliceFilePrefix=STLName.Text;
				}
				String DXFSliceFileName;
				String DXFShellFileName;
				String DXFFillFileName;
				// int DXFSliceNum;
				
				String OpenSCADFileName = DXFSliceFilePrefix + "_" + MyConfig.extruder.ZThickness + ".scad";
				
				output = createWriter(OpenSCADFileName);
				output.println("// OpenSCAD Wrapper for sliced "+STLName.Text+" DXF.\n");
				output.println("layerThickness="+MyConfig.extruder.ZThickness+";");
				output.println("layerHeight="+MyConfig.extruder.ZThickness+"/2;");
				output.printf("minX=%.4f;\nmaxX=%.4f;\n", STLFile.bx1, STLFile.bx2);
				output.printf("minY=%.4f;\nmaxY=%.4f;\n", STLFile.by1, STLFile.by2);
				output.printf("minZ=%.4f;\nmaxZ=%.4f;\n", STLFile.bz1, STLFile.bz2);
				output.println("render_select=0; // render single slice");
				output.println("// render_select=1; // render all slices");
				output.println("\nmodule dxf_slice(index=0) {");
				
				// ArrayList PolyArray;
				//int renderWidth=width, renderHeight=height;

				DXFWriteFraction=0.2;
				redraw();
				ArrayList<SSArea> SliceAreaList = new ArrayList<SSArea>();
				int SliceNum;
				for(double ZLevel = MyConfig.FirstLayer;ZLevel<(STLFile.bz2-MyConfig.FirstLayer);ZLevel=ZLevel+MyConfig.extruder.ZThickness){
					Slice ThisSlice;
					ThisSlice = new Slice(thisapplet, MyConfig, STLFile,ZLevel);
					//will abort if there is an error
					SliceNum = (int)Math.floor(ZLevel / MyConfig.extruder.ZThickness);
					SSArea thisArea = new SSArea(MyConfig);
					if(debugFlag) println("\n	GridScale: "+MyConfig.MachinePercision);
					thisArea.Slice2Area(ThisSlice);
					SliceAreaList.add(SliceNum, thisArea);
				}

				DXFWriteFraction=0.3;
				redraw();
				ArrayList<SSArea> ShellAreaList = new ArrayList<SSArea>();
				for(int ShellNum=0; ShellNum < SliceAreaList.size(); ShellNum++) {
					SSArea thisArea = (SSArea) SliceAreaList.get(ShellNum);
					SSArea thisShell = new SSArea(MyConfig);
					thisShell.add(thisArea);
					thisShell.makeShell(MyConfig.ShellThickness,8);
					if(ShellNum>0) {
						SSArea bridgeCheck = new SSArea(MyConfig);
						bridgeCheck.add(thisArea);
						bridgeCheck.subtract( (SSArea) SliceAreaList.get(ShellNum-1));
						if(!bridgeCheck.isEmpty()){
							println("	Bridges found in "+ShellNum);
							// bridgeCheck.makeShell(0.25,8);
							bridgeCheck.intersect(thisArea);
							// thisShell.add(bridgeCheck);
						}
					}
					ShellAreaList.add(ShellNum,thisShell);
				}

				DXFWriteFraction=0.4;
				redraw();
				Fill areaFill = new Fill(MyConfig, true,(int)Math.floor(MyConfig.BuildPlatformWidth),(int)Math.floor(MyConfig.BuildPlatformHeight),0.2);
				ArrayList<SSArea> FillAreaList = areaFill.GenerateFill(SliceAreaList);

				DXFWriteFraction=0.5;
				redraw();
				DXFSliceFileName = DXFSliceFilePrefix + "_slices_" + MyConfig.extruder.ZThickness + ".dxf";
				print("DXF Slice File Name: " + DXFSliceFileName + "\n");
				AreaWriter dxfOut = new AreaWriter(thisapplet, false,(int)Math.floor(MyConfig.BuildPlatformWidth),(int)Math.floor(MyConfig.BuildPlatformHeight));
				dxfOut.ArrayList2DXF(DXFSliceFileName,SliceAreaList);

				DXFWriteFraction=(float) 0.6;
				redraw();
				DXFShellFileName = DXFSliceFilePrefix + "_shells_" + MyConfig.extruder.ZThickness + ".dxf";
				print("DXF Shell File Name: " + DXFShellFileName + "\n");
				dxfOut.ArrayList2DXF(DXFShellFileName,ShellAreaList);

				DXFWriteFraction=(float) 0.7;
				redraw();
				DXFFillFileName = DXFSliceFilePrefix + "_fill_" + MyConfig.extruder.ZThickness + ".dxf";
				print("DXF Fill File Name: " + DXFFillFileName + "\n");
				dxfOut.ArrayList2DXF(DXFFillFileName,FillAreaList);

				DXFWriteFraction=(float) 0.8;
				redraw();
				for(int DXFSliceNum=0;DXFSliceNum<SliceAreaList.size();DXFSliceNum++) {
					output.println(" if(index>="+DXFSliceNum+"&&index<(1+"+DXFSliceNum+")) {");
					output.println("	echo(\"	Instantiating slice "+DXFSliceNum+".\");");
					output.println("	import_dxf(file=\"" + DXFSliceFileName + "\", layer=\""+DXFSliceNum+"\");\n" );
					output.println(" }");
				}
				DXFWriteFraction=(float) 0.9;
				redraw();
				output.println(" if(index>="+SliceAreaList.size()+") {");
				output.println("	echo(\"ERROR: Out of index bounds.\");");
				output.println(" }");
				output.println("}");
				output.println("function get_dxf_slice_count() = "+SliceAreaList.size()+";\n");
				output.println("render_slice=(get_dxf_slice_count()-1)*$t; // Use OpenSCAD Animation to step thru slices.\n");
				output.println("if(render_select==0) {");
				output.println("	dxf_slice(index=render_slice);");
				output.println("}\n");
				output.println("if(render_select==1) {");
				output.println("	for( i=[0:get_dxf_slice_count()-1] ) {");
				output.println("		translate([0,0,i*layerThickness])");
				output.println("			dxf_slice(index=i);");
				output.println("	}");
				output.println("}\n");

				output.flush();
				output.close();
				
				GUIPage=0;
				DXFWriteFraction=(float) 1.5;
				print("Finished Slicing!	Bounding Box is:\n");
				System.out.printf("X: %.4f - %.4f\t",STLFile.bx1,STLFile.bx2);
				System.out.printf("Y: %.4f - %.4f\t",STLFile.by1,STLFile.by2);
				System.out.printf("Z: %.4f - %.4f\t",STLFile.bz1,STLFile.bz2);
				if(STLFile.bz1<0) print("\n(Values below z=0 not exported.)");

				if(OpenSCADTestMode==1) {
					OpenSCAD runOSCAD = new OpenSCAD();
					runOSCAD.setInput(OpenSCADFileName);
					runOSCAD.setOutput(DXF,OpenSCADFileName+".dxf");
					print("Running OpenSCAD Process:\n");
					print("	 Exec Path: "+runOSCAD.getExecPath()+"\n");
					print("	 Exec Args: "+runOSCAD.getExecArgs()+"\n");
					print("	Input File: "+runOSCAD.getInput()+"\n");
					if(runOSCAD.run()) {
						print("Run Finished!\n");
					} else {
						print("Run Error.\n");
					}
					// open(OpenSCADFileName);
				}

				MeshHeight= STLFile.bz2-STLFile.bz1;
				STLLoadedFlag = true;
				redraw();
			}
		}
		
	}

}
