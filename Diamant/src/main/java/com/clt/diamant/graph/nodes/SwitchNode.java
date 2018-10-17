package com.clt.diamant.graph.nodes;

import com.clt.diamant.*;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.CatchAllEdge;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.table.TableRowDragger;
import com.clt.gui.table.TextRenderer;
import com.clt.script.exp.Expression;
import com.clt.script.exp.values.BoolValue;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * a SwitchNode switches among a list of conditional expressions.
 * The first that is evaluated as true determines the output edge being followed.
 * Additionally, a default/"else" edge can be used
 */
public class SwitchNode extends Node {

//    private static final String EXPRESSION_LIST = "expressionList";
    private static final String HAS_ELSE_EDGE = "else_edge";

    public SwitchNode() {
        super();
//        properties.putIfAbsent(EXPRESSION_LIST, new ArrayList<String>());
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {
        JPanel p = new JPanel(new BorderLayout());

        final EdgeConditionModel model = new EdgeConditionModel(this, properties,
                Resources.getString("Patterns"));
        final JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column)
            {
                JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!model.isCellEditable(row, column))
                    c.setForeground(Color.lightGray);
                return c;
            }
        });
        TableRowDragger.addDragHandler(table);

        JPanel center = new JPanel(new BorderLayout());
        JScrollPane jsp = GUI.createScrollPane(table, new Dimension(300, 150));
        center.add(jsp, BorderLayout.CENTER);
        center.add(NodePropertiesDialog.createCheckBox(properties, HAS_ELSE_EDGE,
                Resources.getString("IncludeCatchAll")), BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        final JButton deleteButton = new CmdButton(
                () -> model.deleteRows(table.getSelectedRows()),
            Resources.getString("Delete"));
        buttons.add(deleteButton);
        deleteButton.setEnabled(table.getSelectedRow() >= 0);
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) ->
                        deleteButton.setEnabled(table.getSelectedRow() >= 0));

        final JButton newButton = new CmdButton(() -> {
                int row = model.addRow();
                table.setRowSelectionInterval(row, row);
            }, Resources.getString("New"));
        buttons.add(newButton);

        p.add(buttons, BorderLayout.SOUTH);
        p.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
        p.add(Box.createHorizontalStrut(8), BorderLayout.EAST);
        return p;
    }

    @Override
    public boolean editProperties(Component parent) {
        Edge elseEdge = null;
        int realEdges = this.numEdges();
        if (this.getBooleanProperty(HAS_ELSE_EDGE)) {
            // if we have an "others" case, it will always be the last outgoing edge
            elseEdge = this.getEdge(this.numEdges() - 1);
            realEdges--;
        }

        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < realEdges; i++) {
            // edit all but the last edge
            edges.add(this.getEdge(i));
        }

        this.setProperty(EdgeConditionModel.EDGE_PROPERTY, edges);
        // call the dialog and if it succeeds set the new edges from the temporary edge_property
        if (super.editProperties(parent)) {
            List<Edge> es = (List<Edge>) this.getProperty(EdgeConditionModel.EDGE_PROPERTY);
            this.setProperty(EdgeConditionModel.EDGE_PROPERTY, null);
            this.removeAllEdges();
            for (Edge e : es) {
                this.addEdge(e);
            }
            if (this.getBooleanProperty(HAS_ELSE_EDGE)) {
                // we have an "else case".
                if (elseEdge != null) {
                    this.addEdge(elseEdge);
                } else { // install new edge if it didn't exist yet.
                    this.addEdge(new CatchAllEdge(this));
                }
            }
            return true;
        }
        this.setProperty(EdgeConditionModel.EDGE_PROPERTY, null);
        return false;
    }

    /**
     * evaluate the list of expressions (in order) and select the first matching one.
     */
    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        try {
            int targetIndex = -1;
            for (Edge edge : getOutEdges()) {
                targetIndex++;
                if (edge instanceof CatchAllEdge) {
                    return edge.getTarget();
                }
                String cond = edge.getCondition();
                Expression e = this.parseExpression(cond);
                BoolValue result = (BoolValue) e.evaluate(comm);
                if (result.getBool()) {
                    Node target = edge.getTarget();
                    comm.transition(this, target, targetIndex, result.toString());
                    return target;
                }
            }
        } catch (Exception exn) {
            throw new NodeExecutionException(this, Resources.getString("ConditionNotEvaluated"), exn, logger);
        }
        Edge elseEdge = (Edge) this.getProperty(HAS_ELSE_EDGE);
        if (elseEdge != null) {
            Node target = elseEdge.getTarget();
            comm.transition(this, target, getOutEdges().size() - 1, "else");
            return target;
        } else {
            throw new NodeExecutionException(this, "No given pattern matches!", logger);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {
        super.writeAttributes(out, uid_map);
        if (this.getBooleanProperty(HAS_ELSE_EDGE)) {
            Graph.printAtt(out, "boolean", HAS_ELSE_EDGE, "1");
        }
        /*        for (Edge edge : getOutEdges()) {
            String cond = edge.getCondition();
            Graph.printAtt(out, EXPRESSION_LIST, cond);
        }*/
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals(HAS_ELSE_EDGE)) {
            this.setProperty(name, value.equals("1"));
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    /** red for default, gray for all others */
    @Override
    public Color getPortColor(int portNumber) {
        return (this.getBooleanProperty(HAS_ELSE_EDGE) && portNumber == this.numEdges() - 1)
                ? new Color(255, 153, 153)
                : super.getPortColor(portNumber);
    }

    @Override
    public void writeVoiceXML(XMLWriter w, IdMap uid_map) throws IOException { }

}
