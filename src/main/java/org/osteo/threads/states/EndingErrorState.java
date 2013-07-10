package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public class EndingErrorState extends State {

	public EndingErrorState(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	protected State error() throws IllegalStateException {
		throw new IllegalStateException("Script ending.");
	}

	@Override
	protected State ending() throws IllegalStateException {
		throw new IllegalStateException("Script ending with errors.");
	}

	@Override
	protected State finishError() throws IllegalStateException {
		return new FinishErrorState(scriptFrame);
	}

	@Override
	protected State finish() throws IllegalStateException {
		throw new IllegalStateException("Script ending with errors.");
	}

	@Override
	protected State endingError() throws IllegalStateException {
		return this;
	}

	@Override
	public int getStateCode() {
		return State.ENDING_ERROR;
	}

	@Override
	public boolean isErrorState() {
		return true;
	}

	@Override
	public boolean isEndingState() {
		return true;
	}

	@Override
	public boolean isFinishState() {
		return false;
	}

}
