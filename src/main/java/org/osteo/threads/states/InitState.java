package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public class InitState extends State {
	
	public InitState(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	protected State error() throws IllegalStateException {
		return new ErrorState(scriptFrame);
	}

	@Override
	protected State finishError() throws IllegalStateException {
		throw new IllegalStateException("The end of the script has not been reached yet.");
	}

	@Override
	protected State finish() throws IllegalStateException {
		throw new IllegalStateException("The end of the script has not been reached yet.");
	}

	@Override
	protected State ending() throws IllegalStateException {
		return new EndingState(scriptFrame);
	}

	@Override
	protected State endingError() throws IllegalStateException {
		throw new IllegalStateException("No errors happend.");
	}

	@Override
	public int getStateCode() {
		return State.INIT;
	}

	@Override
	public boolean isErrorState() {
		return false;
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
