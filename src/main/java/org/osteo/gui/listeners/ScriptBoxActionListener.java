package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.osteo.gui.ScriptFrame;

/**
 * Update script options GUI panel.
 *
 * @author David Rueda
 *
 */
public class ScriptBoxActionListener extends ScriptFrameListener implements ActionListener {

    public ScriptBoxActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        scriptFrame.updateFilter();
        scriptFrame.updateSelectedFiles();
        scriptFrame.setScript(null);
        scriptFrame.updateScriptOptions(true);
    }
}
