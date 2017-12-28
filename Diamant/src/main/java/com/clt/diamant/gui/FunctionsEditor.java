/*
 * @(#)FunctionsEditor.java
 * Created on 30.11.2006 by dabo
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

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Functions;
import com.clt.gui.ListEditor;
import com.clt.gui.ListEditorDialog;

/**
 * @author dabo
 * 
 */
public class FunctionsEditor
    extends ListEditor {

  public FunctionsEditor(List<Functions> functions) {

    super(FunctionsEditor.createModel(functions), true);
  }


  public static void showDialog(Component parent, List<Functions> functions) {

    new ListEditorDialog(parent, Resources.getString("Functions"),
      FunctionsEditor.createModel(functions)).setVisible(true);
  }


  private static ListEditor.Model createModel(final List<Functions> functions) {

    return new ListEditor.Model() {

      private boolean edit(Functions functions, Component parent) {

        return ScriptEditorDialog.editFunctions(parent, functions);
      }


      public int getSize() {

        return functions.size();
      }


      public Object getElementAt(int index) {

        return functions.get(index);
      }


      @Override
      public void editItemAt(Component parent, int index) {

        this.edit(functions.get(index), parent);
      }


      @Override
      public int addElement(Component parent) {

        Functions f = new Functions(Resources.getString("Untitled"), "");
        if (this.edit(f, parent)) {
          functions.add(f);
          return functions.indexOf(f);
        }
        else {
          return -1;
        }
      }


      @Override
      public boolean removeElement(Component parent, int index) {

        functions.remove(index);
        return true;
      }


      @Override
      public boolean moveElement(int from, int to) {

        Functions o = functions.get(from);
        if (from < to) {
          for (int i = from; i < to; i++) {
            functions.set(i, functions.get(i + 1));
          }
        }
        else {
          for (int i = from; i > to; i--) {
            functions.set(i, functions.get(i - 1));
          }
        }
        functions.set(to, o);
        this.fireContentsChanged(this, Math.min(from, to), Math.max(from, to));
        return true;
      }
    };
  }
}
