package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.osteo.gui.ScriptFrame;

/**
 * When the user wants to choose some images for further analysis, this listener
 * is in charge to apply the user decisions (chose files, cancel...).
 *
 * @author David Rueda
 *
 */
public class FileChooserActionListener extends ScriptFrameListener implements
        ActionListener {

    public FileChooserActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        scriptFrame.setFileChooserFrameVisible(false);

        this.scriptFrame.updateImageTable(true);

        if (!e.getActionCommand().equals("update")) {
            new ResultsFileChooserActionListener(scriptFrame)
                    .actionPerformed(new ActionEvent(this, 1,
                    "ApproveSelection"));
        }
    }
}
