package com.clt.diamant.undo;

import java.util.Collection;

import com.clt.diamant.Resources;
import com.clt.undo.AbstractEdit;
import com.clt.util.PropertyContainer;

public class PropertyEdit<T>
    extends AbstractEdit {

  private Object target;
  private String key;
  private T old_value, new_value;


  /**
   * Setup an edit for a single property container
   */
  public PropertyEdit(String name, PropertyContainer<? super T> target,
      String key, T oldValue,
                        T newValue) {

    this(name, key, target, oldValue, newValue);
  }


  /**
   * Setup an edit for a collection of property containers
   */
  public PropertyEdit(String name,
      Collection<PropertyContainer<? super T>> target, String key,
                        T oldValue, T newValue) {

    this(name, key, target, oldValue, newValue);
  }


  private PropertyEdit(String name, String key, Object target, T old_value,
      T new_value) {

    super(Resources.format("Set", Resources.getString(name)));

    this.target = target;
    this.key = key;
    this.old_value = old_value;
    this.new_value = new_value;
  }


  @Override
  public void unrun() {

    this.putProperty(this.target, this.key, this.old_value);
  }


  @Override
  public void run() {

    this.putProperty(this.target, this.key, this.new_value);
  }


  @SuppressWarnings("unchecked")
  private void putProperty(Object o, String key, Object value) {

    // The constructor makes sure, that we really only get here
    // for the right type of property containers
    if (o instanceof PropertyContainer) {
      ((PropertyContainer)o).setProperty(key, value);
    }
    else if (o instanceof Collection) {
      for (Object element : ((Collection)o)) {
        this.putProperty(element, key, value);
      }
    }
  }
}