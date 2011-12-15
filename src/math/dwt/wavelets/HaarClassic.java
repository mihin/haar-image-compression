package math.dwt.wavelets;

import math.dwt.Wavelet2DTransformation;

public class HaarClassic implements Wavelet2DTransformation {
	
	public String getCaption(){return "HaarClassic";}
	
	@Override
	public float [] perform(float [] coef){
		float a,h,v,d;
		
		//average
		a = (coef[0]+coef[1]+coef[2]+coef[3])/4;
		//	1	-1
		//	1	-1
		v = (coef[0]-coef[1]+coef[2]-coef[3]);
		//	1	1
		//	-1	-1
		h = (coef[0]+coef[1]-coef[2]-coef[3]);
		//	1	-1
		//	-1	1
		d = (coef[0]-coef[1]-coef[2]+coef[3]);
		return new float []{a,v,h,d};
	}

	public float [] inverse(float [] coef){
		float c1,c2,c3,c4;
		float a,v,h,d;
		a = coef[0];
		v = coef[1];
		h = coef[2];
		d = coef[3];
		c1=(v+h+d)/4+a;
		c2=a+(h-v-d)/4;
		c3=a+(v-h-d)/4;
		c4=a+(d-v-h)/4;
		return new float[]{c1,c2,c3,c4};
	}
}
