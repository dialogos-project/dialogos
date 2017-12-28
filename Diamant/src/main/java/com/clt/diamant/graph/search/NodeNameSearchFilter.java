/*
 * @(#)NodeNameSearchFilter.java
 * Created on Tue Jul 19 2005
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

package com.clt.diamant.graph.search;

import java.util.Collection;
import java.util.Collections;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class NodeNameSearchFilter
    extends NodeSearchFilter {

  public static final String NAME = "NodeName";

  public static final Relation IS = new Relation("is"),
      CONTAINS = new Relation("contains");


  public static Object[] getRelations() {

    return new Relation[] { NodeNameSearchFilter.IS,
      NodeNameSearchFilter.CONTAINS };
  }

  private String name;
  private Object relation;


  public NodeNameSearchFilter(String name, Object relation) {

    this.name = name;
    this.relation = relation;
  }


  @Override
  public Collection<? extends SearchResult> match(Node n) {

    boolean match = false;
    if (this.relation == NodeNameSearchFilter.IS) {
      match = n.getTitle().equals(this.name);
    }
    else if (this.relation == NodeNameSearchFilter.CONTAINS) {
      match = n.getTitle().indexOf(this.name) >= 0;
    }
    else {
      throw new IllegalStateException("Unsupported relation: " + this.relation);
    }

    if (match) {
      return Collections.singleton(new NodeSearchResult(n, this.toString()));
    }
    else {
      return Collections.emptyList();
    }
  }


  @Override
  public String toString() {

    return Resources.getString(NodeNameSearchFilter.NAME) + " " + this.relation
      + " " + this.name;
  }
}