package com.clt.properties;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Dictionary;
import java.util.Hashtable;

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
public abstract class FloatProperty extends Property<Float> {

    private Float[] values = null;
    private float minimum;
    private float maximum;
    
    public FloatProperty(String id) {
        this(id, 0.0f, 0.0f);
    }

    public FloatProperty(String id, float minimum, float maximum) {
        super(id, minimum == maximum ? Property.EDIT_TYPE_NUMBERFIELD : Property.EDIT_TYPE_SLIDER);

        this.setRangeImpl(minimum, maximum);
    }

    public abstract float getValue();

    protected void setValueImpl(float value) {

        // the default implementation does nothing
    }

    public final void setValue(float value) {

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

        this.setValue(Float.parseFloat(value));
    }

    @Override
    public String getValueAsString() {

        return String.valueOf(this.getValue());
    }

    public float getMinimum() {

        return this.minimum;
    }

    public float getMaximum() {

        return this.maximum;
    }

    public void setRange(float minimum, float maximum) {

        this.setRangeImpl(minimum, maximum);
        if (minimum != maximum) {
            float value = this.getValue();
            if (value < minimum) {
                this.setValueImpl(minimum);
            } else if (value > maximum) {
                this.setValueImpl(maximum);
            }
        }
        this.fireChange();
    }

    private void setRangeImpl(float minimum, float maximum) {

        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum must be <= maximum");
        }

        this.minimum = minimum;
        this.maximum = maximum;

        this.setEditType(minimum == maximum ? Property.EDIT_TYPE_NUMBERFIELD
                : Property.EDIT_TYPE_SLIDER);
    }

    @Override
    public Float[] getPossibleValues() {

        return this.values;
    }

    @Override
    public void setValue(Float o) {

        this.setValue(o.floatValue());
    }

    @Override
    public Float getValueAsObject() {

        return new Float(this.getValue());
    }

    @Override
    protected int getSupportedEditTypesImpl() {

        if (this.minimum == this.maximum) {
            return Property.EDIT_TYPE_NUMBERFIELD | Property.EDIT_TYPE_TEXTFIELD;
        } else {
            return Property.EDIT_TYPE_SLIDER | Property.EDIT_TYPE_NUMBERFIELD
                    | Property.EDIT_TYPE_TEXTFIELD;
        }
    }

    @Override
    protected JComponent createEditorComponent(int editType, boolean label) {

        switch (editType) {
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
                                double pmin = FloatProperty.this.getMinimum();
                                double pmax = FloatProperty.this.getMaximum();
                                double min = getSliderMin();
                                double max = getSliderMax();
                                double val = getSliderValue();
                                FloatProperty.this.setValue((float) (pmin + (val - min)
                                        / (max - min) * (pmax - pmin)));
                                setToolTipText(String.valueOf(FloatProperty.this.getValue()));
                            }
                        }
                    };

                    ComponentListener componentListener = new ComponentAdapter() {

                        @Override
                        public void componentResized(ComponentEvent evt) {

                            if (!updating) {
                                updateValue();
                            }
                        }
                    };

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

                    private void updateValue() {

                        this.updating = true;
                        if (FloatProperty.this.getMinimum() >= FloatProperty.this
                                .getMaximum()) {
                            this.latentDisabled = true;
                        } else {
                            // getModel().setRangeProperties((int)
                            // FloatProperty.this.getValue(), 0,
                            // (int) FloatProperty.this.getMinimum(), (int)
                            // FloatProperty.this.getMaximum(), false);

                            double min = FloatProperty.this.getMinimum();
                            double max = FloatProperty.this.getMaximum();
                            double val = FloatProperty.this.getValue();
                            this.getModel().setRangeProperties(
                                    (int) ((val - min) / (max - min) * this
                                            .getWidth()), 0, 0, this.getWidth(),
                                    false);

                            int majorTicks = 5;

                            this.setMajorTickSpacing((this.getSliderMax() - this
                                    .getSliderMin())
                                    / majorTicks);
                            this.setMinorTickSpacing((this.getSliderMax() - this
                                    .getSliderMin()) / 20);
                            this.setPaintTicks(true);
                            this.setPaintTrack(true);
                            this.setPaintLabels(true);

                            Dictionary<Integer, JLabel> labels
                                    = new Hashtable<Integer, JLabel>();
                            int numDecimalPlaces
                                    = (int) Math.floor((Math.log(max - min) / Math.log(10))) - 1;
                            String formatString = "#0";
                            if (numDecimalPlaces < 0) {
                                formatString += ".";
                                while (numDecimalPlaces++ < 0) {
                                    formatString += "#";
                                }
                            }
                            NumberFormat format = new DecimalFormat(formatString);
                            for (int i = 0; i <= majorTicks; i++) {
                                labels
                                        .put(
                                                new Integer(i * this.getMajorTickSpacing()),
                                                new JLabel(
                                                        format
                                                                .format(min
                                                                        + ((max - min)
                                                                        * (i * this
                                                                                .getMajorTickSpacing()) / this
                                                                                .getSliderMax())),
                                                        SwingConstants.CENTER));
                            }

                            this.setLabelTable(labels);

                            this.latentDisabled = false;

                        }

                        this.setToolTipText(String.valueOf(FloatProperty.this.getValue()));
                        this.repaint();
                        this.updating = false;
                    }

                    @Override
                    public boolean isEnabled() {

                        // Problem: Property.this is only initialized after the
                        // whole Slider object is constructed.
                        // Unfortunately, isEnabled() is called already from
                        // within installUI in the superclass constructor.
                        // Therefore we have to check manually, that
                        // Property.this has been initialized.
                        if (FloatProperty.this == null) {
                            return super.isEnabled();
                        } else {
                            return super.isEnabled() && FloatProperty.this.isEditable()
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
                        this.addComponentListener(this.componentListener);
                        FloatProperty.this.addChangeListener(this.valueListener);
                        this.addChangeListener(this.sliderListener);
                    }

                    @Override
                    public void removeNotify() {

                        this.removeChangeListener(this.sliderListener);
                        FloatProperty.this.removeChangeListener(this.valueListener);
                        this.removeComponentListener(this.componentListener);
                        super.removeNotify();
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
