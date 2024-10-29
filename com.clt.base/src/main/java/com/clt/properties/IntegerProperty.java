package com.clt.properties;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class IntegerProperty extends Property<Integer> {

    private Integer[] values = null;
    private int minimum;
    private int maximum;

    public IntegerProperty(String id) {
        this(id, 0, 0);
    }

    public IntegerProperty(String id, int minimum, int maximum) {

        super(id, minimum == maximum ? Property.EDIT_TYPE_NUMBERFIELD
                : Property.EDIT_TYPE_SLIDER);

        this.setRangeImpl(minimum, maximum);
    }

    public abstract int getValue();

    protected void setValueImpl(int value) {

        // the default implementation does nothing
    }

    public final void setValue(int value) {

        if (value != this.getValue()) {
            if ((this.getMinimum() != this.getMaximum())
                    && ((value < this.getMinimum()) || (value > this.getMaximum()))) {
                throw new IllegalArgumentException("value out of range");
            }
            this.setValueImpl(value);
            this.fireChange();
        }
    }

    @Override
    public void setValueFromString(String value) {

        if (value == null) {
            throw new IllegalArgumentException();
        }
        int val;
        if (value.startsWith("0x")) {
            val = Integer.parseInt(value.substring(2), 16);
        } else {
            val = Integer.parseInt(value, 10);
        }
        this.setValue(val);
    }

    @Override
    public String getValueAsString() {

        return String.valueOf(this.getValue());
    }

    public int getMinimum() {

        return this.minimum;
    }

    public int getMaximum() {

        return this.maximum;
    }

    private void setRangeImpl(int minimum, int maximum) {

        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum must be <= maximum");
        }

        this.minimum = minimum;
        this.maximum = maximum;
        if ((minimum == 0) && (maximum == 1)) {
            this.setEditType(Property.EDIT_TYPE_CHECKBOX);
        } else if ((minimum != maximum) && (maximum - minimum < 10)) {
            this.values = new Integer[maximum - minimum + 1];
            for (int i = minimum; i <= maximum; i++) {
                this.values[i - minimum] = Integer.valueOf(i);
            }
            this.setEditType(Property.EDIT_TYPE_COMBOBOX);
        } else {
            this.values = null;
            this.setEditType(minimum == maximum ? Property.EDIT_TYPE_NUMBERFIELD
                    : Property.EDIT_TYPE_SLIDER);
        }
    }

    public void setRange(int minimum, int maximum) {

        this.setRangeImpl(minimum, maximum);

        if (minimum != maximum) {
            int value = this.getValue();
            if (value < minimum) {
                this.setValueImpl(minimum);
            } else if (value > maximum) {
                this.setValueImpl(maximum);
            }
        }
        this.fireChange();
    }

    @Override
    public Integer[] getPossibleValues() {

        return this.values;
    }

    @Override
    public void setValue(Integer o) {

        this.setValue(o.intValue());
    }

    @Override
    public Integer getValueAsObject() {

        return Integer.valueOf(this.getValue());
    }

    @Override
    protected int getSupportedEditTypesImpl() {

        if (this.minimum == this.maximum) {
            return Property.EDIT_TYPE_NUMBERFIELD | Property.EDIT_TYPE_TEXTFIELD;
        } else {
            int editType
                    = Property.EDIT_TYPE_SLIDER | Property.EDIT_TYPE_NUMBERFIELD
                    | Property.EDIT_TYPE_TEXTFIELD;
            if ((this.getMinimum() == 0) && (this.getMaximum() == 1)) {
                editType |= Property.EDIT_TYPE_CHECKBOX;
            }
            return editType;
        }
    }

    @Override
    protected JComponent createEditorComponent(int editType, boolean label) {

        switch (editType) {
            case EDIT_TYPE_CHECKBOX:
                final JCheckBox b = new JCheckBox(label ? this.getName() : null) {

                    ChangeListener l = new ChangeListener() {

                        public void stateChanged(ChangeEvent evt) {

                            setSelected(IntegerProperty.this.getValue() != IntegerProperty.this
                                    .getMinimum());
                            setEnabled(IntegerProperty.this.isEditable());
                        }
                    };

                    @Override
                    public void addNotify() {

                        super.addNotify();
                        IntegerProperty.this.addChangeListener(this.l);
                        this.l.stateChanged(new ChangeEvent(IntegerProperty.this));
                    }

                    @Override
                    public void removeNotify() {

                        IntegerProperty.this.removeChangeListener(this.l);
                        super.removeNotify();
                    }
                };
                b.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {

                        IntegerProperty.this.setValue(b.isSelected() ? IntegerProperty.this
                                .getMaximum() : IntegerProperty.this.getMinimum());
                    }
                });
                return b;

            case EDIT_TYPE_SLIDER:
                final JSlider s = new JSlider(SwingConstants.HORIZONTAL) {

                    boolean latentDisabled = false;

                    boolean updating = false;

                    ChangeListener valueListener = new ChangeListener() {

                        public void stateChanged(ChangeEvent evt) {

                            if (!updating) {
                                updateValue();
                            }
                        }
                    };

                    ChangeListener sliderListener = new ChangeListener() {

                        public void stateChanged(ChangeEvent evt) {

                            if (!updating) {
                                IntegerProperty.this.setValue(getSliderValue());
                                setToolTipText(String.valueOf(IntegerProperty.this.getValue()));
                            }
                        }
                    };

                    private void updateValue() {

                        this.updating = true;
                        if (IntegerProperty.this.getMinimum() >= IntegerProperty.this
                                .getMaximum()) {
                            this.latentDisabled = true;
                        } else {
                            this.getModel().setRangeProperties(
                                    IntegerProperty.this.getValue(), 0,
                                    IntegerProperty.this.getMinimum(),
                                    IntegerProperty.this.getMaximum(), false);

                            this.setMajorTickSpacing((this.getSliderMax() - this
                                    .getSliderMin()) / 5);
                            this.setMinorTickSpacing((this.getSliderMax() - this
                                    .getSliderMin()) / 20);
                            this.setPaintTicks(true);
                            this.setPaintTrack(true);
                            this.setPaintLabels(true);
                            this.latentDisabled = false;
                        }

                        this
                                .setToolTipText(String.valueOf(IntegerProperty.this.getValue()));

                        this.repaint();
                        this.updating = false;
                    }

                    // wrappers to differentiate from property methods and
                    // because JSlider.this won't work for certain compilers
                    private int getSliderValue() {

                        return this.getValue();
                    }

                    private int getSliderMin() {

                        return this.getMinimum();
                    }

                    private int getSliderMax() {

                        return this.getMaximum();
                    }

                    @Override
                    public boolean isEnabled() {

                        // Problem: Property.this is only initialized after the
                        // whole Slider object is constructed.
                        // Unfortunately, isEnabled() is called already from
                        // within installUI in the superclass constructor.
                        // Therefore we have to check manually, that
                        // Property.this has been initialized.
                        if (IntegerProperty.this == null) {
                            return super.isEnabled();
                        } else {
                            return super.isEnabled() && IntegerProperty.this.isEditable()
                                    && !this.latentDisabled;
                        }
                    }

                    @Override
                    protected synchronized void fireStateChanged() {

                        if (!this.updating) {
                            super.fireStateChanged();
                        }
                    }

                    @Override
                    public void addNotify() {

                        super.addNotify();
                        this.updateValue();
                        IntegerProperty.this.addChangeListener(this.valueListener);
                        this.addChangeListener(this.sliderListener);
                    }

                    @Override
                    public void removeNotify() {

                        this.removeChangeListener(this.sliderListener);
                        IntegerProperty.this.removeChangeListener(this.valueListener);
                        super.removeNotify();
                    }

                    @Override
                    public Dimension getMinimumSize() {

                        return this.getPreferredSize();
                    }
                };

                if (label) {
                    JPanel p = new JPanel(new GridBagLayout()) {

                        @Override
                        public void setEnabled(boolean enabled) {

                            super.setEnabled(enabled);
                            for (int i = 0; i < this.getComponentCount(); i++) {
                                this.getComponent(i).setEnabled(enabled);
                            }
                        }
                    };
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.weightx = 0.0;
                    gbc.weighty = 0.0;
                    JLabel l = new JLabel(this.getName());
                    l.setLabelFor(s);
                    p.add(l, gbc);
                    gbc.gridx++;
                    gbc.weightx = 1.0;
                    gbc.insets = new Insets(0, 6, 0, 0);
                    p.add(s, gbc);
                    return p;
                } else {
                    return s;
                }

            default:
                return super.createEditorComponent(editType, label);
        }
    }
}
