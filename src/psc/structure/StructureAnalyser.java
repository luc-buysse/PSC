package psc.structure;

import java.util.*;

import clustering.*;
import psc.SentenceTree;

public class StructureAnalyser {
	public static class StructureStatistics {
		
	}
	
	static private int hmax;
	static private ArrayList<SentenceTree> ts;
	
	public static void computeClusters(ArrayList<SentenceTree> ts, int hmax) {
		double[][] 	mat = new double[ts.size()][ts.size()];
		StructureAnalyser.hmax = hmax;
		StructureAnalyser.ts = ts;
		
		for(int i = 0; i < ts.size() ; i++) {
			for(int j = i ; j < ts.size() ; j++) {
				if(i == j) 
					mat[i][i] = 0;
				else {
					mat[i][j] = EditDistance.btw(ts.get(i), ts.get(j), hmax);
					mat[j][i] = mat[i][j];
				}
			}
			System.out.print(2. * i / ts.size());
			System.out.print(" " + String.valueOf(Abreviator.reduce(ts.get(i).getRoot()).length) + " ");
			System.out.println(" : " + ts.get(i).getSentence());
			
		}
		
		System.out.println("Number of sentences : " + String.valueOf(ts.size() * ts.size()));
		
		String[] names = new String[ts.size()];
		
		for(int i = 0 ; i < ts.size() ; i++) {
			names[i] = String.valueOf(i);
		}
		
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster root = alg.performClustering(mat, names, new CompleteLinkageStrategy());
		
		ArrayList<ArrayList<SentenceTree>> res = new ArrayList<ArrayList<SentenceTree>>();
		
		add(root, res);
		
		for(ArrayList<SentenceTree> cluster : res) {
			System.out.println("\n\nCluster : \n");
			
			for(SentenceTree t : cluster) {
				System.out.println(t.getSentence());
			}
		}
	}
	
	private static void add(Cluster root, ArrayList<ArrayList<SentenceTree>> res) {
		Distance d = null;
		d = root.getDistance();
		if(d.getDistance() < hmax) {
			List<String> leaf_names = root.getLeafNames();
			ArrayList<SentenceTree> leaves = new ArrayList<SentenceTree>();
			
			for(String n : leaf_names) {
				int i = Integer.valueOf(n);
				
				leaves.add(ts.get(i));
			}
			
			res.add(leaves);
		}
		
		for(Cluster child : root.getChildren()) {
			add(child, res);
		}
	}
}
