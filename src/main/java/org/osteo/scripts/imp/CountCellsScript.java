package org.osteo.scripts.imp;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.VerticalAlignment;
import org.osteo.gui.filters.ImagesFilter;
import org.osteo.io.ImagePlusWriter;
import org.osteo.main.App;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.Instance;
import org.osteo.scripts.util.OptionSet;
import org.osteo.scripts.util.ROCPoint;

public class CountCellsScript extends AbstractScript {

    private volatile static Map<String, List<ROCPoint>> rocCurves = new HashMap<String, List<ROCPoint>>();
    private Map<ImagePlus, List<Float>> values;
    private Float medianPos;
    private Float medianNeg;
    private Float stdDevPos;
    private Float stdDevNeg;
    private Float threshold;
    private final static int RES_N_OST = 0;
    private final static int RES_AREA = 1;
    private final static int RES_WIDTH = 2;
    private final static int RES_HEIGHT = 3;
    private final static int PRECISION = 2;
    private final static int RECALL = 3;
    private final static int THRESHOLD = 4;
    private final static int ACCURACY = 5;
    private final static String[] resultsLabels = new String[]{
        "Counted Osteoclasts", "Labelled Osteoclasts", "Precision",
        "Recall", "Threshold", "Accuracy"};
    private ResultsTable segmentationResults;

    private List<Float> getSortedValues(ImagePlus imp) {
        if (values == null || !values.containsKey(imp)) {
            if (values == null) {
                values = new HashMap<ImagePlus, List<Float>>();
            }

            values.put(imp, extractValues(imp));
            Collections.sort(values.get(imp));
        }
        return values.get(imp);
    }

    private List<Float> extractValues(ImagePlus imp) {
        List<Float> v = new ArrayList<Float>();
        for (int x = 0; x < imp.getWidth(); x++) {
            for (int y = 0; y < imp.getHeight(); y++) {
                float grayValue = imp.getProcessor().getPixelValue(x, y);
                if (grayValue == 255f) {
                    Float value = getImp().getProcessor().getPixelValue(x, y);
                    v.add(value);
                }
            }
        }
        return v;
    }

    public CountCellsScript() {
        super();
        this.segmentationResults = new ResultsTable();
    }

    public CountCellsScript(File file, File resultDir) {
        super(file, resultDir);
        this.segmentationResults = new ResultsTable();
    }

    @Override
    public Object begin() throws ScriptException {
        try {
            if (getImp().getProcessor().getNChannels() > 1) {
                throw new ScriptException(
                        Bundle.UI.getString("message_grayonly"));
            }

            ImagePlus imp = getImp();
            imp.getStack().deleteLastSlice();

            String p = "_(prob|PROB)\\.[a-zA-Z]*$";
            ImagePlus bin = new ImagePlus(getFile().getAbsolutePath()
                    .replaceAll(p, "_LABELS.TIF"));
            ImagePlus negBin = new ImagePlus(getFile().getAbsolutePath()
                    .replaceAll(p, "_NEG-LABELS.TIF"));
            ImagePlus posBin = new ImagePlus(getFile().getAbsolutePath()
                    .replaceAll(p, "_OST-LABELS.TIF"));

            saveScores(bin);

            List<Float> negVals = getSortedValues(negBin);
            List<Float> posVals = getSortedValues(posBin);
            if (negVals.isEmpty() && posVals.isEmpty()) {
                throw new ScriptException("No labels...");
            }

            this.medianNeg = median(negBin);
            this.medianPos = median(posBin);
            this.stdDevNeg = stdDev(negBin);
            this.stdDevPos = stdDev(posBin);

            Float negSide = (this.medianNeg + this.stdDevNeg);
            Float posSide = (this.medianPos - this.stdDevPos);
            this.threshold = Math.abs(posSide - negSide);
            this.threshold /= 2;
            this.threshold += (posSide > negSide) ? negSide : posSide;
            this.threshold = Math.abs(this.threshold);

            imp.setImage(imp.getProcessor().convertToFloat().createImage());
            imp.getProcessor().threshold(
                    new Float(this.threshold * 255).intValue());
            ImagePlusWriter writer = new ImagePlusWriter();
            writer.saveTiff(imp, genName(getFile(), "_THRESHOLDED.TIF"));

            // precision & recall computed with pixel values on binary images
            float[] precRec = precRecPixCompute(imp, posBin, negBin);

            // logging
            String log = getFile().getName() + "<br />threshold=" + threshold
                    + "<br />" + "precision: " + precRec[0] + "<br />"
                    + "recall: " + precRec[1] + "<br />" + "accuracy: "
                    + precRec[2];
            App.log(log);

            // save segmentation results
            segmentationResults.setPrecision(3);
            segmentationResults.incrementCounter();
            segmentationResults.addValue(resultsLabels[THRESHOLD],
                    this.threshold);
            segmentationResults.addValue(resultsLabels[PRECISION], precRec[0]);
            segmentationResults.addValue(resultsLabels[RECALL], precRec[1]);
            segmentationResults.addValue(resultsLabels[ACCURACY], precRec[2]);
            segmentationResults.saveAs(genName(getFile(), "_RESULTS.CSV"));

            // results to return
            Integer[] results = new Integer[4];
            posBin.getProcessor().erode();
            posBin.getProcessor().erode();
            posBin.getProcessor().autoThreshold();
            posBin.getProcessor().invert();
            results[RES_N_OST] = cntOstLabels(posBin);
            results[RES_AREA] = nPixels(255f, imp);
            results[RES_WIDTH] = imp.getWidth();
            results[RES_HEIGHT] = imp.getHeight();

            return results;
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    private void saveScores(ImagePlus bin) throws IOException {
        ResultsTable scoresFalse = new ResultsTable();
        ResultsTable scoresTrue = new ResultsTable();
        scoresFalse.setPrecision(3);
        scoresTrue.setPrecision(3);

        for (int x = 0; x < bin.getWidth(); x++) {
            for (int y = 0; y < bin.getHeight(); y++) {
                float grayValue = bin.getProcessor().getPixelValue(x, y);
                float score = getImp().getProcessor().getPixelValue(x, y);
                if (grayValue == 255f) {
                    scoresFalse.incrementCounter();
                    scoresFalse.addValue("score", score);
                    scoresFalse.addLabel("positive", "FALSE");
                } else if (grayValue == 0f) {
                    scoresTrue.incrementCounter();
                    scoresTrue.addValue("score", score);
                    scoresTrue.addLabel("positive", "TRUE");
                }
            }
        }

        scoresTrue.saveAs(genName(getFile(), "_SCORES-TRUE.CSV"));
        scoresFalse.saveAs(genName(getFile(), "_SCORES-FALSE.CSV"));
    }

    private int nPixels(float intensity, ImagePlus imp) {
        int nPixels = 0;
        for (int x = 0; x < imp.getWidth(); x++) {
            for (int y = 0; y < imp.getHeight(); y++) {
                float grayValue = imp.getProcessor().getPixelValue(x, y);
                if (grayValue == intensity) {
                    nPixels++;
                }
            }
        }
        return nPixels;
    }

    private int cntOstLabels(ImagePlus posBin) {
        ResultsTable paResults = new ResultsTable();
        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.SHOW_NONE, Measurements.AREA, paResults, 0.0,
                INFINITY);
        particleAnalyzer.analyze(posBin);
        return paResults.getCounter();
    }

    @Override
    public void end(File[] files) throws ScriptException {
        try {
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries data = new XYSeries("data");
            ResultsTable table = new ResultsTable();
            for (Object o : getScriptsResults()) {
                if (o instanceof Integer[]) {
                    Integer[] valuesLocal = (Integer[]) o;
                    // percent of square root of predicted area
                    double x_axis = valuesLocal[RES_N_OST];
                    double y_axis = Math.sqrt(valuesLocal[RES_AREA])
                            / Math.sqrt(valuesLocal[RES_HEIGHT] * valuesLocal[RES_WIDTH]);

                    // chart drawing
                    data.add(x_axis, y_axis);
                    table.incrementCounter();
                    table.addValue("nbosteo", x_axis);
                    table.addValue("predict", y_axis);
                }
            }
            table.saveAs(genName(null, "chart-area-correlation.csv"));

            dataset.addSeries(data);
            JFreeChart chart = ChartFactory
                    .createScatterPlot(
                    "Correlation between the predicted area and the number of osteoclasts",
                    "number of osteoclasts",
                    "squared ratio of preticted area", dataset,
                    PlotOrientation.VERTICAL, true, false, false);

            chart.getXYPlot().setBackgroundAlpha(0);
            chart.getXYPlot().setOutlineVisible(false);
            chart.getLegend().setPosition(RectangleEdge.RIGHT);
            chart.getLegend().setBorder(0d, 1d, 0d, 0d);
            chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);

            ChartUtilities.saveChartAsPNG(
                    new File(genName(null, "chart-area-correlation.png")),
                    chart, 1000, 1000);

            // // roc curves
            // saveROC(CountCellsScript.rocCurves, genName(null, "roc_all.png"),
            // false);
        } catch (IOException e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    private float[] precRecPixCompute(ImagePlus classifiedImp,
            ImagePlus posBin, ImagePlus negBin) throws IOException {
        int TP = 0;
        int TN = 0;
        int FP = 0;
        int FN = 0;

        for (int x = 0; x < classifiedImp.getWidth(); x++) {
            for (int y = 0; y < classifiedImp.getHeight(); y++) {
                float classifiedValue = classifiedImp.getProcessor()
                        .getPixelValue(x, y);
                float posValue = posBin.getProcessor().getPixelValue(x, y);
                float negValue = negBin.getProcessor().getPixelValue(x, y);

                if (classifiedValue == 255f && posValue == 255f) {
                    TP++;
                } else if (classifiedValue == 0f && negValue == 255f) {
                    TN++;
                } else if (classifiedValue == 255f && negValue == 255f) {
                    FP++;
                } else if (classifiedValue == 0f && posValue == 255f) {
                    FN++;
                }
            }
        }

        int P = TP + FN;
        int N = FP + TN;

        float precision = (float) TP / (float) (TP + FP);
        float recall = (float) TP / (float) P;
        float accuracy = ((float) TP + TN) / ((float) P + N);

        // genROC(mixSortedValues(getSortedValues(posBin),
        // getSortedValues(negBin)), P, N);

        return new float[]{precision, recall, accuracy};
    }

    @SuppressWarnings("unused")
    private List<Instance> mixSortedValues(List<Float> pos, List<Float> neg) {
        List<Instance> instances = new ArrayList<Instance>();

        int posCnt = 0, negCnt = 0;
        for (; !false;) {
            Float posV = (posCnt < pos.size()) ? pos.get(posCnt) : null;
            Float negV = (negCnt < neg.size()) ? neg.get(negCnt) : null;
            if (posV == null && negV == null) {
                break;
            } else if (posV == null) {
                instances.add(new Instance(negV.doubleValue(), false));
                negCnt++;
            } else if (negV == null) {
                instances.add(new Instance(posV.doubleValue(), true));
                posCnt++;
            } else if (posV <= negV) {
                instances.add(new Instance(posV.doubleValue(), true));
                posCnt++;
            } else if (posV > negV) {
                instances.add(new Instance(negV.doubleValue(), false));
                negCnt++;
            }
        }

        return instances;
    }

    @SuppressWarnings("unused")
    private void genROC(List<Instance> sortedInstances, int P, int N)
            throws IOException {
        int FP = 0, TP = 0;
        List<ROCPoint> R = new ArrayList<ROCPoint>();
        double fprev = -1d / 0d;

        for (ListIterator<Instance> li = sortedInstances
                .listIterator(sortedInstances.size()); li.hasPrevious();) {
            Instance i = li.previous();
            if (i.getValue() != fprev) {
                R.add(new ROCPoint(((double) FP / (double) N),
                        ((double) TP / (double) P)));
                fprev = i.getValue();
            }

            if (i.isPositive()) {
                TP++;
            } else {
                FP++;
            }
        }

        R.add(new ROCPoint(((double) FP / (double) N),
                ((double) TP / (double) P)));
        if (rocCurves.containsKey(getFile().getName())) {
            rocCurves.put(getFile().getName() + " (2)", R);
        } else {
            rocCurves.put(getFile().getName(), R);
        }

        Map<String, List<ROCPoint>> curves = new HashMap<String, List<ROCPoint>>();
        curves.put(getFile().getName(), R);
        saveROC(curves, genName(getFile(), "_ROC.PNG"), true);
    }

    private void saveROC(Map<String, List<ROCPoint>> ROCCurves, String path,
            boolean withLegend) throws IOException {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (Iterator<String> itKeys = ROCCurves.keySet().iterator(); itKeys
                .hasNext();) {
            String key = itKeys.next();
            XYSeries data = new XYSeries(key);
            for (ROCPoint p : ROCCurves.get(key)) {
                data.add(p.getFpRate(), p.getTpRate());
            }
            dataset.addSeries(data);
        }

        JFreeChart chart = ChartFactory.createXYLineChart("ROC curve",
                "fp rate", "tp rate", dataset, PlotOrientation.VERTICAL, true,
                false, false);

        chart.getXYPlot().setBackgroundAlpha(0);
        chart.getXYPlot().setOutlineVisible(false);
        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        chart.getLegend().setBorder(0d, 1d, 0d, 0d);
        chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
        chart.getLegend().setVisible(withLegend);

        ChartUtilities.saveChartAsPNG(new File(path), chart, 1000, 1000);
    }

    private Float median(ImagePlus binaryImp) {
        List<Float> v = getSortedValues(binaryImp);

        return v.get((v.size() / 2) - 1);
    }

    private Float stdDev(ImagePlus binaryImp) {
        List<Float> v = getSortedValues(binaryImp);
        Float mean = 0f;
        for (Float f : v) {
            mean += f;
        }
        mean /= v.size();

        List<Float> pop = new ArrayList<Float>();
        for (Float f : v) {
            pop.add((float) Math.pow((f - mean), 2));
        }

        Float stdDev = 0f;
        for (Float f : pop) {
            stdDev += f;
        }
        stdDev = (float) Math.sqrt(stdDev / pop.size());

        return stdDev;
    }

    @Override
    public String name() {
        return "Count cells";
    }

    @Override
    public boolean acceptStrOption(String optionName, String value) {
        return false;
    }

    @Override
    public OptionSet availableOptions(OptionSet po, String prevScName) {
        return null;
    }

    @Override
    public FileFilter getFileFilter() {
        return new ImagesFilter();
    }
}
