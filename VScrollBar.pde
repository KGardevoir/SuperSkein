class VScrollbar {
	int bheight; 
	int swidth, sheight;		// width and height of bar
	int xpos, ypos;				 // x and y position of bar
	float spos, newspos;		// y position of slider
	int sposMin, sposMax;	 // max and min values of slider
	boolean over;					 // is the mouse over the slider?
	boolean locked;
	float ratio;

	VScrollbar (int xp, int yp, int sw, int sh, int bh) {
		this.swidth = sw;
		this.sheight = sh;
		this.ratio = 1. / (float)(sh - bh);
		this.xpos = xp;
		this.ypos = yp;
		this.newspos = this.spos = ypos + bh + sh/2;
		this.sposMin = ypos;
		this.sposMax = ypos + sh;
		this.bheight = bh; 
	}

	void update(int mx, int my, boolean pressed) {
		if(over(mx, my)) over = true;
		else over = false;
		
		if(pressed && over) locked = true;
		else if(!pressed) locked = false;
		
		if(locked) newspos = constrain(my, sposMin, sposMax-bheight);
		if(abs(newspos - spos) > 1) spos = newspos; 
	}

	int constrain(int val, int minh, int maxh) { return min(max(val, minh), maxh); }

	boolean over(int mx, int my) {
		if(mx > xpos && mx < xpos+swidth && my > ypos && my < ypos+sheight) return true;
		else return false;
	}

	void display(boolean pressed) {
		fill(255);
		rect(xpos, ypos, swidth, sheight);
		if(pressed && (over || locked)) {
			fill(153, 102, 0);
		} else {
			fill(102, 102, 102);
		}
		rect(xpos, spos, swidth, bheight);
	}
	
	void set_BarHeight(int bh){ 
		this.bheight = bh;
		this.ratio = 1. / (float)(sheight - bh);
	}

	float getPos() {
		// Convert spos to be values between
		// 0 and the total width of the scrollbar
		return spos * ratio;
	}
}