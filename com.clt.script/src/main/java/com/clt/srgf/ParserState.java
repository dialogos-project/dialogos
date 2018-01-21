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
