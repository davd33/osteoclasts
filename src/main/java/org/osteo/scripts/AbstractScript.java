package org.osteo.scripts;

import ij.ImagePlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.script.ScriptException;
import javax.swing.filechooser.FileFilter;

import org.osteo.main.App;
import org.osteo.main.Bundle;
import org.osteo.scripts.util.Option;
import org.osteo.scripts.util.OptionSet;

public abstract class AbstractScript {

    private File file;
    private File resultDir;
    private ImagePlus imp;
    private List<Object> scriptsResults;
    protected final static double INFINITY = 1.0 / 0.0;
    protected OptionSet options;

    @SuppressWarnings("unchecked")
    public void loadOptions() {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(
                    new File("saves" + File.separator + "script-options-"
                    + this.name() + ".obj")));
            options = (OptionSet) in.readObject();
            if (options != null) {
                App.log("Script options loaded.");
            } else {
                App.log("Unable to load options.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to load options for '" + this.name()
                    + "' script.");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Unable to load options for '" + this.name()
                    + "' script.");
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to load options for '" + this.name()
                    + "' script.");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (NullPointerException e) {
            }
        }
    }

    public void saveOptions() {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(
                    new File("saves" + File.separator + "script-options-"
                    + this.name() + ".obj")));
            out.writeObject(options);
            App.log("Script options saved.");
        } catch (FileNotFoundException e) {
            System.out.println("Unable to save options for '" + this.name()
                    + "' script.");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Unable to save options for '" + this.name()
                    + "' script.");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public OptionSet getOptions() {
        if (options == null) {
            options = new OptionSet();
        }
        return options;
    }

    public void setOptions(OptionSet options) {
        this.options = options;
    }

    protected File getResultDir() {
        return resultDir;
    }

    public File getFile() {
        return file;
    }

    protected ImagePlus getImp() {
        if (imp == null) {
            this.imp = new ImagePlus(file.getAbsolutePath());
        }
        return imp;
    }

    public AbstractScript() {
    }

    public AbstractScript(File file, File resultDir) {
        this.file = file;
        this.resultDir = resultDir;
    }

    public List<Object> getScriptsResults() {
        return scriptsResults;
    }

    public void setScriptsResults(List<Object> scriptsResults) {
        this.scriptsResults = scriptsResults;
    }

    public abstract Object begin() throws ScriptException, InterruptedException;

    public abstract void end(File[] files) throws ScriptException, InterruptedException;

    public abstract String name();

    public abstract boolean acceptStrOption(String optionName, String value);

    public abstract OptionSet availableOptions(OptionSet prevOptions, String prevScName);

    public abstract FileFilter getFileFilter();

    public void updatedValue(OptionSet os, Option option) {
    }

    public void updateOptions() {
    }

    public void clearOptions() {
        this.options = null;
    }

    public boolean isOptionAvailable(String name) {
        OptionSet av = availableOptions(null, null);
        for (Option o : av) {
            if (o.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Option getOption(String optionName) {
        for (Option o : getOptions()) {
            if (o.getName().equals(optionName)) {
                return o;
            }
        }
        return null;
    }

    public String getErrorMessage(Exception e) {
        String errMessage = e.getMessage();
        return Bundle.UI.getFormatedString("message_script_error",
                new Object[]{getFile().getName(),
            (errMessage == null ? "" : errMessage)});
    }

    public String getErrorMessage(Exception e, File file) {
        String errMessage = e.getMessage();
        return Bundle.UI.getFormatedString("message_script_error",
                new Object[]{(file != null ? file.getName() : ""),
            (errMessage == null ? "" : errMessage)});
    }

    protected String genName(File f, String subExtension) throws IOException {
        return genName(f, subExtension, null);
    }

    protected String genName(File f, String subExtension, String subdirectory) throws IOException {
        if (f == null) {
            if (subdirectory != null) {
                File directory = getResultDir();
                File subdir = new File(directory.getAbsoluteFile().getAbsolutePath() + File.separator + subdirectory);
                if (!(subdir).exists() || !subdir.isDirectory()) {
                    subdir.mkdir();
                }
                return subdir.getAbsoluteFile().getAbsolutePath() + File.separator + subExtension;
            } else {
                return getResultDir().getAbsoluteFile().getAbsolutePath() + File.separator
                        + subExtension;
            }
        }
        if (subdirectory != null) {
            File directory = getResultDir();
            File subdir = new File(directory.getAbsoluteFile().getAbsolutePath() + File.separator + subdirectory);
            if (!(subdir).exists() || !subdir.isDirectory()) {
                subdir.mkdir();
            }
            return subdir.getAbsoluteFile().getAbsolutePath() + File.separator
                    + f.getName().replaceAll("\\.[a-zA-Z]{2,4}$", "")
                    + subExtension;
        } else {
            return getResultDir().getAbsoluteFile().getAbsolutePath() + File.separator
                    + f.getName().replaceAll("\\.[a-zA-Z]{2,4}$", "")
                    + subExtension;
        }
    }
}
