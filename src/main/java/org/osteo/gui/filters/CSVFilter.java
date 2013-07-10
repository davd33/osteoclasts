package org.osteo.gui.filters;

import java.io.File;
import java.io.Serializable;

import javax.swing.filechooser.FileFilter;

import org.osteo.main.Bundle;

public class CSVFilter extends FileFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Extensions allowed by the filter.
	 */
	public static final String[] extensions = new String[] { "csv" };

	@Override
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;

		String fileName = f.getName().toLowerCase();
		for (int e = 0; e < extensions.length; e++) {
			String extension = extensions[e];
			if (fileName.toLowerCase().endsWith(extension)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getDescription() {
		return Bundle.UI.getString("csvfilter_desc");
	}

}
