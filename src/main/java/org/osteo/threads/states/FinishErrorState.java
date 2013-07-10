package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public class FinishErrorState extends State {
	
	public FinishErrorState(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	protected State error() {
		throw new IllegalStateException("Finish state already got.");
	}

	@Override
	protected State finishError() {
		return this;
	}

	@Override
	protected State finish() {
		throw new IllegalStateException("Finish state already got.");
	}

	@Override
	protected State ending() throws IllegalStateException {
		throw new IllegalStateException("Finish state already got.");
	}

	@Override
	protected State endingError() throws IllegalStateException {
		throw new IllegalStateException("Finish state already got.");
	}

	@Override
	public int getStateCode() {
		return State.FINISH_ERROR;
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
		return true;
	}

}
