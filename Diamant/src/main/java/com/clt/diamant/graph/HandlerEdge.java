/*
 * @(#)HandlerEdge.java
 * Created on Mon Aug 30 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.diamant.graph;

import com.clt.diamant.IdMap;
import com.clt.diamant.graph.nodes.EndNode;
import com.clt.xml.XMLWriter;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class HandlerEdge
    extends SpecialEdge {

  // do not modifiy!
  public static final String TYPE = "input handler";

  private InputHandler handler;
  private EndNode endNode;


  public HandlerEdge(Node source) {

    this(source, null, null);
  }


  public HandlerEdge(Node source, Node target, InputHandler handler) {

    this(source, target, handler, null);
  }


  public HandlerEdge(Node source, Node target, InputHandler handler,
      EndNode endNode) {

    super(source, target);

    this.handler = handler;
    this.endNode = endNode;
  }


  @Override
  public Edge clone(Node newSource) {

    return new HandlerEdge(newSource, this.getTarget(), this.getHandler(), this
      .getEndNode());
  }


  public InputHandler getHandler() {

    return this.handler;
  }


  public EndNode getEndNode() {

    return this.endNode;
  }


  public void setHandler(InputHandler handler) {

    this.handler = handler;
  }


  public void setEndNode(EndNode endNode) {

    this.endNode = endNode;
  }


  @Override
  public String getCondition() {

    StringBuffer b = new StringBuffer();
    if (this.handler != null) {
      b.append(this.handler.getTitle());
      if (this.endNode != null) {
        b.append(": ");
        b.append(this.endNode.getTitle());
      }
    }
    return b.toString();
  }


  @Override
  public void setCondition(String condition) {

  }


  @Override
  protected void writeAttributes(XMLWriter out, IdMap uid_map) {

    super.writeAttributes(out, uid_map);

    if (this.getHandler() != null) {
      Graph.printAtt(out, "handler_uid", uid_map.inputHandlers.put(this
        .getHandler()));
    }
    if (this.getEndNode() != null) {
      Graph.printAtt(out, "node_uid", uid_map.nodes.put(this.getEndNode()));
    }
  }


  @Override
  public String getType() {

    return HandlerEdge.TYPE;
  }
}
