/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author David Rueda
 */
public abstract class AbstractOsteoclasts {
    
    private Map<ImagePlus, OverlayStack> overlayStackMap = new LinkedHashMap<ImagePlus, OverlayStack>();
    
    protected OverlayStack getOverlayStack(ImagePlus imp) {
        if (!this.overlayStackMap.containsKey(imp)) {
            this.overlayStackMap.put(imp, new OverlayStack());
        }
        return this.overlayStackMap.get(imp);
    }

    protected ImagePlus getCurrentImp() {
        return IJ.getImage();
    }
    
    protected void updateImpOverlay(ImagePlus imp) {
        imp.setOverlay(getOverlayStack(imp).getOverlay(imp.getSlice()));
    }
}
