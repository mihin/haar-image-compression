package math.dwt;

public interface Wavelet2DTransformation {
	/**
	 * @param coef
	 * @return array [a,v,h,d]
	 */
	public float [] perform(float [] coef);
	
	public String getCaption();

}
