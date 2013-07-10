package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.osteo.gui.ScriptFrame;

/**
 * Pop the results directory chooser up.
 * 
 * @author David Rueda
 *
 */
public class ResultsFileChooserButtonActionListener extends ScriptFrameListener implements ActionListener {

	public ResultsFileChooserButtonActionListener(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		scriptFrame.setResultDirChooserFrameVisible(true);
	}

}
