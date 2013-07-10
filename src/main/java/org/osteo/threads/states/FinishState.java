package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public class FinishState extends State {
	
	public FinishState(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	protected State error() {
		throw new IllegalStateException("Finish state already got.");
	}

	@Override
	protected State finishError() {
		return new FinishErrorState(scriptFrame);
	}

	@Override
	protected State finish() {
		return this;
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
		return State.FINISH;
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
		return true;
	}

}
