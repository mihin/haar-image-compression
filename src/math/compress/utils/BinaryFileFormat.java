package math.compress.utils;

import java.util.logging.Level;

import math.utils.Log;

public class BinaryFileFormat
{
	private static BinaryFileFormat instanse;
	public static BinaryFileFormat getInstanse(){
		return instanse;
	}
	public static void init(int quatLevels){
		instanse = new BinaryFileFormat(quatLevels);
	}
	private BinaryFileFormat(int quatLevels){
		//HTreeValuePull = (short)(logOfBase(2,quatLevels)+1);
//		HTreeValuePull = 16;
//		Log.getInstance().log(Level.FINEST, "HTreeValuePull set to "+ HTreeValuePull);
	}
	
	public boolean toQuntizateMA	= false;
	public boolean toSaveTreeSize	= false;
	public short DWTLevelsPull 		= 3;
	public short HTreeSizePull 		= 2*10;
	public short HTreeValuePull 	= 2*9;
	public short HCodedDataSizePull = 20;	// 1024*1024
	public short DWTCoefValuePull 	= 8;	// 256 values
	public short imageSizeValuePull = 12;	// 4*1024
	public short AdaptiveMapValuePull= 2;	// 4 values
	
	//utils
	private double logOfBase(int base, int num) {
	    return Math.log(num) / Math.log(base);
	}
}
