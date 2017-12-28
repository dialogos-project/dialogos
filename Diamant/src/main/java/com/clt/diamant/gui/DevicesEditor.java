/*
 * @(#)DevicesEditor.java
 * Created on 04.12.2006 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.diamant.gui;

import java.awt.Component;
import java.util.List;

import com.clt.diamant.Device;
import com.clt.diamant.Resources;
import com.clt.gui.table.ItemTableModel;
import com.clt.gui.table.TableEditDialog;
import com.clt.gui.table.TableEditor;

/**
 * @author dabo
 * 
 */
public class DevicesEditor
    extends TableEditor<Device> {

  public DevicesEditor(List<Device> devices, String title) {

    super(DevicesEditor.createModel(devices), title, false, null);
  }


  public static void showDialog(Component parent, List<Device> variables,
      String title) {

    new TableEditDialog<Device>(parent, DevicesEditor.createModel(variables),
      title, null, false).setVisible(true);
  }


  private static ItemTableModel<Device> createModel(List<Device> devices) {

    return new ItemTableModel<Device>(devices) {

      @Override
      public int getColumnCount() {

        return 1;
      }


      @Override
      public String getColumnName(int columnIndex) {

        switch (columnIndex) {
          case 0:
            return Resources.getString("Name");
        }
        return null;
      }


      @Override
      public int getColumnWidth(int columnIndex) {

        switch (columnIndex) {
          case 1:
            return 80;
          case 3:
            return 60;

          default:
            return super.getColumnWidth(columnIndex);
        }
      }


      /**
       * Returns the lowest common denominator Class in the column. This is used
       * by the table to set up a default renderer and editor for the column.
       * 
       * @return the common ancestor class of the object values in the model.
       */
      @Override
      public Class<?> getColumnClass(int columnIndex) {

        switch (columnIndex) {
          case 0:
            return String.class;
        }
        return Object.class;
      }


      @Override
      public boolean isColumnDraggable(int columnIndex) {

        return (columnIndex == 0) || (columnIndex == 2);
      }


      @Override
      protected boolean readyToClose(Component parent) {

        return true;
      }


      @Override
      protected Device createNewItem(Component parent) {

        Device d = new Device();
        if (DeviceEditor.editDevice(d, parent)) {
          return d;
        }
        else {
          return null;
        }
      }


      @Override
      protected boolean confirmDelete(Component parent, int[] indices) {

        return true;
      }


      @Override
      public Object getValue(Device d, int columnIndex) {

        switch (columnIndex) {
          case 0:
            return d.getName();
        }
        return null;
      }


      /**
       * Sets an attribute for the slot at <I>columnIndex</I>.
       * 
       * @param aValue
       *          the new value
       * @param d
       *          the slot whose attribute is to be changed
       * @param columnIndex
       *          the column whose value is to be changed
       * @see #getValue
       */
      @Override
      public void setValue(Device d, int columnIndex, Object aValue) {

        switch (columnIndex) {
          case 0:
            d.setName(String.valueOf(aValue));
            break;
        }
      }
    };
  }
}