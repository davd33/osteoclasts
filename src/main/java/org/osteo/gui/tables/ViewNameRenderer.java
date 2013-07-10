package org.osteo.gui.tables;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.osteo.gui.ScriptFrame;
import org.osteo.scripts.ScriptFile;
import org.osteo.scripts.ScriptFileCollection;

public class ViewNameRenderer extends JButton implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean isFocus, int row, int col) {

        if (value instanceof File) {
            ScriptFileCollection processedFiles = ScriptFrame.getInstance().getProcessedFiles();
            File file = (File) value;
            if (processedFiles != null && processedFiles.contains(file)) {
                ScriptFile scFile = processedFiles.get(file);
                if (scFile.is(ScriptFile.Status.PROCESSED)) {
                    setBackground(Color.GREEN);
                } else if (scFile.is(ScriptFile.Status.ERROR)) {
                    setBackground(Color.RED);
                } else if (scFile.is(ScriptFile.Status.IN_PROGRESS)) {
                    setBackground(Color.ORANGE);
                } else {
                    setBackground(Color.GRAY);
                }
            }
        }

        setFocusable(false);
        setText(value.toString());

        return this;
    }
}
