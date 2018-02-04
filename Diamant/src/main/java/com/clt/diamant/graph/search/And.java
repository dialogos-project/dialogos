package com.clt.diamant.graph.search;

import java.util.Collection;
import java.util.Collections;

import com.clt.diamant.graph.Node;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class And extends NodeSearchFilter {

    private NodeSearchFilter filter[];

    public And(NodeSearchFilter[] filter) {

        this.filter = filter;
    }

    @Override
    public Collection<? extends SearchResult> match(Node n) {

        StringBuffer description = new StringBuffer();
        for (int i = 0; i < this.filter.length; i++) {
            if (this.filter[i].match(n).size() == 0) {
                return Collections.emptyList();
            } else {
                if (i > 0) {
                    description.append(" && ");
                }
                description.append(this.filter[i].toString());
            }
        }
        return Collections
                .singleton(new NodeSearchResult(n, description.toString()));
    }
}
