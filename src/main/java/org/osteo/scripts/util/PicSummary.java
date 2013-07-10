package org.osteo.scripts.util;

/**
 * Available values in the particle analysis summary.
 * 
 * @author David Rueda
 *
 */
public enum PicSummary {
	COUNT("Count"), AREA_TOT("Total Area"), AREA_AVG("Average Size"), 
	AREA_PERCENT("Area Fraction"), MEAN("Mean");
	
	private String headName;
	
	public String getHeadName() {
		return this.headName;
	}
	
	PicSummary(String headName) {
		this.headName = headName;
	}
	
	public static String headNames() {
		StringBuilder str = new StringBuilder("Slice");
		str.append(',');
		PicSummary[] values = PicSummary.values();
		for (int v = 0; v < values.length; v++) {
			PicSummary s = values[v];
			str.append(s.headName);
			if (v < (values.length - 1))
				str.append(',');
		}
		return str.substring(0);
	}
}
