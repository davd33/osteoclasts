/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author davidr
 */
public abstract class AbstractOsteoclasts {
    
    private List<Overlay> stackOverlays;
    
    protected List<Overlay> getStackOverlays() {
        if (this.stackOverlays == null) {
            this.stackOverlays = new ArrayList<Overlay>();
        }
        return this.stackOverlays;
    }

    protected ImagePlus getCurrentImp() {
        return IJ.getImage();
    }
}
