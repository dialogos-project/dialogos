/*
 * @(#)Undo.java
 * Created on Thu Sep 18 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.undo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Stack;

import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * @author Daniel
 * @version 1.0
 */

public class Undo
    extends UndoManager {

  private MultiEdit currentEdit;

  private Stack<MultiEdit> edits;

  private Exception criticalSectionOwner = null;


  public Undo() {

    this.currentEdit = null;
    this.edits = new Stack<MultiEdit>();
  }


  @Override
  public synchronized boolean addEdit(UndoableEdit edit) {

    this.checkCriticalSection();

    this.criticalSectionOwner = new Exception();

    if (edit instanceof AbstractEdit) {
      ((AbstractEdit)edit).run();
    }

    boolean result;
    try {
      if (this.currentEdit != null) {
        result = this.currentEdit.addEdit(edit);
      }
      else {
        result = super.addEdit(edit);
      }
    } finally {
      this.criticalSectionOwner = null;
    }
    return result;
  }


  @Override
  public synchronized void undo()
      throws CannotUndoException {

    this.checkCriticalSection();

    this.criticalSectionOwner = new Exception();
    try {
      super.undo();
    } finally {
      this.criticalSectionOwner = null;
    }
  }


  @Override
  public synchronized void redo()
      throws CannotRedoException {

    this.checkCriticalSection();

    this.criticalSectionOwner = new Exception();
    try {
      super.redo();
    } finally {
      this.criticalSectionOwner = null;
    }
  }


  public synchronized void startEdit(final String description) {

    this.checkCriticalSection();

    this.edits.push(this.currentEdit);
    this.currentEdit = new MultiEdit() {

      @Override
      public String getPresentationName() {

        if (description != null) {
          return description;
        }
        else if (!this.isEmpty()) {
          return this.lastEdit().getPresentationName();
        }
        else {
          return "";
        }
      }


      @Override
      public String getUndoPresentationName() {

        return UIManager.getString("AbstractUndoableEdit.undoText") + " "
          + description;
      }


      @Override
      public String getRedoPresentationName() {

        return UIManager.getString("AbstractUndoableEdit.redoText") + " "
          + description;
      }
    };
  }


  public synchronized void endEdit() {

    this.checkCriticalSection();

    MultiEdit edit = this.currentEdit;
    edit.end();
    this.currentEdit = this.edits.pop();
    if (!edit.isEmpty()) {
      this.addEdit(edit);
    }
  }


  private void checkCriticalSection() {

    if (this.criticalSectionOwner != null) {
      StringWriter stackTrace = new StringWriter();
      PrintWriter pw = new PrintWriter(stackTrace);
      StackTraceElement[] trace = this.criticalSectionOwner.getStackTrace();
      int traceLength = 8;
      for (int i = 0; (i < trace.length) && (i < traceLength); i++) {
        pw.println("\tat " + trace[i]);
      }
      if (trace.length > traceLength) {
        pw.println("\t...");
      }
      pw.flush();
      throw new IllegalStateException(
                "You cannot recursively undo, redo or add new edits while already within such a section. Previous invocation\n"
                        + stackTrace.toString() + "\nCurrent invocation:");
    }
  }

  private static class MultiEdit
        extends CompoundEdit {

    public boolean isEmpty() {

      return this.lastEdit() == null;
    }
  }
}
