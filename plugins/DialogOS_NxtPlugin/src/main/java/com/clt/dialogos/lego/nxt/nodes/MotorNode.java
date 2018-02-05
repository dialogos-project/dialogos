/*
 * @(#)MotorNode.java
 * Created on 04.03.2007 by dabo
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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.xml.sax.SAXException;

import com.clt.dialogos.lego.nxt.Node;
import com.clt.dialogos.lego.nxt.NxtRuntime;
import com.clt.dialogos.lego.nxt.Plugin;
import com.clt.dialogos.lego.nxt.Resources;
import com.clt.diamant.IdMap;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.lego.nxt.Motor;
import com.clt.lego.nxt.Nxt;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 * 
 */
public class MotorNode
    extends Node {

  private static final String MOTOR = "motor";
  private static final String STATE = "state";
  private static final String POWER = "power";

  // Do not change names! These are written to XML.
  enum State {
        FORWARD,
        BACKWARD,
        STOP,
        DRIFT;

    @Override
    public String toString() {

      return Resources.getString("MOTORSTATE_" + this.name());
    }
  };


  public MotorNode() {

    this.setColor(new Color(255, 255, 153));

    this.setProperty(MotorNode.MOTOR, Motor.Port.A);
    this.setProperty(MotorNode.STATE, State.DRIFT);
    this.setProperty(MotorNode.POWER, "80");

    this.addEdge();
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

      Motor.Port port = (Motor.Port)this.getProperty(MotorNode.MOTOR);
      if (port == null) {
        throw new NodeExecutionException(this, Resources
          .getString("NoMotorSelected"));
      }

      State state = (State)this.getProperty(MotorNode.STATE);
      if (state == null) {
        throw new NodeExecutionException(this, Resources
          .getString("NoMotorStateSelected"));
      }

      Motor motor = new Motor(brick, port);
      String power = (String)this.getProperty(MotorNode.POWER);
      if (power != null) {
        Value pwr = this.parseExpression(power).evaluate(comm);
        if (pwr instanceof IntValue) {
          motor.setPower((int)((IntValue)pwr).getInt());
        }
        else {
          throw new NodeExecutionException(this, Resources
            .getString("IllegalPowerValue"));
        }
      }

      switch (state) {
        case FORWARD:
          motor.forward();
          break;
        case BACKWARD:
          motor.backward();
          break;
        case STOP:
          motor.stop();
          break;
        case DRIFT:
          motor.drift();
          break;
      }
    } catch (NodeExecutionException exn) {
      throw exn;
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources
        .getString("CouldNotControlMotor"), exn);
    }

    return 0;
  }


  @Override
  protected JComponent createEditorComponentImpl(Map<String, Object> properties) {

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(3, 3, 3, 3);

    p.add(new JLabel(Resources.getString("MotorPort") + ':'), gbc);
    gbc.gridx++;
    /*
     * final JComboBox motor = NodePropertiesDialog.createComboBox(properties,
     * MOTOR, Motor.Port.values());
     */
    JRadioButton ports[] =
      NodePropertiesDialog.createRadioButtons(properties, MotorNode.MOTOR,
            Motor.Port.values());
    JPanel motor = new JPanel(new GridLayout(1, 0, 12, 12));
    for (JRadioButton b : ports) {
      motor.add(b);
    }
    p.add(motor, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    p.add(new JLabel(Resources.getString("MotorState") + ':'), gbc);
    gbc.gridx++;
    final JComboBox state =
      NodePropertiesDialog.createComboBox(properties, MotorNode.STATE,
            State.values());
    p.add(state, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    p.add(new JLabel(Resources.getString("MotorPower") + ':'), gbc);
    gbc.gridx++;
    final JTextField power =
      NodePropertiesDialog.createTextField(properties, MotorNode.POWER);
    p.add(power, gbc);

    ItemListener l = new ItemListener() {

      public void itemStateChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.SELECTED) {
          Object item = e.getItem();

          power.setEnabled((item == State.FORWARD) || (item == State.BACKWARD));
        }
      }
    };
    state.addItemListener(l);
    l.itemStateChanged(new ItemEvent(state, ItemEvent.ITEM_STATE_CHANGED,
            properties.get(MotorNode.STATE), ItemEvent.SELECTED));

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weighty = 1.0;
    p.add(Box.createVerticalGlue(), gbc);

    return p;
  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(MotorNode.MOTOR)) {
      for (Motor.Port port : Motor.Port.values()) {
        if (String.valueOf(port.getID()).equals(value)) {
          this.setProperty(MotorNode.MOTOR, port);
          break;
        }
      }
      if (this.getProperty(MotorNode.MOTOR) == null) {
        r.raiseException(Resources.format("UnknownMotor", value));
      }
    }
    else if (name.equals(MotorNode.STATE)) {
      for (State state : State.values()) {
        if (state.name().equals(value)) {
          this.setProperty(MotorNode.STATE, state);
          break;
        }
      }
      if (this.getProperty(MotorNode.STATE) == null) {
        r.raiseException(Resources.format("UnknownMotorState", value));
      }
    }
    else if (name.equals(MotorNode.POWER)) {
      this.setProperty(MotorNode.POWER, value);
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    Motor.Port port = (Motor.Port)this.getProperty(MotorNode.MOTOR);
    if (port != null) {
      Graph.printAtt(out, MotorNode.MOTOR, port.getID());
    }

    State state = (State)this.getProperty(MotorNode.STATE);
    if (state != null) {
      Graph.printAtt(out, MotorNode.STATE, state.name());
    }

    if (this.getProperty(MotorNode.POWER) != null) {
      Graph.printAtt(out, MotorNode.POWER, (String)this
        .getProperty(MotorNode.POWER));
    }
  }
}
