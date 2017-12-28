/*
 * @(#)SubGraph.java
 * Created on Wed Aug 25 2004
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

import com.clt.diamant.Mapping;
import com.clt.diamant.graph.nodes.ReturnNode;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class SubGraph extends Graph {

  public SubGraph(GraphOwner owner) {

    super(owner);
  }


  @Override
  public Graph clone(Mapping map) {

    SubGraph g = new SubGraph(null);
    g.copy(this, map);
    return g;
  }


  @Override
  @SuppressWarnings("unchecked")
  public Class<Node>[] supportedEndNodes() {

    return new Class[] { ReturnNode.class };
  }
}
