package com.clt.gui.table;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * This is a wrapper class takes a TreeTableModel and implements the table model
 * interface. The implementation is trivial, with all of the event dispatching
 * support provided by the superclass: the AbstractTableModel.<p>
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc., 901 San Antonio Road, Palo
 * Alto, California, 94303, U.S.A. All rights reserved.<p>
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Sun.
 *
 * @version 1.2 10/27/98
 * @author Philip Milne
 * @author Scott Violet
 */
public class TreeTableModelAdapter extends AbstractTableModel {
    JTree tree;
    TreeTableModel treeTableModel;

    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {

        this.tree = tree;
        this.treeTableModel = treeTableModel;

        tree.addTreeExpansionListener(new TreeExpansionListener() {

            // Don't use fireTableRowsInserted() here; the selection model
            // would get updated twice.
            public void treeExpanded(TreeExpansionEvent event) {

                TreeTableModelAdapter.this.fireTableDataChanged();
            }

            public void treeCollapsed(TreeExpansionEvent event) {

                TreeTableModelAdapter.this.fireTableDataChanged();
            }
        });

        // Install a TreeModelListener that can update the table when
        // tree changes. We use delayedFireTableDataChanged as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        treeTableModel.addTreeModelListener(new TreeModelListener() {

            public void treeNodesChanged(TreeModelEvent e) {

                TreeTableModelAdapter.this.delayedFireTableDataChanged();
            }

            public void treeNodesInserted(TreeModelEvent e) {

                TreeTableModelAdapter.this.delayedFireTableDataChanged();
            }

            public void treeNodesRemoved(TreeModelEvent e) {

                TreeTableModelAdapter.this.delayedFireTableDataChanged();
            }

            public void treeStructureChanged(TreeModelEvent e) {

                TreeTableModelAdapter.this.delayedFireTableDataChanged();
            }
        });
    }

    // Wrappers, implementing TableModel interface.
    public int getColumnCount() {

        return this.treeTableModel.getColumnCount();
    }

    @Override
    public String getColumnName(int column) {

        return this.treeTableModel.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int column) {

        return this.treeTableModel.getColumnClass(column);
    }

    public int getRowCount() {

        return this.tree.getRowCount();
    }

    protected Object nodeForRow(int row) {

        TreePath treePath = this.tree.getPathForRow(row);
        return treePath.getLastPathComponent();
    }

    public Object getValueAt(int row, int column) {

        return this.treeTableModel.getValueAt(this.nodeForRow(row), column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {

        return this.treeTableModel.isCellEditable(this.nodeForRow(row), column);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {

        this.treeTableModel.setValueAt(value, this.nodeForRow(row), column);
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableDataChanged() {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                TreeTableModelAdapter.this.fireTableDataChanged();
            }
        });
    }
}
