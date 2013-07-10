package org.osteo.scripts.util;

import java.awt.Point;

public class Osteoclast {

	private int id;
	private Point pixel;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Point getPixel() {
		return pixel;
	}

	public void setPixel(Point pixel) {
		this.pixel = pixel;
	}

	public Osteoclast(int id, Point pixel) {
		this.id = id;
		this.pixel = pixel;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Osteoclast) {
			if (((Osteoclast) obj).getId() == id) {
				return true;
			} else {
				return false;
			}
		}
		return super.equals(obj);
	}
}
