package com.clt.diamant.graph.nodes;

import java.io.PrintStream;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.script.cmd.ExecutionException;
import com.clt.script.exp.EvaluationException;
import com.clt.util.StringTools;

/**
 * A NodeExecutionExeption should be thrown when an exception occurs while
 * executing a node.
 *
 */
public class NodeExecutionException extends RuntimeException {

    private Node _node;

    /**
     * Creates new {@link NodeExecutionException} and logs it to the
     * {@link ExecutionLogger}
     *
     * @param n Node that threw the exception
     * @param message exception message
     * @param exn
     * @param logger the Exception is going to be logged to this logger
     */
    public NodeExecutionException(Node n, String message, Throwable exn, ExecutionLogger logger) {
        this(n, message, exn);
        logger.logException(this.getClass().getSimpleName(), n.getTitle(), message);
    }

    /**
     * Creates new {@link NodeExecutionException}
     *
     * @param n Node that threw the exception
     * @param message exception message
     * @param exn
     */
    public NodeExecutionException(Node n, String message, Throwable exn) {
        super(message, exn);

        this._node = n;
    }

    /**
     * Creates new {@link NodeExecutionException} and logs it to the
     * {@link ExecutionLogger}
     *
     * @param n Node that threw the exception
     * @param message exception message
     * @param logger the Exception is going to be logged to this logger
     */
    public NodeExecutionException(Node n, String message, ExecutionLogger logger) {
        this(n, message);
        logger.logException(this.getClass().getSimpleName(), n.getTitle(), message);
    }

    /**
     * Creates new {@link NodeExecutionException}
     *
     * @param n Node that threw the exception
     * @param message exception message
     */
    public NodeExecutionException(Node n, String message) {
        this(n, message, (Throwable) null);
    }

    @Override
    public String getMessage() {

        Throwable cause = this.getCause();
        if (cause != null) {
            String s = super.getMessage();
            if (!StringTools.isEmpty(s)) {
                s += ":\n";
            }
            if ((cause instanceof EvaluationException) || (cause instanceof ExecutionException)) {
                s += cause.getLocalizedMessage();
            } else {
                s += cause.toString();
            }
            return s;
        } else {
            return super.getMessage() + ".";
        }
    }

    public Node getNode() {

        return this._node;
    }

    public Throwable getException() {

        return this.getCause();
    }

    @Override
    public String toString() {

        if (this._node == null) {
            return "ExecutionException: " + this.getMessage();
        } else {
            return Resources.getString("ErrorAtNode") + " \"" + this._node.nodePath(false) + "\": " + this.getMessage();
        }
    }

    @Override
    public void printStackTrace() {

        this.printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream out) {

        synchronized (out) {
            out.println(Resources.getString("ErrorAtNode") + "\n   " + this._node.getTitle());
            Graph g = this._node.getGraph();
            while (g != null) {
                out.println("in " + g.getName());
                g = g.getSuperGraph();
            }
            if (this.getCause() != null) {
                this.getCause().printStackTrace(out);
            } else {
                super.printStackTrace(out);
            }
        }
    }
}
