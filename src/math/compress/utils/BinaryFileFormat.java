package math.compress.utils;

public class BinaryFileFormat {
	/*
	 * 	Coefs ->
	 * 		RED ->
	 * 			ma
	 * 			-> 1
	 * [rows](imageSizeValuePull)[columns](imageSizeValuePull)[bindata](DWTCoefValuePull*rows*columns)
	 * 			-> 0 ->
	 * 				ma2 ->  --||--
	 * 			mv ->
	 * [tree](HTreeValuePull*tree_size)[h.codesize](HCodedDataSizePull)[h.code](h.codesize)
	 * 			mh -> 
	 * 			md ->
	 * 			transforms matrix
	 * -> 1[rows](imageSizeValuePull)[columns](imageSizeValuePull)[bindata](AdaptiveMapValuePull*rows*columns)
	 * -> 0
	 * 		GREEN -> 
	 * 		BLUE ->
	 * 		 
	 */
	private static BinaryFileFormat instanse;

	public static BinaryFileFormat getInstanse() {
		return instanse;
	}

	public static void init(int quatLevels) {
		instanse = new BinaryFileFormat(quatLevels);
	}

	private BinaryFileFormat(int quatLevels) {
		// HTreeValuePull = (short)(logOfBase(2,quatLevels)+1);
		// HTreeValuePull = 16;
		// Log.getInstance().log(Level.FINEST, "HTreeValuePull set to "+
		// HTreeValuePull);
	}

	public boolean toQuntizateMA = false;
	public boolean toSaveTreeSize = false;

		public short DWTLevelsPull = 3;
	public short imageSizeValuePull = 12; // 4*1024
	public short DWTCoefValuePull = 8; // 256 values
	public short HTreeValuePull = 2 * 9;
		public short HTreeSizePull = 2 * 10;
	public short HCodedDataSizePull = 20; // 1024*1024
	public short AdaptiveMapValuePull = 2; // 4 values

	// utils
	private double logOfBase(int base, int num) {
		return Math.log(num) / Math.log(base);
	}
}
