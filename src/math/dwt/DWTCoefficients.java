package math.dwt;

import java.io.Serializable;

public class DWTCoefficients implements Serializable{
	private static final long serialVersionUID = 7791903351562707262L;
	
	/**
	 * Matrixes of coefficients (avarege, vertical, horizontal, diagonal)
	 */
	private Matrix ma, mv, mh, md, transformationsMap;
	/*
	 * Norms of matrixes
	 */
	private long nMv = -1, nMh = -1, nMd = -1, nMa = -1;

	
	//TODO make coefs srialisable to save load them
	
	public DWTCoefficients(Matrix ma, Matrix mv, Matrix mh, Matrix md, Matrix adoptiveMap, boolean calculateMatrixNorms) {
		super();
		this.ma = ma;
		this.mv = mv;
		this.mh = mh;
		this.md = md;
		
		this.transformationsMap = adoptiveMap;
		
		if (calculateMatrixNorms){
			nMv = mv.calculateNorm();
			nMh = mh.calculateNorm();
			nMd = md.calculateNorm();
			
			nMa = ma.calculateNorm();
		}
	}

	public Matrix getMa() {
		return ma;
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
	
}
