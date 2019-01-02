package com.clt.diamant.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.AbstractVariable;
import com.clt.diamant.Device;
import com.clt.diamant.Document;
import com.clt.diamant.GroovyVariable;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.Grammar;
import com.clt.diamant.IdMap;
import com.clt.diamant.IdentityObject;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.SingleDocument;
import com.clt.diamant.Slot;
import com.clt.diamant.Version;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.nodes.CatchAllEdge;
import com.clt.diamant.graph.nodes.DialogSuspendedException;
import com.clt.diamant.graph.nodes.EndNode;
import com.clt.diamant.graph.nodes.GraphNode;
import com.clt.diamant.graph.nodes.InputNode;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.graph.nodes.StartNode;
import com.clt.diamant.graph.nodes.TimeoutEdge;
import com.clt.diamant.graph.search.GraphSearchResult;
import com.clt.diamant.graph.search.NodeSearchFilter;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.graph.ui.NodeUI;
import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.AssignmentDialog;
import com.clt.gui.FileChooser;
import com.clt.gui.OptionPane;
import com.clt.script.Environment;
import com.clt.script.Script;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.undo.AbstractEdit;
import com.clt.undo.Undo;
import com.clt.util.StringTools;
import com.clt.util.UserCanceledException;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class Graph implements IdentityObject {

    public static final boolean GLOBAL = false, LOCAL = true;

    private String id;
    private List<Grammar> grammars = null;
    private List<InputHandler> handlers = null;
    private Collection<Node> nodes = null;

    private List<Slot> variables = null;                      // variables for use in DialogOS or Groovy scripts
    private List<Functions> functions = null;

    private String groovyFunctions = null;
    private List<GroovyVariable> groovyOnlyVariables = null;  // variables for use in Groovy scripts

    private Collection<Comment> comments = null;
    private Script compiledScript = null;

    private StartNode startNode = null;
    private GraphOwner owner;

    private Collection<GraphListener> graphListeners = new ArrayList<GraphListener>();

    private int width = 0;
    private int height = 0;

    private Environment localEnvironment = new GraphEnvironment(this, true);
    private Environment globalEnvironment = new GraphEnvironment(this, false);

    public Graph(GraphOwner owner) {

        this.owner = owner;

        this.grammars = new ArrayList<Grammar>();
        this.handlers = new ArrayList<InputHandler>();
        this.nodes = new ArrayList<Node>();
        this.variables = new ArrayList<Slot>();
        this.groovyOnlyVariables = new ArrayList<GroovyVariable>();
        this.functions = new ArrayList<Functions>();
        this.groovyFunctions = new String();
        this.comments = new ArrayList<Comment>();

        this.init();
    }

    /**
     * Initializes the graph: a new start node is created an added into the
     * graph.
     */
    public Graph init() {

        if (this.startNode == null) {
            this.setSize(750, 540);
            this.startNode = new StartNode();
            this.startNode.setProperty("location", new Point(370, 40));
            this.add(this.startNode);
        }

        return this;
    }

    public Procedure toProcedure() {

        Procedure result = new Procedure(null);

        Mapping map = new Mapping();

        result.copy(this, map);
        result.update(map);

        return result;
    }

    public Graph clone(Mapping map) {

        System.out.println("clone");
        Graph g = new Graph(null);
        g.copy(this, map);
        return g;
    }

    protected Graph copy(Graph g, Mapping map) {

        // set size first, otherwise node additions will be constrained to wrong
        // size
        this.setSize(g.getWidth(), g.getHeight());

        for (Slot v : g.variables) {
            Slot newVar = v.clone(map);
            this.variables.add(newVar);
        }

        for (GroovyVariable e : g.groovyOnlyVariables) {
            GroovyVariable newGroovyVar = e.clone();
            this.groovyOnlyVariables.add(newGroovyVar);
        }

        for (Functions f : g.functions) {
            Functions fun = new Functions(f.getName(), f.getScript());
            this.functions.add(fun);
            map.addFunctions(f, fun);
        }

        this.groovyFunctions = g.groovyFunctions;

        for (Grammar grammar : g.grammars) {
            Grammar newGrammar = new Grammar(grammar.getName(), grammar.getGrammar());
            this.grammars.add(newGrammar);
            map.addGrammar(grammar, newGrammar);
        }

        for (InputHandler h : g.handlers) {
            InputHandler newHandler = h.clone(this, map);
            this.handlers.add(newHandler);
        }

        for (Node n : g.nodes) {
            Class<? extends Node> c = n.getClass();
            if (EndNode.class.isAssignableFrom(c)) {
                if (!this.supportsEndNode(c)) {
                    c = this.supportedEndNodes()[0];
                }
            }

            if (n instanceof StartNode) {
                n.cloneInto(this.startNode, map);
            } else {
                n = n.clone(c, map);

                this.add(n);
            }
        }

        return this;
    }

    public void update(Mapping map) {

        for (Node n : this.nodes) {
            n.update(map);
        }
    }

    public void updateEdges() {

        for (InputHandler h : this.handlers) {
            h.updateEdges();
        }

        for (Node n : this.nodes) {
            n.updateEdges();
        }
    }

    public void addGraphListener(GraphListener l) {

        this.graphListeners.add(l);
    }

    public void removeGraphListener(GraphListener l) {

        this.graphListeners.remove(l);
    }

    public void setSize(int width, int height) {

        this.width = width;
        this.height = height;

        for (GraphListener l : this.graphListeners) {
            l.sizeChanged(this, width, height);
        }
    }

    public int getWidth() {

        return this.width;
    }

    public int getHeight() {

        return this.height;
    }

    public void activateNode(Node node) {

        for (Node n : this.nodes) {
            n.setActive(node == n);
        }
    }

    public void rename(Component parent) {

        String s = OptionPane.edit(parent, Resources.getString("EnterName"), 
                Resources.getString("GraphName"), this.getOwner().getGraphName());
        if (s != null) {
            this.getOwner().setGraphName(s);
        }
    }

    public Collection<SearchResult> find(NodeSearchFilter filter) {

        Collection<SearchResult> matches = new ArrayList<SearchResult>();
        for (Node n : this.nodes) {
            matches.addAll(n.find(filter));
        }

        for (InputHandler h : this.handlers) {
            matches.addAll(h.getOwnedGraph().find(filter));
        }

        return matches;
    }

    protected void initVariables(WozInterface comm) {

        Environment env = this.getOwner().getEnvironment(Graph.GLOBAL);
        for (Slot v : this.variables) {
            try {
                v.instantiate(env, comm);
            } catch (Exception exn) {
                throw new RuntimeException(Resources.getString("InitialisationError")
                        + ".\n"
                        + Resources.getString("Slot") + ": " + v.getName() + "\n"
                        + Resources.getString("Value") + ": " + v.getInitValue());
            }
        }

    }

    protected void uninitVariables() {

        for (Slot v : this.variables) {
            v.uninstantiate();
        }
    }
    
    /**
     * Suspends execution of this dialog by throwing a
     * {@link DialogSuspendedException} with the current
     * dialog state.
     * 
     * @param currentNode 
     */
    public void suspend(Node currentNode) throws DialogSuspendedException {
        DialogState state = new DialogState(currentNode, variables, groovyOnlyVariables);
        DialogSuspendedException ex = new DialogSuspendedException(state);
        throw ex;
    }

    /**
     * Iterates over all nodes of the graph and executes them. Returns the
     * end-node after the graph was executed.
     *
     * @return The end-note that was reached at the end of the execution.
     */
    public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {

        Node node = this.startNode;

        for (Node n : this.getNodes()) {
            n.setActive(false);
        }

        this.initVariables(comm);
        if (!(this instanceof SubGraph)) {
            // We don't want to add the global variables of a subgraph to be
            // added to the global variables of the project.S
            List<AbstractVariable> allVariables = new ArrayList<AbstractVariable>(variables);
            allVariables.addAll(groovyOnlyVariables);
            logger.logInitialVariables(allVariables);
        }
        Node next;
        Node end = null;

        do {
            node.setActive(true);

            long time = System.currentTimeMillis();
            try {
                try {
                    comm.preExecute(node);
                    next = node.execute(comm, input, logger);
                } catch (NodeExecutionException|DialogSuspendedException exn) {
                    throw exn;
                } catch (Exception exn) {
                    throw new NodeExecutionException(node, Resources.getString("UnexpectedError"), exn, logger);
                }
                
                time = System.currentTimeMillis() - time;

                try {
                    Thread.sleep(Math.max(comm.getDelay() - time, 0));
                } catch (InterruptedException exn) {
                }
            } finally {
                node.setActive(false);
            }

            if (next == null) {
                if (node instanceof EndNode) {
                    end = node;
                } else {
                    throw new NodeExecutionException(node, Resources
                            .getString("EdgeNotConnected"), logger);
                }
            } else {
                node = next;
                if (node.getGraph() != this) {
                    return node;
                }
            }
        } while (next != null);

        this.uninitVariables();

        return end;
    }

    public void setOwner(GraphOwner owner) {

        this.owner = owner;
    }

    public GraphOwner getOwner() {

        return this.owner;
    }

    public GraphOwner getMainOwner() {

        Graph g = this;
        while (g != null) {
            GraphOwner owner = g.getOwner();
            if (owner == null) {
                return null;
            } else {
                g = owner.getSuperGraph();
                if (g == null) {
                    return owner;
                }
            }
        }
        return null;
    }

    public boolean isReadOnly() {

        GraphOwner owner = this.getMainOwner();
        if (owner instanceof Document) {
            return ((Document) owner).isReadOnly();
        } else {
            return false;
        }
    }

    public Graph getSuperGraph() {

        if (this.getOwner() != null) {
            return this.getOwner().getSuperGraph();
        } else {
            return null;
        }
    }

    public void addComment(Comment note) {

        this.comments.add(note);
        for (GraphListener l : this.graphListeners) {
            l.elementAdded(this, note);
        }
        this.setDirty(true);
    }

    public void removeComment(Comment note) {

        this.comments.remove(note);
        for (GraphListener l : this.graphListeners) {
            l.elementRemoved(this, note);
        }
        this.setDirty(true);
    }

    public Collection<Comment> getComments() {

        return Collections.unmodifiableCollection(this.comments);
    }

    public StartNode getStartNode() {

        return this.startNode;
    }

    @SuppressWarnings("unchecked")
    public Class<Node>[] supportedEndNodes() {

        return new Class[]{EndNode.class};
    }

    public boolean supportsEndNode(Class<?> c) {

        Class<Node> cs[] = this.supportedEndNodes();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i].equals(c)) {
                return true;
            }
        }
        return false;
    }

    public void add(Node n) {

        this.nodes.add(n);

        n.setGraph(this);

        for (GraphListener l : this.graphListeners) {
            l.elementAdded(this, n);
        }

        this.setDirty(true);
    }

    public void nameChanged() {

        for (GraphListener l : new ArrayList<GraphListener>(this.graphListeners)) {
            l.graphRenamed(this, this.getName());
        }
    }

    public String getName() {

        if (this.getOwner() == null) {
            return "";
        } else {
            return this.getOwner().getGraphName();
        }
    }

    public List<Functions> getFunctions() {

        return this.functions;
    }

    public String getGroovyScript() {

        return this.groovyFunctions;
    }

    public void setFunctions(List<Functions> functions) {

        this.functions = functions;
        this.compiledScript = null;
    }

    public void setGroovyFunctions(String groovyFunctions) {

        this.groovyFunctions = groovyFunctions;
    }

    public Script getCompiledScript()
            throws Exception {

        if (this.compiledScript == null) {
            StringBuilder b = new StringBuilder();
            for (Iterator<Functions> it = this.getFunctions().iterator(); it
                    .hasNext();) {
                b.append(it.next().getScript());
                b.append("\n");
            }

            String script = b.toString().trim();

            if (script.length() == 0) {
                this.compiledScript = new Script();
            } else {
                this.compiledScript = new Script(); // important to assign a script
                // in order to avoid recursion
                try {
                    this.compiledScript
                            = Script.parseFunctions(script, this.getEnvironment(Graph.LOCAL));
                } catch (Exception exn) {
                    this.compiledScript = null;
                    throw exn;
                }
            }
        }
        return this.compiledScript;
    }

    public List<Slot> getVariables() {

        return this.variables;
    }

    public List<Slot> getAllVariables(boolean scope) {

        List<Slot> vars;

        if (this.getSuperGraph() != null) {
            vars = this.getSuperGraph().getAllVariables(Graph.GLOBAL);
        } else {
            vars = new ArrayList<Slot>();
        }

        for (Slot v : this.variables) {
            if (v.isExport() || (scope == Graph.LOCAL)) {
                vars.add(v);
            }
        }

        return vars;
    }

    public void delete(GraphElement e, Undo undo) {

        if (e instanceof Comment) {
            final Comment c = (Comment) e;
            undo.addEdit(new AbstractEdit(Resources.getString("DeleteComment")) {

                public void run() {

                    Graph.this.removeComment(c);
                }

                public void unrun() {

                    Graph.this.addComment(c);
                }
            });
        } else if (e instanceof Node) {

        }
    }

    public void remove(Node n) {

        this.remove(n, true);
    }

    private void remove(Node n, boolean keepStartNode) {

        if ((n == this.startNode) && keepStartNode) {
            return;
        }

        if (n.getGroup() != null) {
            n.getGroup().remove(n);
        }

        // disconnect incoming edges
        // must copy the collection of edges, because it will be modified by our
        // action
        for (Edge e : new ArrayList<Edge>(n.in_edges())) {
            e.setTarget(null);
        }

        // disconnect outgoing edges
        for (Edge e : n.edges()) {
            e.setTarget(null);
        }

        this.nodes.remove(n);
        n.setGraph(null);

        for (GraphListener l : this.graphListeners) {
            l.elementRemoved(this, n);
        }

        this.setDirty(true);
    }

    public Collection<Node> getNodes() {

        return Collections.unmodifiableCollection(this.nodes);
    }

    public Collection<ProcNode> getProcedures() {

        /*
     * Vector v; if (getSuperGraph() != null) v =
     * getSuperGraph().getProcedures(); else v = new Vector(); for (int i=0;
     * i<nodes.size(); i++) if (nodes.elementAt(i) instanceof ProcNode)
     * v.addElement(nodes.elementAt(i)); return v;
         */
        return this.getNodes(ProcNode.class, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> Collection<T> getNodes(Class<T> cls,
            boolean localNodesOnly) {

        Collection<T> v;
        if ((this.getSuperGraph() != null) && !localNodesOnly) {
            v = this.getSuperGraph().getNodes(cls, localNodesOnly);
        } else {
            v = new ArrayList<T>();
        }

        for (Node n : this.nodes) {
            if (cls.isInstance(n)) {
                v.add((T) n);
            }
        }
        return v;
    }

    protected void writeVariables(XMLWriter out, IdMap uid_map) {

        for (Slot v : this.variables) {
            v.write(out, uid_map, "variable");
        }
    }

    protected void writeGroovyVariables(XMLWriter out, IdMap uid_map) {
        for (GroovyVariable e : this.groovyOnlyVariables) {
            e.write(out, uid_map, "groovyVariable");
        }
    }

    public void save(XMLWriter out, IdMap uid_map) {

        out.openElement("graph", new String[]{"directed", "Graphic", "Layout"},
                new Object[]{
                    "1", "1", "points"});

        Graph.printAtt(out, "width", this.getWidth());
        Graph.printAtt(out, "height", this.getHeight());

        for (Functions f : this.functions) {
            out.openElement("script");
            out.printElement("name", f.getName());
            out.printElement("value", f.getScript());
            out.closeElement("script");
        }

        out.printElement("groovyFunctions", this.groovyFunctions);

        for (Grammar g : this.grammars) {
            out.openElement("grammar", new String[]{"id"},
                    new Object[]{uid_map.grammars
                                .put(g)});
            out.printElement("name", g.getName());
            out.printElement("value", g.getGrammar());
            out.closeElement("grammar");
        }

        this.writeVariables(out, uid_map);

        this.writeGroovyVariables(out, uid_map);

        for (InputHandler h : this.handlers) {
            h.save(out, uid_map);
        }

        // sort nodes by UID
        Collection<String> sortedNodeUIDs = new TreeSet<String>();
        for (Node n : this.nodes) {
            sortedNodeUIDs.add(uid_map.nodes.put(n));
        }

        for (String uid : sortedNodeUIDs) {
            Node n = uid_map.nodes.get(uid);
            String clss = n.getClassName();
            out.openElement("node", new String[]{"uid", "class"}, new Object[]{
                uid, clss});
            n.write(out, uid_map);
            out.closeElement("node");
        }

        for (String uid : sortedNodeUIDs) {
            Node n = uid_map.nodes.get(uid);
            for (Edge edge : n.edges()) {
                edge.write(out, uid_map);
            }
        }

        Collection<Group> groups = new ArrayList<Group>();
        for (Node n : this.nodes) {
            Group g = Group.getTopGroup(n);
            if ((g != null) && !groups.contains(g)) {
                groups.add(g);
                this.printGroup(g, out, uid_map);
            }
        }

        for (Comment comment : this.getComments()) {
            out.printElement("comment", new String[]{"x", "y", "width", "height",
                "color"},
                    new String[]{String.valueOf(comment.getX()),
                        String.valueOf(comment.getY()),
                        String.valueOf(comment.getWidth()),
                        String.valueOf(comment.getHeight()),
                        StringTools.toHexString(comment.getColor())}, comment
                    .getComment());
        }

        out.closeElement("graph");
    }

    private void printGroup(Group g, XMLWriter out, IdMap uid_map) {

        out.openElement("group");
        for (GroupElement o : g) {
            if (o instanceof Group) {
                this.printGroup((Group) o, out, uid_map);
            } else if (o instanceof NodeUI) {
                // out.printElement("noderef", new String[] { "uid" }, new
                // Object[] {((NodeUI) o).getNode().getProperty("uid")});
                Node n = ((NodeUI<?>) o).getNode();
                out.printElement("noderef", new String[]{"uid"},
                        new Object[]{uid_map.nodes
                                    .put(n)});
            }
        }
        out.closeElement("group");
    }

    public void read(XMLReader r, Runnable completionRoutine, IdMap uid_map) {

        this.variables.clear();
        this.handlers.clear();
        this.comments.clear();

        List<Node> nodes = new ArrayList<Node>(this.nodes);
        for (Node n : nodes) {
            this.remove(n, false);
        }

        this.startNode = null;

        r.setHandler(this.getGraphHandler(r, completionRoutine, uid_map));
    }

    protected XMLHandler getGraphHandler(XMLReader r, Runnable completionRoutine,
            IdMap uid_map) {

        return new GraphHandler(r, completionRoutine, uid_map);
    }

    private Group readGroup(final XMLReader r, final IdMap uid_map) {

        final Group group = Group.group(null);
        r.setHandler(new AbstractHandler("group") {

            public void start(String name, Attributes atts) {

                if (name.equals("group")) {
                    group.add(Graph.this.readGroup(r, uid_map));
                } else if (name.equals("noderef")) {
                    Node n = uid_map.nodes.get(atts.getValue("uid"));
                    group.add(n);
                }
            }
        });
        return group;
    }

    public void setDirty(boolean dirty) {

        if (this.owner != null) {
            this.owner.setDirty(dirty);
        }
    }

    protected void validateVariables(Collection<SearchResult> errors) {

        Environment env = this.getOwner().getEnvironment(Graph.GLOBAL);

        for (Slot v : this.variables) {
            try {
                if (!StringTools.isEmpty(v.getInitValue())) {
                    Type.unify(v.getType(), Expression.parseExpression(v.getInitValue(),
                            env)
                            .getType());
                }
            } catch (Exception exn) {
                String graphName = this.graphPath(false).toString();
                errors.add(new GraphSearchResult(this, graphName, Resources.format(
                        "IllegalInitValue", v.getName())
                        + ": " + exn.getLocalizedMessage(), SearchResult.Type.WARNING));
            }
        }

        Set<String> s = new HashSet<String>();
        for (Slot slot : this.getAllVariables(Graph.LOCAL)) {
            String name = slot.getName();
            if (!s.add(name)) {
                String graphName = this.graphPath(false).toString();
                errors.add(new GraphSearchResult(this, graphName, Resources.format(
                        "DuplicateVariableDefinition", name), SearchResult.Type.WARNING));
            }
        }
    }

    public StringBuilder graphPath(boolean includeDocument) {

        GraphOwner go = this.getOwner();
        if (go instanceof Node) {
            return ((Node) go).nodePath(includeDocument);
        } else {
            StringBuilder b = new StringBuilder();
            if (includeDocument) {
                b.append(this.getOwner().getGraphName());
            }
            return b;
        }
    }

    public void validate(Collection<SearchResult> errors,
            ProgressListener progress)
            throws IllegalStateException {

        ProgressEvent evt
                = new ProgressEvent(this, Resources.format("ValidatingX", this.getName())
                        + "...", 0, this.grammars.size() + this.handlers.size()
                        + this.nodes.size(), 0);

        for (Grammar g : this.grammars) {
            g.validate(errors);
            evt.setCurrent(evt.getCurrent() + 1);
            if (progress != null) {
                progress.progressChanged(evt);
            }
        }

        for (InputHandler h : this.handlers) {
            h.validate(errors);
            evt.setCurrent(evt.getCurrent() + 1);
            if (progress != null) {
                progress.progressChanged(evt);
            }
        }

        this.validateVariables(errors);

        String graphName = this.graphPath(false).toString();
        try {
            Script s = this.getCompiledScript();
            Collection<String> warnings = new ArrayList<String>();
            s.check(warnings);
            // TODO localize
            for (String warning : warnings) {
                errors.add(new GraphSearchResult(this, graphName, "Functions Warning: "
                        + warning,
                        SearchResult.Type.WARNING));
            }
        } catch (Exception exn) {
            errors.add(new GraphSearchResult(this, graphName,
                    "Functions contain an error: "
                    + exn.getLocalizedMessage(), SearchResult.Type.WARNING));
        }

        for (Node n : this.nodes) {
            // long t = System.currentTimeMillis();
            n.validate(errors);
            // t = System.currentTimeMillis() - t;
            // System.out.println(nodes.elementAt(i).getClass().getName() + ": "
            // + t + "ms");
            evt.setCurrent(evt.getCurrent() + 1);
            if (progress != null) {
                progress.progressChanged(evt);
            }
        }
    }

    public Map<Slot, Node> getFreeVariables(Map<Slot, Node> h) {

        for (Node n : this.nodes) {
            h = n.getFreeVariables(h);
        }

        for (Slot v : this.variables) {
            h.remove(v);
        }

        return h;
    }

    public Map<Device, Node> getFreeDevices(Map<Device, Node> h) {

        for (Node n : this.nodes) {
            h = n.getFreeDevices(h);
        }

        return h;
    }

    public Map<Grammar, Node> getFreeGrammars(Map<Grammar, Node> h) {

        for (Node n : this.nodes) {
            h = n.getFreeGrammars(h);
        }

        return h;
    }

    public List<Grammar> getGrammars() {

        return this.grammars;
    }

    public void setGrammars(List<Grammar> grammars) {

        this.grammars = grammars;
    }

    public boolean supportsHandlers() {

        return true;
    }

    public List<InputHandler> getHandlers() {

        return this.handlers;
    }

    public List<Grammar> getLocalGrammars() {

        List<Grammar> v
                = this.getSuperGraph() != null ? this.getSuperGraph().getLocalGrammars()
                : new ArrayList<Grammar>();
        v.addAll(this.getGrammars());
        return v;
    }

    public List<InputHandler> getPrefixHandlers() {

        List<InputHandler> prefixHandlers = new ArrayList<InputHandler>();
        List<InputHandler> beforeLocal = new ArrayList<InputHandler>();

        for (InputHandler h : this.getHandlers()) {
            if (h.getType() == InputHandler.BEFORE_ALL) {
                prefixHandlers.add(h);
            } else if (h.getType() == InputHandler.BEFORE_LOCAL) {
                beforeLocal.add(h);
            }
        }

        if (this.getSuperGraph() != null) {
            prefixHandlers.addAll(this.getSuperGraph().getPrefixHandlers());
        }

        prefixHandlers.addAll(beforeLocal);

        return prefixHandlers;
    }

    public List<InputHandler> getPostfixHandlers() {

        List<InputHandler> postfixHandlers = new ArrayList<InputHandler>();
        List<InputHandler> afterAll = new ArrayList<InputHandler>();

        for (InputHandler h : this.getHandlers()) {
            if (h.getType() == InputHandler.AFTER_LOCAL) {
                postfixHandlers.add(h);
            } else if (h.getType() == InputHandler.AFTER_ALL) {
                afterAll.add(h);
            }
        }

        if (this.getSuperGraph() != null) {
            postfixHandlers.addAll(this.getSuperGraph().getPostfixHandlers());
        }

        postfixHandlers.addAll(afterAll);

        return postfixHandlers;
    }

    public Map<ProcNode, Node> getFreeProcedures(Map<ProcNode, Node> h) {

        for (Node n : this.nodes) {
            h = n.getFreeProcedures(h);
        }

        for (Node n : this.nodes) {
            if (n instanceof ProcNode) {
                h.remove(n);
            }
        }

        if (this.getOwner() instanceof ProcNode) {
            h.remove(this.getOwner());
        }

        return h;
    }

    public void importGraph(Component parent) {

        File f = new FileChooser().standardGetFile(parent);
        if (f != null) {
            Document doc = null;
            try {
                doc = Document.load(f);
                if (doc instanceof SingleDocument) {
                    SingleDocument d = (SingleDocument) doc;

                    Graph g = d.getOwnedGraph();

                    Collection<Device> devices = this.getOwner().getDevices();
                    List<Device> targetDevices = new ArrayList<Device>(devices);
                    List<Device> importDevices = new ArrayList<Device>(g.getFreeDevices(
                            new Hashtable<Device, Node>()).keySet());

                    Mapping map = new Mapping();

                    for (int i = importDevices.size() - 1; i >= 0; i--) {
                        for (int j = devices.size() - 1; j >= 0; j--) {
                            Device d1 = importDevices.get(i);
                            Device d2 = targetDevices.get(j);
                            if (d1.equals(d2)) {
                                if (d1 != d2) {
                                    map.addDevice(d1, d2);
                                }
                                importDevices.remove(i);
                                targetDevices.remove(j);
                            }
                        }
                    }

                    if (importDevices.size() > 0) {
                        Map<Device, Device> t = AssignmentDialog.getAssignment(parent,
                                importDevices, targetDevices);
                        if (t == null) {
                            throw new UserCanceledException();
                        }

                        for (int i = 0; i < importDevices.size(); i++) {
                            Device device = importDevices.get(i);
                            if (t.get(device) == null) {
                                Device dev_new = new Device(device.getName());
                                dev_new.setConnector(device.getConnector().copy());
                                devices.add(dev_new);
                                t.put(device, dev_new);
                            }
                        }

                        for (Device key : t.keySet()) {
                            map.addDevice(key, t.get(key));
                        }
                    }

                    g.update(map);

                    Node n;
                    if (g instanceof Procedure) {
                        n = new ProcNode((Procedure) g);
                    } else if (g instanceof SubGraph) {
                        n = new GraphNode((SubGraph) g);
                    } else {
                        n
                                = new GraphNode((SubGraph) new SubGraph(null).copy(g, new Mapping()));
                    }
                    /*
           * else { int result = OptionPane.showOptionDialog(parent,
           * Resources.getString("ProcOrGraph"),
           * Resources.getString("ImportOptions"), OptionPane.YES_NO_OPTION,
           * OptionPane.QUESTION, null, new String[] {
           * Resources.getString("Procedure"), Resources.getString("Subgraph")
           * }, null); if (result == OptionPane.YES) n = new ProcNode(g); else n
           * = new GraphNode(g); }
                     */
                    n.setTitle(f.getName());
                    this.add(n);

                } else {
                    throw new Exception(Resources.getString("NotWizardDocument")) {

                        public String toString() {

                            return this.getMessage();
                        }
                    };
                }
            } catch (UserCanceledException exn) {
            } catch (Exception exn) {
                if (Version.DEBUG) {
                    exn.printStackTrace();
                }
                OptionPane
                        .error(parent,
                                new String[]{
                                    Resources.format("CouldNotImportFile", f.getName()),
                                    exn.toString()});
            }
        }
    }

    private static void printAtt(XMLWriter out, String type, String name,
            Object value,
            boolean close) {
        if (close) {
            out.printElement("att", new String[]{"name", "type", "value"},
                    new Object[]{name,
                        type, value}, null);
        } else {
            out.openElement("att", new String[]{"name", "type", "value"},
                    new Object[]{name,
                        type, value});
        }
    }

    public static void printAtt(XMLWriter out, String type, String name,
            Object value) {
        Graph.printAtt(out, type, name, value, value != null);
    }

    public static void printTextAtt(XMLWriter out, String name, String value) {
        Graph.printAtt(out, "text", name, null, false);
        out.printElement("value", value);
        out.closeElement("att");
        // out.printElement("att", new String[] { "name", "type" },
        // new Object[] { name, "text" },
        // value);
    }

    public static void printAtt(XMLWriter out, String name, String value) {
        Graph.printAtt(out, "string", name, value, true);
    }

    public static void printAtt(XMLWriter out, String name, Integer value) {
        Graph.printAtt(out, name, value.intValue());
    }

    public static void printAtt(XMLWriter out, String name, Long value) {
        Graph.printAtt(out, name, value.longValue());
    }

    public static void printAtt(XMLWriter out, String name, long value) {
        Graph.printAtt(out, "integer", name, String.valueOf(value), true);
    }

    public static void printAtt(XMLWriter out, String name, boolean value) {
        Graph.printAtt(out, "boolean", name, value ? "1" : "0", true);
    }

    /** check for existance of class with the given name */
    private static boolean existsClass(String clss) {
        try {
            Class.forName(clss);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Class getNodeClassForName(String clss) throws ClassNotFoundException {
        if (existsClass(clss))
            return Class.forName(clss);
        if (existsClass("com.clt.diamant." + clss))
            return Class.forName("com.clt.diamant." + clss);
        if (existsClass("com.clt.diamant.graph.nodes." + clss))
            return Class.forName("com.clt.diamant.graph.nodes." + clss);
        System.err.println("Attempting to substitute class " + clss);
        if ("com.clt.dialogos.tts.TTSNode".equals(clss))
            return Class.forName("de.saar.coli.dialogos.marytts.plugin.TTSNode");
        if ("com.clt.dialogos.vocon.VoconNode".equals(clss))
            return Class.forName("edu.cmu.lti.dialogos.sphinx.plugin.SphinxNode");
        return null;
    }

    

    protected class GraphHandler extends AbstractHandler {

        XMLReader r;
        Runnable completionRoutine;
        IdMap uid_map;
        Attributes atts;

        public GraphHandler(XMLReader r, Runnable completionRoutine, IdMap uid_map) {
            super("graph");

            this.r = r;
            this.completionRoutine = completionRoutine;
            this.uid_map = uid_map;
        }

        public void start(String name, Attributes atts)
                throws SAXException {

            if (name.equals("att")) {
                if (atts.getValue("name").equals("width")) {
                    Graph.this.setSize(Integer.parseInt(atts.getValue("value")),
                            Graph.this.getHeight());
                } else if (atts.getValue("name").equals("height")) {
                    Graph.this.setSize(Graph.this.getWidth(), Integer.parseInt(atts
                            .getValue("value")));
                }
            } else if (name.equals("grammar")) {
                final Grammar g = new Grammar("grammar");
                g.setId(atts.getValue("id"));
                this.uid_map.grammars.put(g);
                Graph.this.grammars.add(g);
                this.r.setHandler(new AbstractHandler("grammar") {

                    @Override
                    protected void end(String name) {

                        if (name.equals("file")) {
                            g.setName(this.getValue());
                            try {
                                File f = new File(this.getValue());
                                StringBuilder s = new StringBuilder((int) f.length());
                                Reader in = new BufferedReader(new FileReader(f));
                                char c;
                                while ((c = (char) in.read()) != -1) {
                                    s.append(c);
                                }
                                in.close();
                                g.setGrammar(s.toString());
                            } catch (Exception exn) {
                                OptionPane.warning(null, new String[]{
                                    Resources.format("CouldNotLoadGrammar", g.getName()) + ".",
                                    exn.getLocalizedMessage()});
                            }
                        } else if (name.equals("name")) {
                            g.setName(this.getValue());
                        } else if (name.equals("value")) {
                            g.setGrammar(this.getValue());
                        }
                    }
                });
            } else if (name.equals("handler")) {
                InputHandler h = InputHandler.read(Graph.this, this.r, this.uid_map);
                String uid = atts.getValue("uid");
                if (uid == null) {
                    uid = atts.getValue("id");
                }
                h.setId(uid);
                this.uid_map.inputHandlers.put(h);
                // input handlers are also OwnerNodes (used in LogDocument)
                this.uid_map.nodes.put(h);
                Graph.this.handlers.add(h);
            } else if (name.equals("script")) {
                this.r.setHandler(new AbstractHandler("script") {

                    String name = "Main";
                    String script = "";

                    protected void end(String name) {

                        if (name.equals("name")) {
                            this.name = this.getValue();
                        } else if (name.equals("value")) {
                            this.script = this.getValue();
                        } else if (name.equals("script")) {
                            Graph.this.functions.add(new Functions(this.name, this.script));
                        }
                    }
                });
            } else if (name.equals("variable")) {
                final String uid = atts.getValue("uid");

                this.r.setHandler(new AbstractHandler("variable") {

                    String vname = null;
                    String vinit = null;
                    boolean vexport = false;
                    Type vtype = null;

                    @Override
                    protected void end(String name) {

                        if (name.equals("name")) {
                            this.vname = this.getValue();
                        } else if (name.equals("type")) {
                            Type t = Type.getTypeForName(this.getValue());
                            if (t == null) {
                                t = Slot.legacyType(this.getValue());
                            }
                            this.vtype = t;
                        } else if (name.equals("value")) {
                            this.vinit = this.getValue();
                        } else if (name.equals("export")) {
                            this.vexport = true;
                        } else if (name.equals("variable")) {
                            Slot v
                                    = new Slot(this.vname, this.vtype, this.vinit, this.vexport);
                            Graph.this.variables.add(v);
                            v.setId(uid);
                            GraphHandler.this.uid_map.variables.put(v);
                        }
                    }
                });
            } else if (name.equals("groovyVariable")) {
                final String uid = atts.getValue("uid");

                this.r.setHandler(new AbstractHandler("groovyVariable") {

                    String vname = null;
                    String vvalue = null;
                    boolean vexport = false;

                    @Override
                    protected void end(String name) {

                        if (name.equals("name")) {
                            this.vname = this.getValue();
                        } else if (name.equals("value")) {
                            this.vvalue = this.getValue();
                        } else if (name.equals("export")) {
                            this.vexport = true;
                        } else if (name.equals("groovyVariable")) {
                            GroovyVariable v
                                    = new GroovyVariable(this.vname, this.vvalue, this.vexport);
                            Graph.this.groovyOnlyVariables.add(v);
                            v.setId(uid);
                            GraphHandler.this.uid_map.groovyVariables.put(v);
                        }
                    }
                });
            } else if (name.equals("node")) {
                try {
                    String clss = atts.getValue("class");

                    Class<?> c = getNodeClassForName(clss);

                    if (c == null) {
                        throw new ClassNotFoundException(clss);
                    }

                    // Old documents may contain EndNodes that need to be
                    // converted
                    // to an appropriate new EndNode subclass.
                    if (EndNode.class.isAssignableFrom(c)) {
                        if (!Graph.this.supportsEndNode(c)) {
                            c = Graph.this.supportedEndNodes()[0];
                        }
                    }

                    Node n = (Node) c.newInstance();
                    String uid = atts.getValue("uid");
                    if (uid != null) {
                        n.setId(uid);
                        this.uid_map.nodes.put(n);
                    }
                    Graph.this.add(n);
                    n.reading = true;
                    n.read(this.r, this.uid_map);

                    if (n instanceof StartNode) {
                        Graph.this.startNode = (StartNode) n;
                    }
                } catch (Exception exn) {
                    exn.printStackTrace();
                    this.r.raiseException(exn.toString());
                }
            } else if (name.equals("edge")) {
                // System.out.println("Reading edge");
                Node src = null;
                Node tgt = null;
                if (atts.getValue("source") != null) {
                    // reference by index for backwards compatibility
                    List<Node> nodeList;
                    if (Graph.this.nodes instanceof List) {
                        nodeList = (List<Node>) Graph.this.nodes;
                    } else {
                        nodeList = new ArrayList<Node>(Graph.this.nodes);
                    }
                    int source = Integer.parseInt(atts.getValue("source"));
                    src = nodeList.get(source);

                    String target = atts.getValue("target");
                    if (target != null) {
                        int targetID = Integer.parseInt(target);
                        if (targetID >= 0) {
                            tgt = nodeList.get(targetID);
                        }
                    }
                } else if (atts.getValue("src") != null) {
                    String source = atts.getValue("src");
                    src = this.uid_map.nodes.get(source);

                    String target = atts.getValue("tgt");
                    if (target != null) {
                        tgt = this.uid_map.nodes.get(target);
                    }
                } else {
                    this.r.raiseException("Missing attributes in <edge> element");
                    return;
                }

                final Edge outEdge;
                String type = atts.getValue("type");
                if (type == null) {
                    outEdge = new Edge(src);
                } else {
                    if (type.equals(CatchAllEdge.TYPE)) {
                        outEdge = new CatchAllEdge(src);
                    } else if (type.equals(TimeoutEdge.TYPE)) {
                        outEdge = new TimeoutEdge(src);
                    } else if (type.equals(HandlerEdge.TYPE)) {
                        outEdge = new HandlerEdge(src);
                    } else {
                        this.r
                                .raiseAttributeValueException("Unexpected edge type: " + type);
                        return;
                    }
                }

                final Node sourceNode = src;
                final Node targetNode = tgt;

                this.r.setHandler(new AbstractHandler("edge") {

                    Edge edge = outEdge;

                    @Override
                    public void start(String name, final Attributes atts) {

                        if (name.equals("att")) {
                            final String attName = atts.getValue("name");
                            if (attName.equals("condition")) {
                                this.edge.setCondition(atts.getValue("value"));
                            } else if (attName.equals("handler_uid")
                                    || attName.equals("node_uid")) {
                                // convert edge if necessary
                                if (!(this.edge instanceof HandlerEdge)) {
                                    this.edge
                                            = new HandlerEdge(this.edge.getSource(), this.edge
                                                    .getTarget(), null);
                                }
                                if (attName.equals("handler_uid")) {
                                    final String uid = atts.getValue("value");
                                    GraphHandler.this.r.addCompletionRoutine(new Runnable() {

                                        public void run() {

                                            ((HandlerEdge) edge)
                                                    .setHandler(GraphHandler.this.uid_map.inputHandlers
                                                            .get(uid));
                                        }
                                    });
                                } else if (attName.equals("node_uid")) {
                                    final String uid = atts.getValue("value");
                                    GraphHandler.this.r.addCompletionRoutine(new Runnable() {

                                        public void run() {

                                            ((HandlerEdge) edge)
                                                    .setEndNode((EndNode) GraphHandler.this.uid_map.nodes
                                                            .get(uid));
                                        }
                                    });
                                }
                            } else if (attName.equals(InputNode.TIMEOUT)
                                    && (this.edge.getSource() instanceof InputNode)) {
                                // convert edge if necessary
                                if (!(this.edge instanceof TimeoutEdge)) {
                                    this.edge
                                            = new TimeoutEdge(this.edge.getSource(), this.edge
                                                    .getTarget());
                                }
                                this.edge.setProperty(attName, atts.getValue("value"));
                            } else {
                                this.edge.setProperty(attName, atts.getValue("value"));
                            }
                        }
                    }

                    @Override
                    public void end(String name) {

                        if (name.equals("edge")) {
                            sourceNode.addEdge(this.edge);
                            this.edge.setTarget(targetNode);
                        }
                    }
                });
            } else if (name.equals("groovyFunctions")) {
                this.r.setHandler(new AbstractHandler("groovyFunctions") {
                    @Override
                    protected void end(String name) throws SAXException {
                        Graph.this.groovyFunctions = this.getValue();
                    }
                });

            } else if (name.equals("group")) {
                Graph.this.readGroup(this.r, this.uid_map);
            } else if (name.equals("comment")) {
                this.atts = AbstractHandler.copyAttributes(atts);
            }
        }

        @Override
        protected void end(String name) {

            if (name.equals("graph")) {
                for (Node n : Graph.this.nodes) {
                    n.reading = false;
                }
                if (this.completionRoutine != null) {
                    this.completionRoutine.run();
                }
            } else if (name.equals("comment")) {
                Comment comment = new Comment();
                comment.setLocation(Integer.parseInt(this.atts.getValue("x")), Integer
                        .parseInt(this.atts
                                .getValue("y")));
                comment.setSize(Integer.parseInt(this.atts.getValue("width")), Integer
                        .parseInt(this.atts
                                .getValue("height")));
                if (this.atts.getValue("color") != null) {
                    comment.setColor(Color.decode(this.atts.getValue("color")));
                }
                comment.setComment(this.getValue());
                Graph.this.addComment(comment);
            }
        }
    }

    public Environment getEnvironment(boolean scope) {

        return scope == Graph.LOCAL ? this.localEnvironment
                : this.globalEnvironment;
    }

    protected void exportVoiceXMLVariables(XMLWriter w) {

        // export all visible variables. Since VoiceXML has no automatic access
        // to variables in the calling scope, we have to pass all global
        // variables
        // manually
        Collection<Slot> vars = this.getAllVariables(Graph.LOCAL);
        for (Slot s : vars) {
            w.printElement("var", new String[]{"name"},
                    new Object[]{s.getName()});
        }

        // write initialization of local variables
        for (Slot s : this.variables) {
            if (s.getInitValue() != null) {
                w.printElement("assign", new String[]{"name", "expr"}, new Object[]{
                    s.getName(), Node.vxmlExp(s.getInitValue())});
            }
        }
    }

    public void exportVoiceXML(XMLWriter w, IdMap uid_map)
            throws IOException {

        w.openElement("form", new String[]{"id"}, new String[]{"graph"
            + uid_map.graphs.put(this)});

        this.exportVoiceXMLVariables(w);

        w.printElement("goto", new String[]{"next"}, new String[]{"#node"
            + uid_map.nodes.put(this.getStartNode())});

        for (Node n : this.getNodes()) {
            n.exportVoiceXML(w, uid_map);
        }

        w.closeElement("form");
    }

    public Expression parseExpression(String expression)
            throws Exception {

        return Expression.parseExpression(expression, this
                .getEnvironment(Graph.LOCAL));
    }

    public Pattern parsePattern(String pattern)
            throws Exception {

        return Expression.parsePattern(pattern);
    }

    public String getId() {

        return this.id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String toString() {

        return this.getName();
    }

    public List<GroovyVariable> getGroovyVariables() {
        return this.groovyOnlyVariables;
    }

    public List<GroovyVariable> getAllGroovyVariables() {

        List<GroovyVariable> vars;

        if (this.getSuperGraph() != null) {
            vars = this.getSuperGraph().getAllGroovyVariables();
        } else {
            vars = new ArrayList<GroovyVariable>();
        }

        for (GroovyVariable v : groovyOnlyVariables) {
            if (v.isExport()) {
                vars.add(v);
            }
        }

        return vars;
    }

}
