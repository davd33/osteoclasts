/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.scripts.imp;

import ij.ImagePlus;
import java.io.File;
import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;
import org.osteo.gui.filters.ImagesFilter;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.spiders.Environment;
import org.osteo.scripts.spiders.exceptions.EmptyEnvironmentException;
import org.osteo.scripts.util.OptionSet;

/**
 *
 * @author davidr
 */
public class SpidersScript extends AbstractScript {
    
    public SpidersScript() {
    }

    public SpidersScript(File image, File resultDir) {
        super(image, resultDir);
    }

    private Object deploySpiders() throws EmptyEnvironmentException {
        ImagePlus impLocal = getImp();
        
        Environment e = new Environment(impLocal);
        e.findNeighbors(null, null);
        
        return null;
    }
    
    @Override
    public Object begin() throws ScriptException, InterruptedException {
        try {
            return deploySpiders();
        } catch (Exception e) {
            String message = getErrorMessage(e);
            throw new ScriptException(message);
        }
    }

    @Override
    public void end(File[] files) throws ScriptException, InterruptedException {
    }

    @Override
    public String name() {
        return "Deploy spiders";
    }

    @Override
    public boolean acceptStrOption(String optionName, String value) {
        return false;
    }

    @Override
    public OptionSet availableOptions(OptionSet prevOptions, String prevScName) {
        OptionSet os = new OptionSet();
        return os;
    }

    @Override
    public FileFilter getFileFilter() {
        return new ImagesFilter();
    }
}
