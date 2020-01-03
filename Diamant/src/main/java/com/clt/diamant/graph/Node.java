package com.clt.diamant.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.dialogos.plugin.Plugin;
import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.Device;
import com.clt.diamant.DialogOutput;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.Grammar;
import com.clt.diamant.IdMap;
import com.clt.diamant.IdentityObject;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.graph.nodes.GraphNode;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.graph.search.NodeSearchFilter;
import com.clt.diamant.graph.search.NodeSearchResult;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.EdgeConditionModel;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.diamant.graph.ui.NodeUI;
import com.clt.diamant.graph.ui.UIElement;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.util.StringTools;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import javax.swing.JTabbedPane;

/**
 * A node of a dialog graph. Each node has a list of ports through which it
 * connects to outgoing edges. You can add outgoing ports by calling the {@link #addEdge()
 * } method. The incoming and outgoing edges of the node are then created by
 * dragging edges in the GUI.<p>
 *
 * Node is an abstract base class. To implement your own node class, you need to
 * implement the methods {@link #execute(com.clt.diamant.WozInterface, com.clt.diamant.InputCenter, com.clt.diamant.ExecutionLogger)
 * }
 * and {@link #createEditorComponent(java.util.Map) }, which see below. You can
 * then define any behavior that you want within the execute method.<p>
 *
 * Nodes of your class have a <i>name</i>, which appears both in the node
 * palette on the right of the DialogOS window and as default labels of new
 * nodes of your class. The name is determined in a somewhat complex way, see {@link #getLocalizedNodeTypeName(java.lang.Class)
 * }. Node types may also have an <i>icon</i>, which appears next to the node
 * name in DialogOS. For the icon, DialogOS looks for a PNG file in the
 * classpath. If the fully qualified name of a node class is
 * com.myproject.MyNode, then the pathname of this file must be
 * com/myproject/MyNode.png. Under normal circumstances, "com" would then be a
 * subdirectory of src/main/resources.<p>
 *
 * Nodes also have <i>properties</i>, i.e. values which can be edited in the GUI
 * and will be saved with the dialog (e.g. the text a TTS node should speak). A
 * node class will typically declare some properties using {@link #setProperty(java.lang.String, java.lang.Object)
 * }. {@link #createEditorComponent(java.util.Map) } will create Swing
 * components for editing them using the methods in
 * {@link NodePropertiesDialog}.
 * {@link #execute(com.clt.diamant.WozInterface, com.clt.diamant.InputCenter, com.clt.diamant.ExecutionLogger) }
 * will then read out the values of properties using {@link #getProperty(java.lang.String) }
 * .<p>
 *
 *
 * A node is not useful by itself. It usually comes as part of a
 * <a href="https://github.com/dialogos-project/dialogos/wiki/Plugins">plugin</a>.
 * When the plugin is loaded, it can register its node types by calling {@link #registerNodeTypes(java.lang.Object, java.util.Collection) }
 * .<p>
 *
 * The UI for the node is handled by {@link com.clt.diamant.graph.ui.NodeUI}.
 */
public abstract class Node extends VisualGraphElement implements IdentityObject {

    public static final String TITLE = "title";

    private static Map<Object, List<Class<Node>>> NODE_TYPES = null;

    public static Map<Object, List<Class<Node>>> getAvailableNodeTypes() {
        if (Node.NODE_TYPES == null) {
            Node.registerBuiltinNodeTypes();
        }
        return Node.NODE_TYPES;
    }

    private static void registerBuiltinNodeTypes() {
        Node.NODE_TYPES = new LinkedHashMap<Object, List<Class<Node>>>();

        Node.registerBuiltinNodeTypes(Resources.getResources().createLocalizedString("NODEGROUP_DEFAULT"),
                new String[]{
                    "OutputNode",
                    "InputNode",
                    "ConditionalNode",
                    "SetVariableNode",
                    "TestVariableNode",
                    "SleepNode",
                    "ScriptNode",
                    "GroovyNode",
                    "EndNode"
                });

        Node.registerBuiltinNodeTypes(Resources.getResources().createLocalizedString("NODEGROUP_SUBGRAPH"),
                new String[]{
                    "GraphNode",
                    "ProcNode",
                    "CallNode",
                    "ReturnNode",
                    "ContinueNode",
                    "LoopNode"
                });

        Node.registerBuiltinNodeTypes(Resources.getResources().createLocalizedString("NODEGROUP_JUMP"),
                new String[]{
                    "LabelNode",
                    "GotoNode"
                });

    }

    @SuppressWarnings("unchecked")
    private static void registerBuiltinNodeTypes(Object key, String[] names) {
        List<Class<Node>> classes = new ArrayList<Class<Node>>();
        for (String name : names) {
            try {
                if (name == null) {
                    classes.add(null);
                } else {
                    Class<?> cls = Class.forName(Node.class.getPackage().getName() + ".nodes." + name);
                    if (Node.class.isAssignableFrom(cls)) {
                        classes.add((Class<Node>) cls);
                    }
                }
            } catch (Exception ignore) {
            }
        }
        Node.NODE_TYPES.put(key, classes);
    }

    /**
     * Registers the given node types with DialogOS, making them appear in the
     * node palette.<p>
     *
     * The "key" is an ID for a group of nodes. Nodes with the same key are kept
     * together in the node palette, and a separator line is drawn between nodes
     * with different keys. A good choice for the key is the plugin ID.
     *
     * @param key
     * @param nodeTypes
     */
    @SuppressWarnings("unchecked")
    public static void registerNodeTypes(Object key, Collection<Class<?>> nodeTypes) {
        if (Node.NODE_TYPES == null) {
            Node.registerBuiltinNodeTypes();
        }

        List<Class<Node>> nodes = Node.NODE_TYPES.get(key);
        if (nodes == null) {
            nodes = new ArrayList<Class<Node>>();
            Node.NODE_TYPES.put(key, nodes);
        }
        for (Class<?> cls : nodeTypes) {
            if (cls == null) {
                nodes.add(null);
            } else if (Node.class.isAssignableFrom(cls)) {
                nodes.add((Class<Node>) cls);
            } else {
                throw new IllegalArgumentException(cls + " does not inherit from Node");
            }
        }

    }

    private String id;
    private List<Edge> out_edges = new Vector<Edge>();
    private Set<Edge> in_edges = new HashSet<Edge>();
    private Graph owner = null;
    private boolean breakpoint = false;
    boolean reading = false;

    public boolean isReading() {
        return this.reading;
    }

    public boolean supportsEdges() {
        return true;
    }

    public static String getLocalizedNodeTypeName(Node n) {
        return Node.getLocalizedNodeTypeName(n.getClass());
    }

    public static String getLocalizedNodeTypeName(Class<? extends Node> c) {
        Class<?> parent = c;
        while (parent != null) {
            try {
                return (String) parent.getDeclaredMethod("getNodeTypeName", new Class[]{Class.class}).invoke(null, new Object[]{c});
            } catch (Exception exn) {
                parent = parent.getSuperclass();
            }
        }
        throw new RuntimeException("This was supposed to be unreachable (but apparently it is reachable)");
    }

    protected static String getNodeTypeName(Class<?> c) {
        if (c == null) {
            return null;
        }

        String name = c.getName();
        // cut off package name
        name = name.substring(name.lastIndexOf('.') + 1);
        if (name.endsWith("Node")) {
            name = name.substring(0, name.length() - 4);
        }
        return Resources.getString(name);
    }

    public Node() {
        Preferences prefs = Preferences.getPrefs();
        Color c = prefs.getDefaultNodeColor(this.getClass());

        if (c == null) {
            c = Color.lightGray;
        }

        this.setProperty(VisualGraphElement.COLOR, c);
        this.setTitle(Node.getLocalizedNodeTypeName(this));

        this.init();
    }

    @Override
    public final Node clone(Mapping map) {
        return this.clone(this.getClass(), map);
    }

    public Node clone(Class<? extends Node> targetClass, Mapping map) {
        Node n;
        try {
            n = targetClass.newInstance();
        } catch (Exception exn) {
            // this should never happen
            throw new IllegalArgumentException("Can't clone instances of " + targetClass.getName());
        }
        return this.cloneInto(n, map);
    }

    public Node cloneInto(Node n, Mapping map) {
        for (String key : this.propertyNames()) {
            n.setProperty(key, this.deep_copy(this.getProperty(key)));
        }

        n.removeAllEdges();
        for (int i = 0; i < this.numEdges(); i++) {
            Edge edge = this.getEdge(i);

            Edge e = edge.clone(n);
            n.addEdge(e);
            map.addEdge(edge, e);
        }

        /*
		 * String title = getTitle(); if (title.endsWith("copy"))
		 * n.setProperty("title", title + " 2"); else if
		 * (Character.isDigit(title.charAt(title.length()-1))) { int i =
		 * title.length()-1; while (i>4 ? Character.isDigit(title.charAt(i-1)) :
		 * false) i--; if (title.substring(0, i).endsWith("copy "))
		 * n.setProperty("title", title.substring(0, i) +
		 * (Integer.parseInt(title.substring(i)) + 1)); else
		 * n.setProperty("title", title + " copy"); } else
		 * n.setProperty("title", title + " copy");
         */
        map.addNode(this, n);
        return n;
    }

    public final void dispose() {
        this.disposeImpl();
    }

    // clean up any listeners etc.
    protected void disposeImpl() {

    }

    @Override
    public void update(Mapping map) {
        /*
		 * Edge[] edges = new Edge[numEdges()]; for (int i=0; i<numEdges(); i++)
		 * edges[i] = getEdge(i); removeAllEdges(); for (int i=0;
		 * i<edges.length; i++) { if (edges[i].getTarget() == null ||
		 * t_node.get(edges[i].getTarget()) == null)
		 * addEdge(edges[i].getTarget(), edges[i].getCondition()); else
		 * addEdge((Node) t_node.get(edges[i].getTarget()),
		 * edges[i].getCondition()); }
         */

        for (int i = 0; i < this.numEdges(); i++) {
            Edge e = this.getEdge(i);
            e.setTarget(map.getNode(e.getTarget()));
        }
    }

    @Override
    public Object getProperty(String key) {
        Object p = super.getProperty(key);
        /*
		 * if (p == null ? key.equals("title") : false) { p =
		 * Resources.getString("Untitled") + ' ' + (++gUntitledNodes);
		 * setProperty("title", p); }
         */
        return p;
    }

    public void updateEdges() {

    }

    private final void init() {
        // if (getTitle() == null)
        // setTitle(Resources.getString("Untitled") + ' ' + (++gUntitledNodes));

        this.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {

                if ((Node.this.getGraph() != null) && !evt.getPropertyName().equals("active")
                        && !evt.getPropertyName().equals("selected")
                        && !evt.getPropertyName().equals(VisualGraphElement.SIZE)
                        && !evt.getPropertyName().equals(EdgeConditionModel.EDGE_PROPERTY)) {
                    Node.this.getGraph().setDirty(true);
                }

                if (evt.getPropertyName().equals("numEdges")) {
                    for (int i = 0; i < Node.this.numEdges(); i++) {
                        Node.this.getEdge(i).setColor(Node.this.getPortColor(i));
                    }
                }
            }
        });

        this.setProperty("numEdges", new Integer(this.numEdges()));

        if (this.getProperty(VisualGraphElement.LOCATION) == null) {
            this.setProperty(VisualGraphElement.LOCATION, new Point(0, 0));
        }
    }

    public void setGraph(Graph g) {
        this.owner = g;
    }

    public final Graph getGraph() {
        return this.owner;
    }

    protected PluginSettings getPluginSettings(Class<? extends Plugin> plugin) {
        return this.getGraph().getOwner().getPluginSettings(plugin);
    }

    protected PluginRuntime getPluginRuntime(Class<? extends Plugin> plugin, WozInterface comm) {
        return this.getPluginSettings(plugin).getRuntime(comm);
    }

    protected Map<String, Type> getAccessibleVartypes() {
        Map<String, Type> h = new HashMap<String, Type>();
        Collection<Slot> vars = this.getGraph().getAllVariables(Graph.LOCAL);

        for (Slot v : vars) {
            h.put(v.getName(), v.getType());
        }

        return h;
    }

    // default: all ports are of the same color
    public Color getPortColor(int portNumber) {
        return new Color(153, 153, 255);
    }

    public boolean isOpaque() {
        return true;
    }

    public void setActive(boolean active) {
        this.setProperty("active", active ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isActive() {
        return this.getBooleanProperty("active");
    }

    /**
     * Remove the last edge of this node. The edge itself is not modified, it is
     * only unregistered from its source and target nodes.
     *
     * @return The edge that was removed.
     */
    public Edge removeEdge() {
        return this.removeEdge(this.out_edges.size() - 1);
    }

    /**
     * Remove the nth edge of this node. The edge itself is not modified, it is
     * only unregistered from its source and target nodes.
     *
     * @param index The index of the edge to remove.
     * @return The edge that was removed.
     */
    public Edge removeEdge(int index) {
        Edge e = this.out_edges.get(index);
        this.out_edges.remove(index);

        // edge is disconnected, so unregister with the target
        if (e.getTarget() != null) {
            e.getTarget().unregisterInEdge(e);
        }

        this.setProperty("numEdges", new Integer(this.numEdges()));
        return e;
    }

    /**
     * Adds an edge to the node. The "edge" has this node as the source and a
     * null target. It is visually represented as an outgoing port (little
     * triangle) of the node in the GUI, and can be connected to an actual
     * target node by drawing an edge.
     *
     * @return Return the reference to that edge.
     */
    public Edge addEdge() {
        return this.addEdge(new Edge(this));
    }

    /**
     * Adds an edge to the node.
     *
     * @param condition the label of the node
     * @return Return the reference to that edge.
     */
    public Edge addEdge(String condition) {
        return this.addEdge(new Edge(this, null, condition));
    }

    /**
     * Adds an edge to the node.
     *
     * @param edge the edge to be added
     * @return Return the reference to that edge.
     */
    public Edge addEdge(Edge edge) {
        if (edge.getSource() != this) {
            throw new IllegalArgumentException("Attempt to install foreign edge. Foreign owner: " + edge.getSource());
        }

        this.out_edges.add(edge);

        // edge is now connected, so register with the target, too
        if (edge.getTarget() != null) {
            edge.getTarget().registerInEdge(edge);
        }

        this.setProperty("numEdges", new Integer(this.numEdges()));

        return edge;
    }

    protected void moveEdge(int from, int to) {
        if (from != to) {
            Edge edge = this.out_edges.get(from);
            if (from < to) {
                for (int i = from; i < to; i++) {
                    this.out_edges.set(i, this.out_edges.get(i + 1));
                }
            } else {
                for (int i = from; i > to; i--) {
                    this.out_edges.set(i, this.out_edges.get(i - 1));
                }
            }
            this.out_edges.set(to, edge);
        }
        this.setProperty("numEdges", new Integer(this.numEdges()));
    }

    /**
     * Retrieves an outgoing edge of this node. The edge at the first outgoing
     * port is index 0, and so on.
     *
     * @param index
     * @return reference to the edge
     */
    public final Edge getEdge(int index) {
        return this.out_edges.get(index);
    }

    /**
     * Gets the number of the outgoing edges.
     *
     * @return number of the outgoing edges
     */
    public final int numEdges() {
        return this.out_edges.size();
    }

    public final Collection<Edge> edges() {
        return Collections.unmodifiableCollection(this.out_edges);
    }

    /**
     * Getter for the outgoing edges.
     *
     * @return List of the outgoing edges
     */
    public List<Edge> getOutEdges() {
        return out_edges;
    }

    /**
     * Register an incoming edge with this node. This method may only be called
     * from {@link Node} and {@link Edge} to satisfy the following invariant: An
     * edge is registered as an incoming edge of node <code>n</code> if and only
     * if its target is <code>n</code> and if it is also registered with its
     * source, i.e. the list of outgoing edges of the edge's source contains the
     * edge: e.source == n1 && n1.edges().contains(e) && e.target == n2 <=>
     * n2.in_edges().contains(e)
     */
    final void registerInEdge(Edge e) {
        this.in_edges.add(e);
    }

    final void unregisterInEdge(Edge e) {
        this.in_edges.remove(e);
    }

    public final Collection<Edge> in_edges() {
        return Collections.unmodifiableCollection(this.in_edges);
    }

    public final void removeAllEdges() {
        for (int i = this.numEdges(); i > 0; i--) {
            this.removeEdge();
        }
    }

    public boolean isConnected() {
        if (this.in_edges.size() > 0) {
            return true;
        }

        for (int i = 0; i < this.numEdges(); i++) {
            if (this.getEdge(i).getTarget() != null) {
                return true;
            }
        }

        return false;
    }

    public int getSelectionDistance(GraphSelectionModel selection, int max) {
        int d = 0;
        if (selection.contains(this)) {
            return 0;
        } else {
            Collection<Node> nodes = Collections.singleton(this);
            for (d = 1; d <= max; d++) {
                Collection<Node> s = new HashSet<Node>();
                for (Node n : nodes) {
                    for (Edge e : n.edges()) {
                        if (e.getTarget() != null) {
                            if (selection.contains(e.getTarget())) {
                                return d;
                            } else {
                                s.add(e.getTarget());
                            }
                        }
                    }
                    for (Edge e : n.in_edges()) {
                        Node src = e.getSource();
                        if (selection.contains(src)) {
                            return d;
                        } else {
                            s.add(src);
                        }
                    }
                }
                nodes = s;
            }
        }
        return Integer.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    protected Object deep_copy(Object o) {
        if (o instanceof Map) {
            Map m = (Map) o;
            Map copy = new Hashtable<Object, Object>(m.size() * 4 / 3, 0.75f);
            for (Object key : m.keySet()) {
                copy.put(key, this.deep_copy(m.get(key)));
            }
            return copy;
        } else if (o instanceof List) {
            List l = (List) o;
            List copy = new Vector(l.size());
            for (Object elem : l) {
                copy.add(this.deep_copy(elem));
            }
            return copy;
        } else if (o instanceof DialogOutput) {
            return ((DialogOutput) o).clone();
        } else {
            return o;
        }
    }

    /**
     * Opens the NodePropertiesDialog and saves the properties.
     *
     * @param parent Parent of the NodePropertiesDialog
     * @return true if the changes in the NodePropertiesDialog got approved
     */
    public boolean editProperties(Component parent) {
        Map<String, Object> props = (Map<String, Object>) this.deep_copy(this.properties);

        NodePropertiesDialog d = new NodePropertiesDialog(this, parent, props, this.createEditorComponent(props));
        d.setVisible(true);

        this.setProperty(NodePropertiesDialog.LAST_TAB, props.get(NodePropertiesDialog.LAST_TAB));
        this.setProperty(NodePropertiesDialog.LAST_SIZE, props.get(NodePropertiesDialog.LAST_SIZE));
        this.setProperty(NodePropertiesDialog.LAST_POSITION, props.get(NodePropertiesDialog.LAST_POSITION));

        if (d.approved()) {
            for (String key : props.keySet()) {
                if (!key.equals("numEdges")) {
                    this.setProperty(key, props.get(key));
                }
            }

            for (Iterator<String> it = this.properties.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                if (!props.containsKey(key)) {
                    it.remove();
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * This method is called by DialogOS whenever this node becomes the "active"
     * node during a run of the dialog graph. It thus performs the main
     * functionality of the node class. The method is expected to return the
     * node that should become active next. These nodes can be found by calling {@link #getEdge(int)
     * } for the chosen outgoing port and retrieving its {@link Edge#getTarget() }
     * .<p>
     *
     * An implementation of execute will typically want to access the value of a
     * node property that was set in the properties window of the node (e.g.,
     * the text that a TTS system should speak). You can access the node
     * properties by name using {@link #getProperty(java.lang.String) }
     * .<p>
     *
     * @param comm
     * @param input
     * @param logger ExecutionLogger that is used to log the node
     * @return the target node
     */
    abstract public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger);

    /**
     * Creates a Swing component for editing the properties of this node. This
     * method is called whenever the user opens the node's properties window (by
     * double-clicking on the node or by right-clicking and selecting
     * "Properties..."). The component returned by createEditorComponent should
     * be a {@link JTabbedPane}, which is added as tabs to the component. By
     * default, the properties window contains one tab with title "General"
     * which is added to the right of the implementation-specific tabs.<p>
     *
     * The JTabbedPane will typically contain Swing components for editing the
     * properties of the node. Properties are initially created by {@link #setProperty(java.lang.String, java.lang.Object)
     * }. You can create Swing components for properties by calling the methods
     * of {@link NodePropertiesDialog}. The values of such properties are
     * automatically updated when you close the properties window by clicking
     * "Ok". You can also update the values of properties by changing the
     * entries in the "properties" argument. (Do <i>not</i>
     * change the values of this.properties directly; this is necessary for
     * proper functioning of the "Cancel" button and proper propagation to
     * propertyChangeListeners.
     *
     * @return A reference on the created JComponent.
     */
    abstract public JComponent createEditorComponent(Map<String, Object> properties);

    public void validate(Collection<SearchResult> errors) {
        for (int i = 0; i < this.numEdges(); i++) {
            if (this.getEdge(i).getTarget() == null) {
                this.reportError(errors, false, Resources.getString("hasUnconnectedEdges"));
                break;
            }
        }
    }

    public Map<Slot, Node> getFreeVariables(Map<Slot, Node> h) {
        return h;
    }

    public Map<Device, Node> getFreeDevices(Map<Device, Node> h) {
        return h;
    }

    public Map<Grammar, Node> getFreeGrammars(Map<Grammar, Node> h) {
        return h;
    }

    public Map<ProcNode, Node> getFreeProcedures(Map<ProcNode, Node> h) {
        return h;
    }

    protected void reportError(Collection<SearchResult> b, boolean fatal, String msg) {
        StringBuilder path = this.nodePath(false);

        b.add(new NodeSearchResult(this, Resources.format("NodeNameSentence", path, msg),
                fatal ? SearchResult.Type.ERROR : SearchResult.Type.WARNING));
    }

    public StringBuilder nodePath(boolean includeDocument) {
        /*
		 * GraphOwner go = getGraph().getOwner(); if (go instanceof Node) {
		 * StringBuffer b = ((Node) go).nodePath(); b.append(':');
		 * b.append(getTitle()); return b; } else return new
		 * StringBuffer(getTitle());
         */
        StringBuilder b = this.getGraph().graphPath(includeDocument);
        if (b.length() > 0) {
            b.append(":");
        }
        b.append(this.getTitle());
        return b;
    }

    protected String html(Object o) {
        return StringTools.toHTML(o.toString());
    }

    public String getDescription(Edge selectedEdge) {
        StringBuffer buffer = new StringBuffer();
        // buffer.append("Type : " + getClass().getName() + "\n");
        // buffer.append("Name : " + getTitle() + "\n");
        buffer.append("<p>");
        buffer.append("<b>" + this.html(Resources.getString("Type")) + " : </b><tt>" + this.html(this.getClassName())
                + "</tt><br>");
        buffer.append("<b>" + this.html(Resources.getString("Name")) + " : </b><tt>" + this.getTitle() + "</tt>");
        buffer.append("</p>");
        return buffer.toString();
    }

    public final void write(final XMLWriter out, IdMap uid_map) {
        Point p = (Point) this.getProperty(VisualGraphElement.LOCATION);
        out.println("<graphics x=\"" + p.x + "\" y=\"" + p.y + "\" " + "fill=\""
                + StringTools.toHexString((Color) this.getProperty(VisualGraphElement.COLOR)) + "\"/>");

        // Graph.printAtt(out, "uid", uid_map.nodes.put(this));
        Graph.printAtt(out, "title", this.getTitle());
        if (!StringTools.isEmpty((String) this.getProperty(VisualGraphElement.COMMENT))) {
            Graph.printTextAtt(out, VisualGraphElement.COMMENT, (String) this.getProperty(VisualGraphElement.COMMENT));
        }
        if (this.breakpoint) {
            Graph.printAtt(out, "breakpoint", this.breakpoint);
        }

        this.writeAttributes(out, uid_map);
    }

    public final void read(final XMLReader r, final IdMap uid_map) {
        this.removeAllEdges();

        r.setHandler(new AbstractHandler("node") {
            @Override
            protected void start(String name, Attributes atts) throws SAXException {
                if (name.equals("graphics")) {
                    String s = atts.getValue("fill");
                    if (s != null) {
                        Node.this.setProperty(VisualGraphElement.COLOR, Color.decode(s));
                    }

                    Point p = new Point();
                    s = atts.getValue("x");
                    if (s != null) {
                        p.x = Integer.parseInt(s);
                    }
                    s = atts.getValue("y");
                    if (s != null) {
                        p.y = Integer.parseInt(s);
                    }
                    Node.this.setProperty(VisualGraphElement.LOCATION, p);
                } else if (name.equals("att")) {
                    final String n = atts.getValue("name");
                    if (n.equals("title")) {
                        Node.this.setTitle(atts.getValue("value"));
                    } else if (n.equals(VisualGraphElement.COMMENT)) {
                        if (atts.getValue("type").equals("text")) {
                            r.setHandler(new AbstractHandler("att") {

                                String value = null;

                                @Override
                                protected void end(String name) {

                                    if (name.equals("value")) {
                                        this.value = this.getValue();
                                    } else if (name.equals("att")) {
                                        if (this.value == null) {
                                            this.value = this.getValue();
                                        }
                                        Node.this.setProperty(n, this.value);
                                    }
                                }
                            });
                        } else {
                            Node.this.setProperty(n, atts.getValue("value"));
                        }
                    } else if (n.equals("uid")) {
                        Node.this.setId(atts.getValue("value"));
                        uid_map.nodes.put(Node.this);
                    } else if (n.equals("breakpoint")) {
                        Node.this.setBreakpoint(atts.getValue("value").equals("1"));
                    } else {
                        if (atts.getValue("type").equals("text")) {
                            r.setHandler(new AbstractHandler("att") {

                                String value;

                                @Override
                                protected void end(String name) throws SAXException {

                                    if (name.equals("value")) {
                                        this.value = this.getValue();
                                    } else if (name.equals("att")) {
                                        if (this.value == null) {
                                            this.value = this.getValue();
                                        }
                                        Node.this.readAttribute(r, n, this.value, uid_map);
                                    }
                                }
                            });
                        } else {
                            Node.this.readAttribute(r, n, atts.getValue("value"), uid_map);
                        }
                    }
                }
            }

            protected void end(String name) {

            }
        });
    }

    /**
     * Reads a setting from a graph file. This method is called whenever
     * DialogOS loads a previously saved graph from a file. It is called
     * individually for each attribute of your node that you saved with the writeAttributes
     * method. The name of the attribute is "name"; its value is "value".
     */
    @SuppressWarnings("unused")
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {

    }

    /**
     * Writes settings to the graph file. This method is called whenever
     * DialogOS saves the graph to a file. You can implement this method to save
     * persistent information for your node in this file. Use {@link Graph#printAtt(com.clt.xml.XMLWriter, java.lang.String, java.lang.Integer)
     * }
     * and its sister methods for writing to the XMLWriter conveniently.
     *
     * Write these settings to XML
     */
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    }

    public GraphOwner getMainOwner() {
        return this.getGraph().getMainOwner();
    }

    public String getTitle() {
        return (String) this.getProperty(Node.TITLE);
    }

    public void setTitle(String title) {
        this.setProperty(Node.TITLE, title);
    }

    public String toString() {
        return String.format("<%s id=%s title=%s>", super.toString(), getId(), getTitle());
    }

    public Collection<? extends SearchResult> find(NodeSearchFilter filter) {
        return filter.match(this);
    }

    public Expression parseExpression(String s) throws Exception {
        return this.getGraph().parseExpression(s);
    }

    public Pattern parsePattern(String s) throws Exception {
        return this.getGraph().parsePattern(s);
    }

    /**
     * Writes a representation of this node into a VoiceXML dialog
     * specification.<p>
     *
     * This is very old code, and nobody can remember how exactly to use it. See
     * issue #97. (AK, Oct 2018)
     *
     * @param w
     * @param uid_map
     * @throws IOException
     */
    abstract public void writeVoiceXML(XMLWriter w, IdMap uid_map) throws IOException;

    protected final void writeVoiceXMLGoto(XMLWriter w, IdMap uid_map, int edgeIndex) {
        Node target = this.getEdge(edgeIndex).getTarget();
        if (target != null) {
            w.printElement("goto", new String[]{"next"}, new String[]{"#node" + uid_map.nodes.put(target)});
        }
    }

    public static final String vxmlExp(String expression) {
        return expression;
    }

    public void exportVoiceXML(XMLWriter w, IdMap uid_map) throws IOException {
        w.openElement("form", new String[]{"id"}, new String[]{"node" + uid_map.nodes.put(this)});

        this.writeVoiceXML(w, uid_map);

        w.closeElement("form");
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBreakpoint() {
        return this.breakpoint;
    }

    public void setBreakpoint(boolean breakpoint) {
        if (this.breakpoint != breakpoint) {
            boolean oldValue = this.breakpoint;
            this.breakpoint = breakpoint;
            this.firePropertyChange("breakpoint", new Boolean(oldValue), new Boolean(breakpoint));
        }
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public UIElement createUI(GraphUI graphUI, MouseInputListener viewScroller) {
        return new NodeUI<>(graphUI, this, viewScroller);
    }

    /**
     * Logs this node.
     *
     * @param logger Logger in which the node is logged
     */
    public void logNode(ExecutionLogger logger) {
        if (this instanceof GraphNode) {
            logger.logGraphNode((GraphNode) this);
        } else {
            logger.logNode(this);
        }
    }

    /**
     * Is called when the "ok" button on this node is pressed - checks if the
     * changes made by the user are acceptable and does not allow them to be
     * saved if not.
     *
     * @return true if it is acceptable to save changes in the node
     */
    public boolean acceptableToSave() {
        return true;
    }

    /**
     * Sets variables of the graph according to a Match. This is used in an
     * input node (e.g. {@link AbstractInputNode}) after the input or the
     * recognition result have been matched against a pattern.
     *
     * @param match
     */
    protected void setVariablesAccordingToMatch(Match match) {
        List<Slot> accessible_vars = this.getGraph().getAllVariables(Graph.LOCAL);
        for (Iterator<String> vars = match.variables(); vars.hasNext();) {
            String name = vars.next();
            Slot v = null;
            for (int j = accessible_vars.size() - 1; (j >= 0)
                    && (v == null); j--) {
                Slot s = accessible_vars.get(j);
                if (name.equals(s.getName())) {
                    v = s;
                }
            }
            if (v != null) {
                v.setValue(match.get(name));
            } else {
                throw new NodeExecutionException(this, "Attempt to bind non existing variable " + name);  // TODO localize
            }
        }
    }
}
