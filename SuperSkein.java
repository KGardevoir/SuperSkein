import java.io.PrintWriter;
import java.util.ArrayList;
import processing.core.*; 

public class SuperSkein extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3965090328975349612L;
	SuperSkein thisapplet = this; 
	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "SuperSkein" });
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

	//Page 1 GUI Widgets
	VScrollbar layerHeight; 
	//AllPage GUI Widgets
	GUI RightButton, LeftButton;


	public void setup(){
//		if(DXFExportMode != 0) size(AppWidth,AppHeight,P3D);
//		if(DXFExportMode == 0) size(AppWidth,AppHeight,JAVA2D);
		STLLoadButton = new GUI(thisapplet,10,125,100,15, "Load STL");
		STLLoadProgress = new GUIProgressBar(thisapplet, 120,125,370,15);
		
		FileWriteButton = new GUI(thisapplet, 10,150,100,15, "Write GCode");
		FileWriteProgress = new GUIProgressBar(thisapplet, 120,150,370,15);
		
		DXFWriteButton = new GUI(thisapplet, 10,175,100,15, "Write DXF Slices");
		DXFWriteProgress = new GUIProgressBar(thisapplet, 120,175,370,15);
		
		STLName = new GUITextBox(thisapplet, 120,25,370,15,"sculpt_dragon.stl");
		STLScale = new GUIFloatBox(thisapplet, 120,50,100,15, "1.0");
		STLXRotate = new GUIFloatBox(thisapplet, 390,50,100,15, "0.0");
		
		layerHeight = new VScrollbar(thisapplet, 0, 0, 10, AppHeight, 10); 
		RightButton = new GUI(thisapplet, AppWidth-90,AppHeight-20,80,15, "Right");
		LeftButton = new GUI(thisapplet, 10,AppHeight-20,80,15, "Left");
		
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

		//Interface Page
		if(GUIPage == 0){
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
			
			text("X-Rotation",300,62);
			STLXRotate.display();

			
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
		if(GUIPage==1){
			layerHeight.display(mousePressed); 
			textAlign(CENTER);
			textFont(font);
			textMode(SCREEN);
			fill(255);
			text("MeshMRI",width/2,15);

			SSLine Intersection;
			//Slice = new ArrayList<Slice>();
			//Draw the grid
			stroke(80);
			strokeWeight(1);
			for(float px = 0; px<(MyConfig.BuildPlatformWidth*DisplayScale+1);px=px+GridSpacing*DisplayScale) line(px,0,px,MyConfig.BuildPlatformHeight*DisplayScale);
			for(float py = 0; py<(MyConfig.BuildPlatformHeight*DisplayScale+1);py=py+GridSpacing*DisplayScale) line(0,py,MyConfig.BuildPlatformWidth*DisplayScale,py);
			 
			if(STLLoadedFlag){
				stroke(255);
				strokeWeight(2);
				//double scale = Math.min(BuildPlatformWidth/(STLFile.bx2-STLFile.bx1),BuildPlatformHeight/(STLFile.by2-STLFile.by1));
				for(int i = 0; i <STLFile.Triangles.size(); i++){
					Triangle tri = (Triangle) STLFile.Triangles.get(i);
					if((Intersection = tri.GetZIntersect(MeshHeight*(1.-layerHeight.getPos())+STLFile.bz1)) != null){
						Intersection.Scale(DisplayScale);
						Intersection.Rotate(PI);
						Intersection.Translate(MyConfig.BuildPlatformWidth*DisplayScale/2, MyConfig.BuildPlatformHeight*DisplayScale/2);				
						line(Intersection.x1,Intersection.y1,Intersection.x2,Intersection.y2);
					}
				}//slice the mesh correctly and draw profile
			} else text("STL File Not Loaded",width/2,height/2);
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
		if( (DXFExportMode != 0) && ((DXFWriteButton.over(mouseX,mouseY)) & GUIPage == 0) ) DXFWriteTrigger = true;
		if( (FileWriteButton.over(mouseX,mouseY)) & GUIPage==0) FileWriteTrigger = true;
		if( (STLLoadButton.over(mouseX,mouseY)) & GUIPage==0) STLLoadTrigger = true;
		if(GUIPage == 0) STLName.checkFocus(mouseX,mouseY);
		if(GUIPage == 0) STLScale.checkFocus(mouseX,mouseY);
		if(GUIPage == 0) STLXRotate.checkFocus(mouseX,mouseY);
		
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
		if(GUIPage == 1) redraw();
	}

	public void mouseReleased(){
		if(GUIPage == 1) redraw(); 
	}


	public void keyTyped(){
		if(GUIPage == 0) STLName.doKeystroke(key);
		if(GUIPage == 0) STLScale.doKeystroke(key);
		if(GUIPage == 0) STLXRotate.doKeystroke(key);	
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
				
				if(newName!=null) STLName.Text=newName;
				else { 
					println("No STL File selected. Aborting");
				}
				
				STLFile = new Mesh(thisapplet, STLName.Text);

				//Scale and locate the mesh
				//These will do nothing if these methods return NaN
				STLFile.Scale(STLScale.getFloat());
				STLFile.RotateX(STLXRotate.getFloat()*180/PI);
				//Put the mesh in the middle of the platform:
				STLFile.Translate(-STLFile.bx1,-STLFile.by1,-STLFile.bz1);
				STLFile.Translate(-STLFile.bx2/2,-STLFile.by2/2,0);
				//STLFile.Translate(0,0,-MyConfig.LayerThickness);	
				STLFile.Translate(0,0,-MyConfig.Sink);
				MeshHeight = STLFile.bz2-STLFile.bz1;
				STLLoadFraction = 1.1;
				STLLoadedFlag = true;
				System.out.println("MeshHeight = STLFile.bz2-STLFile.bz1: " +STLFile.bz2 + "-" + STLFile.bz1 + "="+ MeshHeight); 
				redraw();
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
				for(double ZLevel = MyConfig.FirstLayer;ZLevel<(STLFile.bz2-MyConfig.FirstLayer);ZLevel+=MyConfig.LayerThickness){//slice at the center of the layer
					Slice ThisSlice;
					ThisSlice = new Slice(thisapplet, STLFile, ZLevel);
					int SliceNum = (int) Math.round(ZLevel / MyConfig.LayerThickness);
					SSArea thisArea = new SSArea();
					thisArea.setGridScale(0.01);
					if(debugFlag) println("\n	GridScale: "+thisArea.GridScale);
					thisArea.Slice2Area(ThisSlice);
					SliceAreaList.add(SliceNum, thisArea);
				}
				FileWriteFraction=(float) 0.2;
				redraw();
				ArrayList<SSArea> ShellAreaList = new ArrayList<SSArea>();
				for(int ShellNum=0; ShellNum < SliceAreaList.size(); ShellNum++) {
					SSArea thisArea = (SSArea) SliceAreaList.get(ShellNum);
					SSArea thisShell = new SSArea();
					thisShell.setGridScale(thisArea.getGridScale());
					thisShell.add(thisArea);
					thisShell.makeShell(0.25,8);
					SSArea thisSubArea = new SSArea();
					thisSubArea.setGridScale(thisArea.getGridScale());
					thisSubArea.add(thisArea);
					thisSubArea.subtract(thisShell);
					ShellAreaList.add(ShellNum,thisSubArea);
				}
				FileWriteFraction=(float) 0.3;
				redraw();
				Fill areaFill=new Fill(true,(int)Math.round(MyConfig.BuildPlatformWidth),(int)Math.round(MyConfig.BuildPlatformHeight),0.2);
				ArrayList<SSArea> FillAreaList = areaFill.GenerateFill(ShellAreaList);

				FileWriteFraction=(float) 0.5;
				redraw();
				AreaWriter gcodeOut = new AreaWriter(thisapplet, debugFlag,(int)Math.round(MyConfig.BuildPlatformWidth),(int)Math.round(MyConfig.BuildPlatformHeight));
				gcodeOut.setOperatingTemp(MyConfig.OperatingTemp);
				gcodeOut.setFlowRate(MyConfig.ServoFlowRate);
				gcodeOut.setLayerThickness(MyConfig.LayerThickness);
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
				
				String OpenSCADFileName = DXFSliceFilePrefix + "_" + MyConfig.LayerThickness + ".scad";
				
				output = createWriter(OpenSCADFileName);
				output.println("// OpenSCAD Wrapper for sliced "+STLName.Text+" DXF.\n");
				output.println("layerThickness="+MyConfig.LayerThickness+";");
				output.println("layerHeight="+MyConfig.LayerThickness+"/2;");
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
				for(double ZLevel = MyConfig.FirstLayer;ZLevel<(STLFile.bz2-MyConfig.FirstLayer);ZLevel=ZLevel+MyConfig.LayerThickness){
					Slice ThisSlice;
					ThisSlice = new Slice(thisapplet, STLFile,ZLevel);
					//will abort if there is an error
					SliceNum = (int)Math.round(ZLevel / MyConfig.LayerThickness);
					SSArea thisArea = new SSArea();
					thisArea.setGridScale(0.01);
					if(debugFlag) println("\n	GridScale: "+thisArea.GridScale);
					thisArea.Slice2Area(ThisSlice);
					SliceAreaList.add(SliceNum, thisArea);
				}

				DXFWriteFraction=0.3;
				redraw();
				ArrayList<SSArea> ShellAreaList = new ArrayList<SSArea>();
				for(int ShellNum=0; ShellNum < SliceAreaList.size(); ShellNum++) {
					SSArea thisArea = (SSArea) SliceAreaList.get(ShellNum);
					SSArea thisShell = new SSArea();
					thisShell.setGridScale(thisArea.getGridScale());
					thisShell.add(thisArea);
					thisShell.makeShell(0.25,8);
					if(ShellNum>0) {
						SSArea bridgeCheck = new SSArea();
						bridgeCheck.setGridScale(thisArea.getGridScale());
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
				Fill areaFill = new Fill(true,Math.round(MyConfig.BuildPlatformWidth),Math.round(MyConfig.BuildPlatformHeight),0.2);
				ArrayList<SSArea> FillAreaList = areaFill.GenerateFill(SliceAreaList);

				DXFWriteFraction=0.5;
				redraw();
				DXFSliceFileName = DXFSliceFilePrefix + "_slices_" + MyConfig.LayerThickness + ".dxf";
				print("DXF Slice File Name: " + DXFSliceFileName + "\n");
				AreaWriter dxfOut = new AreaWriter(thisapplet, false,Math.round(MyConfig.BuildPlatformWidth),Math.round(MyConfig.BuildPlatformHeight));
				dxfOut.ArrayList2DXF(DXFSliceFileName,SliceAreaList);

				DXFWriteFraction=(float) 0.6;
				redraw();
				DXFShellFileName = DXFSliceFilePrefix + "_shells_" + MyConfig.LayerThickness + ".dxf";
				print("DXF Shell File Name: " + DXFShellFileName + "\n");
				dxfOut.ArrayList2DXF(DXFShellFileName,ShellAreaList);

				DXFWriteFraction=(float) 0.7;
				redraw();
				DXFFillFileName = DXFSliceFilePrefix + "_fill_" + MyConfig.LayerThickness + ".dxf";
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

				MeshHeight=(float) (STLFile.bz2-STLFile.bz1);
				STLLoadedFlag = true;
				redraw();
			}
		}
		
	}

}
