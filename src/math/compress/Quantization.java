package math.compress;

import java.util.ArrayList;
import java.util.List;

import math.dwt.DWTCoefficients;
import math.dwt.Matrix;

public class Quantization {
	private final int SHIFT = 256; 
	private final int MAX_VAL = 2*SHIFT; 
	
	private int LEVELS = 2*16;
	private int DIVIDER = MAX_VAL/LEVELS;
	public Quantization(int levels){
		LEVELS = levels;
		DIVIDER = MAX_VAL/LEVELS;
		
//		freqences = new int [LEVELS];
		freqStat = new FreqStatistics(LEVELS);
	}
	
	private int [] quantizied;
	public void process(DWTCoefficients image){
		Matrix m = image.getMv();
		quantizied = new int [m.getColumnsCount()*m.getRowsCount()];

		//quatization
		processMatrixQuatization(m);
		
		//Huffman compression
		List<Boolean> haffmanCodes = doCompress();

		//print Haffman code
		StringBuffer sb = new StringBuffer();
		for (Boolean b:haffmanCodes)sb.append(b?'1':'0');
		System.out.println("Haffman encoded values:\n"+sb.toString());
		
		
		System.out.println("\nDecompression. restoring Haffman tree..");
		//read HTree
		StatisticsTreeEntry mHTree = parseHTree();

		//get Map Value -> Code
		HTreeMap codesTree = mHTree.fetchCodesMap();
		System.out.println(codesTree);
		
		//decode Matrix from Haffman codes
		try {
			Matrix decodedMatrix = decodeMatrix(m.getRowsCount(), m.getColumnsCount(), haffmanCodes, codesTree);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		haffmanCodes.clear();
//		processMatrix(image.getMh());
//		processMatrix(image.getMd());
	}

	private void processMatrixQuatization(Matrix m) {
		final int rows = m.getRowsCount();
		int b=0;
		
		//Quantization and calculating frequences
		for (int i=0;i<rows;i++){
			for (int j=0;j<m.getColumnsCount();j++){
				b = quant(m.get()[i][j]);
				quantizied[i*rows + j] = b;
				freqStat.push(b);
			}
		}
	}

	private int quant(float f) {
		f = f + SHIFT;
		if (f < 0) f = 0;						//f = Min(); f = Max()
		else if (f >= MAX_VAL) f = MAX_VAL-1;
		return Math.round(f / DIVIDER);
	}
	
	
	/* Huffman compression */
//	private int [] freqences;
	private FreqStatistics freqStat;
	private List<Boolean> doCompress(){
		//sort by freqs
		freqStat.sort();
		System.out.println("doCompress, sorted, frequences:\n"+freqStat.toString());
		
		//build tree
		StatisticsTreeEntry treeRoot = freqStat.buildTree();

		//get Map Value -> Code
		HTreeMap codesTree = treeRoot.fetchCodesMap();
		System.out.println(codesTree);
		
		//process quantizied Matrix with H-Tree, ?and compress
		List<Boolean> res = processCompression(codesTree);
//		System.out.println("Decoded by Haffman, bytes ");
		
		//TODO Convert HTree to output format
		treeRoot.toBits();
		
		//TODO assemble formated HTree and compressed HCode
		
		//clean - everything except assembled Tree and compressed data
		cleanUp();		
		
		return res;
	}
	private List<Boolean> processCompression(HTreeMap codes){
		assert quantizied!=null;
//		StringBuffer sb = new StringBuffer();
//		boolean[] bits = null;
		List<Boolean> bits = null;
		List<Boolean> haffmanCode = new ArrayList<Boolean>();
		for (int i=0; i<quantizied.length; i++){
			bits = codes.getBits(quantizied[i]);
			haffmanCode.addAll(bits);
			bits.clear();
		}
		// TODO ?compress haffmanCode 
		
		return haffmanCode;
	}

	private void cleanUp() {
		freqStat.free();
		System.gc();
	}
	
	/* decompression*/
	private StatisticsTreeEntry parseHTree(){
		return StatisticsTreeEntry.readTree();
	}

	private Matrix decodeMatrix(int rowsCount, int columnsCount, List<Boolean> haffmanCodes, HTreeMap codesTree) throws Exception {
		String code = "";
		int [] values = new int [rowsCount*columnsCount];
		int index=0, val;
		for (Boolean b:haffmanCodes){
			code+=(b?'1':'0');		
			if ((val = codesTree.getValue(code))<0){
				if (val == -2) throw new Exception("The bits consequence not found in Hafmman tree: \""+code+"\"");
			} else {
				values[index++]= val;
				code="";
			}
		}
		Matrix m = new Matrix(rowsCount, columnsCount);
		return m;
	}
}
