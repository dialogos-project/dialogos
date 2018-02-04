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
