package math.compress;

public class StatisticsEntry implements Comparable<StatisticsEntry>{
	private int value = -1;
	protected int frequency = 0;
	public StatisticsEntry(int val){
		value = val;
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
		super(entry.getValue());
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
		
		frequency = left.frequency+right.frequency;
	}
	private void addPrefix(char c){
		code+=c;
		leftLeaf.addPrefix(c);
		rightLeaf.addPrefix(c);
	}
	
	public void printCodes(){
		printLeafsCode(this);
	}
	public void printLeafsCode(StatisticsTreeEntry node){
		if (node==null) return;
		if (leftLeaf==null && rightLeaf==null){ //this is a leaf
			System.out.print(getValue()+" "+code+"; ");
		} else { //this a node
			printLeafsCode(leftLeaf);
			printLeafsCode(rightLeaf);
		}
	}
}