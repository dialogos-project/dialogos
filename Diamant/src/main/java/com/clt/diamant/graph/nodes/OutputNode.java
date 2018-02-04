package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.Device;
import com.clt.diamant.DialogOutput;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.gui.RawIcon;
import com.clt.gui.table.MovableRowsTableModel;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;
import com.clt.script.exp.Value;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class OutputNode extends Node {

    public static final String OUTPUT = "output";

    public OutputNode() {

        super();

        this.addEdge();
    }

    public static Color getDefaultColor() {

        return new Color(0, 204, 153);
    }

    @Override
    public void update(Mapping map) {

        super.update(map);

        Map<Device, DialogOutput> h = this.getOutputMap(false);
        if (h != null) {
            this.setProperty(OutputNode.OUTPUT, this.updateDeviceMap(h, map));
        }
    }

    protected <T> Map<Device, T> updateDeviceMap(Map<Device, T> map, Mapping mapping) {

        Map<Device, T> updatedMap = new Hashtable<Device, T>();
        for (Device d : map.keySet()) {
            Device targetDev = mapping.getDevice(d);
            if (targetDev == null) {
                targetDev = d;
            }
            updatedMap.put(targetDev, map.get(d));
        }
        return updatedMap;
    }

    public boolean hasOutput() {

        Map<Device, DialogOutput> h = this.getOutputMap(false);
        if (h == null) {
            return false;
        } else if (h.size() == 0) {
            return false;
        } else {
            for (DialogOutput output : h.values()) {
                if (output.size() > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public JComponent createEditorComponent(final Map<String, Object> properties) {

        final Map<Device, DialogOutput> out = this.getOutputMap(properties, true);

        Collection<Device> devs = this.getGraph().getOwner().getDevices();

        JTabbedPane tabs = GUI.createTabbedPane();

        if (devs.size() == 0) {
            final JPanel output = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            output.add(new JLabel(Resources.getString("NoOutputDeviceError")), gbc);
            tabs.addTab(Resources.getString("Output"), output);
        } else {
            final JList devices = new JList(new Vector<Device>(devs));
            devices.setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value,
                        int index,
                        boolean isSelected, boolean cellHasFocus) {

                    JLabel label
                            = (JLabel) super.getListCellRendererComponent(list, value,
                                    index,
                                    isSelected, cellHasFocus);
                    Device.Icon icon = ((Device) value).getIconData();
                    if (icon != null) {
                        label.setIcon(new RawIcon(icon.getData(), icon.getWidth()));
                    }
                    return label;
                }
            });
            devices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane djsp
                    = new JScrollPane(devices, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) {

                @Override
                public Dimension getPreferredSize() {

                    return new Dimension(super.getPreferredSize().width + 10, 200);
                }
            };
            djsp.setMinimumSize(djsp.getPreferredSize());

            GUI.setupScrollBar(djsp.getVerticalScrollBar());
            GUI.setupScrollBar(djsp.getHorizontalScrollBar());

            final JTable table = new JTable() {

                @Override
                public boolean editCellAt(int row, int column, EventObject e) {

                    Object o = this.getValueAt(row, column);
                    if ((o != null) && (o.toString().length() > 40)) {
                        if (!(e instanceof MouseEvent)
                                || (((MouseEvent) e).getClickCount() > 1)) {
                            JTextArea a = new JTextArea();
                            a.setWrapStyleWord(true);
                            a.setLineWrap(true);
                            a.setText(o.toString());
                            a.setEditable(true);

                            JScrollPane jsp = new JScrollPane(a,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                            jsp.setPreferredSize(new Dimension(400, 200));
                            OptionPane.message(this, jsp, Resources.getString("Output"),
                                    OptionPane.PLAIN);
                            this.setValueAt(a.getText(), row, column);
                            return false; // Editing already done, so return false
                        } else {
                            return false;
                        }
                    } else {
                        return super.editCellAt(row, column, e);
                    }
                }
            };

            table.getTableHeader().setReorderingAllowed(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            TableRowDragger.addDragHandler(table, 0);

            JScrollPane tjsp = GUI.createScrollPane(table, new Dimension(400, 200));
            GUI.setupScrollBar(tjsp.getVerticalScrollBar());
            GUI.setupScrollBar(tjsp.getHorizontalScrollBar());

            final JPanel output = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            output.add(djsp, gbc);

            gbc.gridx = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            output.add(tjsp, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(6, 12, 6, 12);
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            JPanel buttons = new JPanel(new GridLayout(1, 0, 12, 0));

            final JButton bAdd = new CmdButton(new Runnable() {

                public void run() {

                    if (table.isEditing() ? table.getCellEditor().stopCellEditing()
                            : true) {
                        ((DialogOutputModel) table.getModel()).addRow();
                        table.setRowSelectionInterval(table.getRowCount() - 1,
                                table.getRowCount() - 1);
                    }
                }
            }, Resources.getString("New"));
            buttons.add(bAdd);

            final JButton bDelete = new CmdButton(new Runnable() {

                public void run() {

                    if (table.isEditing() ? table.getCellEditor().stopCellEditing()
                            : true) {
                        ((DialogOutputModel) table.getModel()).removeRows(table
                                .getSelectedRows());
                        table.clearSelection();
                        ((DialogOutputModel) table.getModel()).fireTableDataChanged();
                        table.requestFocus();
                    }
                }
            }, Resources.getString("Delete"));
            buttons.add(bDelete);

            bAdd.setEnabled(false);
            bDelete.setEnabled(false);

            table.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {

                    if (!e.getValueIsAdjusting()) {
                        bDelete.setEnabled(table.getSelectedRow() >= 0);
                    }
                }
            });

            devices.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent evt) {

                    if (!evt.getValueIsAdjusting()) {
                        if (devices.getSelectedIndex() >= 0) {
                            if (table.isEditing() ? table.getCellEditor().stopCellEditing()
                                    : true) {
                                Device dev = (Device) devices.getSelectedValue();
                                DialogOutput dlgout = out.get(dev);
                                if (dlgout == null) {
                                    dlgout = new DialogOutput();
                                    out.put(dev, dlgout);
                                }
                                table.setModel(new DialogOutputModel(dlgout));

                                bAdd.setEnabled(true);
                            } else {
                                table.getToolkit().beep();
                                Component c = table.getEditorComponent();
                                if (c instanceof JTextComponent) {
                                    ((JTextComponent) c).selectAll();
                                }
                            }
                        } else {
                            table.setModel(new DialogOutputModel(null));
                            bAdd.setEnabled(false);
                        }
                        TableColumn c = table.getColumnModel().getColumn(0);
                        c.setResizable(false);
                        c.setCellRenderer(new TextRenderer());
                        /*
                           * c.setCellRenderer(new TableCellRenderer() { public
                           * Component getTableCellRendererComponent(JTable
                           * table, Object value, boolean isSelected, boolean
                           * hasFocus, int row, int column) { JTextArea a = new
                           * JTextArea(); a.setLineWrap(true);
                           * a.setWrapStyleWord(true); a.setEditable(false);
                           * a.setText(value == null ? "" : value.toString());
                           * 
                           * a.setBackground(isSelected ? Color.blue :
                           * Color.green); a.setBorder(hasFocus ?
                           * BorderFactory.createLineBorder(Color.red, 1) :
                           * BorderFactory.createLineBorder(Color.yellow, 1));
                           * 
                           * return a; } });
                         */

                        table.sizeColumnsToFit(-1);
                    }
                }
            });

            gbc.weighty = 0.0;
            output.add(buttons, gbc);

            devices.setSelectedIndex(0);

            tabs.addTab(Resources.getString("Output"), output);

            JPanel options = new JPanel(new GridBagLayout());

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(6, 12, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = 1;
            options.add(NodePropertiesDialog.createCheckBox(properties, "reset",
                    Resources.getString("SendResetFirst")), gbc);
            gbc.gridy++;
            options.add(NodePropertiesDialog.createCheckBox(properties, "wait",
                    Resources.getString("WaitUntilDone")), gbc);

            gbc.gridy++;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            options.add(Box.createGlue(), gbc);

            tabs.addTab(Resources.getString("Options"), options);

        } // if devs.size()

        return tabs;
    }


    /*
   * public void addOption(JPanel optionsPanel, JComponent c) {
   * GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.gridy
   * = GridBagConstraints.RELATIVE; gbc.insets = new Insets(6, 12, 0, 0);
   * gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.WEST;
   * gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.weighty = 0.0;
   * 
   * optionsPanel.add(c, gbc); }
     */
    @Override
    public String getDescription(Edge selectedEdge) {

        StringBuffer buffer = new StringBuffer(super.getDescription(selectedEdge));

        Map<Device, DialogOutput> h = this.getOutputMap(false);

        if ((h != null) && (h.size() > 0)) {
            for (Device device : h.keySet()) {
                DialogOutput output = h.get(device);
                if (output != null) {
                    buffer.append("<p><b>"
                            + this.html(Resources.getString("OutputToDevice"))
                            + "</b> <tt>" + this.html(device) + "</tt>:");
                    buffer.append(output.getDescription());
                    buffer.append("</p>");
                }
            }
        } else {
            buffer.append("<p><b>" + this.html(Resources.getString("NoOutput"))
                    + "</b></p>");
        }

        buffer.append("<p>");
        buffer.append("<b>" + this.html(Resources.getString("WaitUntilDone"))
                + " :</b> <tt>"
                + this.getBooleanProperty("wait") + "</tt><br>");
        buffer.append("<b>" + this.html(Resources.getString("SendResetFirst"))
                + " :</b> <tt>"
                + this.getBooleanProperty("reset") + "</tt>");
        buffer.append("</p>");
        return buffer.toString();
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        Map<Device, DialogOutput> h = this.getOutputMap(false);
        if (h != null ? h.size() > 0 : false) {
            boolean inited = false;
            Collection<Device> activeDevices
                    = this.getGraph().getOwner().getDevices();
            for (Device d : activeDevices) {
                DialogOutput o = h.get(d);

                if (o != null ? o.size() > 0 : false) {
                    String id = uid_map.devices.getKey(d);
                    if (!inited) {
                        inited = true;
                        Graph.printAtt(out, "list", OutputNode.OUTPUT, null);
                    }

                    Graph.printAtt(out, "list", id, null);

                    for (int i = 0; i < o.size(); i++) {
                        Graph.printAtt(out, "Text", o.getValue(i).toString());
                    }
                    out.closeElement("att");
                }
            }

            if (inited) {
                out.closeElement("att");
            }
        }

        Graph.printAtt(out, "boolean", "reset", this.getBooleanProperty("reset")
                ? "1" : "0");
        Graph.printAtt(out, "boolean", "wait", this.getBooleanProperty("wait")
                ? "1" : "0");
    }

    @Override
    public void readAttribute(final XMLReader r, String name, String value,
            final IdMap uid_map)
            throws SAXException {

        if (name.equals(OutputNode.OUTPUT)) {
            final Map<Device, DialogOutput> out = this.getOutputMap(true);

            r.setHandler(new AbstractHandler("att") {

                @Override
                public void start(String name, Attributes atts) {

                    if (name.equals("att")) {
                        Device d = uid_map.devices.get(atts.getValue("name"));

                        final DialogOutput output = new DialogOutput();
                        out.put(d, output);

                        r.setHandler(new AbstractHandler("att") {

                            @Override
                            public void start(String name, Attributes atts)
                                    throws SAXException {

                                String v = atts.getValue("value");
                                if (v != null) {
                                    r.setHandler(new AbstractHandler("att")); // zu fruehes
                                    // Aussteigen aus
                                    // der Liste
                                    // verhindern
                                    output.add(v);
                                } else {
                                    if (!atts.getValue("type").equals("list")) {
                                        r.raiseException("Illegal dialog output attribute");
                                    }

                                    final StringBuffer data = new StringBuffer();
                                    data.append("{ ");
                                    r.setHandler(new AbstractHandler("att") {

                                        @Override
                                        public void start(String name, Attributes atts) {

                                            if (name.equals("att")) {
                                                r.setHandler(new AbstractHandler("att")); // zu fruehes
                                                // Aussteigen
                                                // aus der
                                                // Liste
                                                // verhindern
                                                String entry = atts.getValue("name");
                                                String exp = atts.getValue("value");
                                                data.append(entry + " = " + exp);
                                            }
                                        }

                                        @Override
                                        public void end(String name) {

                                            data.append(" }");
                                            output.add(data.toString());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        } else if (name.equals("reset") || name.equals("wait")) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        this.prompt(comm, logger);
        Node target = this.getEdge(0).getTarget();
        comm.transition(this, target, 0, null);
        return target;
    }

    protected void prompt(final WozInterface comm, ExecutionLogger logger) {

        Map<Device, DialogOutput> h = this.getOutputMap(false);
        if (h != null) {
            if (this.getBooleanProperty("reset")) {
                for (Device d : h.keySet()) {
                    try {
                        d.reset();
                    } catch (IOException exn) {
                    }
                }
            }

            final boolean wait = this.getBooleanProperty("wait");
            final Collection<Thread> deviceThreads = new ArrayList<Thread>(h.size());
            for (final Device d : h.keySet()) {
                final DialogOutput output = h.get(d);
                if (output.size() > 0) {
                    final Value values[] = new Value[output.size()];
                    for (int i = 0; i < output.size(); i++) {
                        try {
                            String s = output.getValue(i);
                            if (s.trim().length() > 0) {
                                values[i] = this.parseExpression(s).evaluate(comm);
                            } else {
                                values[i] = null;
                            }
                        } catch (Exception exn) {
                            throw new NodeExecutionException(this, Resources.getString("ErrorWhileEvaluating")
                                    + "'" + output.getValue(i) + "'", exn, logger);
                        }
                    }

                    Thread t = new Thread(new Runnable() {

                        public void run() {

                            OutputNode.this.send(comm, d, values, wait);
                        }
                    }, "Output to " + d);
                    deviceThreads.add(t);
                    t.start();
                }
            }

            // if (wait) {
            try {
                for (Thread t : deviceThreads) {
                    t.join();
                }
            } catch (InterruptedException exn) {
                for (Thread t : deviceThreads) {
                    if (t.isAlive()) {
                        t.interrupt();
                    }
                }
            }
            // }
        }
    }

    private void send(WozInterface comm, Device d, Value values[], boolean wait) {

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                try {
                    comm.output(d, values[i]);
                    d.send(values[i]);
                } catch (Exception exn) {
                    if (Version.DEBUG) {
                        exn.printStackTrace();
                    }
                }
            }
        }

        try {
            if (wait) {
                d.echo();
            }
        } catch (Exception exn) {
        }
    }

    @Override
    public Map<Device, Node> getFreeDevices(Map<Device, Node> h) {

        Map<Device, ?> output = (Map<Device, ?>) this.getProperty(OutputNode.OUTPUT);
        if (output != null) {
            for (Device device : output.keySet()) {
                h.put(device, this);
            }
        }
        return h;
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);
        Map<Device, DialogOutput> h = this.getOutputMap(false);
        if (h == null) {
            return;
        }

        for (Device d : h.keySet()) {
            DialogOutput output = h.get(d);
            if (output != null) {
                if (!this.getGraph().getOwner().getDevices().contains(d)) {
                    this.reportError(errors, false, Resources.format(
                            "referencesInaccessibleDevice",
                            d.getName()));
                }
                for (int i = 0; i < output.size(); i++) {
                    try {
                        String s = output.getValue(i).toString();
                        if (s.trim().length() > 0) {
                            this.parseExpression(s).getType();
                        }
                    } catch (Exception exn) {
                        this.reportError(errors, false, Resources
                                .getString("containsIllegalExpression")
                                + ": " + exn.getLocalizedMessage());
                    }
                }
            }
        }
    }

    protected Map<Device, DialogOutput> getOutputMap(boolean create) {

        return this.getOutputMap(this.properties, create);
    }

    @SuppressWarnings("unchecked")
    protected Map<Device, DialogOutput> getOutputMap(
            Map<String, Object> properties, boolean create) {

        Map<Device, DialogOutput> h
                = (Map<Device, DialogOutput>) properties.get(OutputNode.OUTPUT);
        if ((h == null) && create) {
            h = new Hashtable<Device, DialogOutput>();
            properties.put(OutputNode.OUTPUT, h);
        }
        return h;
    }

    private static class DialogOutputModel
            extends AbstractTableModel
            implements MovableRowsTableModel {

        DialogOutput output;

        public DialogOutputModel(DialogOutput output) {

            this.output = output;
        }

        public int getRowCount() {

            return this.output == null ? 0 : this.output.size();
        }

        public int getColumnCount() {

            return 1;
        }

        @Override
        public String getColumnName(int columnIndex) {

            return Resources.getString("Value");
        }

        public Class<?> getColumnClass(int columnIndex) {

            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {

            return true;
        }

        public boolean isRowMovable(int row) {

            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {

            return this.output.getValue(rowIndex);
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

            this.output.setValue(rowIndex, (String) aValue);
        }

        public void addRow() {

            this.output.add("");
            this.fireTableDataChanged();
        }

        public void removeRows(int[] rows) {

            this.output.remove(rows);
        }

        public boolean moveRow(int column, int sourceRow, int targetRow) {

            this.output.move(sourceRow, targetRow);
            return true;
        }

        public void rowMoved(int src, int dst) {

        }
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

        // vxml
        Map<Device, DialogOutput> h = this.getOutputMap(false);
        if (h != null) {
            if (this.getBooleanProperty("reset")) {
                for ( // @SuppressWarnings("unused")
                        Device d : h.keySet()) {
                    // no reset in VoiceXML
                }
            }

            final boolean wait = this.getBooleanProperty("wait");
            for (Device d : h.keySet()) {
                DialogOutput out = h.get(d);
                w.openElement("prompt", new String[]{"device", "bargein"},
                        new String[]{
                            d.getName(), wait ? "false" : "true"});

                for (int i = 0; i < out.size(); i++) {
                    w.printElement("value", new String[]{"expr"},
                            new String[]{Node.vxmlExp(out.getValue(i))});
                }

                w.closeElement("prompt");
            }
        }
    }
}
