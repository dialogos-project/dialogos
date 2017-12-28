/*
 * @(#)ScriptNode.java
 * Created on Fri Oct 24 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.diamant.graph.nodes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.Script;
import com.clt.script.exp.EvaluationException;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ScriptNode
    extends Node {

  // do not change. These are written to XML.
  public static final String SCRIPT = "script";


  public ScriptNode() {

    super();

    this.addEdge();

    // setTitle(Resources.getString("Script"));
    this.setProperty(ScriptNode.SCRIPT, "");
  }


  public static Color getDefaultColor() {

    return new Color(153, 255, 255);
  }


  @Override
  protected JComponent createEditorComponent(Map<String, Object> properties) {

    JPanel p = new JPanel(new BorderLayout());

    // p.add(new JLabel(Resources.getString("Script") + ':'),
    // BorderLayout.NORTH);
    // p.add(NodePropertiesDialog.createTextArea(properties, "script"),
    // BorderLayout.CENTER);
    p.add(NodePropertiesDialog.createScriptEditor(properties, "script"),
      BorderLayout.CENTER);

    return p;
  }


  @Override
  public Node execute(WozInterface comm, InputCenter input, ExecutionLogger logger) {
	logNode(logger);
    Node target = this.getEdge(0).getTarget();

    Script s = null;
    try {
      // FIXME: bad bug fix: it is not possible to have an empty script.
      String script = (String)this.getProperty(ScriptNode.SCRIPT);
      if (script.equals("")) {
        s = Script.parseScript(" ", this.getGraph().getEnvironment(true));
      }
      else {
        s = Script.parseScript((String)this.getProperty(ScriptNode.SCRIPT),
                    this.getGraph().getEnvironment(true));
      }
    } catch (EvaluationException exn) {
      throw new NodeExecutionException(this, "", exn, logger);
    } catch (Exception exn) {
      throw new NodeExecutionException(this, Resources.getString("IllegalScript"), exn, logger);
    }

    s.execute(comm);

    comm.transition(this, target, 0, null);
    return target;
  }


  @Override
  public void validate(Collection<SearchResult> errors) {

    super.validate(errors);

    try {
      Collection<String> warnings = new ArrayList<String>();
      Script.parseScript((String)this.getProperty(ScriptNode.SCRIPT),
        this.getGraph().getEnvironment(true)).check(
                warnings);
      for (String warning : warnings) {
        this.reportError(errors, false, Resources
          .getString("containsProblematicExpression")
                      + ". " + warning);
      }
    } catch (Exception exn) {
      this.reportError(errors, false, Resources.getString("IllegalScript") + ": "
        + exn.getLocalizedMessage());
    }
  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    Graph.printTextAtt(out, ScriptNode.SCRIPT, (String)this
      .getProperty(ScriptNode.SCRIPT));
  }


  @Override
  protected void readAttribute(XMLReader r, String name, String value,
      IdMap uid_map)
        throws SAXException {

    if (name.equals(ScriptNode.SCRIPT)) {
      this.setProperty(name, value);
    }
    else {
      super.readAttribute(r, name, value, uid_map);
    }
  }


  @Override
  protected void writeVoiceXML(XMLWriter w, IdMap uid_map) {

    w.openElement("script");
    w.println("<![CDATA[");
    w.println((String)this.getProperty(ScriptNode.SCRIPT));
    w.println("]]>");
    w.closeElement("script");
  }
}