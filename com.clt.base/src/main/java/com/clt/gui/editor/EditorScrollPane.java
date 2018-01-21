package com.clt.gui.editor;

import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import com.clt.gui.border.LinesBorder;

/**
 * @author dabo
 *
 */
public class EditorScrollPane extends JScrollPane {

    private JTextComponent text;
    private boolean showLineNumbers = true;

    public EditorScrollPane(JTextComponent text) {
        super(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.text = text;
        this.init();
    }

    private void init() {

        if (this.showLineNumbers) {
            LineNumbers ln = new LineNumbers(this.text);
            ln.setForeground(Color.DARK_GRAY);
            ln.setBorder(new LinesBorder("r", Color.LIGHT_GRAY));
            this.setRowHeaderView(ln);
        } else {
            this.setRowHeader(null);
        }
    }

    public boolean getShowLineNumbers() {

        return this.showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {

        if (this.showLineNumbers != showLineNumbers) {
            this.showLineNumbers = showLineNumbers;
            this.init();
        }
    }
}
