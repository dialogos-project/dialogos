package com.clt.diamant.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Selection model (e.g. for GraphUI). It contains the selected GraphElements.
 * These GraphElements can be retrieved with getGraphElements.
 *
 * @author dabo
 */
public class GraphSelectionModel {

    private Collection<GraphElement> selection;

    /**
     * Creates a new GraphSelection Model, with an empty set of selected
     * Elements.
     */
    public GraphSelectionModel() {

        this.selection = new HashSet<GraphElement>();
    }

    /**
     * Adds a new graph element to the set of selected elements.
     *
     * @param e Adds the element e to the selection model.
     * @throws IllegalArgumentException if null is added
     */
    public void add(GraphElement e) {

        if (e == null) {
            throw new IllegalArgumentException(
                    "Attempt to add <null> element to selection");
        }
        this.selection.add(e);
    }

    /**
     * Removes graph element e from the selection model.
     *
     * @param e Graph Element to be removed from the selection model.
     */
    public void remove(GraphElement e) {

        this.selection.remove(e);
    }

    /**
     * Removes all the graph elements from the selection model.
     */
    public void clear() {

        this.selection.clear();
    }

    public boolean contains(Object o) {

        return this.selection.contains(o);
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> getSelectedObjects(Class<T> cls) {

        Set<T> c = new HashSet<T>();
        for (Object o : this.selection) {
            if ((cls == null) || cls.isAssignableFrom(o.getClass())) {
                c.add((T) o);
            }
        }
        return c;
    }

    public int size(Class<?> cls) {

        int count = 0;
        for (Object o : this.selection) {
            if ((cls == null) || cls.isAssignableFrom(o.getClass())) {
                count++;
            }
        }
        return count;
    }

}
