package psc.lexicon;

public class Synset {
	public String id;
	public Synset hyper;
	public String hyperId;
	
	public String[] members;
	
	public String getSuper() {
		if(hyper == null) {
			return id;
		}
		else {
			return hyper.getSuper();
		}
	}
	
	public String toString() {
		String res = id + " :";
		for(String m : members) {
			res += " " + m;
		}
		return res;
	}
}
