package org.osteo.gui.tables;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.osteo.main.Bundle;

/**
 * Defines a personalized CellRenderer for the view button.
 *
 * @author David Rueda
 *
 */
public class ViewButtonRenderer extends JButton implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean isFocus, int row, int col) {

        setText(Bundle.UI.getString("imgtable_col_overview_buttontext"));

        return this;
    }
}
