/*
 * @(#)UniqueList.java
 * Created on 04.04.2006 by dabo
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * This is the implementation of a list with unique elements, or in other words
 * a set with a specified order of elements.
 * <p>
 * It supports all optional operations on sets and lists with two exceptions:
 * <ol>
 * <li> {@link #subList} is not supported because the uniqueness of elements with
 * respect to the complete list cannot be guaranteed.
 * <li> {@link #set} is supported but may throw an IllegalArgumentException if
 * you try to set an element at position <code>n</code> and the element is
 * already contained in the list at another position. The same restriction is
 * valid if you try to set an element through the list iterator returned by
 * {@link #listIterator()}.
 * </ol>
 * Data is backed by an ArrayList and a HashSet such that {@link #get}
 * <b>and</b> {@link #contains} both operate in constant time. The downside of
 * this is, that twice the space is needed, so use this class with caution if
 * you need to handle a very large number of elements.
 * 
 * @author Daniel Bobbert
 */
public class UniqueList<T> implements List<T> {  // AK: used to implement List<T> and Set<T> -- no longer allowed in Java 8

  private List<T> list;
  private Set<T> set;

  private Collection<ListDataListener> listeners =
    new ArrayList<ListDataListener>();


  public UniqueList() {

    this(10);
  }


  public UniqueList(int size) {

    this.list = new ArrayList<T>(size);
    this.set = new HashSet<T>(size * 4 / 3 + 1, 0.75f);
  }


  public UniqueList(Collection<? extends T> c) {

    this(c.size());

    this.addAll(c);
  }


  public int size() {

    return this.list.size();
  }


  public boolean isEmpty() {

    return this.list.isEmpty();
  }


  public boolean contains(Object o) {

    return this.set.contains(o);
  }


  public Iterator<T> iterator() {

    return this.listIterator();
  }


  public Object[] toArray() {

    return this.list.toArray();
  }


  public <ArrayT> ArrayT[] toArray(ArrayT[] a) {

    return this.list.toArray(a);
  }


  public boolean add(T o) {

    return this.insert(this.size(), o);
  }


  public boolean remove(Object o) {

    if (this.set.remove(o)) {
      this.removeFromList(o);
      return true;
    }
    else {
      return false;
    }
  }


  public boolean containsAll(Collection<?> c) {

    return this.set.containsAll(c);
  }


  public boolean addAll(Collection<? extends T> c) {

    return this.addAll(this.size(), c);
  }


  public boolean addAll(int index, Collection<? extends T> c) {

    boolean changed = false;
    for (T e : c) {
      if (this.insert(index, e)) {
        changed = true;
        index++;
      }
    }
    return changed;
  }


  public boolean removeAll(Collection<?> c) {

    if (this.set.removeAll(c)) {
      for (Object o : c) {
        this.removeFromList(o);
      }
      return true;
    }
    else {
      return false;
    }
  }


  public boolean retainAll(Collection<?> c) {

    if (this.set.retainAll(c)) {
      for (Iterator<T> it = this.iterator(); it.hasNext();) {
        if (!c.contains(it.next())) {
          it.remove();
        }
      }
      return true;
    }
    else {
      return false;
    }
  }


  public void clear() {

    int size = this.size();
    if (size > 0) {
      this.set.clear();
      this.list.clear();

      for (ListDataListener l : this.listeners) {
        l.intervalRemoved(new ListDataEvent(this,
          ListDataEvent.INTERVAL_REMOVED, 0,
                  size - 1));
      }
    }
  }


  public T get(int index) {

    return this.list.get(index);
  }


  /**
   * Set the element at the given position. This method will throw an
   * IllegalArgumentException if you try to set an element at position
   * <code>n</code> and the element is already contained in the list at another
   * position.
   */
  public T set(int index, T element) {

    if (this.set.contains(element) && !(this.list.get(index) != element)) {
      throw new IllegalArgumentException(
        "List already contains object at another position");
    }

    T oldElement = this.get(index);
    if (oldElement == element) {
      return element;
    }
    else {
      this.set.remove(oldElement);
      this.set.add(element);
      this.list.set(index, element);

      for (ListDataListener l : this.listeners) {
        l.contentsChanged(new ListDataEvent(this,
          ListDataEvent.CONTENTS_CHANGED, index,
                  index));
      }

      return oldElement;
    }
  }


  private boolean insert(int index, T element) {

    if (this.set.add(element)) {
      this.list.add(index, element);
      for (ListDataListener l : this.listeners) {
        l.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
          index, index));
      }
      return true;
    }
    else {
      return false;
    }
  }


  public void add(int index, T element) {

    this.insert(index, element);
  }


  public T remove(int index) {

    T element = this.list.remove(index);
    this.set.remove(element);
    for (ListDataListener l : this.listeners) {
      l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
        index, index));
    }
    return element;
  }


  private void removeFromList(Object o) {

    int index = this.indexOf(o);
    this.list.remove(index);
    for (ListDataListener l : this.listeners) {
      l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
        index, index));
    }
  }


  public int indexOf(Object o) {

    return this.list.indexOf(o);
  }


  public int lastIndexOf(Object o) {

    return this.list.lastIndexOf(o);
  }


  public ListIterator<T> listIterator() {

    return this.listIterator(0);
  }


  public ListIterator<T> listIterator(int index) {

    // keep set and list consistent, even if the list is modified
    // via the iterator!
    final ListIterator<T> iterator = this.list.listIterator(index);
    return new ListIterator<T>() {

      private T lastObject = null;
      private int pos = -1;


      public boolean hasNext() {

        return iterator.hasNext();
      }


      public T next() {

        this.lastObject = iterator.next();
        this.pos++;
        return this.lastObject;
      }


      public void remove() {

        iterator.remove();
        for (ListDataListener l : UniqueList.this.listeners) {
          l.intervalRemoved(new ListDataEvent(this,
            ListDataEvent.INTERVAL_REMOVED, this.pos,
                      this.pos));
        }
        UniqueList.this.set.remove(this.lastObject);
      }


      public boolean hasPrevious() {

        return iterator.hasPrevious();
      }


      public T previous() {

        this.lastObject = iterator.previous();
        this.pos--;
        return this.lastObject;
      }


      public int nextIndex() {

        return iterator.nextIndex();
      }


      public int previousIndex() {

        return iterator.previousIndex();
      }


      public void set(T o) {

        if (UniqueList.this.set.contains(o)
          && !(UniqueList.this.list.get(this.pos) == o)) {
          throw new IllegalArgumentException(
                        "List already contains object at another position");
        }

        T oldElement = UniqueList.this.list.get(this.pos);
        // ListIterator.set() may throw an exception, so modify
        // the set afterwards
        iterator.set(o);

        UniqueList.this.set.remove(oldElement);
        UniqueList.this.set.add(o);

        for (ListDataListener l : UniqueList.this.listeners) {
          l.contentsChanged(new ListDataEvent(this,
            ListDataEvent.CONTENTS_CHANGED, this.pos,
                      this.pos));
        }
      }


      public void add(T o) {

        if (!UniqueList.this.set.contains(o)) {
          iterator.add(o);
          UniqueList.this.set.add(o);

          for (ListDataListener l : UniqueList.this.listeners) {
            l.intervalAdded(new ListDataEvent(this,
              ListDataEvent.INTERVAL_ADDED, this.pos,
                          this.pos));
          }

          this.pos++;
        }
      }
    };
  }


  /*
   * Not supported. Will throw an UnsupportedOperationException.
   */
  public List<T> subList(int fromIndex, int toIndex) {

    throw new UnsupportedOperationException();
  }


  public void addListDataListener(ListDataListener l) {

    this.listeners.add(l);
  }


  public void removeListDataListener(ListDataListener l) {

    this.listeners.remove(l);
  }

}
