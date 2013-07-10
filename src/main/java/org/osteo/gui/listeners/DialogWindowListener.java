package org.osteo.gui.listeners;


import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.osteo.gui.ScriptFrame;

/**
 * Window listener for dialogs. Useful, but not so much used...
 * 
 * @author David Rueda
 *
 */
public class DialogWindowListener extends ScriptFrameListener implements WindowListener {

	public DialogWindowListener(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	/**
	 * Used to get back main window focus.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		scriptFrame.requestFocus();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

}
