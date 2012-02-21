package math.compress;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import math.compress.utils.BitInputStream;
import math.compress.utils.BitOutputStream;

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
	
	public static StatisticsTreeEntry buildTree(ArrayList<StatisticsEntry> items){
		StatisticsTreeEntry treeRoot = null, minLeaf1, minLeaf2;
		StatisticsEntry item1, item2;
		while (items.size() > 1){
			item1 = items.remove(0);
			item2 = items.remove(0);
			minLeaf1 = (item1 instanceof StatisticsTreeEntry)?(StatisticsTreeEntry) item1:new StatisticsTreeEntry(item1);
			minLeaf2 = (item2 instanceof StatisticsTreeEntry)?(StatisticsTreeEntry) item2:new StatisticsTreeEntry(item2);
			treeRoot = new StatisticsTreeEntry(minLeaf1, minLeaf2);
//			System.out.println(items.size()+" items, leaf1="+minLeaf1+", leaf2="+minLeaf2+", node="+treeRoot);
			items.add(treeRoot);
			Collections.sort(items); 
		}
		items.clear();
		return treeRoot;
	}
	private void addPrefix(char c){
		code=c+code;
		if (leftLeaf!=null)  leftLeaf.addPrefix(c);
		if (rightLeaf!=null) rightLeaf.addPrefix(c);
	}
	
//	public void printCodes(){
//		printLeafsCode(this);
//	}
	
 
//	private HTreeMap mHTreeMap;
	public HTreeMap fetchCodesMap(){
//		mHTreeMap = map;
		HTreeMap map = new HTreeMap(); 
		printLeafsCode(this, map);
		return map;
	}

	// TODO benchmark speed with HTreeMap in params and as a class property
	private void printLeafsCode(StatisticsTreeEntry node, HTreeMap mHTreeMap){
		if (node==null) return;
		if (node.leftLeaf==null && node.rightLeaf==null){ //this is a leaf
			mHTreeMap.add(node.getValue(), node.code);
//			System.out.print(node);
		} else { //this a node
			printLeafsCode(node.rightLeaf, mHTreeMap);
			printLeafsCode(node.leftLeaf, mHTreeMap);
		}
	}
	
	private StringBuffer bitsOutput;
	public void toBits(){
//		List<Boolean> bits = new ArrayList<Boolean>();
		BitOutputStream bos = null;
		try {
			bos = new BitOutputStream(new FileOutputStream("bitOut.txt"));
			bitsOutput = new StringBuffer();
			toBits(this, bos);
			bos.close();
			System.out.println("Tree in bits:\n"+bitsOutput.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		return bos.;
	}
	
	private void toBits(StatisticsTreeEntry node, BitOutputStream bos) throws IOException{
		if (node==null || bos==null) return;
		if (node.leftLeaf==null && node.rightLeaf==null){ //this is a leaf
//			mHTreeMap.add(node.getValue(), node.code);
//			System.out.print(node);
//			bits.add(false);	//is leaf
			bos.writeBit(0);
			bos.writeBits(node.getValue(), (short) 8);
			
			bitsOutput.append("0[");
			bitsOutput.append(node.getValue()+"] ");
		} else { //this a node
			bos.writeBit(1);	//this is Node
			bitsOutput.append("1 ");
			
			bos.writeBit(0); 	//parse Left child
			bitsOutput.append('0');
			toBits(node.rightLeaf, bos);
			bos.writeBit(1);	//parse Right child
			bitsOutput.append('1');
			toBits(node.leftLeaf, bos);
		}
	}
	
	public static StatisticsTreeEntry readTree(){
		try {
			BitInputStream bis = new BitInputStream(new FileInputStream("bitOut.txt"));
			StatisticsTreeEntry root = readNextNode(bis);
			bis.close();
			return root;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private static StatisticsTreeEntry readNextNode(BitInputStream bis) throws IOException{
		int b = bis.readBit(); //type of the node
		if (b == 0)	//is leaf. Reading value
			return new StatisticsTreeEntry(new StatisticsEntry(bis.readBits((short)8)));
		else {	//is Node,
			StatisticsTreeEntry left = null, right = null;
			b = bis.readBit(); //0 = left
			left = readNextNode(bis);
			b = bis.readBit(); //1 = right
			right = readNextNode(bis);
			return new StatisticsTreeEntry(right,left);
		}
	}
	
	
	@Override
	public String toString() {
		return super.toString()+" \""+code+"\"; ";
	}
//	public int countSubnotes(StatisticsTreeEntry entry){
//		if (entry==null) return 0;
//		int c = countSubnotes(entry.leftLeaf);
//		c += countSubnotes(entry.rightLeaf);
//		return c+1;
//	}
}