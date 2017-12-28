package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;

import com.clt.diamant.Resources;
import com.clt.event.DocumentChangeListener;
import com.clt.gui.CmdButton;
import com.clt.gui.Commander;
import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.WindowUtils;
import com.clt.gui.border.GroupBorder;

public class CanvasSizeDialog extends JDialog implements Commander {

  Dimension size;
  int anchor;

  JTextField width, height;
  AnchorChooser anchor_chooser;


  public CanvasSizeDialog(Component parent, Dimension s, boolean readOnly) {

    super(GUI.getFrameForComponent(parent), Resources.getString("CanvasSize"),
      true);
    //this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    this.setResizable(false);

    this.size = s;

    Container c = this.getContentPane();
    if (!(c instanceof JPanel)) {
      c = new JPanel();
    }
    JPanel contents = (JPanel)c;
    contents.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    contents.setLayout(new BorderLayout());

    JPanel oldSize = new JPanel(new GridBagLayout());
    oldSize.setBorder(new GroupBorder(Resources.getString("OldSize")));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 5, 2, 5);
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;

    gbc.gridx = 0;
    gbc.weightx = 0.0;
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridy = 0;
    oldSize.add(new JLabel(Resources.getString("Width") + ':'), gbc);
    gbc.gridy++;
    oldSize.add(new JLabel(Resources.getString("Height") + ':'), gbc);

    gbc.gridx++;
    gbc.weightx = 1.0;
    gbc.insets = new Insets(2, 18, 2, 5);
    gbc.gridy = 0;
    oldSize.add(new JLabel(Integer.toString(this.size.width)), gbc);
    gbc.gridy++;
    oldSize.add(new JLabel(Integer.toString(this.size.height)), gbc);

    JPanel newSize = new JPanel(new GridBagLayout());
    newSize.setBorder(new GroupBorder(Resources.getString("NewSize")));

    gbc.gridx = 0;
    gbc.weightx = 0.0;
    gbc.insets = new Insets(2, 5, 2, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridy = 0;
    newSize.add(new JLabel(Resources.getString("Width") + ':'), gbc);
    gbc.gridy++;
    newSize.add(new JLabel(Resources.getString("Height") + ':'), gbc);
    gbc.gridy++;

    GridBagConstraints gbc2 = (GridBagConstraints)gbc.clone();
    gbc2.fill = GridBagConstraints.NONE;
    gbc2.anchor = GridBagConstraints.NORTHWEST;
    gbc2.insets = new Insets(6, 5, 2, 5);
    newSize.add(new JLabel(Resources.getString("Position") + ':'), gbc2);

    ActionListener al = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        CanvasSizeDialog.this.getRootPane().getDefaultButton().doClick();
      }
    };

    gbc.gridx++;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    this.width = new JTextField(5);
    this.width.addActionListener(al);
    this.height = new JTextField(5);
    this.height.addActionListener(al);
    newSize.add(this.width, gbc);
    gbc.gridy++;
    newSize.add(this.height, gbc);
    gbc.gridy++;
    gbc.insets = new Insets(4, 5, 2, 5);
    this.anchor_chooser = new AnchorChooser();
    newSize.add(this.anchor_chooser, gbc);

    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowOpened(WindowEvent evt) {

        CanvasSizeDialog.this.width.requestFocus();
        CanvasSizeDialog.this.width.selectAll();
      }
    });
    this.width.addFocusListener(new FocusAdapter() {

      @Override
      public void focusGained(FocusEvent evt) {

        CanvasSizeDialog.this.width.selectAll();
      }
    });
    this.height.addFocusListener(new FocusAdapter() {

      @Override
      public void focusGained(FocusEvent evt) {

        CanvasSizeDialog.this.height.selectAll();
      }
    });

    DocumentChangeListener dl = new DocumentChangeListener() {

      @Override
      public void documentChanged(DocumentEvent evt) {

        try {
          int h_diff =
            Integer.parseInt(CanvasSizeDialog.this.width.getText())
              - CanvasSizeDialog.this.size.width;
          int v_diff =
            Integer.parseInt(CanvasSizeDialog.this.height.getText())
              - CanvasSizeDialog.this.size.height;
          CanvasSizeDialog.this.anchor_chooser.setShrink(h_diff, v_diff);
        }
            catch (Exception ignore) {
            }
          }
    };
    GUI.addDocumentChangeListener(this.width, dl);
    GUI.addDocumentChangeListener(this.height, dl);

    this.width.setText(Integer.toString(this.size.width));
    this.height.setText(Integer.toString(this.size.height));

    // JPanel left = new JPanel(new GridLayout(2, 1, 5, 5));
    JPanel left = new JPanel(new GridBagLayout());

    gbc.gridx = 0;
    gbc.gridy = 0;
    left.add(oldSize, gbc);
    gbc.gridy++;
    left.add(newSize, gbc);

    contents.add(left, BorderLayout.WEST);

    JPanel right = new JPanel(new GridBagLayout());

    CmdButton okButton =
      new CmdButton(this, Commands.cmdOK, Resources.getString("OK"));
    CmdButton cancelButton =
      new CmdButton(this, Commands.cmdCancel, Resources.getString("Cancel"));

    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.gridy = 0;
    gbc.weighty = 0.0;
    right.add(Box.createVerticalStrut(2), gbc);
    gbc.gridy++;
    right.add(okButton, gbc);
    gbc.gridy++;
    right.add(cancelButton, gbc);
    gbc.gridy++;
    gbc.weighty = 1.0;
    right.add(Box.createVerticalGlue(), gbc);

    contents.add(right, BorderLayout.EAST);

    GUI.setDefaultButtons(this, okButton, cancelButton);

    if (readOnly) {
      // okButton.setEnabled(false);

      for (int i = 0; i < newSize.getComponentCount(); i++) {
        Component child = newSize.getComponent(i);
        if (!(child instanceof JLabel)) {
          child.setEnabled(false);
        }
      }
      cancelButton.setVisible(false);
    }

    this.pack();

    WindowUtils.setLocationRelativeTo(this, GUI.getWindowForComponent(parent));
  }


  public boolean doCommand(int cmd) {

    switch (cmd) {
      case cmdOK:
        int x = 0,
        y = 0;
        try {
          y = Integer.parseInt(this.height.getText());
          if (y <= 0) {
            throw new NumberFormatException();
          }
        } catch (NumberFormatException exn) {
          this.height.selectAll();
          this.height.requestFocus();
        }
        try {
          x = Integer.parseInt(this.width.getText());
          if (x <= 0) {
            throw new NumberFormatException();
          }
        } catch (NumberFormatException exn) {
          this.width.selectAll();
          this.width.requestFocus();
        }
        if ((x > 0) && (y > 0)) {
          this.size = new Dimension(x, y);
          this.anchor = this.anchor_chooser.getAnchor();
          this.dispose();
        }
        else {
          this.getToolkit().beep();
        }
        break;

      case cmdCancel:
        this.size = null;
        this.dispose();
        break;

      default:
        return false;
    }
    return true;
  }


  public Dimension getNewSize() {

    return this.size;
  }


  public int getAnchor() {

    return this.anchor;
  }
}