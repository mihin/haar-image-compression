package math.compress;

import java.util.SortedMap;
import java.util.TreeMap;

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
		
		freqences = new int [LEVELS];
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
				freqences[b]++;
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
	private int [] freqences;
	private void doCompress(){
		//sort by freqs
		int [] sort_freqs = sortByFreqs(freqences.clone());
		//build tree
//		java.util.Collections.checkedSortedMap(m, keyType, valueType)
	TreeMap map = new TreeMap<String, String>();
	map.
	};
		//convert quant array to bits
		
		cleanUp();
	}

	private int[] sortByFreqs(int[] freqences2) {
		// TODO Auto-generated method stub
		return null;
	}

	private void cleanUp() {
		for (int i=0; i<LEVELS;i++) freqences[i]=0;
	}
}
