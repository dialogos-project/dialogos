/*
 * @(#)DateProperty.java
 * Created on 07.02.2006 by dabo
 *
 * Copyright (c) 2006 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.properties;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author dabo
 */
public abstract class DateProperty
    extends Property<Date> {

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
      this.setValue((Date)null);
    }
    else {
      this.setValue(this.format.parse(value));
    }
  }


  @Override
  public String getValueAsString() {

    Date d = this.getValue();
    if (d == null) {
      return null;
    }
    else {
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
