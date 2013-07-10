package org.osteo.gui.tables;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JTable;

public class ViewNameEditor extends DefaultCellEditor {

    private static final long serialVersionUID = 1L;
    /**
     * The button instance from the image table.
     */
    private JProgressBar textField;

    public ViewNameEditor() {
        super(new JCheckBox());
        this.textField = new JProgressBar();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

        Object[] val = (Object[]) value;
        
        textField.setStringPainted(true); //TODO: progress bar under image name
        textField.setString(val[0].toString());
        textField.setValue((Integer)val[1]);

        return textField;
    }
}
