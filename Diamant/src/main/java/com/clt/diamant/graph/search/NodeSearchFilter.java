/*
 * @(#)NodeSearchFilter.java
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

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class NodeSearchFilter {

  /** Return a collection of SearchResults */
  public abstract Collection<? extends SearchResult> match(Node n);

  static class Relation {

    String name;


    public Relation(String name) {

      this.name = name;
    }


    @Override
    public String toString() {

      return Resources.getString(this.name);
    }
  }
}