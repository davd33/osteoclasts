package org.osteo.gui.listeners;

import org.osteo.gui.ScriptFrame;

/**
 * Parent class for all the listeners belonging to the GUI: need a reference to
 * the ScriptFrame instance which is also reachable by the static
 * ScriptFrame.getInstance() method, but...
 * 
 * @author David Rueda
 * 
 */
public abstract class ScriptFrameListener {

	/**
	 * ScriptFrame unique instance.
	 */
	protected ScriptFrame scriptFrame;

	public ScriptFrameListener(ScriptFrame scriptFrame) {
		this.scriptFrame = scriptFrame;
	}

}
