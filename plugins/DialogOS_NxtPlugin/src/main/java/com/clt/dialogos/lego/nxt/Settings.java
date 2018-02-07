/*
 * @(#)Settings.java
 * Created on 30.03.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */
package com.clt.dialogos.lego.nxt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Graph;
import com.clt.event.ProgressListener;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.nxt.AbstractNxt;
import com.clt.lego.nxt.Nxt;
import com.clt.lego.nxt.NxtDeviceInfo;
import com.clt.lego.nxt.Sensor;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.util.AbstractLongAction;
import com.clt.util.UserCanceledException;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 */
public class Settings extends PluginSettings {

    private static Collection<BrickDescription<? extends Nxt>> availablePorts = new TreeSet<BrickDescription<? extends Nxt>>();
    private DefaultEnumProperty<BrickDescription<? extends Nxt>> nxt;
    private Map<Sensor.Port, DefaultEnumProperty<SensorType>> sensorTypes;

    static {
        Settings.availablePorts.add(new BrickDescription<Nxt>("-", null, null, null) {
            @Override
            protected Nxt createBrickImpl(Component parent) {
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private BrickDescription<Nxt>[] getAvailablePorts() {
        return Settings.availablePorts.toArray(new BrickDescription[Settings.availablePorts.size()]);
    }

    public Settings() {

        this.nxt = new DefaultEnumProperty<BrickDescription<? extends Nxt>>("nxt", Resources.getString("NxtBrick"), null, this.getAvailablePorts()) {
            @Override
            public String getName() {
                return Resources.getString("NxtBrick");
            }
        };

        if (!Settings.availablePorts.isEmpty()) {
            this.nxt.setValue(Settings.availablePorts.iterator().next());
        }

        this.sensorTypes = new LinkedHashMap<Sensor.Port, DefaultEnumProperty<SensorType>>();
        for (Sensor.Port port : Sensor.Port.values()) {
            DefaultEnumProperty<SensorType> p = new DefaultEnumProperty<SensorType>(port.name(), port.toString(), null, SensorType.values(), SensorType.NONE);
            this.sensorTypes.put(port, p);
        }

        // updateBrickList(null, true);
    }

    private void addBrick(BrickDescription<Nxt> desc) {
        Settings.availablePorts.add(desc);
        this.nxt.setPossibleValues(this.getAvailablePorts());
        this.nxt.setValue(desc);
    }

    private void updateBrickList(Component parent, boolean search) {
        try {
            if (search) {
                final ProgressDialog d = new ProgressDialog(parent);
                try {
                    d.run(new AbstractLongAction() {
                        private AtomicBoolean cancel = new AtomicBoolean(false);

                        @Override
                        public void cancel() {
                            this.cancel.set(true);
                        }

                        @Override
                        public boolean canCancel() {
                            return true;
                        }

                        @Override
                        protected void run(ProgressListener progress) throws Exception {
                            StringWriter log = new StringWriter();
                            PrintWriter pw = new PrintWriter(log, true);

                            boolean foundNewBrick = false;
                            
                            Collection<BrickDescription<? extends Nxt>> availableBricks
                                    = AbstractNxt.getAvailableBricks(d, progress, this.cancel, null);

                            // remove bricks that are no longer connected
                            for (BrickDescription x : Settings.availablePorts) {
                                if (!availableBricks.contains(x)) {
                                    Settings.availablePorts.remove(x);
                                }
                            }
                            
                            // add bricks that were newly connected
                            for (BrickDescription x : availableBricks) {
                                if (!Settings.availablePorts.contains(x)) {
                                    foundNewBrick = true;
                                    addBrick(x);
                                }
                            }

                            if (! foundNewBrick ) {
                                pw.println(Resources.getString("NoNewBrickFound"));
                            }
                            
                            pw.close();

                            if (log.getBuffer().length() > 0) {
                                OptionPane.warning(d, log.toString());
                            }
                        }

                        @Override
                        public String getDescription() {
                            return Resources.getString("SearchingForBricks");
                        }
                    });
                } catch (InvocationTargetException exn) {
                    exn.getTargetException().printStackTrace();
                    OptionPane.error(parent, exn.getTargetException());
                }
            }
        } catch (Exception exn) {
            System.err.println(exn);
        }

        BrickDescription<Nxt>[] available = getAvailablePorts();
        this.nxt.setPossibleValues(available);
        
        // display the first of the newly found bricks in
        // the Settings-UI
        if (search && (this.nxt.getPossibleValues().length > 1)) {
            this.nxt.setValue(this.nxt.getPossibleValues()[1]);
        }
    }

    @Override
    public JComponent createEditor() {

        final JPanel p = new JPanel(new BorderLayout());

        // update ports
        this.updateBrickList(null, false);

        PropertySet<Property<?>> ps = new PropertySet<Property<?>>();
        ps.add(this.nxt);
        for (DefaultEnumProperty<SensorType> sensorType : this.sensorTypes.values()) {
            ps.add(sensorType);
        }

        p.add(ps.createPropertyPanel(false), BorderLayout.NORTH);

        JButton update = new JButton(Resources.getString("FindBricks"));
        update.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                Settings.this.updateBrickList(null, true);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(update);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    @Override
    protected void readAttribute(final XMLReader r, String name, String value,
            IdMap uid_map) {

        if (name.equals("nxt")) {
            r.setHandler(new AbstractHandler("att") {

                private Class<?> factory = null;
                private String brickName = null;
                private String uri = null;
                private InterfaceType type = null;
                private String port = null;

                @Override
                protected void start(String name, Attributes atts) {

                    if (name.equals("att")) {
                        r.setHandler(new AbstractHandler("att"));

                        String att = atts.getValue("name");
                        String value = atts.getValue("value");
                        if (att.equals("factory")) {
                            try {
                                this.factory = Settings.class.getClassLoader().loadClass(value);
                            } catch (ClassNotFoundException exn) {
                                // ignore
                            }
                        } else if (att.equals("name")) {
                            this.brickName = value;
                        } else if (att.equals("type")) {
                            this.type = InterfaceType.valueOf(value);
                        } else if (att.equals("port")) {
                            this.port = value;
                        } else if (att.equals("uri")) {
                            this.uri = value;
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void end(String name)
                        throws SAXException {

                    try {
                        BrickDescription<Nxt> desc
                                = (BrickDescription) this.factory.getConstructor(
                                        new Class[]{String.class, NxtDeviceInfo.class,
                                            InterfaceType.class, String.class}).newInstance(
                                                new Object[]{this.uri,
                                                    new NxtDeviceInfo(this.brickName, null, null, 0, 0, 0),
                                                    this.type,
                                                    this.port});
                        Settings.this.addBrick(desc);
                        Settings.this.nxt.setValue(desc);
                    } catch (Exception exn) {
                        r.raiseException(exn);
                    }
                }
            });
        } else if (name.equals("sensor")) {
            r.setHandler(new AbstractHandler("att") {

                @Override
                protected void start(String name, Attributes atts)
                        throws SAXException {

                    if (name.equals("att")) {
                        r.setHandler(new AbstractHandler("att"));

                        String sensor = atts.getValue("name");
                        String value = atts.getValue("value");

                        int sensorID = -1;
                        try {
                            sensorID = Integer.parseInt(sensor);
                        } catch (Exception exn) {
                            r.raiseException(exn);
                        }

                        Sensor.Port port = null;
                        for (Sensor.Port p : Sensor.Port.values()) {
                            if (p.getID() == sensorID) {
                                port = p;
                            }
                        }
                        SensorType type = null;
                        for (SensorType t : SensorType.values()) {
                            if (t.name().equals(value)) {
                                type = t;
                            }
                        }
                        if ((port != null) && (type != null)) {
                            Settings.this.sensorTypes.get(port).setValue(type);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {

        BrickDescription<?> nxt = this.nxt.getValue();
        if ((nxt != null) && (nxt.getInterfaceType() != null)) {
            Graph.printAtt(out, "nxt", "nxt", null);
            Graph.printAtt(out, "factory", nxt.getClass().getName());
            Graph.printAtt(out, "name", nxt.getBrickName());
            Graph.printAtt(out, "type", nxt.getInterfaceType().name());
            if (nxt.getPort() != null) {
                Graph.printAtt(out, "port", nxt.getPort());
            }
            Graph.printAtt(out, "uri", nxt.getURI());
            out.closeElement("att");

            Graph.printAtt(out, "sensor", "sensor", null);
            for (Sensor.Port port : this.sensorTypes.keySet()) {
                Graph.printAtt(out, String.valueOf(port.getID()), this.sensorTypes.get(
                        port).getValue()
                        .name());
            }
            out.closeElement("att");
        }
    }

    public SensorType getSensorType(Sensor.Port port) {

        return this.sensorTypes.get(port).getValue();
    }

    public Nxt createBrick(Component parent)
            throws IOException, UserCanceledException {

        if (this.nxt.getValue() != null) {
            return this.nxt.getValue().createBrick(parent);
        } else {
            return null;
        }
    }

    @Override
    public NxtRuntime createRuntime(Component parent)
            throws Exception {

        Map<Sensor.Port, SensorType> sensorTypes
                = new HashMap<Sensor.Port, SensorType>();
        for (Sensor.Port port : this.sensorTypes.keySet()) {
            sensorTypes.put(port, this.getSensorType(port));
        }
        return new NxtRuntime(this.createBrick(parent), sensorTypes);
    }
}
