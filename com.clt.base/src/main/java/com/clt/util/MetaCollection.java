package com.clt.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * A "meta" collection that is backed by several sub-collections.
 *
 * @author dabo
 *
 * @param <T>
 */
public class MetaCollection<T> extends AbstractCollection<T> {

    private Collection<? extends Collection<? extends T>> subCollections;

    public MetaCollection(Collection<? extends T>... subCollections) {
        this.subCollections = Arrays.asList(subCollections);
    }

    public MetaCollection(Collection<? extends Collection<? extends T>> subCollections) {
        this.subCollections = subCollections;
    }

    @Override
    public int size() {
        int size = 0;
        for (Collection<? extends T> c : this.subCollections) {
            size += c.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {

        for (Collection<? extends T> c : this.subCollections) {
            if (!c.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {

        for (Collection<? extends T> c : this.subCollections) {
            if (c.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {

        Collection<Iterator<? extends T>> its
                = new ArrayList<Iterator<? extends T>>(
                        this.subCollections.size());
        for (Collection<? extends T> c : this.subCollections) {
            its.add(c.iterator());
        }
        return new ConcatIterator<T>(its);
    }

    @Override
    public boolean add(T o) {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {

        for (Collection<? extends T> c : this.subCollections) {
            if (c.remove(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> remove) {

        boolean changed = false;
        for (Collection<? extends T> c : this.subCollections) {
            if (c.removeAll(remove)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> retain) {

        boolean changed = false;
        for (Collection<? extends T> c : this.subCollections) {
            if (c.retainAll(retain)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {

        for (Collection<? extends T> c : this.subCollections) {
            c.clear();
        }
    }
}
