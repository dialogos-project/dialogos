package com.clt.gui.editor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import com.clt.event.DocumentChangeListener;
import com.clt.gui.GUI;

/**
 * @author dabo
 *
 */
public class LineNumbers extends JComponent {

    private JTextComponent text;
    private DocumentChangeListener repainter = new DocumentChangeListener() {

        @Override
        public void documentChanged(DocumentEvent evt) {

            LineNumbers.this.repaint();
        }
    };

    public LineNumbers(JTextComponent text) {

        this.text = text;

        this.setOpaque(false);
    }

    @Override
    public void addNotify() {

        super.addNotify();

        GUI.addDocumentChangeListener(this.text, this.repainter);
    }

    @Override
    public void removeNotify() {

        this.text.getDocument().removeDocumentListener(this.repainter);

        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {

        int width = this.getWidth();
        Font font = this.text.getFont();
        FontMetrics metrics = g.getFontMetrics(font);
        g.setFont(font);
        g.setColor(this.getForeground());

        String text = this.text.getText();
        boolean draw = true;
        int line = 1;
        for (int i = 0; i <= text.length(); i++) {
            if (draw) {
                try {
                    Rectangle r = this.text.modelToView(i);
                    String lineNumber = String.valueOf(line);
                    g.drawString(lineNumber, width - metrics.stringWidth(lineNumber) - 6,
                            r.y
                            + metrics.getHeight() - metrics.getDescent());
                } catch (BadLocationException exn) {
                    System.err.println("Error for pos " + i + ": "
                            + exn.getLocalizedMessage());
                    System.err.println("  Character at "
                            + i
                            + ": "
                            + (i < text.length() ? "0x"
                            + (text.charAt(i) < 0x10 ? "0" : "")
                            + Integer.toHexString(text.charAt(i))
                            : "EOF"));
                }
                draw = false;
            }

            if (i < text.length()) {
                if (text.charAt(i) == '\r') {
                    if ((i < text.length() - 1) && (text.charAt(i + 1) == '\n')) {
                        i++;
                    }
                    line++;
                    draw = true;
                } else if (text.charAt(i) == '\n') {
                    line++;
                    draw = true;
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {

        Dimension d = this.text.getPreferredSize();
        return new Dimension(40, d.height);
    }
}
