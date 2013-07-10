package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.osteo.gui.ScriptFrame;

/**
 * Pop the images chooser up.
 *
 * @author David Rueda
 *
 */
public class FileChooserButtonActionListener extends ScriptFrameListener implements ActionListener {

    public FileChooserButtonActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        scriptFrame.setFileChooserFrameVisible(true);
    }
}
