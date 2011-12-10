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
		private ClosedPath next; 
		private ClosedPath prev; 
		private ClosedPath(double[] pt){
			this.pt = pt; 
			next = this; 
			prev = this; 
		}
		private ClosedPath(double[] pt, ClosedPath n, ClosedPath p){
			pt = new double[2]; 
			this.pt[0] = pt[0]; 
			this.pt[1] = pt[1];
			next = n; 
			prev = p; 
			p.next = this; 
			n.prev = this; 
		}
		public ClosedPath(SSPath t){
			this(new double[2]); 
			PathIterator pi = t.getPathIterator(new AffineTransform()); 
			double[] tmp = new double[6]; 
			pi.currentSegment(tmp); 
			this.pt[0] = tmp[0]; 
			this.pt[1] = tmp[1]; 
			ClosedPath curr = this; 
			for(; !pi.isDone(); pi.next()){
				int type = pi.currentSegment(tmp); 
				if(type == PathIterator.SEG_CLOSE) break; 
				else curr = new ClosedPath(tmp, curr.next, curr.prev); 
			}
		}
		public ClosedPath next(){ return this.next; }
		public ClosedPath prev(){ return this.prev; }
		public ClosedPath remove(){ 
			prev.next = next; 
			next.prev = prev; 
			return next; 
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
		SSPath npath = new SSPath(config); 
		PathIterator pi = this.getPathIterator(new AffineTransform());
		double[][] s = new double[2][6]; 
		double[] fp  = new double[2]; 
		int pt = pi.currentSegment(s[0]); 
		fp[0] = s[0][0]; 
		fp[1] = s[0][1]; 
		npath.moveTo(s[0][0], s[0][1]); 
		pi.next(); 
		pt = pi.currentSegment(s[1]); 
		Culler cull = new Culler(s[0], s[1]); //new Culler Object, maintain only 1, we need remove past points, not current maintain a start and finish point
		
		s[0][0] = s[1][0]; 
		s[0][1] = s[1][1]; 
		//int k = 0; 
		for(; !pi.isDone(); pi.next()){
			pt = pi.currentSegment(s[1]);
			if(pt == PathIterator.SEG_CLOSE){//we also need to reduce the end of the path to the beginning of the path
				/*PathIterator pi2 = npath.getPathIterator(new AffineTransform()); 
				pi2.currentSegment(s[1]); 
				if(cull.distance(s[1]) <= 0){
					pi2.next();
					SSPath n2path = new SSPath(config); 
					pi2.currentSegment(s[1]); 
					n2path.moveTo(s[1][0], s[1][1]);
					n2path.append(pi2, true);
					n2path.closePath(); 
					npath = n2path; 
				} else */{
					npath.lineTo(cull.end[0], cull.end[1]); 
					npath.closePath(); 					
				}
				break; 
			} else {
				if(cull.distance(s[1]) > perc){
					npath.lineTo(cull.end[0], cull.end[1]); 
					cull.setStart(cull.end);
					cull.setEnd(s[1]); 
				} else {
					//k++; 
					cull.setEnd(s[1]); 
				}
			}
			s[0][0] = s[1][0]; 
			s[0][1] = s[1][1]; 
		}
		return npath;  
	}
	
}

