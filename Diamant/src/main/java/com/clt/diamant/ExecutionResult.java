package com.clt.diamant;

import com.clt.diamant.graph.Node;

public class ExecutionResult {

    public static final int ERROR = 0;
    public static final int INFORMATION = 1;

    private int type;
    private Object message;
    private Node node;

    public ExecutionResult(int type, Object message, Node node) {

        this.type = type;
        this.message = message;
        this.node = node;
    }

    public int getType() {

        return this.type;
    }

    public Object getMessage() {

        return this.message;
    }

    public Node getNode() {

        return this.node;
    }

    @Override
    public String toString() {

        return (this.getNode() != null ? Resources.format("ExecutionStoppedAtNode",
                this.getNode())
                + " "
                : "")
                + this.getMessage();
    }
}
