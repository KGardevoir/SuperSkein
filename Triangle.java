// Triangle Class

class Triangle {
	double x1,x2,x3,y1,y2,y3,z1,z2,z3,xn,yn,zn;
	
	Triangle(double tX1, double tY1, double tZ1,double tX2, double tY2, double tZ2,double tX3, double tY3, double tZ3) {
		x1 = tX1;
		y1 = tY1;
		z1 = tZ1;
		x2 = tX2;
		y2 = tY2;
		z2 = tZ2;
		x3 = tX3;
		y3 = tY3;
		z3 = tZ3;
		//Sorting the Triangle according to
		//height makes slicing them easier.
		Resort();
	}
	
	void Scale(double Factor){
		x1 = Factor*x1;
		y1 = Factor*y1;
		z1 = Factor*z1;
		x2 = Factor*x2;
		y2 = Factor*y2;
		z2 = Factor*z2;
		x3 = Factor*x3;
		y3 = Factor*y3;
		z3 = Factor*z3;
	}
	
	
	void Translate(double tX, double tY, double tZ){
		x1+=tX;
		x2+=tX;
		x3+=tX;
		y1+=tY;
		y2+=tY;
		y3+=tY;
		z1+=tZ;
		z2+=tZ;
		z3+=tZ;
	}
	
	//Rotations-- feed these in radians!
	//A great application is rotating your
	//mesh to a desired orientation.
	// 90 degrees = PI/2.
	void RotateZ(double Angle){
		double xn,yn;
		xn = x1*Math.cos(Angle) - y1*Math.sin(Angle);
		yn = x1*Math.sin(Angle) + y1*Math.cos(Angle);
		x1 = xn;
		y1 = yn;
		xn = x2*Math.cos(Angle) - y2*Math.sin(Angle);
		yn = x2*Math.sin(Angle) + y2*Math.cos(Angle);
		x2 = xn;
		y2 = yn;
		xn = x3*Math.cos(Angle) - y3*Math.sin(Angle);
		yn = x3*Math.sin(Angle) + y3*Math.cos(Angle);
		x3 = xn;
		y3 = yn;
		Resort();
	}
	
	void RotateY(double Angle){
		double xn,zn;
		xn = x1*Math.cos(Angle) - z1*Math.sin(Angle);
		zn = x1*Math.sin(Angle) + z1*Math.cos(Angle);
		x1 = xn;
		z1 = zn;
		xn = x2*Math.cos(Angle) - z2*Math.sin(Angle);
		zn = x2*Math.sin(Angle) + z2*Math.cos(Angle);
		x2 = xn;
		z2 = zn;
		xn = x3*Math.cos(Angle) - z3*Math.sin(Angle);
		zn = x3*Math.sin(Angle) + z3*Math.cos(Angle);
		x3 = xn;
		z3 = zn;
		Resort();
	}
	
	void RotateX(double Angle){
		double yn,zn;
		yn = y1*Math.cos(Angle) - z1*Math.sin(Angle);
		zn = y1*Math.sin(Angle) + z1*Math.cos(Angle);
		y1 = yn;
		z1 = zn;
		yn = y2*Math.cos(Angle) - z2*Math.sin(Angle);
		zn = y2*Math.sin(Angle) + z2*Math.cos(Angle);
		y2 = yn;
		z2 = zn;
		yn = y3*Math.cos(Angle) - z3*Math.sin(Angle);
		zn = y3*Math.sin(Angle) + z3*Math.cos(Angle);
		y3 = yn;
		z3 = zn;
		Resort();
	} 
	
	
	//The conditionals here are for working
	//out what kind of intersections the triangle
	//makes with the plane, if any.	Returns
	//null if the triangle does not intersect.
	SSLine GetZIntersect(double ZLevel){
		SSLine Intersect;
		double xa,xb,ya,yb;
		if(z1<ZLevel){
			if(z2>ZLevel){
				xa = x1 + (x2-x1)*(ZLevel-z1)/(z2-z1);
				ya = y1 + (y2-y1)*(ZLevel-z1)/(z2-z1);
				if(z3>ZLevel){
					xb = x1 + (x3-x1)*(ZLevel-z1)/(z3-z1);
					yb = y1 + (y3-y1)*(ZLevel-z1)/(z3-z1);
				} else {
					xb = x2 + (x3-x2)*(ZLevel-z2)/(z3-z2);
					yb = y2 + (y3-y2)*(ZLevel-z2)/(z3-z2);					
				}
				Intersect = new SSLine(xa,ya,xb,yb);
				return Intersect;
			} else {
				if(z3>ZLevel){
					xa = x1 + (x3-x1)*(ZLevel-z1)/(z3-z1);
					ya = y1 + (y3-y1)*(ZLevel-z1)/(z3-z1);
					xb = x2 + (x3-x2)*(ZLevel-z2)/(z3-z2);
					yb = y2 + (y3-y2)*(ZLevel-z2)/(z3-z2);
				
					Intersect = new SSLine(xa,ya,xb,yb);
					return Intersect;
				} else return null;
			}
		} else return null;
	}
	double getHigh(int idx){
		switch(idx){
		case 0: 
			return Math.max(x1,Math.max(x2,x3)); 
		case 1:
			return Math.max(y1,Math.max(y2,y3)); 
		case 2:
			return Math.max(z1,Math.max(z2,z3));
		}
		return 0.;
	}
	double getLow(int idx){
		switch(idx){
		case 0: 
			return Math.min(x1,Math.min(x2,x3)); 
		case 1:
			return Math.min(y1,Math.min(y2,y3)); 
		case 2:
			return Math.min(z1,Math.min(z2,z3));
		}
		return 0.; 
	}
	//In the old days, a triangle's normal was defined
	//by right-hand-rule from the order vertices were
	//defined.	If this were the case with STL this would
	//scramble the normals horribly.
	//Of course, we never USE the normals...
	void Resort(){
		if(z3<z1){
			xn=x1;
			yn=y1;
			zn=z1;
			x1=x3;
			y1=y3;
			z1=z3;
			x3=xn;
			y3=yn;
			z3=zn;
		}
		if(z2<z1){
			xn=x1;
			yn=y1;
			zn=z1;
			x1=x2;
			y1=y2;
			z1=z2;
			x2=xn;
			y2=yn;
			z2=zn;
		}
		if(z3<z2){
			xn=x3;
			yn=y3;
			zn=z3;
			x3=x2;
			y3=y2;
			z3=z2;
			x2=xn;
			y2=yn;
			z2=zn;
		}
	}
}	

