package math.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatFlagsException;
import java.util.List;
import java.util.logging.Level;

import math.compress.utils.BinaryFileFormat;
import math.compress.utils.BitInputStream;
import math.compress.utils.BitOutputStream;
import math.dwt.DWTCoefficients;
import math.dwt.Matrix;
import math.utils.FileNamesConst;
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
	/**
	 * @param image coefs after dwt
	 * @return	image coefs restored from qauntization
	 */
	public DWTCoefficients[] process(DWTCoefficients [] image){
		return new DWTCoefficients[] {
				process(image[0], "Red"),  
				process(image[1], "Green"),  
				process(image[2], "Blue"),
		};
	}
	public DWTCoefficients process(DWTCoefficients image, String RGB){
		
		final File resDir = new File(FileNamesConst.resultsFolder+FileNamesConst.resultsQuantizationFolder);
		resDir.mkdirs();
		final String resFilename = resDir+"/quant"+RGB;
		
		final String outputFilename 	 = resDir+"/out"+RGB+FileNamesConst.myExt;
		final File output = new File(outputFilename);
		BitOutputStream binOut = null;
		BitInputStream binInput = null;
		try {
			binOut = new BitOutputStream(new FileOutputStream(output));
			binInput = new BitInputStream(new FileInputStream(output));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Matrix ma, mv, mh, md;
		mv = matrixCompressCycle(image.getMv(), resFilename+FileNamesConst.mVerticalCoef, binOut, binInput);
//		mh = matrixCompressCycle(image.getMh(), resFilename+FileNamesConst.mHorizCoef,	  null,   null);
//		md = matrixCompressCycle(image.getMd(), resFilename+FileNamesConst.mDialonalCoef, null,   null);
		
//		quantizied = null;
		System.gc();
		return new DWTCoefficients(image.getMa(), mv, image.getMh(), image.getMd(), null, false);
	}
	
	private Matrix matrixCompressCycle(Matrix m, String saveFilename, BitOutputStream binOut, BitInputStream binInput){
		FreqStatistics freqStat = new FreqStatistics(LEVELS);
		
		//quatization & statistics gathering
		int [] quantizied = processMatrixQuatization(m,freqStat);
		
		//Huffman compression
		List<Boolean> haffmanCode = buildTreeAndCompress(freqStat,quantizied, saveFilename, binOut);
		try {
			binOut.flush();
			binOut.close();	//not here!
		} catch (IOException e) {
			e.printStackTrace();
		}

		// save/print Haffman code
		try {
			BitOutputStream bos = new BitOutputStream(new FileOutputStream(saveFilename+"bits.txt"));
			StringBuffer sb = new StringBuffer();
			for (Boolean b:haffmanCode){
				sb.append(b?'1':'0');
				bos.writeBit(b?1:0);
			}
			Log.getInstance().log(Level.FINEST, "Haffman encoded values:\t"+sb.toString());			
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//Decompression
		Log.getInstance().log(Level.FINER,"\nDecompression.");
		//read HTree
//		StatisticsTreeEntry mHTree = parseHTree(saveFilename);
		StatisticsTreeEntry mHTree = parseHTree(binInput);

		//get Map Value -> Code
		HTreeMap codesTree = mHTree.buildCodesMap();
		Log.getInstance().log(Level.FINER, "Loaded codes tree:\n"+codesTree.toString());
		
		//decode Matrix from Haffman codes
		Matrix decodedMatrix = null;
		try {
			decodedMatrix = decodeMatrix(m.getRowsCount(), m.getColumnsCount(), haffmanCode, codesTree);
			String filename = saveFilename.substring(saveFilename.indexOf('\\'))+"decoded.txt";
			decodedMatrix.saveToFile(filename, "Huffman decoded");
			Log.getInstance().log(Level.FINER, "Decoded matrix saved to file "+filename);
		} catch (IllegalFormatFlagsException e) {
			e.printStackTrace();
		}

		
		freqStat.free();
		haffmanCode.clear();
		quantizied = null;
		return decodedMatrix;
	}

	private int [] processMatrixQuatization(Matrix m, FreqStatistics freqStat) {
		int [] quantizied = new int [m.getColumnsCount()*m.getRowsCount()];
		final int columns = m.getColumnsCount();
		int b=0;
		
		//Quantization and calculating frequences
		for (int i=0;i<m.getRowsCount();i++){
			for (int j=0;j<columns;j++){
				b = quant(m.get()[i][j]);
				quantizied[i*columns + j] = b;
				freqStat.push(b);
			}
		}
		return quantizied;
	}

	private int quant(float f) {
		f = f + SHIFT;
		if (f < 0) f = 0;						//f = Min(); f = Max()
		else if (f >= MAX_VAL) f = MAX_VAL-1;
//		return Math.round(f / DIVIDER);
		return (int)(f / DIVIDER);
	}
	private int unQuant(int q) {
		return q*DIVIDER-SHIFT;
	}
	
	
	/* Huffman compression */
//	private int [] freqences;
//	private FreqStatistics freqStat;
	private List<Boolean> buildTreeAndCompress(FreqStatistics freqStat,int [] quantizied, 
			String saveFilename, BitOutputStream binOut){
		//sort by freqs
		freqStat.sort();
		Log.getInstance().log(Level.FINEST, "doCompress, sorted, frequences:\n"+freqStat.toString());
		
		//build tree
		StatisticsTreeEntry treeRoot = freqStat.buildTree();

		//insert next block size
		try {
			int treeBitsLength = treeRoot.getTreeBitSize();
			binOut.writeBits(treeBitsLength, BinaryFileFormat.HTreeSizePull);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//TODO Convert HTree to output format
		treeRoot.toBits(saveFilename, binOut);	//should write treeRoot.getTreeBitSize() bits of data

		//get Map Value -> Code
		HTreeMap codesTree = treeRoot.buildCodesMap();
		Log.getInstance().log(Level.FINER, codesTree.toString());
		
		//process quantizied Matrix with H-Tree, ?and compress
		List<Boolean> huffmanCode = processCompression(codesTree,quantizied);
//		System.out.println("Decoded by Haffman, bytes ");
		
		//output huffman-processed values
		try {
			binOut.writeBits(huffmanCode.size(), BinaryFileFormat.HCodedDataSizePull);
			for (Boolean b : huffmanCode)
				binOut.writeBit(b?1:0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//TODO assemble formated HTree and compressed HCode
		
		return huffmanCode;
	}
	private List<Boolean> processCompression(HTreeMap codesTree,int [] quantizied){
		assert quantizied!=null;
//		StringBuffer sb = new StringBuffer();
//		boolean[] bits = null;
//		List<Boolean> bits = null;
		List<Boolean> haffmanCode = new ArrayList<Boolean>();
		for (int i=0; i<quantizied.length; i++){
//			bits = codes.getBits(quantizied[i]);
//			haffmanCode.addAll(bits);
//			bits.clear();
			haffmanCode.addAll(codesTree.getBits(quantizied[i]));
		}
		// TODO ?compress haffmanCode 
		
		return haffmanCode;
	}

	/* --== decompression ==--  */
	private StatisticsTreeEntry parseHTree(String saveFilename){
		return StatisticsTreeEntry.readTree(saveFilename);
	}
	private StatisticsTreeEntry parseHTree(BitInputStream binIn){
		return StatisticsTreeEntry.readTree(binIn);
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
