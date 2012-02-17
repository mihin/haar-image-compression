package math.compress;

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

		processMatrix(m);
//		processMatrix(image.getMh());
//		processMatrix(image.getMd());
	}

	private void processMatrix(Matrix m) {
		final int rows = m.getRowsCount();
		int b=0;
		
		//matrix quantization and calculating frequences
		for (int i=0;i<rows;i++){
			for (int j=0;j<m.getColumnsCount();j++){
				b = quant(m.get()[i][j]);
				quantizied[i*rows + j] = b;
//				freqences[b]++;
				
				freqStat.push(b);
			}
		}
		
		//Huffman compression
		doCompress();
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
	private void doCompress(){
		System.out.println("doCompress, start, frequences:\n"+freqStat.toString());
		//sort by freqs
		freqStat.sort();
		System.out.println("doCompress, sorted, frequences:\n"+freqStat.toString());
		
		//build tree
		StatisticsTreeEntry treeRoot = freqStat.buildTree();

		//convert quant array to bits
//		treeRoot.printCodes();
		
		//get Map Value -> Code
		HTreeMap codes = new HTreeMap();
		treeRoot.fetchCodes(codes);
		
		System.out.println(codes);
		
		//process quatizied Matrix with H-Tree
		processCompression(codes);
		
		//clean
		cleanUp();
	}
	private void processCompression(HTreeMap codes){
		assert quantizied!=null;
		boolean[] bits = null;
		for (int i=0; i<quantizied.length; i++){
			try {
				bits = codes.getBits(quantizied[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
				
	}

	private void cleanUp() {
//		for (int i=0; i<LEVELS;i++) freqences[i]=0;
		freqStat.free();
		System.gc();
	}
}
