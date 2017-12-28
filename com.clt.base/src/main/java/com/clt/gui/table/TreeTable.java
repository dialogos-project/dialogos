/*
 * @(#)TreeTable.java	1.2 98/10/27
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package com.clt.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * This example shows how to create a simple TreeTable component, by using a
 * JTree as a renderer (and editor) for the cells in a particular column in the
 * JTable.
 * 
 * @version 1.2 10/27/98
 * @author Philip Milne
 * @author Scott Violet
 */
public class TreeTable
    extends JTable {

  /** A subclass of JTree. */
  protected TreeTableCellRenderer tree;

  private ListToTreeSelectionModelWrapper selectionWrapper;


  public TreeTable(TreeTableModel treeTableModel) {

    super();

    this.selectionWrapper = new ListToTreeSelectionModelWrapper();
    this.setSelectionModel(this.selectionWrapper.getListSelectionModel());

    this.setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

    // No grid.
    this.setShowGrid(false);

    // No intercell spacing
    this.setIntercellSpacing(new Dimension(0, 0));

    this.setTreeTableModel(treeTableModel);
  }


  public void setTreeTableModel(TreeTableModel treeTableModel) {

    // Create the tree. It will be used as a renderer and editor.
    this.tree = new TreeTableCellRenderer(treeTableModel);

    // Force the JTable and JTree to share their row selection models.
    this.tree.setSelectionModel(this.selectionWrapper);

    // Install the tree editor renderer and editor.
    this.setDefaultRenderer(TreeTableModel.class, this.tree);

    // Install a tableModel representing the visible rows in the tree.
    super.setModel(new TreeTableModelAdapter(treeTableModel, this.tree));

    // And update the height of the trees row to match that of
    // the table.
    if (this.tree.getRowHeight() < 1) {
      // Metal looks better like this.
      this.setRowHeight(18);
    }
  }


  /**
   * Overridden to message super and forward the method to the tree. Since the
   * tree is not actually in the component hieachy it will never receive this
   * unless we forward it in this manner.
   */
  @Override
  public void updateUI() {

    super.updateUI();
    if (this.tree != null) {
      this.tree.updateUI();
    }

    // Use the tree's default foreground and background colors in the table.
    LookAndFeel.installColorsAndFont(this, "Tree.background",
      "Tree.foreground", "Tree.font");
  }


  /*
   * Workaround for BasicTableUI anomaly. Make sure the UI never tries to paint
   * the editor. The UI currently uses different techniques to paint the
   * renderers and editors and overriding setBounds() below is not the right
   * thing to do for an editor. Returning -1 for the editing row in this case,
   * ensures the editor is never painted.
   */
  @Override
  public int getEditingRow() {

    return (this.getColumnClass(this.editingColumn) == TreeTableModel.class)
      ? -1 : this.editingRow;
  }


  /**
   * Overridden to pass the new rowHeight to the tree.
   */
  @Override
  public void setRowHeight(int rowHeight) {

    super.setRowHeight(rowHeight);
    if ((this.tree != null) && (this.tree.getRowHeight() != rowHeight)) {
      this.tree.setRowHeight(this.getRowHeight());
    }
  }


  public void expandRow(int row) {

    this.tree.expandRow(row);
  }


  /**
   * Returns the tree that is being shared between the model.
   */
  public JTree getTree() {

    return this.tree;
  }

  /**
   * A TreeCellRenderer that displays a JTree.
   */
  public class TreeTableCellRenderer
        extends JTree
        implements TableCellRenderer {

    /** Last table/tree row asked to renderer. */
    protected int visibleRow;


    public TreeTableCellRenderer(TreeModel model) {

      super(model);
    }


    /**
     * updateUI is overridden to set the colors of the Tree's renderer to match
     * that of the table.
     */
    @Override
    public void updateUI() {

      super.updateUI();
      // Make the tree's cell renderer use the table's cell selection
      // colors.
      TreeCellRenderer tcr = this.getCellRenderer();
      if (tcr instanceof DefaultTreeCellRenderer) {
        DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
        // For 1.1 uncomment this, 1.2 has a bug that will cause an
        // exception to be thrown if the border selection color is
        // null.
        // dtcr.setBorderSelectionColor(null);
        dtcr.setTextSelectionColor(UIManager
          .getColor("Table.selectionForeground"));
        dtcr.setBackgroundSelectionColor(UIManager
          .getColor("Table.selectionBackground"));
      }
    }


    /**
     * Sets the row height of the tree, and forwards the row height to the
     * table.
     */
    @Override
    public void setRowHeight(int rowHeight) {

      if (rowHeight > 0) {
        super.setRowHeight(rowHeight);
        if ((TreeTable.this != null)
          && (TreeTable.this.getRowHeight() != rowHeight)) {
          TreeTable.this.setRowHeight(this.getRowHeight());
        }
      }
    }


    /**
     * This is overridden to set the height to match that of the JTable.
     */
    @Override
    public void setBounds(int x, int y, int w, int h) {

      super.setBounds(x, 0, w, TreeTable.this.getHeight());
    }


    /**
     * Sublcassed to translate the graphics such that the last visible row will
     * be drawn at 0,0.
     */
    @Override
    public void paint(Graphics g) {

      Graphics gfx = g.create();
      gfx.translate(0, -this.visibleRow * this.getRowHeight());
      super.paint(gfx);
    }


    /**
     * TreeCellRenderer method. Overridden to update the visible row.
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
        boolean hasFocus,
                                                       int row, int column) {

      if (isSelected) {
        this.setBackground(table.getSelectionBackground());
      }
      else {
        this.setBackground(table.getBackground());
      }

      this.visibleRow = row;
      return this;
    }
  }

  /**
   * TreeTableCellEditor implementation. Component returned is the JTree.
   */
  public class TreeTableCellEditor
        extends AbstractCellEditor
        implements TableCellEditor {

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected,
        int row, int column) {

      return TreeTable.this.tree;
    }


    /**
     * Overridden to return false, and if the event is a mouse event it is
     * forwarded to the tree.
     * <p>
     * The behavior for this is debatable, and should really be offered as a
     * property. By returning false, all keyboard actions are implemented in
     * terms of the table. By returning true, the tree would get a chance to do
     * something with the keyboard events. For the most part this is ok. But for
     * certain keys, such as left/right, the tree will expand/collapse where as
     * the table focus should really move to a different column. Page up/down
     * should also be implemented in terms of the table. By returning false this
     * also has the added benefit that clicking outside of the bounds of the
     * tree node, but still in the tree column will select the row, whereas if
     * this returned true that wouldn't be the case.
     * <p>
     * By returning false we are also enforcing the policy that the tree will
     * never be editable (at least by a key sequence).
     */
    @Override
    public boolean isCellEditable(EventObject e) {

      if (e instanceof MouseEvent) {
        for (int counter = TreeTable.this.getColumnCount() - 1; counter >= 0; counter--) {
          if (TreeTable.this.getColumnClass(counter) == TreeTableModel.class) {
            MouseEvent me = (MouseEvent)e;
            MouseEvent newME =
              new MouseEvent(TreeTable.this.tree, me.getID(), me.getWhen(), me
                                .getModifiers(), me.getX()
                - TreeTable.this.getCellRect(0, counter, true).x, me
                                .getY(), me.getClickCount(), me
                .isPopupTrigger());
            TreeTable.this.tree.dispatchEvent(newME);
            break;
          }
        }
      }
      return false;
    }
  }

  /**
   * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel to listen
   * for changes in the ListSelectionModel it maintains. Once a change in the
   * ListSelectionModel happens, the paths are updated in the
   * DefaultTreeSelectionModel.
   */
  class ListToTreeSelectionModelWrapper
        extends DefaultTreeSelectionModel {

    /** Set to true when we are updating the ListSelectionModel. */
    protected boolean updatingListSelectionModel;


    public ListToTreeSelectionModelWrapper() {

      super();
      this.getListSelectionModel().addListSelectionListener(
        this.createListSelectionListener());
    }


    /**
     * Returns the list selection model. ListToTreeSelectionModelWrapper listens
     * for changes to this model and updates the selected paths accordingly.
     */
    ListSelectionModel getListSelectionModel() {

      return this.listSelectionModel;
    }


    /**
     * This is overridden to set <code>updatingListSelectionModel</code> and
     * message super. This is the only place DefaultTreeSelectionModel alters
     * the ListSelectionModel.
     */
    @Override
    public void resetRowSelection() {

      if (!this.updatingListSelectionModel) {
        this.updatingListSelectionModel = true;
        try {
          super.resetRowSelection();
        } finally {
          this.updatingListSelectionModel = false;
        }
      }
      // Notice how we don't message super if
      // updatingListSelectionModel is true. If
      // updatingListSelectionModel is true, it implies the
      // ListSelectionModel has already been updated and the
      // paths are the only thing that needs to be updated.
    }


    /**
     * Creates and returns an instance of ListSelectionHandler.
     */
    protected ListSelectionListener createListSelectionListener() {

      return new ListSelectionHandler();
    }


    /**
     * If <code>updatingListSelectionModel</code> is false, this will reset the
     * selected paths from the selected rows in the list selection model.
     */
    protected void updateSelectedPathsFromSelectedRows() {

      if (!this.updatingListSelectionModel) {
        this.updatingListSelectionModel = true;
        try {
          // This is way expensive, ListSelectionModel needs an
          // enumerator for iterating.
          int min = this.listSelectionModel.getMinSelectionIndex();
          int max = this.listSelectionModel.getMaxSelectionIndex();

          this.clearSelection();
          if ((min != -1) && (max != -1)) {
            for (int counter = min; counter <= max; counter++) {
              if (this.listSelectionModel.isSelectedIndex(counter)) {
                TreePath selPath = TreeTable.this.tree.getPathForRow(counter);
                if (selPath != null) {
                  this.addSelectionPath(selPath);
                }
              }
            }
          }
        } finally {
          this.updatingListSelectionModel = false;
        }
      }
    }

    /**
     * Class responsible for calling updateSelectedPathsFromSelectedRows when
     * the selection of the list changse.
     */
    class ListSelectionHandler
            implements ListSelectionListener {

      public void valueChanged(ListSelectionEvent e) {

        ListToTreeSelectionModelWrapper.this
          .updateSelectedPathsFromSelectedRows();
      }
    }
  }
}
