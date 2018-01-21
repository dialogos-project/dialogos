package com.clt.properties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.gui.ColorIcon;
import com.clt.gui.GUI;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.gui.menus.MenuCommander;
import com.clt.util.StringTools;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class ColorProperty        extends Property<Color> {

    public ColorProperty(String id) {

        super(id, Property.EDIT_TYPE_COLOR_CHOOSER);
    }

    public abstract Color getValue();

    protected void setValueImpl(Color value) {

        // the default implementation does nothing
    }

    @Override
    public final void setValue(Color value) {

        if (value != this.getValue()) {
            this.setValueImpl(value);
            this.fireChange();
        }
    }

    @Override
    public String getValueAsString() {

        Color c = this.getValue();
        if (c == null) {
            return null;
        } else {
            return StringTools.toHexString(c);
        }
    }

    @Override
    public Color getValueAsObject() {

        return this.getValue();
    }

    @Override
    public void setValueFromString(String s)
            throws java.text.ParseException {

        try {
            this.setValue(Color.decode(s));
        } catch (NumberFormatException exn) {
            throw new java.text.ParseException(exn.getLocalizedMessage(), 0);
        }
    }

    @Override
    public Color[] getPossibleValues() {

        return null;
    }

    @Override
    protected int getSupportedEditTypesImpl() {

        return Property.EDIT_TYPE_COLOR_CHOOSER;
    }

    @Override
    protected JComponent createEditorComponent(int editType, boolean label) {

        switch (editType) {
            case EDIT_TYPE_COLOR_CHOOSER:
                return this.createColorChooser(label);
            default:
                return super.createEditorComponent(editType, label);
        }
    }

    protected JComponent createColorChooser(boolean label) {

        final JComponent chooser = new JComponent() {

            @Override
            public Dimension getMinimumSize() {

                return this.getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {

                return new Dimension(30, 16);
            }

            @Override
            public boolean isOpaque() {

                return true;
            }

            @Override
            public void paintComponent(Graphics g) {

                g.setColor(ColorProperty.this.getValueAsObject());
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(this.isEnabled() ? Color.black : Color.lightGray);
                g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            }

            ChangeListener l = new ChangeListener() {

                public void stateChanged(ChangeEvent evt) {

                    setEnabled(ColorProperty.this.isEditable());
                    repaint();
                }
            };

            MouseListener al = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    if (isEnabled()) {
                        showChooser();
                    }
                }
            };

            private void showChooser() {

                Color c = JColorChooser.showDialog(this, GUI.getString("ChooseColor"),
                        ColorProperty.this.getValueAsObject());
                if (c != null) {
                    ColorProperty.this.setValue(c);
                }
            }

            @Override
            public void addNotify() {

                super.addNotify();
                ColorProperty.this.addChangeListener(this.l);
                this.l.stateChanged(new ChangeEvent(ColorProperty.this));
                this.addMouseListener(this.al);
            }

            @Override
            public void removeNotify() {

                this.removeMouseListener(this.al);
                ColorProperty.this.removeChangeListener(this.l);
                super.removeNotify();
            }

        };

        if (label) {
            JPanel p = new JPanel(new GridBagLayout()) {

                @Override
                public void setEnabled(boolean enabled) {

                    super.setEnabled(enabled);
                    for (int i = 0; i < this.getComponentCount(); i++) {
                        this.getComponent(i).setEnabled(enabled);
                    }
                }
            };
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            JLabel l = new JLabel(this.getName());
            l.setLabelFor(chooser);
            p.add(l, gbc);
            gbc.gridx++;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 6, 0, 0);
            p.add(chooser, gbc);
            return p;
        } else {
            return chooser;
        }
    }

    @Override
    public JMenuItem createMenuItem() {

        CmdMenuItem item
                = new CmdMenuItem(this.getName(), 1, null, new MenuCommander() {

                    public String menuItemName(int cmd, String oldName) {

                        return ColorProperty.this.getName();
                    }

                    public boolean menuItemState(int cmd) {

                        return ColorProperty.this.isEditable();
                    }

                    public boolean doCommand(int cmd) {

                        Color color
                                = JColorChooser.showDialog(null, ColorProperty.this.getName(),
                                        ColorProperty.this.getValue());
                        if (color != null) {
                            ColorProperty.this.setValue(color);
                        }

                        return true;
                    }

                });
        item.setIcon(new ColorIcon(ColorProperty.this.getValue(), 16, 12) {

            @Override
            public Color getColor() {

                return ColorProperty.this.getValue();
            }
        });
        return item;
    }
}
