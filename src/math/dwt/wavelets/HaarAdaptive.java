package math.dwt.wavelets;

import math.dwt.Wavelet2DTransformation;

public class HaarAdaptive implements Wavelet2DTransformation {
//	private Wavelet2DTransformation hc, hv, hh, hd;
	private Wavelet2DTransformation [] transformations; 
	public HaarAdaptive(){
		transformations = new Wavelet2DTransformation[] {
			new HaarClassic(),
			new HaarVertical(),
			new HaarHorizotal(),
			new HaarDiagonal()
		};
	}
	public String getCaption(){return "HaarAdaptive";}
	
	float [] minCoef,coef;
	double minNorm, norm;
//	Wavelet2DTransformation minTrans, tr;
	@Override
	public float[] perform(float[] coef) {
		minNorm = getSquareSum((minCoef = transformations[0].perform(coef)));
		for (int i=1;i<transformations.length;i++){
			norm = getSquareSum((coef = transformations[i].perform(coef)));
			if (norm < minNorm){
				minNorm = norm;
				minCoef = coef;
			}
		}
		
		return minCoef;
		//1
//		minTrans = transformations[0];
//		minCoef=minTrans.perform(coef);
//		minNorm = getSquareSum(minCoef);
//		
//		for (int i=1;i<transformations.length;i++){
//			if ( minNorm >  (norm = getSquareSum((coef=transformations[i].perform(coef))))){
//				minTrans = transformations[i];
//				minCoef=coef;
//				minNorm = norm;
//			}
//		}
//		return minCoef;
		
		//2
//		w1coef = hc.perform(coef);
//		w2coef = hv.perform(coef);
//		if ( (minNorm = getSquareSum(w1coef)) > (norm = getSquareSum(w2coef)) ){
//			minNorm = norm;
//			w1coef = w2coef;
//		}
//		w2coef = hh.perform(coef);
//		if ( (minNorm) > (norm = getSquareSum(w2coef)) ){
//			minNorm = norm;
//			w1coef = w2coef;
//		}
//		w2coef = hd.perform(coef);
//		if ( (minNorm) > (norm = getSquareSum(w2coef)) ){
//			minNorm = norm;
//			w1coef = w2coef;
//		}
//			
//		return w1coef;
	}
	
	/**
	 * coefs [a,v,h,d,t]
	 * t - transformation id
	 */
	public float [] inverse(float [] coef){
		return inverse(coef, (byte)Math.round(coef[4]));
	}
	/**
	 * 
	 * @param tranform
	 * 	0 - HaarClassic(),
	 *	1 - HaarVertical(),
	 *	2 - HaarHorizotal(),
	 *	3 - HaarDiagonal() 
	 * @return
	 */
	public float [] inverse(float [] coef, byte tranform){
		return transformations[tranform].inverse(coef);
	}
	
	/**
	 * @param q coefs array
	 * @return sum of squares of 2nd, 3rd, 4th term 
	 */
	private double getSquareSum(float [] q){
		return q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
	}

}
