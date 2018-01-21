package com.clt.gui;

import java.awt.Component;
import java.awt.Window;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author dabo
 *
 */
public class ComponentRenderer implements ListCellRenderer, TableCellRenderer {

    private ListCellRenderer defaultListRenderer;
    private TableCellRenderer defaultTableCellRenderer;

    public ComponentRenderer() {

        this(new DefaultListCellRenderer(), new DefaultTableCellRenderer());
    }

    public ComponentRenderer(ListCellRenderer defaultRenderer) {

        this.defaultListRenderer = defaultRenderer;
    }

    public ComponentRenderer(TableCellRenderer defaultRenderer) {

        this.defaultTableCellRenderer = defaultRenderer;
    }

    public ComponentRenderer(ListCellRenderer defaultListRenderer, TableCellRenderer defaultTableCellRenderer) {

        this.defaultListRenderer = defaultListRenderer;
        this.defaultTableCellRenderer = defaultTableCellRenderer;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof JSeparator) {
            JComponent c = (JComponent) value;
            c.setEnabled(false);
            return c;
        } else if (value instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) value;
            item.setArmed(isSelected);
            item.setForeground(list.getForeground());
            item.setBackground(list.getBackground());
            return item;
        } else if ((value instanceof Component) && !(value instanceof Window)) {
            Component c = (Component) value;
            return c;
        } else {
            return this.defaultListRenderer.getListCellRendererComponent(list, value,
                    index, isSelected,
                    cellHasFocus);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof JSeparator) {
            JComponent c = (JComponent) value;
            c.setEnabled(false);
            return c;
        } else if (value instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) value;
            item.setArmed(isSelected);
            item.setForeground(table.getForeground());
            item.setBackground(table.getBackground());
            return item;
        } else if ((value instanceof Component) && !(value instanceof Window)) {
            Component c = (Component) value;
            return c;
        } else {
            return this.defaultTableCellRenderer.getTableCellRendererComponent(table,
                    value, isSelected,
                    hasFocus, row, column);
        }
    }
}
