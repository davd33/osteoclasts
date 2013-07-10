package org.osteo.gui.tables;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import org.osteo.gui.ScriptFrame;
import org.osteo.gui.listeners.ViewButtonActionListener;
import org.osteo.main.Bundle;

/**
 * Defines a personalized CellEditor for the view button.
 *
 * @author David Rueda
 *
 */
public class ViewButtonEditor extends DefaultCellEditor {

    private static final long serialVersionUID = 1L;
    /**
     * The button instance from the image table.
     */
    private JButton button;
    /**
     * A reference to the unique ScriptFrame instance.
     */
    private ScriptFrame scriptFrame;
    /**
     * A reference to the button action listener.
     */
    private ViewButtonActionListener actionListener;

    public ViewButtonEditor(ScriptFrame scriptFrame) {
        super(new JCheckBox());
        this.scriptFrame = scriptFrame;
        this.actionListener = new ViewButtonActionListener(this.scriptFrame);
        this.button = new JButton();
        this.button.setOpaque(true);
        this.button.addActionListener(this.actionListener);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

        button.setText(Bundle.UI.getString("imgtable_col_overview_buttontext"));

        if (value instanceof File) {
            actionListener.setImage((File) value);
        }

        return button;
    }
}
