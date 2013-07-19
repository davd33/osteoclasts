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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
        runMiniWin();
        
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
    
    private static final int WIN_N_ACTIONS = 5;
    
    private enum Actions {
        PA("Analyze Particles"),
        OPEN("Open Images"),
        CLASS("Run Classifier"),
        OVERLAYS("Get Overlays"),
        OPTIONS("Options");
        
        private String name;
        
        public String getName() {
            return this.name;
        }
        
        Actions(String name) {
            this.name = name;
        }
    }
    
    private void runMiniWin() {
        miniWin = new JFrame();
        miniWin.setVisible(true);
        
        JButton[] actionsButtons = new JButton[this.WIN_N_ACTIONS];
        JPanel actionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JButton source = (JButton) ae.getSource();
                
                if (source.getText().equals(Actions.PA.getName())) {
                    System.out.println("particle analysis");
                } else if (source.getText().equals(Actions.CLASS.getName())) {
                    System.out.println("classifier");
                } else if (source.getText().equals(Actions.OPEN.getName())) {
                    System.out.println("open images");
                } else if (source.getText().equals(Actions.OPTIONS.getName())) {
                    System.out.println("options");
                } else if (source.getText().equals(Actions.OVERLAYS.getName())) {
                    System.out.println("overlays");
                }
            }
        };
        
        for (Actions action : Actions.values()) {
            JButton actionButton = new JButton(action.getName());
            actionsPanel.add(actionButton, c);
            actionButton.addActionListener(actionListener);
        }
        
        miniWin.getContentPane().add(actionsPanel);
        miniWin.pack();
    }

    private void log(String message) {
        System.out.println(message);
    }
}
