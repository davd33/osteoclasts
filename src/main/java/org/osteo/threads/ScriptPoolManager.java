package org.osteo.threads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osteo.gui.ScriptFrame;
import org.osteo.main.Bundle;
import org.osteo.threads.states.InitState;
import org.osteo.threads.states.State;
import org.osteo.threads.tasks.AbstractTask;
import org.osteo.threads.tasks.EndScriptTask;
import org.osteo.threads.tasks.ScriptTask;

public class ScriptPoolManager implements Observer {

    private static ScriptPoolManager instance;
    private ExecutorService executor;
    private List<AbstractTask> managedTasks = new ArrayList<AbstractTask>();
    private List<Object> tasksResults = new ArrayList<Object>();
    private State state;

    protected State getState() {
        return state;
    }

    protected void setStateInit() {
        state = new InitState(ScriptFrame.getInstance());
    }

    protected void setStateError() {
        try {
            state = state.toError();
        } catch (IllegalStateException e) {
        }
    }

    protected void setStateEnding() {
        try {
            state = state.toEnding();
        } catch (IllegalStateException e) {
        }
    }

    protected void setStateFinish() {
        try {
            state = state.toFinish();
        } catch (IllegalStateException e) {
        }
    }

    public List<AbstractTask> getTasks() {
        return managedTasks;
    }

    public synchronized static ScriptPoolManager getInstance() {
        if (instance == null) {
            instance = new ScriptPoolManager();
        }
        return instance;
    }

    private ScriptPoolManager() {
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public synchronized void update(Observable observable, Object value) {
        ScriptTask task = (ScriptTask) observable;

        managedTasks.remove(observable);
        tasksResults.add(task.getTaskResult());

        // error happened
        if (value != null) {
            setStateError();
            task.getScriptFrame().addErrorMessage(value.toString());
        }

        // all tasks processed
        if (managedTasks.isEmpty()) {
            setStateEnding();
        }

        // global end task
        if (state.isEndingState()) {
            if (!state.isErrorState()) {
                EndScriptTask endTask = new EndScriptTask(task.getScriptType(),
                        task.getScriptFrame(), task.getScriptFrame()
                        .getSelectedDir(), task.getScriptFrame()
                        .getSelectedFiles(), tasksResults);
                endTask.getScript().setOptions(
                        ScriptFrame.getInstance().getOptions());
                addTask(endTask);

                task.getScriptFrame().cleanMessages();
                task.getScriptFrame().addInfoMessage("Global script end task");

                submitManagedTasks();
                setStateFinish();
            } else {
                task.getScriptFrame().enableCommandsPanel();
                task.getScriptFrame().cleanMessages();
                task.getScriptFrame().addErrorMessage(
                        "Script finished with error(s)");
                if (ScriptFrame.isShellMod()) {
                    System.exit(0);
                }
            }
        } else if (state.isFinishState()) {
            if (!state.isErrorState()) {
                task.getScriptFrame().cleanMessages();
                task.getScriptFrame().addSuccessMessage(
                        task.getScriptFrame().getSelectedFiles().length
                        + " images analyzed");
            }
            task.getScriptFrame().enableCommandsPanel();
            if (ScriptFrame.isShellMod()) {
                System.exit(0);
            }
        }
    }

    protected synchronized void submitManagedTasks() {
        ScriptFrame sc = ScriptFrame.getInstance();
        String submitMessage = Bundle.UI.getFormatedString(
                "message_submit_tasks", managedTasks.size());
        String successMessage = Bundle.UI.getFormatedString(
                "message_submit_tasks_success", managedTasks.size());
        sc.addInfoMessage(submitMessage);
        for (Iterator<AbstractTask> tasksIt = managedTasks.iterator(); tasksIt
                .hasNext();) {
            executor.submit(tasksIt.next());
        }
        sc.addInfoMessage(successMessage);
    }

    public void abortAllTasks() {
        executor.shutdownNow();
    }

    public void startThreads() {
        setStateInit();
        submitManagedTasks();
    }

    public void addTask(AbstractTask task) {
        task.addObserver(this);
        managedTasks.add(task);
    }

    public void addAllTask(AbstractTask... tasks) {
        for (AbstractTask t : tasks) {
            t.addObserver(this);
            managedTasks.add(t);
        }
    }
}
