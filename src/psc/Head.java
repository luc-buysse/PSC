package psc;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import psc.lexicon.*;
import psc.structure.*;
import psc.utils.TextCleaner;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class Head {
	
	public static String toWolfType(String t) {
		if(t.equals("VPP")) {
			return "v";
		}
		else if(t.equals("V")) {
			return "v";
		}
		else if(t.equals("VINF")) {
			return "v";
		}
		else if(t.equals("NC")) {
			return "n";
		}
		else if(t.equals("ADJ")) {
			return "a";
		}
		else if(t.equals("ADV")) {
			return "r";
		}
		else {
			return null;
		}
	}
	
	public static void makeSetOfWords() {
		HashSet<String> map = new HashSet<String>();
		File dir = new File(Settings.corpus_parsed);
		File type_output_file = new File(Settings.sow_type_output);
		File output_file = new File(Settings.sow_output);
		
		if(dir.exists() && dir.isDirectory()) {
			try {
				FileWriter fos = new FileWriter(output_file);
				FileWriter fost = new FileWriter(type_output_file);
				
				for(File file : dir.listFiles()) {
					String name = file.getName();
					
					System.out.println("Nom du fichier a ajouter : " + name);
					
					String ext = name.substring(name.lastIndexOf('.') + 1, name.length());
					
					if(ext.equals("txt")) {
						
						Scanner fis = new Scanner(file);
						
						while(fis.hasNext()) {
							String line = fis.nextLine();
							String ws[] = line.split("\t");
							
							String type = null;
							if(ws.length > 3) {
								type = toWolfType(ws[3]);
							}
							
							if(type != null) {
								map.add(ws[2] + "\t" + type);
							}
						}
						
						fis.close();
					}
				}
				
				for(String s : map) {
					String[] comb = s.split("\t");
					fos.write(comb[0] + "\n");
					fost.write(comb[1] + "\n");
				}
				
				fos.close();
				fost.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	public static void main(String[] __unused__) throws Throwable, IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(System.in));
		
		while(true) {
			System.out.print(Settings.working_dir + "~");
			
			String args[] = br.readLine().split(" ");
			
			if(args.length > 0) {
				String cmd = args[0];
				
				switch(cmd) {
				
				// Nettoie le corpus : clean_corpus corpus_directory output_directory
				case "clean_corpus":
				{
					if(args.length >= 2) {
						String dir_path = Settings.working_dir + args[1];
						String output_dir = Settings.working_dir + args[2];
						File dir = new File(dir_path);
						
						if(dir.exists()) {
							if(dir.isDirectory()) {
								File files[] = dir.listFiles();
								for(File file : files) {
									TextCleaner.parse(file.getPath(), output_dir);
								}
							}
							else {
								System.out.println("Veuillez fournir un dossier \n");
							}
						}
						else {
							System.out.println("Ce dossier/fichier n'existe pas \n");
						}
					}
					else {
						System.out.println("Nombre d'arguments incorrect");
					}
				}
					break;
					
				// Nettoie un texte : clean_text text_path output_directory
				case "clean_text":
				{
					if(args.length >= 2) {
						String in_file_path = Settings.working_dir + args[1];
						String output_dir = Settings.working_dir + args[2];
						File file = new File(in_file_path);
						
						TextCleaner.parse(file.getPath(), output_dir);
					}
					else {
						System.out.println("Nombre d'arguments incorrect");
					}
				}
					break;
				
				// Génère les fichier sow_fr.txt et sow_types.txt
				case "makeSOW":
				{
					makeSetOfWords();
				}
					break;
					
				// Charge le wordnet en mémoire, nécessaire avant de lancer une analyse
				case "parseWolf":
				{
					System.out.println("Parsing Wolf...");
					WolfParser.init();
					
					System.out.println("Number of Words : " + String.valueOf(WolfParser.word.size()));
					System.out.println("Number of Senses : " + String.valueOf(WolfParser.sense.size()));
					
					for(Relation.Type t : Relation.Type.values()) {
						System.out.println("Number of Relations of type " + t.toString() + " : " + String.valueOf(WolfParser.relations.get(t).size()));
					}
				}
					break;
				
				// Lance une analyse sur un fichier : analyze [-structure | -global] file_path
				case "analyze":
				{
					if(args.length <= 2) {
						System.out.println("Nombre d'arguments incorrect");
						continue;
					}
					String name = args[1];
					if(name.charAt(0) == '-') {
						
						String opt = name.substring(1);
						name = args[2];
						
						Analyser.initialize(Settings.corpus_parsed + "/" + name);
						
						switch(opt) {
						case "global":
						{
							Statistics stats = Analyser.globalAnalysis();
							if(stats != null)
								System.out.println(stats);
						}
							break;
						case "structure":
							Analyser.structuralAnalysis();
							break;
						}
					}
				}
				break;
				
				// Lance une analyse sur un corpus : analyze [-structure | -global] file_path
				case "analyze_all":
				{
					if(args.length <= 1) {
						System.out.println("Nombre d'arguments incorrect");
						continue;
					}
					String name = args[1];
					
					File corpus_dir = new File(Settings.corpus_parsed);
					
					if(name.charAt(0) == '-' && corpus_dir.isDirectory()) {
						
						String opt = name.substring(1);
						
						FileWriter output = new FileWriter(new File(Settings.statistics_path));
						
						for(File cfile : corpus_dir.listFiles()) {
							Analyser.initialize(cfile.getAbsolutePath());
							
							switch(opt) {
							case "global":
							{
								Statistics stats = Analyser.globalAnalysis();
								if(stats != null)
									output.write("Fichier : " + cfile.getName() + " : \n");
									output.write(stats.toString());
									output.write("\n\n\n");
							}
								break;
							case "structure":
								Analyser.structuralAnalysis();
								break;
							}
						}
						
						output.close();
					}
				}
					break;
				
				// Vérifie si d'après le Wordnet traduit deux mots sont synonymes : are_syns word1 word2
				case "are_syns":
				{
					if(args.length < 3) {
						System.out.println("Incorrect number of arguments (" + String.valueOf(args.length - 1) + " instead of 2)");
						continue;
					}
					String m1 = args[1];
					String m2 = args[2];
					String d;
					
					if(m1.compareTo(m2) >= 0) {
						d = m2 + " " + m1;
					}
					else {
						d = m1 + " " + m2;
					}
					String res;
					if(WolfParser.relations.get(Relation.Type.SYNONYM).contains(d)) {
						res = " synonymous";
					}
					else {
						res = " not synonymous";
					}
					
					System.out.println(m1 + " and " + m2 + " are" + res);
				}
					break;
				
				// Vérifie si d'après le Wordnet traduit deux mots sont super synonymes : are_syns word1-type1 word2-type2
				case "are_super_syns":
				{
					if(args.length < 3) {
						System.out.println("Incorrect number of arguments (" + String.valueOf(args.length - 1) + " instead of 2)");
						continue;
					}
					String m1 = args[1];
					String m2 = args[2];
					String d;
					
					LexicalEntry a = WolfParser.wordFromId.get("oewn-" + m1), b = WolfParser.wordFromId.get("oewn-" + m2);
					if(a != null && b != null) {
						boolean are = false;
						
						for(String x : a.superIds) {
							for(String y : b.superIds) {
								if(x.equals(y)) {
									are = true;
								}
							}
						}
						
						System.out.println(m1 + " and " + m2 + " are" + (are ? "" : " not") + " synonymous");
					}
					else {
						System.out.println("Entree invalide");
					}
				}
					break;
				
				// Affiche l'ensemble des super synonymes d'un mot et leurs relations : print_supers word-type
				case "print_supers":
				{
					if(args.length < 2) {
						System.out.println("Incorrect number of arguments (" + String.valueOf(args.length - 1) + " instead of 2)");
						continue;
					}
					String m = args[1];
					String d;
					
					LexicalEntry a = WolfParser.wordFromId.get("oewn-" + m);
					if(a != null) {
						for(Synset s : a.synsets) {
							ArrayDeque<String> hyperList = new ArrayDeque<String>();
							while(s != null) {
								hyperList.add(s.toString());
								s = s.hyper;
							}
							Iterator<String> it = hyperList.descendingIterator();
							int lvl = 0;
							while(it.hasNext()) {
								for(int i = 0 ; i < lvl ; i++) {
									System.out.print("\t");
								}
								System.out.println(it.next());
								lvl++;
							}
						}
					}
					else {
						System.out.println("Entree invalide");
					}
				}
					break;
					
				// Permet de visualiser les structures des phrases du fichier out.txt dans le répertoire test
				// (répertoire à configurer dans le fichier Settings.java)
				case "test":
				{	
					Scanner fs = new Scanner(new File(Settings.test_path + "/out.txt"));
					
					ArrayList<SentenceTree> res = new ArrayList<SentenceTree>();
					
					StringBuffer sb = new StringBuffer();
					while(fs.hasNext()) {
						String line = fs.nextLine();
						if(line.length() > 1) {
							sb.append(line + "\n");
						}
						else {
							res.add(new SentenceTree(sb.toString()));
							sb = new StringBuffer();
						}
					}
					if(sb.length() > 0) {
						res.add(new SentenceTree(sb.toString()));
					}
					
					fs.close();
					
					for(SentenceTree st : res)
						st.showGraph();
				}
					break;
				
				// Calcul la distance entre les deux premières phrases du fichier out.txt dans le répertoire test
				case "dist":
				{
					Scanner fs = new Scanner(new File(Settings.test_path + "/out.txt"));
					
					ArrayList<SentenceTree> res = new ArrayList<SentenceTree>();
					
					StringBuffer sb = new StringBuffer();
					while(fs.hasNext()) {
						String line = fs.nextLine();
						if(line.length() > 1) {
							sb.append(line + "\n");
						}
						else {
							res.add(new SentenceTree(sb.toString()));
							sb = new StringBuffer();
						}
					}
					if(sb.length() > 0) {
						res.add(new SentenceTree(sb.toString()));
					}
					
					fs.close();
					
					System.out.println("Comparaison de :");
					System.out.println(res.get(0).getSentence());
					System.out.println(res.get(1).getSentence());
					
					res.get(0).showGraph();
					res.get(1).showGraph();
					
					System.out.println("Resultat : " + String.valueOf(EditDistance.btw(res.get(0), res.get(1), 50)));
				}
					break;
				
				// Permet de tester la réduction des phrases sur les phrases de out.txt dans le répertoire test
				case "reduce_test":
					Scanner fs = new Scanner(new File(Settings.test_path + "/out.txt"));
					
					ArrayList<SentenceTree> res = new ArrayList<SentenceTree>();
					
					StringBuffer sb = new StringBuffer();
					while(fs.hasNext()) {
						String line = fs.nextLine();
						if(line.length() > 1) {
							sb.append(line + "\n");
						}
						else {
							res.add(new SentenceTree(sb.toString()));
							sb = new StringBuffer();
						}
					}
					if(sb.length() > 0) {
						res.add(new SentenceTree(sb.toString()));
					}
					
					fs.close();
					
					for(SentenceTree st : res) {
						psc.structure.Node[] nodes = Abreviator.reduce(st.getRoot());
						
						for(psc.structure.Node n : nodes) {
							System.out.println(n);
						}
						
					}
					break;
				default:
					System.out.println("Commande inconnue");
					break;
				}
			}
			else {
				System.out.println("Commande non reconnue \n");
			}
		}
	}
}
