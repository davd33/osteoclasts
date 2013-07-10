package org.osteo.threads.tasks;

import java.io.File;
import java.util.List;

import org.osteo.gui.ScriptFrame;
import org.osteo.scripts.AbstractScript;

public class EndScriptTask extends ScriptTask {

    private File[] files;

    public EndScriptTask(Class<? extends AbstractScript> type,
            ScriptFrame scriptFrame, File directory, File[] files,
            List<Object> tasksResults) {
        super(scriptFrame, new File(System.getProperty("user.dir")), directory,
                type);
        this.files = files;

        getScript().setScriptsResults(tasksResults);
    }

    @Override
    public void run() {
        String message = null;
        try {
            getScript().end(files);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            message = e.getMessage();
        } finally {
            terminate(message);
        }
    }
}
