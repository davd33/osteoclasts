/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;

/**
 *
 * @author David Rueda
 */
public abstract class AbstractOsteoclasts {
    
    private Map<ImagePlus, OverlayStack> overlayStackMap = new LinkedHashMap<ImagePlus, OverlayStack>();
    private String resultsPath;
    private List<JButton> buttons = new LinkedList<JButton>();
    
    protected void registerButton(JButton button) {
        this.buttons.add(button);
    }
    
    protected JButton getRegisteredButtonByText(String text) {
        for (Iterator<JButton> bIt = buttons.iterator(); bIt.hasNext();) {
            JButton button = bIt.next();
            if (button.getText().equals(text)) {
                return button;
            }
        }
        return null;
    }
    
    protected void blockRegisteredButtons() {
        for (Iterator<JButton> bIt = buttons.iterator(); bIt.hasNext();) {
            JButton button = bIt.next();
            button.setEnabled(false);
        }
    }
    
    protected void activateRegisteredButtons() {
        for (Iterator<JButton> bIt = buttons.iterator(); bIt.hasNext();) {
            JButton button = bIt.next();
            button.setEnabled(true);
        }
    }

    protected String getResultsPath() {
        if (this.resultsPath == null || !(new File(this.resultsPath)).exists()) {
            return null;
        }
        return resultsPath;
    }

    protected void setResultsPath(String resultsPath) {
        this.resultsPath = resultsPath;
    }
    
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
