package math.dwt.wavelets;

import math.dwt.Wavelet2DTransformation;

public class HaarDiagonal implements Wavelet2DTransformation {
	
	public String getCaption(){return "HaarDiagonal";}
	
	@Override
	public float [] perform(float [] coef){
		float a,h,v,d;
		
		//average
		a = (coef[0]+coef[1]+coef[2]+coef[3])/4;
		//	1	0
		//	0	-1
		v = (coef[0]-coef[3]);
		//	0	1
		//	-1	0
		h = (coef[1]-coef[2]);
		//	1	-1
		//	-1	1
		d = (coef[0]-coef[1]-coef[2]+coef[3]);
		return new float []{a,v,h,d};
	}
}
