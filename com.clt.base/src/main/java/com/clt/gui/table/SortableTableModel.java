package com.clt.gui.table;

import javax.swing.table.TableModel;

/**
 * @author dabo
 *
 */
public interface SortableTableModel extends TableModel {

    enum SortMode {
        NONE,
        ASCENDING,
        DESCENDING
    }

    public SortMode getSortMode(int column);
}
