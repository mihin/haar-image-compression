package math;

import java.io.File;
import java.io.IOException;

import math.constants.FileNaming;
import math.dwt.DWT;
import math.dwt.DWTCoefficients;
import math.dwt.Matrix;
import math.dwt.Wavelet2DTransformation;
import math.dwt.wavelets.HaarAdaptive;
import math.dwt.wavelets.HaarClassic;
import math.dwt.wavelets.HaarDiagonal;
import math.dwt.wavelets.HaarHorizotal;
import math.dwt.wavelets.HaarVertical;
import math.image.ImageAdapter;
import math.image.ImageObject;

public class Main {

	public static void main(String [] in){
		Main m = new Main();
		
		m.decomposeImage(true);
	
//		m.loadDecompCoefs();
		
//		m.reconstructImage();
	}
	
	private void decomposeImage(boolean doReconstruct){
		final String EXTENTION = ".jpg";
//		final String FILENAME = "image";
//		int inFileCount = 1;
//		while (new File(FILENAME+(inFileCount++)+EXTENTION).exists()){}
//		
//		String filename = null;
//		for (int i = 1; i < inFileCount; i++){
//			filename = FILENAME+i+EXTENTION;
//			decomposeImage(doReconstruct, filename);
//		}
		decomposeImage(doReconstruct, "image3"+EXTENTION);
	}
	private void decomposeImage(boolean doReconstruct, final String FILENAME){
		if (! new File(FILENAME).exists()) {
			System.err.println("Can't find file "+FILENAME);
			return;
		}
		
		ImageAdapter ia = new ImageAdapter();
		ImageObject imageData = null;
		try {
			imageData = ia.readImageFile(FILENAME);
			System.out.println("Image from file "+FILENAME+" was read(w="+imageData.width+", h="+imageData.height+").");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (imageData == null){
			System.err.println("ImageData wasn't loaded, file = "+FILENAME);
			return;
		}

		//start Haar decomposition
		DWT dwt =  new DWT(new HaarHorizotal());
		DWTCoefficients coefR, coefG, coefB;
		System.out.println(dwt.getTranformation().getCaption()+": \nHor\t\tVer\t\tDiag\t\t\tAverage");
//		coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "red");
//		coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "green");
//		coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "blue");
		coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "");
		coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "");
		coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "");
		System.out.println();

//		Matrix m = new Matrix(new float [][]{{1,2,3,4}, {4,6,1,2}, {1,2,3,4}, {5,6,7,2}}); 
//		Matrix m = new Matrix(new float [][]{{1,2}, {3, 4}});
//		DWTCoefficients coef = dwt.decompose( m , true, "testMatr");
//		System.out.println("Reconstr "+ dwt.getTranformation().getCaption());
//		Matrix reconst = dwt.reconstruct(coef);
//		if (reconst.equals(m)) System.out.println("Reconstr M is the same");
//		System.out.println();
//		
//		dwt =  new DWT(new HaarAdaptive());
//		coef = dwt.decompose( m , true, "testMatr2");
//		System.out.println("Reconstr "+ dwt.getTranformation().getCaption());
//		if (reconst.equals(m)) System.out.println("Reconstr M is the same");
//		reconst.equals(m);
		
		
//		{ //dummy reconstruction
//			ImageObject reconstImage = new ImageObject(
//					new Matrix(imageData.pixelsR).get(),
//					new Matrix(imageData.pixelsG).get(),
//					new Matrix(imageData.pixelsB).get(),
//					imageData.width, imageData.height);
//			reconstImage.saveToImageFile("reconstructedImage22", "jpg");
//			
//		}
		
		
		//reconstruction
		if (doReconstruct){
			simpleReconstruct(dwt, imageData, coefR, coefG, coefB);
		}
		
		
		//start AdoptiveHaar decomposition
		dwt =  new DWT(new HaarAdaptive());
		System.out.println(dwt.getTranformation().getCaption()+": \nHor\t\tVer\t\tDiag\t\t\tAverage");
		coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "red");
		coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "green");
		coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "blue");
//		coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "");
//		coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "");
//		coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "");
		System.out.println();
		
		//reconstruction
		if (doReconstruct){
			simpleReconstruct(dwt, imageData, coefR, coefG, coefB);
		}
	}
	
	private int reconsCount = 1;
	private void simpleReconstruct(DWT dwt, ImageObject imageData, DWTCoefficients... coef){
		System.out.println("Reconstruction attempt.. (image"+reconsCount+")");
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
		reconstImage.saveToImageFile("reconstructedImage"+reconsCount++, "jpg");
	}
	
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
