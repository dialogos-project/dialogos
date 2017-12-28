/*
 * @(#)ListEditor.java
 * Created on 15.10.04
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

package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.clt.gui.table.TableEditor;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ListEditor
    extends JPanel
    implements Commander {

  private static final int cmdEditItem = Commands.cmdApplication + 1;

  private Model model;

  private JList list;

  private JButton newButton;
  private JButton deleteButton;
  private JButton editButton;

  public abstract static class Model
        extends AbstractListModel
        implements TableModel {

    private String name;
    private Collection<TableModelListener> tableModelListeners;


    public Model() {

      this("");
    }


    public Model(String name) {

      this.name = name;
      this.tableModelListeners = new LinkedList<TableModelListener>();
      this.addListDataListener(new ListDataListener() {

        public void intervalAdded(ListDataEvent e) {

          TableModelEvent te = new TableModelEvent(Model.this, e.getIndex0(),
                        e.getIndex1(), 0, TableModelEvent.INSERT);
          for (TableModelListener l : Model.this.tableModelListeners) {
            l.tableChanged(te);
          }
        }


        public void intervalRemoved(ListDataEvent e) {

          TableModelEvent te = new TableModelEvent(Model.this, e.getIndex0(),
                        e.getIndex1(), 0, TableModelEvent.DELETE);
          for (TableModelListener l : Model.this.tableModelListeners) {
            l.tableChanged(te);
          }
        }


        public void contentsChanged(ListDataEvent e) {

          TableModelEvent te = new TableModelEvent(Model.this, e.getIndex0(),
                        e.getIndex1(), 0, TableModelEvent.UPDATE);
          for (TableModelListener l : Model.this.tableModelListeners) {
            l.tableChanged(te);
          }
        }

      });
    }


    protected abstract void editItemAt(Component parent, int index);


    /**
     * @return The position where the new element was inserted or -1 if no
     *         element was added
     */
    protected abstract int addElement(Component parent);


    /**
     * @return true, if the element was successfully removed
     */
    protected abstract boolean removeElement(Component parent, int index);


    public boolean moveElement(int src, int dst) {

      return false;
    }


    public int addNewElement(Component parent) {

      int index = this.addElement(parent);
      if (index >= 0) {
        /*
         * ListDataListener[] listeners = getListDataListeners(); ListDataEvent
         * evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index,
         * index); for (int i=0; i<listeners.length; i++)
         * listeners[i].intervalAdded(evt);
         */
        this.fireIntervalAdded(this, index, index);
      }
      return index;
    }


    public void removeElements(Component parent, int[] indices) {

      if (indices != null) {
        Arrays.sort(indices);
        /*
         * ListDataListener[] listeners = getListDataListeners(); for (int
         * i=indices.length-1; i>=0; i--) { if (removeElement(parent,
         * indices[i])) { ListDataEvent evt = new ListDataEvent(this,
         * ListDataEvent.INTERVAL_REMOVED, indices[i], indices[i]); for (int
         * j=0; j<listeners.length; j++) listeners[j].intervalRemoved(evt); } }
         */
        for (int i = indices.length - 1; i >= 0; i--) {
          if (this.removeElement(parent, indices[i])) {
            this.fireIntervalRemoved(this, indices[i], indices[i]);
          }
        }
      }
    }


    public final int getRowCount() {

      return this.getSize();
    }


    public final int getColumnCount() {

      return 1;
    }


    public Class<?> getColumnClass(int columnIndex) {

      return Object.class;
    }


    public String getColumnName(int columnIndex) {

      return this.name;
    }


    public Object getValueAt(int rowIndex, int columnIndex) {

      return this.getElementAt(rowIndex);
    }


    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

      // can't change elements.
      // You can only edit them using editElementAt()
    }


    public boolean isCellEditable(int rowIndex, int columnIndex) {

      return false;
    }


    public void addTableModelListener(TableModelListener l) {

      this.tableModelListeners.add(l);
    }


    public void removeTableModelListener(TableModelListener l) {

      this.tableModelListeners.remove(l);
    }
  }


  public ListEditor(Model model) {

    this(model, new JButton[0]);
  }


  public ListEditor(Model model, boolean showEditButton) {

    this(model, showEditButton, new JButton[0]);
  }


  public ListEditor(Model model, JButton additionalButton) {

    this(model, new JButton[] { additionalButton });
  }


  public ListEditor(Model model, boolean showEditButton,
      JButton additionalButton) {

    this(model, showEditButton, new JButton[] { additionalButton });
  }


  public ListEditor(final Model model, JButton[] additionalButtons) {

    this(model, false, additionalButtons);
  }


  public ListEditor(final Model model, boolean showEditButton,
      JButton[] additionalButtons) {

    super(new BorderLayout());

    this.model = model;

    this.list = new JList(model);

    this.list.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent evt) {

        if (!evt.getValueIsAdjusting()) {
          ListEditor.this.updateButtons();
        }
      }
    });

    this.list.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent evt) {

        if (evt.getClickCount() == 2) {
          int index = ListEditor.this.list.locationToIndex(evt.getPoint());
          if (index >= 0) {
            model.editItemAt(ListEditor.this.list, index);
          }
        }
      }
    });

    /*
     * list.registerKeyboardAction(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { int[] selection =
     * list.getSelectedIndices(); if (selection != null && selection.length ==
     * 1) model.editElementAt(list, selection[0]); } },
     * KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
     */

    GUI.addMouseInputListener(this.list, new MouseInputAdapter() {

      private int draggedRow = -1;


      @Override
      public void mousePressed(MouseEvent e) {

        int row = ListEditor.this.list.locationToIndex(e.getPoint());
        this.draggedRow = row;
      }


      @Override
      public void mouseDragged(MouseEvent e) {

        if (this.draggedRow != -1) {
          int target =
            ListEditor.this.list.locationToIndex(new Point(0, e.getY()));
          if (target == -1) {
            Rectangle bounds = ListEditor.this.list.getBounds();
            if (e.getY() <= bounds.y) {
              target = 0;
            }
            else if (e.getY() >= bounds.y + bounds.height) {
              target = model.getSize() - 1;
            }
          }
          if (target != this.draggedRow) {
            boolean moved = model.moveElement(this.draggedRow, target);

            if (moved) {
              this.draggedRow = target;
              ListEditor.this.list.setSelectedIndex(this.draggedRow);
            }
          }
        }
      }


      @Override
      public void mouseReleased(MouseEvent e) {

        this.draggedRow = -1;
      }
    });

    JScrollPane jsp =
      GUI.createScrollPane(this.list,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    this.add(jsp, BorderLayout.CENTER);

    this.list.setCellRenderer(new TableEditor.Renderer());

    JPanel buttonPanel = new JPanel(new BorderLayout());

    this.newButton = new CmdButton(this, Commands.cmdNew, GUI.getString("New"));
    this.deleteButton =
      new CmdButton(this, Commands.cmdDelete, GUI.getString("Delete"));
    // newButton = new CmdButton(this, cmdNew, GUI.getString("Add"));
    // deleteButton = new CmdButton(this, cmdDelete, GUI.getString("Remove"));
    this.editButton =
      new CmdButton(this, ListEditor.cmdEditItem, GUI.getString("Edit"));

    JButton[] buttons;
    if (showEditButton) {
      buttons =
        new JButton[] { this.newButton, this.editButton, this.deleteButton };
    }
    else {
      buttons = new JButton[] { this.newButton, this.deleteButton };
    }

    // buttonPanel.add(createButtonPanel(buttons, additionalButtons),
    // BorderLayout.CENTER);
    buttonPanel.add(TableEditor.createButtonPanel(buttons, additionalButtons),
      BorderLayout.CENTER);

    this.add(buttonPanel, BorderLayout.SOUTH);

    GUI.assignMnemonics(this);

    // buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    Dimension d = this.getPreferredSize();
    d.width = Math.max(d.width, 400);
    d.height = Math.max(d.height, 250);
    this.setPreferredSize(d);

    this.updateButtons();
  }


  private void updateButtons() {

    int[] indices = this.list.getSelectedIndices();
    this.deleteButton.setEnabled((indices != null) && (indices.length > 0));
    this.editButton.setEnabled((indices != null) && (indices.length == 1));
  }


  public boolean doCommand(int cmd) {

    switch (cmd) {
      case cmdNew:
        this.list.clearSelection();
        int index = this.model.addNewElement(this);
        if (index >= 0) {
          this.list.setSelectedIndex(index);
        }
        this.list.requestFocusInWindow();
        break;
      case cmdDelete:
        this.model.removeElements(this, this.list.getSelectedIndices());
        this.list.clearSelection();
        this.updateButtons();
        this.list.requestFocusInWindow();
        break;
      case cmdEditItem:
        int[] selection = this.list.getSelectedIndices();
        if (selection != null) {
          for (int i = 0; i < selection.length; i++) {
            this.model.editItemAt(this.list, selection[i]);
          }
        }
        this.list.requestFocusInWindow();
        break;
      default:
        return false;
    }
    return true;
  }


  @SuppressWarnings("unused")
  private JPanel createButtonPanel(JButton[] left, JButton[] right) {

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.gridy = 0;

    gbc.insets = new Insets(6, 12, 6, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.0;

    Dimension d_left = new Dimension(0, 0);
    for (int i = 0; i < left.length; i++) {
      p.add(left[i], gbc);
      d_left.width = Math.max(d_left.width, left[i].getPreferredSize().width);
      d_left.height =
        Math.max(d_left.height, left[i].getPreferredSize().height);
      gbc.gridx++;
    }

    gbc.weightx = 1.0;
    gbc.insets = new Insets(6, 0, 6, 12);
    p.add(Box.createHorizontalGlue(), gbc);

    if (right != null) {
      gbc.gridx++;
      gbc.weightx = 0.0;
      Dimension d_right = new Dimension(0, 0);
      for (int i = 0; i < right.length; i++) {
        p.add(right[i], gbc);
        d_right.width =
          Math.max(d_right.width, right[i].getPreferredSize().width);
        d_right.height =
          Math.max(d_right.height, right[i].getPreferredSize().height);
        gbc.gridx++;
      }

      for (int i = 0; i < left.length; i++) {
        left[i].setPreferredSize(d_left);
      }
      for (int i = 0; i < right.length; i++) {
        right[i].setPreferredSize(d_right);
      }
    }

    return p;
  }

}