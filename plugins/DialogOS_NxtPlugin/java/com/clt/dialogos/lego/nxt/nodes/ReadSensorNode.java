/*
 * @(#)SensorNode.java
 * Created on 05.03.2007 by dabo
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

package com.clt.dialogos.lego.nxt.nodes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.dialogos.lego.nxt.Node;
import com.clt.dialogos.lego.nxt.NxtRuntime;
import com.clt.dialogos.lego.nxt.Plugin;
import com.clt.dialogos.lego.nxt.Resources;
import com.clt.dialogos.lego.nxt.SensorType;
import com.clt.dialogos.lego.nxt.Settings;
import com.clt.diamant.IdMap;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.lego.nxt.Nxt;
import com.clt.lego.nxt.Sensor;
import com.clt.script.exp.values.IntValue;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 * 
 */
public class ReadSensorNode
    extends Node {

  private static final String SENSOR = "sensor";
  private static final String MODE = "mode";
  private static final String ACTIVATE = "activate";
  private static final String VARIABLE = "variable";


  // Don't change names. They are written to XML
  public ReadSensorNode() {

    this.setProperty(ReadSensorNode.MODE, Sensor.Mode.RAW);
    this.setProperty(ReadSensorNode.SENSOR, new SensorPort(Sensor.Port.S1));
    this.addEdge();
  }


  public static Color getDefaultColor() {

    return new Color(255, 255, 153);
  }


  @Override
  protected JComponent createEditorComponentImpl(
      final Map<String, Object> properties) {

    final JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(3, 3, 3, 3);

    SensorPort ports[] = new SensorPort[Sensor.Port.values().length];
    int i = 0;
    for (Sensor.Port port : Sensor.Port.values()) {
      ports[i++] = new SensorPort(port);
    }

    p.add(new JLabel(Resources.getString("SensorPort") + ':'), gbc);
    gbc.gridx++;
    final JComboBox sensor =
      NodePropertiesDialog.createComboBox(properties, ReadSensorNode.SENSOR,
        ports);
    this.setProperty(ReadSensorNode.SENSOR, ports[0]);
    // sensor.setSelectedItem(ports[0]);

    p.add(sensor, gbc);

    final JCheckBox activate =
      NodePropertiesDialog.createCheckBox(properties, ReadSensorNode.ACTIVATE,
            Resources.getString("ActivateSensor"));

    Sensor.Mode[] modes =
      new Sensor.Mode[] { Sensor.Mode.RAW, Sensor.Mode.BOOLEAN,
                Sensor.Mode.PERCENTAGE };
    final JComboBox sensorMode =
      NodePropertiesDialog.createComboBox(properties, ReadSensorNode.MODE,
        modes);

    final JPanel options = new JPanel(new GridLayout(1, 1));

    ItemListener typeListener = new ItemListener() {

      public void itemStateChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.SELECTED) {
          SensorPort port = (SensorPort)e.getItem();
          SensorType value = null;
          if (port != null) {
            value = port.getType();
            if (value == SensorType.ULTRASONIC) {
              sensorMode.setSelectedItem(Sensor.Mode.RAW);
              sensorMode.setEnabled(false);
            }
            else {
              sensorMode.setEnabled(true);
            }
          }

          options.removeAll();
          if (value == SensorType.LIGHT) {
            options.add(activate);
          }
          if (p.isShowing()) {
            p.revalidate();
            p.repaint();
          }
        }
      }
    };
    sensor.addItemListener(typeListener);
    typeListener.itemStateChanged(new ItemEvent(sensor,
      ItemEvent.ITEM_STATE_CHANGED,
            properties.get(ReadSensorNode.SENSOR), ItemEvent.SELECTED));

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    // gbc.weightx = 1.0;
    p.add(options, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    p.add(new JLabel(Resources.getString("SensorMode") + ':'), gbc);
    gbc.gridx++;
    p.add(sensorMode, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    p.add(new JLabel(Resources.getString("SaveInVariable") + ':'), gbc);
    gbc.gridx++;
    p.add(NodePropertiesDialog.createComboBox(properties,
      ReadSensorNode.VARIABLE, this.getGraph().getAllVariables(
            Graph.LOCAL)), gbc);

    gbc.gridy++;
    gbc.weighty = 1.0;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    p.add(new JPanel(), gbc);

    return p;
  }


  @Override
  protected int executeNXT(WozInterface comm) {

    try {
      NxtRuntime runtime =
        (NxtRuntime)this.getPluginRuntime(Plugin.class, comm);
      Nxt brick = runtime.getBrick();
      if (brick == null) {
        throw new NodeExecutionException(this, Resources
          .getString("NoNxtBrickSelected"));
      }

      SensorPort sensorPort =
        (SensorPort)this.getProperty(ReadSensorNode.SENSOR);
      if (sensorPort == null) {
        throw new NodeExecutionException(this, Resources
          .getString("NoSensorSelected"));
      }
      boolean activate = this.getBooleanProperty(ReadSensorNode.ACTIVATE);
      Sensor.Mode mode = (Sensor.Mode)this.getProperty(ReadSensorNode.MODE);
      if (mode == null) {
        throw new NodeExecutionException(this, Resources
          .getString("SensorModeNotSet"));
      }

      Slot v = (Slot)this.getProperty(ReadSensorNode.VARIABLE);
      if (v == null) {
        throw new NodeExecutionException(this,
                  com.clt.diamant.Resources.getString("NoVariableAssigned"));
      }

      Sensor sensor = new Sensor(brick, sensorPort.getPort());
      SensorType type = runtime.getSensorType(sensorPort.getPort());
      switch (type) {
        case TOUCH:
          sensor.setType(Sensor.Type.SWITCH, mode);
          break;
        case LIGHT:
          sensor.setType(activate ? Sensor.Type.LIGHT_ON
            : Sensor.Type.LIGHT_OFF, mode);
          break;
        case SOUND:
          sensor.setType(Sensor.Type.SOUND_DB, mode);
          break;
        case ULTRASONIC:
          sensor.setType(Sensor.Type.I2C_9V, mode);
          break;
        default:
          throw new NodeExecutionException(this, Resources
            .getString("SensorTypeNotSet"));
      }

      int value = sensor.getValue();
      v.setValue(new IntValue(value));
    } catch (NodeExecutionException exn) {
      throw exn;
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources
        .getString("CouldNotReadSensor"), exn);
    }
    return 0;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);

    SensorPort sensor = (SensorPort)this.getProperty(ReadSensorNode.SENSOR);
    if (sensor == null) {
      this.reportError(errors, false, Resources.getString("NoSensorSelected"));
    }
    else {
      SensorType type = sensor.getType();
      if ((type == null) || (type == SensorType.NONE)) {
        this
          .reportError(errors, false, Resources.getString("SensorTypeNotSet"));
      }
    }

    Slot v = (Slot)this.getProperty(ReadSensorNode.VARIABLE);
    if (v != null) {
      if (!this.getGraph().getAllVariables(Graph.LOCAL).contains(v)) {
        this.reportError(errors, false, com.clt.diamant.Resources.format(
                  "referencesInaccessibleVariable", v.getName()));
      }

      if (v.getType() != com.clt.script.exp.Type.Int) {
        this.reportError(errors, false, Resources.format("usesNonIntVariable",
          v.getName()));
      }
    }
    else {
      this.reportError(errors, false, com.clt.diamant.Resources
        .getString("hasNoVariableAssigned"));
    }

  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(ReadSensorNode.SENSOR)) {
      for (Sensor.Port s : Sensor.Port.values()) {
        if (String.valueOf(s.getID()).equals(value)) {
          this.setProperty(ReadSensorNode.SENSOR, new SensorPort(s));
          break;
        }
      }
      if (this.getProperty(ReadSensorNode.SENSOR) == null) {
        r.raiseException(Resources.format("UnknownSensor", value));
      }
    }
    else if (name.equals(ReadSensorNode.MODE)) {
      for (Sensor.Mode mode : Sensor.Mode.values()) {
        if (String.valueOf(mode.getValue()).equals(value)) {
          this.setProperty(ReadSensorNode.MODE, mode);
          break;
        }
      }
      if (this.getProperty(ReadSensorNode.SENSOR) == null) {
        r.raiseException(Resources.format("UnknownSensor", value));
      }
    }
    else if (name.equals(ReadSensorNode.VARIABLE) && (value != null)) {
      try {
        this.setProperty(ReadSensorNode.VARIABLE, uid_map.variables.get(value));
      } catch (Exception exn) {
        r.raiseException(com.clt.diamant.Resources.format("UnknownVariable",
          "ID " + value));
      }
    }
    else if (name.equals(ReadSensorNode.ACTIVATE)) {
      this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    SensorPort sensor = (SensorPort)this.getProperty(ReadSensorNode.SENSOR);
    if (sensor != null) {
      Graph.printAtt(out, ReadSensorNode.SENSOR, sensor.getPort().getID());
    }

    Slot v = (Slot)this.getProperty(ReadSensorNode.VARIABLE);
    if (v != null) {
      try {
        String uid = uid_map.variables.getKey(v);
        Graph.printAtt(out, ReadSensorNode.VARIABLE, uid);
      } catch (Exception exn) {
      } // variable deleted
    }

    if (this.getProperty(ReadSensorNode.MODE) != null) {
      System.out.println(((Sensor.Mode)this.getProperty(ReadSensorNode.MODE))
        .getValue());
      System.out.println((this.getProperty(ReadSensorNode.MODE)));
      Graph.printAtt(out, ReadSensorNode.MODE, ((Sensor.Mode)this
        .getProperty(ReadSensorNode.MODE)).getValue());
    }
    if (this.getBooleanProperty(ReadSensorNode.ACTIVATE)) {
      Graph.printAtt(out, ReadSensorNode.ACTIVATE, true);
    }
  }

  private class SensorPort {

    private Sensor.Port port;


    public SensorPort(Sensor.Port port) {

      this.port = port;
    }


    public Sensor.Port getPort() {

      return this.port;
    }


    public SensorType getType() {

      return ((Settings)ReadSensorNode.this.getPluginSettings(Plugin.class))
        .getSensorType(this.port);
    }


    @Override
    public int hashCode() {

      return this.port.hashCode();
    }


    @Override
    public boolean equals(Object o) {

      if (o instanceof SensorPort) {
        return ((SensorPort)o).getPort().equals(this.getPort());
      }
      else {
        return false;
      }
    }


    @Override
    public String toString() {

      return this.port + " (" + this.getType() + ")";
    }
  }
}
