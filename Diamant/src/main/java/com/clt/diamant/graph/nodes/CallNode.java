package com.clt.diamant.graph.nodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.EdgeEvent;
import com.clt.diamant.graph.EdgeListener;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.gui.GUI;
import com.clt.gui.border.GroupBorder;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.util.StringTools;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class CallNode extends Node {

    private static final String PROCEDURE = "procedure";
    private static final String ARGUMENTS = "arguments";
    private static final String RETURNS = "returnvalues";

    private final EdgeListener edgeListener = new EdgeListener() {

        public void edgeAdded(EdgeEvent e) {

            ProcNode proc = CallNode.this.getProcedure();
            if (e.getSource() == proc) {
                CallNode.this.addEdge(proc.getPortName(e.getIndex()));
            }
        }

        public void edgeRemoved(EdgeEvent e) {

            if (e.getSource() == CallNode.this.getProcedure()) {
                CallNode.this.removeEdge(e.getIndex());
            }
        }

        public void edgeUpdated(EdgeEvent e) {

            ProcNode proc = CallNode.this.getProcedure();
            if (e.getSource() == proc) {
                Edge edge = CallNode.this.getEdge(e.getIndex());
                edge.setCondition(proc.getPortName(e.getIndex()));
                edge.setColor(proc.getPortColor(e.getIndex()));
            }
        }
    };

    public CallNode() {

        super();

        this.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {

                if (evt.getPropertyName().equals(CallNode.PROCEDURE)) {
                    ProcNode old_p = (ProcNode) evt.getOldValue();
                    if (old_p != null) {
                        old_p.removeEdgeListener(CallNode.this.edgeListener);
                    }

                    CallNode.this.removeAllEdges();
                    ProcNode p = (ProcNode) evt.getNewValue();
                    if (p != null) {
                        for (EndNode n : p.getEndNodes()) {
                            CallNode.this.addEdge(n.getTitle());
                        }

                        p.addEdgeListener(CallNode.this.edgeListener);
                    }
                }
            }
        });
    }

    public static Color getDefaultColor() {

        return new Color(255, 255, 153);
    }

    @Override
    protected void disposeImpl() {

        // destroy the link to the proc
        this.setProperty(CallNode.PROCEDURE, null);
    }

    public ProcNode getProcedure() {

        return (ProcNode) this.getProperty(CallNode.PROCEDURE);
    }

    @Override
    public void update(Mapping map) {

        super.update(map);
        ProcNode proc = this.getProcedure();
        if (proc != null) {
            ProcNode new_proc = (ProcNode) map.getNode(proc);
            if (new_proc != proc) {
                Node[] targets = new Node[this.numEdges()];
                for (int i = 0; i < this.numEdges(); i++) {
                    targets[i] = this.getEdge(i).getTarget();
                }
                this.setProperty(CallNode.PROCEDURE, new_proc);
                for (int i = 0; i < this.numEdges(); i++) {
                    this.getEdge(i).setTarget(targets[i]);
                }
            }
        }

        Map<Slot, String> arguments = this.getArgumentMap(this.properties, false);
        if (arguments != null) {
            Map<Slot, String> t = new Hashtable<Slot, String>();
            for (Slot v : arguments.keySet()) {
                Slot newVar = map.getVariable(v);
                t.put(newVar, arguments.get(v));
            }
            this.setProperty(CallNode.ARGUMENTS, t);
        }
    }

    public void updateEdgeNames() {

        ProcNode proc = this.getProcedure();
        if (proc != null) {
            List<? extends EndNode> endNodes = proc.getEndNodes();
            int i = 0;
            for (EndNode n : endNodes) {
                this.getEdge(i++).setCondition(n.getTitle());
            }
        }
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {

        final JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(6, 6, 6, 6);
        p.add(new JLabel(Resources.getString("Procedure") + ':'), gbc);
        gbc.gridx++;

        final JComboBox procPopup
                = NodePropertiesDialog.createComboBox(properties, CallNode.PROCEDURE,
                        this.getGraph().getProcedures());
        p.add(procPopup, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        final JPanel args = new JPanel();
        final JPanel returns = new JPanel();

        boolean useTabs = false;

        if (useTabs) {
            JTabbedPane jtp = new JTabbedPane();

            JScrollPane jsp
                    = new JScrollPane(args, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jsp.setBorder(BorderFactory.createEmptyBorder());
            jtp.add(jsp, Resources.getString("Arguments"));

            jsp
                    = new JScrollPane(returns, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jsp.setBorder(BorderFactory.createEmptyBorder());
            jtp.add(jsp, Resources.getString("ReturnVariables"));

            p.add(jtp, gbc);
        } else {
            JScrollPane jsp
                    = new JScrollPane(args, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {

                @Override
                public Dimension getPreferredSize() {

                    Dimension d = super.getPreferredSize();
                    d.height = Math.min(Math.max(d.height, 100), 250);
                    return d;
                }
            };
            jsp.setBorder(new GroupBorder(Resources.getString("Arguments")));
            p.add(jsp, gbc);

            gbc.gridy++;
            jsp
                    = new JScrollPane(returns, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {

                @Override
                public Dimension getPreferredSize() {

                    Dimension d = super.getPreferredSize();
                    d.height = Math.min(Math.max(d.height, 100), 250);
                    return d;
                }
            };
            jsp.setBorder(new GroupBorder(Resources.getString("ReturnVariables")));
            p.add(jsp, gbc);
        }

        if (properties.get(CallNode.ARGUMENTS) == null) {
            properties.put(CallNode.ARGUMENTS, new Hashtable<Slot, String>());
        }

        if (properties.get(CallNode.RETURNS) == null) {
            properties.put(CallNode.RETURNS, new Hashtable<Slot, String>());
        }

        final Map<String, String> editArgs = new Hashtable<String, String>();
        final Map<Slot, String> arguments = this.getArgumentMap(properties, false);
        if (arguments != null) {
            for (Slot s : arguments.keySet()) {
                editArgs.put(s.getName(), arguments.get(s));
            }
        }

        final Map<String, String> editReturns = new Hashtable<String, String>();
        final Map<Slot, String> returnValues = this.getReturnMap(properties, false);
        if (returnValues != null) {
            for (Slot s : returnValues.keySet()) {
                editReturns.put(s.getName(), returnValues.get(s));
            }
        }

        ActionListener l = new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                ProcNode proc = (ProcNode) procPopup.getSelectedItem();
                if (proc != null) {
                    CallNode.addSlotFields(args, Resources.getString("Expression"), proc
                            .getParameters(),
                            arguments, editArgs);
                    CallNode.addSlotFields(returns, Resources.getString("Pattern"),
                            proc.getReturnVariables(), returnValues, editReturns);

                    p.revalidate();

                    Window w = GUI.getWindowForComponent(p);
                    if (w != null) {
                        w.setSize(w.getPreferredSize());
                        w.repaint();
                    }
                }
            }
        };

        procPopup.addActionListener(l);
        l.actionPerformed(new ActionEvent(procPopup, ActionEvent.ACTION_PERFORMED,
                null));

        return p;
    }


    /*
   * We keep a separate nameMap in order to keep argument values when switching
   * to a procedure which has parameters of the same name.
     */
    private static void addSlotFields(JPanel p, String description,
            List<Slot> slots,
            final Map<Slot, String> slotMap, final Map<String, String> nameMap) {

        p.removeAll();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 6, 3, 6);
        for (int i = 0; i < slots.size(); i++) {
            final Slot slot = slots.get(i);
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            p.add(new JLabel(slot.getName() + ":"), gbc);
            final JTextField tf = new JTextField(20);
            tf.setText(nameMap.get(slot.getName()));
            slotMap.put(slot, tf.getText());
            tf.addCaretListener(new CaretListener() {

                public void caretUpdate(CaretEvent e) {

                    slotMap.put(slot, tf.getText());
                    nameMap.put(slot.getName(), tf.getText());
                }
            });
            gbc.gridx++;
            gbc.weightx = 1.0;
            p.add(tf, gbc);
        }
    }

    @Override
    public Color getPortColor(int portNumber) {

        ProcNode proc = this.getProcedure();
        if ((portNumber >= 0) && (portNumber < this.numEdges()) && (proc != null)) {
            // return (Color) proc.getEndNodeProperty("color", portNumber);
            return proc.getPortColor(portNumber);
        } else {
            return super.getPortColor(portNumber);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<Slot, String> getArgumentMap(Map<String, Object> properties,
            boolean create) {

        Map<Slot, String> map
                = (Map<Slot, String>) properties.get(CallNode.ARGUMENTS);
        if ((map == null) && create) {
            map = new Hashtable<Slot, String>();
            properties.put(CallNode.ARGUMENTS, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    protected Map<Slot, String> getReturnMap(Map<String, Object> properties,
            boolean create) {

        Map<Slot, String> map = (Map<Slot, String>) properties.get(CallNode.RETURNS);
        if ((map == null) && create) {
            map = new Hashtable<Slot, String>();
            properties.put(CallNode.RETURNS, map);
        }
        return map;
    }

    public Map<Slot, String> getArguments() {

        ProcNode proc = this.getProcedure();
        if (proc == null) {
            return new Hashtable<Slot, String>();
        }

        List<Slot> parameters = proc.getParameters();
        Map<Slot, String> arguments = this.getArgumentMap(this.properties, false);
        Map<Slot, String> args = new Hashtable<Slot, String>();

        for (int i = 0; i < parameters.size(); i++) {
            Slot s = parameters.get(i);
            String arg = arguments.get(s);
            if ((arg != null) && (arg.trim().length() > 0)) {
                args.put(s, arg);
            }
        }

        return args;
    }

    public Map<Slot, String> getReturnPatterns() {

        ProcNode proc = this.getProcedure();
        if (proc == null) {
            return new Hashtable<Slot, String>();
        }

        Map<Slot, String> usedPatterns = new Hashtable<Slot, String>();
        Map<Slot, String> returnPatterns
                = this.getReturnMap(this.properties, false);
        if (returnPatterns != null) {
            List<Slot> returnVariables = proc.getReturnVariables();

            for (int i = 0; i < returnVariables.size(); i++) {
                Slot s = returnVariables.get(i);
                String pattern = returnPatterns.get(s);
                if ((pattern != null) && (pattern.trim().length() > 0)) {
                    usedPatterns.put(s, pattern);
                }
            }
        }

        return usedPatterns;
    }

    @Override
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
        logNode(logger);

        ProcNode proc = this.getProcedure();
        if (proc == null) {
            throw new NodeExecutionException(this, Resources
                    .getString("UnspecifiedProcedureCall"), logger);
        }

        Map<Slot, Value> args = new Hashtable<Slot, Value>();
        Map<Slot, Value> returns = new Hashtable<Slot, Value>();

        Map<Slot, String> arguments = this.getArguments();
        for (Slot s : arguments.keySet()) {
            String arg = arguments.get(s);
            try {
                args.put(s, this.parseExpression(arg).evaluate(comm));
            } catch (Exception exn) {
                throw new NodeExecutionException(this, Resources.getString("ErrorWhileEvaluating")
                        + " " + arg + "'", exn, logger);
            }
        }

        Node target = proc.execute(comm, args, returns, input, logger);

        int index = proc.getEndNodeIndex(target);
        // if index == -1, we didn't reach an end node, which means a goto was
        // issued
        if (index >= 0) {
            target = this.getEdge(index).getTarget();
            comm.transition(this, target, index, null);

            // only evaluate return variables, if we are not jumping out from the
            // procedure
            Map<Slot, String> returnPatterns = this.getReturnPatterns();
            for (Slot s : returnPatterns.keySet()) {
                String pattern = returnPatterns.get(s);

                if ((pattern != null) && (pattern.trim().length() > 0)) {
                    try {
                        Pattern p = this.parsePattern(pattern);
                        Match match = p.match(returns.get(s));

                        if (match != null) {
                            List<Slot> accessible_vars
                                    = this.getGraph().getAllVariables(Graph.LOCAL);
                            for (Iterator<String> vars = match.variables(); vars.hasNext();) {
                                String name = vars.next();
                                Slot v = null;
                                for (int j = accessible_vars.size() - 1; (j >= 0)
                                        && (v == null); j--) {
                                    Slot variable = accessible_vars.get(j);
                                    if (name.equals(variable.getName())) {
                                        v = variable;
                                    }
                                }
                                if (v != null) {
                                    v.setValue(match.get(name));
                                } else {
                                    throw new NodeExecutionException(this,
                                            Resources.getString("BindNonExistingVariable")
                                            + name, logger);
                                }
                            }
                        } else {
                            throw new NodeExecutionException(this, "Return pattern "
                                    + pattern
                                    + " does not match return value "
                                    + returns.get(s), logger);
                        }
                    } catch (Exception exn) {
                        throw new NodeExecutionException(this,
                                "Error while evaluating return pattern '" + pattern
                                + "'", exn, logger);
                    }
                }
            }
        }

        return target;
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);
        ProcNode proc = this.getProcedure();
        if (proc == null) {
            this.reportError(errors, false, Resources
                    .getString("emptyProcedureReference"));
        } else if (!this.getGraph().getProcedures().contains(proc)) {
            this.reportError(errors, true, Resources.format(
                    "referencesInaccessibleProcedure",
                    proc.getTitle()));
        } else {
            List<Slot> parameters = proc.getParameters();
            Map<Slot, String> args = this.getArgumentMap(this.properties, false);
            if (args != null) {
                boolean typeClash = false;
                for (int i = 0; i < parameters.size(); i++) {
                    Slot param = parameters.get(i);
                    String arg = args.get(param);
                    if ((arg != null) && (arg.trim().length() > 0)) {
                        try {
                            Type t = this.parseExpression(arg).getType();
                            try {
                                Type.unify(t, param.getType());
                            } catch (Exception exn) {
                                typeClash = true;
                            }
                        } catch (Exception exn) {
                            this.reportError(errors, false, Resources.format(
                                    "containsIllegalArgument",
                                    param.getName())
                                    + ": " + exn.getLocalizedMessage());
                        }
                    }
                }
                if (typeClash) {
                    this
                            .reportError(
                                    errors,
                                    false,
                                    ": argument types do not match parameter types of procedure \""
                                    + proc.getTitle() + "\"");
                }
            }

            List<Slot> returns = proc.getReturnVariables();
            List<Slot> accessible_vars = this.getGraph().getAllVariables(Graph.LOCAL);

            Map<Slot, String> patterns = this.getReturnMap(this.properties, false);
            if (patterns != null) {
                boolean typeClash = false;
                for (int i = 0; i < returns.size(); i++) {
                    Slot param = returns.get(i);
                    String pattern = patterns.get(param);
                    if ((pattern != null) && (pattern.trim().length() > 0)) {
                        try {
                            Pattern p = this.parsePattern(pattern);
                            Map<String, Type> variablesTypes = new HashMap<String, Type>();
                            for (String varName : p.getFreeVars()) {
                                Slot v = null;
                                for (int j = accessible_vars.size() - 1; (j >= 0)
                                        && (v == null); j--) {
                                    Slot s = accessible_vars.get(j);
                                    if (varName.equals(s.getName())) {
                                        v = s;
                                        variablesTypes.put(varName, v.getType());
                                    }
                                }
                                if (v == null) {
                                    this.reportError(errors, false, Resources.format(
                                            "containsIllegalInputPattern",
                                            "Attempt to bind non existing variable "
                                            + varName));
                                }
                            }
                            Type t = p.getType(variablesTypes);
                            try {
                                Type.unify(t, param.getType());
                            } catch (Exception exn) {
                                typeClash = true;
                            }
                        } catch (Exception exn) {
                            this
                                    .reportError(
                                            errors,
                                            false,
                                            Resources
                                                    .format(
                                                            "containsIllegalReturnPattern", param.getName())
                                            + ": " + exn.getLocalizedMessage());
                        }
                    }
                }
                if (typeClash) {
                    this
                            .reportError(
                                    errors,
                                    false,
                                    ": pattern types do not match return value types of procedure \""
                                    + proc.getTitle() + "\"");
                }
            }

        }
    }

    @Override
    protected void readAttribute(final XMLReader r, String name,
            final String value,
            final IdMap uid_map)
            throws SAXException {

        if (name.equals(CallNode.PROCEDURE)) { // alte Loesung ueber Index, nur noch
            // aus Kompatibilitaetsgruenden
            if (!StringTools.isEmpty(value)) {
                // da Prozeduren verschraenkt rekursiv sein koennen, gibt es den
                // ProcNode moeglicherweise noch nicht. Daher stellen wir die
                // Referenzen erst am Ende des Parsens her:
                r.addCompletionRoutine(new XMLReader.CompletionRoutine() {

                    public void run()
                            throws SAXException {

                        Graph owner = CallNode.this.getGraph();
                        int i = 0;
                        while (i < value.length() ? value.charAt(i) == '^' : false) {
                            owner = owner.getSuperGraph();
                            i++;
                        }
                        try {
                            int index = Integer.parseInt(value.substring(i));
                            CallNode.this.setProcPointer((ProcNode) new ArrayList<Node>(owner
                                    .getNodes()).get(index));
                        } catch (NumberFormatException exn) {
                            r
                                    .raiseException(Resources
                                            .getString("IllegalProcID"));
                        }
                    }
                });
            }
        } else if (name.equals("proc_uid")) { // neue, bessere Loesung ueber UIDs
            if (!StringTools.isEmpty(value)) {
                // da Prozeduren verschraenkt rekursiv sein koennen, gibt es den
                // ProcNode moeglicherweise noch nicht. Daher stellen wir die
                // Referenzen erst am Ende des Parsens her:
                r.addCompletionRoutine(new XMLReader.CompletionRoutine() {

                    public void run()
                            throws SAXException {

                        try {
                            Node n = uid_map.nodes.get(value);
                            if (n instanceof ProcNode) {
                                CallNode.this.setProcPointer((ProcNode) n);
                                // jetzt muessen wir noch die Argumente richtig zuordnen
                                Map<?, String> tmp
                                        = (Map<?, String>) CallNode.this.getProperty("tmp_arguments");
                                if (tmp != null) {
                                    List<Slot> parameters = ((ProcNode) n).getParameters();
                                    Map<Slot, String> arguments = new Hashtable<Slot, String>();
                                    for (int i = 0; i < parameters.size(); i++) {
                                        String val = tmp.get(parameters.get(i).getName());
                                        if (val != null) {
                                            arguments.put(parameters.get(i), val);
                                        }
                                    }
                                    CallNode.this.setProperty(CallNode.ARGUMENTS, arguments);
                                    CallNode.this.setProperty("tmp_arguments", null);
                                }

                                // jetzt muessen wir noch die ReturnValues richtig zuordnen
                                tmp = (Map<?, String>) CallNode.this.getProperty("tmp_returns");
                                if (tmp != null) {
                                    List<Slot> returnVariables
                                            = ((ProcNode) n).getReturnVariables();
                                    Map<Slot, String> returnPatterns
                                            = new Hashtable<Slot, String>();
                                    for (int i = 0; i < returnVariables.size(); i++) {
                                        String pattern = tmp.get(returnVariables.get(i).getName());
                                        if (pattern != null) {
                                            returnPatterns.put(returnVariables.get(i), pattern);
                                        }
                                    }
                                    CallNode.this.setProperty(CallNode.RETURNS, returnPatterns);
                                    CallNode.this.setProperty("tmp_returns", null);
                                }
                            } else {
                                r.raiseException(Resources.format("notProcNodeErr", value));
                            }
                        } catch (NumberFormatException exn) {
                            r
                                    .raiseException(Resources
                                            .getString("IllegalProcID"));
                        }
                    }
                });
            }
        } else if (name.equals(CallNode.ARGUMENTS)) {
            final Map<String, String> tmp = new Hashtable<String, String>();
            this.setProperty("tmp_arguments", tmp);
            r.setHandler(new AbstractHandler("att") {

                @Override
                public void start(String name, Attributes atts) {

                    if (name.equals("att")) {
                        r.setHandler(new AbstractHandler("att")); // zu fruehes Aussteigen
                        // aus der Liste
                        // verhindern

                        String param_name = atts.getValue("name");
                        String arg_value = atts.getValue("value");
                        tmp.put(param_name, arg_value);
                    }
                }

                @Override
                protected void end(String name) {

                }
            });
        } else if (name.equals(CallNode.RETURNS)) {
            final Map<String, String> tmp = new Hashtable<String, String>();
            this.setProperty("tmp_returns", tmp);
            r.setHandler(new AbstractHandler("att") {

                @Override
                public void start(String name, Attributes atts) {

                    if (name.equals("att")) {
                        r.setHandler(new AbstractHandler("att")); // zu fruehes Aussteigen
                        // aus der Liste
                        // verhindern

                        String param_name = atts.getValue("name");
                        String pattern = atts.getValue("value");
                        tmp.put(param_name, pattern);
                    }
                }

                @Override
                protected void end(String name) {

                }
            });
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    public Map<ProcNode, Node> getFreeProcedures(Map<ProcNode, Node> h) {

        ProcNode p = this.getProcedure();
        if (p != null) {
            h.put(p, this);
        }
        return h;
    }

    private void setProcPointer(ProcNode n) {

        // need to save and restore edges because they are
        // deleted by the property listener for "procedure"
        Edge[] edges = new Edge[this.numEdges()];
        for (int k = 0; k < this.numEdges(); k++) {
            edges[k] = this.getEdge(k);
        }
        this.setProperty(CallNode.PROCEDURE, n);
        this.removeAllEdges();
        for (int k = 0; k < edges.length; k++) {
            this.addEdge(edges[k]);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        ProcNode p = this.getProcedure();
        if (p != null) {
            Graph.printAtt(out, "proc_uid", uid_map.nodes.put(p));

            List<Slot> parameters = p.getParameters();
            Map<Slot, String> arguments = this.getArgumentMap(this.properties, false);
            if (arguments != null) {
                Graph.printAtt(out, "list", CallNode.ARGUMENTS, null);
                for (Slot param : parameters) {
                    String arg = arguments.get(param);
                    if (arg != null) {
                        Graph.printAtt(out, param.getName(), arg);
                    }
                }
                out.closeElement("att");
            }

            List<Slot> returnVariables = p.getReturnVariables();
            Map<Slot, String> returnPatterns
                    = this.getReturnMap(this.properties, false);
            if (returnPatterns != null) {
                Graph.printAtt(out, "list", CallNode.RETURNS, null);
                for (Slot param : returnVariables) {
                    String pattern = returnPatterns.get(param);
                    if (pattern != null) {
                        Graph.printAtt(out, param.getName(), pattern);
                    }
                }
                out.closeElement("att");
            }
        }
    }

    @Override
    public String getDescription(Edge selectedEdge) {

        StringBuilder buffer
                = new StringBuilder(super.getDescription(selectedEdge));

        buffer.append("<p><b>" + this.html(Resources.getString("CallToProcedure"))
                + "</b> <tt>"
                + this.html(this.getProcedure().getTitle()) + "</tt></p>");

        return buffer.toString();
    }


}
