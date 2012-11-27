package math;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import math.compress.Quantization;
import math.dwt.DWT;
import math.dwt.DWTCoefficients;
import math.dwt.Matrix;
import math.dwt.Wavelet2DTransformation;
import math.dwt.wavelets.HaarAdaptive;
import math.dwt.wavelets.HaarClassic;
import math.image.ImageAdapter;
import math.image.ImageObject;
import math.utils.FileNamesConst;
import math.utils.Log;

public class TransmormationManager {
	private int level;
	private boolean doReconstruct;
	private Class transformClass;

	TransmormationManager(int level, boolean toReconstruct) {
		this.level = level;
		doReconstruct = toReconstruct;
		transformClass = HaarClassic.class;
	}

	public void startTransforms() {
		performDecomposition();
	}

	/**
	 * Launch transmormation(s) Chooses files to be processed
	 */
	private void performDecomposition() {
		new File(FileNamesConst.resultsFolder).mkdirs();
		final String FILENAME = "image";
		final String EXTENTION = ".jpg";
		final String PICFOLDER = "pictures/";

		int fileLimit = 1;

		// int inFileCount = 0;
		// while ( fileLimit-->-1 &&
		// new File(PICFOLDER+FILENAME+(++inFileCount)+EXTENTION).exists()){}
		//
		// String filename = null;
		// for (int i = 1; i < inFileCount; i++){
		// filename = PICFOLDER+FILENAME+i+EXTENTION;
		// decomposeImage(filename);
		// }
		//
		// if (DO_RECONSTRUCT)
		// new File(FileNamesConst.resultsFolder+PICFOLDER).mkdir();
		decomposeImage(PICFOLDER + "image2" + EXTENTION);
	}

	/**
	 * Calls decompose with custom transform types Prints statistics and
	 * comapison Make quatization of decompCoefs Calls reconstruction
	 * 
	 * @param filename
	 *            Image file for processing
	 */
	private void decomposeImage(String filename) {
		ImageAdapter ia = new ImageAdapter();
		ImageObject imageData = null;
		try {
			imageData = ia.readImageFile(filename);
			Log.getInstance().log(
					Level.FINE,
					"Image from file " + filename + " was read(w="
							+ imageData.width + ", h=" + imageData.height
							+ ").");
			// System.out.println("Image from file "+filename+" was read(w="+imageData.width+", h="+imageData.height+").");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		final boolean logCoefsToFile = true;

		DWTCoefficients[] coefClassic, coefAdaptive, dwtCoefs;
		// coefClassic = decomposeImage(logCoefsToFile, imageData, new
		// HaarClassic());
		// coefAdaptive = decomposeImage(logCoefsToFile, imageData, new
		// HaarAdaptive());

		// Wavelet2DTransformation method = new HaarAdaptive();
		// Wavelet2DTransformation method = new HaarClassic();
		Wavelet2DTransformation method = null;
		try {
			method = (Wavelet2DTransformation) transformClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		dwtCoefs = decomposeImage(logCoefsToFile, imageData, method);
		if (doReconstruct)
			simpleReconstruct(new DWT(method), imageData, dwtCoefs);

		// comparison output
		/*
		 * DecimalFormat myFormatter = new DecimalFormat("#,000"); String []
		 * colors = new String [] {"Red", "Green", "Blue"};
		 * 
		 * if (logFile!=null) { StringBuffer sb = new StringBuffer();
		 * sb.append("Norm sum decay comparison:");
		 * 
		 * // System.out.println("Norm sum decay comparison:"); int j = 0;
		 * String title = "\n"+filename.intern()+"\n"; String logs = null; for
		 * (String color:colors){ logs = title+ color+":\t" +
		 * myFormatter.format(coefClassic[j] .getNormVHDSum()) + " -> " +
		 * myFormatter.format(coefAdaptive[j].getNormVHDSum()) +
		 * "\t("+(coefAdaptive[j].getNormVHDSum()*10000/coefClassic[j]
		 * .getNormVHDSum()/100f)+"%)"; // System.out.println(logs);
		 * sb.append(logs);
		 * 
		 * try { FileOutputStream fos = new FileOutputStream(logFile, true);
		 * fos.write((logs+"\n").getBytes()); fos.close(); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); } title = ""; j++; }
		 * Log.get().log(Level.FINEST, sb.toString()); }
		 */

		final int quantLevels = 2 * 32;
		Log.getInstance().log(Level.INFO,
				"\n -=Quantization=-  [" + quantLevels + " levels]\n");
		Quantization mQuantization = new Quantization(quantLevels);
		DWTCoefficients decodedCoefs[] = mQuantization.process(dwtCoefs);

		// method = new HaarClassic();
		DWT dwt = new DWT(method);
		String newFile = filename.replace(".", "Huffman.");
		// try {
		// new File(newFile).createNewFile();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		imageData.setFilename(newFile);
		simpleReconstruct(dwt, imageData, decodedCoefs);
	}

	/**
	 * 
	 * @param doLogCoefs
	 *            to save decomp coefs to corresponding file
	 * @param imageData
	 *            image
	 * @param transform
	 *            transformation type
	 * @return R, G, B coefs
	 */
	private DWTCoefficients[] decomposeImage(boolean doLogCoefs,
			ImageObject imageData, Wavelet2DTransformation transform) {
		// start Haar decomposition
		DWT dwt = new DWT(transform);
		Log.getInstance().log(
				Level.FINE,
				"\n" + dwt.getTranformation().getCaption()
						+ ": \n\tHor\t\tVer\t\tDiag\t\t\tAverage");
		// System.out.println(dwt.getTranformation().getCaption()+": \n\tHor\t\tVer\t\tDiag\t\t\tAverage");
		DWTCoefficients[] coefs = dwt.decompose(new Matrix[] {
				new Matrix(imageData.pixelsR), new Matrix(imageData.pixelsG),
				new Matrix(imageData.pixelsB), }, true, doLogCoefs, level);

		// Matrix m = new Matrix(new float [][]{{1,2,3,4}, {4,6,1,2}, {1,2,3,4},
		// {5,6,7,2}});
		// DWTCoefficients coef = dwt.decompose( m , true, "testMatr");
		// System.out.println("Reconstr "+ dwt.getTranformation().getCaption());
		// Matrix reconst = dwt.reconstruct(coef);
		// if (reconst.equals(m)) System.out.println("Reconstr M is the same");
		// System.out.println();

		return coefs;
	}

	// private int reconsCount = 1;
	private void simpleReconstruct(DWT dwt, ImageObject imageData,
			DWTCoefficients... coef) {
		Log.getInstance().log(Level.FINE,
				"\nReconstruction attempt.. (" + imageData.getFilename() + ")");
		// System.out.println("Reconstruction attempt.. ("+imageData.getFilename()+")");

		Matrix reconstR = dwt.reconstruct(coef[0]);
		Matrix reconstG = dwt.reconstruct(coef[1]);
		Matrix reconstB = dwt.reconstruct(coef[2]);

		// if (reconstR.equals(new Matrix(imageData.pixelsR)) &&
		// reconstG.equals(new Matrix(imageData.pixelsG)) &&
		// reconstB.equals(new Matrix(imageData.pixelsB)) )
		// System.out.println("_Reconstructed Matrixes are equal");

		ImageObject reconstImage = new ImageObject(reconstR.get(),
				reconstG.get(), reconstB.get(), reconstR.getColumnsCount(),
				reconstR.getRowsCount());
		reconstImage.saveToImageFile(imageData.getFilename() + "Reconst"
				+ dwt.getTranformation().getCaption(), "jpg");
		// System.out.println();
	}

	/**
	 * Load coefs, make 4 filtered images
	 */
	private void loadDecompCoefs() {
		// loading image coefs
		Wavelet2DTransformation tranformation = new HaarClassic();
		loadDecompCoefs(tranformation.getCaption());

		tranformation = new HaarAdaptive();
		loadDecompCoefs(tranformation.getCaption());
	}

	private void loadDecompCoefs(String transfName) {
		ImageObject[] _4CoefMatrix = new ImageObject[] {
				loadDecompCoefs(transfName, FileNamesConst.mAverageCoef, false),
				loadDecompCoefs(transfName, FileNamesConst.mHorizCoef, false),
				loadDecompCoefs(transfName, FileNamesConst.mVerticalCoef, false),
				loadDecompCoefs(transfName, FileNamesConst.mDialonalCoef, false), };

		// gather 4 pictures pretty
		ImageObject.saveDecompImagesToFile(_4CoefMatrix, "converted" + "_"
				+ transfName + "_Combined", "jpg");
	}

	private ImageObject loadDecompCoefs(String transfName, String matrixName,
			boolean saveToSeparateFile) {
		ImageAdapter ia = new ImageAdapter();
		ImageObject imageData = ia.readImageCoefficients(new String[] {
				FileNamesConst.cRed + transfName + matrixName
						+ FileNamesConst.ext,
				FileNamesConst.cGreen + transfName + matrixName
						+ FileNamesConst.ext,
				FileNamesConst.cBlue + transfName + matrixName
						+ FileNamesConst.ext });
		if (imageData == null) {
			System.err.println("Reading image from coefs unsuccessful ("
					+ transfName + ", " + matrixName + ")");
			return null;
		}

		// reconstructing image from Average coefs
		if (saveToSeparateFile) {
			System.out.println("Image coefs loaded, making picture file..  ("
					+ transfName + ", " + matrixName + ")");
			if (!imageData.saveToImageFile("converted" + "_" + transfName + "_"
					+ matrixName.toUpperCase(), "jpg")) {
				System.err.println("Writting file unsuccessful (" + transfName
						+ ", " + matrixName + ")");
				return null;
			}
		}
		return imageData;
	}

	private void reconstructImage() {
		// TODO Auto-generated method stub

	}

}
