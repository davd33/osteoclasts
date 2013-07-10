package org.osteo.scripts.imp;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.ParticleAnalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.osteo.gui.filters.ImagesFilter;
import org.osteo.io.ImagePlusWriter;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.OptionSet;
import org.osteo.scripts.util.PicSummary;

/**
 * Handle all the steps for the particle analysis of an osteoclast image.
 *
 * @author David Rueda
 *
 */
public class PitScript extends AbstractScript {

    private String resFilePath;
    private String imgFilePath;
    private String sumFilePath;
    private Map<PicSummary, Double> summary = new HashMap<PicSummary, Double>();
    private String summarySlice;

    /**
     * Get one of the values of the particle analysis summary.
     */
    public Double getSummary(PicSummary option) throws IllegalAccessException {

        Double res = summary.get(option);
        if (res != null) {
            return res;
        } else {
            throw new IllegalAccessError("The script must be started.");
        }
    }

    public String getSummarySlice() {
        return this.summarySlice;
    }

    public String getSummaryString() {
        if (summary.isEmpty()) {
            return null;
        } else {
            StringBuilder str = new StringBuilder(this.summarySlice);
            str.append(',');

            PicSummary[] values = PicSummary.values();
            for (int v = 0; v < values.length; v++) {
                PicSummary s = values[v];
                str.append(summary.get(s));
                if (v < (values.length - 1)) {
                    str.append(',');
                }
            }

            return str.substring(0);
        }
    }

    public String getResFileName() {
        return resFilePath;
    }

    public void setResFileName(String resFileName) {
        this.resFilePath = resFileName;
    }

    public String getImgFileName() {
        return imgFilePath;
    }

    public void setImgFileName(String imgFileName) {
        this.imgFilePath = imgFileName;
    }

    public String getSumFileName() {
        return sumFilePath;
    }

    public void setSumFileName(String sumFileName) {
        this.sumFilePath = sumFileName;
    }

    public PitScript() {
        super();
    }

    public PitScript(File file, File resultDir) {
        super(file, resultDir);
        this.imgFilePath = resultDir.getAbsolutePath() + File.separator
                + file.getName() + "_particules.tif";
        this.resFilePath = resultDir.getAbsolutePath() + File.separator
                + file.getName() + "_results.csv";
        this.sumFilePath = resultDir.getAbsolutePath() + File.separator
                + file.getName() + "_summary.csv";
    }

    /**
     * Start the script.
     *
     * @throws ScriptException
     */
    @Override
    public Object begin() throws ScriptException {
        try {
            // image processes
            ImagePlus imp = getImp();
            // enhance contrast
            ContrastEnhancer ce = new ContrastEnhancer();
            ce.stretchHistogram(imp, 0.35);
            imp.getProcessor().autoThreshold();

            ResultsTable paResults = new ResultsTable();
            analyze(paResults, imp);
            save(paResults, resFilePath);

            savePic(imp, imgFilePath);
            updateSummary(this);
            saveSummary(sumFilePath, getSummaryString());

            return null;
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    private static synchronized void savePic(ImagePlus imp, String path)
            throws IllegalAccessException, IOException {
        ImagePlusWriter impWriter = new ImagePlusWriter();
        impWriter.saveTiff(imp, path);
    }

    private static synchronized void analyze(ResultsTable paResults, ImagePlus imp) {
        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.IN_SITU_SHOW
                | ParticleAnalyzer.SHOW_OUTLINES, Measurements.AREA
                | Measurements.MEAN | Measurements.MIN_MAX,
                paResults, 0.0, INFINITY);
        particleAnalyzer.analyze(imp);
    }

    private static synchronized void save(ResultsTable rt, String path)
            throws IOException {
        rt.saveAs(path);
    }

    /**
     * Save the summary of the current processed image in a csv document located
     * in the image directory.
     */
    private static synchronized void saveSummary(String path, String sumStr) {
        BufferedWriter buffWriter = null;
        try {
            File summaryFile = new File(path);
            if (!summaryFile.exists()) {
                summaryFile.createNewFile();
            }

            buffWriter = new BufferedWriter(new FileWriter(summaryFile));
            buffWriter.write(PicSummary.headNames());
            buffWriter.newLine();
            buffWriter.write(sumStr);
        } catch (IOException e) {
            log(Level.SEVERE,
                    "Problem for saving summary informations: "
                    + e.getMessage());
        } finally {
            try {
                buffWriter.close();
            } catch (IOException e) {
                log(Level.SEVERE, "Problem for saving summary informations: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Open xls results file and calculate summary.
     */
    private static synchronized void updateSummary(PitScript ps) {
        try {
            List<List<String>> results = openCSV(ps.resFilePath);

            ps.summarySlice = ps.getFile().getName();

            // init summary
            for (PicSummary s : PicSummary.values()) {
                ps.summary.put(s, 0d);
            }

            for (Iterator<List<String>> rowIt = results.iterator(); rowIt
                    .hasNext();) {
                List<String> row = rowIt.next();

                // count
                if (!rowIt.hasNext()) {
                    ps.summary.put(PicSummary.COUNT,
                            Double.parseDouble(row.get(0)));
                }
                // total area
                ps.summary.put(
                        PicSummary.AREA_TOT,
                        ps.summary.get(PicSummary.AREA_TOT)
                        + Double.parseDouble(row.get(1)));
                // mean
                ps.summary.put(PicSummary.MEAN, ps.summary.get(PicSummary.MEAN)
                        + Double.parseDouble(row.get(2)));
            }

            // average of area
            ps.summary.put(PicSummary.AREA_AVG, ps.summary.get(PicSummary.AREA_TOT)
                    / ps.summary.get(PicSummary.COUNT));
            // % area
            double area = ps.getImp().getStatistics().area;
            ps.summary.put(
                    PicSummary.AREA_PERCENT,
                    ((1 - ((area - ps.summary.get(PicSummary.AREA_TOT)) / area)) * 100));
            // mean
            ps.summary.put(
                    PicSummary.MEAN,
                    ps.summary.get(PicSummary.MEAN)
                    / ps.summary.get(PicSummary.COUNT));
        } catch (FileNotFoundException e) {
            log(Level.SEVERE, ps.getFile().getName()
                    + " results file does not exist.");
        } catch (IOException e) {
            log(Level.SEVERE, "Unable to read " + ps.getFile().getName()
                    + "results file.");
        }
    }

    @Override
    public void end(File[] files) throws ScriptException {

        String directory = getResultDir().getAbsolutePath();
        if (directory == null) {
            throw new ScriptException("No result directory selected.");
        }

        File sumDirectory = new File(directory);
        if (!sumDirectory.exists()) {
            throw new ScriptException(
                    "The global summary directory does not exists.");
        }
        if (!sumDirectory.isDirectory()) {
            throw new ScriptException("'" + directory + "' is not a directory.");
        }

        File globalSummaryFile = new File(sumDirectory.getAbsolutePath()
                + "/summary.csv");
        try {
            globalSummaryFile.createNewFile();
        } catch (IOException e) {
            throw new ScriptException(
                    "The global summary file could not be created.");
        }

        // getting the summary files in the current directory
        List<File> summaries = new ArrayList<File>();
        for (File s : sumDirectory.listFiles()) {
            if (s.getAbsolutePath().endsWith("_summary.csv")) {
                summaries.add(s);
            }
        }

        // getting the values of each summary
        Map<String, String> summariesContentsTmp = new HashMap<String, String>();
        for (Iterator<File> sumIt = summaries.iterator(); sumIt.hasNext();) {
            File summaryFile = sumIt.next();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(summaryFile));
                reader.readLine();
                summariesContentsTmp.put(summaryFile.getName(),
                        reader.readLine());
                summaryFile.delete();
            } catch (FileNotFoundException e) {
                throw new ScriptException(summaryFile.getName()
                        + "' summary file not found.");
            } catch (IOException e) {
                throw new ScriptException("Error reading '"
                        + summaryFile.getName() + "' summary file.");
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (NullPointerException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        // sort the summaries by name
        List<String> summariesNames = new ArrayList<String>();
        summariesNames.addAll(summariesContentsTmp.keySet());
        Collections.sort(summariesNames);

        // sorted contents
        Map<String, String> summariesContents = new LinkedHashMap<String, String>();
        for (Iterator<String> nameIt = summariesNames.iterator(); nameIt
                .hasNext();) {
            String name = nameIt.next();
            summariesContents.put(name, summariesContentsTmp.get(name));
        }

        // writing in the global summary
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(globalSummaryFile));
            writer.write(PicSummary.headNames());
            for (Iterator<String> contentIt = summariesContents.values()
                    .iterator(); contentIt.hasNext();) {
                String content = contentIt.next();
                writer.newLine();
                writer.write(content);
            }
        } catch (IOException e) {
            throw new ScriptException(
                    "Unable to write into the global summary file ("
                    + globalSummaryFile.getAbsolutePath() + ")");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new ScriptException("Error closing '"
                        + globalSummaryFile.getAbsolutePath() + "'");
            }
        }
    }

    /**
     * Open the result file of the current analyzed image, and return the data
     * in a matrix containing the lines and rows of the document.
     *
     * @return the results matrix
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static List<List<String>> openCSV(String path)
            throws FileNotFoundException, IOException {

        List<List<String>> res = null;
        BufferedReader reader = null;
        try {
            res = new ArrayList<List<String>>();
            reader = new BufferedReader(new FileReader(new File(path)));

            String line = null;
            boolean notFirstLine = false;
            while ((line = reader.readLine()) != null) {
                if (notFirstLine) {
                    String[] cols = line.split(",");
                    List<String> colsList = new ArrayList<String>();
                    Collections.addAll(colsList, cols);
                    res.add(colsList);
                } else {
                    notFirstLine = true;
                }
            }
        } finally {
            reader.close();
        }
        return res;
    }

    private static void log(Level level, Object message) {
        Logger.getLogger(PitScript.class.getName()).log(level,
                message.toString());
    }

    @Override
    public String name() {
        return "Particle analyser";
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
