package org.osteo.scripts.imp;

import ij.ImagePlus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.osteo.gui.filters.ImagesFilter;
import org.osteo.main.App;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.Option;
import org.osteo.scripts.util.OptionSet;

import trainableSegmentation.FeatureStack;
import trainableSegmentation.WekaSegmentation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;

public class TrainClassifierScript extends AbstractScript {

    private static final boolean debug = false;
    private static final int OSTEO_CLASS = 0;
    private static final int OTHER_CLASS = 1;
    private File current;
    private WekaSegmentation weka;

    public TrainClassifierScript() {
        super();
    }

    public TrainClassifierScript(File file, File resultDir) {
        super(file, resultDir);
    }

    @Override
    public Object begin() throws ScriptException {
        return null;
    }

    @Override
    public void end(File[] files) throws ScriptException {
        try {
            OptionSet os = getOptions();
            
            weka = new WekaSegmentation();
            weka.setClassLabel(OSTEO_CLASS, "osteo");
            weka.setClassLabel(OTHER_CLASS, "other");

            int maxInstances = getMaxInstOpt(os);
            int nInstances = maxInstances / (files.length * 2);

            for (File f : files) {
                current = f;
                App.log(f.getName());
                ImagePlus imp = new ImagePlus(f.getAbsolutePath().replaceAll(
                        "\\.[a-zA-Z]{2,4}$", "")
                        + "-1.TIF");

                ImagePlus oriImp = new ImagePlus(f.getAbsolutePath());
                FeatureStack fsImp = new FeatureStack(oriImp);
                fsImp.updateFeaturesMT();

                weka.setTrainingImage(oriImp);

                if (imp.getProcessor().getNChannels() != 3) {
                    throw new ScriptException(
                            Bundle.UI.getString("message_rgbonly"));
                }

                String prefix = f.getName().replaceAll("\\.[a-zA-Z]*$", "");
                String binName = null;
                for (String name : f.getAbsoluteFile().getParentFile().list()) {
                    if (name.contains(prefix) && name.contains("_LABELS")) {
                        binName = name;
                        break;
                    }
                }
                if (binName == null) {
                    throw new ScriptException("No binary image found!");
                }
                ImagePlus binImp = new ImagePlus(f.getAbsoluteFile()
                        .getParentFile().getAbsolutePath()
                        + File.separator + binName);

                if (debug) {
                    boolean[] enabledFeatures = fsImp.getEnabledFeatures();
                    String[] featuresNames = FeatureStack.availableFeatures;
                    App.log("Enabled Features:");
                    for (int i = 0; i < featuresNames.length; i++) {
                        App.log(featuresNames[i]
                                + (enabledFeatures[i] ? " enabled"
                                : " disabled"));
                    }
                }

                weka.addRandomBalancedBinaryData(binImp.getProcessor(), fsImp,
                        "other", "osteo", nInstances);
            }

            App.log("Homogenizing training data");
            weka.homogenizeTrainingData();
            setMLAlg(os.getOptionValue("Algorithm"));
            App.log("Classifier used: "
                    + weka.getClassifier().getClass().getSimpleName());
            boolean classified = weka.trainClassifier();
            if (classified) {
                System.gc();
                File save = new File(genName(null, "classifier.model"));
                weka.saveClassifier(save.getAbsolutePath());
            }
        } catch (OutOfMemoryError e) {
            String message = e.getMessage();
            throw new ScriptException(message);
        } catch (Exception e) {
            String message = getErrorMessage(e, current);
            throw new ScriptException(message);
        }
    }

    private int getMaxInstOpt(OptionSet os) {
        String value = os.getOptionValue("Maximum number of instances");
        return Integer.parseInt(value);
    }

    private void setMLAlg(String optionValue) {
        if (optionValue.equals("Naive Bayes")) {
            weka.setClassifier(new NaiveBayes());
        } else if (optionValue.equals("Random forests")) {
            // default algorithm... nothing to change
        } else if (optionValue.equals("Artificial neural networks")) {
            weka.setClassifier(new MultilayerPerceptron());
        }
    }

    @Override
    public String name() {
        return "Train classifier";
    }

    @Override
    public boolean acceptStrOption(String optionName, String value) {
        return false;
    }

    @Override
    public OptionSet availableOptions(OptionSet po, String prevScName) {
        OptionSet avOpts = new OptionSet();
        Option algo = new Option("Algorithm", Option.Type.STRING, "Naive Bayes",
                "Random forests", "Artificial neural networks");
        algo.setCurrentValue("Artificial neural networks");

        Option instances = new Option("Maximum number of instances", Option.Type.STRING);
        instances.setCurrentValue("10000");

        avOpts.add(algo);
        avOpts.add(instances);
        return avOpts;
    }

    @Override
    public FileFilter getFileFilter() {
        return new ImagesFilter();
    }
}
