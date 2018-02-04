package com.clt.diamant.graph.search;

import java.util.Collection;
import java.util.LinkedList;

import com.clt.diamant.graph.Node;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Or extends NodeSearchFilter {

    private NodeSearchFilter filter[];

    public Or(NodeSearchFilter[] filter) {

        this.filter = filter;
    }

    @Override
    public Collection<? extends SearchResult> match(Node n) {

        Collection<SearchResult> matches = new LinkedList<SearchResult>();
        for (int i = 0; i < this.filter.length; i++) {
            matches.addAll(this.filter[i].match(n));
        }
        return matches;
    }
}
