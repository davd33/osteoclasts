/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.gui.Overlay;
import ij.gui.Roi;
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
        if (overlay != null) {
            for (Roi roi : overlay.toArray()) {
                roi.setPosition(ID);
            }
        }
        stackOverlays.put(ID, overlay);
    }

    public void set(OverlayStack ovStack) {
        if (ovStack == null) {
            return;
        }
        if (ovStack.stackOverlays == null) {
            this.stackOverlays = new LinkedHashMap<Integer, Overlay>();
        } else {
            this.stackOverlays = ovStack.stackOverlays;
        }
    }

    public Overlay getOverlay(Integer ID) {
        return this.stackOverlays.get(ID);
    }

    public void clear() {
        this.stackOverlays.clear();
    }
}
