package psc;
import java.util.ArrayList;
import java.util.Arrays;

import org.w3c.dom.*;

import psc.lexicon.LexicalEntry;
import psc.lexicon.Relation;
import psc.lexicon.WolfParser;

import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class SentenceNode {
	public ArrayList<SentenceNode> children;
	
	public int id;
	public String form;
	public String lemma;
	public String pos;
	public String feats;
	public int head;
	public String deps;
	
	private boolean hasLexicalEntry = true;
	private LexicalEntry lexicalEntry;
	
	SentenceNode(	int id, 
					String form, 
					String lemma, 
					String pos, 
					String feats, 
					int head, 
					String deps) {
		this.id = id;
		this.form = form;
		this.lemma = lemma;
		this.pos = pos;
		this.feats = feats;
		this.head = head;
		this.deps = deps;
		
		children = new ArrayList<SentenceNode>();
	}
	
	public String getWolfId() {
		return "oewn-" + lemma + "-" + Head.toWolfType(pos);
	}
	
	public LexicalEntry getLexicalEntry() {
		if(hasLexicalEntry) {
			if(lexicalEntry == null) {
				String wolfId = getWolfId();
				if(hasLexicalEntry = WolfParser.wordFromId.containsKey(wolfId)) {
					lexicalEntry = WolfParser.wordFromId.get(wolfId);
				}
					
				return lexicalEntry;
			}
			else {
				return lexicalEntry;
			}
		}
		
		return null;
	}
	
	public int getDepth() {
		int result = 1;
		Iterator<SentenceNode> it = children.iterator();
		
		while(it.hasNext()) {
			SentenceNode child = it.next();
			int childDepth = child.getDepth();
			if(childDepth + 1 > result) {
				result = childDepth + 1;
			}
		}
		
		return result;
	}
	
	public static boolean areNeighbors(SentenceNode n1, SentenceNode n2) {
		String str_pair;
		if(n1.lemma.compareTo(n2.lemma) >= 0) {
			str_pair = n2.lemma + " " + n1.lemma;
		}
		else {
			str_pair = n1.lemma + " " + n2.lemma;
		}
		return WolfParser.relations.get(Relation.Type.DERIVATION).contains(str_pair);
	}
	
	public DefaultMutableTreeNode getTree() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(this.form + "(" + this.pos + "," + this.deps + ")");
		
		ListIterator<SentenceNode> it = children.listIterator();
		
		while(it.hasNext()) {
			SentenceNode n = it.next();
			top.add(n.getTree());
		}
		
		return top;
	}
	
	public String flatten() {
		String res = "";
		res += deps + " ( ";
		String[] flattenedChildren = new String[children.size()];
		
		int i = 0;
		ListIterator<SentenceNode> it = children.listIterator();
		while(it.hasNext()) {
			flattenedChildren[i] = it.next().flatten();
			i++;
		}
		Arrays.sort(flattenedChildren);
		
		for(String s : flattenedChildren) {
			res += s;
		}
		res += " ) ";
		
		return res;
	} 
	
	public int leavesCount() {
		int res = 0;
		ListIterator<SentenceNode> it = children.listIterator();
		
		while(it.hasNext()) {
			res += it.next().leavesCount();
		}
		
		return res > 0 ? res : 1;
	}
}