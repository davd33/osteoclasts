/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.gui.Overlay;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author davd33
 */
public class OverlayStack {
    
    private Map<Integer, Overlay> stackOverlays = new LinkedHashMap<Integer, Overlay>();
    
    public OverlayStack() {
    }
    
    public void put(Integer ID, Overlay overlay) {
        stackOverlays.put(ID, overlay);
    }
    
    public Overlay getOverlay(Integer ID) {
        return this.stackOverlays.get(ID);
    }
    
    public void clear() {
        this.stackOverlays.clear();
    }
}
