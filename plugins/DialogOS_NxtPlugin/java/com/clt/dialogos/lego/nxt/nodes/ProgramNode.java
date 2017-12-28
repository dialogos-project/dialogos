/*
 * @(#)ProgramNode.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.xml.sax.SAXException;

import com.clt.dialogos.lego.nxt.Node;
import com.clt.dialogos.lego.nxt.NxtRuntime;
import com.clt.dialogos.lego.nxt.Plugin;
import com.clt.dialogos.lego.nxt.Resources;
import com.clt.dialogos.lego.nxt.Settings;
import com.clt.diamant.IdMap;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.event.ProgressListener;
import com.clt.gui.CmdButton;
import com.clt.gui.ListSelectionDialog;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.lego.nxt.Nxt;
import com.clt.script.cmd.ExecutionException;
import com.clt.util.AbstractLongCallable;
import com.clt.util.StringTools;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 * 
 */
public class ProgramNode
    extends Node {

  private static final String PROGRAM_NAME = "program";
  private static final String WAIT = "wait";


  public ProgramNode() {

    this.addEdge();

    this.setProperty(ProgramNode.WAIT, Boolean.TRUE);
  }


  @Override
  protected JComponent createEditorComponentImpl(
      final Map<String, Object> properties) {

    final JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(3, 3, 3, 3);

    p.add(new JLabel(Resources.getString("ProgramName") + ':'), gbc);
    gbc.gridx++;
    gbc.weightx = 1.0;
    final JTextComponent programName =
      NodePropertiesDialog.createTextField(properties,
            ProgramNode.PROGRAM_NAME);
    p.add(programName, gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    p.add(new CmdButton(new Runnable() {

      public void run() {

        final Settings settings =
          (Settings)ProgramNode.this.getPluginSettings(Plugin.class);
        try {
          String[] programs =
            new ProgressDialog(p, 200).run(new AbstractLongCallable<String[]>()
                    {

                      @Override
                      public String getDescription()
                        {

                          return Resources.getString("RetrievingProgramNames");
                        }


                      @Override
                      protected String[] call(ProgressListener l)
                          throws Exception
                        {

                          String[] programs = null;
                          Nxt brick = settings.createBrick(p);
                          if (brick != null) {
                            try {
                              programs = brick.getPrograms();
                            }
                                finally {
                                  brick.close();
                                }
                              }
                              return programs;
                            }
                    });

          if (programs == null) {
            OptionPane
              .error(p, Resources.getString("NoNxtBrickSelected") + ".");
          }
          else {
            if (programs.length == 0) {
              OptionPane.message(p, Resources.getString("NoProgramsOnBrick"));
            }
            else {
              String program =
                new ListSelectionDialog<String>(p,
                                Resources.getString("ChooseProgram"), null,
                  programs).getSelectedItem();
              if (program != null) {
                programName.setText(program);
              }
            }
          }
        }
                catch (java.lang.reflect.InvocationTargetException exn) {
                  OptionPane.error(p, exn.getTargetException());
                }
                catch (Exception exn) {
                  OptionPane.error(p, exn);
                }
              }
    }, Resources.getString("Choose") + "..."), gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 3;

    p.add(NodePropertiesDialog.createCheckBox(properties, ProgramNode.WAIT,
            Resources.getString("WaitUntilProgramFinished")), gbc);

    gbc.gridy++;
    gbc.weighty = 1.0;
    p.add(new JPanel(), gbc);

    return p;
  }


  @Override
  protected int executeNXT(WozInterface comm) {

    String program = (String)this.properties.get(ProgramNode.PROGRAM_NAME);
    if (program == null) {
      throw new com.clt.diamant.graph.nodes.NodeExecutionException(this,
              Resources.getString("NoNxtProgramSelected"));
    }

    try {
      NxtRuntime runtime =
        (NxtRuntime)this.getPluginRuntime(Plugin.class, comm);
      if (runtime.getBrick() == null) {
        throw new ExecutionException(Resources.getString("NoNxtBrickSelected"));
      }
      runtime.getBrick().startProgram(program);

      if (this.getBooleanProperty(ProgramNode.WAIT)) {
        while (runtime.getBrick().getCurrentProgram() != null) {
          Thread.sleep(50);
        }
      }
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources
        .getString("CouldNotStartProgram"), exn);
    }
    return 0;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);

    String program = (String)this.getProperty(ProgramNode.PROGRAM_NAME);
    if (StringTools.isEmpty(program)) {
      this.reportError(errors, false, Resources
        .getString("NoNxtProgramSelected"));
    }
  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(ProgramNode.PROGRAM_NAME)) {
      this.setProperty(ProgramNode.PROGRAM_NAME, value);
    }
    else if (name.equals(ProgramNode.WAIT)) {
      this.setProperty(ProgramNode.WAIT, value.equals("1"));
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    String program = (String)this.getProperty(ProgramNode.PROGRAM_NAME);
    if (!StringTools.isEmpty(program)) {
      Graph.printAtt(out, ProgramNode.PROGRAM_NAME, program);
    }
    Graph.printAtt(out, ProgramNode.WAIT, this
      .getBooleanProperty(ProgramNode.WAIT));
  }

}
