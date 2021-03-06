package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.osteo.ij.morph.GrayMorphology_;
import trainableSegmentation.WekaSegmentation;
//import trainableSegmentation.WekaSegmentation;

/**
 * Supplies all the features for classifying and quantifying osteoclast images.
 *
 * @author David Rueda
 */
public class Osteoclasts_ extends AbstractOsteoclasts implements PlugIn {

    private final String mfRadiusName = "Median filter radius";
    private final String mfRepeatName = "Median filter repetitions";
    private final String paMinSizeName = "Particle analyzer min size";
    private final String paMinCircName = "Particle analyzer min circularity";
    private final String paMaxCircName = "Particle analyzer max circularity";
    private final String incBright = "Increase Brightness";
    /**
     * One instance for the plugin.
     */
    private static Osteoclasts_ instance;

    public static Osteoclasts_ getInstance() {
        return instance;
    }
    /**
     * Little window with a few buttons.
     */
    private static JFrame miniWin;
    private JPanel miniWinInfosPanel;
    /**
     *
     */
    private ImagePlus cpyOverlaysImpKey;
    /**
     * One unique color for managing overlays.
     */
    public static final Color OVERLAY_COLOR = Color.magenta;
    /**
     * Simulate infinity.
     */
    protected final static double INFINITY = 1.0 / 0.0;

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
        instance = this;
        if (miniWin == null) {
            runMiniWin();
            logToMiniWin("Hello there! ;)");
        } else {
            miniWin.setVisible(true);
            miniWin.requestFocus();
        }
    }

    /**
     * Several actions that the plugin allows the user to do.
     */
    private enum Actions {

        SAVE_RESULTS("Save Results", "compute the final results and save them in a csv file", JButton.class),
        SEPARATOR(null, null, JSeparator.class),
        CLASSIFY("Run Classifier", "soon...", JButton.class),
        OVERLAYS("Compute Overlay", "analyze classified images", JButton.class),
        RES_DIR("Result Folder", "choose a directory where the results will be saved", JButton.class),
        RM_OVERLAYS("Reset Overlay", "delete all ROIs in the selected image", JButton.class),
        UP_OVERLAYS("Update Overlay", "draw the overlay for the current image or slice", JButton.class),
        CPY_OVERLAYS("Copy Overlays", "copy overlays for pasting into other stacks or images", JButton.class),
        PASTE_OVERLAYS("Paste Overlays", "paste previously chosen overlays", JButton.class, false),
        OPEN_IMG("Open Images", "Open images in a stack (PROB or original)", JButton.class);
        private String name;
        private String desc;
        private boolean visible;
        private boolean enabled;
        private Class<? extends JComponent> type;

        public Class<? extends JComponent> getType() {
            return this.type;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isVisible() {
            return this.visible;
        }

        public String getDesc() {
            return this.desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        Actions(String name, String desc, Class<? extends JComponent> type) {
            this.name = name;
            this.desc = desc;
            this.visible = true;
            this.enabled = true;
            this.type = type;
        }

        Actions(String name, String desc, Class<? extends JComponent> type, boolean display) {
            this.name = name;
            this.desc = desc;
            this.visible = display;
            this.enabled = true;
            this.type = type;
        }

        Actions(boolean enabled, String name, String desc, Class<? extends JComponent> type) {
            this.name = name;
            this.desc = desc;
            this.visible = true;
            this.enabled = enabled;
            this.type = type;
        }

        public JComponent getComponent() {
            try {
                if (this.name != null) {
                    JComponent jc = this.type.getDeclaredConstructor(String.class).newInstance(this.name);
                    jc.setToolTipText(this.desc);
                    jc.setEnabled(this.enabled);
                    jc.setVisible(this.visible);
                    return jc;
                } else {
                    return this.type.newInstance();
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(Osteoclasts_.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Osteoclasts_.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(Osteoclasts_.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(Osteoclasts_.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Osteoclasts_.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Osteoclasts_.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
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
    public void logToMiniWin(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JPanel mwip = Osteoclasts_.this.miniWinInfosPanel;
                mwip.removeAll();
                mwip.add(new JLabel(message));
                mwip.updateUI();
                miniWin.pack();
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
        miniWin.setTitle("I ღ Osteo");
        miniWin.setResizable(false);

        final JPanel actionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JButton source = (JButton) ae.getSource();
                ImageOperationsWorker iow = new ImageOperationsWorker(Osteoclasts_.this);

                if (source.getText().equals(Actions.SAVE_RESULTS.getName())) {
                    iow.setMethodToInvoke("pa");
                } else if (source.getText().equals(Actions.CLASSIFY.getName())) {
                    iow.setMethodToInvoke("classify");
                } else if (source.getText().equals(Actions.OVERLAYS.getName())) {
                    iow.setMethodToInvoke("overlays");
                } else if (source.getText().equals(Actions.RM_OVERLAYS.getName())) {
                    iow.setMethodToInvoke("rmOverlays");
                } else if (source.getText().equals(Actions.UP_OVERLAYS.getName())) {
                    iow.setMethodToInvoke("updateOverlay");
                } else if (source.getText().equals(Actions.RES_DIR.getName())) {
                    iow.setMethodToInvoke("setResultDir");
                } else if (source.getText().equals(Actions.CPY_OVERLAYS.getName())) {
                    iow.setMethodToInvoke("cpyOverlays");
                } else if (source.getText().equals(Actions.PASTE_OVERLAYS.getName())) {
                    iow.setMethodToInvoke("pasteOverlays");
                } else if (source.getText().equals(Actions.OPEN_IMG.getName())) {
                    iow.setMethodToInvoke("open");
                }

                logToMiniWin("working...");
                blockRegisteredButtons();
                iow.execute();
            }
        };

        List<JComponent> cList = new LinkedList<JComponent>();
        cList.add(Actions.OPEN_IMG.getComponent());
        cList.add(Actions.SAVE_RESULTS.getComponent());
        cList.add(Actions.RES_DIR.getComponent());
        cList.add(Actions.SEPARATOR.getComponent());
        cList.add(Actions.OVERLAYS.getComponent());
        cList.add(Actions.CPY_OVERLAYS.getComponent());
        cList.add(Actions.UP_OVERLAYS.getComponent());
        cList.add(Actions.RM_OVERLAYS.getComponent());
        cList.add(Actions.SEPARATOR.getComponent());
        cList.add(Actions.CLASSIFY.getComponent());

        for (Iterator<JComponent> jcIt = cList.iterator(); jcIt.hasNext();) {
            JComponent action = jcIt.next();

            if (action.isVisible()) {

                if (action instanceof JButton) {
                    actionsPanel.add(action, c);
                    ((JButton) action).addActionListener(actionListener);
                } else if (action instanceof JSeparator) {
                    actionsPanel.add(Box.createVerticalStrut(3), c);
                    actionsPanel.add(action, c);
                    actionsPanel.add(Box.createVerticalStrut(2), c);
                }

                if (!action.isEnabled()) {
                    action.setEnabled(false);
                } else if (action instanceof JButton) {
                    registerButton((JButton) action);
                }
            }
        }

        miniWinInfosPanel = new JPanel();

        miniWin.getContentPane().add(miniWinInfosPanel, BorderLayout.NORTH);
        miniWin.getContentPane().add(actionsPanel, BorderLayout.CENTER);
        miniWin.pack();
    }

    /**
     * Copy the list of overlays for the current image or stack.
     */
    void cpyOverlays() {
        this.cpyOverlaysImpKey = getCurrentImp();

        JButton cpOvButton = getRegisteredButtonByText(Actions.CPY_OVERLAYS.getName());
        cpOvButton.setText(Actions.PASTE_OVERLAYS.getName());
        cpOvButton.setToolTipText(Actions.PASTE_OVERLAYS.getDesc());
    }

    /**
     * Paste the list of previous chosen overlays.
     */
    void pasteOverlays() {
        ImagePlus imp = getCurrentImp();
        getOverlayStack(imp).set(getOverlayStack(this.cpyOverlaysImpKey));
        updateImpOverlay(imp);

        JButton cpOvButton = getRegisteredButtonByText(Actions.PASTE_OVERLAYS.getName());
        cpOvButton.setText(Actions.CPY_OVERLAYS.getName());
        cpOvButton.setToolTipText(Actions.CPY_OVERLAYS.getDesc());
    }

    /**
     * Run the classifier on selected image.
     */
    void classify() {
        try {
            ImagePlus imp = getCurrentImp();

            String classifierPath = IJ.getFilePath("Tell me please, where the classifier is.");

            if (imp.getProcessor().getNChannels() != 3) {
                IJ.error("The script can be processed only on RGB images.");
                return;
            }

            WekaSegmentation segmentator = new WekaSegmentation(imp);
            segmentator.getFeatureStackArray().setOldColorFormat(true);
            segmentator.loadClassifier(classifierPath);
            segmentator.applyClassifier(0, true);
            ImagePlus result = segmentator.getClassifiedImage();
            result.show();

            String path = IJ.getFilePath("Where should the results be saved?");
            if (path != null) {
                File file = new File(path);

                if (file.exists()) {
                    boolean nonetheless = IJ.showMessageWithCancel(
                            "Save results...",
                            "\"" + file.getName() + "\" already exists.\nDo you want to replace it?");
                    if (nonetheless) {
                        IJ.saveAs(result, "Tiff", path);
                    }
                } else {
                    IJ.saveAs(result, "Tiff", path);
                }
            }
        } catch (Exception ex) {
            IJ.error(ex.getMessage());
        }
    }

    /**
     * Will remove the overlay for the selected image.
     */
    void rmOverlays() {
        ImagePlus imp = getCurrentImp();
        getOverlayStack(imp).clear();
        imp.setOverlay(null);
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
//            gm.setup("radius=5 type=circle operator=close", workingImg);
            GrayMorphology_.radius = 5f;
            GrayMorphology_.options = 0;
            GrayMorphology_.showoptions = false;
            GrayMorphology_.morphoptions = 3;
            gm.imp = workingImg;
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

        // analyze particles
        ResultsTable paResults = new ResultsTable();
        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.IN_SITU_SHOW
                | ParticleAnalyzer.INCLUDE_HOLES
                | ParticleAnalyzer.SHOW_OVERLAY_OUTLINES,
                Measurements.AREA | Measurements.MEAN
                | Measurements.MIN_MAX | Measurements.CENTROID
                | Measurements.PERIMETER
                | Measurements.SHAPE_DESCRIPTORS,
                paResults,
                Double.parseDouble(os.getOptionValue(paMinSizeName)),
                INFINITY,
                Double.parseDouble(os.getOptionValue(paMinCircName)),
                Double.parseDouble(os.getOptionValue(paMaxCircName)));
        particleAnalyzer.analyze(finalImg);

        return finalImg;
    }

    /**
     * Get the overlays. Analyze probability images, which are the results of
     * the classification.
     */
    void overlays() {
        ImagePlus imp = getCurrentImp();
        Overlay o = imp.getOverlay() == null ? new Overlay() : imp.getOverlay();

        OptionSet os = new OptionSet();
        Option mf_radius = new Option(
                mfRadiusName, Option.Type.STRING);
        mf_radius.setCurrentValue("5");
        Option mf_repets = new Option(
                mfRepeatName, Option.Type.STRING);
        mf_repets.setCurrentValue("4");
        Option pa_minSize = new Option(
                paMinSizeName, Option.Type.STRING);
        pa_minSize.setCurrentValue("200");
        Option pa_minCirc = new Option(
                paMinCircName, Option.Type.STRING);
        pa_minCirc.setCurrentValue("0.00001");
        Option pa_maxCirc = new Option(
                paMaxCircName, Option.Type.STRING);
        pa_maxCirc.setCurrentValue("0.8");
        Option inc_bright = new Option(
                incBright, Option.Type.STRING);
        inc_bright.setSelected(false);

        os.add(mf_radius);
        os.add(mf_repets);
        os.add(pa_minSize);
        os.add(pa_minCirc);
        os.add(pa_maxCirc);
        os.add(inc_bright);

        ImagePlus maskResult;
        ImageStack stack = imp.getStack();
        for (int s = 1; s <= stack.getSize(); s++) {
            IJ.showStatus(s + "/" + stack.getSize());
            maskResult = applyIPP(new ImagePlus(imp.getTitle(), stack.getProcessor(s)), os);
            getOverlayStack(imp).put(s, maskResult.getOverlay());
        }

        updateImpOverlay(imp);
    }

    /**
     * For the current selected slice of a stack, displays the already computed
     * overlay.
     */
    void updateOverlay() {
        updateImpOverlay(getCurrentImp());
    }

    /**
     * Open PROBs images yielded by the classifier. Each of those images have
     * two slices, but we want to work with only one of them: the first one,
     * where the whiter a pixel, the bigger the probability to belong to the
     * osteoclast class.
     */
    void open() throws Exception {
        String prb = IJ.getDirectory("select img directory");
        File dir = new File(prb);
        List<String> sortedFiles = new LinkedList<String>();
        sortedFiles.addAll(Arrays.asList(dir.list()));
        Collections.sort(sortedFiles);

        ImagePlus imp = new ImagePlus(dir.getAbsoluteFile().getAbsolutePath() + "/" + sortedFiles.get(0));
        ImageStack ims = new ImageStack(imp.getWidth(), imp.getHeight());
        for (int i = 0; i < sortedFiles.size(); i++) {
            int slice = i + 1;
            imp = new ImagePlus(dir.getAbsoluteFile().getAbsolutePath() + "/" + sortedFiles.get(i));
            IJ.showStatus(slice + "/" + sortedFiles.size());
            while (imp.getNSlices() > 1) {
                imp.getStack().deleteLastSlice();
            }
            ims.addSlice(imp.getProcessor());
            ims.setSliceLabel(imp.getTitle(), slice);
        }

        (new ImagePlus(dir.getName(), ims)).show();
    }

    /**
     * Change the directory where all result files (images, values...) are
     * saved.
     */
    boolean setResultDir() {
        String path = IJ.getDirectory("Choose a directory");
        if (path == null) {
            return false;
        }

        File f = new File(path);
        if (!f.exists()) {
            return false;
        } else if (f.listFiles().length > 0) {
            if (!IJ.showMessageWithCancel(
                    "Folder not empty",
                    "\"" + f.getName() + "\" already contains files."
                    + "\nThe plugin may override them.")) {
                return false;
            }
        }

        setResultsPath(path);
        JButton button = getRegisteredButtonByText(Actions.RES_DIR.getName());
        button.setToolTipText(path);
        Actions.RES_DIR.setDesc(path);

        return true;
    }

    private ResultsTable applyPA(ImagePlus imp, Overlay o) {
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

        return paResults;
    }

    private void savePA(ImagePlus imp, ResultsTable paResults, String path) {
        try {
            if (path != null) {
                path += path.endsWith("/") ? "" : "/";
                path += imp.getTitle();
                path += ".csv";

                if (paResults == null) {
                    File f = new File(path);
                    f.createNewFile();
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
     * After classification is corrected (by hand), runs the particle analysis
     * and save the results in a csv file.
     */
    void pa() {
        ImagePlus imp = getCurrentImp();
        String impTitle = imp.getTitle();
        OverlayStack ovStack = getOverlayStack(imp);
        ImageStack stack = imp.getStack();
        int n = stack.getSize();
        for (int s = 1; s <= n; s++) {
            IJ.showStatus(s + "/" + n);
            String sliceName = stack.getSliceLabel(s);
            String fileName = n > 1 ? sliceName == null ? impTitle : sliceName : impTitle;
            Overlay o = ovStack.getOverlay(s);
            ImagePlus impFor = new ImagePlus(fileName, stack.getProcessor(s));
            String path = getResultsPath();
            if (path == null) {
                if (!setResultDir()) {
                    return;
                }
            }
            if (o == null) {
                IJ.log("no overlay: " + fileName);
                savePA(impFor, null, path);
                continue;
            }
            savePA(impFor, applyPA(impFor, o), path);
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
