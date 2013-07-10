package org.osteo.ij;

import ij.plugin.frame.RoiManager;

import java.awt.Point;

public class ROISelection {

	public ROISelection() {
	}
	
	public boolean hihglightSelection(Point pixel) {
		RoiManager manager = RoiManager.getInstance();
		if (manager == null)
			manager = new RoiManager();
		if (manager.contains(pixel)) {
			return true;
		}
		return false;
	}
}
