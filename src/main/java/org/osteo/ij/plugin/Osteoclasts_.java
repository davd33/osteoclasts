/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.Converter;
import ij.plugin.ImageCalculator;
import ij.plugin.Thresholder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Supply all the features for osteoclast images classification and
 * segmentation.
 *
 * @author davidr
 */
public class Osteoclasts_ implements PlugInFilter {

    /**
     * Main Image. Does not handle stacks yet.
     */
    private ImagePlus imp;
    /**
     * Little window with a few buttons.
     */
    private JFrame miniWin;
    private JPanel miniWinInfosPanel;
    /**
     * One unique color for managing overlays.
     */
    public static final Color OVERLAY_COLOR = Color.MAGENTA;
    /**
     * Simulate infinity.
     */
    protected final static double INFINITY = 1.0 / 0.0;

    /**
     * The plugin is given an image. It does not manage with stacks yet.
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
        logToMiniWin("Welcome Ana! ;)");

        if (imp.getOverlay() == null) {
            imp.setOverlay(new Overlay());
        }
        
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
    }

    /**
     * Several actions that the plugin allows the user to do.
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
     * Display little message into little window.
     *
     * @param message
     */
    private void logToMiniWin(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Osteoclasts_.this.miniWinInfosPanel.add(new JLabel(message));
                Osteoclasts_.this.miniWinInfosPanel.updateUI();
                Osteoclasts_.this.miniWin.pack();
            }
        });
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

        miniWinInfosPanel = new JPanel();

        miniWin.getContentPane().add(miniWinInfosPanel, BorderLayout.NORTH);
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
     * Get the overlays. Analyze probability images, which are the results of
     * the classification.
     */
    private void overlays() {
        System.out.println("overlays");
    }

    /**
     * After classification is corrected (by hand), runs the particle analysis
     * and save the results in a csv file.
     */
    private void pa() {
        Overlay o = imp.getOverlay();

        RoiManager rm = new RoiManager(true);
        for (int i = 0; i < o.size(); i++) {
            rm.addRoi(o.get(i));
        }

        byte[] maskBytes = new byte[imp.getWidth() * imp.getHeight()];
        Arrays.fill(maskBytes, (byte) 255);
        ImagePlus mask = new ImagePlus("mask", new ByteProcessor(imp.getWidth(), imp.getHeight(), maskBytes));

        for (Roi roi : rm.getRoisAsArray()) {
            mask.getProcessor().setColor(Color.BLACK);
            mask.getProcessor().fill(roi);
        }
        
        mask.show();

        // analyze particles
        ResultsTable paResults = new ResultsTable();
        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.IN_SITU_SHOW
                | ParticleAnalyzer.INCLUDE_HOLES
                | ParticleAnalyzer.SHOW_NONE,
                Measurements.AREA | Measurements.MEAN
                | Measurements.MIN_MAX | Measurements.CENTROID
                | Measurements.PERIMETER
                | Measurements.SHAPE_DESCRIPTORS,
                paResults,
                0d, INFINITY, 0d, 1d);
        particleAnalyzer.analyze(mask);
        try {
            String path = IJ.getFilePath("Where should the results be saved?");
            if (path != null) {
                File file = new File(path);

                if (file.exists()) {
                    boolean nonetheless = IJ.showMessageWithCancel(
                            "Save results...",
                            "\"" + file.getName() + "\" already exists.\nDo you want to replace it?");
                    if (nonetheless) {
                        paResults.saveAs(path);
                    }
                } else {
                    paResults.saveAs(path);
                }
            }
        } catch (IOException ex) {
            IJ.log("Unable to save the results file.");
            log(ex.getMessage());
        }
    }

    /**
     * Color threshold. It's not working well... It actually is under
     * implementation... :D Since I don't need it for the plugin, I won't
     * continue developing this method until I have to.
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
