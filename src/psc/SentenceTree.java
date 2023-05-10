package psc;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.w3c.dom.*;

import psc.lexicon.LexicalEntry;
import psc.lexicon.Relation;
import psc.lexicon.WolfParser;


public class SentenceTree {
	public static HashSet<String> sensations = new HashSet<String>();
	public static HashSet<String> artWords = new HashSet<String>();
	
	public class LocalSentenceStatistics {
		public int adjAfter = 0;
		public int adjBefore = 0;
		public int synEnumCount = 0;
		public int synEnumSize = 0;
		public int supEnumCount = 0;
		public int supEnumSize = 0;
		public int neighCouples = 0;
		public int numSensations = 0;
		public boolean hasVerb = false;
		public int numComme = 0;
		public int numWords = 0;
		public int numPPAdj = 0;
		public int numImp = 0;
		public int numPPresAdj = 0;
		public int numArt = 0;
		public int depth = 0;
		public float width = 0;
	}
	
	public static class Pair {
		String a, b;
		
		Pair(String _A, String _b) {
			a = _A;
			b = _b;
		}
		
		boolean test(SentenceNode n) {
			switch(a) {
			case "pos":
				return n.pos.equals(b);
			case "lemma":
				return n.lemma.equals(b);
			default:
				System.out.println("Erreur format incompatible");
				return false;
			}
		}
	}
	
	private SentenceNode[] wordList;
	private String raw;
	private String sentence;
	
	public SentenceTree(String raw) {
		initFromRaw(raw);
	}
	
	private void initFromRaw(String raw) {
		this.raw = raw;
		
		String[] lines = raw.split("\n");
		
		wordList = new SentenceNode[lines.length + 1];
		wordList[0] = new SentenceNode(0, "#RACINE#","#RACINE#","","", 0, "");
		ArrayList<Integer>[] treeSketch = new ArrayList[lines.length+1];
		
		for(int i = 0 ; i < lines.length + 1 ; i++) {
			treeSketch[i] = new ArrayList<Integer>();
		}
		
		StringBuffer sentenceBuffer = new StringBuffer();
		
		for(int i = 1 ; i < lines.length + 1 ; i++) {
			String[] properties = lines[i-1].split("\t");
			
			sentenceBuffer.append(properties[1] + " ");
			
			SentenceNode newNode = new SentenceNode(Integer.parseInt(properties[0]), properties[1], properties[2], properties[4], properties[5], Integer.parseInt(properties[6]), properties[7]);
			
			if(newNode.head >= treeSketch.length) {
				System.out.println("Ligne " + String.valueOf(i) + " problème :" + lines[i-1]);
			}
			treeSketch[newNode.head].add(i);
			
			wordList[i] = newNode;
		}
		
		sentence = sentenceBuffer.toString();
		
		for(int i = 0 ; i < lines.length ; i++) {
			Iterator<Integer> it = treeSketch[i].iterator();
			while(it.hasNext()) {
				wordList[i].children.add(wordList[it.next()]);
			}
		}
		
	}
	
	public int countNeighborCouples() {
		int  count = 0;
		
		return count;
	}
	
	public LocalSentenceStatistics computeLocalStats() {
		LocalSentenceStatistics stats = new LocalSentenceStatistics();
		
		for(SentenceNode w : wordList) {
			if(w.pos.equals("NC")) {
				for(SentenceNode c : w.children) {
					if(c.pos.equals("ADJ")) {
						if(c.id > w.id)
							stats.adjAfter += 1;
						else
							stats.adjBefore += 1;
					}
				}
			}
		}
		
		// Compute synonyms
		for(int i = 0 ; i < wordList.length ; i++) {
			int j, len = 0;
			for(j = i+1 ; j < wordList.length ; j++) {
				if(wordList[j].pos != "PONCT" && wordList[j].pos != "CC") {
					String tp;
					if(wordList[i].lemma.compareTo(wordList[j].lemma) >= 0) {
						tp = wordList[j].lemma + " " + wordList[i].lemma;
					}
					else {
						tp = wordList[j].lemma + " " + wordList[i].lemma;
					}
					if(!WolfParser.relations.get(Relation.Type.SYNONYM).contains(tp)) {
						break;
					}
					len++;
				}
			}
			if(len > 0) {
				stats.synEnumSize += len;
				stats.synEnumCount += 1;
				
				String top = "";
				for(int a = i ; a < j ; a++) {
					top += " " + wordList[a].form;
				}
			}
			i = j;
		}
		
		// Same for super synonyms
		for(int i = 0 ; i < wordList.length ; i++) {
			
			ArrayList<LexicalEntry> prev = new ArrayList<LexicalEntry>();
			LexicalEntry a0 = wordList[i].getLexicalEntry();
			if(a0 == null)
				continue;
			prev.add(a0);
			int j, len = 0;
			boolean go = true;
			for(j = i+1 ; j < wordList.length && go ; j++) {
				LexicalEntry a = wordList[j].getLexicalEntry();
				if(a != null) {
					go = false;
					for(LexicalEntry b : prev) {
						for(String x : a.superIds) {
							for(String y : b.superIds) {
								if(x.equals(y)) {
									len++;
									go = true;
								}
							}
						}
					}
					if(go)
						prev.add(a);
				}
			}
			if(len > 0) {
				stats.supEnumSize += len;
				stats.supEnumCount += 1;
				
				String top = "";
				for(int a = i ; a < j ; a++) {
					top += " " + wordList[a].form;
				}
			}
			i = j;
		}
		
		for(int i = 1 ; i < wordList.length ; i++) {
			for(int j = i+1 ; j < wordList.length ; j++) {
				String d;
				String m1 = wordList[i].getWolfId(), m2 = wordList[j].getWolfId();
				if(m1.compareTo(m2) >= 0) {
					d = m2 + " " + m1;
				}
				else {
					d = m1 + " " + m2;
				}
				
				boolean foundNewNeighCouple = WolfParser.relations.get(Relation.Type.DERIVATION).contains(d);
				stats.neighCouples += foundNewNeighCouple ? 1 : 0;
			}
		}
		
		for(SentenceNode n : wordList) {
			if(sensations.contains(n.lemma)) {
				stats.numSensations++;
			}
		}
		
		for(SentenceNode n : wordList) {
			if(artWords.contains(n.lemma)) {
				stats.numArt++;
			}
		}
		
		for(SentenceNode n : wordList) {
			if(n.pos.equals("V") || n.pos.equals("VIMP") || n.pos.equals("VS")) {
				stats.hasVerb = true;
			}
		}
		
		for(SentenceNode n : wordList) {
			if(n.lemma.equalsIgnoreCase("comme")) {
				stats.numComme++;
			}
		}
		
		for(SentenceNode n : wordList) {
			if(!n.pos.equals("PONCT")) {
				stats.numWords++;
			}
		}
		
		ArrayList<ArrayList<Pair>> tests = new ArrayList<ArrayList<Pair>>();
		ArrayList<Pair> test0 = new ArrayList<Pair>();
		ArrayList<Pair> test1 = new ArrayList<Pair>();
		ArrayList<Pair> test2 = new ArrayList<Pair>();
		ArrayList<Pair> test3 = new ArrayList<Pair>();
		ArrayList<Pair> test4 = new ArrayList<Pair>();
		ArrayList<Pair> test5 = new ArrayList<Pair>();
		ArrayList<Pair> test6 = new ArrayList<Pair>();
		ArrayList<Pair> test7 = new ArrayList<Pair>();
		ArrayList<Pair> test8 = new ArrayList<Pair>();
		ArrayList<Pair> test9 = new ArrayList<Pair>();
		
		test0.add(new Pair("lemma", "il"));
		test0.add(new Pair("lemma", "y"));
		test0.add(new Pair("lemma", "avoir"));
		
		test1.add(new Pair("lemma", "il"));
		test1.add(new Pair("lemma", "ne"));
		test1.add(new Pair("lemma", "y"));
		test1.add(new Pair("lemma", "avoir"));
		
		test2.add(new Pair("lemma", "y"));
		test2.add(new Pair("lemma", "avoir"));
		test2.add(new Pair("lemma", "il"));
		
		test3.add(new Pair("lemma", "il"));
		test3.add(new Pair("lemma", "faire"));
		test3.add(new Pair("pos", "ADJ"));
		
		test4.add(new Pair("lemma", "il"));
		test4.add(new Pair("lemma", "convenir"));
		test4.add(new Pair("lemma", "de"));
		
		test5.add(new Pair("lemma", "il"));
		test5.add(new Pair("lemma", "se"));
		test5.add(new Pair("lemma", "agir"));
		
		test6.add(new Pair("lemma", "il"));
		test6.add(new Pair("lemma", "falloir"));
		
		test7.add(new Pair("lemma", "il"));
		test7.add(new Pair("lemma", "importer"));
		test7.add(new Pair("lemma", "que"));
		
		test8.add(new Pair("lemma", "ce"));
		test8.add(new Pair("lemma", "être"));
		
		test9.add(new Pair("lemma", "ce"));
		test9.add(new Pair("lemma", "ne"));
		test9.add(new Pair("lemma", "être"));
		
		tests.add(test0);
		tests.add(test1);
		tests.add(test2);
		tests.add(test3);
		tests.add(test4);
		tests.add(test5);
		tests.add(test6);
		tests.add(test7);
		tests.add(test8);
		tests.add(test9);
		
		ArrayList<Integer> stages = new ArrayList<Integer>();
		for(int i = 0 ; i < tests.size() ; i++) {
			stages.add(0);
		}
		
		for(int i = 0; i < wordList.length ; i++) {
			SentenceNode n = wordList[i];
			
			for(int j = 0 ; j < tests.size() ; j++) {
				ArrayList<Pair> current_test = tests.get(j);
				int current_stage = stages.get(j);
				
				if(current_stage >= current_test.size()) {
					stats.numImp++;
					stages.set(j, 0);
				}
				else {
					Pair p = current_test.get(stages.get(j));
					
					if(p.test(n)) {
						stages.set(j, current_stage + 1);
					}
					else {
						stages.set(j, 0);
					}
				}
			}
		}
		
		for(SentenceNode n : wordList) {
			if(n.pos.equals("VPP") && (n.deps.equalsIgnoreCase("ato") || n.deps.equalsIgnoreCase("ats"))) {
				stats.numPPAdj++;
			}
		}
		
		for(SentenceNode n : wordList) {
			if(n.lemma.endsWith("ant") && n.pos.equals("ADJ")) {
				stats.numPPresAdj++;
			}
		}
		
		stats.width = leavesCount() / getDepth();
		stats.depth = getDepth();
		
		return stats;
	}

	public void showGraph() {
		JFrame mainFrame = new JFrame("Graphique de phrase");
		mainFrame.setSize(300, 300);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JLabel originalSentence = new JLabel("Phrase originale : " + sentence);
		mainPanel.add(originalSentence, BorderLayout.NORTH);
		
		DefaultMutableTreeNode rootTree = wordList[0].getTree();
		JTree graphicTree = new JTree(rootTree);
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) graphicTree.getCellRenderer();
		renderer.setClosedIcon(null);
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setFont(new Font("Arial", Font.PLAIN, 24));
		
		for(int i = 0 ; i < graphicTree.getRowCount() ; i++) {
			graphicTree.expandRow(i);
		}
		
		JScrollPane treeScrollPane = new JScrollPane(graphicTree);
		mainPanel.add(treeScrollPane, BorderLayout.CENTER);
		
		mainFrame.add(mainPanel);
		mainFrame.setVisible(true);
	}

	public int leavesCount() {
		return wordList[0].leavesCount();
	}

	public String getSentence()
	{
		return sentence;
	}

	public int getDepth() {
		return wordList[0].getDepth();
	}

	public SentenceNode getRoot() {
		return wordList[0];
	}
}
