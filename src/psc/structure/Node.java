package psc.structure;

import psc.SentenceNode;

public class Node {
	String form;
	String deps;
	String pos;
	
	// NC
	int numAdj;
	int numDet;
	int numPro;
	
	// VB
	int numMod;
	
	int numDes;
	int parentId;
	
	Node(SentenceNode n) {
		form = n.form;
		deps = n.deps;
		pos = n.pos;
		
		numDes = -1;
		parentId = -1;
	}
	
	public String toString() {
		return form + "(" + pos + ", " + deps + ")";
	}
}