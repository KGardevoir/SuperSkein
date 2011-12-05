import java.io.BufferedReader;
import java.util.ArrayList;
import processing.core.PApplet;

// Mesh Class

class Mesh {
	ArrayList<Triangle> Triangles;
	double bx1,by1,bz1,bx2,by2,bz2; //bounding box
	boolean Valid;
	double Sink;

	PApplet applet; 
	//Mesh Loading routine
	Mesh(PApplet app, String FileName){
		Valid = false;
		applet = app; 
		System.out.println ("loading file " + FileName + "...");
		Triangles = new ArrayList<Triangle>();
		Sink=0;

		if (LoadTextMesh (FileName)){
			Valid = true;
			CalculateBoundingBox();
		} else if (LoadBinaryMesh (FileName)){
			Valid = true;
			CalculateBoundingBox();
		}
	}

	private boolean LoadBinaryMesh(String FileName){
		try{
			byte b[] = applet.loadBytes(FileName);
			double[] Tri = new double[9];
			//Skip the header
			int offs = 84;
			//Read each triangle out
			while(offs<b.length){
				offs = offs + 12; //skip the normals entirely!
				for(int i = 0; i<9; i++){
					Tri[i] = bin_to_float(b[offs], b[offs+1], b[offs+2], b[offs+3]);
					offs = offs+4;
				}
				offs = offs+2;//Skip the attribute bytes
				Triangles.add(new Triangle(Tri[0],Tri[1],Tri[2],Tri[3],Tri[4],Tri[5],Tri[6],Tri[7],Tri[8]));
			}
		} catch (Exception e) {
			System.out.println ("Unable to load binary STL");
			return false;
		}
		return true;
	}

	private float bin_to_float(byte b, byte c, byte d, byte e) {//little endian to big endian
		return  Float.intBitsToFloat((e << 24) |
				((d & 0xff) << 16) |
				((c & 0xff) << 8)|
				(b & 0xff));
	}

	private boolean LoadTextMesh (String path){
		// text format:
		// solid ascii
		//	 facet normal -2.242146e-006 -8.944270e-001 -4.472139e-001
		//		 outer loop
		//			 vertex	 5.000000e+001 3.829179e+001 7.236046e+000
		//			 vertex	 4.048944e+001 3.690984e+001 1.000000e+001
		//			 vertex	 4.412215e+001 4.190984e+001 -1.963632e-006
		//		 endloop
		//	 endfacet
		//	 facet normal -8.506515e-001 -2.763928e-001 -4.472125e-001
		//		 outer loop
		// .
		// .
		//	 endfacet
		// endsolid

		BufferedReader reader;
		String buf;

		try {
			reader = applet.createReader(path);
			buf = reader.readLine();
			if(buf.indexOf ("solid") != 0) // doesn't appear to be a text stl..
				return false;
		} catch (RuntimeException ex) {
			// this is (more than..) a bit stupid. java complains if you ask it to open
			// abc.stl when the file name is ABC.stl. figuring out and using then actual
			// path somewhere above would be good..
			System.out.println("Unable to read from buffered reader. Check to make sure you gave the EXACT pathname (case matters)");
			return false;
		} catch (Exception e) {
			// file doesn't exist or something.. likely won't fail on binary stl
			System.out.println("unable to read from buffered reader..");
			return false;
		}

		try{
			while (true){
				buf = reader.readLine();
				if (buf == null || buf.indexOf ("endsolid") == 0)// end of file or last valid line.. good stuff
					break;
				if (buf.indexOf ("facet normal") == -1)// sanity check.. 
					return false;
	
				buf = reader.readLine();	// "		outer loop" 

				String[] doubles;
				int offset;

				// read in the first triangle.. "vertex " followed by 3 doubles.. the 
				// regex string split sometimes returns an extra leading entry with
				// nothing in it (5 elements in the 'doubles' array) and sometimes 
				// doesn't (4 elements) so check the length and offset by 1 if needed
				doubles = reader.readLine().split("[\\s,;]+");
				offset = doubles.length == 5 ? 1 : 0;
				double x1 = Float.parseFloat (doubles[1 + offset]);
				double y1 = Float.parseFloat (doubles[2 + offset]);
				double z1 = Float.parseFloat (doubles[3 + offset]);

				// 2nd triangle
				doubles = reader.readLine().split("[\\s,;]+");
				offset = doubles.length == 5 ? 1 : 0;
				double x2 = Float.parseFloat (doubles[1 + offset]);
				double y2 = Float.parseFloat (doubles[2 + offset]);
				double z2 = Float.parseFloat (doubles[3 + offset]);

				// 3rdd triangle
				doubles = reader.readLine().split("[\\s,;]+");
				offset = doubles.length == 5 ? 1 : 0;
				double x3 = Float.parseFloat (doubles[1 + offset]);
				double y3 = Float.parseFloat (doubles[2 + offset]);
				double z3 = Float.parseFloat (doubles[3 + offset]);

				Triangles.add (new Triangle (x1, y1, z1, x2, y2, z2, x3, y3, z3));

				reader.readLine(); // "	 endloop"
				reader.readLine(); // "	endfacet"
			}
		} catch (Exception e){ return false; }
		return true;
	}

	void Scale(double Factor){
		if(Double.isNaN(Factor))return;
		for(int i = Triangles.size()-1;i>=0;i--){
			Triangle tri = (Triangle) Triangles.get(i);
			tri.Scale(Factor);
		}
		CalculateBoundingBox();
	}

	void Translate(double tx, double ty, double tz){
		for(int i = Triangles.size()-1;i>=0;i--){
			Triangle tri = (Triangle) Triangles.get(i);
			tri.Translate(tx,ty,tz);
		}
		CalculateBoundingBox();
	}


	void RotateX(double Angle){
		if(Double.isNaN(Angle)) return;
		for(int i = Triangles.size()-1;i>=0;i--){
			Triangle tri = (Triangle) Triangles.get(i);
			tri.RotateX(Angle);
		}
		CalculateBoundingBox();
	} 

	void CalculateBoundingBox(){
		Triangle tri = Triangles.get(0);
		bx1 = tri.getLow(0);
		bx2 = tri.getHigh(0);
		by1 = tri.getLow(1);
		by2 = tri.getHigh(1);
		bz1 = tri.getLow(2);
		bz2 = tri.getHigh(2);
		for(int i = 1; i < Triangles.size(); i++){
			tri = Triangles.get(i); 
			bx1 = Math.min(tri.getLow(0), bx1);
			bx2 = Math.max(tri.getHigh(0), bx2);
			by1 = Math.min(tri.getLow(1), by1);
			by2 = Math.max(tri.getHigh(1), by2);
			bz1 = Math.min(tri.getLow(2), bz1);
			bz2 = Math.max(tri.getHigh(2), bz2);
		}		
	}
}	

