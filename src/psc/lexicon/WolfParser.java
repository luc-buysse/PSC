package psc.lexicon;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import psc.Settings;

public class WolfParser {
	public static boolean initialized = false;
	
	// Entrée lexicale par sa forme écrite
	// Un peu obsolete parce que plusieurs entrée lexicales peuvent exister pour le même mot, seulement pas pour le même couple (lemma, pos)
	// Il vaut mieux utiliser wordFromId
	public static HashMap<String, LexicalEntry> word = new HashMap<String, LexicalEntry>();
	// Son id
	public static HashMap<String, LexicalEntry> wordFromId = new HashMap<String, LexicalEntry>();
	
	// Sens par son identifiant
	public static HashMap<String, Sense> sense = new HashMap<String, Sense>();
	
	public static EnumMap<Relation.Type, Set<String>> relations = new EnumMap<Relation.Type, Set<String>>(Relation.Type.class);
	
	public static HashMap<String, Synset> synset = new HashMap<String, Synset>();
	
	private static class SaxHandler extends DefaultHandler {
		// stock les tructures en cours de construction
		private LexicalEntry currentWord;
		private Sense currentSense;
		private Relation currentRelation;
		private Synset currentSynset;
		
		@Override
		public void startElement(
				String uri,
				String localName,
				String qName,
				Attributes attributes) {
			
			if(qName.equalsIgnoreCase("LexicalEntry")) {
				currentWord = new LexicalEntry();
				currentWord.id = attributes.getValue("id");
			}
			if(qName.equalsIgnoreCase("Lemma")) {
				currentWord.writtenForm = attributes.getValue("writtenForm");
			}
			if(qName.equalsIgnoreCase("Sense")) {
				currentSense = new Sense();
				currentSense.id = attributes.getValue("id");
				currentSense.synset_id = attributes.getValue("synset");
				currentSense.word = currentWord;
			}
			if(qName.equalsIgnoreCase("SenseRelation")) {
				currentRelation = new Relation();
				currentRelation.target_id = attributes.getValue("target");
				currentRelation.type = Relation.getType(attributes.getValue("relType"));
				currentRelation.origin = currentSense;
				currentSense.relations.add(currentRelation);
			}
			if(qName.equalsIgnoreCase("Synset")) {
				String id = attributes.getValue("id");
				currentSynset = new Synset();
				currentSynset.id = id;
				synset.put(id, currentSynset);
				
				String[] wl = attributes.getValue("members").split(" ");
				currentSynset.members = wl;
				
				for(int i = 0 ; i < wl.length ; i++) {
					for(int j = i+1 ; j < wl.length; j++) {
						String w1 = wl[i], w2 = wl[j];
						if(w1.compareTo(w2) > 0) {
							relations.get(Relation.Type.SYNONYM).add(w2 + " " + w1);
						}
						else {
							relations.get(Relation.Type.SYNONYM).add(w1 + " " + w2);
						}
					}
				}
			}
			if(qName.equalsIgnoreCase("SynsetRelation")) {
				String reltype = attributes.getValue("relType");
				if(reltype.equals("hypernym")) {
					currentSynset.hyperId = attributes.getValue("target");
				}
			}
		}
		
		@Override
		public void endElement(
				String uri,
				String localName,
				String qName) {
			
			if(qName.equalsIgnoreCase("LexicalEntry")) {
				word.put(currentWord.writtenForm, currentWord);
				wordFromId.put(currentWord.id, currentWord);
			}
			
			else if(qName.equalsIgnoreCase("Sense")) {
				sense.put(currentSense.id, currentSense);
				currentWord.senses.add(currentSense);
			}
		}
		
		@Override
		public void characters(
				char[] ch,
				int start,
				int length) {
		}
	}
	
	public static void init() {
		// appelle SAX pour le parsing
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try {
			SAXParser parser = factory.newSAXParser();
			
			SaxHandler handler = new SaxHandler();
			
			for(Relation.Type t : Relation.Type.values()) {
				relations.put(t, new HashSet<String>());
			}
			
			parser.parse(Settings.wolf_path, handler);
			
			// On remplit le tableau de relations à partir des relations entre mots
			for(String w : wordFromId.keySet()) {
				LexicalEntry e = wordFromId.get(w);
				for(Sense s : e.senses) {
					for(Relation r : s.relations) {
						r.target = sense.get(r.target_id);
						String sw = r.target.word.id;
						String d;
						if(w.compareTo(sw) >= 0) {
							d = sw + " " + w;
						}
						else {
							d = w + " " + sw;
						}
						relations.get(r.type).add(d);
					}
				}
			}
			
			// On remplit les synonymes à partir des synsets communs
			HashSet<String> nr = new HashSet<String>();
			for(String unp : relations.get(Relation.Type.SYNONYM)) {
				String[] unpa = unp.split(" ");
				String id1 = unpa[0], id2 = unpa[1];
				String w1 = wordFromId.get(id1).writtenForm, w2 = wordFromId.get(id2).writtenForm;
				
				String d;
				if(w1.compareTo(w2) >= 0) {
					d = w2 + " " + w1;
				}
				else {
					d = w1 + " " + w2;
				}
				nr.add(d);
			}
			relations.put(Relation.Type.SYNONYM, nr);
			
			// On lie chaque mot à ses synsets
			for(String sid : synset.keySet()) {
				Synset s = synset.get(sid);
				
				if(s.hyperId != null && synset.containsKey(s.hyperId)) {
					s.hyper = synset.get(s.hyperId);
				}
				
				for(String lid : s.members) {
					LexicalEntry le = wordFromId.get(lid);
					le.synsets.add(s);
				}
			}
			
			// On ajoute les identifiant des "super" c'est à dire des représentant de leurs catégories
			for(LexicalEntry e : wordFromId.values()) {
				e.superIds = new String[e.synsets.size()];
				for(int i = 0 ; i < e.synsets.size() ; i++) {
					e.superIds[i] = e.synsets.get(i).getSuper();
				}
			}
			
			initialized = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
