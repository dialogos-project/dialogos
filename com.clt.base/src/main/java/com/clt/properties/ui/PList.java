package com.clt.properties.ui;

import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.clt.properties.Property;

/**
 * @author dabo
 *
 */
public class PList<T> extends JList {

    private Property<T> property;
    private T[] values;

    public PList(Property<T> property) {
        this.property = property;
        this.values = property.getPossibleValues();
    }

    boolean updating = false;

    boolean latentDisabled = false;

    ChangeListener l = new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
            if (!PList.this.updating) {
                PList.this.updating = true;
                if (PList.this.values != PList.this.property.getPossibleValues()) {
                    PList.this.initOptions();
                } else {
                    PList.this.setSelectedValue(PList.this.property.getValueAsObject(),
                            true);
                }
                PList.this.setEnabled(PList.this.property.isEditable());
                PList.this.updating = false;
            }
        }
    };

    ListSelectionListener lsl = new ListSelectionListener() {

        @SuppressWarnings("unchecked")
        public void valueChanged(ListSelectionEvent e) {

            if (!PList.this.updating) {
                PList.this.updating = true;
                PList.this.property.setValue((T) PList.this.getSelectedValue());
                PList.this.updating = false;
            }
        }
    };

    @Override
    public void addNotify() {

        super.addNotify();

        this.initOptions();

        this.addListSelectionListener(this.lsl);
        this.property.addChangeListener(this.l);
        this.l.stateChanged(new ChangeEvent(this.property));
    }

    @Override
    public void removeNotify() {

        this.property.removeChangeListener(this.l);
        this.removeListSelectionListener(this.lsl);
        super.removeNotify();
    }

    @Override
    public boolean isEnabled() {

        return super.isEnabled() && !this.latentDisabled;
    }

    private void initOptions() {

        this.values = this.property.getPossibleValues();
        if (this.values == null) {
            this.latentDisabled = true;
            this.setListData(new Object[0]);
        } else {
            this.latentDisabled = false;
            this.setListData(this.values);
            this.setVisibleRowCount(Math.max(4, Math.min(this.values.length, 8)));

            this.setSelectedValue(this.property.getValueAsObject(), true);
        }
        this.setEnabled(this.property.isEditable());
    }
}
