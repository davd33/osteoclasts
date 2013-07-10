package org.osteo.scripts.imp;

import ij.ImagePlus;
import ij.process.ColorProcessor;

import java.awt.Color;
import java.io.File;
import java.util.Random;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.osteo.gui.filters.ImagesFilter;
import org.osteo.io.ImagePlusWriter;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.OptionSet;

public class LabelScript extends AbstractScript {

    public static final Color CYAN = new Color(0, 255, 255);
    public static final Color GREEN = new Color(0, 255, 0);
    private boolean randomlyBalanced = false;
    private int[] nExamplesOstNeg = null;

    public boolean isRandomlyBalanced() {
        return randomlyBalanced;
    }

    public void setRandomlyBalanced(boolean randomlyBalanced) {
        this.randomlyBalanced = randomlyBalanced;
    }

    public LabelScript() {
        super();
    }

    public LabelScript(File file, File resultDir) {
        super(file, resultDir);
    }

    public LabelScript(File file, File resultDir, boolean randomlyBalanced) {
        super(file, resultDir);
        this.randomlyBalanced = randomlyBalanced;
    }

    @Override
    public Object begin() throws ScriptException {
        try {
            ImagePlus imp = getImp();

            initiateNExamplesOstNeg(imp, CYAN, GREEN);

            ImagePlusWriter tiffWriter = new ImagePlusWriter();
            tiffWriter.saveTiff(osteoSelection(imp, CYAN),
                    genName(getFile(), "_OST-LABELS.TIF"));
            tiffWriter.saveTiff(negSelection(imp, GREEN),
                    genName(getFile(), "_NEG-LABELS.TIF"));
            tiffWriter.saveTiff(binSelection(imp, CYAN, GREEN),
                    genName(getFile(), "_LABELS.TIF"));

            return null;
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    private void initiateNExamplesOstNeg(ImagePlus imp, Color ost, Color neg) {
        ImagePlus selection = imp;
        this.nExamplesOstNeg = new int[]{0, 0};

        // thresholding by cyan pixels
        for (int x = 0; x < selection.getWidth(); x++) {
            for (int y = 0; y < selection.getHeight(); y++) {
                int[] pixel = selection.getPixel(x, y);
                if (pixel[0] == ost.getRed() && pixel[1] == ost.getGreen()
                        && pixel[2] == ost.getBlue()) {
                    this.nExamplesOstNeg[0]++;
                } else if (pixel[0] == neg.getRed()
                        && pixel[1] == neg.getGreen()
                        && pixel[2] == neg.getBlue()) {
                    this.nExamplesOstNeg[1]++;
                }
            }
        }
    }

    @Override
    public void end(File[] files) throws ScriptException {
    }

    private boolean test(double value) {
        if (this.randomlyBalanced) {
            return ((new Random().nextDouble()) <= (value));
        } else {
            return true;
        }
    }

    public ImagePlus osteoSelection(ImagePlus image, Color color) {
        image.setOverlay(null);
        image.deleteRoi();
        ImagePlus selection = image.duplicate();

        double rateNegOst = ((double) nExamplesOstNeg[1])
                / ((double) nExamplesOstNeg[0] + (double) nExamplesOstNeg[1]);

        // thresholding by cyan pixels
        ColorProcessor cp = new ColorProcessor(selection.getImage());
        for (int x = 0; x < selection.getWidth(); x++) {
            for (int y = 0; y < selection.getHeight(); y++) {
                int[] pixel = selection.getPixel(x, y);

                if (test(rateNegOst) && pixel[0] == color.getRed()
                        && pixel[1] == color.getGreen()
                        && pixel[2] == color.getBlue()) {
                    cp.setColor(Color.BLACK);
                } else { // non osteoclast
                    cp.setColor(Color.WHITE);
                }
                cp.drawPixel(x, y);
            }
        }

        // convert to 8-bit image
        selection.setImage(cp.convertToByte(true).createImage());

        // // getting measurements for each label
        // ResultsTable paResults = new ResultsTable();
        // ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
        // ParticleAnalyzer.SHOW_NONE,
        // Measurements.RECT | Measurements.ELLIPSE,
        // paResults,
        // 0.0,
        // INFINITY
        // );
        // particleAnalyzer.analyze(selection);
        //
        // // X Y Major Minor Angle
        // for (int row = 0; row < paResults.getCounter(); row++) {
        // double bX =
        // paResults.getColumnAsDoubles(paResults.getColumnIndex("BX"))[row],
        // bY =
        // paResults.getColumnAsDoubles(paResults.getColumnIndex("BY"))[row],
        // bWidth =
        // paResults.getColumnAsDoubles(paResults.getColumnIndex("Width"))[row],
        // bHeight =
        // paResults.getColumnAsDoubles(paResults.getColumnIndex("Height"))[row],
        // eAngle =
        // paResults.getColumnAsDoubles(paResults.getColumnIndex("Angle"))[row];
        //
        // EllipseRoi posSelection = null;
        // if (eAngle < 90) {
        // posSelection = new EllipseRoi(bX + bWidth, bY, bX, bY + bHeight,
        // 0.60);
        // } else {
        // posSelection = new EllipseRoi(bX, bY, bX + bWidth, bY + bHeight,
        // 0.60);
        // }
        //
        // selection.getProcessor().setColor(Color.BLACK);
        // selection.getProcessor().fillPolygon(posSelection.getPolygon());
        // }

        selection.getProcessor().invert();
        return selection;
    }

    public ImagePlus negSelection(ImagePlus image, Color color) {
        image.setOverlay(null);
        image.deleteRoi();
        ImagePlus selection = image.duplicate();

        double rateOstNeg = ((double) nExamplesOstNeg[0])
                / ((double) nExamplesOstNeg[1] + (double) nExamplesOstNeg[0]);

        ColorProcessor cp = new ColorProcessor(selection.getImage());
        for (int x = 0; x < selection.getWidth(); x++) {
            for (int y = 0; y < selection.getHeight(); y++) {
                int[] pixel = selection.getPixel(x, y);
                if (test(rateOstNeg) && pixel[0] == color.getRed()
                        && pixel[1] == color.getGreen()
                        && pixel[2] == color.getBlue()) {
                    cp.setColor(Color.WHITE);
                } else {
                    cp.setColor(Color.BLACK);
                }
                cp.drawPixel(x, y);
            }
        }

        selection.setImage(cp.convertToByte(true).createImage());
        return selection;
    }

    /**
     * The first color will be in black and the second in white. The rest of the
     * image will be in gray.
     *
     * @param image
     * @param ost
     * @param neg
     * @return an ImagePlus object
     */
    public ImagePlus binSelection(ImagePlus image, Color ost, Color neg) {
        image.setOverlay(null);
        image.deleteRoi();
        ImagePlus selection = image.duplicate();

        double rateOstNeg = ((double) nExamplesOstNeg[0])
                / ((double) nExamplesOstNeg[1] + (double) nExamplesOstNeg[0]);
        double rateNegOst = ((double) nExamplesOstNeg[1])
                / ((double) nExamplesOstNeg[0] + (double) nExamplesOstNeg[1]);

        // thresholding by cyan pixels
        ColorProcessor cp = new ColorProcessor(selection.getImage());
        for (int x = 0; x < selection.getWidth(); x++) {
            for (int y = 0; y < selection.getHeight(); y++) {
                int[] pixel = selection.getPixel(x, y);
                if (test(rateNegOst) && pixel[0] == ost.getRed()
                        && pixel[1] == ost.getGreen()
                        && pixel[2] == ost.getBlue()) {
                    // osteoclast
                    cp.setColor(Color.BLACK);
                } else if (test(rateOstNeg) && pixel[0] == neg.getRed()
                        && pixel[1] == neg.getGreen()
                        && pixel[2] == neg.getBlue()) {
                    // negative example
                    cp.setColor(Color.WHITE);
                } else {
                    cp.setColor(Color.GRAY);
                }
                cp.drawPixel(x, y);
            }
        }

        // convert to 8-bit image
        selection.setImage(cp.convertToByte(true).createImage());
        return selection;
    }

    @Override
    public String name() {
        return "Create binaries";
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
