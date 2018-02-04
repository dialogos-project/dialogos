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
import com.clt.gui.table.ChoiceEditor;
import com.clt.gui.table.ItemTableModel;
import com.clt.gui.table.TableEditDialog;
import com.clt.gui.table.TableEditor;
import com.clt.script.exp.Type;

public class VariableDialog extends TableEditor<Slot> {

    public VariableDialog(List<Slot> variables, List<GroovyVariable> groovyVariables, String title) {

        super(VariableDialog.createModel(variables, groovyVariables), title, false, null);
    }

    public static void showDialog(Component parent, List<Slot> variables, List<GroovyVariable> groovyVariables,
            String title) {

        new TableEditDialog<Slot>(parent, VariableDialog.createModel(variables, groovyVariables),
                title, null, false).setVisible(true);
    }

    private static ItemTableModel<Slot> createModel(List<Slot> variables, List<GroovyVariable> groovyVariables) {

        return new ItemTableModel<Slot>(variables) {

            @Override
            public int getColumnCount() {

                return 4;
            }

            @Override
            public String getColumnName(int columnIndex) {

                switch (columnIndex) {
                    case 0:
                        return Resources.getString("Name");
                    case 1:
                        return Resources.getString("Type");
                    case 2:
                        return Resources.getString("Value");
                    case 3:
                        return Resources.getString("Export");
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
             * Returns the column renderer of a column.
             */
            @Override
            public TableCellRenderer getColumnRenderer(int columnIndex) {

                switch (columnIndex) {
                    case 1:
                        return new ChoiceEditor(Slot.supportedTypes);
                    case 3:
                        return new CheckboxRenderer();
                    default:
                        return super.getColumnRenderer(columnIndex);
                }
            }

            @Override
            public TableCellEditor getColumnEditor(int columnIndex) {

                switch (columnIndex) {
                    case 1:
                        return new ChoiceEditor(Slot.supportedTypes);

                    default:
                        return super.getColumnEditor(columnIndex);
                }
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
                        return Type.class;
                    case 2:
                        return String.class;
                    case 3:
                        return Boolean.class;
                }
                return Object.class;
            }

            @Override
            public boolean isColumnDraggable(int columnIndex) {

                return (columnIndex == 0) || (columnIndex == 2);
            }

            @Override
            protected boolean readyToClose(Component parent) {
                if (conflictWithGroovyVars(parent)) {
                    // Global variables should not be able to have the same names as 
                    // groovy variables (no duplicates)
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

            protected boolean conflictWithGroovyVars(Component parent) {

                for (int i = 0; i < this.numItems(); i++) {
                    String name = this.getItem(i).getName();
                    for (int j = 0; j < groovyVariables.size(); j++) {
                        String name2 = groovyVariables.get(j).getName();
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
            protected Slot createNewItem(Component parent) {

                return new Slot();
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

            private boolean isUsed(Slot s) {

                return true;
            }

            @Override
            public Object getValue(Slot v, int columnIndex) {

                switch (columnIndex) {
                    case 0:
                        return v.getName();
                    case 1:
                        return v.getType();
                    case 2:
                        return v.getInitValue();
                    case 3:
                        return v._export ? Boolean.TRUE : Boolean.FALSE;
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
            public void setValue(Slot v, int columnIndex, Object aValue) {

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
                    case 1:
                        v.setType((Type) aValue);
                        break;
                    case 2:
                        v.setInitValue((String) aValue);
                        break;
                    case 3:
                        v._export = ((Boolean) aValue).booleanValue();
                        break;
                }
            }
        };
    }
}
