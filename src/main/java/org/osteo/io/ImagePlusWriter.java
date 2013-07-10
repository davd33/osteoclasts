package org.osteo.io;

import ij.ImagePlus;
import ij.io.TiffEncoder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This was written for facilitating saving images with ImageJ library. For the
 * moment this class is able to save only 'Tiff' files.
 * 
 * @author David Rueda
 * 
 */
public class ImagePlusWriter {

	/**
	 * ImagePlus instance of the image to be saved.
	 */
	private ImagePlus imp;

	/**
	 * Destination directory where the image will be saved.
	 */
	private String path;

	public ImagePlusWriter() {
	}

	public ImagePlusWriter(ImagePlus imp) {
		this.imp = imp;
	}

	public ImagePlusWriter(ImagePlus imp, String path) {
		this.imp = imp;
		this.path = path;
	}

	public void saveTiff() throws IOException {
		DataOutputStream out = null;
		try {
			TiffEncoder file = new TiffEncoder(imp.getFileInfo());
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(path)));
			file.write(out);
		} finally {
			out.close();
		}
	}

	public void saveTiff(String path) throws IllegalAccessException,
			IOException {
		if (this.imp == null)
			throw new IllegalAccessException("No image specified");
		if (path == null || path.isEmpty())
			throw new IllegalArgumentException("Wrong or null path");

		this.path = path;
		saveTiff();
	}

	public void saveTiff(ImagePlus imp, String path)
			throws IllegalAccessException, IOException {
		if (imp == null)
			throw new IllegalAccessException("No image specified");
		if (path == null || path.isEmpty())
			throw new IllegalArgumentException("Wrong or null path");

		this.imp = imp;
		this.path = path;
		saveTiff();
	}
}
