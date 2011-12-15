package math.dwt.wavelets;

import math.dwt.Wavelet2DTransformation;

public class HaarVertical implements Wavelet2DTransformation {
	
	public String getCaption(){return "HaarVertical";}
	
	@Override
	public float [] perform(float [] coef){
		float a,h,v,d;
		
		//average
		a = (coef[0]+coef[1]+coef[2]+coef[3])/4;
		//	1	-1
		//	1	-1
		v = (coef[0]-coef[1]+coef[2]-coef[3]);
		//	1	0
		//	-1	0
		h = (coef[0]-coef[2]);
		//	0	1
		//	0	-1
		d = (coef[1]-coef[3]);
		return new float []{a,v,h,d};
	}

	public float [] inverse(float [] coef){
		float c1,c2,c3,c4;
		float a,v,h,d;
		a = coef[0];
		v = coef[1];
		h = coef[2];
		d = coef[3];
		c1=a+(2*h+v)/4;
		c2=a+(2*d-v)/4;
		c3=a+(v-2*h)/4;
		c4=a-(v+2*d)/4;
		return new float[]{c1,c2,c3,c4};
	}
}