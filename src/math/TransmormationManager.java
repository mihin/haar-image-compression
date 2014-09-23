package math;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	private int mDecompLevels = 1;
	private int mQuantizLevels = 64;
	private boolean doReconstruct;
	private Class classWaveletTransform;
	private String mOutputFormat = FileNamesConst.extBMP;
	private boolean toCopyOriginImageToResults = true;

	public void setOutputFormat(String mOutputFormat) {
		this.mOutputFormat = mOutputFormat;
	}

	TransmormationManager(int dLvls, int quantLvls, Class transformClass) {
		mDecompLevels = dLvls;
		mQuantizLevels = quantLvls;
		doReconstruct = true;
		classWaveletTransform = transformClass;
		
		new File(FileNamesConst.resultsFolder).mkdirs();
		new File(FileNamesConst.resultsFolder, FileNamesConst.picsFolder).mkdirs();
		new File(FileNamesConst.resultsFolder, FileNamesConst.resultsDebugDataFolder).mkdirs();
	}

	public List<String> start(int filesCount) {
		return performDecomposition(filesCount);
	}
	public boolean start(String imageFileName) {
		File f = null;
		if (!(f = new File(FileNamesConst.picsFolder + imageFileName)).exists())
			return false;
		decomposeImage(f.toString());
		return true;
	}

	/**
	 * Launch transmormation(s) Chooses files to be processed
	 * @param filesCount 
	 */
	private List<String> performDecomposition(int filesCount) {
		final String EXTENTION = FileNamesConst.extBMP;
		final String INPUT_FILENAME = "image";

		int inFileCount = 0;
		String currImageName = null;
		List<String> processingImages = new ArrayList<String>();
		while (filesCount-- > -1 && 
				new File(FileNamesConst.picsFolder + (currImageName = INPUT_FILENAME + (++inFileCount) + EXTENTION)).exists()) {
			processingImages.add(currImageName);
		}

		for (String filename : processingImages) {
			decomposeImage(FileNamesConst.picsFolder + filename);
		}

		return processingImages;
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
			Log.getInstance().log(Level.FINE,
							"Image from file " + filename + " was read(w=" + imageData.width + ", h="
									+ imageData.height + ").");
			// System.out.println("Image from file "+filename+" was read(w="+imageData.width+", h="+imageData.height+").");
	
			if (toCopyOriginImageToResults)
				imageData.saveToImageFile(filename, mOutputFormat);
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
			method = (Wavelet2DTransformation) classWaveletTransform.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dwtCoefs = decomposeImage(logCoefsToFile, imageData, method);
		if (doReconstruct)
			simpleReconstruct(new DWT(method), imageData.getFilename(), imageData.width, imageData.height, false, dwtCoefs);

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

		final String imageFilename = imageData.getFilename() + method.getCaption();
		Log.getInstance().log(Level.INFO,
				"\n -=Quantization=-  [" + mQuantizLevels + " levels]");
		Quantization mQuantization = new Quantization(mQuantizLevels);
		DWTCoefficients decodedCoefs[] = mQuantization.process(dwtCoefs, imageFilename);
			
		if (doReconstruct)
			if (decodedCoefs != null) {
				DWT dwt = new DWT(method);
//				String newFile = filename.replace(".", "Huffman.");
//				imageData.setFilename(newFile);
				simpleReconstruct(dwt, imageData.getFilename(), imageData.width, imageData.height, true, decodedCoefs);
			} else {
				Log.getInstance().log(Level.WARNING,
						"Reconstruction received empty image coefs");
			}
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
	private DWTCoefficients[] decomposeImage(boolean doLogCoefs, ImageObject imageData, Wavelet2DTransformation transform) {
		// start Haar decomposition
		DWT dwt = new DWT(transform);
		Log.getInstance().log(
				Level.FINE,
				"\n" + dwt.getTranformation().getCaption()
						+ ": \n\tHor\t\tVer\t\tDiag\t\t\tAverage");
		// System.out.println(dwt.getTranformation().getCaption()+": \n\tHor\t\tVer\t\tDiag\t\t\tAverage");
		DWTCoefficients[] coefs = dwt.decompose(
				new Matrix[] {
					new Matrix(imageData.pixelsR), 
					new Matrix(imageData.pixelsG),
					new Matrix(imageData.pixelsB)}, 
					true, doLogCoefs, mDecompLevels);
		return coefs;
	}

	// private int reconsCount = 1;
	private void simpleReconstruct(DWT dwt, String imageFilename, int w, int h, boolean isHuffman, DWTCoefficients... coef) {
		Log.getInstance().log(Level.FINE,
				"\nReconstruction attempt.. (" + imageFilename + ")");

		Matrix reconstR = dwt.reconstruct(coef[0]);
		Matrix reconstG = dwt.reconstruct(coef[1]);
		Matrix reconstB = dwt.reconstruct(coef[2]);

		ImageObject reconstImage = new ImageObject(reconstR.get(),
				reconstG.get(), reconstB.get(), w, h);
		
		String filename = String.format(isHuffman?"%1$sHuffmanQ%3$dReconstL%2$d%4$s":"%1$sReconstL%2$d%4$s", 
				imageFilename, mDecompLevels, mQuantizLevels, dwt.getTranformation().getCaption());
		reconstImage.saveToImageFile(filename, mOutputFormat);
		
//		reconstImage.saveToImageFile(imageData.getFilename() + "Reconst"
//				+ dwt.getTranformation().getCaption(),FileNamesConst.extJPEG);
//		reconstImage.saveToImageFile(imageData.getFilename() + "Reconst"
//				+ dwt.getTranformation().getCaption(),FileNamesConst.extBMP);
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
						+ FileNamesConst.extData,
				FileNamesConst.cGreen + transfName + matrixName
						+ FileNamesConst.extData,
				FileNamesConst.cBlue + transfName + matrixName
						+ FileNamesConst.extData });
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

}
