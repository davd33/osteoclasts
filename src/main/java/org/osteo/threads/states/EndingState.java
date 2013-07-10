package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public class EndingState extends State {

	public EndingState(ScriptFrame scriptFrame) {
		super(scriptFrame);
	}

	@Override
	protected State error() throws IllegalStateException {
		throw new IllegalStateException("Script ending.");
	}

	@Override
	protected State finishError() throws IllegalStateException {
		throw new IllegalStateException("Script ending without errors.");
	}

	@Override
	protected State finish() throws IllegalStateException {
		return new FinishState(scriptFrame);
	}

	@Override
	protected State ending() throws IllegalStateException {
		return this;
	}

	@Override
	protected State endingError() throws IllegalStateException {
		return new EndingErrorState(scriptFrame);
	}

	@Override
	public int getStateCode() {
		return State.ENDING;
	}

	@Override
	public boolean isErrorState() {
		return false;
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
