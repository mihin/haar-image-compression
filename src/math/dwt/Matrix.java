package math.dwt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import math.constants.FileNaming;

public class Matrix {
	private float [][] values;
	private long norm = 0;
	public Matrix(int rows, int columns){
		values = new float[rows][columns];
//		System.out.println("Matrix created. Empty, rows = "+rows+", columns = "+columns);
		
	}
	public Matrix(float [][] input){
//		int rows = (input.length % 2 == 1?input.length+1:input.length);
//		int columns = (input[0].length % 2 == 1?input[0].length+1:input[0].length);
//		values = new float [rows][columns]; 
//		Collections.addAll(values, input); 
		values = input;
//		System.out.println("Matrix created. Rows = "+getRowsCount()+", columns = "+getColumnsCount());
	}
	public Matrix(int [][] input){
		int rows = input.length;
		int columns = input[0].length;
		values = new float[rows][columns];
		for (int i=0; i < rows; i++)
			for (int j=0; j < columns; j++)
				values[i][j] = input[i][j]; 
				
	}
	
	public float [][] get(){
		return values;
	}
	public int getRowsCount(){
		return values.length;
	}
	public int getColumnsCount(){
		return values[0].length;
	}
	
	public void set(int row, int column, float value){
		values[row][column] = value;
	}
	
	public long calculateNorm(){
		if (norm == 0){ 
			if (values == null || getColumnsCount()<1 || getRowsCount()<1)
				return -1;
			else {
				norm = 0;
				for (int i = 0; i < getRowsCount(); i++ ){
					for (int j = 0; j < getColumnsCount(); j++ ){
						norm += values[i][j]*values[i][j];
					}
				}
			}
		}
		return norm;
	}
	
	@Override
	public String toString() {
		if (values == null) return "null";
		if (getColumnsCount()<1 || getRowsCount()<1)
			return "empty";
		else {
			StringBuffer sb = new StringBuffer("{\n");
			for (int i = 0; i < getRowsCount(); i++ ){
				for (int j = 0; j < getColumnsCount(); j++ ){
					sb.append(values[i][j]);
					sb.append("\t");
				}
				sb.append("\n");
			}
			sb.append("}");
			if (norm > 0) sb.append("\nNorm = "+norm);
			return sb.toString();
		}
	}
	
	public void saveToFile(String path, String comments){
//		BufferedOutputStream os = null;
		BufferedWriter bw = null;
		try {
//			File f = new File(FileNaming.resultsFolder);
//			f.mkdirs();
			File f = new File(FileNaming.resultsFolder+path);
			
			bw = new BufferedWriter(new FileWriter(f));
//			os = new BufferedOutputStream(new FileOutputStream(f));
			if (comments!=null && !comments.equals("")){
//				os.write(comments.concat("\n\n").getBytes());
				bw.write(comments.concat("\n\n"));
			}
//			os.write(toString().getBytes());
//			os.flush();
			bw.write(FileNaming.propSize+getColumnsCount()+FileNaming.propSizesDelimeter+getRowsCount()+"\n");
			bw.write(toString());
			bw.flush();
//			System.out.println("File "+path+"("+comments+") was read.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
//				os.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
