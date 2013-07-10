package org.osteo.gui.filters;

import org.osteo.main.Bundle;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: davidr
 * Date: 7/3/13
 * Time: 7:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImagesFilter extends FileFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Extensions allowed by the filter (jpg, png, tiff).
     */
    public static final String[] extensions = new String[] { "jpg", "jpeg",
            "png", "tiff", "tif" };

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
        return Bundle.UI.getString("filefilter_desc");
    }


}
