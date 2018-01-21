package com.clt.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.clt.gui.table.SortableTableModel.SortMode;

/**
 * @author dabo
 */
public class SortableTableHeaderRenderer implements TableCellRenderer {

    private TableCellRenderer defaultRenderer;
    private SortableTableModel model;
    private Icon arrow;
    private Icon arrow_rev;
    private Icon empty;

    private Color defaultBackground;

    public SortableTableHeaderRenderer(JTableHeader header, SortableTableModel model) {

        this.model = model;
        this.defaultRenderer = header.getDefaultRenderer();
        this.arrow
                = new ImageIcon(ClassLoader
                        .getSystemResource("com/clt/resources/ArrowUp.png"));
        this.arrow_rev = new ImageIcon(
                ClassLoader.getSystemResource("com/clt/resources/ArrowDown.png"));

        this.empty = new Icon() {

            public void paintIcon(Component c, Graphics g, int x, int y) {

                // an empty icon has nothing to paint
            }

            public int getIconWidth() {

                return SortableTableHeaderRenderer.this.arrow.getIconWidth();
            }

            public int getIconHeight() {

                return SortableTableHeaderRenderer.this.arrow.getIconHeight();
            }

        };

        if (this.defaultRenderer instanceof Component) {
            this.defaultBackground
                    = ((Component) this.defaultRenderer).getBackground();
        }

        if (this.defaultBackground == null) {
            this.defaultBackground = new JLabel().getBackground();
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row, int column) {

        boolean selected
                = isSelected
                || ((value != null) && (this.model.getSortMode(column) != SortMode.NONE));
        Component c
                = this.defaultRenderer.getTableCellRendererComponent(table, value,
                        isSelected,
                        hasFocus, row, column);
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;

            this.setSelected(jc, selected);
            this.setSelected(jc.getBorder(), selected);
            // jc.setBackground(selected ? defaultBackground.darker() :
            // defaultBackground);

            if (jc instanceof JLabel) {
                JLabel l = (JLabel) jc;
                if (this.model.getSortMode(column) == SortMode.ASCENDING) {
                    l.setIcon(this.arrow);
                } else if (this.model.getSortMode(column) == SortMode.DESCENDING) {
                    l.setIcon(this.arrow_rev);
                } else {
                    l.setIcon(this.empty);
                }
                l.setHorizontalTextPosition(SwingConstants.LEADING);
            }
        }
        return c;
    }

    // We dont know what kind of object/component we will get.
    // So we look for a method setSelected(boolean) in the object and call that.
    private void setSelected(Object o, boolean selected) {

        try {
            o.getClass().getMethod("setSelected", new Class[]{Boolean.TYPE})
                    .invoke(o,
                            new Object[]{selected ? Boolean.TRUE : Boolean.FALSE});
        } catch (Exception ignore) {
            // The object couldn't be selected.
        }
    }
}
