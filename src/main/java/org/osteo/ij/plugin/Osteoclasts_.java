/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.IJEventListener;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
import ij.plugin.EventListener;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RankFilters;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.osteo.ij.morph.GrayMorphology_;

/**
 * Supply all the features for osteoclast images classification and
 * segmentation.
 *
 * @author davidr
 */
public class Osteoclasts_ implements PlugIn {
    
    public final String mfRadiusName  = "Median filter radius";
    public final String mfRepeatName  = "Median filter repetitions";
    public final String paMinSizeName = "Particle analyzer min size";
    public final String paMinCircName = "Particle analyzer min circularity";
    public final String paMaxCircName = "Particle analyzer max circularity";
    public final String incBright     = "Increase Brightness";

    /**
     * Little window with a few buttons.
     */
    private static JFrame miniWin;
    private JPanel miniWinInfosPanel;
    /**
     * One unique color for managing overlays.
     */
    public static final Color OVERLAY_COLOR = Color.MAGENTA;
    /**
     * Simulate infinity.
     */
    protected final static double INFINITY = 1.0 / 0.0;

    private ImagePlus getCurrentImp() {
        return IJ.getImage();
    }

    /**
     * Basic constructor.
     */
    public Osteoclasts_() {
    }

    /**
     * Run the osteoclast counting plugin.
     *
     * @param ip
     */
    @Override
    public void run(String arg) {
        if (miniWin == null) {
            runMiniWin();
            logToMiniWin("Welcome Ana! ;)");
        } else {
            miniWin.setVisible(true);
        }
        
        IJ.run("Labels...", "color=blue font=12 show draw");
        IJ.run("Overlay Options...", "stroke=yellow width=2 fill=none");
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
     * Analyze probability image.
     * 
     * @param workingImg
     * @param os
     * @return 
     */
    private ImagePlus applyIPP(ImagePlus workingImg, OptionSet os) {
        RankFilters rf = new RankFilters();

        workingImg.getStack().deleteLastSlice();
        workingImg.setProcessor(workingImg.getProcessor().convertToByte(true));
        ImageProcessor workingProcessor = workingImg.getProcessor();

        // increase brightness in working image
        if (os.getOption(incBright).isSelected()) {
            // close 5px
            GrayMorphology_ gm = new GrayMorphology_();
            gm.setup("radius=5 type=circle operator=close", workingImg);
            gm.run(workingProcessor);
            // median filter
            Double n = Double.parseDouble(os.getOptionValue(mfRepeatName));
            Integer medRadius = Integer.parseInt(os.getOptionValue(mfRadiusName).split("\\.")[0]);
            for (int i = 0; i < n; i++) {
                rf.rank(workingProcessor, medRadius, RankFilters.MEDIAN);
            }
        }

        ImagePlus edgesImg = workingImg.duplicate();
        ImageProcessor edgesProcessor = edgesImg.getProcessor();

        // find edges & sharpen
        edgesProcessor.findEdges();

        // subtract
        ImageCalculator imgCalc = new ImageCalculator();
        ImagePlus finalImg = imgCalc.run("Subtract create", workingImg, edgesImg);
        ImageProcessor finalProcessor = finalImg.getProcessor();

        // post processing
        rf.rank(edgesProcessor, 5d, RankFilters.MEDIAN);

        // enhance contrast
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(finalImg, 0.35);

        // auto threshold
        finalProcessor.setAutoThreshold(AutoThresholder.Method.Minimum, true);
        finalProcessor.autoThreshold();
        finalProcessor.invert();

        // close & fill holes
        Binary binary = new Binary();
        binary.setup("close", finalImg);
        binary.run(finalProcessor);
        binary.setup("fill", finalImg);
        binary.run(finalProcessor);

        return finalImg;
    }

    /**
     * Get the overlays. Analyze probability images, which are the results of
     * the classification.
     */
    private void overlays() {
        ImagePlus imp = getCurrentImp();
        Overlay o = imp.getOverlay() == null ? new Overlay() : imp.getOverlay();
    }

    /**
     * After classification is corrected (by hand), runs the particle analysis
     * and save the results in a csv file.
     */
    private void pa() {
        ImagePlus imp = getCurrentImp();
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
