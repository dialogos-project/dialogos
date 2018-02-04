package com.clt.diamant;

import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.InputHandler;
import com.clt.diamant.graph.Node;
import com.clt.xml.UID_Map;
import com.clt.xml.UUID_Map;

/**
 * @author dabo
 *
 */
public class IdMap {

    public IdentityMap<Device> devices;
    public IdentityMap<Slot> variables;
    public IdentityMap<Grammar> grammars;
    public IdentityMap<GroovyVariable> groovyVariables;
    public IdentityMap<Node> nodes;
    public IdentityMap<Graph> graphs;
    public IdentityMap<InputHandler> inputHandlers;

    public IdMap() {

        this(false);
    }

    public IdMap(boolean rewriteIDs) {

        this.devices = IdMap.createMap(rewriteIDs);
        this.variables = IdMap.createMap(rewriteIDs);
        this.grammars = IdMap.createMap(rewriteIDs);
        this.groovyVariables = IdMap.createMap(rewriteIDs);
        this.nodes = IdMap.createMap(rewriteIDs);
        this.graphs = IdMap.createMap(rewriteIDs);
        this.inputHandlers = IdMap.createMap(rewriteIDs);
    }

    private static <T extends IdentityObject> IdentityMap<T> createMap(
            boolean rewriteIDs) {

        return new IdentityMap<T>(rewriteIDs);
    }

    public static class IdentityMap<T extends IdentityObject> {

        private UID_Map<T> map;
        private boolean rewriteIDs;

        public IdentityMap(boolean rewriteIDs) {

            this.map = new UUID_Map<T>();
            this.rewriteIDs = rewriteIDs;
        }

        /**
         * Add an object to the map and return its ID. If the object already has
         * an ID, use it. Otherwise let the map create a new ID and assign it to
         * the object.
         *
         * @param value The object to add to the map
         * @return The ID of the object
         */
        public String put(T value) {

            try {
                String id = this.map.getKey(value, false);
                if (!id.equals(value.getId())) {
                    throw new java.util.ConcurrentModificationException(
                            "Object id changed");
                }
                return id;
            } catch (java.util.NoSuchElementException exn) {
                String id = value.getId();
                if ((id != null) && this.rewriteIDs) {
                    try {
                        // if the id is a simple integer, erase it
                        Integer.parseInt(id);
                        id = null;
                    } catch (Exception ignore) {
                    }
                }
                if (id != null) {
                    this.map.add(value, id);
                } else {
                    id = this.map.add(value);
                    value.setId(id);
                }
                return id;
            }
        }

        /**
         * Return the object with the given key.
         *
         * @throws java.util.NoSuchElementException if the key doesn't exist in
         * the map
     *
         */
        public T get(String key) {

            return this.map.getValue(key);
        }

        /**
         * Return the key for the given object.
         *
         * @throws java.util.NoSuchElementException if the object doesn't exist
         * in the map
     *
         */
        public String getKey(T value) {

            return this.map.getKey(value, false);
        }
    }
}
