package org.osteo.main;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.DefaultImageDisplay;
import imagej.io.DefaultIOService;
import imagej.io.IOService;
import imagej.ui.swing.sdi.viewer.SwingDisplayWindow;
import imagej.ui.swing.sdi.viewer.SwingSdiImageDisplayViewer;
import imagej.ui.swing.viewer.image.SwingDisplayPanel;
import imagej.ui.swing.viewer.image.SwingImageDisplayViewer;
import org.osteo.gui.ScriptFrame;
import org.osteo.gui.ShellScriptFrame;
import org.osteo.gui.listeners.ImagePlusPanelListener;
import org.osteo.scripts.imp.ApplyClassifierScript;
import org.osteo.scripts.util.Available;
import org.osteo.threads.ScriptPoolManager;
import org.osteo.threads.tasks.ScriptTask;
import org.scijava.Context;
import org.scijava.thread.ThreadService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for this software. It handles the shell commands mode and is able
 * to run the program with appropriate options.
 *
 * @author David Rueda
 */
public class App {

    /**
     * Interface resource bundle.
     */
    public static final Bundle UI = Bundle.UI;
    protected static ImageJ imagej;
    protected static IOService ioService;

    public static Context getIJContext() {
        return getImageJ().getContext();
    }

    public static ImageJ getImageJ() {
        if (imagej == null) {
            imagej = new ImageJ();
        }
        return imagej;
    }

    public static IOService getIoService() {
        if (ioService == null) {
            ioService = getImageJ().io();
        }
        return ioService;
    }

    /**
     * If we are in shell mode, runs the shell mode. Otherwise, runs the GUI.
     *
     * @param args shell input arguments
     */
    public static void main(String[] args) {
        if (false) {
            final ImageJ imagej = new ImageJ();
            final IOService ioService = imagej.io();
            try {
                final String path = args.length > 0 ? args[0] :
                    "/home/bioinf/davidr/Downloads/anaJune13/GSKexp.11.1.2012Don2/PROBFiles Don2exp.11.1.2012/M25+R5+100nM5541_10xA_PROB.TIF";
                final Dataset data = ioService.loadDataset(path);
                final DefaultImageDisplay display = new DefaultImageDisplay();
                display.setContext(ioService.getContext());
                display.display(data);

                final ThreadService threadService = imagej.get(ThreadService.class);
                threadService.queue(new Runnable() {

                    @Override
                    public void run() {
                        final SwingImageDisplayViewer displayViewer = new SwingSdiImageDisplayViewer();
                        displayViewer.setContext(imagej.getContext());
                        imagej.ui().addDisplayViewer(displayViewer);
                            final SwingDisplayWindow displayWindow = new SwingDisplayWindow();
                        displayViewer.view(displayWindow, display);
                        final SwingDisplayPanel displayPanel = displayViewer.getPanel();

                        final JFrame myFrame = new JFrame("My Frame");
                        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        myFrame.getContentPane().setLayout(new BorderLayout());
                        myFrame.getContentPane().add(displayPanel);
                        myFrame.pack();
                        myFrame.setVisible(true);
                    }
                });
            } catch (Exception e) {
                //
            }
        } else {
            if (args.length > 0) {
                ScriptFrame.setShellMod(true);
                ScriptFrame.start();
                if (!runShellMod(args)) {
                    StringBuilder descriptionsStr = new StringBuilder();
                    String[] descriptions = Available.descriptions();
                    for (String desc : descriptions) {
                        descriptionsStr.append("	");
                        descriptionsStr.append(desc);
                        descriptionsStr.append('\n');
                    }
                    log(UI.getFormatedString("main_shell_options",
                            descriptionsStr.substring(0)));
                }
            } else {
                ScriptFrame.setShellMod(false);
                ScriptFrame.start();
            }
        }
    }

    /**
     * If we are in shell mode: analyze the options the user chose and run the
     * script on the given images.
     *
     * @param args shell input arguments
     * @return true if the command did not contain syntax errors
     */
    private static boolean runShellMod(String[] args) {
        // scriptframe instance
        if (!ScriptFrame.isShellMod()) {
            return false;
        }
        if (args[0].equals("help")) {
            return false;
        }
        ShellScriptFrame scriptFrame = (ShellScriptFrame) ScriptFrame
                .getInstance();
        // get results directory
        Available scriptToRun = null;
        List<File> files = new ArrayList<File>();
        File resDir = null;
        int skipNext = 0;
        for (int a = 0; a < args.length; a++) {
            String arg = args[a];
            if (skipNext > 0) {
                skipNext--;
            } else {
                boolean scriptToRunFound = false;
                for (Available av : Available.values()) {
                    if (arg.matches(av.getShellPattern())) {
                        scriptToRun = av;
                        scriptToRunFound = true;
                        break;
                    }
                }
                if (!scriptToRunFound) {
                    if (arg.matches("^(-rd|--results-dir)$")) {
                        if (a < args.length - 1) {
                            resDir = new File(args[a + 1]);
                        }
                        skipNext = 1;
                    } else if (arg.matches("^(-nt|--num-threads)$")) {
                        if (a < args.length - 1) {
                            try {
                                ApplyClassifierScript.NUM_THREADS = Integer
                                        .valueOf(args[a + 1]);
                            } catch (NumberFormatException e) {
                                log(UI.getString("main_nthreads_int"));
                            }
                        }
                        skipNext = 1;
                    } else {
                        File f = new File(arg);
                        if (f.exists()) {
                            files.add(f);
                        } else {
                            log(UI.getFormatedString("message_file_not_found",
                                    f.getPath()));
                        }
                    }
                }
            }
        }

        if (files.isEmpty()) {
            log(UI.getString("main_args_no_files"));
            return false;
        }

        if (scriptToRun != null) {
            ScriptPoolManager spm = ScriptPoolManager.getInstance();
            scriptFrame.setSelectedFiles(files.toArray(new File[0]));
            if (resDir == null) {
                resDir = new File(System.getProperty("user.dir"));
            }
            scriptFrame.setSelectedDir(resDir);
            scriptFrame.setSelectedScript(scriptToRun.getName());
            log(UI.getFormatedString("main_resdir_selected", resDir.getPath()));
            log(UI.getFormatedString("main_script_to_use",
                    scriptToRun.getName()));
            for (File f : files) {
                if (f.exists()) {
                    spm.addTask(new ScriptTask(scriptFrame, f, resDir,
                            scriptToRun.getType()));
                }
            }
            spm.startThreads();
            return true;
        }
        return false;
    }

    /**
     * Add a log message whether you use the shell commands mode or the GUI.
     *
     * @param message a string value
     */
    public synchronized static void log(String message) {
        ScriptFrame.getInstance().addInfoMessage(message);
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.indexOf("win") >= 0;
    }
}
