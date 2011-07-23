/** 
 * Model.java
 *
 * Description:		Model specifications
 * @author			David Posada, University of Vigo, Spain  
 *					dposada@uvigo.es | darwin.uvigo.es
 */

package es.uvigo.darwin.jmodeltest.model;

import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;

import pal.tree.ReadTree;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;

public class Model implements Serializable {

	private static final long serialVersionUID = 4173257281316945987L;
	
	private String name, partition, treeString;
	private Tree tree;
	
	private int id;
	private int K;
	private int numTi;
	private int numTv;
	private int numGammaCat;
	
	private boolean pF, pT, pR, pI, pG, pV;
	
	private boolean isCandidate, isInAICinterval, isInAICcinterval, 
			isInBICinterval, isInDTinterval;
	
	private double lnL;

	/* statistical */
	private double AIC, AICd, AICw, cumAICw;
	private double AICc, AICcd, AICcw, cumAICcw;
	private double BIC, BICd, BICw, cumBICw;
	private double DT, DTd, DTw, cumDTw;

	private double fA, fG, fC, fT, titv, kappa;
	private double Ra, Rb, Rc, Rd, Re, Rf, pinv, shape;
	
	private long computationTime;

	public static Model JC, JCI, JCG, JCIG, F81, F81I, F81G, F81IG, K80, K80I,
			K80G, K80IG, HKY, HKYI, HKYG, HKYIG, TrNef, TrNefI, TrNefG,
			TrNefIG, TrN, TrNI, TrNG, TrNIG, TPM, TPMI, TPMG, TPMIG, TPM1,
			TPM1I, TPM1G, TPM1IG, TPM1uf, TPM1ufI, TPM1ufG, TPM1ufIG, TPM2,
			TPM2I, TPM2G, TPM2IG, TPMuf, TPMufI, TPMufG, TPMufIG, TPM2uf,
			TPM2ufI, TPM2ufG, TPM2ufIG, TPM3, TPM3I, TPM3G, TPM3IG, TPM3uf,
			TPM3ufI, TPM3ufG, TPM3ufIG, TIMef, TIMefI, TIMefG, TIMefIG, TIM,
			TIMI, TIMG, TIMIG, TIM1ef, TIM1efI, TIM1efG, TIM1efIG, TIM1, TIM1I,
			TIM1G, TIM1IG, TIM2ef, TIM2efI, TIM2efG, TIM2efIG, TIM2, TIM2I,
			TIM2G, TIM2IG, TIM3ef, TIM3efI, TIM3efG, TIM3efIG, TIM3, TIM3I,
			TIM3G, TIM3IG, TVMef, TVMefI, TVMefG, TVMefIG, TVM, TVMI, TVMG,
			TVMIG, SYM, SYMI, SYMG, SYMIG, GTR, GTRI, GTRG, GTRIG;

	//TODO: As the constructor is never called with known values, this
	//      kind of constructor with lots of parameters is totally unnecessary.
	public Model(int mid, String mname, String mpartition, double mlnL,
			int mparameters, boolean mpF, boolean mpT, boolean mpV,
			boolean mpR, boolean mpI, boolean mpG, int mTi, int mTv,
			double mfA,	double mfC, double mfG, double mfT, double mtitv, 
			double mkappa, double mRa, double mRb, double mRc, double mRd, 
			double mRe,	double mRf, double mpinv, double mshape, /* int mnumGammaCat, */
			boolean misCandidate) {
		
		id = mid;
		name = mname;
		partition = mpartition; // model substitution specification (e.g. GTR =
								// 012345)
		lnL = mlnL;
		K = mparameters;
		pF = mpF; // includes unequal base frequencies
		pT = mpT; // includes ti/tv
		pV = mpV; // only for phyml: includes a Ti/Tv, apart from R(x)
		pR = mpR; // includes Rates R(x)
		pI = mpI; // includes p-inv
		pG = mpG; // includes gamma
		numTi = mTi; // number of transition rates
		numTv = mTv; // number of transversion rates
		fA = mfA;
		fC = mfC;
		fG = mfG;
		fT = mfT;
		titv = mtitv;
		kappa = mkappa;
		Ra = mRa;
		Rb = mRb;
		Rc = mRc;
		Rd = mRd;
		Re = mRe;
		Rf = mRf;
		pinv = mpinv;
		shape = mshape;
		isCandidate = misCandidate;
	}

	/****************************
	 * print ************************************ * Print model components * *
	 ************************************************************************/

	public void print(TextOutputStream stream) {

		stream.println("   Model = " + getName());
		stream.println("   partition = " + getPartition());

		if (getLnL() == 0) {
			stream.println(" OPTIMIZATION FAILED!");
		} else {
			stream.printf("   -lnL = %.4f", getLnL());
			stream.printf("\n   K = %d", getK());
			/*
			 * stream.printf ("\n     optimized substitution parameters = %d",
			 * Model.freeParameters[id]); if (ModelTest.countBLasParameters ==
			 * true) stream.printf ("\n     optimized branch lengths = %d",
			 * ModelTest.numBranches); if (ModelTest.optimizeMLTopology)
			 * stream.printf ("\n     optimized topology = %d", 1);
			 */
			if (ispF()) {
				stream.printf("\n   freqA = %5.4f ", getfA());
				stream.printf("\n   freqC = %5.4f ", getfC());
				stream.printf("\n   freqG = %5.4f ", getfG());
				stream.printf("\n   freqT = %5.4f ", getfT());
			}
			if (ispT()) {
				stream.printf("\n   kappa = %.4f", getKappa());
				stream.printf(" (ti/tv = %.4f)", getTitv());
			}
			if (ispR()) {
				stream.printf("\n   R(a) [AC] =  %.4f", getRa());
				stream.printf("\n   R(b) [AG] =  %.4f", getRb());
				stream.printf("\n   R(c) [AT] =  %.4f", getRc());
				stream.printf("\n   R(d) [CG] =  %.4f", getRd());
				stream.printf("\n   R(e) [CT] =  %.4f", getRe());
				stream.printf("\n   R(f) [GT] =  %.4f", 1.0);
			}
			if (ispI())
				stream.printf("\n   p-inv = %5.4f", getPinv());
			if (ispG()) {
				stream.printf("\n   gamma shape = %6.4f", getShape());
			}
			stream.println(" ");
		}
	}

	



	public void setCumBICw(double cumBICw) {
		this.cumBICw = cumBICw;
	}

	public double getCumBICw() {
		return cumBICw;
	}

	public void setLnL(double lnL) {
		this.lnL = lnL;
	}

	public double getLnL() {
		return lnL;
	}

	public void setAIC(double aIC) {
		AIC = aIC;
	}

	public double getAIC() {
		return AIC;
	}

	public void setAICd(double aICd) {
		AICd = aICd;
	}

	public double getAICd() {
		return AICd;
	}

	public void setAICw(double aICw) {
		AICw = aICw;
	}

	public double getAICw() {
		return AICw;
	}

	public void setCumAICw(double cumAICw) {
		this.cumAICw = cumAICw;
	}

	public double getCumAICw() {
		return cumAICw;
	}

	public void setAICc(double aICc) {
		AICc = aICc;
	}

	public double getAICc() {
		return AICc;
	}

	public void setAICcd(double aICcd) {
		AICcd = aICcd;
	}

	public double getAICcd() {
		return AICcd;
	}

	public void setAICcw(double aICcw) {
		AICcw = aICcw;
	}

	public double getAICcw() {
		return AICcw;
	}

	public void setCumAICcw(double cumAICcw) {
		this.cumAICcw = cumAICcw;
	}

	public double getCumAICcw() {
		return cumAICcw;
	}

	public void setBICd(double bICd) {
		BICd = bICd;
	}

	public double getBICd() {
		return BICd;
	}

	public void setBIC(double bIC) {
		BIC = bIC;
	}

	public double getBIC() {
		return BIC;
	}

	public void setBICw(double bICw) {
		BICw = bICw;
	}

	public double getBICw() {
		return BICw;
	}

	public void setDT(double dT) {
		DT = dT;
	}

	public double getDT() {
		return DT;
	}

	public void setCumDTw(double cumDTw) {
		this.cumDTw = cumDTw;
	}

	public double getCumDTw() {
		return cumDTw;
	}

	public void setDTw(double dTw) {
		DTw = dTw;
	}

	public double getDTw() {
		return DTw;
	}

	public void setDTd(double dTd) {
		DTd = dTd;
	}

	public double getDTd() {
		return DTd;
	}

	public String getName() {
		return name;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	public String getPartition() {
		return partition;
	}

	public Tree getTree() {
		return tree;
	}
	
	public void setTreeString(String tree) throws TreeParseException {
		this.treeString = tree;

		if (tree == null)
			throw new TreeParseException("Attempting to set a null tree");
		StringReader sr = new StringReader(tree);
		this.tree = new ReadTree(new PushbackReader(sr));
	}

	public String getTreeString() {
		return treeString;
	}

	public void setfG(double fG) {
		this.fG = fG;
	}

	public double getfG() {
		return fG;
	}

	public void setfA(double fA) {
		this.fA = fA;
	}

	public double getfA() {
		return fA;
	}

	public void setfC(double fC) {
		this.fC = fC;
	}

	public double getfC() {
		return fC;
	}

	public void setfT(double fT) {
		this.fT = fT;
	}

	public double getfT() {
		return fT;
	}

	public void setTitv(double titv) {
		this.titv = titv;
	}

	public double getTitv() {
		return titv;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	public double getKappa() {
		return kappa;
	}

	public void setRa(double ra) {
		Ra = ra;
	}

	public double getRa() {
		return Ra;
	}

	public void setShape(double shape) {
		this.shape = shape;
	}

	public double getShape() {
		return shape;
	}

	public void setPinv(double pinv) {
		this.pinv = pinv;
	}

	public double getPinv() {
		return pinv;
	}

	public void setRf(double rf) {
		Rf = rf;
	}

	public double getRf() {
		return Rf;
	}

	public void setRe(double re) {
		Re = re;
	}

	public double getRe() {
		return Re;
	}

	public void setRd(double rd) {
		Rd = rd;
	}

	public double getRd() {
		return Rd;
	}

	public void setRc(double rc) {
		Rc = rc;
	}

	public double getRc() {
		return Rc;
	}

	public void setRb(double rb) {
		Rb = rb;
	}

	public double getRb() {
		return Rb;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setNumGammaCat(int numGammaCat) {
		this.numGammaCat = numGammaCat;
	}

	public int getNumGammaCat() {
		return numGammaCat;
	}

	public void setK(int k) {
		K = k;
	}

	public int getK() {
		return K;
	}

	public void setNumTi(int numTi) {
		this.numTi = numTi;
	}

	public int getNumTi() {
		return numTi;
	}

	public void setpV(boolean pV) {
		this.pV = pV;
	}

	public boolean ispV() {
		return pV;
	}

	public void setpG(boolean pG) {
		this.pG = pG;
	}

	public boolean ispG() {
		return pG;
	}

	public void setpI(boolean pI) {
		this.pI = pI;
	}

	public boolean ispI() {
		return pI;
	}

	public void setpR(boolean pR) {
		this.pR = pR;
	}

	public boolean ispR() {
		return pR;
	}

	public void setpT(boolean pT) {
		this.pT = pT;
	}

	public boolean ispT() {
		return pT;
	}

	public void setpF(boolean pF) {
		this.pF = pF;
	}

	public boolean ispF() {
		return pF;
	}

	public void setInDTinterval(boolean isInDTinterval) {
		this.isInDTinterval = isInDTinterval;
	}

	public boolean isInDTinterval() {
		return isInDTinterval;
	}

	public void setInBICinterval(boolean isInBICinterval) {
		this.isInBICinterval = isInBICinterval;
	}

	public boolean isInBICinterval() {
		return isInBICinterval;
	}

	public void setInAICcinterval(boolean isInAICcinterval) {
		this.isInAICcinterval = isInAICcinterval;
	}

	public boolean isInAICcinterval() {
		return isInAICcinterval;
	}

	public void setInAICinterval(boolean isInAICinterval) {
		this.isInAICinterval = isInAICinterval;
	}

	public boolean isInAICinterval() {
		return isInAICinterval;
	}

	public boolean isCandidate() {
		return isCandidate;
	}

	public int getNumTv() {
		return numTv;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (pF ? 1231 : 1237);
		result = prime * result + (pG ? 1231 : 1237);
		result = prime * result + (pI ? 1231 : 1237);
		result = prime * result
				+ ((partition == null) ? 0 : partition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Model other = (Model) obj;
		if (pF != other.pF)
			return false;
		if (pG != other.pG)
			return false;
		if (pI != other.pI)
			return false;
		if (partition == null) {
			if (other.partition != null)
				return false;
		} else if (!partition.equals(other.partition))
			return false;
		return true;
	}

	public long getComputationTime() {
		return computationTime;
	}

	public void setComputationTime(long computationTime) {
		this.computationTime = computationTime;
	}
	
	public void update(Model model) {
		if (this.equals(model)) {
			lnL = model.lnL;
			numGammaCat = model.numGammaCat;
			shape = model.shape;
			pinv = model.pinv;
			kappa = model.kappa;
			treeString = model.treeString;
			tree = model.tree;
			computationTime = model.computationTime;
		}
	}
} // class Model

