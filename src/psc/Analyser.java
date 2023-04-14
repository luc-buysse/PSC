package psc;
import java.io.BufferedReader;
import java.lang.runtime.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

import psc.lexicon.WolfParser;
import psc.structure.StructureAnalyser;
import psc.utils.Graph;

import java.util.*;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.StringReader;

public class Analyser {
	private static Statistics stats = new Statistics();
	private static ArrayList<SentenceTree> sentences;
	private static boolean initialized = false;
	
	private static int graphSpan;
	private static String fileName;
	
	public static void initialize(String file_path)  {
		if(!WolfParser.initialized) {
			System.out.println("Erreur : WolfParser n'a pas été initialisé");
			return;
		}
		
		try  
		{  
			System.out.println("Génération des structures d'arbre...");
			
			{
				fileName = (new File(file_path)).getName();
			}
			
			String content;
			
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(file_path));
	            content = new String(bytes, StandardCharsets.UTF_8);
			}
			catch(Exception e) {
				System.out.println("Fichier " + file_path + " inexistant.");
				return;
			}
            
			
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new StringReader(content));
			String line;
			
			sentences = new ArrayList<SentenceTree>();
			
			int line_index = 0;
			while((line = br.readLine()) != null) {
				if(line.length() == 0) {
					SentenceTree newSentenceTree = new SentenceTree(sb.toString());
					sentences.add(newSentenceTree);
					
					sb = new StringBuffer();
				}
				else {
					sb.append(line + "\n");
				}
				
				line_index++;
			}
			
			Scanner sc = new Scanner(new File(Settings.sensations_path));
			
			while(sc.hasNext()) {
				String cl = sc.nextLine();
				
				SentenceTree.sensations.add(cl);
			}
			
			sc.close();
			
			sc = new Scanner(new File(Settings.art_path));
			
			while(sc.hasNext()) {
				String cl = sc.nextLine();
				
				SentenceTree.artWords.add(cl);
			}
			
			sc.close();
			
			initialized = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Statistics globalAnalysis() {
		if(initialized) {
			stats.sentenceCount = sentences.size();
			
			graphSpan = stats.sentenceCount / 10;
			
			depthStats();
			widthStats();
			localSentenceStats();
		}
		else {
			System.out.println("L'analyseur n'a pas été initialisé.");
		}
		
		return stats;
	}
	
	public static void structuralAnalysis() 	{
		StructureAnalyser.computeClusters(sentences, 50);
	}

	public static int countNeighborCouples() {
		int count = 0;
		for(SentenceTree s : sentences) {
			count += s.countNeighborCouples();
		}
		return count;
	}
	
	public static void depthStats() {
		int sum0 = 0;
		int count = 0;
		
		ListIterator<SentenceTree> it = sentences.listIterator();
		while(it.hasNext()) {
			SentenceTree sentence = it.next();
			sum0 += sentence.getDepth();
			count++;
		}
		
		double avg = count != 0 ? (double) sum0 / count : 0;
		double sum1 = 0;
		it = sentences.listIterator();
		while(it.hasNext()) {
			SentenceTree sentence = it.next();
			sum1 += Math.pow(sentence.getDepth() - avg, 2);
		}
		
		double et = Math.sqrt(sum1 / count);
		stats.avgDepth = avg;
		stats.sdDepth = et;
	}
	
	public static void widthStats() {
		double sum = 0;
		int count = 0;
		
		ListIterator<SentenceTree> it = sentences.listIterator();
		while(it.hasNext()) {
			SentenceTree sentence = it.next();
			
			sum += (double) sentence.leavesCount() / (double) sentence.getDepth();
			count++;
		}
		
		stats.avgWidth = (double) sum / count;
	}
	
	public void printTopFlatStructures(int nb) {
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		
		ListIterator<SentenceTree> it1 = sentences.listIterator();
		while(it1.hasNext()) {
			String flat = it1.next().getRoot().flatten();
			if(counter.containsKey(flat)) {
				counter.put(flat, counter.get(flat) + 1);
			}
			else {
				counter.put(flat, 1);
			}
		}
		
		Set<Map.Entry<String, Integer>> entrySet = counter.entrySet();
		Map.Entry<String, Integer>[] ls = entrySet.toArray(new Map.Entry[entrySet.size()]); 
		
		Arrays.sort(ls, (a, b) -> b.getValue() - a.getValue());
		
		for(int i = 0 ; i < (nb < ls.length ? nb : ls.length ) ; i++) {
			System.out.println(Integer.toString(i) + ". " + ls[i]);
		}
	}
	
	public static void localSentenceStats() {
		stats.sumAdjAfter = 0;
		stats.sumAdjBefore = 0;
		stats.synEnumAvgSize = 0;
		stats.synEnumCount = 0;
		stats.supEnumCount = 0;
		stats.supEnumAvgSize = 0;
		stats.neighCouples = 0;
		stats.numSensations = 0;
		stats.numNoVerb = 0;
		stats.numComme = 0;
		stats.numWords = 0;
		stats.numImp = 0;
		stats.numPPAdj = 0;
		stats.numPPresAdj = 0;
		stats.numArt = 0;
		
		ArrayList<Integer> sensList = new ArrayList<Integer>();
		ArrayList<String> tmpNoVerbRow = new ArrayList<String>();
		Map<Integer, ArrayList<String[]>> noVerbRows = new HashMap<Integer, ArrayList<String[]>>();
		
		// Sum all local stats into global stats
		for(int i = 0 ; i < sentences.size() ; i++) {
			SentenceTree s = sentences.get(i);
			
			SentenceTree.LocalSentenceStatistics st = s.computeLocalStats();
			stats.sumAdjAfter += st.adjAfter;
			stats.sumAdjBefore += st.adjBefore;
			stats.synEnumCount += st.synEnumCount;
			stats.synEnumAvgSize += st.synEnumSize;
			stats.supEnumCount += st.supEnumCount;
			stats.supEnumAvgSize += st.supEnumSize;
			stats.neighCouples += st.neighCouples;
			stats.numSensations += st.numSensations;
			stats.numComme += st.numComme;
			stats.numWords += st.numWords;
			stats.numImp += st.numImp;
			stats.numPPAdj += st.numPPAdj;
			stats.numPPresAdj += st.numPPresAdj;
			stats.numArt += st.numArt;
			
			if(!st.hasVerb) {
				tmpNoVerbRow.add(s.getSentence());
				stats.numNoVerb++;
			}
			else {
				int rowSize = tmpNoVerbRow.size();
				if(rowSize > 0) {
					if(!noVerbRows.containsKey(rowSize)) {
						noVerbRows.put(rowSize, new ArrayList<String[]>());
					}
					
					noVerbRows.get(rowSize).add(tmpNoVerbRow.toArray(new String[rowSize]));
					tmpNoVerbRow = new ArrayList<String>();
				}
			}
			
			sensList.add(st.numSensations);
		}
		
		// Compute means
		stats.synEnumAvgSize = stats.synEnumAvgSize / (stats.synEnumCount > 0 ? stats.synEnumCount : 1);
		stats.supEnumAvgSize = stats.supEnumAvgSize / (stats.supEnumCount > 0 ? stats.supEnumCount : 1);
		
		Map<Object, Object> noVerbsGraphData = new HashMap<Object, Object>();
		for(Integer i : noVerbRows.keySet()) {
			noVerbsGraphData.put(i, (Integer) noVerbRows.get(i).size());
		}
		
		String name = fileName.substring(0, fileName.indexOf("."));
		
		// To build graphs
		Graph.average(name + ".sense", sensList, graphSpan);
		Graph.histogram(name + ".verbs", noVerbsGraphData);
	}
}

