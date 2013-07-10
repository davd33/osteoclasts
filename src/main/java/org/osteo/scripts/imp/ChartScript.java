package org.osteo.scripts.imp;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.VerticalAlignment;
import org.osteo.gui.filters.ImagesFilter;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.OptionSet;

public class ChartScript extends AbstractScript {

    private Map<String, List<Float>> charts;

    public ChartScript() {
        super();
        this.charts = new HashMap<String, List<Float>>();
    }

    public ChartScript(File file, File resultDir) {
        super(file, resultDir);
        this.charts = new HashMap<String, List<Float>>();
    }

    @Override
    public Object begin() throws ScriptException {
        try {
            if (getImp().getProcessor().getNChannels() > 1) {
                throw new ScriptException(
                        Bundle.UI.getString("message_grayonly"));
            }

            // enhance contrast
            ContrastEnhancer ce = new ContrastEnhancer();
            ce.stretchHistogram(getImp(), 0.35);

            String p = "_(prob|PROB)\\.[a-zA-Z]*$";
            ImagePlus negBin = new ImagePlus(getFile().getAbsolutePath()
                    .replaceAll(p, "_NEG-LABELS.TIF"));
            ImagePlus ostBin = new ImagePlus(getFile().getAbsolutePath()
                    .replaceAll(p, "_OST-LABELS.TIF"));

            updateChart(negBin, "Negative");
            updateChart(ostBin, "Positive");

            saveCharts();
            return null;
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    private void saveCharts() throws IOException {
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        String plotTitle = getFile().getName(), xAxis = "Probability", yAxis = "Number of pixels";

        for (Iterator<List<Float>> chartsIt = charts.values().iterator(); chartsIt
                .hasNext();) {
            HistogramDataset subdataset = new HistogramDataset();
            subdataset.setType(HistogramType.FREQUENCY);

            List<Float> chart = chartsIt.next();
            double[] data = new double[chart.size()];
            int cnt = 0;
            for (Iterator<Float> chartIt = chart.iterator(); chartIt.hasNext();) {
                double value = chartIt.next().doubleValue();
                data[cnt++] = value;
            }
            String chartTitle = "";
            for (Iterator<String> chartsKeysIt = charts.keySet().iterator(); chartsKeysIt
                    .hasNext();) {
                String key = chartsKeysIt.next();
                if (chart.equals(charts.get(key))) {
                    chartTitle = key;
                    break;
                }
            }
            dataset.addSeries(chartTitle, data, data.length);
            subdataset.addSeries(chartTitle, data, data.length);

            JFreeChart histogram = ChartFactory.createHistogram(plotTitle,
                    xAxis, yAxis, subdataset, PlotOrientation.VERTICAL, true,
                    false, false);

            histogram.getXYPlot().setBackgroundAlpha(0);
            histogram.getXYPlot().setOutlineVisible(false);
            histogram.getLegend().setPosition(RectangleEdge.RIGHT);
            histogram.getLegend().setBorder(0d, 1d, 0d, 0d);
            histogram.getLegend().setVerticalAlignment(VerticalAlignment.TOP);

            ChartUtilities
                    .saveChartAsPNG(
                    new File(genName(getFile(), "_chart-" + chartTitle
                    + ".png")), histogram, getImp().getWidth(),
                    getImp().getHeight());
        }

        JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis,
                yAxis, dataset, PlotOrientation.VERTICAL, true, false, false);

        ChartUtilities.saveChartAsPNG(
                new File(genName(getFile(), "_chart-both.png")), chart,
                getImp().getWidth(), getImp().getHeight());
    }

    private void updateChart(ImagePlus ostBin, String name) {
        ImagePlus binImp = ostBin;

        List<Float> chart = new ArrayList<Float>();
        for (int x = 0; x < binImp.getWidth(); x++) {
            for (int y = 0; y < binImp.getHeight(); y++) {
                float grayValue = binImp.getProcessor().getPixelValue(x, y);
                if (grayValue == 255f) {
                    Float value = getImp().getProcessor().getPixelValue(x, y);
                    chart.add(value);
                }
            }
        }

        if (!chart.isEmpty()) {
            charts.put(name, chart);
        }
    }

    @Override
    public void end(File[] files) throws ScriptException {
    }

    @Override
    public String name() {
        return "Draw charts";
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
