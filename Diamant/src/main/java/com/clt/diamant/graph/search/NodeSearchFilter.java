package com.clt.diamant.graph.search;

import java.util.Collection;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class NodeSearchFilter {

    /**
     * Return a collection of SearchResults
     */
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
