package math.dwt;

public class DWTCoefficients {
	/**
	 * Matrixes of coefficients (avarege, vertical, horizontal, diagonal)
	 */
	private Matrix ma, mv, mh, md;
	/*
	 * Norms of matrixes
	 */
	private double nMv = -1, nMh = -1, nMd = -1;

	public DWTCoefficients(Matrix ma, Matrix mv, Matrix mh, Matrix md, boolean calculateMatrixNorms) {
		super();
		this.ma = ma;
		this.mv = mv;
		this.mh = mh;
		this.md = md;
		
		if (calculateMatrixNorms){
			nMv = mv.calculateNorm();
			nMh = mh.calculateNorm();
			nMd = md.calculateNorm();
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

	public double getNormMv() {
		return nMv;
	}

	public double getNormMh() {
		return nMh;
	}

	public double getNormMd() {
		return nMd;
	}
	
	
	
}
