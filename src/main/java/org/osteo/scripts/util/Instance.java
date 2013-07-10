package org.osteo.scripts.util;

public class Instance {

	private boolean positive;
	private double value;
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isPositive() {
		return positive;
	}

	public void setPositive(boolean positive) {
		this.positive = positive;
	}


	public Instance(double value, boolean positive) {
		this.value = value;
		this.positive = positive;
	}
}
