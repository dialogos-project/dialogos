package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.clt.gui.table.MovableRowsTableModel;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;

public class AssignmentDialog<SRC_TYPE, DST_TYPE> extends JDialog implements Commander {

    private Vector<SRC_TYPE> src;
    private Vector<DST_TYPE> dst;

    private AssignmentDialog(Component parent, Collection<SRC_TYPE> src, Collection<DST_TYPE> dst) {
        super(GUI.getFrameForComponent(parent), "Assign", true);

        // must clone vectors, because we will modify them
        this.src = new Vector<SRC_TYPE>(src);
        this.dst = new Vector<DST_TYPE>(dst);

        int size = Math.max(src.size(), dst.size());
        this.src.setSize(size);
        this.dst.setSize(size);

        this.getContentPane().setLayout(new BorderLayout());

        JTable table = new JTable(new AssignmentModel());
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);
        TableRowDragger.addDragHandler(table);

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(160);
        column.setCellRenderer(new TextRenderer());

        column = table.getColumnModel().getColumn(1);
        column.setResizable(false);
        column.setPreferredWidth(40);
        column.setMinWidth(column.getPreferredWidth());
        column.setMaxWidth(column.getPreferredWidth());
        TextRenderer tr = new TextRenderer();
        tr.setHorizontalAlignment(SwingConstants.CENTER);
        column.setCellRenderer(tr);

        column = table.getColumnModel().getColumn(2);
        column.setResizable(false);
        column.setMinWidth(160);
        column.setCellRenderer(new TextRenderer());

        JScrollPane jsp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 200);
            }
        };
        
        jsp.setBorder(BorderFactory.createEmptyBorder());
        GUI.setupScrollBar(jsp.getVerticalScrollBar());
        GUI.setupScrollBar(jsp.getHorizontalScrollBar());

        JButton b1 = new CmdButton(this, Commands.cmdCancel, GUI.getString("Cancel"));
        JButton b2 = new CmdButton(this, Commands.cmdOK, GUI.getString("OK"));

        Dimension d1 = b1.getPreferredSize();
        Dimension d2 = b2.getPreferredSize();
        Dimension d = new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
        b1.setPreferredSize(d);
        b2.setPreferredSize(d);

        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        buttons.add(Box.createHorizontalGlue(), gbc);

        gbc.insets = new Insets(6, 0, 6, 12);
        gbc.weightx = 0.0;
        gbc.gridx++;

        buttons.add(b1, gbc);
        gbc.gridx++;
        buttons.add(b2, gbc);

        this.getContentPane().add(jsp, BorderLayout.NORTH);
        this.getContentPane().add(new JSeparator(), BorderLayout.CENTER);
        this.getContentPane().add(buttons, BorderLayout.SOUTH);

        this.pack();
        WindowUtils.setLocationRelativeTo(this, parent);
    }

    public boolean doCommand(int cmd) {
        switch (cmd) {
            case Commands.cmdCancel:
                this.src = null;
                this.dispose();
                return true;

            case Commands.cmdOK:
                this.dispose();
                return true;

            default:
                return false;
        }
    }

    private SRC_TYPE getSrc(int index) {
        return this.src.get(index);
    }

    private DST_TYPE getDst(int index) {
        return this.dst.get(index);
    }

    public static <X, Y> Map<X, Y> getAssignment(Component parent, Collection<X> src, Collection<Y> dst) {
        AssignmentDialog<X, Y> d = new AssignmentDialog<X, Y>(parent, src, dst);

        d.setVisible(true);

        if (d.src != null) {
            Map<X, Y> t = new HashMap<X, Y>();
            for (int i = 0; i < d.src.size(); i++) {
                X source = d.getSrc(i);
                Y target = d.getDst(i);
                if ((source != null) && (target != null)) {
                    t.put(source, target);
                }
            }
            return t;
        } else {
            return null;
        }
    }

    private class AssignmentModel extends AbstractTableModel implements MovableRowsTableModel {

        public int getRowCount() {
            return AssignmentDialog.this.src.size();
        }

        public int getColumnCount() {
            return 3;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return AssignmentDialog.this.src.elementAt(row);
                case 1:
                    return "=>";
                case 2:
                    return AssignmentDialog.this.dst.elementAt(row) == null ? ('<' + GUI
                            .getString("New") + '>')
                            : AssignmentDialog.this.dst.elementAt(row);
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Source";
                case 1:
                    return "=>";
                case 2:
                    return "Target";
                default:
                    return null;
            }
        }

        public boolean isRowMovable(int row) {
            return true;
        }

        public boolean moveRow(int draggedColumn, int sourceRow, int targetRow) {
            if (draggedColumn == 0) {
                SRC_TYPE tmp = AssignmentDialog.this.src.get(sourceRow);
                AssignmentDialog.this.src.set(sourceRow, AssignmentDialog.this.src.get(targetRow));
                AssignmentDialog.this.src.set(targetRow, tmp);
            } else if (draggedColumn == 2) {
                DST_TYPE tmp = AssignmentDialog.this.dst.get(sourceRow);
                AssignmentDialog.this.dst.set(sourceRow, AssignmentDialog.this.dst.get(targetRow));
                AssignmentDialog.this.dst.set(targetRow, tmp);
            } else {
                return false;
            }

            this.fireTableDataChanged();
            return true;
        }

        public void rowMoved(int src, int dst) {

            // there is nothing else to do
        }
    }
}
