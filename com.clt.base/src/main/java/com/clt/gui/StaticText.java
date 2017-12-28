package com.clt.gui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StaticText
    extends JTextPane {

  public static final int LEFT = StyleConstants.ALIGN_LEFT;
  public static final int RIGHT = StyleConstants.ALIGN_RIGHT;
  public static final int CENTER = StyleConstants.ALIGN_CENTER;
  public static final int JUSTIFIED = StyleConstants.ALIGN_JUSTIFIED;

  private int alignment;
  private int maxChars;


  public StaticText(String label) {

    this(label, Integer.MAX_VALUE);
  }


  /** */
  public StaticText(String label, int maxChars) {

    if (maxChars <= 0) {
      this.maxChars = Integer.MAX_VALUE;
    }
    else {
      this.maxChars = maxChars;
    }

    this.setAlignment(StaticText.LEFT);
    this.setOpaque(false);
    this.setEditable(false);
    this.setEnabled(false);
    this.setDisabledTextColor(Color.BLACK);
    this.setBorder(null);

    this.setText(label);
  }


  public StaticText(String[] labels) {

    this("");
    this.setTextLines(labels);
  }


  public StaticText(String[] labels, int maxChars) {

    this("", maxChars);
    this.setTextLines(labels);
  }


  public int getAlignment() {

    return this.alignment;
  }


  public void setAlignment(int alignment) {

    switch (alignment) {
      case LEFT:
      case RIGHT:
      case CENTER:
      case JUSTIFIED:
        this.alignment = alignment;
        break;
      default:
        throw new IllegalArgumentException("improper alignment: " + alignment);
    }

    StyledDocument doc = this.getStyledDocument();
    SimpleAttributeSet alignmentAttributes = new SimpleAttributeSet();
    StyleConstants.setAlignment(alignmentAttributes, alignment);
    doc.setParagraphAttributes(0, doc.getLength(), alignmentAttributes, false);
  }


  @Override
  public void setText(String label) {

    this.setTextLines(new String[] { label });
  }


  @Override
  public String getText() {

    return super.getText();
  }


  public void setTextLines(String[] labels) {

    StringBuilder b = new StringBuilder();
    if (labels != null) {
      for (int i = 0; i < labels.length; i++) {
        if (i > 0) {
          b.append("\n");
        }
        if (labels[i] != null) {
          if (labels[i].length() < this.maxChars) {
            b.append(labels[i]);
          }
          else {
            String line = labels[i];
            int start = 0;
            while (start < line.length()) {
              int end = Math.min(start + this.maxChars, line.length());
              if (end == line.length()) {
                b.append(line.substring(start));
              }
              else {
                int lineBreak = end;

                while ((lineBreak > start)
                                        && (" -".indexOf(line
                                          .charAt(lineBreak - 1)) < 0)) {
                  lineBreak--;
                }
                if (lineBreak == start) {
                  lineBreak = end;
                }
                else {
                  end = lineBreak;
                  while ((lineBreak > start)
                    && (line.charAt(lineBreak - 1) == ' ')) {
                    lineBreak--;
                  }
                }

                b.append(line.substring(start, lineBreak));
                b.append("\n");
              }
              start = end;
            }
          }
        }
      }
    }

    super.setText(b.toString());
  }


  public String[] getTextLines() {

    String text = this.getText();
    List<String> lines = new LinkedList<String>();
    try {
      String line;
      BufferedReader r = new BufferedReader(new StringReader(text));
      while ((line = r.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException shouldntHappen) {
      // StringReaders don't throw
    }
    return lines.toArray(new String[lines.size()]);
  }
}