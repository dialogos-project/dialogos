package com.clt.speech.recognition;

import java.beans.PropertyChangeListener;
import java.util.LinkedHashSet;
import java.util.Set;

import com.clt.util.PropertyContainer;

/**
 * @author Daniel Bobbert
 *
 */
public abstract class Word implements com.clt.srgf.Word, PropertyContainer<Object> {

    // return the start offset in the audio stream, as returned by the recognizer
    public abstract long getStart();

    // return the end offset in the audio stream, as returned by the recognizer
    public abstract long getEnd();

    @Override
    public String toString() {

        return this.getWord();
    }

    public Object getProperty(String key) {

        if (key.equals("word")) {
            return this.getWord();
        } else if (key.equals("confidence")) {
            return Float.valueOf(this.getConfidence());
        } else if (key.equals("start")) {
            return Long.valueOf(this.getStart());
        } else if (key.equals("end")) {
            return Long.valueOf(this.getEnd());
        } else {
            return null;
        }
    }

    public Set<String> propertyNames() {

        Set<String> names = new LinkedHashSet<String>();
        names.add("word");
        names.add("confidence");
        names.add("start");
        names.add("end");
        return names;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {

        // no-op, because properties are read only
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {

        // no-op, because properties are read only
    }

    public void setProperty(String key, Object value) {

        throw new UnsupportedOperationException();
    }

}
