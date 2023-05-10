package psc.lexicon;

public class Relation {
	public static Type getType(String s) {
		if(s.equals("antonym")) {
			return Type.ANTONYM;
		}
		else if(s.equals("derivation")) {
			return Type.DERIVATION;
		}
		else if(s.equals("pertainym")) {
			return Type.PERTAINYM;
		}
		else if(s.equals("also")) {
			return Type.ALSO;
		}
		else if(s.equals("synonym")) {
			return Type.SYNONYM;
		}
		else {
			return Type.UNKNOWN;
		}
	}
	
	static int num_types = 5;
	
	public enum Type {
		ANTONYM,
		DERIVATION,
		PERTAINYM,
		ALSO,
		SYNONYM,
		UNKNOWN
	}
	public Sense target;
	public String target_id;
	public Type type;
	
	public Sense origin;
}