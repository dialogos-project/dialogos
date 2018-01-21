package com.clt.gui.table;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.clt.gui.Images;

public class IconHeaderRenderer extends JLabel implements TableCellRenderer {

    ImageIcon icon;

    TableCellRenderer defaultRenderer;

    String tooltip;

    public IconHeaderRenderer(String icon, String tooltip) {

        super(null, Images.load(icon), SwingConstants.CENTER);
        this.setToolTipText(tooltip);

        this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row, int column) {

        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                this.setForeground(header.getForeground());
                this.setBackground(header.getBackground());
            }
        }

        return this;
    }

    public void dispose() {

        if (this.icon != null) {
            this.icon.getImage().flush();
        }
    }
}
