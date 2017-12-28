/*
 * @(#)InputHandlerDialog.java
 * Created on Tue Aug 24 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
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
import java.awt.Window;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.InputHandler;
import com.clt.gui.GUI;
import com.clt.gui.table.ChoiceEditor;
import com.clt.gui.table.ItemTableModel;
import com.clt.gui.table.TableEditDialog;
import com.clt.gui.table.TableEditor;
import com.clt.util.StringTools;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class InputHandlerDialog
    extends TableEditor<InputHandler> {

  public InputHandlerDialog(Graph owner, String title) {

    super(InputHandlerDialog.createModel(owner), title, true, null);
  }


  public static void showDialog(Component parent, Graph owner) {

    new TableEditDialog<InputHandler>(parent, InputHandlerDialog
      .createModel(owner), Resources.getString("InputHandlers"), null, true)
      .setVisible(true);
  }


  private static ItemTableModel<InputHandler> createModel(final Graph owner) {

    ItemTableModel<InputHandler> model =
      new ItemTableModel<InputHandler>(owner.getHandlers()) {

        @Override
        public int getColumnCount() {

          return 3;
        }


        @Override
        public int getColumnWidth(int columnIndex) {

          switch (columnIndex) {
            case 2:
                        return 120;
                      default:
                        return super.getColumnWidth(columnIndex);
                    }
                  }


        @Override
        public String getColumnName(int columnIndex) {

          switch (columnIndex) {
            case 0:
                        return Resources.getString("Name");
                      case 1:
                        return Resources.getString("Patterns");
                      case 2:
                        return Resources.getString("Type");
                      default:
                        return null;
                    }
                  }


        @Override
        public Class<?> getColumnClass(int columnIndex) {

          return String.class;
        }


        @Override
        public boolean isColumnDraggable(int columnIndex) {

          return columnIndex != 2;
        }


        @Override
        public Object getValue(InputHandler h, int columnIndex) {

          switch (columnIndex) {
            case 0:
                        return h.getTitle();
                      case 1:
                        return h.getPattern();
                      case 2:
                        return InputHandler.TYPENAMES[h.getType()];
                      default:
                        return null;
                    }
                  }


        @Override
        public void setValue(InputHandler h, int columnIndex, Object aValue) {

          switch (columnIndex) {
            case 0:
                        h.setTitle((String)aValue);
                        break;
                      case 1:
                        h.setPattern((String)aValue);
                        break;
                      case 2:
                        h.setType(StringTools.indexOf(InputHandler.TYPENAMES,
                          (String)aValue));
                        break;
                    }
                  }

        ChoiceEditor typeComboBox = new ChoiceEditor(InputHandler.TYPENAMES);


        @Override
        public TableCellRenderer getColumnRenderer(int columnIndex) {

          switch (columnIndex) {
            case 2:
                        return this.typeComboBox;
                      default:
                        return super.getColumnRenderer(columnIndex);
                    }
                  }


        @Override
        public TableCellEditor getColumnEditor(int columnIndex) {

          switch (columnIndex) {
            case 2:
                        return this.typeComboBox;
                      default:
                        return super.getColumnEditor(columnIndex);
                    }
                  }


        @Override
        protected void editItem(Component parent, InputHandler h) {

          Window w = GUI.getWindowForComponent(parent);
          if (w instanceof TableEditDialog) {
            w.dispose();
          }
          h.editProperties(parent);
          // owner.updateEdges();
        }


        @Override
        protected InputHandler createNewItem(Component parent) {

          return new InputHandler(owner);
        }
      };

    model.addTableModelListener(new TableModelListener() {

      public void tableChanged(TableModelEvent e) {

        owner.updateEdges();
      }
    });

    return model;
  }
}
