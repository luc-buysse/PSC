package psc.lexicon;
import java.util.ArrayList;

public class LexicalEntry {
	public String[] superIds;
	public ArrayList<Synset> synsets = new ArrayList<Synset>();
	public String id;
	public String writtenForm;
	public ArrayList<Sense> senses = new ArrayList<Sense>();
}
