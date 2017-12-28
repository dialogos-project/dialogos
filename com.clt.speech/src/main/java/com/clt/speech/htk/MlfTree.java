/*
 * @(#)ParseTree.java
 * Created on Fri Sep 17 2004
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

package com.clt.speech.htk;

import javax.swing.JTree;

/**
 * A helper class to visualize an MLF tree.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class MlfTree extends JTree {

  public MlfTree(MlfNonterminalNode root) {

    super(new MlfTreeNode(null, root));
  }
}
