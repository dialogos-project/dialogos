/*
 * @(#)Mapping.java
 * Created on Fri Aug 27 2004
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

package com.clt.diamant;

import java.util.HashMap;
import java.util.Map;

import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Functions;
import com.clt.diamant.graph.InputHandler;
import com.clt.diamant.graph.Node;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Mapping {

  private Map<Node, Node> nodes;
  private Map<InputHandler, InputHandler> handlers;
  private Map<Device, Device> devices;
  private Map<Slot, Slot> variables;
  private Map<Edge, Edge> edges;
  private Map<Functions, Functions> functions;
  private Map<Grammar, Grammar> grammars;


  public Mapping() {

    this(null, null, null, null, null);
  }


  private Mapping(Map<Slot, Slot> variables, Map<Device, Device> devices,
      Map<Node, Node> nodes,
                    Map<InputHandler, InputHandler> handlers,
      Map<Edge, Edge> edges) {

    this.nodes = nodes;
    this.devices = devices;
    this.variables = variables;
    this.handlers = handlers;
    this.edges = edges;
  }


  public void addVariable(Slot source, Slot target) {

    if (this.variables == null) {
      this.variables = new HashMap<Slot, Slot>();
    }
    Mapping.put(this.variables, source, target);
  }


  public Slot getVariable(Slot source) {

    return Mapping.get(this.variables, source);
  }


  public void addDevice(Device source, Device target) {

    if (this.devices == null) {
      this.devices = new HashMap<Device, Device>();
    }
    Mapping.put(this.devices, source, target);
  }


  public Device getDevice(Device source) {

    return Mapping.get(this.devices, source);
  }


  public void addNode(Node source, Node target) {

    if (this.nodes == null) {
      this.nodes = new HashMap<Node, Node>();
    }
    Mapping.put(this.nodes, source, target);
  }


  public Node getNode(Node source) {

    return Mapping.get(this.nodes, source);
  }


  public void addHandler(InputHandler source, InputHandler target) {

    if (this.handlers == null) {
      this.handlers = new HashMap<InputHandler, InputHandler>();
    }
    Mapping.put(this.handlers, source, target);
  }


  public InputHandler getHandler(InputHandler source) {

    return Mapping.get(this.handlers, source);
  }


  public void addEdge(Edge source, Edge target) {

    if (this.edges == null) {
      this.edges = new HashMap<Edge, Edge>();
    }
    Mapping.put(this.edges, source, target);
  }


  public Edge getEdge(Edge source) {

    return Mapping.get(this.edges, source);
  }


  public void addFunctions(Functions source, Functions target) {

    if (this.functions == null) {
      this.functions = new HashMap<Functions, Functions>();
    }
    Mapping.put(this.functions, source, target);
  }


  public Functions getFunctions(Functions source) {

    return Mapping.get(this.functions, source);
  }


  public void addGrammar(Grammar source, Grammar target) {

    if (this.grammars == null) {
      this.grammars = new HashMap<Grammar, Grammar>();
    }
    Mapping.put(this.grammars, source, target);
  }


  public Grammar getGrammar(Grammar source) {

    return Mapping.get(this.grammars, source);
  }


  private static <T> void put(Map<T, T> map, T key, T value) {

    map.put(key, value);
  }


  private static <T> T get(Map<T, T> map, T key) {

    if ((map == null) || (key == null)) {
      return key;
    }
    else {
      T result = map.get(key);
      if (result == null) {
        return key;
      }
      else {
        return result;
      }
    }
  }

}
