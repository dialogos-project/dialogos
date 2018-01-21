package com.clt.properties;

import javax.swing.JMenuItem;

import com.clt.gui.OptionPane;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.gui.menus.MenuCommander;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class StringProperty extends Property<String> {

    public StringProperty(String id) {

        super(id, Property.EDIT_TYPE_TEXTFIELD);
    }

    public abstract String getValue();

    protected void setValueImpl(String value) {

        // the default implementation does nothing
    }

    @Override
    public void setValue(String value) {

        this.setValueFromString(value);
    }

    @Override
    public final void setValueFromString(String value) {

        if (value != this.getValue()) {
            this.setValueImpl(value);
            this.fireChange();
        }
    }

    @Override
    public String getValueAsString() {

        return this.getValue();
    }

    @Override
    public String getValueAsObject() {

        return this.getValue();
    }

    @Override
    public String[] getPossibleValues() {

        return null;
    }

    @Override
    protected int getSupportedEditTypesImpl() {

        return Property.EDIT_TYPE_TEXTFIELD;
    }

    @Override
    public JMenuItem createMenuItem() {

        return new CmdMenuItem(this.getName(), 1, null, new MenuCommander() {

            public String menuItemName(int cmd, String oldName) {

                return StringProperty.this.getName();
            }

            public boolean menuItemState(int cmd) {

                return StringProperty.this.isEditable();
            }

            public boolean doCommand(int cmd) {

                String newValue
                        = OptionPane.edit(null, StringProperty.this.getName() + ":",
                                StringProperty.this.getName(), StringProperty.this
                                .getValue());
                if (newValue != null) {
                    StringProperty.this.setValue(newValue);
                }

                return true;
            }
        });
    }
}
