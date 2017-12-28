/**
 * 
 */

package com.clt.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * @author dabo
 * 
 */
public abstract class AbstractAction
    implements Action, Runnable {

  private Map<String, Object> properties = new HashMap<String, Object>();
  private Collection<PropertyChangeListener> listeners =
    new LinkedList<PropertyChangeListener>();


  public AbstractAction() {

    this(null);
  }


  public AbstractAction(String name) {

    this(name, (Icon)null);
  }


  public AbstractAction(String name, Image image) {

    this(name, new ImageIcon(image));
  }


  public AbstractAction(String name, Icon icon) {

    this.setName(name);
    this.setIcon(icon);
    this.setEnabled(true);
  }


  public Object getValue(String key) {

    return this.properties.get(key);
  }


  public void putValue(String key, Object value) {

    Object oldValue = this.properties.put(key, value);
    if (oldValue != value) {
      this.firePropertyChange(key, oldValue, value);
    }
  }


  public boolean isEnabled() {

    return ((Boolean)this.getValue("enabled")).booleanValue();
  }


  public void setEnabled(boolean enabled) {

    this.putValue("enabled", new Boolean(enabled));
  }


  public String getName() {

    return String.valueOf(this.getValue(Action.NAME));
  }


  public void setName(String name) {

    this.putValue(Action.NAME, name);
  }


  public Icon getIcon() {

    return (Icon)this.getValue(Action.SMALL_ICON);
  }


  public void setIcon(Icon icon) {

    this.putValue(Action.SMALL_ICON, icon);
  }


  public void setIcon(Image image) {

    this.putValue(Action.SMALL_ICON, new ImageIcon(image));
  }


  public KeyStroke getAccelerator() {

    return (KeyStroke)this.getValue(Action.ACCELERATOR_KEY);
  }


  public void setAccelerator(int keyCode) {

    this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode,
      Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  }


  public void setAccelerator(KeyStroke keystroke) {

    this.putValue(Action.ACCELERATOR_KEY, keystroke);
  }


  public void addPropertyChangeListener(PropertyChangeListener listener) {

    synchronized (this.listeners) {
      this.listeners.add(listener);
    }
  }


  public void removePropertyChangeListener(PropertyChangeListener listener) {

    synchronized (this.listeners) {
      this.listeners.remove(listener);
    }
  }


  protected void firePropertyChange(String property, Object oldValue,
      Object newValue) {

    synchronized (this.listeners) {
      PropertyChangeEvent evt =
        new PropertyChangeEvent(this, property, oldValue, newValue);
      for (PropertyChangeListener l : this.listeners) {
        l.propertyChange(evt);
      }
    }
  }


  public void actionPerformed(ActionEvent e) {

    this.run();
  }


  public abstract void run();
}
