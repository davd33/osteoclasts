/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.Converter;
import ij.plugin.ImageCalculator;
import ij.plugin.Thresholder;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Supply all the features for osteoclast images classification 
 * and segmentation.
 *
 * @author davidr
 */
public class Osteoclasts_ implements PlugInFilter {

    /**
     * Main Image.
     * Does not handle stacks yet.
     */
    private ImagePlus imp;
    
    /**
     * Little window with a few buttons.
     */
    private JFrame miniWin;
    
    /**
     * One unique color for managing overlays.
     */
    public static final Color OVERLAY_COLOR = Color.MAGENTA;

    /**
     * The plugin is given an image.
     * It does not manage with stacks yet.
     * 
     * @param arg useless for now
     * @param imp
     * @return 
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    /**
     * Run the osteoclast counting plugin.
     * 
     * @param ip 
     */
    @Override
    public void run(ImageProcessor ip) {
        runMiniWin();

        if (imp.getOverlay() != null) {
            imp.getOverlay().setFillColor(Osteoclasts_.OVERLAY_COLOR);
            imp.getOverlay().drawBackgrounds(true);
            imp.getOverlay().drawLabels(true);
        } else {
            imp.setOverlay(new Overlay());
        }

        final ImagePlus impML = this.imp;
        this.imp.getWindow().addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent we) {
            }

            public void windowClosing(WindowEvent we) {
            }

            public void windowClosed(WindowEvent we) {
                closeMiniWin();
            }

            public void windowIconified(WindowEvent we) {
            }

            public void windowDeiconified(WindowEvent we) {
            }

            public void windowActivated(WindowEvent we) {
            }

            public void windowDeactivated(WindowEvent we) {
            }
        });
        
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

    /**
     * Several actions that the plugin 
     * allows the user to do.
     */
    private enum Actions {

        PA("Analyze Particles", "compute the final results and save them in a csv file"),
        CLASS("Run Classifier", "analyze the original images"),
        OVERLAYS("Get Overlays", "analyze classified images");
        private String name;
        private String desc;

        public String getDesc() {
            return this.desc;
        }

        public String getName() {
            return this.name;
        }

        Actions(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    /**
     * Close the little user interface.
     */
    private void closeMiniWin() {
        if (miniWin != null) {
            miniWin.setVisible(false);
        }
        miniWin = null;
    }

    /**
     * Open the plugin's window.
     */
    private void runMiniWin() {
        miniWin = new JFrame();
        miniWin.setLayout(new BorderLayout());
        miniWin.setVisible(true);

        JPanel actionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JButton source = (JButton) ae.getSource();

                if (source.getText().equals(Actions.PA.getName())) {
                    pa();
                } else if (source.getText().equals(Actions.CLASS.getName())) {
                    classify();
                } else if (source.getText().equals(Actions.OVERLAYS.getName())) {
                    overlays();
                }
            }
        };

        for (Actions action : Actions.values()) {
            JButton actionButton = new JButton(action.getName());
            actionButton.setToolTipText(action.getDesc());
            actionsPanel.add(actionButton, c);
            actionButton.addActionListener(actionListener);
        }

        miniWin.getContentPane().add(actionsPanel, BorderLayout.CENTER);
        miniWin.pack();
    }

    /**
     * Run the classifier on selected image.
     */
    private void classify() {
        System.out.println("classifier");
    }

    /**
     * Get the overlays.
     * Analyze probability images, which 
     * are the results of the classification.
     */
    private void overlays() {
        System.out.println("overlays");
    }

    /**
     * After classification is corrected (by hand),
     * runs the particle analysis and save the 
     * results in a csv file.
     */
    private void pa() {
        Overlay o = imp.getOverlay();
        o.drawLabels(false);
        o.drawNames(false);
        o.drawBackgrounds(true);
        IJ.log(o.size() + " detected osteoclasts");

        RoiManager rm = new RoiManager(true);
        for (int i = 0; i < o.size(); i++) {
            rm.addRoi(o.get(i));
        }

        IJ.log("" + rm.getCount());

        byte[] maskBytes = new byte[imp.getWidth() * imp.getHeight()];
        Arrays.fill(maskBytes, (byte) 255);
        ImagePlus mask = new ImagePlus("mask", new ByteProcessor(imp.getWidth(), imp.getHeight(), maskBytes));
        mask.show();

        for (Roi roi : rm.getRoisAsArray()) {
            mask.getProcessor().setColor(Color.BLACK);
            mask.getProcessor().fill(roi);
        }
    }

    /**
     * Color threshold.
     * It's not working well...
     * 
     * @param impLocal
     * @param color
     * @return 
     */
    private ImagePlus threshold(ImagePlus impLocal, Color color) {
        int[] min = new int[3];
        int[] max = new int[3];
        String[] filter = new String[3];

        impLocal.show();

        ColorProcessor cpLocal = ((ColorProcessor) impLocal.getProcessor());
        ImagePlus[] channels = new ImagePlus[3];
        for (int i = 0; i < 3; i++) {
            ByteProcessor bp = new ByteProcessor(impLocal.getWidth(), impLocal.getHeight(), cpLocal.getChannel(i));
            channels[i] = new ImagePlus("channel " + i, bp);
            channels[i].show();
        }

        min[0] = color.getRed();
        max[0] = 255;
        filter[0] = "pass";
        min[1] = color.getGreen();
        max[1] = 255;
        filter[1] = "pass";
        min[2] = color.getBlue();
        max[2] = 255;
        filter[2] = "pass";

        for (int i = 0; i < 3; i++) {
            ImagePlus channel = channels[0];
            ImageProcessor channelProcessor = channel.getProcessor();
            channelProcessor.setThreshold(min[i], max[i], ImageProcessor.BLACK_AND_WHITE_LUT);
            channelProcessor.threshold(min[i]);

            if (filter[i].equals("stop")) {
                IJ.run("Invert");
            }
        }

        ImageCalculator ic = new ImageCalculator();
        ImagePlus binary = ic.run("AND create", channels[0], channels[1]);
        binary = ic.run("AND create", binary, channels[2]);

        binary.show();
        return binary;
    }

    /**
     * Log message into console.
     * 
     * @param message 
     */
    private void log(String message) {
        System.out.println(message);
    }
}
