package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public class ErrorState extends State {
	
	public ErrorState(ScriptFrame scriptFrame) {
		super(scriptFrame);
		scriptFrame.cleanMessages();
	}

	@Override
	protected State error() {
		return this;
	}

	@Override
	protected State finishError() {
		throw new IllegalStateException("The end of the script has not been reached yet.");
	}

	@Override
	protected State finish() {
		throw new IllegalStateException("The end of the script has not been reached yet.");
	}

	@Override
	protected State ending() throws IllegalStateException {
		throw new IllegalStateException("An error happend.");
	}

	@Override
	protected State endingError() throws IllegalStateException {
		return new EndingErrorState(scriptFrame);
	}

	@Override
	public int getStateCode() {
		return State.ERROR;
	}

	@Override
	public boolean isErrorState() {
		return true;
	}

	@Override
	public boolean isEndingState() {
		return false;
	}

	@Override
	public boolean isFinishState() {
		return false;
	}

}
