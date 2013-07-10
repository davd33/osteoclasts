package org.osteo.scripts.imp;

import ij.ImagePlus;

import java.io.File;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.osteo.gui.filters.ClassifierFilter;
import org.osteo.gui.filters.ImagesFilter;
import org.osteo.io.ImagePlusWriter;
import org.osteo.main.App;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.Option;
import org.osteo.scripts.util.OptionSet;

import trainableSegmentation.WekaSegmentation;

public class ApplyClassifierScript extends AbstractScript {

    public static int NUM_THREADS = 0;
    private File current;

    public ApplyClassifierScript() {
        super();
    }

    public ApplyClassifierScript(File file, File resultDir) {
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
            for (File f : files) {
                current = f;
                App.log(f.getPath());
                ImagePlus imp = new ImagePlus(f.getAbsolutePath());

                if (imp.getProcessor().getNChannels() != 3) {
                    throw new ScriptException(
                            Bundle.UI.getString("message_rgbonly"));
                }

                WekaSegmentation weka = new WekaSegmentation();
                weka.setTrainingImage(imp);
                if (loadClassifier(weka, os.getOptionValue("Classifier"))) {
                    weka.applyClassifier(NUM_THREADS, true);
                    ImagePlus result = weka.getClassifiedImage();
                    //result.show();
                    ImagePlusWriter tiffWriter = new ImagePlusWriter();
                    tiffWriter.saveTiff(result, genName(f, "_PROB.TIF"));
                }
            }
        } catch (NullPointerException e) {
            String message = e.getMessage();
            throw new ScriptException(message);
        } catch (OutOfMemoryError e) {
            String message = e.getMessage();
            throw new ScriptException(message);
        } catch (Exception e) {
            String message = getErrorMessage(e, current);
            throw new ScriptException(message);
        }
    }

    private boolean loadClassifier(WekaSegmentation weka, String classifierPath)
            throws ScriptException {
        if (classifierPath == null || classifierPath.isEmpty()) {
            throw new ScriptException("No classifier file specified!");
        }

        App.log("Loading Weka classifier from " + classifierPath + "...");
        // Try to load Weka model (classifier and train header)
        System.gc();
        if (!weka.loadClassifier(classifierPath)) {
            throw new ScriptException(
                    "Error when loading Weka classifier from file");
        }

        App.log("Read header from " + classifierPath
                + " (number of attributes = "
                + weka.getTrainHeader().numAttributes() + ")");
        if (weka.getTrainHeader().numAttributes() < 1) {
            throw new ScriptException(
                    "Error: No attributes were found on the model header");
        }

        App.log("Loaded " + classifierPath);
        return true;
    }

    @Override
    public String name() {
        return "Apply classifier";
    }

    @Override
    public boolean acceptStrOption(String optionName, String value) {
        return false;
    }

    @Override
    public OptionSet availableOptions(OptionSet po, String prevScName) {
        OptionSet avOpts = new OptionSet();
        Option classifier = new Option("Classifier", Option.Type.FILE);
        classifier
                .setCurrentValue("classifiers" + File.separator + "10x.model");
        classifier.setFileFilter(new ClassifierFilter());

        avOpts.add(classifier);
        return avOpts;
    }

    @Override
    public FileFilter getFileFilter() {
        return new ImagesFilter();
    }
}
