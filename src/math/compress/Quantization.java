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
		final File resDir = new File(FileNamesConst.resultsFolder+FileNamesConst.resultsQuantizationFolder);
		resDir.mkdirs();
		final String outputFilename 	 = resDir+"/out"+FileNamesConst.myExt;
		final File output = new File(outputFilename);
		BinaryFileFormat.init(LEVELS);
		DWTCoefficients[] mDWTCoefficients = null;
		try {
			BitOutputStream binOut = null;
			BitInputStream binInput = null;
			binOut = new BitOutputStream(new FileOutputStream(output));
			binInput = new BitInputStream(new FileInputStream(output));
			mDWTCoefficients =  new DWTCoefficients[] {
					process(image[0], "Red", 	binOut, binInput),  
//					process(image[1], "Green",	binOut, binInput),
//					process(image[2], "Blue", 	binOut, binInput),
					image[1],
					image[2],
			};
			binOut.close();
			binInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mDWTCoefficients;
	}
	public DWTCoefficients process(DWTCoefficients image, String RGB, BitOutputStream binOut, BitInputStream binInput) throws IOException{
		final File resDir = new File(FileNamesConst.resultsFolder+FileNamesConst.resultsQuantizationFolder);
		final String resFilename = resDir+"/quant"+RGB;
		int columns = image.getMv().getColumnsCount();
		int rows = image.getMv().getRowsCount();

		try {
			matrixToBin(image.getMa(), binOut);
			huffman(image.getMv(), resFilename+FileNamesConst.mVerticalCoef,	binOut);
			huffman(image.getMh(), resFilename+FileNamesConst.mHorizCoef, 		binOut);
			huffman(image.getMd(), resFilename+FileNamesConst.mDialonalCoef,	binOut);
			
			binOut.flush();
		} catch (IOException e) {
			Log.getInstance().log(Level.WARNING, "ERROR while quntization");
			throw e;
		}

		Matrix 
			ma = image.getMa(),
			mv = image.getMv(),
			mh = image.getMh(),
			md = image.getMd();
		
//		quantizied = null;
		try {
			ma = readMatrixBin(binInput);
			mv = huffmanReverse(binInput, rows, columns, resFilename+FileNamesConst.mVerticalCoef);
			mh = huffmanReverse(binInput, rows, columns, resFilename+FileNamesConst.mHorizCoef);
			md = huffmanReverse(binInput, rows, columns, resFilename+FileNamesConst.mDialonalCoef);
		} catch (IOException e) {
			Log.getInstance().log(Level.WARNING, "ERROR while reverse quntization");
			throw e;
		}
		System.gc();
		return new DWTCoefficients(ma, mv, mh, md, null, false);
	}
	
	private void huffman(Matrix m, String saveFilename, BitOutputStream binOut) throws IOException{
		Log.getInstance().log(Level.FINER,"\nHuffman codding.");
		FreqStatistics freqStat = new FreqStatistics(LEVELS);
		//quatization & statistics gathering
		int [] quantizied = processMatrixQuatization(m,freqStat);
	
		List<Boolean> haffmanCode = buildTreeAndCompress(freqStat,quantizied, saveFilename, binOut);
//		try {
//			binOut.flush();
////			binOut.close();	//not here!
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		// save/print Haffman code
//		try {
//			BitOutputStream bos = new BitOutputStream(new FileOutputStream(saveFilename+"bits.txt"));
//			for (Boolean b:haffmanCode){
//				bos.writeBit(b?1:0);
//			}
//			bos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		haffmanCode.clear();
		quantizied = null;
		freqStat.free();
	}
	private Matrix huffmanReverse(BitInputStream binInput, int rows, int columns, String saveFilename) throws IOException{
		Log.getInstance().log(Level.FINER,"\nHuffman decompression.");
		
		//read HTree
//		StatisticsTreeEntry mHTree = parseHTree(saveFilename);
		StatisticsTreeEntry mHTree = parseHTree(binInput);

		//get Map Value -> Code
		HTreeMap codesTree = mHTree.buildCodesMap();
		Log.getInstance().log(Level.FINER, "Restored codes tree (size = "
					+mHTree.getTreeBitSize()+"):\n"+codesTree.toString());
		
		Matrix decodedMatrix = null;
		int count = 0;
		int hufCodeLength = 0;
		try {
			//read huffmanCode
			hufCodeLength = binInput.readBits(BinaryFileFormat.getInstanse().HCodedDataSizePull);
			Log.getInstance().log(Level.FINEST, "Restoring Huffman code.. Expecting code length is "+hufCodeLength);
			List<Boolean> huffmanCodeRestored = new ArrayList<Boolean>();
			
			for (int i = 0; i < hufCodeLength; i++){
				huffmanCodeRestored.add(binInput.readBit()!=0);
				count++;
			}
			Log.getInstance().log(Level.FINER, "Restored Huffman code size is "+huffmanCodeRestored.size());
		
			//decode Matrix from Haffman codes
			decodedMatrix = decodeMatrix(rows, columns, huffmanCodeRestored, codesTree);
			String filename = saveFilename.substring(saveFilename.indexOf('\\'))+"decoded.txt";
			decodedMatrix.saveToFile(filename, "Huffman decoded");
			Log.getInstance().log(Level.FINER, "Decoded matrix saved to file "+filename);
//		} catch (IllegalFormatFlagsException e) {
//			e.printStackTrace();
		} catch (IOException e) {
			Log.getInstance().log(Level.FINEST, "Exception after read bits:"+count+" ("+hufCodeLength+")");
//			e.printStackTrace();
			throw e;
		}
		return decodedMatrix;
	}
	private Matrix matrixCompressCycle(Matrix m, String saveFilename, BitOutputStream binOut, BitInputStream binInput) throws IOException{
			huffman(m, saveFilename, binOut);
			return huffmanReverse(binInput, m.getRowsCount(), m.getColumnsCount(), saveFilename);
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
			String saveFilename, BitOutputStream binOut) throws IOException{
		//sort by freqs
		freqStat.sort();
		Log.getInstance().log(Level.FINEST, "buildTreeAndCompress, sorted, frequences:\n"+freqStat.toString());
		
		//build tree
		StatisticsTreeEntry treeRoot = freqStat.buildTree();

		//insert next block size
		if (BinaryFileFormat.getInstanse().toSaveTreeSize){
			int treeBitsLength = treeRoot.getTreeBitSize();
			binOut.writeBits(treeBitsLength, BinaryFileFormat.getInstanse().HTreeSizePull);
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
		binOut.writeBits(huffmanCode.size(), BinaryFileFormat.getInstanse().HCodedDataSizePull);
		int count = 0;
		for (Boolean b : huffmanCode){
			binOut.writeBit(b?1:0);
			count++;
//				binOut.writeBits(b?1:0, (short)1);
		}
		
		Log.getInstance().log(Level.FINER, "buildTreeAndCompress, huffmanCode (size="+ huffmanCode.size() +" bits), "+count+" was output to stream.");
		//TODO assemble formated HTree and compressed HCode
		
		return huffmanCode;
	}
	private List<Boolean> processCompression(HTreeMap codesTree,int [] quantizied){
		assert quantizied!=null;
//		StringBuilder sb = new StringBuilder();
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
	
	/**
	 * Write matrix to bit-file in a common way
	 * @throws IOException 
	 */
	private void matrixToBin(Matrix m, BitOutputStream binOut) throws IOException{
		int rows 	= m.getRowsCount();
		int columns = m.getColumnsCount();
		binOut.writeBits(rows, BinaryFileFormat.getInstanse().imageSizeValuePull);
		binOut.writeBits(columns, BinaryFileFormat.getInstanse().imageSizeValuePull);
		for (int row = 0; row < rows; row++){
			for (int column = 0; column < columns; column++){
				binOut.writeBits(Math.round(m.get(row, column)), BinaryFileFormat.getInstanse().DWTCoefValuePull);
			}
		}
	}
	private Matrix readMatrixBin(BitInputStream binInput) throws IOException{
		int rows 	= binInput.readBits(BinaryFileFormat.getInstanse().imageSizeValuePull);
		int columns = binInput.readBits(BinaryFileFormat.getInstanse().imageSizeValuePull);
		Matrix res = new Matrix(rows, columns);
		for (int row = 0; row < rows; row++){
			for (int column = 0; column < columns; column++){
				res.set(row, column, binInput.readBits(BinaryFileFormat.getInstanse().DWTCoefValuePull));
			}
		}
		return res;
	}
}
