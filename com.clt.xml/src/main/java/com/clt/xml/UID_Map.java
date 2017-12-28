package com.clt.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class UID_Map<VALUE> {

  private Map<String, VALUE> map;
  private Map<VALUE, String> reverse_map;


  public UID_Map() {

    this.map = new HashMap<String, VALUE>();
    this.reverse_map = new HashMap<VALUE, String>();
  }


  private void put(String key, VALUE value) {

    this.map.put(key, value);
    this.reverse_map.put(value, key);
  }


  /**
   * Put an object into the map using a given uid.
   */
  public synchronized String add(VALUE o, String uid) {

    if (uid == null) {
      return this.add(o);
    }
    else if (this.reverse_map.containsKey(o)) {
      throw new IllegalArgumentException("Attempt to add object twice");
    }
    else if (this.map.containsKey(uid)) {
      throw new IllegalArgumentException("Attempt to overwrite existing key");
    }
    else {
      this.put(uid, o);
      return uid;
    }
  }


  /**
   * Put an object into the map creating a new uid.
   * 
   * @return The uid created for the object
   */
  public synchronized String add(VALUE o) {

    if (this.reverse_map.containsKey(o)) {
      throw new IllegalArgumentException("Attempt to add object twice");
    }
    else {
      // no id yet, so generate one
      String uid = this.createUID();
      this.put(uid, o);
      return uid;
    }
  }


  /**
   * Return the uid assigned to an object.
   */
  public synchronized String getKey(VALUE o, boolean add) {

    String key = this.reverse_map.get(o);
    if (key != null) {
      return key;
    }
    else if (add) {
      return this.add(o);
    }
    else {
      throw new NoSuchElementException("Object not found");
    }
  }


  /**
   * Return the object for a given uid.
   */
  public synchronized VALUE getValue(String uid) {

    VALUE o = this.map.get(uid);
    if (o == null) {
      throw new NoSuchElementException("Unknown uid: " + uid);
    }
    else {
      return o;
    }
  }


  public synchronized boolean containsKey(String uid) {

    return this.map.containsKey(uid);
  }


  public synchronized boolean containsValue(VALUE value) {

    return this.map.containsValue(value);
  }


  protected abstract String createUID();
}