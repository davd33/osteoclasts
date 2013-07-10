package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.osteo.gui.ScriptFrame;
import org.osteo.main.Bundle;

/**
 * When the user wants to choose some images for further analysis, this listener
 * is in charge to apply the user decisions (chose directory for saving script's
 * results, cancel...).
 *
 * @author David Rueda
 *
 */
public class ResultsFileChooserActionListener extends ScriptFrameListener
        implements ActionListener {

    public ResultsFileChooserActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        scriptFrame.setResultDirChooserFrameVisible(false);

        File resultsDir = scriptFrame.getSelectedDir();
        if (resultsDir != null && resultsDir.isDirectory()) {
            if (scriptFrame.getSelectedFiles().length > 0) {
                scriptFrame.setRunButtonVisible(true);
            }

            if (e.getActionCommand().equals("ApproveSelection")) {
                scriptFrame.addInfoMessage(Bundle.UI.getFormatedString(
                        "message_resdir_selected", resultsDir.getPath()));
            }
        }
    }
}
