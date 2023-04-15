package psc;

class Statistics {
	public int neighCouples;
	public int sentenceCount;
	public double avgDepth, sdDepth;
	public double avgWidth;
	public int sumAdjAfter;
	public int sumAdjBefore;
	public int synEnumCount;
	public int synEnumAvgSize;
	public int supEnumCount;
	public int supEnumAvgSize;
	public int numSensations;
	public int numNoVerb;
	public int numComme;
	public int numWords;
	public int numImp;
	public int numPPAdj;
	public int numPPresAdj;
	public int numArt;
	
	@Override
	public String toString() {
		String res = "";
		res += "Number of neighbor couples," + neighCouples + "\n";
		res += "Number of sentences," + sentenceCount + "\n";
		res += "Average depth," + avgDepth + "\n";
		res += "Depth standart deviation," + sdDepth + "\n";
		res += "Average width," + avgWidth + "\n";
		res += "Adj Before," + String.valueOf(sumAdjBefore) + "\n";
		res += "After," + String.valueOf(sumAdjAfter) + "\n";
		res += "Enumeration of synonyms : \n";
		res += "Syn enum avg size," + synEnumAvgSize+ "\n";
		res += "Syn enum count," + synEnumCount + "\n";
		res += "Sup enum avg size," + supEnumAvgSize+ "\n";
		res += "Sup enum count," + supEnumCount + "\n";
		res += "Num sensations," + numSensations + "\n";
		res += "Num \"Comme\"," + numComme + "\n";
		res += "Number of sentences with no verbs," + numNoVerb + "\n";
		res += "Number of words," + numWords + "\n";
		res += "Num imp," + numImp + "\n";
		res += "Num PPAdj," + numPPAdj + "\n";
		res += "Num PPresAdj," + numPPresAdj + "\n";
		res += "Num Art," + numArt + "\n";
		return res;
	}
}