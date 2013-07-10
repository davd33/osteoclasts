package org.osteo.threads.tasks;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.osteo.gui.ScriptFrame;
import org.osteo.scripts.AbstractScript;

public class ScriptTask extends AbstractTask {

    private File file;
    private File resultDir;
    private AbstractScript script;
    protected Object taskResult;

    public Object getTaskResult() {
        return taskResult;
    }

    protected File getResultDir() {
        return resultDir;
    }

    protected File getFile() {
        return file;
    }

    public AbstractScript getScript() {
        return script;
    }

    protected void setScript(AbstractScript script) {
        this.script = script;
    }

    public String getFileName() {
        return file.getName();
    }

    public Class<? extends AbstractScript> getScriptType() {
        return script.getClass();
    }

    public ScriptTask(ScriptFrame scriptFrame, File file, File resultDir,
            Class<? extends AbstractScript> type) {
        super(scriptFrame);
        this.file = file;
        this.resultDir = resultDir;

        try {
            Constructor<?> cScript = type
                    .getConstructor(File.class, File.class);
            script = (AbstractScript) cScript.newInstance(file, resultDir);
        } catch (InstantiationException e) {
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        } catch (NoSuchMethodException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (InvocationTargetException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        String message = null;
//		boolean errorHappend = false;
//		ScriptFileCollection processedFiles = ScriptFrame.getInstance().getProcessedFiles();
//		File scFile = script.getFile();
        try {
            if (script != null) {
//				processedFiles.edit(scFile, ScriptFile.Status.IN_PROGRESS);
//				ScriptFrame.getInstance().updateImageTable(false);
                this.taskResult = script.begin();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            message = e.getMessage();
//			errorHappend = true;
        } finally {
//			if (script != null) {
//				if (errorHappend)
//					processedFiles.edit(scFile, ScriptFile.Status.ERROR);
//				else
//					processedFiles.edit(scFile, ScriptFile.Status.PROCESSED);
//				ScriptFrame.getInstance().updateImageTable(false);
//			}
            terminate(message);
        }
    }
}
