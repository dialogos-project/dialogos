package com.clt.gui.table;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.clt.gui.CmdButton;
import com.clt.gui.Commander;
import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.StaticText;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class TableEditor<E> extends JPanel        implements Commander {

    private static final int cmdEdit = Commands.cmdApplication + 1;

    private ItemTableModel<E> items;

    private JTable table;
    private JButton deleteButton;
    private JButton editButton;

    public TableEditor(ItemTableModel<E> items, String info,
            boolean includeEditButton,
            JButton[] additionalButtons) {

        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        this.items = items;

        this.table = new JTable(items);
        this.table.getTableHeader().setReorderingAllowed(false);
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        this.table.setDefaultRenderer(String.class, new Renderer());

        this.table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    TableEditor.this.updateButtons();
                }
            }
        });

        for (int i = 0; i < items.getColumnCount(); i++) {
            int width = items.getColumnWidth(i);
            TableColumn column = this.table.getColumnModel().getColumn(i);
            if ((width >= 0) && (width < Integer.MAX_VALUE)) {
                column.setPreferredWidth(width);
                column.setMinWidth(column.getPreferredWidth());
                column.setMaxWidth(column.getPreferredWidth());
            } else {
                column.setMinWidth(60);
            }

            TableCellRenderer r = items.getColumnRenderer(i);
            if (r != null) {
                column.setCellRenderer(r);
            }
            TableCellEditor e = items.getColumnEditor(i);
            if (e != null) {
                column.setCellEditor(e);
            }

            if (items.isColumnDraggable(i)) {
                TableRowDragger.addDragHandler(this.table, i);
            }
        }

        JScrollPane jsp = GUI.createScrollPane(this.table, 200);

        jsp.setBorder(BorderFactory.createEmptyBorder());
        GUI.setupScrollBar(jsp.getVerticalScrollBar());
        GUI.setupScrollBar(jsp.getHorizontalScrollBar());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        if (info != null) {
            JComponent label = new StaticText(info);
            label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            this.add(label, gbc);
            gbc.gridy++;
        }

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        this.add(jsp, gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        this.add(new JSeparator(), gbc);

        JButton newButton
                = new CmdButton(this, Commands.cmdNew, GUI.getString("New"));
        this.deleteButton
                = new CmdButton(this, Commands.cmdDelete, GUI.getString("Delete"));
        this.editButton
                = new CmdButton(this, TableEditor.cmdEdit, GUI.getString("Edit"));

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton[] buttons
                = includeEditButton ? new JButton[]{newButton, this.editButton,
                    this.deleteButton}
                : new JButton[]{newButton, this.deleteButton};
        this.add(TableEditor.createButtonPanel(buttons, additionalButtons), gbc);

        this.updateButtons();
    }

    private void updateButtons() {

        int[] indices = this.table.getSelectedRows();
        this.deleteButton.setEnabled((indices != null) && (indices.length > 0));
        if (this.editButton != null) {
            this.editButton.setEnabled((indices != null) && (indices.length == 1));
        }
    }

    public boolean readyToClose() {

        if (this.table.isEditing() ? this.table.getCellEditor().stopCellEditing()
                : true) {
            ItemTableModel<?> model = (ItemTableModel<?>) this.table.getModel();
            return model.readyToClose(this);
        } else {
            return false;
        }
    }

    public boolean doCommand(int cmd) {

        switch (cmd) {
            case cmdNew:
                if (this.table.isEditing() ? this.table.getCellEditor()
                        .stopCellEditing() : true) {
                    E newItem = this.items.createNewItem(this);
                    if (newItem != null) {
                        this.items.add(newItem);
                        this.table.setRowSelectionInterval(this.table.getRowCount() - 1,
                                this.table.getRowCount() - 1);
                    }
                }
                break;
            case cmdDelete:
                if (this.table.isEditing() ? this.table.getCellEditor()
                        .stopCellEditing() : true) {
                    if (this.items.confirmDelete(this, this.table.getSelectedRows())) {
                        this.items.removeElements(this.table.getSelectedRows());

                        this.table.clearSelection();
                        this.updateButtons();
                        this.table.requestFocus();
                    }
                }
                break;

            case cmdEdit:
                this.items.editItemAt(this, this.table.getSelectedRow());
                break;

            default:
                return false;
        }
        return true;
    }

    public static JPanel createButtonPanel(JButton[] left, JButton[] right) {

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.insets = new Insets(6, 12, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;

        Dimension d_left = new Dimension(0, 0);
        for (int i = 0; i < left.length; i++) {
            p.add(left[i], gbc);
            d_left.width = Math.max(d_left.width, left[i].getPreferredSize().width);
            d_left.height
                    = Math.max(d_left.height, left[i].getPreferredSize().height);
            gbc.gridx++;
        }

        for (int i = 0; i < left.length; i++) {
            left[i].setPreferredSize(d_left);
        }

        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 12);
        p.add(Box.createHorizontalGlue(), gbc);

        if (right != null) {
            gbc.gridx++;
            gbc.weightx = 0.0;
            Dimension d_right = new Dimension(0, 0);
            for (int i = 0; i < right.length; i++) {
                p.add(right[i], gbc);
                d_right.width
                        = Math.max(d_right.width, right[i].getPreferredSize().width);
                d_right.height
                        = Math.max(d_right.height, right[i].getPreferredSize().height);
                gbc.gridx++;
            }

            for (int i = 0; i < right.length; i++) {
                right[i].setPreferredSize(d_right);
            }
        }

        return p;
    }

    public static class Renderer
            extends TextRenderer {

    }
}
