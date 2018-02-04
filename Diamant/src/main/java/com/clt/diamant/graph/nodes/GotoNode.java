package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class GotoNode extends Node {

    private static final String TARGET = "target";

    public GotoNode() {

        super();

        // setTitle(Resources.getString("Goto"));
    }

    public static Color getDefaultColor() {

        return new Color(76, 76, 128);
    }

    @Override
    public void update(Mapping map) {

        super.update(map);

        this.setProperty(GotoNode.TARGET, map.getNode(this.getTarget()));
    }

    public LabelNode getTarget() {

        return (LabelNode) this.getProperty(GotoNode.TARGET);
    }

    @Override
    protected JComponent createEditorComponent(Map<String, Object> properties) {

        final JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(6, 6, 6, 6);
        p.add(new JLabel(Resources.getString("Label") + ':'), gbc);
        gbc.gridx++;

        final JComboBox labelPopup
                = NodePropertiesDialog.createComboBox(properties, GotoNode.TARGET,
                        this.getGraph().getNodes(LabelNode.class, false));
        p.add(labelPopup, gbc);

        return p;
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);
        LabelNode target = this.getTarget();
        if (target == null) {
            throw new NodeExecutionException(this, Resources
                    .getString("UnspecifiedGotoTarget"), logger);
        }
        /*
     * Graph srcGraph = getGraph(); Graph dstGraph = target.getGraph(); while
     * (srcGraph != dstGraph) { comm.subgraph(srcGraph.getOwner(), false);
     * srcGraph = srcGraph.getSuperGraph(); }
         */
        return target;
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);
        LabelNode label = this.getTarget();
        if (label == null) {
            this.reportError(errors, false, Resources
                    .getString("emptyLabelReference"));
        } else if (!this.getGraph().getNodes(LabelNode.class, false).contains(label)) {
            this.reportError(errors, true, Resources.format(
                    "referencesInaccessibleLabel",
                    label.getTitle()));
        }
    }

    @Override
    protected void readAttribute(final XMLReader r, String name,
            final String value,
            final IdMap uid_map)
            throws SAXException {

        if (name.equals("label_uid")) {
            if (!StringTools.isEmpty(value)) {
                // Das Label gibt es moeglicherweise noch nicht. Daher stellen
                // wir die Referenzen erst am Ende des Parsens her.
                r.addCompletionRoutine(new XMLReader.CompletionRoutine() {

                    public void run()
                            throws SAXException {

                        try {
                            Node n = uid_map.nodes.get(value);
                            if (n instanceof LabelNode) {
                                GotoNode.this.setProperty(GotoNode.TARGET, n);
                            } else {
                                r.raiseException(Resources.format("notLabelNodeErr", value));
                            }
                        } catch (NumberFormatException exn) {
                            r.raiseException(Resources
                                    .getString("IllegalLabelID"));
                        }
                    }
                });
            }
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        LabelNode label = this.getTarget();
        if (label != null) {
            Graph.printAtt(out, "label_uid", uid_map.nodes.put(label));
        }
    }

    @Override
    protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

        // vxml
    }
}
