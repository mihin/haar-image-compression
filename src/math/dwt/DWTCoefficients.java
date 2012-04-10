package math.dwt;

import java.io.Serializable;

public class DWTCoefficients implements Serializable, Composable{
	private static final long serialVersionUID = 7791903351562707262L;
	
	/**
	 * Matrixes of coefficients (avarege, vertical, horizontal, diagonal)
	 */
	private Matrix mv, mh, md, transformationsMap;
	private Composable ma;
	private Composable composed = null;
	/*
	 * Norms of matrixes
	 */
	private long nMv = -1, nMh = -1, nMd = -1, nMa = -1;

	
	//TODO make coefs srialisable to save load them
	
	public DWTCoefficients(Composable ma, Matrix mv, Matrix mh, Matrix md, Matrix adoptiveMap, boolean calculateMatrixNorms) {
		super();
		this.ma = ma;
		this.mv = mv;
		this.mh = mh;
		this.md = md;
		
		this.transformationsMap = adoptiveMap;
		
		if (ma.getTransform() != null) transform = ma.getTransform(); //inheritance of the transformation type  
		
		if (calculateMatrixNorms){
			nMv = mv.calculateNorm();
			nMh = mh.calculateNorm();
			nMd = md.calculateNorm();
			
//			nMa = ma.compose().calculateNorm();
		}
	}

	public Matrix getMa() {
		return ma.compose();
	}

	public Matrix getMv() {
		return mv;
	}

	public Matrix getMh() {
		return mh;
	}

	public Matrix getMd() {
		return md;
	}
	
	public Matrix getMap() {
		return transformationsMap;
	}

	public long getNormMv() {
		return nMv;
	}

	public long getNormMh() {
		return nMh;
	}

	public long getNormMd() {
		return nMd;
	}
	
	public long getNormMa() {
		return nMa;
	}
	public long getNormVHDSum() {
		return nMv+nMh+nMd;
	}

	@Override
	public Matrix compose() {
		if (composed == null){
			// TODO return composition for this coefs
			composed = new DWT(getTransform()).reconstruct(this);
		}
		return composed.compose();
	}

	private Wavelet2DTransformation transform;
	@Override
	public Wavelet2DTransformation getTransform() {
		return transform;
	}
	
}
