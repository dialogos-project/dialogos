package com.clt.diamant.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.clt.diamant.GroovyVariable;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.gui.OptionPane;
import com.clt.gui.table.CheckboxRenderer;
import com.clt.gui.table.ItemTableModel;
import com.clt.gui.table.TableEditDialog;
import com.clt.gui.table.TableEditor;

public class GroovyVariableDialog extends TableEditor<GroovyVariable> {

    public GroovyVariableDialog(List<GroovyVariable> groovyVariables, String title, List<Slot> globalVars) {

        super(GroovyVariableDialog.createModel(groovyVariables, globalVars), title, false, null);
    }

    public static void showDialog(Component parent, List<GroovyVariable> variables,
            String title, List<Slot> globalVars) {

        new TableEditDialog<GroovyVariable>(parent, GroovyVariableDialog.createModel(variables, globalVars),
                title, null, false).setVisible(true);
    }

    private static ItemTableModel<GroovyVariable> createModel(List<GroovyVariable> groovyVariables, List<Slot> globalVars) {

        return new ItemTableModel<GroovyVariable>(groovyVariables) {

            @Override
            public int getColumnCount() {

                return 3;
            }

            @Override
            public String getColumnName(int columnIndex) {

                switch (columnIndex) {
                    case 0:
                        return Resources.getString("Name");
                    case 1:
                        return Resources.getString("Type");
                    case 2:
                        return Resources.getString("Export");
                }
                return null;
            }

            @Override
            public int getColumnWidth(int columnIndex) {

                switch (columnIndex) {
                    case 0:
                        return 120;
                    case 1:
                        return 100;
                    case 2:
                        return 80;
                    default:
                        return super.getColumnWidth(columnIndex);
                }
            }

            /**
             * Returns the column renderer of a column.
             */
            @Override
            public TableCellRenderer getColumnRenderer(int columnIndex) {

                switch (columnIndex) {
                    case 2:
                        return new CheckboxRenderer();
                    default:
                        return super.getColumnRenderer(columnIndex);
                }
            }

            @Override
            public TableCellEditor getColumnEditor(int columnIndex) {
                return super.getColumnEditor(columnIndex);
            }

            /**
             * Returns the lowest common denominator Class in the column. This
             * is used by the table to set up a default renderer and editor for
             * the column.
             *
             * @return the common ancestor class of the object values in the
             * model.
             */
            @Override
            public Class<?> getColumnClass(int columnIndex) {

                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Boolean.class;
                }
                return Object.class;
            }

            @Override
            public boolean isColumnDraggable(int columnIndex) {

                return (columnIndex == 0) || (columnIndex == 2);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 1) {
                    return false;
                }
                return true;
            }

            @Override
            protected boolean readyToClose(Component parent) {
                if (conflictWithGlobalVars(parent)) {
                    return false;
                }
                for (int i = 0; i < this.numItems() - 1; i++) {
                    String name = this.getItem(i).getName();
                    for (int j = i + 1; j < this.numItems(); j++) {
                        String name2 = this.getItem(j).getName();
                        if (name.equals(name2)) {
                            OptionPane.error(parent, Resources.format(
                                    "DuplicateVariableDefinition", name));
                            return false;
                        }
                    }
                }

                return true;
            }

            protected boolean conflictWithGlobalVars(Component parent) {

                for (int i = 0; i < this.numItems(); i++) {
                    String name = this.getItem(i).getName();
                    for (int j = 0; j < globalVars.size(); j++) {
                        String name2 = globalVars.get(j).getName();
                        if (name.equals(name2)) {
                            OptionPane.error(parent, Resources.format(
                                    "DuplicateVariableDefinition", name));
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            protected GroovyVariable createNewItem(Component parent) {

                return new GroovyVariable();
            }

            @Override
            protected boolean confirmDelete(Component parent, int[] indices) {

                StringBuilder b = new StringBuilder();
                for (int i = 0; i < indices.length; i++) {
                    if (this.isUsed(this.getItem(indices[i]))) {
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(this.getItem(indices[i]).getName());
                    }
                }

                if (b.length() > 0) {
                    int result = OptionPane.confirm(
                            parent,
                            new String[]{
                                // "You are about to delete variables that are still accessed from variable nodes: "
                                // + b.toString() + ". " +
                                Resources.format("ReallyDeleteVariable")},
                            "Delete variables?", OptionPane.OK_CANCEL_OPTION);
                    return result == OptionPane.OK;
                } else {
                    return true;
                }
            }

            private boolean isUsed(GroovyVariable entry) {

                return true;
            }

            @Override
            public Object getValue(GroovyVariable v, int columnIndex) {

                switch (columnIndex) {
                    case 0:
                        return v.getName();
                    case 1:
                        return v.getType();
                    case 2:
                        return v.isExport() ? Boolean.TRUE : Boolean.FALSE;
                }
                return null;
            }

            /**
             * Sets an attribute for the slot at <I>columnIndex</I>.
             *
             * @param aValue the new value
             * @param v the slot whose attribute is to be changed
             * @param columnIndex the column whose value is to be changed
             * @see #getValue
             */
            @Override
            public void setValue(GroovyVariable v, int columnIndex, Object aValue) {

                switch (columnIndex) {
                    case 0:
                        // assert that we have a legal identifier name
                        String name = (String) aValue;
                        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
                            Toolkit.getDefaultToolkit().beep();
                            return;
                        }
                        for (int i = 1; i < name.length(); i++) {
                            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                                Toolkit.getDefaultToolkit().beep();
                                return;
                            }
                        }
                        v.setName((String) aValue);
                        break;
                    case 2:
                        v.setExport((Boolean) aValue);
                        break;
                }
            }
        };
    }
}
