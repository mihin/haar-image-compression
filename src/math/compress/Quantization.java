package math.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatFlagsException;
import java.util.List;
import java.util.logging.Level;

import math.compress.utils.BinaryFileFormat;
import math.compress.utils.BitInputStream;
import math.compress.utils.BitOutputStream;
import math.dwt.Composable;
import math.dwt.DWTCoefficients;
import math.dwt.Matrix;
import math.dwt.wavelets.HaarAdaptive;
import math.utils.FileNamesConst;
import math.utils.Log;

public class Quantization {

	private final int SHIFT = 256;
	private final int MAX_VAL = 2 * SHIFT;

	private final int qLEVELS;
	private final int DIVIDER;

	public Quantization(int levels) {
		qLEVELS = levels;
		DIVIDER = MAX_VAL / qLEVELS;
	}

	// private int [] quantizied;

	// whole image level
	/**
	 * @param image
	 *            coefs after dwt
	 * @param imageFilename
	 * @return image coefs restored from qauntization
	 */
	public DWTCoefficients[] process(DWTCoefficients[] image, String imageFilename) {
		BinaryFileFormat.init(qLEVELS);
		// final File resDir = new
		// File(FileNamesConst.resultsFolder+FileNamesConst.resultsQuantizationFolder);
		// resDir.mkdirs();
		// final String outputFilename = resDir+"/out"+FileNamesConst.myExt;
		final File output = new File(FileNamesConst.resultsFolder, imageFilename + FileNamesConst.extBIN);
		DWTCoefficients[] mDWTCoefficients = null;
		try {
			BitOutputStream binOut = null;
			BitInputStream binInput = null;
			binOut = new BitOutputStream(new FileOutputStream(output));
			Log.getInstance().log(Level.FINER, "\nStart image bit-codding, output file: " + output.getName());
			compressColorToStream(image[DWTCoefficients.RED], binOut);
			compressColorToStream(image[DWTCoefficients.GREEN], binOut);
			compressColorToStream(image[DWTCoefficients.BLUE], binOut);
			binOut.close();
			
			Log.getInstance().log(Level.FINER, "\nStart image bit-decompression");
			binInput = new BitInputStream(new FileInputStream(output));
			mDWTCoefficients = new DWTCoefficients[] { decompressColorFromStream(binInput),
					decompressColorFromStream(binInput), decompressColorFromStream(binInput),
			// image[DWTCoefficients.GREEN],
			// image[DWTCoefficients.BLUE]
			};

			binInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mDWTCoefficients;
	}

	// color matrix level
	private void compressColorToStream(DWTCoefficients image, BitOutputStream binOut) throws IOException {
		try {
			DWTCoefficients maDecomposition = image.getMaDecomposition();
			if (maDecomposition != null) {
				binOut.writeBit(0);
				compressColorToStream(maDecomposition, binOut);
			} else {
				binOut.writeBit(1);
				matrixToBin(image.getMa(), binOut, BinaryFileFormat.getInstanse().DWTCoefValuePull);
			}
			huffman(image.getMv(), binOut);
			huffman(image.getMh(), binOut);
			huffman(image.getMd(), binOut);

			// HuffmanAdaptive sign
			if (image.getMap() != null) {
				binOut.writeBit(1);
				matrixToBin(image.getMap(), binOut, BinaryFileFormat.getInstanse().AdaptiveMapValuePull);
			} else {
				binOut.writeBit(0);
			}

			// binOut.flush();
		} catch (IOException e) {
			Log.getInstance().log(Level.WARNING, "ERROR while quntization");
			throw e;
		}
	}

	private DWTCoefficients decompressColorFromStream(BitInputStream binInput) throws IOException {
		Matrix mv, mh, md, map = null;
		Matrix ma;
		try {
			if (binInput.readBit() == 1) {
				ma = readMatrixBin(binInput, BinaryFileFormat.getInstanse().DWTCoefValuePull);
				// TODO get transformation
				ma.setTransform(new HaarAdaptive());
			} else {
				DWTCoefficients dwtCoefs = decompressColorFromStream(binInput);
				ma = dwtCoefs.compose();
			}
			int rows = ma.getRowsCount();
			int columns = ma.getColumnsCount();
			mv = huffmanReverse(binInput, rows, columns);
			mh = huffmanReverse(binInput, rows, columns);
			md = huffmanReverse(binInput, rows, columns);

			// HuffmanAdaptive sign
			boolean isAdaptiveMethod = binInput.readBit() == 1;
			if (isAdaptiveMethod) {
				map = readMatrixBin(binInput, BinaryFileFormat.getInstanse().AdaptiveMapValuePull);
				Log.getInstance().log(
						Level.FINER,
						"decompressColorFromStream, Color compressed with" + (isAdaptiveMethod ? "" : "out")
								+ " adaptive method");
			}
		} catch (IOException e) {
			Log.getInstance().log(Level.WARNING, "ERROR while reverse quntization");
			throw e;
		}
		System.gc();
		return new DWTCoefficients(ma, mv, mh, md, map, false);
	}

	// dwt color matrixes level
	private void huffman(Matrix m, BitOutputStream binOut) throws IOException {
		Log.getInstance().log(Level.FINER, "\nHuffman codding.");
		FreqStatistics freqStat = new FreqStatistics(qLEVELS);
		// quatization & statistics gathering
		int[] quantizied = processMatrixQuatization(m, freqStat);

		List<Boolean> haffmanCode = buildTreeAndCompress(freqStat, quantizied, binOut);

		haffmanCode.clear();
		quantizied = null;
		freqStat.free();
	}

	private Matrix huffmanReverse(BitInputStream binInput, int rows, int columns) throws IOException {
		Log.getInstance().log(Level.FINER, "\nHuffman decompression.");

		// read HTree
		// StatisticsTreeEntry mHTree = parseHTree(saveFilename);
		StatisticsTreeEntry mHTree = parseHTree(binInput);

		// get Map Value -> Code
		HTreeMap codesTree = mHTree.buildCodesMap();
		Log.getInstance().log(Level.FINER,
				"Restored codes tree (size = " + mHTree.getTreeBitSize() + "):\n" + codesTree.toString());

		Matrix decodedMatrix = null;
		int count = 0;
		int hufCodeLength = 0;
		try {
			// read huffmanCode
			hufCodeLength = binInput.readBits(BinaryFileFormat.getInstanse().HCodedDataSizePull);
			Log.getInstance().log(Level.FINEST, "Restoring Huffman code.. Expecting code length is " + hufCodeLength);
			List<Boolean> huffmanCodeRestored = new ArrayList<Boolean>();

			for (int i = 0; i < hufCodeLength; i++) {
				huffmanCodeRestored.add(binInput.readBit() != 0);
				count++;
			}
			Log.getInstance().log(Level.FINER, "Restored Huffman code size is " + huffmanCodeRestored.size());

			// decode Matrix from Haffman codes
			decodedMatrix = decodeMatrix(rows, columns, huffmanCodeRestored, codesTree);
		} catch (IOException e) {
			Log.getInstance().log(Level.FINEST, "Exception after read bits:" + count + " (" + hufCodeLength + ")");
			// e.printStackTrace();
			throw e;
		}
		return decodedMatrix;
	}

	// quantization utils
	private int[] processMatrixQuatization(Matrix m, FreqStatistics freqStat) {
		int[] quantizied = new int[m.getColumnsCount() * m.getRowsCount()];
		final int columns = m.getColumnsCount();
		int b = 0;

		// Quantization and calculating frequences
		for (int i = 0; i < m.getRowsCount(); i++) {
			for (int j = 0; j < columns; j++) {
				b = quant(m.get()[i][j]);
				quantizied[i * columns + j] = b;
				freqStat.push(b);
			}
		}
		return quantizied;
	}

	private int quant(float f) {
		f = f + SHIFT;
		if (f < 0)
			f = 0; // f = Min(); f = Max()
		else if (f >= MAX_VAL)
			f = MAX_VAL - 1;
		// return Math.round(f / DIVIDER);
		return (int) (f / DIVIDER);
	}

	private int unQuant(int q) {
		return q * DIVIDER - SHIFT;
	}

	// Huffman compression

	private List<Boolean> buildTreeAndCompress(FreqStatistics freqStat, int[] quantizied, BitOutputStream binOut)
			throws IOException {
		// sort by freqs
		freqStat.sort();
		Log.getInstance().log(Level.FINEST, "buildTreeAndCompress, sorted, frequences:\n" + freqStat.toString());

		// build tree
		StatisticsTreeEntry treeRoot = freqStat.buildTree();

		// insert next block size
		int treeBitsLength = treeRoot.getTreeBitSize();
		if (BinaryFileFormat.getInstanse().toSaveTreeSize) {
			binOut.writeBits(treeBitsLength, BinaryFileFormat.getInstanse().HTreeSizePull);
		}

		// TODO Convert HTree to output format
		treeRoot.toBits(binOut); // should write treeRoot.getTreeBitSize() bits
									// of data

		// get Map Value -> Code
		HTreeMap codesTree = treeRoot.buildCodesMap();
		Log.getInstance().log(Level.FINER, codesTree.toString());

		// process quantizied Matrix with H-Tree, ?and compress
		List<Boolean> huffmanCode = processCompression(codesTree, quantizied);
		// System.out.println("Decoded by Haffman, bytes ");

		// output huffman-processed values
		binOut.writeBits(huffmanCode.size(), BinaryFileFormat.getInstanse().HCodedDataSizePull);
		int count = 0;
		for (Boolean b : huffmanCode) {
			binOut.writeBit(b ? 1 : 0);
			count++;
			// binOut.writeBits(b?1:0, (short)1);
		}

		Log.getInstance().log(
				Level.FINER,
				"buildTreeAndCompress, huffmanCode (size=" + huffmanCode.size() + " bits, max="
						+ Math.pow(2, BinaryFileFormat.getInstanse().HCodedDataSizePull) + "), " + count
						+ " was output to stream. Tree size = " + treeBitsLength);
		// TODO assemble formated HTree and compressed HCode

		return huffmanCode;
	}

	private List<Boolean> processCompression(HTreeMap codesTree, int[] quantizied) {
		assert quantizied != null;
		// StringBuilder sb = new StringBuilder();
		// boolean[] bits = null;
		// List<Boolean> bits = null;
		List<Boolean> haffmanCode = new ArrayList<Boolean>();
		for (int i = 0; i < quantizied.length; i++) {
			// bits = codes.getBits(quantizied[i]);
			// haffmanCode.addAll(bits);
			// bits.clear();
			haffmanCode.addAll(codesTree.getBits(quantizied[i]));
		}
		// TODO ?compress haffmanCode

		return haffmanCode;
	}

	// Huffman decompression
	private StatisticsTreeEntry parseHTree(String saveFilename) {
		return StatisticsTreeEntry.readTree(saveFilename);
	}

	private StatisticsTreeEntry parseHTree(BitInputStream binIn) {
		return StatisticsTreeEntry.readTree(binIn);
	}

	private Matrix decodeMatrix(int rowsCount, int columnsCount, List<Boolean> haffmanCodes, HTreeMap codesTree)
			throws IllegalFormatFlagsException {
		String code = "";
		int[] values = new int[rowsCount * columnsCount];
		int index = 0, val;
		for (Boolean b : haffmanCodes) {
			code += (b ? '1' : '0');
			if ((val = codesTree.getValue(code)) < 0) {
				if (val == -2)
					throw new IllegalFormatFlagsException("The bits consequence not found in Hafmman tree: \"" + code
							+ "\"");
			} else {
				values[index++] = unQuant(val); // reverse quantization
				code = "";
			}
		}
		final Matrix m = new Matrix(rowsCount, columnsCount).buildMatrix(values);
		values = null;
		return m;
	}

	// non quantization utils
	private void matrixToBin(Matrix m, BitOutputStream binOut, short valuePull) throws IOException {
		Log.getInstance().log(Level.FINER, "\nMatrixToBin.");
		int rows = m.getRowsCount();
		int columns = m.getColumnsCount();
		binOut.writeBits(rows, BinaryFileFormat.getInstanse().imageSizeValuePull);
		binOut.writeBits(columns, BinaryFileFormat.getInstanse().imageSizeValuePull);
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				binOut.writeBits(Math.round(m.get(row, column)), valuePull);
			}
		}
	}

	private Matrix readMatrixBin(BitInputStream binInput, short valuePull) throws IOException {
		Log.getInstance().log(Level.FINER, "\nRead Matrix Bin.");
		int rows = binInput.readBits(BinaryFileFormat.getInstanse().imageSizeValuePull);
		int columns = binInput.readBits(BinaryFileFormat.getInstanse().imageSizeValuePull);
		Matrix res = new Matrix(rows, columns);
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				res.set(row, column, binInput.readBits(valuePull));
			}
		}
		return res;
	}

}
