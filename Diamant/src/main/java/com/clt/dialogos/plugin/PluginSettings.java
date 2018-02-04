package com.clt.dialogos.plugin;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.IdMap;
import com.clt.diamant.WozInterface;
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

                    if (atts.getValue("type").equals("text")) {
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
                        PluginSettings.this.readAttribute(r, n, atts
                                .getValue("value"), uid_map);
                    }
                }
            }
        });
    }

    protected abstract void readAttribute(XMLReader r, String name,
            String value, IdMap uid_map)
            throws SAXException;

    /**
     * Creates an editor component to configure the plug-in.
     *
     * @return A Reference on the created editor.
     */
    public abstract JComponent createEditor();

    public final PluginRuntime initializeRuntime(Component parent,
            WozInterface comm)
            throws Exception {

        PluginRuntime runtime = this.createRuntime(parent);

        this.runtimes.put(comm, runtime);

        return runtime;
    }

    public PluginRuntime getRuntime(WozInterface comm) {

        return this.runtimes.get(comm);
    }

    public void disposeRuntime(WozInterface comm) {

        PluginRuntime runtime = this.runtimes.get(comm);
        if (runtime != null) {
            runtime.dispose();
            this.runtimes.remove(comm);
        }
    }

    protected abstract PluginRuntime createRuntime(Component parent)
            throws Exception;
}
