package com.clt.diamant.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.clt.diamant.Device;
import com.clt.diamant.Resources;

/**
 * @author dabo
 *
 */
public class CommTableModel extends AbstractTableModel {

    private List<Entry> entries;

    public CommTableModel() {

        this.entries = new ArrayList<Entry>();
    }

    public int getRowCount() {

        return this.entries.size();
    }

    public int getColumnCount() {

        return 3;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        switch (columnIndex) {
            case 0:
                return Device.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public String getColumnName(int columnIndex) {

        switch (columnIndex) {
            case 0:
                return Resources.getString(("Device"));
            case 1:
                return " ";
            case 2:
                return Resources.getString("Input") + " / "
                        + Resources.getString("Output");
            default:
                return null;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        Entry entry = this.entries.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return entry.device;
            case 1:
                return entry.in ? "->" : "<-";
            case 2:
                return entry.value;
            default:
                return null;
        }
    }

    public void add(Device d, boolean in, String value) {

        synchronized (this.entries) {
            this.entries.add(new Entry(d, in, value));
            this.fireTableRowsInserted(this.entries.size() - 1,
                    this.entries.size() - 1);
        }
    }

    private static class Entry {

        public Device device;
        public boolean in;
        public String value;

        public Entry(Device device, boolean in, String value) {

            this.device = device;
            this.in = in;
            this.value = value;
        }
    }
}
