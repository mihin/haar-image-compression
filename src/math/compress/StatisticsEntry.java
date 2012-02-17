package math.compress;

public class StatisticsEntry implements Comparable<StatisticsEntry>{
	private int value = -1;
	protected int frequency = 0;
	public StatisticsEntry(int val){
		value = val;
	}
	protected StatisticsEntry(StatisticsEntry entry){
		value = entry.value;
		frequency = entry.frequency;
	}
	public void inc(){
		frequency++;
	}
	@Override
	public int compareTo(StatisticsEntry o) {
		return frequency - o.getFrequency();
	}
	public int getValue() {
		return value;
	}
	public int getFrequency() {
		return frequency;
	}
	@Override
	public String toString() {
		return value+"("+frequency+")";
	}
	
	
}

class StatisticsTreeEntry extends StatisticsEntry {
	/**
	 * Leafs, used to assemble code=-string
	 * @param entry
	 */
	public StatisticsTreeEntry(StatisticsEntry entry) {
		super(entry);
		leftLeaf = rightLeaf = null;
	}
	
	private String code = "";
	private StatisticsTreeEntry leftLeaf, rightLeaf;
	/**
	 * Nodes, used to build tree, that recursively concatinate codes 
	 * @param left
	 * @param right
	 */
	public StatisticsTreeEntry (StatisticsTreeEntry left, StatisticsTreeEntry right) {
		super(-1);
		leftLeaf = left;
		rightLeaf = right;
		
		leftLeaf.addPrefix('1');
		rightLeaf.addPrefix('0');
		
		frequency = left.frequency + right.frequency;
	}
	private void addPrefix(char c){
		code=c+code;
		if (leftLeaf!=null)  leftLeaf.addPrefix(c);
		if (rightLeaf!=null) rightLeaf.addPrefix(c);
	}
	
//	public void printCodes(){
//		printLeafsCode(this);
//	}
	
	private HTreeMap mHTreeMap;
	public void fetchCodes(HTreeMap map){
		mHTreeMap = map;
		printLeafsCode(this);
	}
	
	public void printLeafsCode(StatisticsTreeEntry node){
		if (node==null) return;
		if (node.leftLeaf==null && node.rightLeaf==null){ //this is a leaf
			mHTreeMap.add(node.getValue(), node.code);
//			System.out.print(node);
		} else { //this a node
			printLeafsCode(node.rightLeaf);
			printLeafsCode(node.leftLeaf);
		}
	}
	@Override
	public String toString() {
		return super.toString()+" \""+code+"\"; ";
	}
	
	public int countSubnotes(StatisticsTreeEntry entry){
		if (entry==null) return 0;
		int c = countSubnotes(entry.leftLeaf);
		c += countSubnotes(entry.rightLeaf);
		return c+1;
	}
}