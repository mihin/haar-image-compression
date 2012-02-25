package math.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import math.utils.FileNamesConst;

public class ImageObject {
	private int [] rgbs;
	public int width;
	public int height;
	public int [][] pixels;
	public int [][] pixelsR;
	public int [][] pixelsG;
	public int [][] pixelsB;
	
	public ImageObject(int[] _rgbs, int _width, int _height) {
		super();
		rgbs = _rgbs;
		width = _width;
		height = _height;
		
		Color color;
		pixels = new int [height][width];
		pixelsR = new int [height][width];
		pixelsG = new int [height][width];
		pixelsB = new int [height][width];
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				pixels[i][j] = 
						rgbs[i*width+j];
				
				color = new Color(pixels[i][j]);
				pixelsR[i][j] = color.getRed();
				pixelsG[i][j] = color.getGreen();
				pixelsB[i][j] = color.getBlue();
			}
		}
	}
	
	public ImageObject(float [][] _pixelsR, float [][] _pixelsG, float [][] _pixelsB, int _width, int _height) {
		System.out.println("Creating ImageObject with color arrays");
		width = _width;
		height = _height;
		
//		StringBuffer sb = new StringBuffer();
		
		Color color;
		pixels = new int [height][width];
		pixelsR = new int [height][width];
		pixelsG = new int [height][width];
		pixelsB = new int [height][width];
		rgbs = new int [height*width];
//		rgbs = new int [height*width*3];
		
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				pixelsR[i][j] = (int)_pixelsR[i][j];
				pixelsG[i][j] = (int)_pixelsG[i][j];
				pixelsB[i][j] = (int)_pixelsB[i][j];
				
				try {
					color = new Color(
							Math.abs(pixelsR[i][j]), 
							Math.abs(pixelsG[i][j]), 
							Math.abs(pixelsB[i][j])
							);
				} catch (Exception e) {
					System.err.println(e.getMessage()+
							"\nК="+pixelsR[i][j]+", G="+pixelsG[i][j]+", B="+pixelsB[i][j]);
					color = new Color(
							Math.min(255,Math.abs(pixelsR[i][j])), 
							Math.min(255,Math.abs(pixelsG[i][j])), 
							Math.min(255,Math.abs(pixelsB[i][j]))
							);
				}
				pixels[i][j] = color.getRGB();

//				rgbs[(i*width+j)*3]   = pixelsR[i][j];
//				rgbs[(i*width+j)*3+1] = pixelsG[i][j];
//				rgbs[(i*width+j)*3+2] = pixelsB[i][j];
				rgbs[i*width+j] = pixels[i][j];
//				sb.append(rgbs[i*width+j]);
				
//				sb.append((i*width+j)*3);
//				sb.append(") ");
//				sb.append(rgbs[(i*width+j)*3]);
//				sb.append(' ');
//				sb.append(rgbs[(i*width+j)*3+1]);
//				sb.append(' ');
//				sb.append(rgbs[(i*width+j)*3+2]);
//				sb.append("  ");
			}
//			sb.append('\n');
		}
//		System.out.println(sb.toString());
	}
	
	public boolean saveToImageFile(String filename, String ext){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, rgbs, 0, width);

        File imageFile = new File(FileNamesConst.resultsFolder+filename+"."+ext);
        try {
			ImageIO.write(image, ext, imageFile);
			System.out.println("Image saved to file " + filename+"."+ext);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean saveDecompImagesToFile(ImageObject[] images, String filename, String ext){
		final int width = images[0].width;
		final int height = images[0].height;
		BufferedImage image = new BufferedImage(width*2, height*2, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 	0, 		width, height, images[0].rgbs, 0, width);
		image.setRGB(width, 0, 		width, height, images[1].rgbs, 0, width);
		image.setRGB(0, 	height, width, height, images[2].rgbs, 0, width);
		image.setRGB(width, height, width, height, images[3].rgbs, 0, width);
		
		File imageFile = new File(FileNamesConst.resultsFolder+filename+"."+ext);
        try {
			ImageIO.write(image, ext, imageFile);
			System.out.println("4 Images saved to file " + filename+"."+ext);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String filename = "";
	public ImageObject setFilename(String name){
		filename = name.subSequence(0, name.lastIndexOf('.')).toString();
		return this;
	}
	public String getFilename() {
		return filename;
	}
}
