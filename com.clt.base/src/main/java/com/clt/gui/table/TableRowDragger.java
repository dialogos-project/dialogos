package com.clt.gui.table;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class TableRowDragger extends MouseInputAdapter {

    private JTable table;

    private int onlyColumn;

    private int originalRow = -1;

    private int draggedRow = -1;

    private int draggedColumn = -1;

    private TableRowDragger(JTable table, int onlyColumn) {

        this.table = table;
        this.onlyColumn = onlyColumn;
    }

    public static void addDragHandler(JTable table) {

        TableRowDragger.addDragHandler(table, -1);
    }

    public static void addDragHandler(JTable table, int onlyColumn) {

        TableRowDragger d = new TableRowDragger(table, onlyColumn);
        table.addMouseListener(d);
        table.addMouseMotionListener(d);
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (this.table.isEnabled()) {
            TableModel model = this.table.getModel();
            int column = this.table.columnAtPoint(e.getPoint());
            int row = this.table.rowAtPoint(e.getPoint());
            if ((this.onlyColumn == -1) || (column == this.onlyColumn)) {
                if (model instanceof MovableRowsTableModel
                        ? ((MovableRowsTableModel) model).isRowMovable(row)
                        : true) {
                    this.originalRow = row;
                    this.draggedRow = row;
                    this.draggedColumn = column;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (this.draggedRow != -1) {
            int target = this.table.rowAtPoint(new Point(0, e.getY()));
            if (target == -1) {
                Rectangle bounds = this.table.getBounds();
                if (e.getY() <= bounds.y) {
                    target = 0;
                } else if (e.getY() >= bounds.y + bounds.height) {
                    target = this.table.getRowCount() - 1;
                }
            }
            if (target != this.draggedRow) {
                if (this.table.isEditing() ? this.table.getCellEditor()
                        .stopCellEditing() : true) {
                    TableModel model = this.table.getModel();
                    boolean moved = true;
                    if (model instanceof DefaultTableModel) {
                        ((DefaultTableModel) model).moveRow(this.draggedRow,
                                this.draggedRow, target);
                    } else if (model instanceof MovableRowsTableModel) {
                        moved
                                = ((MovableRowsTableModel) model).moveRow(this.draggedColumn,
                                        this.draggedRow,
                                        target);
                    } else {
                        for (int column = this.table.getColumnCount() - 1; column >= 0; column--) {
                            Object value = this.table.getValueAt(target, column);
                            this.table.setValueAt(this.table.getValueAt(this.draggedRow,
                                    column), target, column);
                            this.table.setValueAt(value, this.draggedRow, column);
                        }
                    }

                    if (moved) {
                        this.draggedRow = target;
                        this.table
                                .setRowSelectionInterval(this.draggedRow, this.draggedRow);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        TableModel model = this.table.getModel();
        if (model instanceof MovableRowsTableModel) {
            if (this.originalRow != this.draggedRow) {
                ((MovableRowsTableModel) model).rowMoved(this.originalRow,
                        this.draggedRow);
            }
        }
        this.draggedRow = -1;
    }
}
