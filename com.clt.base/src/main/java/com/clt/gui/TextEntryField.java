package com.clt.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class TextEntryField extends JTextField {

  String description = null;


  public TextEntryField(String description) {

    this(description, null, 0);
  }


  public TextEntryField(String description, String text) {

    this(description, text, 0);
  }


  public TextEntryField(String description, int columns) {

    this(description, null, columns);
  }


  public TextEntryField(String description, String text, int columns) {

    super(text, columns);

    this.addFocusListener(new FocusListener() {

      public void focusGained(FocusEvent e) {

        TextEntryField.this.repaint();
      }


      public void focusLost(FocusEvent e) {

        TextEntryField.this.repaint();
      }
    });

    this.setDescription(description);
  }


  public String getDescription() {

    return this.description;
  }


  public void setDescription(String description) {

    this.description = description;
    this.repaint();
  }


  @Override
  protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    if ((this.getDocument().getLength() == 0) && this.isEnabled()
      && !this.hasFocus() && (this.description != null)) {
      FontMetrics fm = g.getFontMetrics(this.getFont());
      javax.swing.plaf.TextUI ui = this.getUI();
      int x = 0;
      int baseline = 16;
      try {
        Rectangle r = ui.modelToView(this, 0);
        x = r.x;
        baseline = r.y + fm.getAscent();
      } catch (BadLocationException exn) {
        System.out.println("Ouch");
      }
      g.setColor(Color.lightGray);
      g.drawString(this.description, x, baseline);
    }
  }
}