package org.osteo.scripts.util;

public class ROCPoint {

	private double fpRate;
	private double tpRate;
	
	public double getFpRate() {
		return fpRate;
	}

	public void setFpRate(double fpRate) {
		this.fpRate = fpRate;
	}

	public double getTpRate() {
		return tpRate;
	}

	public void setTpRate(double tpRate) {
		this.tpRate = tpRate;
	}

	public ROCPoint(double fpRate, double tpRate) {
		this.fpRate = fpRate;
		this.tpRate = tpRate;
	}
}
