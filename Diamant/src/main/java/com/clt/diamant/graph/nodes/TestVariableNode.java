package com.clt.diamant.graph.nodes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class TestVariableNode extends Node {

    // property names. Don't change, these are written to XML.
    public static final String ELSE_EDGE = "else_edge";
    public static final String VAR_ID = "var_id";
    public static final String VAR_UID = "var_uid";
    public static final String VAR_NAME = "var_name";
    public EdgeConditionModel edgeModel;

    public TestVariableNode() {

        super();
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {

        JPanel p = new JPanel(new BorderLayout());

        JPanel popup = new JPanel(new FlowLayout(FlowLayout.LEFT));
        popup.add(new JLabel(Resources.getString("Variable") + ':'));
        popup.add(NodePropertiesDialog.createComboBox(properties,
                TestVariableNode.VAR_NAME,
                this.getGraph().getAllVariables(Graph.LOCAL)));

        p.add(popup, BorderLayout.NORTH);

        final EdgeConditionModel model = new EdgeConditionModel(this, properties,
                Resources.getString("Patterns"));
        this.edgeModel = model;
        final JTable table = new JTable(model);
        // table.setRowSelectionAllowed(false);
        // table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel c
                        = (JLabel) super.getTableCellRendererComponent(table, value,
                                isSelected,
                                hasFocus, row, column);
                if (!model.isCellEditable(row, column)) {
                    c.setForeground(Color.lightGray);
                }
                return c;
            }
        });
        TableRowDragger.addDragHandler(table);

        JPanel center = new JPanel(new BorderLayout());
        JScrollPane jsp = GUI.createScrollPane(table, new Dimension(300, 150));
        center.add(jsp, BorderLayout.CENTER);
        center.add(NodePropertiesDialog.createCheckBox(properties,
                TestVariableNode.ELSE_EDGE,
                Resources.getString("IncludeCatchAll")), BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        final JButton deleteButton = new CmdButton(new Runnable() {

            public void run() {

                model.deleteRows(table.getSelectedRows());
            }
        }, Resources.getString("Delete"));
        buttons.add(deleteButton);
        deleteButton.setEnabled(table.getSelectedRow() >= 0);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {

                deleteButton.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        final JButton newButton = new CmdButton(new Runnable() {

            public void run() {

                int row = model.addRow();
                table.setRowSelectionInterval(row, row);
            }
        }, Resources.getString("New"));
        buttons.add(newButton);

        p.add(buttons, BorderLayout.SOUTH);
        p.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
        p.add(Box.createHorizontalStrut(8), BorderLayout.EAST);

        return p;

    }

    @SuppressWarnings("unchecked")
    private List<Edge> getEdgeModel() {

        return (List<Edge>) this.getProperty(EdgeConditionModel.EDGE_PROPERTY);
    }

    @Override
    public boolean editProperties(Component parent) {

        Edge elseEdge = null;

        int realEdges = this.numEdges();

        if (this.getBooleanProperty(TestVariableNode.ELSE_EDGE)) {
            // if we have an "others" case, it will always be the last outgoing edge
            elseEdge = this.getEdge(this.numEdges() - 1);
            realEdges--;
        }

        List<Edge> v = new Vector<Edge>();
        for (int i = 0; i < realEdges; i++) {
            // edit all but the last edge
            v.add(this.getEdge(i));
        }

        this.setProperty(EdgeConditionModel.EDGE_PROPERTY, v);

        if (super.editProperties(parent)) {
            List<Edge> es = this.getEdgeModel();

            this.removeAllEdges();
            for (Edge e : es) {
                this.addEdge(e);
            }

            if (this.getBooleanProperty(TestVariableNode.ELSE_EDGE)) {
                // we have an "else case". Install new edge if it didn't exist yet.
                if (elseEdge != null) {
                    this.addEdge(elseEdge);
                } else {
                    this.addEdge(new CatchAllEdge(this));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription(Edge selectedEdge) {

        StringBuffer buffer = new StringBuffer(super.getDescription(selectedEdge));

        Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
        if (v == null) {
            buffer.append("<p><tt><b>case</b> &lt;"
                    + this.html(Resources.getString("NoVariableAssigned"))
                    + "&gt; <b>of</b></tt>");
        } else {
            buffer.append("<p><tt><b>case</b> " + this.html(v.getName())
                    + " <b>of</b></tt>");
        }

        buffer.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        for (int i = 0; i < this.numEdges(); i++) {
            Edge e = this.getEdge(i);
            buffer.append("<tr>");
            buffer.append("<td width=\"20\"></td><td><tt>");
            if (e == selectedEdge) {
                buffer.append("<font color=\"red\">");
            }
            buffer.append(this.html(e.getCondition()));
            if (e == selectedEdge) {
                buffer.append("</font>");
            }
            buffer.append("</tt></td></tr>");
        }
        buffer.append("</table>");

        return buffer.toString();
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
        if (v != null) {
            try {
                String uid = uid_map.variables.getKey(v);
                Graph.printAtt(out, TestVariableNode.VAR_UID, uid);
                Graph.printAtt(out, TestVariableNode.VAR_NAME, v.getName());
            } catch (Exception exn) {
            } // variable deleted
        }

        if (this.getBooleanProperty(TestVariableNode.ELSE_EDGE)) {
            Graph.printAtt(out, "boolean", TestVariableNode.ELSE_EDGE, "1");
        }
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value,
            IdMap uid_map)
            throws SAXException {

        if (name.equals(TestVariableNode.VAR_NAME) && (value != null)) {
            Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
            if (v == null) {
                // old file format, only local variables by name
                Collection<Slot> variables = this.getGraph().getVariables();
                for (Slot slot : variables) {
                    if (slot.getName().equals(value)) {
                        this.setProperty(name, slot);
                        return;
                    }
                }
                r.raiseException(Resources.format("UnknownVariable", value));
            } else {
                // new file format, variable already set by id
                if (!v.getName().equals(value)) {
                    r.raiseException(Resources.format("NameIdMatch", value));
                }
            }
        } else if (name.equals(TestVariableNode.VAR_ID) && (value != null)) {
            Graph g = this.getGraph();
            int pos;
            for (pos = 0; pos < value.length() ? value.charAt(pos) == '^' : false; pos++) {
                g = g.getSuperGraph();
            }

            try {
                int index = Integer.parseInt(value.substring(pos));
                this
                        .setProperty(TestVariableNode.VAR_NAME, g.getVariables().get(index));
            } catch (Exception exn) {
                r.raiseAttributeValueException(TestVariableNode.VAR_ID);
            }
        } else if (name.equals(TestVariableNode.VAR_UID) && (value != null)) {
            try {
                this.setProperty(TestVariableNode.VAR_NAME, uid_map.variables
                        .get(value));
            } catch (Exception exn) {
                r.raiseException(Resources.format("UnknownVariable", "ID " + value));
            }
        } else if (name.equals(TestVariableNode.ELSE_EDGE)) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    public void update(Mapping map) {

        super.update(map);

        Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
        if (v != null) {
            Slot new_v = map.getVariable(v);
            if (new_v != v) {
                this.setProperty(TestVariableNode.VAR_NAME, new_v);
            }
        }
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
        if (v == null) {
            throw new NodeExecutionException(this, Resources.getString("NoVariableAssigned"), logger);
        }

        boolean others = this.getBooleanProperty(TestVariableNode.ELSE_EDGE);

        List<Slot> accessible_vars = this.getGraph().getAllVariables(Graph.LOCAL);
        for (int i = 0; i < (others ? this.numEdges() - 1 : this.numEdges()); i++) {
            Edge e = this.getEdge(i);

            Pattern pattern = null;
            try {
                pattern = this.parsePattern(e.getCondition());
            } catch (Exception exn) {
                throw new NodeExecutionException(this, Resources.format(
                        "containsIllegalInputPattern", exn.getLocalizedMessage()), logger);
            }
            Match match = pattern.match(v.getValue());
            if (match != null) {
                for (Iterator<String> vars = match.variables(); vars.hasNext();) {
                    Slot slot = null;
                    String name = vars.next();
                    for (int j = accessible_vars.size() - 1; (j >= 0) && (slot == null); j--) {
                        Slot s = accessible_vars.get(j);
                        if (name.equals(s.getName())) {
                            slot = s;
                        }
                    }
                    if (slot != null) {
                        slot.setValue(match.get(name));
                    } else {
                        throw new NodeExecutionException(this,
                                "Attempt to bind non existing variable " + name, logger);
                    }
                }

                comm.transition(this, e.getTarget(), i, v.getName() + " = "
                        + v.getValue());
                return e.getTarget();
            }
        }

        if (others) {
            Edge e = this.getEdge(this.numEdges() - 1);
            comm.transition(this, e.getTarget(), this.numEdges() - 1, v.getName()
                    + " = " + v.getValue());
            return e.getTarget();
        } else {
            throw new NodeExecutionException(this, "No given pattern matches value "
                    + v.getValue(), logger);
        }
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);
        Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
        if (v != null) {
            if (!this.getGraph().getAllVariables(Graph.LOCAL).contains(v)) {
                this.reportError(errors, false, Resources.format(
                        "referencesInaccessibleVariable",
                        v.getName()));
            }
        } else {
            this.reportError(errors, false, Resources
                    .getString("hasNoVariableAssigned"));
        }

        int n
                = this.getBooleanProperty(TestVariableNode.ELSE_EDGE) ? this.numEdges() - 1
                : this.numEdges();
        List<Slot> accessible_vars = this.getGraph().getAllVariables(Graph.LOCAL);
        for (int i = 0; i < n; i++) {
            Edge e = this.getEdge(i);

            try {
                Pattern p = this.parsePattern(e.getCondition());

                Slot slot = null;
                Map<String, Type> variablesTypes = new HashMap<String, Type>();
                for (Iterator<String> vars = p.getFreeVars().iterator(); vars.hasNext()
                        && (slot == null);) {
                    String name = vars.next();
                    for (int j = accessible_vars.size() - 1; j >= 0; j--) {
                        Slot s = accessible_vars.get(j);
                        if (name.equals(s.getName())) {
                            slot = s;
                            variablesTypes.put(name, slot.getType());
                        }
                    }
                    if (slot == null) {
                        this.reportError(errors, false, Resources.format(
                                "containsIllegalInputPattern",
                                "Attempt to bind non existing variable " + name));
                    }
                }

                if (v != null) {
                    Type.unify(v.getType(), p.getType(variablesTypes));
                }
            } catch (Exception exn) {
                this.reportError(errors, false, Resources
                        .getString("containsIllegalInputPattern")
                        + exn.getLocalizedMessage());
            }
        }
    }

    @Override
    public Map<Slot, Node> getFreeVariables(Map<Slot, Node> h) {

        Slot v = (Slot) this.getProperty(TestVariableNode.VAR_NAME);
        if (v != null) {
            h.put(v, this);
        }
        return h;
    }

    @Override
    public boolean acceptableToSave() {
        return !existDuplicateEdgesOrEmptyLabels(this.edgeModel.getEdges());
    }

    /**
     *
     * @param edgeList
     * @return true if edge labels are empty or duplicate
     */
    public boolean existDuplicateEdgesOrEmptyLabels(List<Edge> edgeList) {
        ArrayList<String> labels = new ArrayList<>(edgeList.size());
        for (Edge edge : edgeList) {
            String label = edge.getCondition();
            if (label.equals("")) {
                return true;
            } else if (labels.contains(edge.getCondition())) {
                return true;
            } else {
                labels.add(edge.getCondition());
            }
        }
        return false;
    }
}
