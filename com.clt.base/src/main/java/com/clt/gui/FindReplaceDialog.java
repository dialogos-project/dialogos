/*
 * @(#)FindReplaceDialog.java
 * Created on 04.04.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;

import com.clt.event.DocumentChangeListener;
import com.clt.properties.BooleanProperty;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.Property;
import com.clt.util.UniqueList;

/**
 * @author dabo
 * 
 */
public class FindReplaceDialog
    extends JFrame
    implements Commander {

  private enum Direction {
        FORWARD("Forward"),
        BACKWARD("Backward");

    private String label;


    private Direction(String label) {

      this.label = label;
    }


    @Override
    public String toString() {

      return GUI.getString(this.label);
    }
  }

  private static final int cmdFind = 1;
  private static final int cmdReplaceAndFind = 2;
  private static final int cmdReplace = 3;
  private static final int cmdReplaceAll = 4;

  private UniqueList<String> findHistory = new UniqueList<String>();
  private UniqueList<String> replaceHistory = new UniqueList<String>();

  private JComboBox find;
  private JComboBox replace;

  private EnumProperty<Direction> direction =
    EnumProperty.create(Direction.class,
        GUI.getString("Direction"), null);

  private BooleanProperty caseSensitive =
    FindReplaceDialog.createBooleanProperty("CaseSensitive");
  private BooleanProperty wholeWord =
    FindReplaceDialog.createBooleanProperty("WholeWord");
  private BooleanProperty wrapSearch =
    FindReplaceDialog.createBooleanProperty("WrapSearch");

  private JTextComponent activeComponent = null;
  private Match lastMatch = null;

  private CmdButton buttonFind;
  private CmdButton buttonReplaceAndFind;
  private CmdButton buttonReplace;
  private CmdButton buttonReplaceAll;


  public FindReplaceDialog(JTextComponent component, boolean replace) {

    this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    this.setResizable(false);

    GUI.setKeyBinding(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
      new ActionListener()
        {

          public void actionPerformed(ActionEvent e) {

            FindReplaceDialog.this.setVisible(false);
          }
        });

    this.direction.setValue(Direction.FORWARD);

    this.find = this.createComboBox(this.findHistory);
    this.replace = this.createComboBox(this.replaceHistory);

    this.buttonFind =
      new CmdButton(this, FindReplaceDialog.cmdFind, GUI.getString("Find"));
    this.buttonReplaceAndFind =
      new CmdButton(this, FindReplaceDialog.cmdReplaceAndFind,
            GUI.getString("ReplaceAndFind"));
    this.buttonReplace =
      new CmdButton(this, FindReplaceDialog.cmdReplace, GUI
        .getString("Replace"));
    this.buttonReplaceAll =
      new CmdButton(this, FindReplaceDialog.cmdReplaceAll, GUI
        .getString("ReplaceAll"));

    this.pack();

    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowActivated(WindowEvent e)
            {

              FindReplaceDialog.this.updateMenus();
            }


      @Override
      public void windowOpened(WindowEvent e)
            {

              FindReplaceDialog.this.updateMenus();
              FindReplaceDialog.this.find.requestFocus();
            }
    });
    this.updateMenus();

    this.setTargetComponent(component, replace);

    WindowUtils.setLocationRelativeTo(this, component);
  }


  @Override
  public void setVisible(boolean visible) {

    super.setVisible(visible);
    if (visible && ((this.getState() & Frame.ICONIFIED) != 0)) {
      this.setState(Frame.NORMAL);
    }
  }


  public JTextComponent getTargetComponent() {

    return this.activeComponent;
  }


  public void setTargetComponent(JTextComponent component, boolean replace) {

    if (component != this.activeComponent) {
      this.activeComponent = component;
      this.lastMatch = null;
    }

    this.setTitle(GUI.getString("Find")
      + (replace ? " & " + GUI.getString("Replace") : ""));

    JPanel content = new JPanel(new GridBagLayout());
    content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.insets = new Insets(3, 6, 3, 0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    content.add(new JLabel(GUI.getString("Find") + ":"), gbc);
    gbc.gridx++;

    gbc.fill = GridBagConstraints.HORIZONTAL;
    content.add(this.find, gbc);

    gbc.gridx = 0;
    gbc.gridy++;

    if (replace) {
      gbc.fill = GridBagConstraints.NONE;
      content.add(new JLabel(GUI.getString("ReplaceWith") + ":"), gbc);
      gbc.gridx++;

      gbc.fill = GridBagConstraints.HORIZONTAL;
      content.add(this.replace, gbc);
      gbc.gridx = 0;
      gbc.gridy++;
    }

    JPanel direction = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    direction.setBorder(BorderFactory.createTitledBorder(this.direction
      .getName()));
    direction.add(this.direction.createEditor(
      Property.EDIT_TYPE_RADIOBUTTONS_HORIZONTAL, false));

    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(3, 0, 3, 0);
    content.add(direction, gbc);
    gbc.gridy++;

    JPanel options = new JPanel(new GridLayout(0, 1, 2, 2));
    options.setBorder(BorderFactory
      .createTitledBorder(GUI.getString("Options")));

    options.add(this.caseSensitive.createEditor(true));
    options.add(this.wholeWord.createEditor(true));
    options.add(this.wrapSearch.createEditor(true));

    content.add(options, gbc);
    gbc.gridy++;

    JPanel buttons = new JPanel();

    if (replace) {
      buttons.setLayout(new GridLayout(2, 2, 6, 6));
      buttons.add(this.buttonFind);
      buttons.add(this.buttonReplaceAndFind);
      buttons.add(this.buttonReplace);
      buttons.add(this.buttonReplaceAll);
    }
    else {
      buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
      buttons.add(this.buttonFind);
    }

    content.add(buttons, gbc);

    this.setContentPane(content);
    this.getRootPane().setDefaultButton(this.buttonFind);

    this.setSize(this.getPreferredSize());
  }


  public void updateMenus() {

    if (this.lastMatch != null) {
      if ((this.lastMatch.document != this.activeComponent.getDocument())
                    || (this.lastMatch.location != this.activeComponent
                      .getSelectionStart())
                    || (this.lastMatch.location + this.lastMatch.length != this.activeComponent
                      .getSelectionEnd())) {
        this.lastMatch = null;
      }
    }

    for (CmdButton b : new CmdButton[] { this.buttonFind, this.buttonReplace,
      this.buttonReplaceAndFind,
                this.buttonReplaceAll }) {
      b.setEnabled(this.menuItemState(b.getCommand()));
      b.setName(this.menuItemName(b.getCommand(), b.getText()));
    }
  }


  public boolean menuItemState(int cmd) {

    switch (cmd) {
      case cmdFind:
        return this.getFindString().length() > 0;
      default:
        return this.lastMatch != null;
    }
  }


  public String menuItemName(int cmd, String oldName) {

    switch (cmd) {
      case cmdFind:
        return GUI.getString("Find");
      case cmdReplace:
        return GUI.getString("Replace");
      case cmdReplaceAndFind:
        return GUI.getString("ReplaceAndFind");
      case cmdReplaceAll:
        return GUI.getString("ReplaceAll");
      default:
        return oldName;
    }
  }


  public boolean doCommand(int cmd) {

    switch (cmd) {
      case cmdFind:
        if (this.find() == null) {
          this.getToolkit().beep();
        }
        break;

      case cmdReplaceAndFind:
        if (this.find() == null) {
          this.getToolkit().beep();
        }
        break;

      case cmdReplace:
        this.replace();
        break;

      case cmdReplaceAll:
        this.activeComponent.setCaretPosition(0);
        while (this.find(Direction.FORWARD, false) != null) {
          this.replace();
        }

      default:
        return false;
    }

    this.updateMenus();

    return true;
  }


  private String getFindString() {

    return (String)this.find.getEditor().getItem();
  }


  private String getReplaceString() {

    return (String)this.replace.getEditor().getItem();
  }


  private void replace() {

    Document doc = this.activeComponent.getDocument();
    if ((this.lastMatch != null) && (this.lastMatch.document == doc)) {
      String replaceString = this.getReplaceString();

      try {
        doc.remove(this.lastMatch.location, this.lastMatch.length);
        doc.insertString(this.lastMatch.location, replaceString, null);
        this.activeComponent.select(this.lastMatch.location,
          this.lastMatch.location
                        + replaceString.length());
      } catch (BadLocationException exn) {
        // shouldn't happen
      }
    }
    this.lastMatch = null;
  }


  private boolean equal(char c1, char c2) {

    if (c1 == c2) {
      return true;
    }
    else if (!this.caseSensitive.getValue()) {
      return Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }
    else {
      return false;
    }
  }


  public Match find() {

    return this.find(this.direction.getValue(), this.wrapSearch.getValue());
  }


  private Match find(Direction direction, boolean wrap) {

    int start = this.activeComponent.getSelectionStart();
    int end = this.activeComponent.getSelectionEnd();
    Document doc = this.activeComponent.getDocument();

    String findString = this.getFindString();
    if (findString.length() == 0) {
      return null;
    }

    this.findHistory.add(findString);

    Match m;
    if (direction == Direction.BACKWARD) {
      m =
        this.findBackward(findString, doc, this.lastMatch == null ? (end - 1)
          : (end - 2), wrap);
    }
    else {
      m =
        this.findForward(findString, doc, this.lastMatch == null ? start
          : (start + 1), wrap);
    }

    if (m != null) {
      // System.out.println("Found match: " + m.location + " - " + (m.location +
      // m.length));
      this.activeComponent.select(m.location, m.location + m.length);
      this.lastMatch = m;
    }

    return m;
  }


  private Match findForward(String findString, Document doc, int pos,
      boolean wrap) {

    int findLength = findString.length();

    boolean previousWasLetter = false;
    if (pos < doc.getLength()) {
      List<Match> possibleMatches = new LinkedList<Match>();
      CharacterIterator text = new DocumentIterator(doc, pos);

      char c = text.current();
      while (c != CharacterIterator.DONE) {
        if (this.equal(c, findString.charAt(0))) {
          if (!this.wholeWord.getValue() || !previousWasLetter) {
            possibleMatches.add(new Match(doc, pos));
          }
        }

        for (Iterator<Match> it = possibleMatches.iterator(); it.hasNext();) {
          Match m = it.next();
          if (this.equal(c, findString.charAt(m.length))) {
            m.length++;
            if (m.length == findLength) {
              if (this.wholeWord.getValue()) {
                if (pos == doc.getLength() - 1) {
                  return m;
                }
                else {
                  char next = text.next();
                  text.previous();
                  if (Character.isUnicodeIdentifierPart(next)) {
                    it.remove();
                  }
                  else {
                    return m;
                  }
                }
              }
              else {
                return m;
              }
            }
          }
          else {
            it.remove();
          }
        }

        previousWasLetter = Character.isUnicodeIdentifierPart(c);
        c = text.next();
        pos++;
      }
    }

    if (wrap) {
      return this.findForward(findString, doc, 0, false);
    }
    else {
      return null;
    }
  }


  private Match findBackward(String findString, Document doc, int pos,
      boolean wrap) {

    boolean nextWasLetter = false;

    if (pos >= 0) {
      List<Match> possibleMatches = new LinkedList<Match>();
      CharacterIterator text = new DocumentIterator(doc, pos);

      int findLength = findString.length();

      System.out.println("Find backward starting at pos " + pos);

      char c = text.current();
      while (c != CharacterIterator.DONE) {
        if (this.equal(c, findString.charAt(findLength - 1))) {
          if (!this.wholeWord.getValue() || !nextWasLetter) {
            possibleMatches.add(new Match(doc, pos - (findLength - 1)));
          }
        }

        for (Iterator<Match> it = possibleMatches.iterator(); it.hasNext();) {
          Match m = it.next();
          if (this.equal(c, findString.charAt(findLength - (m.length + 1)))) {
            m.length++;
            if (m.length == findLength) {
              if (this.wholeWord.getValue()) {
                if (pos == 0) {
                  return m;
                }
                else {
                  char previous = text.previous();
                  text.next();
                  if (Character.isUnicodeIdentifierPart(previous)) {
                    it.remove();
                  }
                  else {
                    return m;
                  }
                }
              }
              else {
                return m;
              }
            }
          }
          else {
            it.remove();
          }
        }

        nextWasLetter = Character.isUnicodeIdentifierPart(c);
        c = text.previous();
        pos--;
      }
    }

    if (wrap) {
      return this.findBackward(findString, doc, doc.getLength() - 1, false);
    }
    else {
      return null;
    }
  }


  private JComboBox createComboBox(final UniqueList<String> history) {

    JComboBox cb = new JComboBox();
    cb.setEditable(true);

    Component editor = cb.getEditor().getEditorComponent();
    if (editor instanceof JTextComponent) {
      GUI.addDocumentChangeListener((JTextComponent)editor,
        new DocumentChangeListener() {

          @Override
          public void documentChanged(DocumentEvent evt)
                {

                  FindReplaceDialog.this.updateMenus();
                }
        });
    }

    cb.setModel(new ComboBoxModel() {

      private Object selection = null;


      public Object getSelectedItem() {

        return this.selection;
      }


      public void setSelectedItem(Object anItem) {

        this.selection = anItem;
      }


      public Object getElementAt(int index) {

        return history.get(index);
      }


      public int getSize() {

        return history.size();
      }


      public void addListDataListener(ListDataListener l) {

        history.addListDataListener(l);
      }


      public void removeListDataListener(ListDataListener l) {

        history.removeListDataListener(l);
      }
    });

    return cb;
  }


  private static BooleanProperty createBooleanProperty(String id) {

    return new DefaultBooleanProperty(id, GUI.getString(id), null, false);
  }

  private static class Match {

    Document document;
    int location;
    int length;


    public Match(Document document, int location) {

      this.document = document;
      this.location = location;
      this.length = 0;
    }
  }

  private static class DocumentIterator
        implements CharacterIterator {

    private Document d;
    private List<PartialSegment> segments;
    private int currentSegment;
    private int pos;


    public DocumentIterator(Document d, int start) {

      this.d = d;
      this.segments = new ArrayList<PartialSegment>();

      this.pos = 0;
      this.currentSegment = this.loadNextSegment();

      this.setIndex(start);
    }


    private DocumentIterator(Document d, List<PartialSegment> segments,
        int currentSegment,
                                 int pos) {

      this.d = d;
      this.segments = new ArrayList<PartialSegment>(segments.size());
      for (PartialSegment s : segments) {
        this.segments.add(s.clone());
      }
      this.currentSegment = currentSegment;
      this.pos = pos;
    }


    @Override
    public DocumentIterator clone() {

      return new DocumentIterator(this.d, this.segments, this.currentSegment,
        this.pos);
    }


    private int loadNextSegment() {

      PartialSegment segment = new PartialSegment(this.pos);
      segment.setPartialReturn(true);
      try {
        this.d.getText(this.pos, this.d.getLength() - this.pos, segment);
        this.segments.add(segment);
        this.pos += segment.count;
      } catch (BadLocationException exn) {
        // shouldn't happen
      }
      return this.segments.size() - 1;
    }


    public char current() {

      return this.curSeg().current();
    }


    public char first() {

      this.currentSegment = 0;
      return this.curSeg().first();
    }


    public int getBeginIndex() {

      return 0;
    }


    public int getEndIndex() {

      return this.d.getLength();
    }


    public int getIndex() {

      PartialSegment segment = this.curSeg();
      return segment.documentOffset
        + (segment.getIndex() - segment.getBeginIndex());
    }


    public char last() {

      int length = this.d.getLength();
      while (this.pos < length) {
        this.currentSegment = this.loadNextSegment();
      }

      return this.curSeg().last();
    }


    public char next() {

      char c = this.curSeg().next();
      if (c == CharacterIterator.DONE) {
        if (this.currentSegment < this.segments.size() - 1) {
          this.currentSegment++;
          return this.curSeg().first();
        }
        else {
          if (this.pos < this.d.getLength()) {
            this.currentSegment = this.loadNextSegment();
            return this.curSeg().first();
          }
        }
      }
      return c;
    }


    public char previous() {

      char c = this.curSeg().previous();
      if ((c == CharacterIterator.DONE) && (this.currentSegment > 0)) {
        this.currentSegment--;
        return this.curSeg().last();
      }
      return c;
    }


    public char setIndex(int position) {

      if ((position < 0) || (position >= this.d.getLength())) {
        throw new IllegalArgumentException("bad position: " + position);
      }

      this.currentSegment = 0;
      while (position >= this.pos) {
        this.currentSegment = this.loadNextSegment();
      }
      while ((this.currentSegment < this.segments.size() - 1)
                    && (this.segments.get(this.currentSegment).documentOffset
                            + this.segments.get(this.currentSegment).count <= position)) {
        this.currentSegment++;
      }

      return this.curSeg().setIndex(
        position - this.curSeg().documentOffset + this.curSeg().offset);
    }


    private PartialSegment curSeg() {

      return this.segments.get(this.currentSegment);
    }
  }

  private static class PartialSegment
        extends Segment {

    public int documentOffset = 0;


    public PartialSegment(int offset) {

      this.documentOffset = offset;
    }


    @Override
    public PartialSegment clone() {

      return (PartialSegment)super.clone();
    }
  }
}
