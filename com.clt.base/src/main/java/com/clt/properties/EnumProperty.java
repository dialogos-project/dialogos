package com.clt.properties;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class EnumProperty<E> extends Property<E> {

    public EnumProperty(String id) {
        super(id, Property.EDIT_TYPE_COMBOBOX);
    }

    public static <T extends Enum<T>> EnumProperty<T> create(final Class<T> c) {
        return EnumProperty.create(c, null, null);
    }

    public static <T extends Enum<T>> EnumProperty<T> create(final Class<T> c,
            final String name,
            final String description) {

        return EnumProperty.create(c, name, description, c.getEnumConstants(), null);
    }

    public static <T extends Enum<T>> EnumProperty<T> create(final Class<T> c,
            final String name,
            final String description, final T[] values, final T initialValue) {

        return new EnumProperty<T>(c.getName()) {

            private T value = initialValue;

            @Override
            public String getName() {

                return name != null ? name : super.getName();
            }

            @Override
            public String getDescription() {

                return description;
            }

            @Override
            public T getValue() {

                return this.value;
            }

            @Override
            protected void setValueImpl(T value) {

                this.value = value;
            }

            @Override
            public T[] getPossibleValues() {

                return values;
            }
        };
    }

    public abstract E getValue();

    protected void setValueImpl(E value) {

        // the default implementation does nothing
    }

    @Override
    public final void setValue(E value) {

        if (value != this.getValue()) {
            E[] values = this.getPossibleValues();
            if (value == null) {
                this.setValueImpl(null);
                this.fireChange();
            } else {
                for (int i = 0; i < values.length; i++) {
                    if (value == null ? values[i] == null : value.equals(values[i])) {
                        this.setValueImpl(values[i]);
                        this.fireChange();
                        return;
                    }
                }
                throw new IllegalArgumentException("value out of range");
            }
        }
    }

    @Override
    public E getValueAsObject() {
        return this.getValue();
    }

    @Override
    public void setValueFromString(String value) {
        if (value != this.getValue()) {
            E[] values = this.getPossibleValues();
            for (int i = 0; i < values.length; i++) {
                if (value == null ? values[i] == null : value.equals(values[i].toString())) {
                    this.setValueImpl(values[i]);
                    this.fireChange();
                    return;
                }
            }

            throw new IllegalArgumentException(value + " is not a legal value for property " + this.getName());
        }
    }

    @Override
    public String getValueAsString() {
        Object value = this.getValue();
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    @Override
    protected int getSupportedEditTypesImpl() {

        return Property.EDIT_TYPE_COMBOBOX | Property.EDIT_TYPE_LIST
                | Property.EDIT_TYPE_RADIOBUTTONS
                | Property.EDIT_TYPE_RADIOBUTTONS_HORIZONTAL
                | Property.EDIT_TYPE_RADIOBUTTONS_VERTICAL;
    }
}
