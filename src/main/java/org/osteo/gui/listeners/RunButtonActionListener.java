package org.osteo.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.SwingUtilities;

import org.osteo.gui.ScriptFrame;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.util.Available;
import org.osteo.threads.ScriptPoolManager;
import org.osteo.threads.tasks.AbstractTask;
import org.osteo.threads.tasks.ScriptTask;

/**
 * According to the chosen script and images... Enable the creation of
 * multi-threaded tasks to handle parallel execution of the script on the
 * different selected images.
 *
 * @author David Rueda
 *
 */
public class RunButtonActionListener extends ScriptFrameListener implements
        ActionListener {

    public RunButtonActionListener(ScriptFrame scriptFrame) {
        super(scriptFrame);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        scriptFrame.disableCommandsPanel();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                File[] images = scriptFrame.getImages();
                for (int f = 0; f < images.length; f++) {
                    File file = images[f];

                    if (!file.exists()) {
                        scriptFrame.addErrorMessage(Bundle.UI.getFormatedString(
                                "message_file_not_found", file.getAbsolutePath()));
                        break;
                    } else {
                        AbstractTask scriptTask = null;
                        File resultsDir = scriptFrame.getSelectedDir();
                        Object scriptType = scriptFrame.getScriptBoxSelectedItem();
                        for (String availableScript : Available.names()) {
                            String className = Available
                                    .className((String) availableScript);
                            Class<? extends AbstractScript> classe;
                            try {
                                classe = (Class<? extends AbstractScript>) Class
                                        .forName(className);
                                if (className.equals(scriptType)) {
                                    scriptTask = new ScriptTask(scriptFrame, file,
                                            resultsDir, classe);
                                    ((ScriptTask) scriptTask).getScript().setOptions(
                                            scriptFrame.getOptions());
                                }
                            } catch (ClassNotFoundException e1) {
                                System.out.println("Class not available!");
                            }
                        }
                        if (scriptTask != null) {
                            ScriptPoolManager.getInstance().addTask(scriptTask);
                        } else {
                            scriptFrame.addErrorMessage(Bundle.UI.getFormatedString(
                                    "message_cant_add_task", file.getName()));
                            return;
                        }
                    }
                }

                ScriptPoolManager.getInstance().startThreads();
                scriptFrame.cleanMessages();
                scriptFrame.addInfoMessage(Bundle.UI.getString("tasks_launched"));
                scriptFrame.saveScriptOptions();
            }
        });
    }
}
