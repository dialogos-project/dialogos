package com.clt.gui.table;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ChoiceEditor extends DefaultCellEditor implements TableCellRenderer {

    private static TextRenderer textRenderer = null;

    private boolean useComboBoxRenderer = false;

    public ChoiceEditor(Object[] choices) {

        super(new JComboBox(choices));
    }

    public ChoiceEditor(Collection<?> choices) {

        super(new JComboBox(new Vector<Object>(choices)));
    }

    public ChoiceEditor(Map<?, ?> choices) {

        super(new JComboBox(new Vector<Object>(choices.keySet())));
    }

    public void useComboBoxRenderer(boolean b) {

        this.useComboBoxRenderer = b;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row,
            int column) {

        if (this.useComboBoxRenderer) {
            JComboBox renderer = new JComboBox(new Object[]{value});
            renderer.setEditable(false);
            return renderer;
        } else {
            if (ChoiceEditor.textRenderer == null) {
                ChoiceEditor.textRenderer = new TextRenderer();
            }
            return ChoiceEditor.textRenderer.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus,
                    row, column);
        }
    }
}
