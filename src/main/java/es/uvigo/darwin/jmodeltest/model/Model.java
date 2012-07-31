/*
Copyright (C) 2011  Diego Darriba, David Posada

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package es.uvigo.darwin.jmodeltest.model;

import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;

import pal.tree.ReadTree;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ModelTest;
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

	private boolean isInAICinterval, isInAICcinterval, isInBICinterval,
			isInDTinterval;

	private double lnL;
	/* Unconstrained LK */
	private double unconstrainedLnL;
	private double lnLIgnoringGaps;

	/* statistical */
	private double AIC, AICd, AICw, cumAICw, uAICd;
	private double AICc, AICcd, AICcw, cumAICcw, uAICcd;
	private double BIC, BICd, BICw, cumBICw, uBICd;
	private double DT, DTd, DTw, cumDTw;

	private double fA, fG, fC, fT, titv, kappa;
	private double Ra, Rb, Rc, Rd, Re, Rf, pinv, shape;

	private long computationTime;

	public Model(int mid, String mname, String mpartition, int mparameters,
			boolean mpF, boolean mpT, boolean mpV, boolean mpR, boolean mpI,
			boolean mpG, int mTi, int mTv) {

		id = mid;
		name = mname;
		partition = mpartition; // model substitution specification (e.g. GTR =
								// 012345)
		K = mparameters;
		pF = mpF; // includes unequal base frequencies
		pT = mpT; // includes ti/tv
		pV = mpV; // only for phyml: includes a Ti/Tv, apart from R(x)
		pR = mpR; // includes Rates R(x)
		pI = mpI; // includes p-inv
		pG = mpG; // includes gamma
		numTi = mTi; // number of transition rates
		numTv = mTv; // number of transversion rates

		lnL = 0.0;
		pinv = 0.0;
		shape = ModelTest.INFINITY;
	}

	public Model(int mid, String mname, String mpartition, int mparameters) {

		id = mid;
		name = mname;
		partition = mpartition; // model substitution specification (e.g. GTR =
								// 012345)
		K = mparameters;
		pF = false; // includes unequal base frequencies
		pT = false; // includes ti/tv
		pV = false; // only for phyml: includes a Ti/Tv, apart from R(x)
		pR = false; // includes Rates R(x)
		pI = false; // includes p-inv
		pG = false; // includes gamma
		numTi = 0; // number of transition rates
		numTv = 0; // number of transversion rates

		lnL = 0.0;
		pinv = 0.0;
		shape = ModelTest.INFINITY;
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

	public String printForTesting() {
		StringBuilder str = new StringBuilder();
		str.append(getName() + " ");
		str.append(ispF() ? (getfA() + " " + getfC() + " " + getfG() + " "
				+ getfT() + " ") : ("NA NA NA NA "));
		str.append(ispT() ? (getKappa() + " " + getTitv() + " ") : ("NA NA "));

		str.append(ispR() ? (getRa() + " " + getRb() + " " + getRc() + " "
				+ getRd() + " " + getRe() + " " + getRf() + " ")
				: ("NA NA NA NA NA NA "));
		str.append(ispI() ? (getPinv() + " ") : ("NA "));
		str.append(ispG() ? (getShape() + " ") : ("NA "));
		str.append(getPartition());
		return str.toString();
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

	public void setLnLIgnoringGaps(double lnLIgnoringGaps) {
		this.lnLIgnoringGaps = lnLIgnoringGaps;
	}

	public double getLnLIgnoringGaps() {
		return lnLIgnoringGaps;
	}

	public double getUnconstrainedLnL() {
		return unconstrainedLnL;
	}

	public void setUnconstrainedLnL(double unconstrainedLnL) {
		this.unconstrainedLnL = unconstrainedLnL;
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

	public void setUAICd(double uAICd) {
		this.uAICd = uAICd;
	}

	public double getUAICd() {
		return uAICd;
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

	public void setUAICcd(double uAICcd) {
		this.uAICcd = uAICcd;
	}

	public double getUAICcd() {
		return uAICcd;
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

	public void setUBICd(double uBICd) {
		this.uBICd = uBICd;
	}

	public double getUBICd() {
		return uBICd;
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

	public int getId() {
		return id;
	}

	public void setNumGammaCat(int numGammaCat) {
		this.numGammaCat = numGammaCat;
	}

	public int getNumGammaCat() {
		return numGammaCat;
	}

	public int getK() {
		return K;
	}

	public int getNumTi() {
		return numTi;
	}

	public boolean ispV() {
		return pV;
	}

	public boolean ispG() {
		return pG;
	}

	public boolean ispI() {
		return pI;
	}

	public boolean ispR() {
		return pR;
	}

	public boolean ispT() {
		return pT;
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

