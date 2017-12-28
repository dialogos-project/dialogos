/*
 * @(#)SpecialEdge.java
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

/**
 * A tagging interface to indicate that an edge is automatically generated from
 * special external data (like global input handlers)
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class SpecialEdge extends Edge {

  public SpecialEdge(Node source) {

    super(source);
  }


  public SpecialEdge(Node source, Node target) {

    super(source, target);
  }


  public abstract String getType();
}
