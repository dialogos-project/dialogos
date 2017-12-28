/*
 * @(#)StopProgramNode.java
 * Created on 05.04.2007 by dabo
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

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
import com.clt.script.cmd.ExecutionException;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 * 
 */
public class StopProgramNode
    extends Node {

  private static final String CHECK_RUNNING = "checkRunning";


  public StopProgramNode() {

    this.setProperty(StopProgramNode.CHECK_RUNNING, Boolean.FALSE);
    this.addEdge(Resources.getString("ProgramStopped"));
  }


  @Override
  protected JComponent createEditorComponentImpl(Map<String, Object> properties) {

    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

    p.add(NodePropertiesDialog.createCheckBox(properties,
      StopProgramNode.CHECK_RUNNING,
            Resources.getString("CheckIfRunning")));

    return p;
  }


  @Override
  public boolean editProperties(Component parent) {

    boolean oldCheck = this.getBooleanProperty(StopProgramNode.CHECK_RUNNING);
    if (super.editProperties(parent)) {
      boolean newCheck = this.getBooleanProperty(StopProgramNode.CHECK_RUNNING);
      if (!oldCheck && newCheck) {
        this.getEdge(0).setCondition(Resources.getString("ProgramStopped"));
        this.addEdge(Resources.getString("NoProgramRunning"));
      }
      else if (oldCheck && !newCheck) {
        this.removeEdge(1);
      }

      return true;
    }
    else {
      return false;
    }
  }


  @Override
  protected int executeNXT(WozInterface comm) {

    try {
      NxtRuntime runtime =
        (NxtRuntime)this.getPluginRuntime(Plugin.class, comm);
      if (runtime.getBrick() == null) {
        throw new ExecutionException(Resources.getString("NoNxtBrickSelected"));
      }
      boolean stopped = runtime.getBrick().stopProgram();
      boolean check = this.getBooleanProperty(StopProgramNode.CHECK_RUNNING);
      if (!stopped && check) {
        return 1;
      }
      else {
        return 0;
      }
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources
        .getString("CouldNotStopProgram"), exn);
    }
  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(StopProgramNode.CHECK_RUNNING)) {
      boolean check = value.equalsIgnoreCase("1");
      this.setProperty(StopProgramNode.CHECK_RUNNING, check ? Boolean.TRUE
        : Boolean.FALSE);
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    if (this.getBooleanProperty(StopProgramNode.CHECK_RUNNING)) {
      Graph.printAtt(out, StopProgramNode.CHECK_RUNNING, true);
    }
  }

}
