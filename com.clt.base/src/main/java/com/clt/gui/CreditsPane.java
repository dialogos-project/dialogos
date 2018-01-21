package com.clt.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.clt.util.StringTools;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class CreditsPane extends JTextPane {
    public static final int ALIGN_LEFT = StyleConstants.ALIGN_LEFT;
    public static final int ALIGN_RIGHT = StyleConstants.ALIGN_RIGHT;
    public static final int ALIGN_CENTER = StyleConstants.ALIGN_CENTER;
    public static final int ALIGN_JUSTIFIED = StyleConstants.ALIGN_JUSTIFIED;

    private int alignment = CreditsPane.ALIGN_RIGHT;

    public CreditsPane() {

        this(null, null);
    }

    public CreditsPane(Color foreground, Color background) {

        if (foreground != null) {
            this.setForeground(foreground);
        }
        this.setBackground(background);

        this.setEditable(false);
    }

    @Override
    public void setBackground(Color c) {

        if (c != null) {
            super.setBackground(c);
        }
        this.setOpaque(c != null);
    }

    public void setHorizontalAlignment(int align) {

        if ((align != CreditsPane.ALIGN_LEFT)
                && (align != CreditsPane.ALIGN_CENTER)
                && (align != CreditsPane.ALIGN_RIGHT)
                && (align != CreditsPane.ALIGN_JUSTIFIED)) {
            throw new IllegalArgumentException("Illegal horizontal alignment: " + align);
        }
        this.alignment = align;
    }

    public synchronized void setText(String[] text) {

        this.setText(text, GUI.getSmallSystemFont());
    }

    public synchronized void setText(String[] text, Font font) {

        Font[] fonts = new Font[text.length];
        for (int i = 0; i < text.length; i++) {
            fonts[i] = font;
        }
        this.setText(text, fonts);
    }

    public synchronized void setText(String[] text, String fontname, int size,
            int styles[]) {

        Font[] fonts = new Font[text.length];
        for (int i = 0; i < text.length; i++) {
            fonts[i] = new Font(fontname, styles[i], size);
        }
        this.setText(text, fonts);
    }

    public synchronized void setText(String[] text, Font[] fonts) {

        StyledDocument d = this.getStyledDocument();
        MutableAttributeSet p_attr = new SimpleAttributeSet();

        StyleConstants.setAlignment(p_attr, this.alignment);
        StyleConstants.setForeground(p_attr, this.getForeground());

        d.setParagraphAttributes(0, d.getLength(), p_attr, false);

        MutableAttributeSet attr = new SimpleAttributeSet(p_attr);

        for (int i = 0; i < text.length; i++) {
            if (fonts[i] != null) {
                StyleConstants.setFontFamily(attr, fonts[i].getFamily());
                StyleConstants.setFontSize(attr, fonts[i].getSize());
                StyleConstants.setBold(attr, fonts[i].isBold());
                StyleConstants.setItalic(attr, fonts[i].isItalic());
            }

            try {
                if (text[i] != null) {
                    d.insertString(d.getLength(), text[i], attr);
                }
                d.insertString(d.getLength(), "\n", attr);
            } catch (BadLocationException exn) {
                // can't happen since we are simply appending to the end
            }
        }
    }

    @Override
    public synchronized void setText(String text) {

        this.setText(text, GUI.getSmallSystemFont());
    }

    public synchronized void setText(String text, Font font) {

        String[] s = text == null ? new String[0] : StringTools.split(text, '\n');
        Font[] fonts = new Font[s.length];

        for (int i = 0; i < s.length; i++) {
            if (s[i].startsWith("<")) {
                int attributes = Font.PLAIN;
                int size = font.getSize();
                int j = 1;
                while (s[i].charAt(j) != '>') {
                    switch (s[i].charAt(j)) {
                        case 'b':
                        case 'B':
                            attributes |= Font.BOLD;
                            break;
                        case 'i':
                        case 'I':
                            attributes |= Font.ITALIC;
                            break;
                        case 's':
                        case 'S':
                            boolean neg;
                            if (s[i].charAt(j + 1) == '-') {
                                neg = true;
                                j++;
                            } else if (s[i].charAt(j + 1) == '+') {
                                neg = false;
                                j++;
                            } else {
                                neg = false;
                                size = 0;
                            }
                            int n = 0;
                            while (Character.isDigit(s[i].charAt(j + 1))) {
                                n *= 10;
                                n += Character.digit(s[i].charAt(j + 1), 10);
                                j++;
                            }
                            if (neg) {
                                size -= n;
                            } else {
                                size += n;
                            }
                            break;
                    }
                    j++;
                }
                s[i] = s[i].substring(j + 1);
                fonts[i] = new Font(font.getName(), attributes, size);
            } else {
                s[i] = s[i];
                fonts[i] = font;
            }
        }

        this.setText(s, fonts);
    }

}
