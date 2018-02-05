/*
 * @(#)Plugin.java
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

package com.clt.dialogos.lego.nxt;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;

import javax.swing.Icon;

import com.clt.dialogos.lego.nxt.nodes.MotorNode;
import com.clt.dialogos.lego.nxt.nodes.ProgramNode;
import com.clt.dialogos.lego.nxt.nodes.ReadSensorNode;
import com.clt.dialogos.lego.nxt.nodes.StopProgramNode;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.event.ProgressListener;
import com.clt.gui.Images;

/**
 * @author dabo
 * 
 */
public class Plugin implements com.clt.dialogos.plugin.Plugin {

  public Plugin(Component parent, File appDir, ClassLoader classLoader, ProgressListener progress, int userData) {
    com.clt.diamant.graph.Node.registerNodeTypes(this.getName(), Arrays.asList(new Class<?>[] { ProgramNode.class, StopProgramNode.class, ReadSensorNode.class, MotorNode.class }));
        // SendMessageNode.class
//      }));
  }


  @Override
  public String getId() {
    return "dialogos.plugin.lego";
  }


  @Override
  public String getName() {
    return "Lego Mindstorms NXT";
  }


  @Override
  public Icon getIcon() {
    return Images.load(this, "Lego.png");
  }


  @Override
  public String getVersion() {
    return "1.2";
  }


  @Override
  public PluginSettings createDefaultSettings() {
    return new Settings();
  }
}
