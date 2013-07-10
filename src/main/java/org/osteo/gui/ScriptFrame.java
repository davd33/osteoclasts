package org.osteo.gui;

import ij.IJ;
import ij.ImagePlus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.iharder.dnd.FileDrop;

import org.osteo.gui.listeners.DialogWindowListener;
import org.osteo.gui.listeners.FileChooserActionListener;
import org.osteo.gui.listeners.FileChooserButtonActionListener;
import org.osteo.gui.listeners.ResultsFileChooserActionListener;
import org.osteo.gui.listeners.ResultsFileChooserButtonActionListener;
import org.osteo.gui.listeners.RunButtonActionListener;
import org.osteo.gui.listeners.ScriptBoxActionListener;
import org.osteo.gui.listeners.StopButtonActionListener;
import org.osteo.gui.tables.ViewButtonEditor;
import org.osteo.gui.tables.ViewButtonRenderer;
import org.osteo.gui.tables.ViewNameRenderer;
import org.osteo.main.App;
import org.osteo.main.Bundle;
import org.osteo.scripts.AbstractScript;
import org.osteo.scripts.ScriptFileCollection;
import org.osteo.scripts.util.Available;
import org.osteo.scripts.util.Option;
import org.osteo.scripts.util.OptionSet;

/**
 *
 * Principal GUI Class. If the program is run as a shell command, the
 * getInstance() method will return a new ShellScriptFrame object.
 *
 * @author David Rueda
 *
 */
public class ScriptFrame implements MouseListener {

    /**
     * Width value for the main window.
     */
    private final int WIDTH = 1000;
    /**
     * Height value for the main window.
     */
    private final int HEIGHT = 800;
    /**
     * Unique instance of this class.
     */
    private static ScriptFrame instance;
    /**
     * If false, the GUI will be used.
     */
    protected static boolean shellMod = false;
    /**
     * Swing JFrame instance.
     */
    protected JFrame frame;
    /**
     * Mouse location in the screen, needed to place new JDialog windows.
     */
    private Point mouseLocation;

    /**
     * Is the process run in shell mod?
     *
     * @return true if it's in shell mod.
     */
    public static boolean isShellMod() {
        return shellMod;
    }

    /**
     * Switching the executed program into shell mod or GUI mod. Used preferably
     * in the main class when running.
     *
     * @param shellMod true if shell mod is chosen.
     */
    public static void setShellMod(boolean shellMod) {
        ScriptFrame.instance = null;
        ScriptFrame.shellMod = shellMod;
    }

    /**
     * Create the new and unique instance of ScriptFrame.
     */
    public static void start() {
        ScriptFrame.getInstance();
    }

    /**
     * Get unique instance of ScriptFrame.
     *
     * @return ScriptFrame instance.
     */
    public static ScriptFrame getInstance() {
        if (instance == null) {
            synchronized (ScriptFrame.class) {
                if (instance == null) {
                    instance = shellMod ? new ShellScriptFrame() : new ScriptFrame();
                }
            }
        }
        return instance;
    }
    /**
     * Chosen files with they current status.
     */
    protected ScriptFileCollection processedFiles;
    /**
     * Panel containing the messages and script status informations.
     */
    private JPanel southPanel;
    /**
     * Files, Results directory, Script selection. Plus Run command.
     */
    private JPanel commandsPanel;
    /**
     * This panel contain the working images.
     */
    private JPanel imagesPanel;
    /**
     * Panel for dedicated options of the chosen script.
     */
    private JPanel additionalOptionsPanel;
    /**
     * Displaying message generated by the run script.
     */
    private JEditorPane messagesPanel;
    /**
     * Display the JDialog of JFileChooser associated to the images to be
     * processed.
     */
    private JButton fileChooserButton;
    /**
     * Run the script.
     */
    private JButton runButton;
    /**
     * Stop the script and all the threads.
     */
    private JButton stopButton;
    /**
     * Open JDialog for selecting a directory where the results will be saved.
     */
    private JButton resultDirButton;
    /**
     * Window for images JFileChooser.
     */
    private JDialog fileChooserFrame;
    /**
     * Window for result directory JFileChooser.
     */
    private JDialog resultDirChooserFrame;
    /**
     * Images JFileChooser.
     */
    private JFileChooser fileChooser;
    /**
     * Result directory JFileChooser.
     */
    private JFileChooser resultDirChooser;
    /**
     * JTable for displaying the selected images.
     */
    private JTable imagesTable;
    /**
     * options / current image / images list
     */
    private JSplitPane splitPane;
    /**
     * Display all the script available in org.osteo.script.imp package.
     */
    private JComboBox scriptBox;
    /**
     * Scrolling in case of multiple images...
     */
    private JScrollPane imagesScrollPane;
    /**
     * Multiple script messages Scroll pane.
     */
    private JScrollPane messagesScrollPane;
    /**
     * Scrolling options panel.
     */
    private JScrollPane optionsScroll;
    /**
     * Script status.
     */
    private JLabel scriptStatus;
    /**
     * Options for the script together with chosen values.
     */
    private OptionSet options;
    /**
     * Current image to display.
     */
    private ImagePlusPanel ipPanel;

    public ImagePlusPanel getIpPanel() {
        if (ipPanel == null) {
            ipPanel = new ImagePlusPanel();
            ipPanel.setMinimumSize(new Dimension(200, -1));
        }
        return ipPanel;
    }

    /**
     * Get options for the script together with chosen values.
     *
     * @return the script options values.
     */
    public OptionSet getOptions() {
        return options;
    }

    public ScriptFileCollection getProcessedFiles() {
        return processedFiles;
    }

    public void setProcessedFiles(ScriptFileCollection processedFiles) {
        this.processedFiles = processedFiles;
    }

    /**
     * It is useful to get a new instance of the chosen script in order to get
     * informations about the available options and default values.
     *
     * @return an AbstractScript instance of the selected script.
     */
    protected AbstractScript getAbstractScript() {
        try {
            return (AbstractScript) Class.forName(getScriptBoxSelectedItem())
                    .newInstance();
        } catch (InstantiationException e) {
            System.err.println("InstantiationException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtain the selected images.
     *
     * @return a list of File instances from the JFileChooser selected files.
     */
    public File[] getImages() {
        List<File> files = new ArrayList<File>();
        FileFilter filter = fileChooser.getFileFilter();
        for (File f : this.fileChooser.getSelectedFiles()) {
            if (f.isDirectory()) {
                for (File sf : f.listFiles()) {
                    if (!sf.isDirectory() && filter.accept(sf)) {
                        files.add(sf);
                    }
                }
            } else if (filter.accept(f)) {
                files.add(f);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    /**
     * Protected constructor doing nothing when shell mod is set to true.
     */
    protected ScriptFrame() {
        if (isShellMod()) {
            return;
        }

        frame = new JFrame();
        frame.setSize(new Dimension(WIDTH, HEIGHT));
        frame.setVisible(true);
        frame.setTitle("Images Particle Analyser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //TODO: bug maximized window

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            App.log(e.getMessage());
        } catch (InstantiationException e) {
            App.log(e.getMessage());
        } catch (IllegalAccessException e) {
            App.log(e.getMessage());
        } catch (UnsupportedLookAndFeelException e) {
            App.log(e.getMessage());
        }

        resultDirChooserFrame = new JDialog();
        resultDirChooserFrame.setModal(true);
        resultDirChooserFrame.setTitle("Choose the results directory");
        resultDirChooserFrame.setLocationRelativeTo(frame);
        resultDirChooserFrame.addWindowListener(new DialogWindowListener(this));
        resultDirChooser = new JFileChooser();
        resultDirChooser.setMultiSelectionEnabled(false);
        resultDirChooser
                .addActionListener(new ResultsFileChooserActionListener(this));
        resultDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        resultDirChooserFrame.add(resultDirChooser);
        resultDirChooserFrame.pack();

        fileChooserButton = new JButton("Choose images");
        fileChooserButton
                .addActionListener(new FileChooserButtonActionListener(this));
        resultDirButton = new JButton("Choose results folder");
        resultDirButton
                .addActionListener(new ResultsFileChooserButtonActionListener(
                this));
        runButton = new JButton("Run");
        runButton.setVisible(false);
        runButton.addActionListener(new RunButtonActionListener(this));
        stopButton = new JButton("Stop");
        stopButton.setVisible(false);
        stopButton.addActionListener(new StopButtonActionListener(this));

        imagesTable = new JTable();
        imagesTable.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        List<String> avNames = new ArrayList();
        for (String str : Available.names()) {
            avNames.add(str);
        }
        Collections.sort(avNames);
        ComboBoxModel scriptBoxModel = new DefaultComboBoxModel(
                avNames.toArray(new String[0]));
        scriptBox = new JComboBox(scriptBoxModel);
        scriptBox.addActionListener(new ScriptBoxActionListener(this));

        fileChooserFrame = new JDialog();
        fileChooserFrame.setModal(true);
        fileChooserFrame.setTitle("Choose images files or directories");
        fileChooserFrame.setLocationRelativeTo(frame);
        fileChooserFrame.addWindowListener(new DialogWindowListener(this));
        fileChooser = new JFileChooser();
        FileFilter filter = getAbstractScript().getFileFilter();
        fileChooser.setFileFilter(filter);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addActionListener(new FileChooserActionListener(this));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooserFrame.add(fileChooser);
        fileChooserFrame.pack();

        imagesScrollPane = new JScrollPane();
        imagesScrollPane.setViewportView(imagesTable);

        commandsPanel = new JPanel();
        commandsPanel.add(fileChooserButton);
        commandsPanel.add(resultDirButton);
        commandsPanel.add(scriptBox);
        commandsPanel.add(runButton);
        commandsPanel.add(stopButton);
        imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));
        imagesPanel.add(imagesScrollPane);
        JSplitPane imgTabAndView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getIpPanel(), imagesPanel);
        imgTabAndView.setContinuousLayout(true);
        imgTabAndView.setDividerLocation(0.80);
        scriptStatus = new JLabel();
        messagesPanel = new JEditorPane();
        HTMLEditorKit editor = new HTMLEditorKit();
        HTMLDocument document = (HTMLDocument) editor.createDefaultDocument();
        messagesPanel.setEditorKit(editor);
        messagesPanel.setDocument(document);
        messagesPanel.setContentType("text/html");
        messagesPanel.setBorder(BorderFactory.createEtchedBorder(1));
        messagesPanel.setBackground(Color.WHITE);
        messagesPanel.setEditable(false);
        messagesScrollPane = new JScrollPane();
        messagesScrollPane.setViewportView(messagesPanel);
        messagesScrollPane.setPreferredSize(new Dimension(200, 100));
        southPanel = new JPanel(new BorderLayout());
        southPanel.add(messagesScrollPane, BorderLayout.CENTER);
        southPanel.add(scriptStatus, BorderLayout.SOUTH);

        Image icon = getImageResource("icon32.png");
        frame.setIconImage(icon);

        GridBagLayout addOptPLayout = new GridBagLayout();
        additionalOptionsPanel = new JPanel(addOptPLayout);
        optionsScroll = new JScrollPane();
        optionsScroll.getVerticalScrollBar().setUnitIncrement(10);
        optionsScroll.setViewportView(additionalOptionsPanel);
        optionsScroll.setMinimumSize(new Dimension(400, 300));
        optionsScroll.setVisible(false);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                optionsScroll, imgTabAndView);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(0.80);

        this.updateScriptOptions(true);

        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                splitPane, southPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(0.80);

        JPanel southPanelLocal = new JPanel(new BorderLayout());
        southPanelLocal.add(splitPane2, BorderLayout.CENTER);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(commandsPanel, BorderLayout.NORTH);
        frame.getContentPane().add(southPanelLocal, BorderLayout.CENTER);

        mouseLocation = new Point(0, 0);
        fileChooserButton.addMouseListener(this);
        resultDirButton.addMouseListener(this);

        new FileDrop(this.imagesPanel, new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] droppedFiles) {
                List<File> acceptedFiles = new ArrayList<File>();
                for (int i = 0; i < droppedFiles.length; i++) {
                    File f = droppedFiles[i];
                    if (fileChooser.getFileFilter().accept(f)) {
                        acceptedFiles.add(f);
                    }
                }
                ScriptFrame.this.fileChooser.setSelectedFiles(acceptedFiles
                        .toArray(new File[0]));

                new FileChooserActionListener(ScriptFrame.this)
                        .actionPerformed(new ActionEvent(ScriptFrame.this, 2,
                        "drop"));
            }
        });

        addInfoMessage("Please choose one or many files");
        frame.setSize(WIDTH, HEIGHT);
    }

    /**
     * Add a message to the messages panel.
     *
     * @param message a String value
     * @param color a color for the message
     */
    private void addMessage(final String message, final Color color) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // update files panel
                ScriptFrame.getInstance().updateImageTable(false);

                // add message in message panel
                JEditorPane pane = ScriptFrame.this.messagesPanel;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                try {
                    String c = Integer.toHexString(color.getRGB());
                    c = c.substring(2, c.length());
                    Element root = doc.getDefaultRootElement();
                    Element body = root.getElement(root.getElementIndex(1));
                    doc.insertBeforeEnd(
                            body.getElement(root.getElementIndex(0)),
                            "<span style=\"color: #" + c + "\">" + message
                            + "<br /></span>");
                } catch (BadLocationException e) {
                } catch (IOException e) {
                }
            }
        });
    }

    /**
     * Add an error message (with red color).
     *
     * @param message a string value
     */
    public void addErrorMessage(String message) {
        addMessage(message, new Color(170, 55, 55));
    }

    /**
     * Add an info message (blue color).
     *
     * @param message a string value
     */
    public void addInfoMessage(String message) {
        addMessage(message, new Color(0, 102, 204));
    }

    /**
     * Add a success message (green color).
     *
     * @param message a string value
     */
    public void addSuccessMessage(String message) {
        addMessage(message, new Color(110, 173, 128));
    }

    /**
     * Put a horizontal line to separate messages and improve visibility.
     */
    public void cleanMessages() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JEditorPane pane = ScriptFrame.this.messagesPanel;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                try {
                    Element root = doc.getDefaultRootElement();
                    Element body = root.getElement(root.getElementIndex(1));
                    doc.insertBeforeEnd(
                            body.getElement(root.getElementIndex(0)), "<hr />");
                } catch (BadLocationException e) {
                } catch (IOException e) {
                }
            }
        });
    }
    private OptionSet previousOptions = null;
    private String previousScName = null;
    private Map<String, JLabel> optionInfoFields;
    private AbstractScript script;

    public AbstractScript getScript() {
        if (script == null) {
            script = getAbstractScript();
        }
        return script;
    }

    public void setScript(AbstractScript script) {
        this.script = script;
    }

    public Map<String, JLabel> getOptionInfoFields() {
        if (optionInfoFields == null) {
            optionInfoFields = new HashMap<String, JLabel>();
        }
        return optionInfoFields;
    }

    public void updateOptionInfo(String optionName, String value) {
        JLabel label = getOptionInfoFields().get(optionName);
        if (label != null) {
            label.setText(value);
            additionalOptionsPanel.updateUI();
        }
    }

    public String getPreviousScName() {
        return previousScName;
    }

    public void setPreviousScName(String previousScName) {
        this.previousScName = previousScName;
    }

    public OptionSet getPreviousOptions() {
        return previousOptions;
    }

    public void setPreviousOptions(OptionSet previousOptions) {
        this.previousOptions = previousOptions;
    }

    /**
     * Update additionalOptionsPanel to fit with the available options that the
     * selected script provides.
     */
    public void updateScriptOptions(final Boolean loadOptions) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ScriptFrame.this.options = new OptionSet();
                final AbstractScript script = getScript();
                if (loadOptions) {
                    script.loadOptions();
                    script.updateOptions();
                }
                additionalOptionsPanel.removeAll();
                additionalOptionsPanel.updateUI();

                // add adapted inputs for the selected script
                OptionSet available = script.getOptions().isEmpty() ? script
                        .availableOptions(getPreviousOptions(), getPreviousScName()) : script.getOptions();

                setPreviousOptions(available);
                script.setOptions(available);
                setPreviousScName(script.name());

                if (available != null && available.size() > 0) {
                    optionsScroll.setVisible(true);
                } else {
                    optionsScroll.setVisible(false);
                }
                splitPane.updateUI(); //TODO: update ui first time

                if (available != null) {
                    for (Iterator<Option> optIt = available.iterator(); optIt
                            .hasNext();) {

                        final Option opt = optIt.next();

                        JPanel optionPanel = new JPanel(new BorderLayout());
                        optionPanel.add(new JLabel(opt.isNameDisplayed() ? opt.getName() + ":" : ""),
                                BorderLayout.WEST);

                        if (opt.getPossibleValues() != null
                                && !opt.getPossibleValues().isEmpty()) {
                            String[] possV = opt.getPossibleValues().toArray(
                                    new String[0]);
                            int selectedIndex = 0;
                            ComboBoxModel values = new DefaultComboBoxModel(
                                    possV);
                            final JComboBox valuesComboBox = new JComboBox(
                                    values);
                            for (int i = 0; i < possV.length; i++) {
                                String v = possV[i];
                                if (v.equals(opt.getCurrentValue())) {
                                    selectedIndex = i;
                                }
                            }
                            valuesComboBox.setSelectedIndex(selectedIndex);
                            valuesComboBox.addItemListener(new ItemListener() {
                                @Override
                                public void itemStateChanged(ItemEvent e) {
                                    opt.setCurrentValue((String) valuesComboBox
                                            .getSelectedItem());
                                }
                            });
                            JPanel panel = new JPanel();
                            panel.add(valuesComboBox);
                            optionPanel.add(panel, BorderLayout.EAST);
                        } else if (opt.getOptionType().equals(
                                Option.Type.CRITICAL_INFO)) {
                            JPanel panel = new JPanel();
                            final JLabel text = new JLabel(opt
                                    .getCurrentValue());
                            text.setForeground(Color.red);
                            getOptionInfoFields().put(opt.getName(), text);
                            panel.add(text);
                            optionPanel.add(panel, BorderLayout.EAST);
                        } else if (opt.getOptionType().equals(
                                Option.Type.STRING)) {
                            JPanel panel = new JPanel();
                            final JTextField text = new JTextField(opt
                                    .getCurrentValue());
                            text.setColumns(5);
                            text.addFocusListener(new FocusListener() {
                                @Override
                                public void focusGained(FocusEvent fe) {
                                    text.selectAll();
                                }

                                @Override
                                public void focusLost(FocusEvent fe) {
                                }
                            });
                            text.addKeyListener(new KeyListener() {
                                @Override
                                public void keyTyped(KeyEvent e) {
                                }

                                @Override
                                public void keyReleased(KeyEvent e) {
                                    opt.setCurrentValue(text.getText());
                                    getScript().updatedValue(getScript().getOptions(), opt);
                                }

                                @Override
                                public void keyPressed(KeyEvent e) {
                                }
                            });
                            panel.add(text);
                            optionPanel.add(panel, BorderLayout.EAST);
                        } else if (opt.getOptionType().equals(Option.Type.CHECK)) {
                            final JCheckBox checkbox = new JCheckBox();
                            checkbox.setSelected(opt.isSelected());

                            checkbox.addMouseListener(new MouseListener() {
                                @Override
                                public void mouseClicked(MouseEvent me) {
                                    opt.setSelected(checkbox.isSelected());
                                    setPreviousOptions(getScript().getOptions());
                                    getScript().clearOptions();
                                    updateScriptOptions(false);
                                }

                                @Override
                                public void mousePressed(MouseEvent me) {
                                }

                                @Override
                                public void mouseReleased(MouseEvent me) {
                                }

                                @Override
                                public void mouseEntered(MouseEvent me) {
                                }

                                @Override
                                public void mouseExited(MouseEvent me) {
                                }
                            });
                            optionPanel.add(checkbox, BorderLayout.EAST);
                        } else if (opt.getOptionType().equals(Option.Type.FILE)) {
                            final JTextField text = new JTextField(opt
                                    .getCurrentValue());
                            text.setEditable(false);
                            File f = new File(opt.getCurrentValue());
                            if (f.exists()) {
                                text.setForeground(Color.GREEN);
                            } else {
                                text.setForeground(Color.RED);
                            }
                            JButton button = new JButton("...");

                            button.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    final JDialog chFrame = new JDialog();
                                    chFrame.setSize(480, 500);
                                    chFrame.setModal(true);
                                    chFrame.setTitle("Choose image files or directories");
                                    chFrame.setLocationRelativeTo(frame);

                                    final JFileChooser ch = new JFileChooser();
                                    ch.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                                    ch.addMouseListener(ScriptFrame.this);
                                    ch.setLocation(mouseLocation);
                                    ch.setVisible(true);
                                    ch.setFileFilter((opt.getFileFilter() == null) ? null
                                            : opt.getFileFilter());
                                    ch.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(
                                                ActionEvent e) {

                                            if (e.getActionCommand().equals(
                                                    "ApproveSelection")) {

                                                if (ch.getSelectedFile()
                                                        .exists()) {
                                                    text.setForeground(Color.GREEN);
                                                } else {
                                                    text.setForeground(Color.RED);
                                                }

                                                text.setText(ch
                                                        .getSelectedFile()
                                                        .getAbsolutePath());

                                                opt.setCurrentValue(text
                                                        .getText());
                                            }
                                            chFrame.setVisible(false);
                                        }
                                    });

                                    chFrame.add(ch);
                                    chFrame.setVisible(true);
                                    chFrame.pack();
                                }
                            });

                            JPanel fileP = new JPanel();
                            fileP.add(text);
                            fileP.add(button);
                            optionPanel.add(fileP, BorderLayout.EAST);
                        }

                        options = available;
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridwidth = GridBagConstraints.REMAINDER;
                        gbc.anchor = GridBagConstraints.NORTHEAST;
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        additionalOptionsPanel.add(optionPanel, gbc);
                    }

                    additionalOptionsPanel.updateUI();
                }
            }
        });
    }

    /**
     * Open an image from 'img' directory.
     *
     * @param name name of image
     * @return an Image object.
     */
    public static Image getImageResource(String name) {
        ImagePlus img = IJ.openImage("img" + File.separator + name);
        return img != null ? img.getImage() : null;
    }

    /**
     * Open an image from 'img' directory.
     *
     * @param name name of image
     * @return an ImageIcon object.
     */
    public static ImageIcon getIconResource(String name) {
        ImagePlus img = IJ.openImage("img" + File.separator + name);
        return img != null ? new ImageIcon(getImageResource(name)) : null;
    }

    /**
     * Enables all the components in the commands panel.
     */
    public void enableCommandsPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Component c : commandsPanel.getComponents()) {
                    c.setEnabled(true);
                }
                stopButton.setVisible(false);
                runButton.setVisible(true);
            }
        });
    }

    /**
     * Disable all the components in the commands panel.
     */
    public void disableCommandsPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Component c : commandsPanel.getComponents()) {
                    c.setEnabled(false);
                }
                stopButton.setEnabled(true);
                stopButton.setVisible(true);
                runButton.setVisible(false);
            }
        });
    }

    /**
     * Request focus of the main window (useful for Windows).
     */
    public void requestFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.requestFocus();
            }
        });
    }

    /**
     * Get selected result directory.
     *
     * @return a File.
     */
    public File getSelectedDir() {
        return resultDirChooser.getSelectedFile();
    }

    /**
     * Get selected images from the fileChooser instance.
     *
     * @return a list of selected images.
     */
    public File[] getSelectedFiles() {
        return fileChooser.getSelectedFiles();
    }

    /**
     * Get the class name of the chosen script for the next run.
     *
     * @return an object whose type should be an String.
     */
    public String getScriptBoxSelectedItem() {
        return Available.className((String) scriptBox.getSelectedItem());
    }

    /**
     * Set script status.
     *
     * @param value a string value.
     */
    public void setStatus(final String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ScriptFrame.this.scriptStatus.setText(value);
            }
        });
    }

    /**
     * Setting the result directory. Should be called internally. For example
     * when images are chosen, the result directory will be automatically set to
     * the containing directory of selected images.
     *
     * @param value a directory File
     */
    public void setResultDirChooserCurrent(File value) {
        if (!value.isDirectory()) {
            value = value.getParentFile();
        }

        resultDirChooser.setCurrentDirectory(value);
        resultDirChooser.setSelectedFile(value);
    }

    /**
     * Set the 'Run' button visible or not.
     *
     * @param value true for visible
     */
    public void setRunButtonVisible(boolean value) {
        runButton.setVisible(value);
    }

    /**
     * Display JDialog of images file chooser.
     *
     * @param value true for visible
     */
    public void setFileChooserFrameVisible(boolean value) {
        fileChooserFrame.setLocation(mouseLocation);
        fileChooserFrame.setVisible(value);
    }

    /**
     * Display JDialog of results directory file chooser.
     *
     * @param value
     */
    public void setResultDirChooserFrameVisible(boolean value) {
        resultDirChooserFrame.setLocation(mouseLocation);
        resultDirChooserFrame.setVisible(value);
    }

    /**
     * Set the data model of images table.
     *
     * @param dataModel contain a list of images (names)
     */
    public void setImagesTableModel(TableModel dataModel) {
        imagesTable.setModel(dataModel);
    }

    /**
     * New renderer for a given column of the images table.
     *
     * @param column for example name
     * @param renderer personalized renderer (org.osteo.gui.tables)
     */
    public void setImagesTableColumnCellRenderer(Object column,
            TableCellRenderer renderer) {
        imagesTable.getColumn(column).setCellRenderer(renderer);
    }

    public void updateImageTable(boolean displayMessages) {
        if (this.getSelectedFiles().length > 0) {
            File[] imgs = this.getImages();
            this.getIpPanel().update(imgs[0].getAbsoluteFile());
            Object[][] data = new Object[imgs.length][];
            for (int f = 0; f < imgs.length; f++) {
                File img = imgs[f];
                data[f] = new Object[]{img.getName(), img};
            }

            ScriptFileCollection processedFilesLocal = new ScriptFileCollection(imgs);
            this.setProcessedFiles(processedFilesLocal);
            this.updateImageTable(data);

            if (displayMessages) {
                this.addInfoMessage(Bundle.UI.getFormatedString(
                        "message_images_selected", imgs.length));
            }
        } else {
            this.updateImageTable(new Object[0][0]);
        }
    }

    public void updateImageTable(Object[][] imgNames) {
        String[] heads = {Bundle.UI.getString("imgtable_col_names"),
            Bundle.UI.getString("imgtable_col_overview")};

        this.setImagesTableModel(new DefaultTableModel(imgNames, heads));
        this.setImagesTableColumnCellRenderer(
                Bundle.UI.getString("imgtable_col_overview"),
                new ViewButtonRenderer());
        this.setImagesTableColumnCellEditor(
                Bundle.UI.getString("imgtable_col_overview"),
                new ViewButtonEditor(this));
        this.setImagesTableColumnCellRenderer(
                Bundle.UI.getString("imgtable_col_names"),
                new ViewNameRenderer());
    }

    /**
     * New renderer for a given column of the images table.
     *
     * @param column for example name
     * @param editor personalized editor (org.osteo.gui.tables)
     */
    public void setImagesTableColumnCellEditor(Object column,
            TableCellEditor editor) {
        imagesTable.getColumn(column).setCellEditor(editor);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Useful to place a new Window.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        mouseLocation = new Point(e.getX() + frame.getX(), e.getY()
                + frame.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public void updateFilter() {
        fileChooser.setFileFilter(getAbstractScript().getFileFilter());
    }

    public void updateSelectedFiles() {
        List<File> fList = new ArrayList<File>();
        File[] selFiles = fileChooser.getSelectedFiles();
        for (int i = 0; i < selFiles.length; i++) {
            File f = selFiles[i];
            if (fileChooser.getFileFilter().accept(f)) {
                fList.add(f);
            }
        }
        fileChooser.setSelectedFiles(fList.toArray(new File[0]));
        // System.out.println(fileChooser.getSelectedFiles().length);

        new FileChooserActionListener(ScriptFrame.this)
                .actionPerformed(new ActionEvent(ScriptFrame.this, 2, "update"));
    }

    public void saveScriptOptions() {
        AbstractScript script = getAbstractScript();
        script.setOptions(getOptions());
        script.saveOptions();
    }
}
