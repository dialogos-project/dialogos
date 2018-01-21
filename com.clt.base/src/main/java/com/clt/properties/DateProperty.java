package com.clt.properties;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author dabo
 */
public abstract class DateProperty extends Property<Date> {

    private DateFormat format;

    public DateProperty(String id) {

        super(id, Property.EDIT_TYPE_TEXTFIELD);

        this.format = new SimpleDateFormat("dd.MM.yyyy");
    }

    public abstract Date getValue();

    protected abstract void setValueImpl(Date value);

    @Override
    public final void setValue(Date value) {

        if (value != this.getValue()) {
            this.setValueImpl(value);
            this.fireChange();
        }
    }

    public void setValue(Calendar c) {

        this.setValue(c.getTime());
    }

    @Override
    public void setValueFromString(String value)
            throws ParseException {

        if (value == null) {
            this.setValue((Date) null);
        } else {
            this.setValue(this.format.parse(value));
        }
    }

    @Override
    public String getValueAsString() {

        Date d = this.getValue();
        if (d == null) {
            return null;
        } else {
            return this.format.format(d);
        }
    }

    @Override
    public Date[] getPossibleValues() {

        return null;
    }

    @Override
    public Date getValueAsObject() {

        return this.getValue();
    }

    @Override
    protected int getSupportedEditTypesImpl() {

        return Property.EDIT_TYPE_TEXTFIELD;
    }
}
