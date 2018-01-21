package com.clt.script.exp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Match {

    private Map<String, Value> match;

    public Match() {

        this.match = new HashMap<String, Value>();
    }

    public void put(String variable, Value value) {

        this.match.put(variable, value);
    }

    public Value get(String variable) {

        return this.match.get(variable);
    }

    public void merge(Match m) {

        this.match.putAll(m.match);
    }

    public Iterator<String> variables() {

        return this.match.keySet().iterator();
    }
}
