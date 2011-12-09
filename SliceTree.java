import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


public class SliceTree {
	public class SliceTreeLevel {
		public SSPath path; 
		public boolean isOuter;
	}
	private class ITn {
		public SSPath path; 
		public int index; 
		public ArrayList<ITn> children; //use ArrayList for speed
		public ITn(SSPath p, int idx){
			path = p; 
			index = idx; 
			children = new ArrayList<ITn>(); 
		}
	}
	private class IndexBooleanArray{
		public int index; 
		public boolean[] arr; 
		public IndexBooleanArray(int idx, int arrlen){
			index = idx; 
			arr = new boolean[arrlen]; 
		}
		public boolean has(int idx){
			return arr[idx]; 
		}
	}
	ArrayList<ITn> root; 
	public SliceTree(ArrayList<SSPath> paths){
		LinkedList<IndexBooleanArray> pathtype = new LinkedList<IndexBooleanArray>();
		for(int i = 0; i < paths.size(); i++){
			IndexBooleanArray cpath = new IndexBooleanArray(i, paths.size());
			for(int j = 0; j < paths.size(); j++)
				if(i == j) cpath.arr[j] = false; //the diagonal should be false (path doesn't contain itself)
				else cpath.arr[j] = paths.get(j).contains(paths.get(i).getCurrentPoint());//assume that we don't have intersecting paths
			pathtype.add(cpath); 
		}//n^2 
		
		root = new ArrayList<ITn>(); 
		ArrayList<ITn> roots = root; 
		int bneed = 0; 
		while(!pathtype.isEmpty()){
			int i = 0, k = -1; 
			for(IndexBooleanArray links : pathtype){
				k = 0; 
				for(int j = 0; j < links.arr.length; j++){
					if(links.has(j)) k++; 
					if(k > bneed) break; //abort if we have too many
				}
				if(k == bneed) break; //we found a link
				i++;
			}
			if(k == bneed){//we have a node, its of the right order, now we need to its home
				IndexBooleanArray links = pathtype.remove(i);
				SSPath thePath = paths.get(links.index);
				if(roots.isEmpty() || bneed == 0) roots.add(new ITn(thePath, links.index)); 
				else {
					boolean has = false; 
					for(ITn startingroots : roots){
						has = false; 
						while(links.has(startingroots.index)){//this is the correct path, continue down while this is true
							has = true; 
							boolean ohas = false; 
							if(startingroots.children.isEmpty()) break; 
							for(ITn ml : startingroots.children){
								if(links.has(ml.index)){
									ohas = true; 
									startingroots = ml; 
									break; 
								}
							}
							if(!ohas) break; 
						}
						if(has){
							startingroots.children.add(new ITn(thePath, links.index)); 
							break; //we found its place
						}
					}
					if(!has) roots.add(new ITn(thePath, links.index)); 
				}
			} else if(k == -1){
				System.out.println("Empty Slice List"); 
			} else {
				bneed++; 
			}
		}
		fixWindings(); 
	}
	public SSPath flatten(Configuration conf){
		SSPath p = new SSPath(conf); 
		if(root.isEmpty()) return p; 
		else {
			LinkedList<ITn> que = new LinkedList<ITn>(root);
			while(!que.isEmpty()){
				ITn list = que.remove();
				p.append(list.path, false); 
				que.addAll(list.children); 
			}
		}
		return p;
	}
	private void fixWindings(){
		LinkedList<ITn> clevel = new LinkedList<ITn>(root), nlevel = new LinkedList<ITn>(); 
		while(!clevel.isEmpty()){
			ITn list = clevel.remove();
			PathIterator p = list.path.getPathIterator(new AffineTransform());
			double sum = 0; 
			double[][] s = new double[2][6]; 
			double[] fs = new double[2]; 
			p.currentSegment(s[0]); 
			fs[0] = s[0][0]; 
			fs[1] = s[0][1]; 
			for(; !p.isDone(); p.next()){
				int f = p.currentSegment(s[1]); 
				if(f == PathIterator.SEG_CLOSE){
					sum += (fs[0]-s[1][0])*(fs[1]+s[1][1]); 
					break;
				} else {
					sum += (s[1][0]-s[0][0])*(s[1][1]+s[0][1]);
					s[0][0] = s[1][0]; 
					s[0][1] = s[1][1]; 
				}
			}
			if(sum < 0)//incorrect winding order
				list.path.reverse(); 
			clevel.addAll(list.children); 
		}
	}
	public SliceTree computeShell(){//compute shell by computing the vertex normals based on the geometry of the object
		
		return null;
	}
}
