/*
 * @(#)TrieMap.java
 * Created on 14.02.2006 by dabo
 *
 * Copyright (c) 2006 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A map from strings to a type T that uses a trie to organize the keys. This
 * map does NOT support <code>null</code> keys or values. The keys in this map
 * (and thus the keys returned by keySet() and entrySet() will always be ordered
 * according to POSIX rules (i.e. by comparing the unicode values of individual
 * characters). So 'a' &lt; 'z' &lt; 'A' &lt; 'Z'
 * 
 * @author dabo
 */
public class TrieMap<T>
    implements Map<String, T> {

  /** Which strategy should be used to grow the list of nodes on insert */
  public static enum GrowStrategy {
        GROW_BY_ONE,
        GROW_DOUBLE
  }

  /** Which strategy should be used to shrink the list of nodes on delete */
  public static enum ShrinkStrategy {
        SHRINK_BY_ONE,
        SHRINK_HALF,
        DONT_SHRINK
  }

  private Node<T> root;
  private GrowStrategy growStrategy;
  private ShrinkStrategy shrinkStrategy;


  public TrieMap() {

    this(GrowStrategy.GROW_DOUBLE, ShrinkStrategy.SHRINK_HALF);
  }


  public TrieMap(GrowStrategy growStrategy, ShrinkStrategy shrinkStrategy) {

    this.root = new Node<T>((char)0);
    this.growStrategy = growStrategy;
    this.shrinkStrategy = shrinkStrategy;
  }


  public int size() {

    return this.root.size();
  }


  public boolean isEmpty() {

    return (this.root.value == null) && this.root.isEmpty();
  }


  public boolean containsKey(Object key) {

    return this.root.getNode((String)key, 0, true) != null;
  }


  public boolean containsPrefix(String s) {

    return this.root.getNode(s, 0, false) != null;
  }


  public boolean containsValue(Object value) {

    for (T val : this.values()) {
      if (val == null ? value == null : val.equals(value)) {
        return true;
      }
    }
    return false;
  }


  public T get(Object key) {

    if (key instanceof String) {
      Node<T> node = this.root.getNode((String)key, 0, true);
      if (node == null) {
        return null;
      }
      else {
        return node.value;
      }
    }
    else {
      return null;
    }
  }


  public T put(String key, T value) {

    if (key == null) {
      throw new IllegalArgumentException("<null> key not supported");
    }
    if (value == null) {
      throw new IllegalArgumentException("<null> value not supported");
    }
    return this.root.put(key, 0, value, this.growStrategy);
  }


  public T remove(Object key) {

    return this.root.remove((String)key, 0, this.shrinkStrategy);
  }


  public void putAll(Map<? extends String, ? extends T> t) {

    for (Map.Entry<? extends String, ? extends T> entry : t.entrySet()) {
      this.put(entry.getKey(), entry.getValue());
    }
  }


  public void clear() {

    this.root = new Node<T>((char)0);
  }


  public Set<Map.Entry<String, T>> entrySet() {

    return this.entrySet("");
  }


  public Set<Map.Entry<String, T>> entrySet(final String prefix) {

    final Node<T> prefixRoot = this.root.getNode(prefix, 0, false);
    if (prefixRoot == null) {
      return Collections.emptySet();
    }
    else {
      return new AbstractSet<Map.Entry<String, T>>() {

        @Override
        public Iterator<Map.Entry<String, T>> iterator() {

          return prefixRoot.collect("");
        }


        @Override
        public int size() {

          return prefixRoot.size();
        }


        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {

          if (o instanceof Map.Entry) {
            Map.Entry<String, T> entry = (Map.Entry)o;
            Node<T> node = prefixRoot.getNode(entry.getKey(), 0, true);
            if (node == null) {
              return false;
            }
            else {
              return node.value.equals(entry.getValue());
            }
          }
          else {
            return false;
          }
        }
      };
    }
  }


  public Set<String> keySet() {

    return this.keySet("");
  }


  public Set<String> keySet(String prefix) {

    final Node<T> prefixRoot = this.root.getNode(prefix, 0, false);
    if (prefixRoot == null) {
      return Collections.emptySet();
    }
    else {
      return new AbstractSet<String>() {

        @Override
        public Iterator<String> iterator() {

          final Iterator<Map.Entry<String, T>> iterator =
            prefixRoot.collect("");
          return new Iterator<String>() {

            public boolean hasNext() {

              return iterator.hasNext();
            }


            public String next() {

              return iterator.next().getKey();
            }


            public void remove() {

              iterator.remove();
            }
          };
        }


        @Override
        public int size() {

          return prefixRoot.size();
        }


        // override to provide more efficient search
        @Override
        public boolean contains(Object o) {

          if (o instanceof String) {
            Node<T> node = prefixRoot.getNode((String)o, 0, true);
            return node != null;
          }
          else {
            return false;
          }
        }
      };
    }
  }


  public Collection<T> values() {

    return this.values("");
  }


  public Collection<T> values(String prefix) {

    final Set<Map.Entry<String, T>> entries = this.entrySet(prefix);
    return new AbstractCollection<T>() {

      @Override
      public Iterator<T> iterator() {

        final Iterator<Map.Entry<String, T>> iterator = entries.iterator();
        return new Iterator<T>() {

          public boolean hasNext() {

            return iterator.hasNext();
          }


          public T next() {

            return iterator.next().getValue();
          }


          public void remove() {

            iterator.remove();
          }
        };
      }


      @Override
      public int size() {

        return entries.size();
      }
    };
  }

  private class Node<ValueType> {

    /** signal that this node marks the end of a word */
    private ValueType value;

    /** the name of this node, i.e. the character that this node represents */
    private char key;

    /** the list of children */
    private Node<ValueType>[] children;


    /** Construct a new node with the given name */
    public Node(char key) {

      this.key = key;

      this.value = null;
      this.children = null;
    }


    public boolean isEmpty() {

      if ((this.children == null) || (this.children.length == 0)) {
        return true;
      }
      else if (this.children[0] == null) {
        return true;
      }
      else {
        return false;
      }
    }


    private Node<ValueType> get(char c) {

      int index = this.binarySearch(this.children, c);
      if ((index < 0) || (index >= this.children.length)) {
        return null;
      }
      else if (this.children[index].key == c) {
        return this.children[index];
      }
      else {
        return null;
      }
    }


    public Node<ValueType> getNode(String s, int pos, boolean wordsOnly) {

      if (pos == s.length()) {
        if (!wordsOnly || (this.value != null)) {
          return this;
        }
        else {
          return null;
        }
      }
      else {
        Node<ValueType> next = this.get(s.charAt(pos));
        if (next == null) {
          return null;
        }
        else {
          return next.getNode(s, pos + 1, wordsOnly);
        }
      }
    }


    public ValueType put(String s, int pos, ValueType value,
        GrowStrategy growStrategy) {

      if (pos == s.length()) {
        ValueType oldValue = this.value;
        this.value = value;
        return oldValue;
      }
      else {
        char c = s.charAt(pos);
        int index = this.binarySearch(this.children, c);
        if (index >= 0) {
          return this.children[index].put(s, pos + 1, value, growStrategy);
        }
        else {
          Node<ValueType> child = new Node<ValueType>(s.charAt(pos));
          this.children =
            this.insertChild(this.children, child, -index - 1, growStrategy);
          return child.put(s, pos + 1, value, growStrategy);
        }
      }
    }


    public ValueType remove(String s, int pos, ShrinkStrategy shrinkStrategy) {

      if (pos == s.length()) {
        ValueType oldValue = this.value;
        this.value = null;
        return oldValue;
      }
      else {
        char c = s.charAt(pos);
        int index = this.binarySearch(this.children, c);
        if (index >= 0) {
          Node<ValueType> next = this.children[index];
          ValueType oldValue = next.remove(s, pos + 1, shrinkStrategy);
          if (oldValue != null) {
            if ((next.value == null) && next.isEmpty()) {
              this.removeKey(index, shrinkStrategy);
            }
            return oldValue;
          }
          else {
            return null;
          }
        }
        else {
          return null;
        }
      }
    }


    @SuppressWarnings("unchecked")
    private void removeKey(int index, ShrinkStrategy shrinkStrategy) {

      if (shrinkStrategy == ShrinkStrategy.SHRINK_BY_ONE) {
        Node<ValueType>[] children = new Node[this.children.length - 1];
        if (index > 0) {
          System.arraycopy(this.children, 0, children, 0, index);
        }

        if (index < this.children.length - 1) {
          System.arraycopy(this.children, index + 1, children, index,
            children.length
                          - index);
        }
        this.children = children;
      }
      else if ((shrinkStrategy == ShrinkStrategy.SHRINK_HALF)
                    && (this.children[this.children.length / 2] == null)) {
        Node<ValueType>[] children = new Node[this.children.length / 2];
        if (index > 0) {
          System.arraycopy(this.children, 0, children, 0, index);
        }

        if (index < this.children.length - 1) {
          System.arraycopy(this.children, index + 1, children, index,
            children.length
                          - index);
        }
        this.children = children;
      }
      else {
        // move trailing entries one to the left
        if (index < this.children.length - 1) {
          System.arraycopy(this.children, index + 1, this.children, index,
            this.children.length
                          - (index + 1));
        }
        // clear last entry
        this.children[this.children.length - 1] = null;
      }
    }


    public int size() {

      int size = 0;
      if (this.value != null) {
        size++;
      }
      if (this.children != null) {
        for (int i = 0; (i < this.children.length)
          && (this.children[i] != null); i++) {
          size += this.children[i].size();
        }
      }
      return size;
    }


    @SuppressWarnings("unchecked")
    private Iterator<Map.Entry<String, ValueType>> collect(final String prefix) {

      return new Iterator<Map.Entry<String, ValueType>>() {

        boolean hasValue = Node.this.value != null;
        boolean lastWasValue = false;
        int child = -1;
        Iterator<Map.Entry<String, ValueType>> childIterator = null;


        public boolean hasNext() {

          return this.hasValue
                            || ((this.childIterator != null) && this.childIterator
                              .hasNext())
                            || ((Node.this.children != null)
                              && (this.child + 1 < Node.this.children.length) && (Node.this.children[this.child + 1] != null));
        }


        public Entry<String, ValueType> next() {

          if (this.hasValue) {
            Map.Entry<String, ValueType> entry =
              new Map.Entry<String, ValueType>() {

                public String getKey() {

                  return prefix;
                }


                public ValueType getValue() {

                  return Node.this.value;
                }


                public ValueType setValue(ValueType newValue) {

                  if (newValue == null) {
                    throw new IllegalArgumentException(
                      "<null> value not supported");
                  }
                  ValueType oldValue = Node.this.value;
                  Node.this.value = newValue;
                  return oldValue;
                }
              };
            this.hasValue = false;
            this.lastWasValue = true;
            return entry;
          }
          else {
            this.advanceChild();
            if (this.childIterator != null) {
              Map.Entry<String, ValueType> entry = this.childIterator.next();
              this.lastWasValue = false;
              return entry;
            }
            else {
              throw new NoSuchElementException();
            }
          }
        }


        private void advanceChild() {

          while ((this.child + 1 < Node.this.children.length)
            && (Node.this.children[this.child + 1] != null)
                            && ((this.childIterator == null) || !this.childIterator
                              .hasNext())) {
            this.child++;
            if ((this.child < Node.this.children.length)
              && (Node.this.children[this.child] != null)) {
              this.childIterator =
                Node.this.children[this.child].collect(prefix
                  + Node.this.children[this.child].key);
            }
            else {
              this.childIterator = null;
            }
          }
        }


        public void remove() {

          if (this.lastWasValue) {
            // simply delete by discarding the value
            Node.this.value = null;

            // TODO: we would like to discard the whole node if it has no
            // children
            // but we can't do that because we have no link to the parent
          }
          else if (this.childIterator != null) {
            this.childIterator.remove();
          }
          else {
            throw new IllegalStateException();
          }
        }

      };
    }


    /**
     * Return the index of the node with the given key or a negative number that
     * represents the offset where the key should inserted.
     * 
     * @return index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
     *         <i>insertion point</i> is defined as the point at which the key
     *         would be inserted into the list: the index of the first element
     *         greater than the key, or <tt>list.size()</tt>, if all elements in
     *         the list are less than the specified key. Note that this
     *         guarantees that the return value will be &gt;= 0 if and only if
     *         the key is found.
     */
    private int binarySearch(Node<ValueType>[] nodes, char key) {

      if (nodes == null) {
        return -1;
      }

      int low = 0;
      int high = nodes.length - 1;

      while (low <= high) {
        int mid = (low + high) >> 1;
        Node<ValueType> midVal = nodes[mid];
        if (midVal == null) {
          high = mid - 1;
        }
        else if (midVal.key < key) {
          low = mid + 1;
        }
        else if (midVal.key > key) {
          high = mid - 1;
        }
        else {
          return mid; // key found
        }
      }
      return -(low + 1); // key not found.
    }


    @SuppressWarnings("unchecked")
    private Node<ValueType>[] insertChild(Node<ValueType>[] nodes,
        Node<ValueType> n,
                int index, GrowStrategy growStrategy) {

      if ((nodes == null) || (nodes.length == 0)) { // empty array
        return new Node[] { n };
      }
      else {
        // resize
        Node<ValueType>[] newNodes;
        if (nodes[nodes.length - 1] != null) { // array is full.
          // resize the array
          switch (growStrategy) {
            case GROW_BY_ONE:
              newNodes = new Node[nodes.length + 1];
              break;
            default:
              newNodes = new Node[nodes.length * 2];
              break;
          }

          // copy all entries before index
          if (index > 0) {
            System.arraycopy(nodes, 0, newNodes, 0, index);
          }

          // copy all entries after the index
          if (index < nodes.length) {
            System.arraycopy(nodes, index, newNodes, index + 1, nodes.length
              - index);
          }
        }
        else {
          newNodes = nodes;

          // move all entries after the index
          // The last element is null and will be overwritten
          if (index < nodes.length) {
            System.arraycopy(nodes, index, newNodes, index + 1, nodes.length
              - index
                              - 1);
          }
        }

        // insert at index
        newNodes[index] = n;
        return newNodes;
      }
    }


    void write(DataOutputStream out, Map<T, Integer> valuePointers)
        throws IOException {

      int size = 0;
      if (this.children != null) {
        size = this.children.length;
        while ((size > 0) && (this.children[size - 1] == null)) {
          size--;
        }
      }
      out.writeInt(size);
      if (this.value == null) {
        out.writeInt(-1);
      }
      else {
        out.writeInt(valuePointers.get(this.value).intValue());
      }
      for (int i = 0; i < size; i++) {
        out.writeChar(this.children[i].key);
      }
      for (int i = 0; i < size; i++) {
        this.children[i].write(out, valuePointers);
      }
    }

  }


  public void write(DataOutputStream out, Map<T, Integer> valuePointers)
      throws IOException {

    this.root.write(out, valuePointers);
  }

}
