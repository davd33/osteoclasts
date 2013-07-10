package org.osteo.threads.tasks;

import java.util.Observable;

import org.osteo.gui.ScriptFrame;

public abstract class AbstractTask extends Observable implements Runnable {

    private ScriptFrame scriptFrame;

    public ScriptFrame getScriptFrame() {
        return scriptFrame;
    }

    public AbstractTask(ScriptFrame scriptFrame) {
        this.scriptFrame = scriptFrame;
    }

    protected void terminate(Object errorMessage) {
        setChanged();
        notifyObservers(errorMessage);
    }
}
