package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.osteo.gui.ScriptFrame;
import org.osteo.main.App;
import org.osteo.threads.ScriptPoolManager;

/**
 * Listener for stopping all the running tasks.
 *
 * @author David Rueda
 *
 */
public class StopButtonActionListener extends ScriptFrameListener implements ActionListener {

    public StopButtonActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScriptPoolManager.getInstance().abortAllTasks();
        App.log("Tasks where interrupted");
        scriptFrame.enableCommandsPanel();
    }
}
