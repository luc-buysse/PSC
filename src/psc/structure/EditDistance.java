package psc.structure;
import java.util.*;

import lapjv.lapjv;
import psc.SentenceNode;
import psc.SentenceTree;

public class EditDistance {
	private static Node[] t1;
	private static Node[] t2;
	
	private static int hmax;
	
	private static class Pair {
		Pair(int i, int j) {
			first = i;
			second = j;
		}
		
		int first;
		int second;
	}
	
	private static class Matching {
		public HashMap<Integer, Integer> h;
		public ArrayList<Pair> l;
		
		public int key;
		
		Matching (){
			h = new HashMap<Integer, Integer>();
			l = new ArrayList<Pair>();
		}
		
		public void add(Pair p) {
			l.add(p);
			h.put(p.first, p.second);
		}
		
		public void completeJ() {
			HashSet<Integer> res = new HashSet<Integer>();
			
			for(int i = 0; i < t2.length ; i++) {
				res.add(i);
			}
			
			for(int j : h.values()) {
				res.remove(j);
			}
			
			for(int i : res) {
				l.add(new Pair(-1, i));
			}
		}
		
		Matching(Matching m) {
			h = new HashMap<Integer, Integer>();
			l = new ArrayList<Pair>();
			
			for(Pair p : m.l) {
				l.add(p);
				h.put(p.first, p.second);
			}
		}
		
		// DEBUG
		public void print() {
			System.out.println("Key : " + String.valueOf(key));
			
			for(Pair p : l) {
				String s1;
				if(p.first == -1)
					s1 = "-1";
				else
					s1 = t1[p.first].form;
				
				String s2;
				if(p.second == -1)
					s2 = "-1";
				else
					s2 = t2[p.second].form;
				
				System.out.println(s1 + "  " + s2 + " " + String.valueOf(cost(p.first, p.second)));
			}
		}
	}
	
	private static class pqCmp implements Comparator<Matching> {
	
		@Override
		public int compare(Matching a, Matching b) {
			return a.key - b.key;
		}
	}

	private static PriorityQueue<Matching> pq;
	
	private static int importance(Node n) {
		switch(n.deps) {
		case "root": return 0;
		case "suj" : return 1;
		case "obj" : return 2;
		case "dep_coord" : return 3;
		case "sub" : return 4;
		case "mod" : return 5;
		case "mod_rel" : return 6;
		case "ats":		return 7;
		default: return Integer.MAX_VALUE;
		}
	}
	
	private static boolean authorize(int i1, int i2) {
		Node n1 = t1[i1];
		Node n2 = t2[i2];
		return n1.pos.equals(n2.pos);
	}

	private static int loss(Node n) {
		switch(n.deps) {
		
		case "root":	return 10;
		case "suj":		return 10;
		case "obj":		return 10;
		case "dep_coord":return 10;
		case "sub":		return 10;
		// fus ?
		case "ats":		return 5;
		case "mod":		return 3;
		case "coord":	return 2;
		case "mod_rel":	return 1;
		case "det": 	return 1;
		
		case "aux_pass":return 0;
		case "aux_tps":	return 0;
		
		case "prep":	return 1;
		
		// ?
		case "aux_caus":return 0;
		case "dep":		return 1;
		case "aff":		return 1;
		case "_":		return 0;
		case "ato":		return 0;
		case "de_obj":	return 0;
		case "p_obj":	return 0;
		case "a_obj":	return 0;
		case "arg": 	return 0;
		case "comp": 	return 0;
		
		
		// suppr
		case "ponct":	return 0;
		default:	
			System.out.println("Cas non traité " + n.deps + " : " + n.form);
			return 0;
		}
	}

	private static int cost(int i1, int i2) {
		if((i1 == -1 && i2 == -1) || i1 >= t1.length || i2 >= t2.length) {
			System.out.println("Erreur dans les indices fournis à la fonction cost");
			return 0;
		}
		
		int ret = 0;
		
		if(i1 == -1) {
			ret = loss(t2[i2]);
		}
		else if(i2 == -1) {
			ret = loss(t1[i1]);
		}
		else if(t1[i1].deps.compareTo(t2[i2].deps) == 0) {
			ret = 0;
		}
		else {
			int min = Math.min(loss(t1[i1]), loss(t2[i2]));
			int diff = Math.max(loss(t1[i1]), loss(t2[i2])) - min;
			if(diff > min) {
				ret = diff;
			}
			else {
				ret = min;
			}
		}
		
		return ret;
	}

	private static int cost(Matching m) {
		int res = 0;
		
		for(Pair p : m.l) {
			res += cost(p.first, p.second);
		}
		
		return res;
	}

	private static int heuristics(Integer[] I, Integer[] J) {
		
		if(I.length == 0 && J.length == 0) {
			return 0;
		}
		
		int n = Math.max(I.length, J.length);
		
		int[][] mat = new int[n][n];
		
		for(int i = 0 ; i < n ; i++) {
			for(int j = 0 ; j < n ; j++) {
				if(j >= J.length) {
					mat[i][j] = cost(I[i], -1);
				}
				else if(i >= I.length) {
					mat[i][j] = cost(-1, J[j]);
				}
				else {
					mat[i][j] = cost(I[i], J[j]);
				}
			}
		}
		
		int[] match = lapjv.execute(mat);
		
		int res = 0;
		
		for(int i = 0 ; i < n ; i++) {
			res += mat[i][match[i]];
		}
		
		return res;
	}

	private static int btw(Node[] _t1, Node[] _t2) {
		try {
			pq = new PriorityQueue<Matching>(12, new pqCmp());
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		
		t1 = _t1;
		t2 = _t2;
		
		HashSet<Integer> l1 = new HashSet<Integer>();
		HashSet<Integer> l2 = new HashSet<Integer>();
		
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		ArrayList<Integer> a2 = new ArrayList<Integer>();
		
		for(int i = 1 ; i < t1.length ; i++) {
			a1.add(i);
		}
		
		for(int i = 1 ; i < t2.length ; i++) {
			a2.add(i);
		}
		
		Matching mc = new Matching();
		mc.add(new Pair(0,0));
		mc.key = heuristics(a1.toArray(new Integer[t1.length-1]), a2.toArray(new Integer[t2.length-1]));

		pq.add(mc);
		
		int counter = 0;
		
		while(!pq.isEmpty()) {
			
			counter++;
			
			Matching M = pq.poll();
			
			if(M.key > hmax) {
				return counter;
			}
			
			int i = 0;
			while(M.h.containsKey(i)) {
				i++;
			}
			if(i == t1.length) {
				M.completeJ();
				//return cost(M);
				return counter;
			}
			
			int k = -1;
			int l = -1;
			for(Pair p : M.l) {
				if(p.first > -1 && p.first + t1[p.first].numDes >= i && p.second > -1 && p.first > k) {
					k = p.first;
					l = p.second;
				}
			}
			if(k == -1) {
				System.out.println("Erreur : Aucun k n'a été trouvé");
				return 0;
			}
			
			l1.clear();
			l2.clear();
			
			{
				for(int j = 0 ; j < t1.length ; j++) 	l1.add(j);
				for(int j = 0 ; j < t2.length ; j++) 	l2.add(j);
				
				for( Pair p : M.l) {
					l1.remove(p.first);
					l2.remove(p.second);
				}
				
				for(int j = k ; j <= k + t1[k].numDes ; j++) 	l1.remove(j);
				for(int j = l ; j <= l + t2[l].numDes ; j++) 	l2.remove(j);
			}
			
			// PROBLEM
			int hp = heuristics(l1.toArray(new Integer[l1.size()]), l2.toArray(new Integer[l2.size()]));
			
			Matching M0 = new Matching(M);
			M0.add(new Pair(i, -1));
			
			l1.clear();
			l2.clear();
			
			{
				for(int j = k ; j <= k + t1[k].numDes ; j++) 	l1.add(j);
				for(int j = l ; j <= l + t2[l].numDes ; j++) 	l2.add(j);
				
				for( Pair p : M0.l) {
					l1.remove(p.first);
					l2.remove(p.second);
				}
			}
			
			int h0 = heuristics(l1.toArray(new Integer[l1.size()]), l2.toArray(new Integer[l2.size()])) + hp;
			
			M0.key = cost(M0) + h0;
			pq.add(M0);
			
			l2.clear();
			{
				
				for(int j = l ; j <= l + t2[l].numDes ; j++) {
					if(authorize(i, j))
						l2.add(j);
				}
				for( Pair p : M.l) {
					l2.remove(p.second);
				}
			}
			
			Matching[] Ms = new Matching[l2.size()];
			
			ArrayList<Integer> YbJ = new ArrayList<Integer>();
			for(int j : l2) YbJ.add(j);
			int ac = 0;
			for(int j : YbJ) {
				Ms[ac] = new Matching(M);
				Ms[ac].add(new Pair(i, j));
				
				int cid = t2[j].parentId;
				while(cid != l) {
					Ms[ac].add(new Pair(-1, cid));
					cid = t2[cid].parentId;
				}
				
				l1.clear();
				l2.clear();
				
				for(int ci = i ; ci <= t1[i].numDes ; ci++) l1.add(ci);
				for(Pair p : Ms[ac].l) {
					l1.remove(p.first);
				}
				
				for(int ci = j ; ci <= t2[j].numDes ; ci++) l2.add(ci);
				for(Pair p : Ms[ac].l) {
					l2.remove(p.second);
				}
				
				int h1 = heuristics(l1.toArray(new Integer[l1.size()]), l2.toArray(new Integer[l2.size()]));
				
				l1.clear();
				l2.clear();
				
				for(int ci = k ; ci <= t1[k].numDes ; ci++) l1.add(ci);
				for(int ci = i ; ci <= t1[i].numDes ; ci++) l1.remove(ci);
				for(Pair p : Ms[ac].l) l1.remove(p.first);
				
				for(int ci = l ; ci <= t2[l].numDes ; ci++) l2.add(ci);
				for(int ci = j ; ci <= t2[j].numDes ; ci++) l2.remove(ci);
				for(Pair p : Ms[ac].l) l2.remove(p.second);
				
				int h2 = heuristics(l1.toArray(new Integer[l1.size()]), l2.toArray(new Integer[l2.size()]));
				
				int hj = h1 + h2;
				
				Ms[ac].key = cost(Ms[ac]) + hj + hp;
				
				pq.add(Ms[ac]);
				
				ac++;
			}
		}
		
		return 0;
	}
	
	public static int btw(SentenceTree t1, SentenceTree t2, int hmax) {
		EditDistance.hmax = hmax;
		
		return btw(Abreviator.reduce(t1.getRoot()), Abreviator.reduce(t2.getRoot()));
	}

	public static int btw(SentenceTree t1, SentenceTree t2) {
		return btw(t1, t2, Integer.MAX_VALUE);
	}
}
