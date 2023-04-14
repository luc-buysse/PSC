package psc.structure;

import java.util.ArrayList;

import psc.SentenceNode;

public class Abreviator {
	private static int reduce(SentenceNode root, int parentId, ArrayList<Node> res) {
		Node nroot = new Node(root);
		res.add(nroot);
		
		nroot.parentId = parentId;
		nroot.numDes = 0;
		
		int index = res.size() - 1;
		
		for(int i = 0 ; i < root.children.size() ; i++) {
			SentenceNode n = root.children.get(i);
			
			if(!merge(nroot, n)) {
				nroot.numDes += reduce(n, index, res);
			}
		}
		
		return nroot.numDes + 1;
	}
	
	Node[] input;
	
	private static boolean isVerb(Node root) {
		return root.pos.equals("V") || root.pos.equals("VIMP") || root.pos.equals("VINF") || root.pos.equals("VPP") || root.pos.equals("VPR") || root.pos.equals("VS");
	}
	
	private static boolean merge(Node root, SentenceNode child) {
		// Ponctuation
		if(child.pos.equals("PONCT")) {
			return true;
		}
		
		// Groupe nominaux
		if(root.pos.equals("NC") && child.pos.equals("ADJ")) {
			root.numAdj++;
			return true;
		}
		if(root.pos.equals("NC") && child.pos.equals("DET")) {
			root.numDet++;
			return true;
		}
		if(root.pos.equals("NC") && child.pos.equals("P")) {
			root.numPro++;
			return true;
		}
		
		// Participes passés
		if(root.pos.equals("VPP") && child.pos.equals("aux_tps")) {
			root.pos = "V";
			return true;
		}
		
		// Groupes verbaux
		if(isVerb(root) && child.deps.equals("mod")) {
			root.numMod++;
			return true;
		}
		
		return false;
	}
	
	public static Node[] reduce(SentenceNode root) {
		ArrayList<Node> res = new ArrayList<Node>();
		
		reduce(root, 0, res);
		
		return res.toArray(new Node[res.size()]);
	}
}
