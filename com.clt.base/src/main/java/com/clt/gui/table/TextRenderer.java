package com.clt.gui.table;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class TextRenderer extends JLabel implements TableCellRenderer, ListCellRenderer {

    private static Border noFocusBorder = new EmptyBorder(1, 2, 1, 2);

    public TextRenderer() {

        this.setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row, int column) {

        JComponent c = this.getRendererComponent(table, value, isSelected, hasFocus);

        if (isSelected) {
            c.setForeground(table.getSelectionForeground());
            c.setBackground(table.getSelectionBackground());
        }

        return c;
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index,
            boolean isSelected, boolean cellHasFocus) {

        JComponent c = this.getRendererComponent(list, value, isSelected, cellHasFocus);

        if (isSelected) {
            c.setForeground(list.getSelectionForeground());
            c.setBackground(list.getSelectionBackground());
        }

        return c;
    }

    protected JComponent getRendererComponent(JComponent parent, Object value,
            boolean isSelected,
            boolean hasFocus) {

        this.setForeground(parent.getForeground());
        this.setBackground(parent.getBackground());

        this.setFont(parent.getFont());

        if (hasFocus) {
            this.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            this.setBorder(TextRenderer.noFocusBorder);
        }

        this.setText((value == null) ? "" : value.toString());

        return this;
    }
}
