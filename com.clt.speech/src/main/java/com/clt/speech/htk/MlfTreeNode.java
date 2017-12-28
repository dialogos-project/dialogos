/*
 * @(#)MlfTreeNode.java
 * Created on 15.11.2006 by dabo
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

package com.clt.speech.htk;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

/**
 * @author dabo
 * 
 */
public class MlfTreeNode
    implements TreeNode {

  private MlfTreeNode parent;
  private MlfNode n;
  private MlfTreeNode[] children;


  public MlfTreeNode(MlfNode n) {

    this(null, n);
  }


  public MlfTreeNode(MlfTreeNode parent, MlfNode n) {

    this.parent = parent;
    this.n = n;
    if (n instanceof MlfNonterminalNode) {
      MlfNonterminalNode nt = (MlfNonterminalNode)n;
      this.children = new MlfTreeNode[nt.numChildren()];
      for (int i = 0; i < nt.numChildren(); i++) {
        this.children[i] = new MlfTreeNode(this, nt.getChild(i));
      }
    }
    else {
      this.children = new MlfTreeNode[0];
    }
  }


  public TreeNode getChildAt(int childIndex) {

    return this.children[childIndex];
  }


  public int getChildCount() {

    return this.children.length;
  }


  public TreeNode getParent() {

    return this.parent;
  }


  public int getIndex(TreeNode node) {

    for (int i = 0; i < this.children.length; i++) {
      if (node == this.children[i]) {
        return i;
      }
    }
    return -1;
  }


  public boolean getAllowsChildren() {

    return this.n instanceof MlfNonterminalNode;
  }


  public boolean isLeaf() {

    return this.n instanceof MlfTerminalNode;
  }


  public Enumeration<MlfTreeNode> children() {

    return new Enumeration<MlfTreeNode>() {

      int i = 0;


      public boolean hasMoreElements() {

        return this.i < MlfTreeNode.this.children.length;
      }


      public MlfTreeNode nextElement() {

        return MlfTreeNode.this.children[this.i++];
      }
    };
  }


  @Override
  public String toString() {

    return this.n.toString();
  }
}