package org.osteo.threads.states;

import org.osteo.gui.ScriptFrame;

public abstract class State {
	
	public static final int INIT = 0;
	public static final int ERROR = 1;
	public static final int FINISH = 2;
	public static final int FINISH_ERROR = 3;
	public static final int ENDING = 4;
	public static final int ENDING_ERROR = 5;
	
	protected ScriptFrame scriptFrame;
	
	public State(ScriptFrame scriptFrame) {
		this.scriptFrame = scriptFrame;
	}

	protected abstract State error() throws IllegalStateException;
	
	protected abstract State ending() throws IllegalStateException;
	
	protected abstract State endingError() throws IllegalStateException;
	
	protected abstract State finishError() throws IllegalStateException;
	
	protected abstract State finish() throws IllegalStateException;
	
	public abstract boolean isErrorState();
	
	public abstract boolean isEndingState();
	
	public abstract boolean isFinishState();
	
	public abstract int getStateCode();
	
	public State toEnding() throws IllegalStateException {
		if (!isErrorState()) {
			return ending();
		} else {
			return endingError();
		}
	}
	
	public State toFinish() throws IllegalStateException {
		if (!isErrorState()) {
			return finish();
		} else {
			return finishError();
		}
	}
	
	public State toError() throws IllegalStateException {
		if (isErrorState()) {
			return this;
		} else if (isEndingState()) {
			return endingError();
		} else if (isFinishState()) {
			return finishError();
		} else {
			return error();
		}
	}
	
	public boolean isState(int type) {
		return type == getStateCode();
	}
}
