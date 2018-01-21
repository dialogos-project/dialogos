package com.clt.gui;

import java.awt.Color;
import java.io.Writer;
import java.util.LinkedList;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class LoggingPane extends JTextPane {

    private int bufferSize;

    private LinkedList<Position> lines;

    private MutableAttributeSet style = new SimpleAttributeSet();

    public LoggingPane() {

        this(200);
    }

    public LoggingPane(int bufferSize) {

        super(new DefaultStyledDocument());
        this.setEditable(false);

        this.lines = new LinkedList<Position>();
        this.bufferSize = bufferSize;
        // setFont(new Font("Serif", Font.PLAIN, 12));
    }

    public void setFont(String name, int size) {

        StyleConstants.setFontFamily(this.style, name);
        StyleConstants.setFontSize(this.style, size);
    }

    private void newline() {

        try {
            Document d = this.getDocument();

            while ((this.bufferSize > 0) && (this.lines.size() >= this.bufferSize)) {
                Position pos = this.lines.removeFirst();

                d.remove(0, pos.getOffset() + 1);
            }

            int end = d.getLength();
            d.insertString(end, "\n", null);
            this.lines.add(d.createPosition(end));
        } catch (BadLocationException ignore) {
            // shouldn't happen
        }
    }

    public synchronized void setBold(boolean bold) {

        StyleConstants.setBold(this.style, bold);
    }

    public synchronized void print(String s) {

        this.print(s, Color.black);
    }

    public synchronized void print(String s, Color c) {

        try {
            Document d = this.getDocument();

            MutableAttributeSet attr = new SimpleAttributeSet();
            attr.addAttributes(this.style);
            StyleConstants.setForeground(attr, c);

            int nl = s.indexOf('\n');
            while (nl != -1) {
                d.insertString(d.getLength(), s.substring(0, nl), attr);
                this.newline();
                s = s.substring(nl + 1);
                nl = s.indexOf('\n');
            }
            d.insertString(d.getLength(), s, attr);

            this.setCaretPosition(d.getLength());
        } catch (BadLocationException ignore) {
            // shouldn't happen
        }
    }

    public synchronized void println() {

        this.newline();
    }

    public synchronized void println(String s) {

        this.print(s);
        this.newline();
    }

    public synchronized void println(String s, Color c) {

        this.print(s, c);
        this.newline();
    }

    public synchronized void clear() {

        try {
            Document d = this.getDocument();
            d.remove(0, d.getLength());
            this.lines.clear();

            this.setBold(false);
        } catch (BadLocationException ignore) {
            // shouldn't happen
        }
    }

    public void flush() {

        this.paintImmediately(0, 0, this.getWidth(), this.getHeight());
    }

    public Writer getWriter() {

        return new Writer() {

            @Override
            public void write(char[] data, int offset, int length) {

                LoggingPane.this.print(new String(data, offset, length));
            }

            @Override
            public void close() {

                // there is nothing to do
            }

            @Override
            public void flush() {

                LoggingPane.this.flush();
            }
        };
    }

}
