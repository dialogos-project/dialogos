package com.clt.util;

import java.beans.PropertyChangeListener;
import java.util.Set;

public interface PropertyContainer<ValueType> {

  public Set<String> propertyNames();


  public void setProperty(String key, ValueType value);


  public ValueType getProperty(String key);


  public void addPropertyChangeListener(PropertyChangeListener listener);


  public void removePropertyChangeListener(PropertyChangeListener listener);
}