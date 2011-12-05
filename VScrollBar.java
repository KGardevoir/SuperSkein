import processing.core.PApplet;

class VScrollbar {
	int bheight; 
	int swidth, sheight;		// width and height of bar
	int xpos, ypos;				 // x and y position of bar
	double spos, newspos;		// y position of slider
	int sposMin, sposMax;	 // max and min values of slider
	boolean over;					 // is the mouse over the slider?
	boolean locked;
	double ratio;
	PApplet applet; 
	public VScrollbar (PApplet app, int xp, int yp, int sw, int sh, int bh) {
		this.swidth = sw;
		this.sheight = sh;
		this.ratio = 1. / (double)(sh - bh);
		this.xpos = xp;
		this.ypos = yp;
		this.newspos = this.spos = ypos + bh + sh/2;
		this.sposMin = ypos;
		this.sposMax = ypos + sh;
		this.bheight = bh; 
		this.applet = app; 
	}

	void update(int mx, int my, boolean pressed) {
		if(over(mx, my)) over = true;
		else over = false;
		
		if(pressed && over) locked = true;
		else if(!pressed) locked = false;
		
		if(locked) newspos = constrain(my, sposMin, sposMax-bheight);
		if(Math.abs(newspos - spos) > 1) spos = newspos; 
	}

	int constrain(int val, int minh, int maxh) { return Math.min(Math.max(val, minh), maxh); }

	boolean over(int mx, int my) {
		if(mx > xpos && mx < xpos+swidth && my > ypos && my < ypos+sheight) return true;
		else return false;
	}

	void display(boolean pressed) {
		applet.fill(255);
		applet.rect(xpos, ypos, swidth, sheight);
		if(pressed && (over || locked)) {
			applet.fill(153, 102, 0);
		} else {
			applet.fill(102, 102, 102);
		}
		applet.rect(xpos, (float) spos, swidth, bheight);
	}
	
	void set_BarHeight(int bh){ 
		this.bheight = bh;
		this.ratio = 1. / (double)(sheight - bh);
	}

	double getPos() {
		// Convert spos to be values between
		// 0 and the total width of the scrollbar
		return spos * ratio;
	}
}