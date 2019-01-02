package com.clt.diamant;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.clt.dialogos.plugin.PluginLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.dialog.client.ConnectionChooser;
import com.clt.dialog.client.ConnectionState;
import com.clt.dialog.client.Connector;
import com.clt.dialog.client.ServerDevice;
import com.clt.dialogos.plugin.Plugin;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.Procedure;
import com.clt.diamant.graph.nodes.DialogSuspendedException;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.nodes.ProcNode;
import com.clt.diamant.graph.search.NodeSearchFilter;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.event.ProgressListener;
import com.clt.gui.ProgressDialog;
import com.clt.properties.Property;
import com.clt.script.DefaultEnvironment;
import com.clt.script.Environment;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;
import com.clt.script.exp.expressions.Function;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.types.StructType;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;
import com.clt.script.exp.values.Undefined;
import com.clt.util.DefaultLongAction;
import com.clt.util.LongAction;
import com.clt.util.StringTools;
import com.clt.util.TemplateBundle;
import com.clt.util.UserCanceledException;
import com.clt.xml.AbstractHandler;
import com.clt.xml.Base64;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class SingleDocument extends Document implements GraphOwner {

    private final class DeviceXMLHandler extends AbstractHandler {

        private final Graph graph;
        private final IdMap uid_map;
        private final XMLReader r;

        private DeviceXMLHandler(String elementName, Graph graph, IdMap uid_map, XMLReader r) {

            super(elementName);
            this.graph = graph;
            this.uid_map = uid_map;
            this.r = r;
        }

        public void start(String name, Attributes atts)
                throws SAXException {

            if (name.equals("att")) {
                String n = atts.getValue("name");
                if (n.equals("delay")) {
                    // ignore
                } else if (n.equals("executeInSeparateWindows")) {
                    // ignore
                }
            } else if (name.equals("device")) {
                final String id = atts.getValue("id");

                final Device d = new Device(id, "");
                this.uid_map.devices.put(d);
                this.r.setHandler(new AbstractHandler("device") {

                    int iconWidth = 0;

                    protected void start(String name, Attributes atts)
                            throws SAXException {

                        if (name.equals("connector")) {
                            try {
                                String cid = atts.getValue("class");
                                if (cid == null) {
                                    cid = atts.getValue("name");
                                }
                                final Connector connector = (Connector) Class.forName(cid)
                                        .newInstance();
                                d.setConnector(connector);
                                DeviceXMLHandler.this.r.setHandler(new AbstractHandler(
                                        "connector") {

                                    String pname;

                                    private void setConnectorProperty(String name, String value) {

                                        if (name != null) {
                                            Property<?> properties[] = connector.getProperties();
                                            for (Property<?> p : properties) {
                                                if (name.equals(p.getID())) {
                                                    try {
                                                        p.setValueFromString(value);
                                                    } catch (ParseException e) {
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    protected void start(String name, Attributes atts) {

                                        if (name.equals("att")) {
                                            String pname = atts.getValue("name");
                                            String value = atts.getValue("value");
                                            if (value != null) {
                                                this.setConnectorProperty(pname, value);
                                            } else {
                                                this.pname = pname;
                                            }
                                        }
                                    }

                                    protected void end(String name) {

                                        if (name.equals("att") && (this.pname != null)) {
                                            this.setConnectorProperty(this.pname, this.getValue());
                                        }
                                    }
                                });
                            } catch (Exception exn) {
                                DeviceXMLHandler.this.r
                                        .raiseException("Illegal type of connector: "
                                                + atts.getValue("name"));
                            }
                        } else if (name.equals("icon")) {
                            try {
                                this.iconWidth = Integer.parseInt(atts.getValue("width"));
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    protected void end(String name)
                            throws SAXException {

                        if (name.equals("name")) {
                            d.setName(this.getValue());
                        } else if (name.equals("icon")) {
                            byte[] cdata = Base64.decode(this.getValue());
                            if (cdata.length % 4 != 0) {
                                DeviceXMLHandler.this.r
                                        .raiseException("Illegal icon data for device " + id);
                            }
                            int data[] = SingleDocument.createIntArray(cdata);
                            if (this.iconWidth == 0) {
                                this.iconWidth = (int) Math.sqrt(data.length);
                            }
                            d.setIconData(new Device.Icon(data, this.iconWidth));
                        } else if (name.equals("type")) {
                            /* deprecated */
                        } else if (name.equals("port")) {
                            /* deprecated */
 /*
               * try { d.setPort(Integer.parseInt(getValue())); } catch
               * (NumberFormatException exn) {
               * r.raiseAttributeValueException(name); }
                             */
                        }
                    }
                });

                SingleDocument.this.getDevices().add(d);
            } else if (name.equals("plugin")) {
                String id = atts.getValue("type");
                try {
                    for (Plugin plugin : PluginLoader.getPlugins()) {
                        if (plugin.getId().equals(id)) {
                            PluginSettings settings
                                    = SingleDocument.this.getPluginSettings(plugin.getClass());
                            settings.read(this.r, this.uid_map);
                            break;
                        }
                    }
                } catch (Exception exn) {
                    System.err.println(exn);
                }
            } else if (name.equals("grammar")) {
                final Grammar g = new Grammar("grammar");
                g.setId(atts.getValue("id"));
                this.uid_map.grammars.put(g);
                this.graph.getGrammars().add(g);
                this.r.setHandler(new AbstractHandler("grammar") {

                    protected void end(String name) {

                        if (name.equals("file")) {
                            g.setName(this.getValue());
                            try {
                                File grammarFile = new File(this.getValue());
                                StringBuilder s = new StringBuilder((int) grammarFile.length());
                                Reader in = new BufferedReader(new FileReader(grammarFile));
                                char c;
                                while ((c = (char) in.read()) != -1) {
                                    s.append(c);
                                }
                                in.close();
                                g.setGrammar(s.toString());
                            } catch (Exception exn) {
                                // dabo: die Warnings muessen anders
                                // hochgereicht werden
                                /*
                 * OptionPane.warning(SingleDocument.this, new String[] {
                 * "Could not load grammar " + g.getName() + ".",
                 * exn.getLocalizedMessage() });
                                 */
                            }
                        } else if (name.equals("name")) {
                            g.setName(this.getValue());
                        } else if (name.equals("value")) {
                            g.setGrammar(this.getValue());
                        }
                    }
                });
            } else if (name.equals("graph")) {
                this.graph.read(this.r, null, this.uid_map);
            } else {
                this.r.raiseUnexpectedElementException(name);
            }
        }

        public void end(String name) {

            if (name.equals(SingleDocument.this.getDocumentType().toLowerCase())) {
                this.r.addCompletionRoutine(new Runnable() {

                    public void run() {

                        DeviceXMLHandler.this.graph.updateEdges();
                        SingleDocument.this.setGraph(DeviceXMLHandler.this.graph);
                    }
                });
            }
        }
    }

    private Graph graph = null;

    private Map<Class<? extends Plugin>, PluginSettings> pluginSettings;
    private Collection<Device> devices;

    private Map<String, TemplateBundle> localizationBundles;

    /**
     * Constructs a new single document with one start node.
     */
    public SingleDocument() {

        this.devices = new ArrayList<Device>();
        this.pluginSettings = new HashMap<Class<? extends Plugin>, PluginSettings>();
        this.localizationBundles = new HashMap<String, TemplateBundle>();

        for (Plugin plugin : PluginLoader.getPlugins()) {
            this.pluginSettings.put(plugin.getClass(), plugin.createDefaultSettings());
        }

        Graph g = new Graph(this);
        this.setGraph(g);
    }

    void setGraph(Graph graph) {

        if (this.graph != graph) {
            Graph oldGraph = this.graph;
            this.graph = graph;

            this.firePropertyChange("graph", oldGraph, graph);
        }
    }

    public String getDocumentType() {

        return "Wizard";
    }

    /**
     * Establish socket connections to client modules.
     *
     * @param timeout Connection timeout in milliseconds.
     */
    public boolean connectDevices(ConnectionChooser chooser, int timeout) {

        Collection<Device> devices = this.getDevices();
        if (devices.size() > 0) {
            boolean ok = ServerDevice.connect(devices.toArray(new Device[devices.size()]),
                    timeout,
                    Resources.getString("ConnectingDevices"),
                    chooser);

            if (!ok) {
                this.closeDevices();
            }

            return ok;
        } else {
            return true;
        }
    }

    public void closeDevices() {

        for (Device d : this.getDevices()) {
            try {
                d.close();
            } catch (Exception ignore) {
            }
        }
    }

    public ExecutionResult run(Component parent, final WozInterface transition) throws Exception {
        Object message = null;
        Node node = null;
        int type = ExecutionResult.INFORMATION;

        final ProgressDialog d;
        if (parent != null) {
            d = new ProgressDialog(parent, 0);
        } else {
            d = null;
        }

        LongAction init = new DefaultLongAction(Resources.getString("InitializingModel")) {
            @Override
            protected void run(ProgressListener l) throws Exception {
                for (Plugin plugin : PluginLoader.getPlugins()) {
                    Class<? extends Plugin> c = plugin.getClass();
                    SingleDocument.this.getPluginSettings(c).initializeRuntime(d, transition);
                }
            }
        };

        LongAction uninit = new DefaultLongAction(Resources.getString("UninitializingModel")) {
            @Override
            protected void run(ProgressListener l) throws Exception {
                for (Plugin plugin : PluginLoader.getPlugins()) {
                    Class<? extends Plugin> c = plugin.getClass();
                    SingleDocument.this.getPluginSettings(c).disposeRuntime(transition);
                }
            }
        };

        if (d != null) {
            d.run(init);
        } else {
            init.run();
        }

        try {
            if (transition.initInterface()) {
                this.execute(parent, transition);

                message = Resources.getString("ExecutionComplete");
                type = ExecutionResult.INFORMATION;
            } else {
                message = Resources.format("TransitionInitError", transition.getName());
                type = ExecutionResult.ERROR;
            }
        } catch (ExecutionStoppedException exn) {
            message = Resources.getString("ExecutionStopped");
            type = ExecutionResult.INFORMATION;
        } catch (ThreadDeath death) {
            throw death;
        } catch (DialogSuspendedException exn) {
            // pass DialogSuspendedExceptions through to caller
            throw exn;
        } catch (Throwable exn) {
            if (exn instanceof InvocationTargetException) {
                exn = ((InvocationTargetException) exn).getTargetException();
            }
            if (exn instanceof UserCanceledException) {
                message = Resources.getString("ExecutionStopped");
                type = ExecutionResult.INFORMATION;
            } else {
                message = new String[]{Resources.getString("ExecutionError"), exn.toString()};
                type = ExecutionResult.ERROR;
                if (exn instanceof NodeExecutionException) {
                    node = ((NodeExecutionException) exn).getNode();
                    if (((NodeExecutionException) exn).getException() instanceof ExecutionStoppedException) {
                        message = Resources.getString("ExecutionStopped");
                        type = ExecutionResult.INFORMATION;
                    } else {
                        exn.printStackTrace();
                    }
                } else {
                    exn.printStackTrace();
                }
            }
        } finally {
            if (d != null) {
                d.run(uninit);
            } else {
                uninit.run();
            }

            transition.disposeInterface(type == ExecutionResult.ERROR);
            this.closeDevices();
        }

        return new ExecutionResult(type, message, node);
    }

    public void execute(Component parent, final WozInterface transition) throws Exception {
        final InputCenter input = new InputCenter(this.getDevices());
        final ExecutionLogger logger = new ExecutionLogger(graph, getGraphName(), Preferences.getPrefs().loggingEnabled.getValue());

        try {
            transition.startDocument(SingleDocument.this, SingleDocument.this.getTitle(), input);

            try {
                transition.setState(WozInterface.State.INSTRUCT);
                transition.setState(WozInterface.State.RUN);

                for (Device device : this.getDevices()) {
                    try {
                        device.start();
                    } catch (Exception exn) {
                    }
                }

                try {
                    this.graph.execute(transition, input, logger);
                } catch (ExecutionStoppedException exn) {
                    transition.error("abort", "Execution stopped by user");
                    throw exn;
                } catch (NodeExecutionException exn) {
                    if (exn.getException() instanceof ExecutionStoppedException) {
                        transition.error("abort", "Execution stopped by user");
                    } else {
                        transition.error(exn.getClass().getName(), exn.getLocalizedMessage());
                    }
                    throw exn;
                } catch(DialogSuspendedException exn) {
                    // pass through
                    throw exn;
                } catch (Exception exn) {
                    // exn.printStackTrace();
                    transition.error(exn.getClass().getName(), exn.getLocalizedMessage());
                    if (exn instanceof RuntimeException) {
                        throw (RuntimeException) exn;
                    } else {
                        throw new RuntimeException(exn.getLocalizedMessage());
                    }
                }
            } finally {
                transition.setState(WozInterface.State.END);

                transition.endDocument(SingleDocument.this);
            }
        } finally {
            input.dispose();
            transition.setState(WozInterface.State.NORMAL);
            logger.saveDocAsXML();
        }
    }

    public void load(File f, XMLReader r) {

        this.load(f, r, new IdMap());
    }

    protected void load(final File f, final XMLReader r, final IdMap uid_map) {

        final Graph graph = new Graph(this);

        this.devices.clear();

        r.setHandler(new DeviceXMLHandler(this.getDocumentType().toLowerCase(),
                graph, uid_map, r));

        this.setFile(f);

    }

    public void validate(Collection<SearchResult> errors, ProgressListener progress) {
        super.validate(errors, progress);
        this.graph.validate(errors, progress);
    }

    private void writeDocumentSettings(XMLWriter out, File file, IdMap uid_map) {

        for (Device d : this.devices) {
            out.openElement("device", new String[]{"id"},
                    new Object[]{uid_map.devices.put(d)});

            out.printElement("name", d.getName());
            // out.printElement("port", new Integer(d.getPort()));
            Connector connector = d.getConnector();
            if (connector != null) {
                out.openElement("connector", new String[]{"class"},
                        new Object[]{connector
                                    .getClass().getName()});
                Property<?>[] properties = connector.getProperties();
                for (int j = 0; j < properties.length; j++) {
                    String value = properties[j].getValueAsString();
                    if (value != null) {
                        Graph.printAtt(out, properties[j].getID(), value);
                    }
                }
                out.closeElement("connector");
            }

            Device.Icon iconData = d.getIconData();
            if (iconData != null) {
                out.printElement("icon", new String[]{"width"},
                        new String[]{String
                                    .valueOf(iconData.getWidth())}, Base64
                        .encodeBytes(SingleDocument.createByteArray(iconData.getData())));
            }

            out.closeElement("device");
        }

        for (Plugin plugin : PluginLoader.getPlugins()) {
            out.openElement("plugin", new String[]{"type"}, new String[]{plugin
                .getId()});
            this.getPluginSettings(plugin.getClass()).writeAttributes(out, uid_map);
            out.closeElement("plugin");
        }
    }

    /**
     * Helper method to convert byte arrays to int arrays
     */
    private static int[] createIntArray(byte[] byteArray) {

        if (byteArray.length % 4 != 0) {
            throw new IllegalArgumentException();
        }
        int intArray[] = new int[byteArray.length / 4];
        for (int i = 0; i < intArray.length; i++) {
            int b1 = byteArray[4 * i];
            int b2 = byteArray[4 * i + 1];
            int b3 = byteArray[4 * i + 2];
            int b4 = byteArray[4 * i + 3];
            if (b1 < 0) {
                b1 += 256;
            }
            if (b2 < 0) {
                b2 += 256;
            }
            if (b3 < 0) {
                b3 += 256;
            }
            if (b4 < 0) {
                b4 += 256;
            }
            intArray[i] = (b1 << 24) | (b2 << 16) | (b3 << 8) | (b4 << 0);
        }
        return intArray;
    }

    /**
     * Helper method to convert int arrays to byte arrays
     */
    private static byte[] createByteArray(int[] intArray) {

        byte byteArray[] = new byte[intArray.length * 4];
        for (int i = 0; i < intArray.length; i++) {
            byteArray[4 * i] = (byte) ((intArray[i] >> 24) & 0x000000ff);
            byteArray[4 * i + 1] = (byte) ((intArray[i] >> 16) & 0x000000ff);
            byteArray[4 * i + 2] = (byte) ((intArray[i] >> 8) & 0x000000ff);
            byteArray[4 * i + 3] = (byte) ((intArray[i] >> 0) & 0x000000ff);
        }
        return byteArray;
    }

    @Override
    public void write(final XMLWriter out, final IdMap uid_map) {

        out.openElement(this.getDocumentType().toLowerCase());

        this.writeDocumentSettings(out, this.getFile(), uid_map);

        this.graph.save(out, uid_map);

        out.closeElement(this.getDocumentType().toLowerCase());
    }

    private void addGlobalProcs(Graph addToThisGraph, Graph procsFromThisGraph,
            Mapping map) {

        Map<ProcNode, Node> freeprocs = procsFromThisGraph
                .getFreeProcedures(new Hashtable<ProcNode, Node>());

        for (ProcNode p : freeprocs.keySet()) {
            if (map.getNode(p) == p) {
                ProcNode newProc = (ProcNode) p.clone(map);
                addToThisGraph.add(newProc);
                map.addNode(p, newProc);
                this.addGlobalProcs(addToThisGraph, p.getOwnedGraph(), map);
            }
        }
    }

    public void exportVoiceXML(XMLWriter w, IdMap uid_map)
            throws IOException {

        w.openElement("vxml", new String[]{"version"}, new String[]{"2.0"});

        this.getOwnedGraph().exportVoiceXML(w, uid_map);

        w.closeElement("vxml");
    }

    public void export(final Graph export_graph, File file)
            throws IOException {

        if (export_graph instanceof Procedure) {
            List<Slot> p = ((Procedure) export_graph).getParameters();
            if (p.size() > 0) {
                throw new IOException(Resources.getString("CannotExportProcedures"));
            }
        }

        Mapping map = new Mapping();

        final Graph g = export_graph.clone(map);

        this.addGlobalProcs(g, export_graph, map);

        // update references to global variables and procedures
        // It is important to do an update here in order to update local
        // variable references before determining free variables
        g.update(map);

        // get free variables from the cloned graph, because we must include
        // variables
        // used by the global procedures.
        Map<Slot, Node> freevars = g.getFreeVariables(new Hashtable<Slot, Node>());
        List<Slot> gvars = g.getVariables();
        if (freevars.size() > 0) {
            // new ListSelectionDialog(parent, "Free variables", "Cannot export
            // graph because of references to these free variables:",
            // Misc.hashKeys(freevars), 0, false);
            // return;
            for (Slot v : freevars.keySet()) {
                for (int i = 0; i < gvars.size(); i++) {
                    if (gvars.get(i).getName().equals(v.getName())) {
                        throw new IOException(
                                "Cannot export graph because it contains a references to the global variable \""
                                + v.getName()
                                + "\", and it already contains a local variable with the same name.");
                    }
                }
            }
            for (Slot v : freevars.keySet()) {
                Slot newVar = v.clone(map);
                gvars.add(newVar);
            }
        }

        // reupdate references to variables
        g.update(map);

        g.setOwner(new GraphOwner() {

            public Graph getSuperGraph() {

                return null;
            }

            public Graph getOwnedGraph() {

                return g;
            }

            public Collection<Device> getDevices() {

                return SingleDocument.this.getDevices();
            }

            public List<Grammar> getGrammars() {

                return SingleDocument.this.getGrammars();
            }

            public PluginSettings getPluginSettings(
                    Class<? extends Plugin> pluginClass) {

                return SingleDocument.this.getPluginSettings(pluginClass);
            }

            public Environment getEnvironment(boolean local) {

                return SingleDocument.this.getEnvironment(local);
            }

            public void setDirty(boolean dirty) {

            }

            public void export(Graph g, File f)
                    throws IOException {

                SingleDocument.this.export(g, f);
            }

            public String getGraphName() {

                return "Dialogue";
            }

            public void setGraphName(String name) {

            }
        });

        XMLWriter out = new XMLWriter(file);
        this.writeHeader(out);

        out.openElement(this.getDocumentType().toLowerCase());

        IdMap uid_map = new IdMap(true);
        this.writeDocumentSettings(out, file, uid_map);

        g.save(out, uid_map);

        out.closeElement(this.getDocumentType().toLowerCase());

        out.close();
    }

    private Environment environment = new DefaultEnvironment() {

        @Override
        public Variable createVariableReference(final String name) {

            for (final Device d : SingleDocument.this.devices) {
                if (d.getName().equals(name)) {
                    return new Variable() {

                        public String getName() {

                            return name;
                        }

                        public Value getValue() {

                            return new DeviceValue(d);
                        }

                        public void setValue(Value value) {

                        }

                        public Type getType() {

                            return DeviceValue.TYPE;
                        }
                    };
                }
            }

            return super.createVariableReference(name);
        }

        @Override
        public Expression createFunctionCall(final String name,
                final Expression[] arguments) {

            if (name.equals("rpc")) {
                if (arguments.length < 2) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function rpc()");
                }
                return new Function(name, arguments) {

                    @Override
                    protected Value eval(Debugger dbg, Value[] args) {

                        if (!(args[0] instanceof DeviceValue)
                                || !(args[1] instanceof StringValue)) {
                            throw new EvaluationException(
                                    "Wrong type of arguments in call to function rpc()");
                        }
                        try {
                            Device d = ((DeviceValue) args[0]).getDevice();
                            String procedure = ((StringValue) args[1]).getString();
                            Value[] as = new Value[args.length - 2];
                            System.arraycopy(args, 2, as, 0, args.length - 2);
                            try {
                                return d.rpc(procedure, as);
                            } catch (ConnectException exn) {
                                throw new EvaluationException(
                                        "RPC failed because the device \""
                                        + d.getName() + "\" is not connected");
                            } catch (RemoteException exn) {
                                throw new EvaluationException(
                                        "RPC failed because the remote procedure " + procedure
                                        + "() raised an error: "
                                        + exn.getLocalizedMessage());
                            }
                        } catch (Exception exn) {
                            throw new EvaluationException(exn.getLocalizedMessage());
                        }
                    }

                    @Override
                    public Type getType() {

                        Type.unify(arguments[0].getType(), DeviceValue.TYPE);
                        Type.unify(arguments[1].getType(), Type.String);

                        return new TypeVariable();
                    }
                };
            } else if (name.equals("getLetters")) {
                if (arguments.length != 3) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function getLetters()");
                }
                return new Function(name, arguments) {

                    @Override
                    protected Value eval(Debugger dbg, Value[] args) {

                        if (!((args[0] instanceof ListValue)
                                && (args[1] instanceof StringValue) && (args[2] instanceof IntValue))) {
                            throw new EvaluationException(
                                    "Wrong type of arguments in call to function getLetters()");
                        }
                        String prefix = ((StringValue) args[1]).getString();
                        int numLetters = (int) ((IntValue) args[2]).getInt();
                        ListValue list = (ListValue) args[0];
                        return SingleDocument.getPrefixLetters(list, prefix, numLetters);
                    }

                    @Override
                    public Type getType() {

                        Type.unify(arguments[0].getType(), new ListType(Type.String));
                        Type.unify(arguments[1].getType(), Type.String);
                        Type.unify(arguments[2].getType(), Type.Int);

                        return new ListType(Type.String);
                    }
                };
            } else if (name.equals("isConnected")) {
                if (arguments.length != 1) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function isConnected()");
                }
                return new Function(name, arguments) {

                    @Override
                    protected Value eval(Debugger dbg, Value[] args) {

                        if (!(args[0] instanceof DeviceValue)) {
                            throw new EvaluationException(
                                    "Wrong type of arguments in call to function isCOnnected()");
                        }
                        DeviceValue d = (DeviceValue) args[0];
                        return new BoolValue(
                                d.getDevice().getState() == ConnectionState.CONNECTED);
                    }

                    @Override
                    public Type getType() {

                        Type.unify(arguments[0].getType(), DeviceValue.TYPE);
                        return Type.Bool;
                    }
                };
            } else if (name.equals("capitalize")) {
                if (arguments.length != 1) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function capitalize()");
                }
                return new Function(name, arguments) {

                    @Override
                    protected Value eval(Debugger dbg, Value[] args) {

                        if (!(args[0] instanceof ListValue)) {
                            throw new EvaluationException(
                                    "Wrong type of arguments in call to function capitalize()");
                        }
                        ListValue list = (ListValue) args[0];
                        Value result[] = new StringValue[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            String s = ((StringValue) list.get(i)).getString();
                            // dont't use String.toUpperCase(), because it will
                            // transform \u00df -> SS
                            StringBuffer b = new StringBuffer(s.length());
                            for (int j = 0; j < s.length(); j++) {
                                b.append(Character.toUpperCase(s.charAt(j)));
                            }
                            result[i] = new StringValue(b.toString());
                        }

                        return new ListValue(result);
                    }

                    public Type getType() {

                        Type.unify(arguments[0].getType(), new ListType(Type.String));
                        return new ListType(Type.String);
                    }
                };
            } else if (name.equals("getNBestList")) {
                if (arguments.length != 1) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function getNBestList()");
                }
                return new Function(name, arguments) {

                    protected Value eval(Debugger dbg, Value[] args) {

                        if (!(args[0] instanceof StructValue)) {
                            throw new EvaluationException(
                                    "Wrong type of arguments in call to function getNBestList()");
                        }
                        StructValue v = (StructValue) args[0];
                        Map<Integer, Value> map = new TreeMap<Integer, Value>();
                        for (Iterator<String> it = v.labels(); it.hasNext();) {
                            String label = it.next();
                            if (label.startsWith("r")) {
                                try {
                                    int num = Integer.parseInt(label.substring(1));
                                    map.put(new Integer(num), v.getValue(label));
                                } catch (Exception ignore) {
                                }
                            }
                        }

                        Value[] nbest = new Value[map.size()];
                        int i = 0;
                        for (Iterator<Integer> it = map.keySet().iterator(); it.hasNext(); i++) {
                            nbest[i] = map.get(it.next());
                        }
                        return new ListValue(nbest);
                    }

                    public Type getType() {

                        Type.unify(arguments[0].getType(), new StructType());
                        return new ListType(Type.String);
                    }
                };
            } else if (name.equals("getModelName")) {
                if (arguments.length != 0) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function " + name
                            + "()");
                }
                return new Function(name, arguments) {

                    protected Value eval(Debugger dbg, Value[] args) {

                        return new StringValue(SingleDocument.this.getTitle());
                    }

                    public Type getType() {

                        return Type.String;
                    }
                };
            } else if (name.equals("getModelPath")) {
                if (arguments.length != 0) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function " + name
                            + "()");
                }
                return new Function(name, arguments) {

                    protected Value eval(Debugger dbg, Value[] args) {

                        File f = SingleDocument.this.getFile();
                        if (f == null) {
                            throw new EvaluationException("Error in getModelPath(): Model "
                                    + SingleDocument.this.getTitle() + " hasn't been saved yet.");
                        } else {
                            if (f.getParent() == null) {
                                return new StringValue(System.getProperty("user.dir"));
                            } else {
                                return new StringValue(f.getParent());
                            }
                        }
                    }

                    public Type getType() {

                        return Type.String;
                    }
                };
            } else if (name.equals("formatString")) {
                if (arguments.length < 2) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function " + name
                            + "()");
                }
                return new Function(name, arguments) {

                    protected Value eval(Debugger dbg, Value[] args) {

                        String bundleName = ((StringValue) args[0]).getString();
                        String language = ((StringValue) args[1]).getString();
                        String templateName = ((StringValue) args[2]).getString();

                        Locale locale = StringTools.parseLocale(language);

                        Map<String, String> vars = new HashMap<String, String>();
                        List<String> selectors = new ArrayList<String>();
                        if (args.length > 3) {
                            if (!(args[3] instanceof Undefined)) {
                                StructValue s = (StructValue) args[3];
                                for (String label : s.getLabels()) {
                                    Value v = s.getValue(label);
                                    if (!(v instanceof Undefined)) {
                                        vars.put(label,
                                                v instanceof StringValue ? ((StringValue) v)
                                                                .getString() : v.toString());
                                    }
                                }
                            }
                        }

                        if (args.length > 4) {
                            if (!(args[4] instanceof Undefined)) {
                                for (Value selector : (ListValue) args[4]) {
                                    selectors
                                            .add(selector instanceof StringValue
                                                    ? ((StringValue) selector)
                                                            .getString()
                                                    : selector.toString());
                                }
                            }
                        }

                        TemplateBundle bundle
                                = SingleDocument.this.localizationBundles.get(bundleName);
                        if (bundle == null) {
                            throw new EvaluationException("Unknown bundle " + bundleName);
                        }

                        return new StringValue(bundle.format(locale, templateName, vars,
                                (Object[]) selectors.toArray(new String[selectors.size()])));
                    }

                    public Type getType() {

                        return Type.String;
                    }
                };
            } else if (name.equals("loadBundle")) {
                if (arguments.length != 1) {
                    throw new TypeException(
                            "Wrong number of arguments in call to function " + name
                            + "()");
                }
                return new Function(name, arguments) {

                    @Override
                    protected Value eval(Debugger dbg, Value[] args) {

                        String name = ((StringValue) args[0]).getString();

                        File f = SingleDocument.this.getFile();
                        if (f == null) {
                            throw new EvaluationException("Error in getModelPath(): Model "
                                    + SingleDocument.this.getTitle() + " hasn't been saved yet.");
                        } else {
                            if (f.getParent() == null) {
                                f = new File(System.getProperty("user.dir"));
                            } else {
                                f = f.getParentFile();
                            }
                        }

                        TemplateBundle bundle = new TemplateBundle();

                        File files[] = f.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                String fileName = file.getName();
                                String ending = ".tpl";
                                if (file.isFile() && fileName.startsWith(name + "_")
                                        && fileName.endsWith(ending)) {
                                    String locale = fileName.substring((name + "_").length(),
                                            fileName.length() - ending.length());

                                    try {
                                        bundle.read(StringTools.parseLocale(locale),
                                                new FileReader(file));
                                    } catch (IOException exn) {
                                        throw new EvaluationException(exn.getLocalizedMessage(),
                                                exn);
                                    }
                                }
                            }
                        }

                        SingleDocument.this.localizationBundles.put(name, bundle);

                        return Value.Void;
                    }

                    @Override
                    public Type getType() {

                        return Type.Void;
                    }
                };
            } else {
                return super.createFunctionCall(name, arguments);
            }
        }

    };

    static ListValue getPrefixLetters(ListValue words, String prefix,
            int numLetters) {

        int position = prefix.length();
        Set<String> letters = new HashSet<String>();
        for (int i = 0; i < words.size(); i++) {
            String s = ((StringValue) words.get(i)).getString();
            if (s.startsWith(prefix) && (s.length() > position)) {
                if (numLetters == -1) {
                    letters.add(s.substring(position));
                } else {
                    if (s.charAt(position) == ' ') {
                        letters.add(" ");
                    } else {
                        StringBuilder b = new StringBuilder();
                        for (int j = 0; (j < numLetters) && (position + j < s.length())
                                && (s.charAt(position + j) != ' '); j++) {
                            if (j > 0) {
                                b.append(' ');
                            }
                            b.append(s.charAt(position + j));
                            letters.add(b.toString());
                        }
                    }
                }
                /*
         * if (firstLetterOnly) { letters.add(s.substring(position,
         * position+1)); if (s.length() > position+1)
         * letters.add(s.substring(position, position+1) + " " +
         * s.substring(position+1, position+2)); } else
         * letters.add(s.substring(position));
                 */
            }
        }

        StringValue s[] = new StringValue[letters.size()];
        int i = 0;
        for (String letter : new TreeSet<String>(letters)) {
            s[i++] = new StringValue(letter);
        }

        return new ListValue(s);
    }

    public Environment getEnvironment(boolean local) {

        return this.environment;
    }

    public Collection<Device> getDevices() {

        return this.devices;
    }

    public PluginSettings getPluginSettings(Class<? extends Plugin> pluginClass) {

        return this.pluginSettings.get(pluginClass);
    }

    public List<Grammar> getGrammars() {

        return this.getOwnedGraph().getGrammars();
    }

    public Graph getSuperGraph() {

        return null;
    }

    public Graph getOwnedGraph() {

        return this.graph;
    }

    public String getGraphName() {

        return this.getTitle();
    }

    public void setGraphName(String name) {

        this.setTitle(name);
    }

    public Collection<SearchResult> find(NodeSearchFilter filter) {

        return this.graph.find(filter);
    }
}
