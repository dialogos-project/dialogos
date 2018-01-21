package com.clt.gui.table;

import java.awt.Component;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.clt.util.Misc;

/**
 * @author dabo
 *
 */
public abstract class ItemTableModel<E> extends AbstractTableModel        implements MovableRowsTableModel {

    private List<E> items;

    public ItemTableModel(List<E> items) {

        this.items = items;
    }

    public final int getRowCount() {

        if (this.items != null) {
            return this.items.size();
        }
        return 0;
    }

    public final int size() {

        return this.getRowCount();
    }

    public final int numItems() {

        return this.getRowCount();
    }

    public void add(E item) {
        this.items.add(item);
        this.fireTableRowsInserted(this.items.size() - 1, this.items.size() - 1);
    }

    public void removeElements(int indices[]) {

        Misc.removeElements(this.items, indices);
        this.fireTableDataChanged();
    }

    protected abstract E createNewItem(Component parent);

    protected void editItemAt(Component parent, int index) {

        this.editItem(parent, this.items.get(index));
    }

    /**
     * Override to edit the whole item. This method is called when the Edit
     * button is pressed.
     */
    protected void editItem(Component parent, E o) {

    }

    protected boolean readyToClose(Component parent) {

        return true;
    }

    /**
     * Optionally ask the user for confirmation, and then return whether it is
     * ok to delete selected items
     */
    protected boolean confirmDelete(Component parent, int[] indices) {

        return true;
    }

    public E getItem(int rowIndex) {

        return this.items.get(rowIndex);
    }

    public abstract int getColumnCount();

    @Override
    public abstract String getColumnName(int columnIndex);

    @Override
    public abstract Class<?> getColumnClass(int columnIndex);

    public int getColumnWidth(int columnIndex) {

        return -1;
    }

    public TableCellRenderer getColumnRenderer(int columnIndex) {

        return null;
    }

    public TableCellEditor getColumnEditor(int columnIndex) {

        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {

        return true;
    }

    /**
     * return whether a row drag handler should be installed on this column
     */
    public boolean isColumnDraggable(int columnIndex) {

        return false;
    }

    /**
     * return whether this row may be dragged to another position
     */
    public boolean isRowMovable(int row) {

        return true;
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {

        return this.getValue(this.items.get(rowIndex), columnIndex);
    }

    protected abstract Object getValue(E item, int columnIndex);

    @Override
    public final void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        this.setValue(this.items.get(rowIndex), columnIndex, aValue);
    }

    protected abstract void setValue(E item, int columnIndex, Object value);

    public boolean moveRow(int column, int from, int to) {

        if (from == to) {
            return false;
        }

        E o = this.items.get(from);
        if (from < to) {
            for (int i = from; i < to; i++) {
                this.items.set(i, this.items.get(i + 1));
            }
        } else {
            for (int i = from; i > to; i--) {
                this.items.set(i, this.items.get(i - 1));
            }
        }
        this.items.set(to, o);
        this.fireTableDataChanged();
        return true;
    }

    public void rowMoved(int src, int dst) {

    }
}
