import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

// GUI Classes

class GUI {
	
	int x,y,w,h;
	String title;
	boolean Focus;
	boolean Pressed;
	PFont font;
	PApplet applet;
	GUI(PApplet app, int ix, int iy, int iw, int ih, String ititle) {
		this.applet = app; 
		Focus = false;
		Pressed = false;
		x=ix;
		y=iy;
		w=iw;
		h=ih;
		title = ititle;
	}
	
	void display(){
		font = applet.loadFont("data/ArialMT-12.vlw");
		applet.noFill();
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.arc((float)x+4,(float)y+4,8,8,(float)Math.PI,(float)(Math.PI*2-Math.PI/2));
		applet.line(x+4,y,x+w-4,y);
		applet.arc((float)x+w-4,(float)y+4,8,8,(float)(2*Math.PI-Math.PI/2), (float)(2*Math.PI));
		applet.line(x+w,y+4,x+w,y+h-4);
		applet.arc((float)x+w-4,(float)y+h-4,8,8,0,(float)Math.PI/2);
		applet.line(x+4,y+h,x+w-4,y+h);
		applet.arc((float)x+4,(float)y+h-4,8,8,(float)Math.PI/2,(float)Math.PI);
		applet.line(x,y+4,x,y+h-3);
		
		//rect(x,y,w,h);
		
		applet.fill(255);
		applet.textAlign(PConstants.CENTER);
		applet.textFont(font);
		applet.textMode(PConstants.SCREEN);
		applet.text(title,x+w/2,(y+h/2)+6);
	}
	
	boolean over(int ix,int iy){
		if((ix>x)&(ix<(x+w)&(iy>y)&(iy<(y+h)))) return true;
		else return false;
	}
	
}	

//Value is between 0 and 1
//Value set to >1 becomes a DONE bar.
class GUIProgressBar {
	
	double Value;
	int x,y,w,h;
	String Mesg;
	PApplet applet; 
	GUIProgressBar(PApplet app, int ix, int iy, int iw, int ih){
		applet = app; 
		Value = 0;
		x=ix;
		y=iy;
		w=iw;
		h=ih;
		Mesg = "";
	}

	void update(double fileWriteFraction){ Value = fileWriteFraction; }
	
	void display(){
		PFont font;
		font = applet.loadFont("data/ArialMT-12.vlw");
		applet.textAlign(PConstants.CENTER);
		applet.textFont(font);
		applet.textMode(PConstants.SCREEN);

		if(Value<1){
			applet.fill(0,0,200);
			applet.rect(x,y,(float) (w*Value),h);
			applet.fill(255);
			//if(Value>0.01)text("Working...",x+w/2,(y+h/2)+6);
		} else {
			applet.fill(0,200,0);
			applet.rect(x,y,w,h);

			applet.fill(255);
			Mesg="Done!";
			//text("Done!",x+w/2,(y+h/2)+6);
		}
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.noFill();
		applet.rect(x,y,w,h);
		applet.text(Mesg,x+w/2,(y+h/2)+6);
	}
	
	void message(String input){ Mesg=input; }
}



//Note, cursor is always at the end.
//One day maybe we'll fix this.
class GUITextBox{
	String Text;
	int x,y,w,h;
	boolean Focus;
	PApplet applet; 
	GUITextBox(PApplet app, int ix, int iy, int iw, int ih, String iText) {
		Focus = false;
		applet = app; 
		x=ix;
		y=iy;
		w=iw;
		h=ih;
		Text = iText;
	}
	
	boolean over(int ix,int iy){
		if((ix>x)&(ix<(x+w)&(iy>y)&(iy<(y+h)))) return true;
		else return false;
	}
	
	void display(){
		PFont font = applet.loadFont("data/ArialMT-12.vlw");
		applet.textAlign(PConstants.LEFT);
		applet.textFont(font);
		applet.textMode(PConstants.SCREEN);
		applet.fill(0);
		applet.stroke(200);
		applet.strokeWeight(1);
		if(Focus)applet.stroke(255);
		applet.rect(x,y,w,h);
		applet.fill(255);
		applet.text(Text,x+2,y+12);
	}

	void doKeystroke(int KeyStroke){
		if(Focus){
			if((KeyStroke==8)&(Text.length()>0))Text=Text.substring(0,Text.length()-1);
			if((KeyStroke>31)&(KeyStroke<177))Text = Text + (char)(KeyStroke);
		}
	}
	
	void checkFocus(int X, int Y){
		if(this.over(X,Y)) Focus = true;
		else Focus = false;
	}
}

class GUIFloatBox {
	String Text;
	int x,y,w,h;
	boolean Focus;
	boolean Valid;
	PApplet applet; 
	GUIFloatBox(PApplet app, int ix, int iy, int iw, int ih, String iText) {
		applet = app; 
		Focus = false;
		x=ix;
		y=iy;
		w=iw;
		h=ih;
		Text = iText;
		Valid = true;
	}

	
	boolean over(int ix,int iy) {
		if((ix>x)&&(ix<(x+w)&&(iy>y)&&(iy<(y+h)))) return true;
		else return false;
	}
	
	void display() {
		PFont font = applet.loadFont("data/ArialMT-12.vlw");
		applet.textAlign(PConstants.LEFT);
		applet.textFont(font);
		applet.textMode(PConstants.SCREEN);
		applet.fill(0);
		applet.stroke(200);
		applet.strokeWeight(1);
		if(Focus)applet.stroke(255);
		if(!Valid)applet.stroke(200,100,100);
		if(!Valid&Focus)applet.stroke(255,100,100);
		applet.rect(x,y,w,h);
		applet.fill(255);
		applet.text(Text,x+2,y+12);
	}

	void doKeystroke(int KeyStroke){
		if(Focus){
			if((KeyStroke==8)&(Text.length()>0))Text=Text.substring(0,Text.length()-1);
			if((KeyStroke>31)&(KeyStroke<177))Text = Text + (char)(KeyStroke);
			Valid=true;
			applet.redraw();
		}
	}
	
	void checkFocus(int X, int Y){
		if(this.over(X,Y))Focus=true;
		else Focus=false;
	}
	
	float getFloat(){
		try { 
			return Float.parseFloat(Text); 
		} catch(Exception e){
			Valid=false;
			return Float.NaN;
		}
	}
	
	void setFloat(float in){
		if(in<0)in=-in;
		if(in<10)Text=PApplet.nf(in,1,2);
		if((in<100)&(in>10))Text=PApplet.nf(in,2,2);
		if((in<1000)&(in>100))Text=PApplet.nf(in,3,2);
		if((in<10000)&(in>1000))Text=PApplet.nf(in,4,2);
		if(in>10000)Text=PApplet.nf(in,5,2);
	}
}
class GUICheckBox{
	String Text; 
	int x,y, w = 15, h = 15; 
	boolean checked; 
	PApplet applet; 
	GUICheckBox(PApplet app, int ix, int iy, boolean state, String tex){
		applet = app; 
		Text = tex; 
		x = ix; 
		y = iy; 
		checked = state; 
	}
	boolean over(int ix,int iy) {
		if((ix>x)&&(ix<(x+w)&&(iy>y)&&(iy<(y+h)))) return true;
		else return false;
	}
	void toggle(){
		checked = !checked; 
	}
	void display() {
		PFont font = applet.loadFont("data/ArialMT-12.vlw");
		applet.textAlign(PConstants.LEFT);
		applet.textFont(font);
		applet.textMode(PConstants.SCREEN);
		applet.fill(0);
		applet.stroke(200);
		applet.strokeWeight(1);

		applet.rect(x,y,w,h);
		if(checked){
			applet.line(x, y, x+w, y+h); 
			applet.line(x+w, y, x, y+h);
		}
		applet.fill(255);
		applet.text(Text,x+w+2,y+12);
	}
}