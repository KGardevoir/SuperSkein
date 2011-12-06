import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


public class SliceTree {
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
		
		ArrayList<ITn> roots = new ArrayList<ITn>(); 
		root = roots; 
		int bneed = 0; 
		while(!pathtype.isEmpty()){
			int i = 0, k = -1; 
			for(IndexBooleanArray links : pathtype){
				i++; k = 0; 
				for(int j = 0; j < links.arr.length; j++)
					if(links.arr[j]) k++; 
				if(k == bneed) break; //we found a link
			}
			if(k == bneed){//we have a node, its of the right order, now we need to its home
				IndexBooleanArray links = pathtype.remove(k);
				SSPath thePath = paths.get(links.index);
				if(roots.isEmpty()) roots.add(new ITn(thePath, links.index)); 
				else {
					LinkedList<ITn> que = new LinkedList<ITn>(roots);
					while(!que.isEmpty()){
						ITn list = que.remove();
						boolean has = false; 
						for(ITn ml : list.children)
							if(links.has(ml.index)){
								has = true;
								ml.children.add(new ITn(thePath, links.index));
								break; 
							}//iterate through all elements in the LinkedList
						if(!has) que.addAll(list.children);
						else break; 
					}
				}
			} else if(k == -1){
				System.out.println("Empty Slice List"); 
			} else {
				bneed++; 
			}
		}
	}
}
