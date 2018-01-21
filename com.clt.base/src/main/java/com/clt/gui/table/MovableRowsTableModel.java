package com.clt.gui.table;

import javax.swing.table.TableModel;

public interface MovableRowsTableModel extends TableModel {

    /**
     * Return whether the row was successfully moved
     */
    public boolean moveRow(int column, int sourceRow, int targetRow);

    /**
     * Callback, that is called when a user has finished a drag.
     */
    public void rowMoved(int sourceRow, int targetRow);

    /**
     * Return whether the argument row may be moved
     */
    public boolean isRowMovable(int row);
}
