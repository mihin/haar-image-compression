package math;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import math.constants.FileNaming;
import math.dwt.DWT;
import math.dwt.DWTCoefficients;
import math.dwt.Matrix;
import math.dwt.Wavelet2DTransformation;
import math.dwt.wavelets.HaarAdaptive;
import math.dwt.wavelets.HaarClassic;
import math.image.ImageAdapter;
import math.image.ImageObject;

public class Main {
	File logFile = null;

	public static void main(String [] in){
		Main m = new Main();
//		m.logFile = new File("log.txt");
		
		
		final boolean performReconstruction = false;
		final int level = 1;
		m.decomposeImage(performReconstruction, level);
	
//		m.loadDecompCoefs();
		
//		m.reconstructImage();
	}
	
	private void decomposeImage(boolean doReconstruct, int level){
		final String FILENAME = "image";
		final String EXTENTION = ".jpg";
		final String PICFOLDER = "pictures/";
		
		int fileLimit = 1;
		
		int inFileCount = 0;
		while ( fileLimit-->-1 && 
				new File(PICFOLDER+FILENAME+(++inFileCount)+EXTENTION).exists()){}
		
		String filename = null;
		for (int i = 1; i < inFileCount; i++){
			filename = PICFOLDER+FILENAME+i+EXTENTION;
			decomposeImage(doReconstruct, filename, level);
		}
//		decomposeImage(doReconstruct, "image4"+EXTENTION);
	}
	private void decomposeImage(boolean doReconstruct, String filename, int level){
		ImageAdapter ia = new ImageAdapter();
		ImageObject imageData = null;
		try {
			imageData = ia.readImageFile(filename);
			System.out.println("Image from file "+filename+" was read(w="+imageData.width+", h="+imageData.height+").");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		final boolean logCoefsToFile = false;
		
		DWTCoefficients[] coefClassic, coefAdaptive; 
		coefClassic =  decomposeImage(doReconstruct, logCoefsToFile, imageData, new HaarClassic(), level);
		coefAdaptive = decomposeImage(doReconstruct, logCoefsToFile, imageData, new HaarAdaptive(), level);
		
		//comparison output
		DecimalFormat myFormatter = new DecimalFormat("#,000");
		String [] colors = new String [] {"Red", "Green", "Blue"};
		
		if (logFile!=null) {
			System.out.println("Norm sum decay comparison:");
			int j = 0;
			String title = "\n"+filename.intern()+"\n";
			String logs = null;
			for (String color:colors){
				logs =  title+
						color+":\t" + myFormatter.format(coefClassic[j] .getNormVHDSum())
						+ " -> "   + myFormatter.format(coefAdaptive[j].getNormVHDSum())
						+ "\t("+(coefAdaptive[j].getNormVHDSum()*10000/coefClassic[j] .getNormVHDSum()/100f)+"%)";
				System.out.println(logs);
				
				try {
					FileOutputStream fos = new FileOutputStream(logFile, true);
					fos.write((logs+"\n").getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				title = "";
				j++;
			}
		}
		System.out.println();
	}
	private DWTCoefficients[] decomposeImage(boolean doReconstruct, boolean doLogCoefs,
			ImageObject imageData, Wavelet2DTransformation transform, int level){
		//start Haar decomposition
		DWT dwt =  new DWT(transform);
		DWTCoefficients coefR, coefG, coefB;
		System.out.println(dwt.getTranformation().getCaption()+": \nHor\t\tVer\t\tDiag\t\t\tAverage");
		if (doLogCoefs){
			coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "red"	, level);
			coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "green"	, level);
			coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "blue"	, level);
		} else {
			coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, ""	, level);
			coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, ""	, level);
			coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, ""	, level);
		}
		System.out.println();

//		Matrix m = new Matrix(new float [][]{{1,2,3,4}, {4,6,1,2}, {1,2,3,4}, {5,6,7,2}}); 
//		DWTCoefficients coef = dwt.decompose( m , true, "testMatr");
//		System.out.println("Reconstr "+ dwt.getTranformation().getCaption());
//		Matrix reconst = dwt.reconstruct(coef);
//		if (reconst.equals(m)) System.out.println("Reconstr M is the same");
//		System.out.println();
		
		//reconstruction
		if (doReconstruct){
			simpleReconstruct(dwt, imageData, coefR, coefG, coefB);
		}
		
		return new DWTCoefficients[] {coefR, coefG, coefB};
	}
	
//	private int reconsCount = 1;
	private void simpleReconstruct(DWT dwt, ImageObject imageData, DWTCoefficients... coef){
		System.out.println("Reconstruction attempt.. ("+imageData.getFilename()+")");
		Matrix reconstR = dwt.reconstruct(coef[0]);
		Matrix reconstG = dwt.reconstruct(coef[1]);
		Matrix reconstB = dwt.reconstruct(coef[2]);
		
//		if (reconstR.equals(new Matrix(imageData.pixelsR)) &&
//			reconstG.equals(new Matrix(imageData.pixelsG)) &&
//			reconstB.equals(new Matrix(imageData.pixelsB)) )
//			System.out.println("_Reconstructed Matrixes are equal");
		
		ImageObject reconstImage = new ImageObject(
				reconstR.get(), reconstG.get(), reconstB.get(), 
				reconstR.getColumnsCount(), reconstR.getRowsCount()
				);
		reconstImage.saveToImageFile(imageData.getFilename()+"rec"+dwt.getTranformation().getCaption(), "jpg");
		System.out.println();
	}
	
	/**
	 * Load coefs, make 4 filtered images 
	 */
	private void loadDecompCoefs(){
		//loading image coefs
		Wavelet2DTransformation tranformation = new HaarClassic();
		loadDecompCoefs(tranformation.getCaption());
		
		tranformation = new HaarAdaptive();
		loadDecompCoefs(tranformation.getCaption());
	}
	
	private void loadDecompCoefs(String transfName){
		ImageObject[] _4CoefMatrix = new ImageObject[] {
			loadDecompCoefs(transfName, FileNaming.mAverageCoef, false),
			loadDecompCoefs(transfName, FileNaming.mHorizCoef, false),
			loadDecompCoefs(transfName, FileNaming.mVerticalCoef, false),
			loadDecompCoefs(transfName, FileNaming.mDialonalCoef, false),
		};
		
		//gather 4 pictures pretty
		ImageObject.saveDecompImagesToFile(_4CoefMatrix, "converted"+"_"+transfName+"_Combined", "jpg");	
	}
	private ImageObject loadDecompCoefs(String transfName, String matrixName, boolean saveToSeparateFile){
		ImageAdapter ia = new ImageAdapter();
		ImageObject imageData = ia.readImageCoefficients(new String[]{
				FileNaming.cRed  +transfName+matrixName+FileNaming.ext,
				FileNaming.cGreen+transfName+matrixName+FileNaming.ext,
				FileNaming.cBlue +transfName+matrixName+FileNaming.ext
				});
		if (imageData == null){
			System.err.println("Reading image from coefs unsuccessful ("+transfName+", "+matrixName+")");
			return null;
		}

		
		//reconstructing image from Average coefs
		if (saveToSeparateFile){
			System.out.println("Image coefs loaded, making picture file..  ("+transfName+", "+matrixName+")");
			if (!imageData.saveToImageFile("converted"+"_"+transfName+"_"+matrixName.toUpperCase(), "jpg")){
				System.err.println("Writting file unsuccessful ("+transfName+", "+matrixName+")");
				return null;
			}
		}
		return imageData;
	}

	private void reconstructImage() {
		// TODO Auto-generated method stub
		
	}

	
}
