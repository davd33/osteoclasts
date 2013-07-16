/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;

/**
 *
 * @author davidr
 */
public class Osteoclasts_ implements PlugInFilter {

    private ImagePlus imp;
    private JFrame miniWin;

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (imp.getOverlay() != null) {
            imp.getOverlay().setFillColor(Color.GREEN);
            imp.getOverlay().drawBackgrounds(true);
            imp.getOverlay().drawLabels(true);
        } else {
            imp.setOverlay(new Overlay());
        }

        final ImagePlus impML = this.imp;
        this.imp.getCanvas().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.isControlDown() && impML.getOverlay() != null && impML.getRoi() != null) {
                    impML.getOverlay().add(impML.getRoi());
                    impML.getOverlay().setFillColor(Color.GREEN);
                    impML.getOverlay().drawBackgrounds(true);
                    impML.getOverlay().drawLabels(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });
    }

    private void log(String message) {
        System.out.println(message);
    }
}
