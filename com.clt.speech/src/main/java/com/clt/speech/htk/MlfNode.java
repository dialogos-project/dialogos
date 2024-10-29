package com.clt.speech.htk;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.clt.util.PropertyContainer;

public abstract class MlfNode implements PropertyContainer<Object> {

    public static final String CONFIDENCE = "Confidence";

    private List<MlfNode> children = new ArrayList<MlfNode>();

    private String label;
    private float confidence;
    private MlfNode parent;

    protected MlfNode(MlfNode parent, String label) {

        this(parent, label, 0.0);
    }

    protected MlfNode(MlfNode parent, String label, double confidence) {

        this.parent = parent;
        this.label = label;
        this.confidence = (float) Math.min(Math.exp(confidence), 1.0);
    }

    abstract public boolean getAllowsChildren();

    abstract public long getStart();

    abstract public long getEnd();

    public String getLabel() {

        return this.label;
    }

    public float getConfidence() {

        return this.confidence;
    }

    @Override
    public String toString() {

        return this.label;
    }

    public int numChildren() {

        return this.children.size();
    }

    public void addChild(MlfNode node) {

        if (this.getAllowsChildren()) {
            this.children.add(node);
            node.setParent(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    void setParent(MlfNode parent) {

        this.parent = parent;
    }

    public void removeChild(MlfNode node) {

        this.children.remove(node);
    }

    public void removeAllChildren() {

        this.children.clear();
    }

    public int getDepth() {

        int depth = 0;
        for (int i = this.numChildren() - 1; i >= 0; i--) {
            depth = Math.max(depth, this.getChild(i).getDepth());
        }
        return depth + 1;
    }

    public MlfNode getChild(int index) {

        return this.children.get(index);
    }

    public int getIndex(MlfNode child) {

        return this.children.indexOf(child);
    }

    public MlfNode getParent() {

        return this.parent;
    }

    public Iterator<MlfNode> children() {

        return this.children.iterator();
    }

    public Set<String> propertyNames() {

        return Collections.singleton(MlfNode.CONFIDENCE);
    }

    public Object getProperty(String key) {

        if (key.equals(MlfNode.CONFIDENCE)) {
            return Double.valueOf(this.getConfidence());
        } else {
            return null;
        }
    }

    public void setProperty(String key, Object value) {

    }

    // properties never change, so we don't need to track listeners
    public void addPropertyChangeListener(PropertyChangeListener l) {

    }

    public void removePropertyChangeListener(PropertyChangeListener l) {

    }
}
