//
//  TristateCheckBox.java
//  Base
//
//  Created by Daniel Bobbert on Fri May 21 2004.
//  Copyright (c) 2004 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.gui;

import java.awt.Graphics;

import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;

public class TristateCheckBox
    extends JCheckBox {

  public static final int SELECTED = 0;
  public static final int DESELECTED = 1;
  public static final int BOTH = 2;

  private JCheckBox tristateComponent;


  /**
   * Create a new TristateCheckBox.
   */
  public TristateCheckBox() {

    this(null, TristateCheckBox.DESELECTED);
  }


  /**
   * Create a new TristateCheckBox.
   */
  public TristateCheckBox(String text) {

    this(text, TristateCheckBox.DESELECTED);
  }


  /**
   * Create a new TristateCheckBox.
   */
  public TristateCheckBox(String text, int state) {

    super(text, null, false);

    super.setModel(new DefaultTristateButtonModel(this.getModel()));

    this.tristateComponent = new JCheckBox();

    this.setState(state);
  }


  public void setState(int state) {

    ((TristateButtonModel)this.getModel()).setState(state);
  }


  public int getState() {

    return ((TristateButtonModel)this.getModel()).getState();
  }


  /**
   * Override paint() to paint the tristate checkbox differently if it's in
   * tristate.
   */
  @Override
  public void paint(Graphics g) {

    TristateButtonModel model = (TristateButtonModel)this.getModel();
    if (model.getState() == TristateCheckBox.BOTH) {
      this.tristateComponent.setBackground(this.getBackground());
      this.tristateComponent.setForeground(this.getForeground());
      this.tristateComponent.setFont(this.getFont());
      this.tristateComponent.setText(this.getText());
      this.tristateComponent.setIcon(this.getIcon());
      this.tristateComponent.setEnabled(this.isEnabled());
      this.tristateComponent.setBorder(this.getBorder());
      this.tristateComponent.setBounds(this.getBounds());

      this.tristateComponent.setBorderPainted(this.isBorderPainted());
      // tristateComponent.setBorderPaintedFlat(isBorderPaintedFlat());
      this.tristateComponent.setContentAreaFilled(this.isContentAreaFilled());
      this.tristateComponent.setHorizontalAlignment(this
        .getHorizontalAlignment());
      this.tristateComponent.setHorizontalTextPosition(this
        .getHorizontalTextPosition());
      this.tristateComponent.setFocusPainted(this.isFocusPainted());
      this.tristateComponent.setMargin(this.getMargin());
      this.tristateComponent.setRolloverEnabled(this.isRolloverEnabled());
      this.tristateComponent.setVerticalAlignment(this.getVerticalAlignment());
      this.tristateComponent.setVerticalTextPosition(this
        .getVerticalTextPosition());

      ButtonModel componentModel = this.tristateComponent.getModel();
      componentModel.setEnabled(model.isEnabled());
      componentModel.setSelected(model.isArmed());
      componentModel.setArmed(true);
      componentModel.setPressed(true);
      componentModel.setRollover(model.isRollover());

      this.tristateComponent.paint(g);
    }
    else {
      super.paint(g);
    }
  }

  private interface TristateButtonModel
        extends ButtonModel {

    public int getState();


    public void setState(int state);
  }

  private static class DefaultTristateButtonModel
        extends JToggleButton.ToggleButtonModel
        implements TristateButtonModel {

    int state = TristateCheckBox.BOTH;


    public DefaultTristateButtonModel(ButtonModel model) {

      super();

      this.setActionCommand(model.getActionCommand());
      this.setMnemonic(model.getMnemonic());
      this.setArmed(model.isArmed());
      this.setEnabled(model.isEnabled());
      this.setPressed(model.isPressed());
      this.setRollover(model.isRollover());
      this.setSelected(this.isSelected());
    }


    public int getState() {

      return this.state;
    }


    public void setState(int state) {

      if (state != this.state) {
        if (state == TristateCheckBox.SELECTED) {
          this.setSelected(true);
        }
        else if (state == TristateCheckBox.DESELECTED) {
          this.setSelected(false);
        }
        else {
          this.state = state;

          if (this.isSelected()) {
            super.setSelected(false);
          }
          else {
            this.fireStateChanged();
          }
        }
      }
    }


    @Override
    public void setSelected(boolean selected) {

      if ((selected != this.isSelected())
        || (this.state == TristateCheckBox.BOTH)) {
        this.state =
          selected ? TristateCheckBox.SELECTED : TristateCheckBox.DESELECTED;

        if (this.isSelected() != selected) {
          super.setSelected(selected);
        }
        else {
          this.fireStateChanged();
        }
      }
    }
  }
}
