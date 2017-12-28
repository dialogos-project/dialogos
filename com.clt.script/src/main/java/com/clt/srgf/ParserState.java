/*
 * @(#)ParserState.java
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

package com.clt.srgf;

import java.util.Map;

import javax.swing.tree.TreeNode;

import com.clt.script.exp.Value;

/**
 * @author dabo
 * 
 */
public interface ParserState {

  long getStart();


  long getEnd();


  Value getValue();


  void setValue(Value value);


  String getText();


  Map<String, ? extends Value> getBinding();


  TreeNode getCurrentNode();
}
