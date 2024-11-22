package com.clt.dialogos.plugin;

import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComponent;

import com.clt.diamant.graph.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.IdMap;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 *
 */
public abstract class PluginSettings {

    private Map<WozInterface, PluginRuntime> runtimes = new HashMap<WozInterface, PluginRuntime>();

    /**
     * Writes settings to the graph file. This method is called whenever
     * DialogOS saves the graph to a file. You can implement this method to save
     * persistent information in this file. Use {@link Graph#printAtt(com.clt.xml.XMLWriter, java.lang.String, java.lang.Integer)
     * }
     * and its sister methods for writing to the XMLWriter conveniently.
     * <p>
     * Write these settings to XML
     */
    public abstract void writeAttributes(XMLWriter out, IdMap uidMap);

    /**
     * Read these settings from XML
     */
    public final void read(final XMLReader r, final IdMap uid_map) {

        r.setHandler(new AbstractHandler("plugin") {

            @Override
            protected void start(String name, Attributes atts) throws SAXException {

                if (name.equals("att")) {
                    final String n = atts.getValue("name");

                    if (atts.getValue("type") != null && atts.getValue("type").equals("text")) {
                        r.setHandler(new AbstractHandler("att") {

                            String value;

                            @Override
                            protected void end(String name)
                                    throws SAXException {

                                if (name.equals("value")) {
                                    this.value = this.getValue();
                                } else if (name.equals("att")) {
                                    if (this.value == null) {
                                        this.value = this.getValue();
                                    }
                                    PluginSettings.this.readAttribute(r, n,
                                            this.value, uid_map);
                                }
                            }
                        });
                    } else {
                        PluginSettings.this.readAttribute(r, n, atts.getValue("value"), uid_map);
                    }
                } else {
                    readOtherXML(r, name, atts, uid_map);
                }
            }
        });
    }

    /**
     * Reads a setting from a graph file. This method is called whenever
     * DialogOS loads a previously saved graph from a file. It is called
     * individually for each attribute that you saved with the writeAttributes
     * method. The name of the attribute is "name"; its value is "value".
     */
    protected abstract void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException;

    /**
     * override this operation if you need to deal with more than just
     * name-value pairs
     */
    protected void readOtherXML(XMLReader r, String name, Attributes atts, IdMap uid_map) throws SAXException {
        throw new RuntimeException(new IOException("weird xml, did not expect " + name + " here."));
    }

    /**
     * Creates an editor component to configure the plug-in.
     *
     * @return A Reference on the created editor.
     */
    public abstract JComponent createEditor();

    public final PluginRuntime initializeRuntime(Component parent, WozInterface comm) throws Exception {
        PluginRuntime runtime = this.createRuntime(parent);
        this.runtimes.put(comm, runtime);
        return runtime;
    }

    public PluginRuntime getRuntime(WozInterface comm) {
        return this.runtimes.get(comm);
    }

    public synchronized void disposeRuntime(WozInterface comm) {
        PluginRuntime runtime = this.runtimes.get(comm);
        if (runtime != null) {
            runtime.dispose();
            this.runtimes.remove(comm);
        }
    }

    public boolean isRelevantForNodes(Collection<Node> nodes) {
        Set<Class<? extends Node>> nodeTypes = nodes.stream().map(Node::getClass).collect(Collectors.toSet());
        return isRelevantForNodes(nodeTypes);
    }

    public boolean isRelevantForNodes(Set<Class<? extends Node>> nodeTypes) {
        return true;
    }

    protected abstract PluginRuntime createRuntime(Component parent) throws Exception;
}
