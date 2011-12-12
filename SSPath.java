// Path Class
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

class SSPath extends Path2D.Double {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8034991198131925267L;
	private Configuration config; 
	// constructor.
	// Initializes a new path object, with no path points.
	SSPath(Configuration conf){
		config = conf; 
	}

	SSPath(Configuration conf, SSLine firstLine) {
		super(firstLine);
		config = conf; 
	}

	void reverse(){
		PathIterator iter = this.getPathIterator(new AffineTransform()); 
		ArrayList<java.lang.Double> list = new ArrayList<java.lang.Double>(); 
		ArrayList<Integer> olist = new ArrayList<Integer>(); 
		for(; !iter.isDone(); iter.next()){
			double[] d = new double[6]; 
			olist.add(iter.currentSegment(d));
			list.add(d[0]);
			list.add(d[1]);
		}
		this.reset(); 
		for(int i = 0, j = 0; i < olist.size(); i++, j+=2){
			if(olist.get(i) == PathIterator.SEG_MOVETO || i == olist.size()-1){
				int k = j; 
				for(; i < olist.size(); i++, j+=2){
					if(olist.get(i) == PathIterator.SEG_CLOSE){
						list.set(j, list.get(k)); 
						list.set(j+1, list.get(k+1)); 
						break; 
					}
				}
			}
		}
		for(int i = olist.size()-1, j = list.size()-1; i >= 0; i--, j-=2){
			double s2 = list.get(j); 
			double s1 = list.get(j-1); 
			try{
				if(olist.get(i) == PathIterator.SEG_MOVETO) this.closePath(); 
				else if(olist.get(i) == PathIterator.SEG_CLOSE) this.moveTo(s1, s2);
				else this.lineTo(s1, s2);
			} catch (java.awt.geom.IllegalPathStateException e){
				System.out.println("Something happened"); 
			}
		}
	}
	
	
	private class Culler{
		//double[] start = new double[2]; 
		//double[] end = new double[2];  
		ClosedPath start, end; 
		public Culler(ClosedPath s1, ClosedPath s2){  
			start = s1; 
			end = s2; 
		}
		public double distance(double[] p1){
			double a = end.pt[0]-start.pt[0], b = end.pt[1] - start.pt[1]; 
			return Math.abs(a*(start.pt[1]-p1[1])-(start.pt[0]-p1[0])*b)/Math.sqrt((a*a+b*b)); 
		}
		public void setStart(ClosedPath p){ start = p; }
		public void setEnd(ClosedPath p){ end = p; }
	}
	private class ClosedPath{//represent path as a linkedlist
		public double[] pt; 
		ClosedPath next; 
		ClosedPath prev; 
		private ClosedPath(double[] pt){
			this.pt = pt; 
			next = this; 
			prev = this; 
		}
		private ClosedPath(double[] pt, ClosedPath p){
			this.pt = new double[2]; 
			this.pt[0] = pt[0]; 
			this.pt[1] = pt[1];
			prev = p;
			next = prev.next; 
			prev.next = this;
		}
		public ClosedPath(SSPath t){
			this(new double[2]); 
			int k = 0; 
			PathIterator pi = t.getPathIterator(new AffineTransform()); 
			double[] tmp = new double[6]; 
			pi.currentSegment(tmp); 
			this.pt[0] = tmp[0]; 
			this.pt[1] = tmp[1]; 
			ClosedPath curr = this; 
			for(; !pi.isDone(); pi.next()){
				k++; 
				int type = pi.currentSegment(tmp); 
				if(type == PathIterator.SEG_CLOSE) break; 
				else {
					curr = new ClosedPath(tmp, curr); 
				}
			}
			//DONE!
		}
		public SSPath toPath(){
			ClosedPath star = this;
			SSPath path = new SSPath(config); 
			path.moveTo(star.pt[0], star.pt[1]); 
			ClosedPath curr = star.next;
			for(;curr != star; curr = curr.next) path.lineTo(curr.pt[0], curr.pt[1]); 
			path.closePath(); 
			return path; 
		}
	}
	/**
	 * Automatically determine culling distance based on machine precision.
	 * @return Path with all extraneous points removed
	 */
	public SSPath cull(){ return cull(config.MachinePercision); }
	/**
	 * Removes points which are on a line and are less than perc away from the line
	 * @param perc Specifies distance at which to consider a point for culling not in the path
	 * @return Path with all extraneous points removed
	 */
	public SSPath cull(double perc){
		ClosedPath npath = new ClosedPath(this); 
		Culler cull = new Culler(npath, npath.next); //new Culler Object, maintain only 1, we need remove past points, not current maintain a start and finish point
		ClosedPath spoint = npath;
		npath = npath.next; 
		boolean done = false;
		//int k = 0; 
		for(int hitcount = 0; !done ; npath = npath.next){//we have to go through twice, to make sure the path is truly culled (*note this is because of the beginning never being culled otherwise)
			if(spoint == npath){//we also need to reduce the end of the path to the beginning of the path
				if(hitcount == 0) spoint = spoint.next; 
				else if(hitcount > 1) {//make sure we skip the second time we hit this 
					cull.start.next = cull.end; 
					cull.end.prev = cull.start; 
					done = true; 
					break; 
				}
				hitcount++; 
			} 
			
			if(cull.distance(npath.pt) > perc){//TODO need to maintain size to ensure that if size < 3 that we don't cull any points 
				cull.start.next = cull.end; 
				cull.end.prev = cull.start; 
				cull.setStart(cull.end);
				cull.setEnd(npath); 
			} else {
				if(npath == spoint)//make sure we have a starting point which is actually valid, e.g. make sure its not culled
					spoint = cull.start; 
				cull.setEnd(npath); 
			}
		
		}
		return spoint.toPath();  
	}
	public SSPath computeShell(double dist){
		ClosedPath pi = new ClosedPath(this); //create a copy of a closed list
		double[][] ls = new double[3][6]; 
		ls[0] = pi.pt;
		pi = pi.next; 
		ls[1] = pi.pt; 
		pi = pi.next; //fill in first two segments, but
		ClosedPath fp = pi; 
		boolean done = false; 
		for(int hitcount = 0; !done; pi = pi.next){
			ls[2] = pi.pt; 
			if(fp == pi){//we also need to reduce the end of the path to the beginning of the path
				if(hitcount == 0) fp = fp.next; 
				else if(hitcount > 1) {//make sure we skip the second time we hit this 
					done = true; 
					break; 
				}
				hitcount++; 
			} 
			double[] u = { ls[1][0]-ls[0][0], ls[1][1]-ls[0][1] };//vector starting from ls[0] 
			double ulen = mag(u); 
			double[] u_perp = { -u[1]*dist/ulen, u[0]*dist/ulen }; 
			double[] v = { ls[2][0]-ls[1][0], ls[2][1]-ls[1][1] };//vector starting from ls[1]
			double vlen = mag(v);
			double[] v_perp = { -v[1]*dist/vlen, v[0]*dist/vlen }; 
			
			double[][] ls2= {  {ls[0][0] + u_perp[0], ls[0][1] + u_perp[1]},
								{ls[1][0] + u_perp[0], ls[1][1] + u_perp[1]},
							   {ls[1][0] + v_perp[0], ls[1][1] + v_perp[1]},
								{ls[2][0] + v_perp[0], ls[2][1] + v_perp[1]}};//build perpendicular lines
			ls[1] = findIntersection(ls2); 
			//add ls[0] and ls[1] to the list
			ls[0] = ls[1]; 
			ls[1] = ls[2]; 
		}
		return fp.toPath();
	}
	private double[] findIntersection(double[][] ls) {
		double m0 = (ls[1][1]-ls[0][1])/(ls[1][0]-ls[0][0]);
		double b0 = ls[0][1]-m0*ls[0][0]; 
		double m1 = (ls[3][1]-ls[2][1])/(ls[3][0]-ls[2][0]); 
		double b1 = ls[2][1]-m1*ls[2][0]; //all necessary parameters are computed.
		if(m0 == java.lang.Double.NaN 
				|| m0 == java.lang.Double.NEGATIVE_INFINITY 
				|| m0 == java.lang.Double.POSITIVE_INFINITY) System.out.println("m0 is a vertical line");
		if(m1 == java.lang.Double.NaN
				|| m1 == java.lang.Double.NEGATIVE_INFINITY //handle these cases
				|| m1 == java.lang.Double.POSITIVE_INFINITY) System.out.println("m1 is a vertical line");
		if(m0 == m1){
			return ls[1]; //return any point as they are parallel
		} else {
			double[] pt = new double[6]; 
			pt[0] = (b0-b1)/(m1-m0);
			pt[1] = m0*pt[0]+b0; //finally compute y value.
			return pt;
		}
	}

	private double mag(double[] u){ return Math.sqrt(u[0]*u[0]+u[1]*u[1]); }
}

