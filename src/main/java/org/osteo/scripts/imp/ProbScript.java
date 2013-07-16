package org.osteo.scripts.imp;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RankFilters;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;
import levelsets.filter.MedianFilter;
import org.osteo.gui.ScriptFrame;


import org.osteo.gui.filters.ImagesFilter;
import org.osteo.ij.morph.GrayMorphology_;
import org.osteo.io.ImagePlusWriter;
import org.osteo.main.App;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.Option;
import org.osteo.scripts.util.OptionSet;

public class ProbScript extends AbstractScript {

    private static List<OptionSet> optionSets;

    protected Integer[] getStepsGenParam(OptionSet os) {
        Integer[] o;
        Option incBrightOpt = OptionSet.findOption(incBright, os);
        if (incBrightOpt.isSelected()) {
            o = new Integer[]{
                Integer.parseInt(os.getOptionValue(mfRadiusName + nstepsName)),
                Integer.parseInt(os.getOptionValue(mfRepeatName + nstepsName)),
                Integer.parseInt(os.getOptionValue(paMinSizeName + nstepsName)),
                Integer.parseInt(os.getOptionValue(paMinCircName + nstepsName)),
                Integer.parseInt(os.getOptionValue(paMaxCircName + nstepsName))
            };
        } else {
            o = new Integer[]{
                Integer.parseInt(os.getOptionValue(paMinSizeName + nstepsName)),
                Integer.parseInt(os.getOptionValue(paMinCircName + nstepsName)),
                Integer.parseInt(os.getOptionValue(paMaxCircName + nstepsName))
            };
        }
        return o;
    }

    protected Double[] getMaxsGenParam(OptionSet os) {
        Double[] o;
        Option incBrightOpt = OptionSet.findOption(incBright, os);
        if (incBrightOpt.isSelected()) {
            o = new Double[]{
                Double.parseDouble(os.getOptionValue(mfRadiusName + maxName)),
                Double.parseDouble(os.getOptionValue(mfRepeatName + maxName)),
                Double.parseDouble(os.getOptionValue(paMinSizeName + maxName)),
                Double.parseDouble(os.getOptionValue(paMinCircName + maxName)),
                Double.parseDouble(os.getOptionValue(paMaxCircName + maxName))
            };
        } else {
            o = new Double[]{
                Double.parseDouble(os.getOptionValue(paMinSizeName + maxName)),
                Double.parseDouble(os.getOptionValue(paMinCircName + maxName)),
                Double.parseDouble(os.getOptionValue(paMaxCircName + maxName))
            };
        }
        return o;
    }

    protected Double[] getMinsGenParam(OptionSet os) {
        Double[] o;
        Option incBrightOpt = OptionSet.findOption(incBright, os);
        if (incBrightOpt.isSelected()) {
            o = new Double[]{
                Double.parseDouble(os.getOptionValue(mfRadiusName + minName)),
                Double.parseDouble(os.getOptionValue(mfRepeatName + minName)),
                Double.parseDouble(os.getOptionValue(paMinSizeName + minName)),
                Double.parseDouble(os.getOptionValue(paMinCircName + minName)),
                Double.parseDouble(os.getOptionValue(paMaxCircName + minName))
            };
        } else {
            o = new Double[]{
                Double.parseDouble(os.getOptionValue(paMinSizeName + minName)),
                Double.parseDouble(os.getOptionValue(paMinCircName + minName)),
                Double.parseDouble(os.getOptionValue(paMaxCircName + minName))
            };
        }
        return o;
    }

    protected Option[] getOptionsGenParam(OptionSet os) {
        Option[] o;
        Option incBrightOpt = OptionSet.findOption(incBright, os);
        if (incBrightOpt.isSelected()) {
            o = new Option[]{
                new Option(mfRadiusName, Option.Type.STRING),
                new Option(mfRepeatName, Option.Type.STRING),
                new Option(paMinSizeName, Option.Type.STRING),
                new Option(paMinCircName, Option.Type.STRING),
                new Option(paMaxCircName, Option.Type.STRING),};
        } else {
            o = new Option[]{
                new Option(paMinSizeName, Option.Type.STRING),
                new Option(paMinCircName, Option.Type.STRING),
                new Option(paMaxCircName, Option.Type.STRING),};
        }
        return o;
    }

    protected Integer getNOptionSets(OptionSet os) {
        Integer nos;
        try {
            Option[] optionss = getOptionsGenParam(os);
            Double[] mins = getMinsGenParam(os);
            Double[] maxs = getMaxsGenParam(os);
            Integer[] steps = getStepsGenParam(os);

            nos = OptionSet.nOptionSets(optionss, mins, maxs, steps);
        } catch (NullPointerException ex) {
            nos = 0;
        }
        return nos;
    }

    protected List<OptionSet> getOptionSets(OptionSet os) {
        if (optionSets == null) {
            Option[] optionss = getOptionsGenParam(os);
            Double[] mins = getMinsGenParam(os);
            Double[] maxs = getMaxsGenParam(os);
            Integer[] steps = getStepsGenParam(os);

            optionSets = OptionSet.genOptSpace(optionss, mins, maxs, steps);
        }
        return optionSets;
    }

    public ProbScript() {
    }

    public ProbScript(File image, File resultDir) {
        super(image, resultDir);
    }

    private ImagePlus applyIPP(ImagePlus workingImg, OptionSet os) {
        RankFilters rf = new RankFilters();

        workingImg.getStack().deleteLastSlice();
        workingImg.setProcessor(workingImg.getProcessor().convertToByte(true));
        ImageProcessor workingProcessor = workingImg.getProcessor();

        // increase brightness in working image
        if (getOption(incBright).isSelected()) {
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

    private void runAnalysis(OptionSet os, String subdirectory) throws ScriptException, IOException, IllegalAccessException {
        if (getImp().getProcessor().getNChannels() > 1) {
            throw new ScriptException(
                    Bundle.UI.getString("message_grayonly"));
        }

        ImagePlus imp = applyIPP(getImp(), os);

        // analyze particles
        ResultsTable paResults = new ResultsTable();
        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.IN_SITU_SHOW
                | ParticleAnalyzer.INCLUDE_HOLES
                | Show.valueOf(os.getOptionValue("Show")).paInt,
                Measurements.AREA | Measurements.MEAN
                | Measurements.MIN_MAX | Measurements.CENTROID
                | Measurements.PERIMETER
                | Measurements.SHAPE_DESCRIPTORS,
                paResults,
                Double.parseDouble(os.getOptionValue(paMinSizeName)),
                INFINITY,
                Double.parseDouble(os.getOptionValue(paMinCircName)),
                Double.parseDouble(os.getOptionValue(paMaxCircName)));
        particleAnalyzer.analyze(imp);
        paResults.saveAs(genName(getFile(), ".CSV", subdirectory));

        ImagePlus mask = null;
        try {
            if (Show.valueOf(os.getOptionValue("Show")) == Show.Overlay) {
                File originalImg = new File(os.getOptionValue("Original images path"));
                if (originalImg.exists()) {
                    mask = new ImagePlus(originalImg.getAbsolutePath()
                            + File.separator + originalFileName());
                    mask.setOverlay(imp.getOverlay());
                }
            }
        } catch (IllegalArgumentException ignored) {
        } catch (NullPointerException ignored) {
        }

        if (mask == null) {
            mask = imp;
        }

        synchronized (ProbScript.class) {
            try {
                IJ.saveAs(mask, "Tiff", genName(getFile(), "_MASK.TIF", subdirectory));
            } catch (Exception e) {
                App.log("error saving overlay..");
                App.log(e.getMessage());
                throw new ScriptException(e.getMessage());
            }
//            ImagePlusWriter ipw = new ImagePlusWriter();
//            ipw.saveTiff(mask, genName(getFile(), "_MASK.TIF", subdirectory));
        }
    }

    private void clearOptionSets() {
        optionSets = null;
    }

    @Override
    public Object begin() throws ScriptException {
        try {
            if (getOption(genParamsName).isSelected()) {
                clearOptionSets();
                List<OptionSet> oss = getOptionSets(getOptions());
                int cnt = 0;
                for (OptionSet os : oss) {
                    runAnalysis(os, Integer.toString(++cnt));
                }
            } else {
                runAnalysis(getOptions(), null);
            }

            return null;
        } catch (ScriptException e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        } catch (IOException e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        } catch (IllegalAccessException e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    @Override
    public void end(File[] files) throws ScriptException {
    }

    public String originalFileName() {
        return getFile().getName().replace("_PROB", "");
    }

    @Override
    public String name() {
        return "Analyse Images";
    }

    @Override
    public boolean acceptStrOption(String optionName, String value) {
        return false;
    }

    protected enum Show {

        Nothing("Nothing", ParticleAnalyzer.SHOW_NONE),
        Overlay("Overlay", ParticleAnalyzer.SHOW_OVERLAY_OUTLINES),
        Mask("Mask", ParticleAnalyzer.SHOW_MASKS);
        public final String name;
        public final int paInt;

        private Show(String name, int paInt) {
            this.name = name;
            this.paInt = paInt;
        }
    }
    public final String mfRadiusName = Bundle.UI.getString("probscript_opt_mefradius");
    public final String mfRepeatName = Bundle.UI.getString("probscript_opt_mefrepeat");
    public final String paMinSizeName = Bundle.UI.getString("probscript_opt_paminsize");
    public final String paMinCircName = Bundle.UI.getString("probscript_opt_pamincirc");
    public final String paMaxCircName = Bundle.UI.getString("probscript_opt_pamaxcirc");
    public final String genParamsName = Bundle.UI.getString("probscript_opt_genparams");
    public final String minName = Bundle.UI.getString("probscript_opt_min");
    public final String maxName = Bundle.UI.getString("probscript_opt_max");
    public final String nstepsName = Bundle.UI.getString("probscript_opt_nsteps");
    public final String ngensetsNameToFormat = "probscript_opt_ngensets";
    public final String ngensetsName = "ngensets";
    public final String incBright = Bundle.UI.getString("probscript_opt_incbright");

    @Override
    public void updateOptions() {
        Option o = getOptions().getOption(ngensetsName);
        if (o != null) {
            o.setCurrentValue(Bundle.UI.getFormatedString(ngensetsNameToFormat, getNOptionSets(getOptions()).toString()));
        }
    }

    @Override
    public OptionSet availableOptions(OptionSet prevOptions, String prevScName) {
        OptionSet avOpts = new OptionSet();

        Option show = showOption();
        Option originalFiles = orgImagesOption();

        Option genPOpt = OptionSet.findOption(genParamsName, prevOptions);
        Option generateParamsSets = new Option(genParamsName, Option.Type.CHECK);
        if (prevOptions == null || !prevScName.equals(this.name()) || (genPOpt != null && !genPOpt.isSelected())) {
            generateParamsSets.setSelected(Boolean.FALSE);
        } else if (genPOpt != null) {
            generateParamsSets.setSelected(genPOpt.isSelected());
        }

        Option incBrightOpt = OptionSet.findOption(incBright, prevOptions);
        Option increaseBrightness = increaseBrightness();
        if (prevOptions == null || !prevScName.equals(this.name()) || (incBrightOpt != null && !incBrightOpt.isSelected())) {
            increaseBrightness.setSelected(Boolean.FALSE);
        } else if (incBrightOpt != null) {
            increaseBrightness.setSelected(incBrightOpt.isSelected());
        }

        if (prevOptions == null || !prevScName.equals(this.name()) || (genPOpt != null && !genPOpt.isSelected())) {

            Option medRadius = null, medRepeat = null;
            if (increaseBrightness.isSelected()) {
                medRadius = new Option(mfRadiusName,
                        Option.Type.STRING);
                medRadius.setCurrentValue("2");

                medRepeat = new Option(mfRepeatName,
                        Option.Type.STRING);
                medRepeat.setCurrentValue("3");
            }

            Option partMinSize = new Option(paMinSizeName,
                    Option.Type.STRING);
            partMinSize.setCurrentValue("200");

            Option partMinCircularity = new Option(
                    paMinCircName, Option.Type.STRING);
            partMinCircularity.setCurrentValue("0.30");

            Option partMaxCircularity = new Option(
                    paMaxCircName, Option.Type.STRING);
            partMaxCircularity.setCurrentValue("1");

            avOpts.add(generateParamsSets);
            if (increaseBrightness.isSelected()) {
                avOpts.add(medRadius);
                avOpts.add(medRepeat);
            }
            avOpts.add(partMinSize);
            avOpts.add(partMinCircularity);
            avOpts.add(partMaxCircularity);
        } else {
            String basicNSteps = "1";

            Option mfRadMin = null,
                    mfRadMax = null,
                    mfRadSteps = null,
                    mfRepeatMin = null,
                    mfRepeatMax = null,
                    mfRepeatSteps = null;
            if (increaseBrightness.isSelected()) {
                // radius of median filter / min, max, nsteps
                mfRadMin = new Option(mfRadiusName + minName,
                        Option.Type.STRING);
                mfRadMin.setCurrentValue("1");

                mfRadMax = new Option(mfRadiusName + maxName,
                        Option.Type.STRING);
                mfRadMax.setCurrentValue("15");

                mfRadSteps = new Option(mfRadiusName + nstepsName,
                        Option.Type.STRING);
                mfRadSteps.setCurrentValue(basicNSteps);

                // number of repeatitions mf / min, max, nsteps
                mfRepeatMin = new Option(mfRepeatName + minName,
                        Option.Type.STRING);
                mfRepeatMin.setCurrentValue("1");

                mfRepeatMax = new Option(mfRepeatName + maxName,
                        Option.Type.STRING);
                mfRepeatMax.setCurrentValue("10");

                mfRepeatSteps = new Option(mfRepeatName + nstepsName,
                        Option.Type.STRING);
                mfRepeatSteps.setCurrentValue(basicNSteps);
            }

            // pa min size / min, max, nsteps
            Option partMinSizeMin = new Option(paMinSizeName + minName,
                    Option.Type.STRING);
            partMinSizeMin.setCurrentValue("0");

            Option partMinSizeMax = new Option(paMinSizeName + maxName,
                    Option.Type.STRING);
            partMinSizeMax.setCurrentValue("500");

            Option partMinSizeSteps = new Option(paMinSizeName + nstepsName,
                    Option.Type.STRING);
            partMinSizeSteps.setCurrentValue(basicNSteps);

            // pa min circularity / min, max, nsteps
            Option partMinCircularityMin = new Option(paMinCircName + minName,
                    Option.Type.STRING);
            partMinCircularityMin.setCurrentValue("0");

            Option partMinCircularityMax = new Option(paMinCircName + maxName,
                    Option.Type.STRING);
            partMinCircularityMax.setCurrentValue("0.2");

            Option partMinCircularitySteps = new Option(paMinCircName + nstepsName,
                    Option.Type.STRING);
            partMinCircularitySteps.setCurrentValue(basicNSteps);

            // pa max circularity / min, max, nsteps
            Option partMaxCircularityMin = new Option(paMaxCircName + minName,
                    Option.Type.STRING);
            partMaxCircularityMin.setCurrentValue("0.8");

            Option partMaxCircularityMax = new Option(paMaxCircName + maxName,
                    Option.Type.STRING);
            partMaxCircularityMax.setCurrentValue("1");

            Option partMaxCircularitySteps = new Option(paMaxCircName + nstepsName,
                    Option.Type.STRING);
            partMaxCircularitySteps.setCurrentValue(basicNSteps);

            avOpts.add(generateParamsSets);
            if (increaseBrightness.isSelected()) {
                avOpts.add(mfRadMin);
                avOpts.add(mfRadMax);
                avOpts.add(mfRadSteps);
                avOpts.add(mfRepeatMin);
                avOpts.add(mfRepeatMax);
                avOpts.add(mfRepeatSteps);
            }
            avOpts.add(partMinSizeMin);
            avOpts.add(partMinSizeMax);
            avOpts.add(partMinSizeSteps);
            avOpts.add(partMinCircularityMin);
            avOpts.add(partMinCircularityMax);
            avOpts.add(partMinCircularitySteps);
            avOpts.add(partMaxCircularityMin);
            avOpts.add(partMaxCircularityMax);
            avOpts.add(partMaxCircularitySteps);

            Option nGeneratedSets = genSetsOption(avOpts);
            avOpts.add(1, nGeneratedSets);
        }

        avOpts.add(increaseBrightness);
        avOpts.add(originalFiles);
        avOpts.add(show);

        return avOpts;
    }

    private Option increaseBrightness() {
        return new Option(
                incBright,
                Option.Type.CHECK);
    }

    private Option genSetsOption(OptionSet os) {
        Option o = new Option(ngensetsName,
                Option.Type.CRITICAL_INFO,
                Bundle.UI.getFormatedString(ngensetsNameToFormat, getNOptionSets(os).toString()));
        o.setNameDisplayed(false);
        return o;
    }

    private Option orgImagesOption() {
        Option originalFiles = new Option("Original images path", Option.Type.FILE);
        originalFiles.setCurrentValue("choose a directory");
        originalFiles.setFileFilter(new ImagesFilter());
        return originalFiles;
    }

    private Option showOption() {
        Option show = new Option("Show", Option.Type.STRING);
        ArrayList<String> showValues = new ArrayList<String>();
        for (Show s : Show.values()) {
            showValues.add(s.name);
        }
        show.setPossibleValues(showValues);
        show.setCurrentValue(Show.Nothing.name());
        return show;
    }

    @Override
    public void updatedValue(OptionSet os, Option option) {
        ScriptFrame sf = ScriptFrame.getInstance();
        String value = Bundle.UI.getFormatedString(ngensetsNameToFormat, getNOptionSets(os).toString());
        sf.updateOptionInfo(ngensetsName, value);
    }

    @Override
    public FileFilter getFileFilter() {
        return new ImagesFilter();
    }
}
