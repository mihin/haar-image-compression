package math;

import java.io.IOException;

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

	public static void main(String [] in){
		Main m = new Main();
		
		m.decomposeImage();
	
//		m.loadDecompCoefs();
		
//		m.reconstructImage();
	}
	
	private void decomposeImage(){
		final String PATH = "image.jpg";
		
		ImageAdapter ia = new ImageAdapter();
		ImageObject imageData = null;
		try {
			imageData = ia.readImageFile(PATH);
			System.out.println("Image from file "+PATH+" was read(w="+imageData.width+", h="+imageData.height+"). Array size "+imageData.pixels.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (imageData == null){
			System.err.println("ImageData wasn't loaded, file = "+PATH);
			return;
		}
		

		DWT dwt =  new DWT(new HaarClassic());
		DWTCoefficients coefR, coefG, coefB; 
		coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "red");
		coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "green");
		coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "blue");

//		Matrix m = new Matrix(new float [][]{{1,2,3,4}, {4,6,1,2}, {1,2,3,4}, {5,6,7,2}}); 
//		Matrix m = new Matrix(new float [][]{{1,2}, {3, 4}});
//		DWTCoefficients coef = dwt.decompose( m , true, "testMatr");
		
//		{ //dummy reconstruction
//			ImageObject reconstImage = new ImageObject(
//					new Matrix(imageData.pixelsR).get(),
//					new Matrix(imageData.pixelsG).get(),
//					new Matrix(imageData.pixelsB).get(),
//					imageData.width, imageData.height);
//			reconstImage.saveToImageFile("reconstructedImage22", "jpg");
//			
//		}
		{ //reconstruction
			System.out.println("Reconstruction attempt..");
//			Matrix reconst = dwt.reconstruct(coef);
//			reconst.equals(m);
			
			
			Matrix reconstR = dwt.reconstruct(coefR);
			Matrix reconstG = dwt.reconstruct(coefG);
			Matrix reconstB = dwt.reconstruct(coefB);
			if (reconstR.equals(new Matrix(imageData.pixelsR)) &&
				reconstG.equals(new Matrix(imageData.pixelsG)) &&
				reconstB.equals(new Matrix(imageData.pixelsB))) 
				System.out.println("_Reconstructed Matrixes are equal");
			ImageObject reconstImage = new ImageObject(
					reconstR.get(), reconstG.get(), reconstB.get(), reconstR.getColumnsCount(), reconstR.getRowsCount());
			reconstImage.saveToImageFile("reconstructedImage", "jpg");
		}
		
		
//		dwt =  new DWT(new HaarAdaptive());
//		coefR = dwt.decompose(new Matrix(imageData.pixelsR), true, "red");
//		coefG = dwt.decompose(new Matrix(imageData.pixelsG), true, "green");
//		coefB = dwt.decompose(new Matrix(imageData.pixelsB), true, "blue");
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
