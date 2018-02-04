package com.clt.diamant.graph.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.InputHandler;
import com.clt.diamant.graph.Node;
import com.clt.gui.table.MovableRowsTableModel;

public class EdgeConditionModel extends AbstractTableModel implements MovableRowsTableModel {

    public static final String EDGE_PROPERTY = "tmp_edges";

    private List<Edge> v;
    private List<InputHandler> prefix, postfix;
    private String name;
    private Map<String, Object> p;
    private boolean showPreAndPostfix;
    private Node source;

    public EdgeConditionModel(Node source, Map<String, Object> properties, String name) {

        this(source, properties, name, null, null, false);
    }

    public EdgeConditionModel(Node source, Map<String, Object> properties,
            String name,
            List<InputHandler> prefix,
            List<InputHandler> postfix,
            boolean showPreAndPostfix) {

        this.source = source;
        this.prefix = prefix == null ? new ArrayList<InputHandler>() : prefix;
        this.postfix = postfix == null ? new ArrayList<InputHandler>() : postfix;
        this.showPreAndPostfix = showPreAndPostfix;

        this.v = new ArrayList<Edge>();
        this.p = properties;
        List<Edge> data = (List<Edge>) this.p.get(EdgeConditionModel.EDGE_PROPERTY);
        for (Edge edge : data) {
            this.v.add(edge.clone(source));
        }

        this.name = name;
        this.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                EdgeConditionModel.this.p.put(EdgeConditionModel.EDGE_PROPERTY,
                        EdgeConditionModel.this.v);
            }
        });
    }

    public void setName(String name) {

        this.name = name;
        this
                .fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    public void showPreAndPostfix(boolean showPreAndPostfix) {

        this.showPreAndPostfix = showPreAndPostfix;
        this.fireTableDataChanged();
    }

    private int getPrefixSize() {

        if (this.showPreAndPostfix) {
            return this.prefix.size();
        } else {
            return 0;
        }
    }

    private int getPostfixSize() {

        if (this.showPreAndPostfix) {
            return this.postfix.size();
        } else {
            return 0;
        }
    }

    public void clear() {

        this.v.clear();
        this.fireTableDataChanged();
    }

    public int addRow() {
        this.v.add(new Edge(this.source, null, ""));
        int row = this.getPrefixSize() + this.v.size() - 1;
        this.fireTableRowsInserted(row, row);
        return row;
    }

    public void deleteRows(int rows[]) {

        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            this.deleteRow(rows[i]);
        }
    }

    public void deleteRow(int row) {

        int ps = this.getPrefixSize();
        if ((row >= ps) && (row < ps + this.v.size())) {
            this.v.remove(row - ps);
            this.fireTableRowsDeleted(row, row);
        }
    }

    public boolean isRowMovable(int row) {

        int ps = this.getPrefixSize();
        return (row >= ps) && (row < ps + this.v.size());
    }

    public boolean moveRow(int column, int from, int to) {

        int ps = this.getPrefixSize();
        if (from == to) {
            return false;
        }
        if ((from < ps) || (to < ps) || (from >= ps + this.v.size())
                || (to >= ps + this.v.size())) {
            return false;
        }

        Edge e = this.v.get(from - ps);
        if (from < to) {
            for (int i = from; i < to; i++) {
                this.v.set(i - ps, this.v.get(i - ps + 1));
            }
        } else {
            for (int i = from; i > to; i--) {
                this.v.set(i - ps, this.v.get(i - ps - 1));
            }
        }
        this.v.set(to - ps, e);
        this.fireTableDataChanged();
        return true;
    }

    public void rowMoved(int src, int dst) {

    }

    public int getRowCount() {

        return this.getPrefixSize() + this.v.size() + this.getPostfixSize();
    }

    public int getColumnCount() {

        return 1;
    }

    public Object getValueAt(int row, int column) {

        int ps = this.getPrefixSize();
        if (row < ps) {
            return this.prefix.get(row);
        } else if (row >= ps + this.v.size()) {
            return this.postfix.get(row - ps - this.v.size());
        } else {
            return this.v.get(row - ps).getCondition();
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {

        int ps = this.getPrefixSize();
        if ((row >= ps) && (row < ps + this.v.size())) {
            this.v.get(row - ps).setCondition((String) aValue);
            this.fireTableCellUpdated(row, column);
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {

        return String.class;
    }

    @Override
    public String getColumnName(int column) {

        return this.name;
    }

    @Override
    public boolean isCellEditable(int row, int column) {

        int ps = this.getPrefixSize();
        if ((row >= ps) && (row < ps + this.v.size())) {
            return true;
        } else {
            return false;
        }
    }

    public List<Edge> getEdges() {
        return this.v;
    }
}
