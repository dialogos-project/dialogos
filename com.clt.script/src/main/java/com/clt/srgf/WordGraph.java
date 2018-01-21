package com.clt.srgf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

class WordGraph {

    private StartNode start;
    private EndNode end;
    private boolean isRoot;

    public WordGraph(Expansion e, boolean mergePrivateRules, Tokenizer tokenizer) {

        this(e, mergePrivateRules, tokenizer, false);
    }

    public WordGraph(Expansion e, boolean mergePrivateRules, Tokenizer tokenizer, boolean isRoot) {

        this.start = new StartNode();
        this.end = new EndNode();
        this.isRoot = isRoot;

        Node start = this.start;
        Node end = this.end;
        if (isRoot) {
            start = new WordNode("!ENTER");
            this.start.addEdge(start);

            end = new WordNode("!EXIT");
            end.addEdge(this.end);
        }

        Node[] nodes  = e.createWordGraph(new Node[]{start}, mergePrivateRules, tokenizer);
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].addEdge(end);
        }
    }

    public StartNode getStart() {

        return this.start;
    }

    private int collectNodes(Collection<Node> nodes) {

        return this.collectNodes(this.getStart(), nodes);
    }

    private int collectNodes(Node n, Collection<Node> nodes) {

        int edges = 0;
        if (!nodes.contains(n)) {
            nodes.add(n);
            for (Edge e : n.edges()) {
                edges++;
                if (e.getTarget() != null) {
                    edges += this.collectNodes(e.getTarget(), nodes);
                }
            }
        }
        return edges;
    }

    void print(PrintWriter w) {

        final Collection<Node> nodes = new HashSet<Node>();

        int numEdges = this.collectNodes(nodes);

        if (!this.isRoot) {
            boolean onlyWords = true;
            for (Iterator<Node> it = nodes.iterator(); it.hasNext() && onlyWords;) {
                Node n = it.next();
                if (!(n instanceof WordNode) && (n != this.start) && (n != this.end)) {
                    onlyWords = false;
                }
            }

            w.println("SUBLATTYPE=" + (onlyWords ? "VP_WordClass" : "VP_Concept"));
        } else {
            w.println("SUBLATTYPE=Root");
        }

        w.println("# number of nodes and links");
        w.println("N=" + nodes.size() + " L=" + numEdges);
        w.println("# nodes");
        final Map<Node, Integer> nodeIndex = new HashMap<Node, Integer>();
        int i = 0;
        for (Node n : nodes) {
            w.println("I=" + i + " " + n.toExtLat());
            nodeIndex.put(n, i);
            i++;
        }

        w.println("# links");
        int j = 0;
        for (Node n : nodes) {
            for (Edge e : n.edges()) {
                w.print("J=" + j);
                w.print(" S=" + nodeIndex.get(e.getSource()));
                w.print(" E=" + nodeIndex.get(e.getTarget()));
                w.println(" l=0.00 C=0.00");
            }
            j++;
        }
    }

    static abstract class Node {

        Collection<Edge> edges;

        public Node() {

            this.edges = new ArrayList<Edge>();
        }

        public Edge addEdge(Node target) {

            if (target == this) {
                throw new IllegalArgumentException("Empty loop");
            }
            Edge e = new Edge(this, target);
            this.edges.add(e);
            return e;
        }

        Collection<Edge> edges() {

            return Collections.unmodifiableCollection(this.edges);
        }

        public abstract String toExtLat();
    }

    static class StartNode
            extends Node {

        public StartNode() {

            super();
        }

        @Override
        public String toExtLat() {

            return "W=!NULL";
        }
    }

    static class EndNode
            extends Node {

        public EndNode() {

            super();
        }

        @Override
        public String toExtLat() {

            return "W=!NULL";
        }
    }

    static class WordNode
            extends Node {

        String word;

        public WordNode(String word) {

            super();

            if (word == null) {
                throw new IllegalArgumentException();
            }
            this.word = word;
        }

        public String getWord() {

            return this.word;
        }

        @Override
        public String toExtLat() {

            return "W=" + this.getWord();
        }
    }

    static class RuleNode
            extends Node {

        Rule rule;

        public RuleNode(Rule rule) {

            super();

            if (rule == null) {
                throw new IllegalArgumentException();
            }
            this.rule = rule;
        }

        @Override
        public String toExtLat() {

            return "L=" + this.rule.getName();
        }
    }

    private static class Edge {

        Node source, target;

        public Edge(Node source, Node target) {

            this.source = source;
            this.target = target;
        }

        public Node getSource() {

            return this.source;
        }

        public Node getTarget() {

            return this.target;
        }
    }
}
