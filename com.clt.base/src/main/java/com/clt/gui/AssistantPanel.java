package com.clt.gui;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.properties.Property;

public abstract class AssistantPanel extends JPanel {

    private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private List<Property<?>> properties = new ArrayList<Property<?>>();
    private String id;

    public AssistantPanel(String id) {
        super();
        this.id = id;
    }

    public AssistantPanel(String id, LayoutManager layout) {
        super(layout);
        this.id = id;
    }

    private final ChangeListener propertyListener = new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
            AssistantPanel.this.fireEditStateChange();
        }
    };

    final void addEditStateListener(ChangeListener l) {
        synchronized (this.changeListeners) {
            this.changeListeners.add(l);
        }
    }

    final void removeEditStateListener(ChangeListener l) {
        synchronized (this.changeListeners) {
            this.changeListeners.remove(l);
        }
    }

    final protected void fireEditStateChange() {

        synchronized (this.changeListeners) {
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener l : this.changeListeners) {
                l.stateChanged(evt);
            }
        }
    }

    @Override
    public synchronized void removeNotify() {

        super.removeNotify();

        for (Property<?> p : this.properties) {
            p.removeChangeListener(this.propertyListener);
        }

        this.properties.clear();
    }

    public synchronized void add(Property<?> property, Object constraints) {

        this.add(property.createEditor(), constraints);
        property.addChangeListener(this.propertyListener);
        this.properties.add(property);
    }

    public String getId() {

        return this.id;
    }

    public abstract String getNextPanel();

    public abstract boolean isComplete();
}
