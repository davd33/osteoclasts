package org.osteo.scripts.imp;

import ij.ImagePlus;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.osteo.gui.filters.ImagesFilter;
import org.osteo.io.ImagePlusWriter;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.OptionSet;

public class TrainingDataScript extends AbstractScript implements MouseListener {

    private final TrainingDataScript.Stop stop = new Stop();

    private class Stop {

        boolean sel = true;
        boolean imp = true;
    }

    public TrainingDataScript() {
        super();
    }

    public TrainingDataScript(File file, File resultDir) {
        super(file, resultDir);
    }

    @Override
    public Object begin() throws ScriptException {
        return null;
    }

    @Override
    public void end(File[] files) throws ScriptException {
        try {
            int counter = 1;
            for (File f : files) {
                ImagePlus imp = new ImagePlus(f.getAbsolutePath());
                ImagePlus labelledImp = new ImagePlus(f.getAbsolutePath().replaceAll("\\.[a-zA-Z]{2,4}$", "") + "-1.TIF");

                labelledImp.show();
                labelledImp.getCanvas().addMouseListener(this);

                stop.imp = true;
                while (stop.imp) {
                    stop.sel = true;
                    for (; stop.sel; System.out.print(""));

                    if (stop.imp) {
                        ImagePlus savedImp = new ImagePlus();
                        imp.setRoi(labelledImp.getRoi());
                        savedImp.setProcessor(imp.getProcessor().crop());
                        ImagePlusWriter tiffWriter = new ImagePlusWriter();
                        tiffWriter.saveTiff(savedImp, genName(null, counter + ".TIF"));
                        savedImp.setProcessor(labelledImp.getProcessor().crop());
                        tiffWriter.saveTiff(savedImp, genName(null, counter + "-1.TIF"));

                        counter++;
                    }
                }

                labelledImp.getCanvas().removeMouseListener(this);
                labelledImp.close();
            }
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isControlDown()) {
            stop.imp = false;
            stop.sel = false;
        } else {
            stop.sel = false;
        }
    }

    @Override
    public String name() {
        return "Create training data";
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
