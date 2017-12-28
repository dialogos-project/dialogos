package com.clt.diamant.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Group
    implements GroupElement, Iterable<GroupElement> {

  private Set<GroupElement> elements;
  private Group group = null;


  public static Group group(Collection<GroupElement> elements) {

    return new Group(elements);
  }


  public static Set<GroupElement> ungroup(Group g) {

    for (GroupElement e : g) {
      e.setGroup(null);
    }

    return g.elements;
  }


  private Group(Collection<GroupElement> elems) {

    this.elements = new HashSet<GroupElement>();

    if (elems != null) {
      for (GroupElement elem : elems) {
        Group g = Group.getTopGroup(elem);
        if (g == null) {
          elem.setGroup(this);
          this.elements.add(elem);
        }
        else if (g != this) {
          g.setGroup(this);
          this.elements.add(g);
        }
      }
    }
  }


  public Group getGroup() {

    return this.group;
  }


  public void setGroup(Group ng) {

    this.group = ng;
  }


  public void add(GroupElement e) {

    if (e.getGroup() == null) {
      e.setGroup(this);
      this.elements.add(e);
    }
  }


  public void remove(GroupElement e) {

    this.elements.remove(e);
  }


  public Iterator<GroupElement> iterator() {

    return this.elements.iterator();
  }


  public Iterator<GroupElement> leafs() {

    return new LeafIterator(this);
  }

  private static class LeafIterator
        implements Iterator<GroupElement> {

    Iterator<GroupElement> elements = null;
    Iterator<GroupElement> subelements = null;


    public LeafIterator(Group g) {

      this.elements = g.iterator();
    }


    public GroupElement next() {

      if (this.subelements != null) {
        GroupElement o = this.subelements.next();
        if (!this.subelements.hasNext()) {
          this.subelements = null;
        }
        return o;
      }
      else {
        GroupElement o = this.elements.next();
        if (o instanceof Group) {
          this.subelements = ((Group)o).leafs();
          return this.subelements.next();
        }
        else {
          return o;
        }
      }
    }


    public boolean hasNext() {

      return (this.subelements != null) || this.elements.hasNext();
    }


    public void remove() {

      throw new UnsupportedOperationException();
    }
  }


  public static Group getTopGroup(GroupElement e) {

    if (e.getGroup() == null) {
      return null;
    }
    else {
      Group g = e.getGroup();
      while (g.getGroup() != null) {
        g = g.getGroup();
      }
      return g;
    }
  }


  public static boolean isGroup(GroupElement objects[]) {

    if ((objects == null) || (objects.length < 2)) {
      return false;
    }

    Group g = Group.getTopGroup(objects[0]);
    if (g == null) {
      return false;
    }
    for (int i = 1; i < objects.length; i++) {
      if (Group.getTopGroup(objects[i]) != g) {
        return false;
      }
    }
    return true;
  }

}