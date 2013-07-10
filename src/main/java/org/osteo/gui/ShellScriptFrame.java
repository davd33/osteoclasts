package org.osteo.gui;

import org.osteo.gui.filters.ImagesFilter;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidr
 * Date: 7/3/13
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShellScriptFrame extends ScriptFrame {

    /**
     * Selected image files.
     */
    private File[] selectedFiles;

    /**
     * Selected result directory.
     */
    private File selectedDir;

    /**
     * Selected script.
     */
    private String selectedScript;

    /**
     * Get the selected script.
     *
     * @return a string value.
     */
    public String getSelectedScript() {
        return selectedScript;
    }

    /**
     * Choose an other script.
     *
     * @param selectedScript
     *            the chosen script
     */
    public void setSelectedScript(String selectedScript) {
        this.selectedScript = selectedScript;
    }

    /**
     * Choose a result directory.
     *
     * @param selectedDir
     */
    public void setSelectedDir(File selectedDir) {
        this.selectedDir = selectedDir;
    }

    /**
     * Set the chosen images.
     *
     * @param selectedFiles
     *            collection of File instances
     */
    public void setSelectedFiles(File... selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    /**
     * Public constructor setting the GUI JFrame unique instance to null.
     */
    public ShellScriptFrame() {
        frame = null;
    }

    /**
     * Override: just return the private attribute 'selectedFiles'.
     */
    @Override
    public File[] getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * Override: just return the private attribute 'selectedDir'.
     */
    @Override
    public File getSelectedDir() {
        return selectedDir;
    }

    @Override
    public File[] getImages() {
        List<File> files = new ArrayList<File>();
        FileFilter filter = new ImagesFilter();
        for (File f : selectedFiles) {
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

    @Override
    public String getScriptBoxSelectedItem() {
        return getSelectedScript();
    }

    @Override
    public void setResultDirChooserCurrent(File value) {
    }

    @Override
    public void setRunButtonVisible(boolean value) {
    }

    @Override
    public void setFileChooserFrameVisible(boolean value) {
    }

    @Override
    public void setResultDirChooserFrameVisible(boolean value) {
    }

    @Override
    public void setImagesTableModel(TableModel dataModel) {
    }

    @Override
    public void setImagesTableColumnCellRenderer(Object column,
                                                 TableCellRenderer renderer) {
    }

    @Override
    public void setImagesTableColumnCellEditor(Object column,
                                               TableCellEditor editor) {
    }

    @Override
    public void addErrorMessage(String message) {
        addMessage(message);
    }

    @Override
    public void addInfoMessage(String message) {
        addMessage(message);
    }

    @Override
    public void addSuccessMessage(String message) {
        addMessage(message);
    }

    private void addMessage(String message) {
        message = message.replaceAll("<br />", " | ");
        System.out.println(message);
    }

    @Override
    public void cleanMessages() {
        System.out.println("--------------");
    }

    @Override
    public void enableCommandsPanel() {
    }

    @Override
    public void disableCommandsPanel() {
    }

    @Override
    public void requestFocus() {
    }
}
