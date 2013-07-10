package org.osteo.gui.listeners;

import ij.IJ;
import ij.ImagePlus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.osteo.gui.ScriptFrame;

/**
 * Pop a window up that show you a selected image.
 *
 * @author David Rueda
 *
 */
public class ViewButtonActionListener extends ScriptFrameListener implements
        ActionListener {

    /**
     * Image file that we want to see.
     */
    private File image;
    /**
     * ImagePlus instance of the image the user wants to see.
     */
    private ImagePlus imp;

    /**
     * Change viewed image.
     *
     * @param image a File instance of the image
     */
    public void setImage(File image) {
        this.image = image;
    }

    public ViewButtonActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (image != null) {
            if (imp != null) {
                imp.close();
            }
            imp = IJ.openImage(image.getAbsolutePath());
            scriptFrame.getIpPanel().update(image);
        }
    }
}
