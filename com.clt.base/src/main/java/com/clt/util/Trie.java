package com.clt.util;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * A set of strings that uses a trie to organize the elements. This set does NOT
 * support <code>null</code> elements. The elements will always be ordered
 * according to POSIX rules (i.e. by comparing the unicode values of individual
 * characters). So 'a' &lt; 'z' &lt; 'A' &lt; 'Z'
 *
 * @author dabo
 */
public class Trie extends AbstractCollection<String> {

    private static final Object VALUE = new Object();

    private TrieMap<Object> map;

    public Trie() {

        this.map = new TrieMap<Object>();
    }

    @Override
    public Iterator<String> iterator() {

        return this.map.keySet().iterator();
    }

    public Iterator<String> iterator(String prefix) {

        return this.map.keySet(prefix).iterator();
    }

    @Override
    public int size() {

        return this.map.size();
    }

    @Override
    public boolean isEmpty() {

        return this.map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {

        return this.map.containsKey(o);
    }

    public boolean containsPrefix(String prefix) {

        return this.map.containsPrefix(prefix);
    }

    @Override
    public Object[] toArray() {

        return this.map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {

        return this.map.keySet().toArray(a);
    }

    @Override
    public boolean add(String o) {

        // return true if there was no old entry
        return this.map.put(o, Trie.VALUE) == null;
    }

    @Override
    public boolean remove(Object o) {

        return this.map.remove(o) != null;
    }

    @Override
    public void clear() {

        this.map.clear();
    }

}
