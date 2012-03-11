package math.compress;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatFlagsException;
import java.util.List;
import java.util.logging.Level;

import math.compress.utils.BitOutputStream;
import math.dwt.DWTCoefficients;
import math.dwt.Matrix;
import math.utils.Log;

public class Quantization {
	
	private final int SHIFT = 256; 
	private final int MAX_VAL = 2*SHIFT; 
	
	private int LEVELS = 2*16;
	private int DIVIDER = MAX_VAL/LEVELS;
	public Quantization(int levels){
		LEVELS = levels;
		DIVIDER = MAX_VAL/LEVELS;
	}
	
//	private int [] quantizied;
	public DWTCoefficients process(DWTCoefficients image){
		Matrix ma, mv, mh, md;
		mv = matrixCompressCycle(image.getMv());
		mh = matrixCompressCycle(image.getMh());
		md = matrixCompressCycle(image.getMd());
		
//		quantizied = null;
		System.gc();
		System.gc();
		return new DWTCoefficients(image.getMa(), mv, mh, md, null, false);
	}
	
	private Matrix matrixCompressCycle(Matrix m){
		FreqStatistics freqStat = new FreqStatistics(LEVELS);

		//quatization & statistics gathering
		int [] quantizied = processMatrixQuatization(m,freqStat);
		
		//Huffman compression
		List<Boolean> haffmanCodes = doCompress(freqStat,quantizied);

		// save/print Haffman code
		try {
			BitOutputStream bos = new BitOutputStream(new FileOutputStream("MVBits.txt"));
			StringBuffer sb = new StringBuffer();
			for (Boolean b:haffmanCodes){
				sb.append(b?'1':'0');
				bos.writeBit(b?1:0);
			}
			Log.get().log(Level.FINEST, "Haffman encoded values:\n"+sb.toString());			
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.get().log(Level.FINER,"\nDecompression.");
		//read HTree
		StatisticsTreeEntry mHTree = parseHTree();

		//get Map Value -> Code
		HTreeMap codesTree = mHTree.fetchCodesMap();
		Log.get().log(Level.FINER, "Loaded codes tree:\n"+codesTree.toString());
		
		//decode Matrix from Haffman codes
		Matrix decodedMatrix = null;
		try {
			decodedMatrix = decodeMatrix(m.getRowsCount(), m.getColumnsCount(), haffmanCodes, codesTree);
			String filename = "redMVHDecoded.txt";
			decodedMatrix.saveToFile(filename, "Huffman decoded");
			Log.get().log(Level.FINER, "Decoded matrix saved to file "+filename);
		} catch (IllegalFormatFlagsException e) {
			e.printStackTrace();
		}

		
		freqStat.free();
		haffmanCodes.clear();
		quantizied = null;
		return decodedMatrix;
	}

	private int [] processMatrixQuatization(Matrix m, FreqStatistics freqStat) {
		int [] quantizied = new int [m.getColumnsCount()*m.getRowsCount()];
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
		return quantizied;
	}

	private int quant(float f) {
		f = f + SHIFT;
		if (f < 0) f = 0;						//f = Min(); f = Max()
		else if (f >= MAX_VAL) f = MAX_VAL-1;
		return Math.round(f / DIVIDER);
	}
	private int unQuant(int q) {
		return q*DIVIDER-SHIFT;
	}
	
	
	/* Huffman compression */
//	private int [] freqences;
//	private FreqStatistics freqStat;
	private List<Boolean> doCompress(FreqStatistics freqStat,int [] quantizied){
		//sort by freqs
		freqStat.sort();
		Log.get().log(Level.FINEST, "doCompress, sorted, frequences:\n"+freqStat.toString());
		
		//build tree
		StatisticsTreeEntry treeRoot = freqStat.buildTree();

		//get Map Value -> Code
		HTreeMap codesTree = treeRoot.fetchCodesMap();
		Log.get().log(Level.FINER, codesTree.toString());
		
		//process quantizied Matrix with H-Tree, ?and compress
		List<Boolean> res = processCompression(codesTree,quantizied);
//		System.out.println("Decoded by Haffman, bytes ");
		
		//TODO Convert HTree to output format
		treeRoot.toBits();
		
		//TODO assemble formated HTree and compressed HCode
		
		return res;
	}
	private List<Boolean> processCompression(HTreeMap codes,int [] quantizied){
		assert quantizied!=null;
//		StringBuffer sb = new StringBuffer();
//		boolean[] bits = null;
//		List<Boolean> bits = null;
		List<Boolean> haffmanCode = new ArrayList<Boolean>();
		for (int i=0; i<quantizied.length; i++){
//			bits = codes.getBits(quantizied[i]);
//			haffmanCode.addAll(bits);
//			bits.clear();
			haffmanCode.addAll(codes.getBits(quantizied[i]));
		}
		// TODO ?compress haffmanCode 
		
		return haffmanCode;
	}

	/* --== decompression ==--  */
	private StatisticsTreeEntry parseHTree(){
		return StatisticsTreeEntry.readTree();
	}

	private Matrix decodeMatrix(int rowsCount, int columnsCount, List<Boolean> haffmanCodes, HTreeMap codesTree) throws IllegalFormatFlagsException {
		String code = "";
		int [] values = new int [rowsCount*columnsCount];
		int index=0, val;
		for (Boolean b:haffmanCodes){
			code+=(b?'1':'0');		
			if ((val = codesTree.getValue(code))<0){
				if (val == -2) throw new IllegalFormatFlagsException("The bits consequence not found in Hafmman tree: \""+code+"\"");
			} else {
				values[index++]= unQuant(val);	//reverse quantization
				code="";
			}
		}
		final Matrix m = new Matrix(rowsCount, columnsCount).buildMatrix(values);
		values = null;
		return m;
	}
}
