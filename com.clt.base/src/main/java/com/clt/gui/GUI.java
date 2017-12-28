package com.clt.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.clt.event.DocumentChangeListener;
import com.clt.gui.border.LinesBorder;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuBar;
import com.clt.gui.menus.MenuCommander;
import com.clt.gui.menus.MenuOwner;
import com.clt.resources.DynamicResourceBundle;
import com.clt.util.Platform;
import com.clt.util.StringTools;

public class GUI {

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
      "com.clt.gui.Resources", new Runnable() {

        public void run() {

          GUI.localizeLookAndFeel();
        }
      });


  public static String getString(String key) {

    return GUI.resources.getString(key);
  }


  public static String format(String key, Object... args) {

    return StringTools.format(GUI.getString(key), args);
  }


  public static void scrollIntoCenter(Component c, JViewport view) {

    GUI.scrollIntoCenter(c.getBounds(), view);
  }


  public static void scrollIntoCenter(Rectangle r, JViewport view) {

    Dimension d = view.getExtentSize();
    Dimension s = view.getViewSize();

    // System.out.println("View size: " + s + ", extent: " + d);

    Point optimalPosition = new Point(r.x + (r.width - d.width) / 2, r.y
        + (r.height - d.height) / 2);
    // System.out.print(optimalPosition + " -> ");
    optimalPosition.x = Math.max(0, Math.min(s.width - d.width,
        optimalPosition.x));
    optimalPosition.y = Math.max(0, Math.min(s.height - d.height,
        optimalPosition.y));
    /*
     * Point oldpos = view.getViewPosition(); int steps = 20; for (int i=0;
     * i<steps; i++) { Point p = new Point(oldpos.x + (optimalPosition.x -
     * oldpos.x)/steps, oldpos.y + (optimalPosition.y - oldpos.y)/steps);
     * view.setViewPosition(p); view.repaint(); try { Thread.sleep(50); } catch
     * (InterruptedException exn) {} }
     */

    // System.out.print(optimalPosition + " -> ");
    view.setViewPosition(optimalPosition);
    view.setViewPosition(optimalPosition);
    // System.out.println(view.getViewPosition());
    view.repaint();
  }


  public static JScrollPane createScrollPane(JComponent view,
      int vScrollBarPolicy, int hScrollBarPolicy, final int maxWidth,
      final int maxHeight) {

    // enforce Human interface guidelines
    if (Platform.isMac()) {
      if (vScrollBarPolicy == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED) {
        vScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
      }
      if (hScrollBarPolicy == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
        hScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
      }
    }

    JScrollPane jsp = new JScrollPane(view, vScrollBarPolicy,
        hScrollBarPolicy) {

      @Override
      public Dimension getPreferredSize() {

        Dimension d = super.getPreferredSize();
        d.width = Math.min(d.width, maxWidth);
        d.height = Math.min(d.height, maxHeight);
        return d;
      }
    };

    GUI.setupScrollBar(jsp.getVerticalScrollBar());
    GUI.setupScrollBar(jsp.getHorizontalScrollBar());

    GUI.setupScrollPaneCorner(jsp);

    return jsp;
  }


  public static JScrollPane createScrollPane(JComponent view,
      int vScrollBarPolicy, int hScrollBarPolicy) {

    return GUI.createScrollPane(view, vScrollBarPolicy, hScrollBarPolicy,
        Integer.MAX_VALUE, Integer.MAX_VALUE);
  }


  public static JScrollPane createScrollPane(JTable table) {

    return GUI.createScrollPane(table, 0);
  }


  public static JScrollPane createScrollPane(JTable table, int height) {

    return GUI.createScrollPane(table, new Dimension(0, height));
  }


  public static JScrollPane createScrollPane(final JTable table,
      final Dimension size) {

    final JScrollPane jsp = new JScrollPane(table,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {

      @Override
      public Dimension getPreferredSize() {

        return new Dimension(size.width > 0 ? size.width : super
            .getPreferredSize().width,
            size.height > 0 ? size.height : super
                .getPreferredSize().height);
      }
    };
    jsp.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent evt) {

        if (table.isEditing() ? table.getCellEditor().stopCellEditing()
            : true) {
          table.clearSelection();
          jsp.requestFocus();
        }
      }
    });

    GUI.setupScrollBar(jsp.getVerticalScrollBar());
    GUI.setupScrollBar(jsp.getHorizontalScrollBar());

    GUI.setupScrollPaneCorner(jsp);

    if (Platform.isMac()) {
      jsp.setBorder(BorderFactory.createEmptyBorder());
      table.setBorder(BorderFactory.createEmptyBorder());
    }

    return jsp;
  }


  public static void setupScrollPaneCorner(JScrollPane jsp) {

    if ((jsp != null) && Platform.isMac() && false) {
      JComponent corner = new JComponent() {

        @Override
        public boolean isOpaque() {

          return true;
        }


        @Override
        protected void paintComponent(Graphics g) {

          g.setColor(UIManager.getColor("control"));
          g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
      };

      corner.setBorder(new LinesBorder("tl", Color.black));

      jsp.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
    }
  }


  public static void setupScrollBar(final JScrollBar scroller) {

    if (scroller != null) {
      // Wenn der ScrollBar nix anzuzeigen hat, soll er sich gefaelligst
      // disablen.
      scroller.addAdjustmentListener(new AdjustmentListener() {

        public void adjustmentValueChanged(AdjustmentEvent e) {

          int diff = scroller.getMaximum() - scroller.getMinimum();
          boolean enabled =
            ((diff > 0) && (scroller.getVisibleAmount() < diff));
          if (scroller.isEnabled() != enabled) {
            scroller.setEnabled(enabled);
          }
        }
      });
    }
  }


  public static JTabbedPane createTabbedPane() {

    if (Platform.isMac()) {
      return new JTabbedPane() {

        @Override
        protected void addImpl(Component comp, Object constraints,
            int index) {

          this.makeTransparent(comp);
          super.addImpl(comp, constraints, index);
        }


        private void makeTransparent(Component comp) {

          if ((comp instanceof AbstractButton)
              || (comp instanceof JComboBox)
              || (comp instanceof JSlider)) {
            ((JComponent)comp).setOpaque(false);
          }
          else if (comp instanceof JPanel) {
            JPanel p = (JPanel)comp;
            p.setOpaque(false);
            for (int i = 0; i < p.getComponentCount(); i++) {
              this.makeTransparent(p.getComponent(i));
            }
          }
        }
      };
    }
    else {
      return new JTabbedPane();
    }
  }


  public static JPanel createButtonPanel(JButton buttons[]) {

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    JPanel bs = new JPanel(new GridLayout(1, 0, 12, 12));
    if (Platform.isWindows()) {
      gbc.anchor = GridBagConstraints.CENTER;
      for (int i = 0; i < buttons.length; i++) {
        bs.add(buttons[i]);
      }
    }
    else {
      gbc.anchor = GridBagConstraints.EAST;
      for (int i = buttons.length - 1; i >= 0; i--) {
        bs.add(buttons[i]);
      }
    }
    p.add(bs, gbc);
    // let the parent handle global spacing
    // p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    return p;
  }


  public static boolean isPopupTrigger(MouseEvent e) {

    // circumvent modifier overloading by using the extended
    // modifier BUTTON3_DOWN_MASK
    int modifiers = e.getModifiersEx();
    return e.isControlDown()
        || ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0);
  }


  public static Font getSystemFont() {

    Font f = UIManager.getFont("Label.font");
    if (f != null) {
      return f;
    }
    else {
      return new Font("Dialog", Font.PLAIN, 12);
    }
  }


  public static Font getSmallSystemFont() {

    if (Platform.isMac()) {
      return new Font("SansSerif", Font.PLAIN, 11);
    }
    else {
      Font f = UIManager.getFont("Label.font");
      if (f == null) {
        return new Font("SansSerif", Font.PLAIN, 12);
      }
      else {
        return f.deriveFont(f.getSize2D() - 1.0f);
      }
    }
  }


  public static Font getTinySystemFont() {

    Font f = GUI.getSmallSystemFont();
    return f.deriveFont(f.getSize2D() - 1.0f);
  }


  public static JPanel initContentPane(RootPaneContainer root) {

    Container c = root.getContentPane();
    if (c instanceof JPanel) {
      return (JPanel)c;
    }
    else {
      JPanel p = new JPanel();
      root.setContentPane(p);
      return p;
    }
  }


  public static void setWaitCursor(Component c) {

    if (c == null) {
      throw new IllegalArgumentException(
          "Can't change cursor on <null> component");
    }
    Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    if (cursor == null) {
      throw new MissingResourceException("Failed to load wait cursor",
          Cursor.class.getName(), "WAIT_CURSOR");
    }
    c.setCursor(cursor);
  }


  public static void setTextCursor(Component c) {

    c.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
  }


  public static void setDefaultCursor(Component c) {

    c.setCursor(Cursor.getDefaultCursor());
  }


  public static void setDefaultButtons(RootPaneContainer c, final JButton ok,
      final JButton cancel) {

    if (ok != null) {
      c.getRootPane().setDefaultButton(ok);
    }

    final CmdMenuBar mbar = new CmdMenuBar(new MenuCommander() {

      public boolean menuItemState(int cmd) {

        return true;
      }


      public String menuItemName(int cmd, String oldName) {

        return oldName;
      }


      public boolean doCommand(int cmd) {

        switch (cmd) {
          case Commands.cmdOK:
          if (ok != null) {
            ok.doClick();
          }
          break;
        case Commands.cmdCancel:
          if (cancel != null) {
            cancel.doClick();
          }
          break;
        default:
          return false;
      }
      return true;
    }
    });

    CmdMenu m = mbar.addMenu("");
    m.addItem(GUI.getString("OK"), Commands.cmdOK, KeyStroke.getKeyStroke(
        KeyEvent.VK_ENTER, 0));
    m.addItem(GUI.getString("Cancel"), Commands.cmdCancel, KeyStroke
        .getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    m.addItem(GUI.getString("Cancel"), Commands.cmdCancel, KeyStroke
        .getKeyStroke(KeyEvent.VK_CANCEL, 0));
    if (Platform.isMac()) {
      m.addItem(GUI.getString("Cancel"), Commands.cmdCancel,
          KeyEvent.VK_PERIOD);
    }

    mbar.updateMenus();

    c.getLayeredPane().add(mbar);
  }

  static FindReplaceDialog findReplaceDialog = null;


  public static void addFindReplaceSupport(final JTextComponent c,
      final boolean replace) {

    GUI.addFindReplaceSupport(c, KeyEvent.VK_F, replace);
    if (!replace) {
      GUI.addFindReplaceSupport(c, KeyEvent.VK_H, true);
    }

    GUI.setKeyBinding(c, KeyStroke.getKeyStroke(KeyEvent.VK_G, c.getToolkit()
        .getMenuShortcutKeyMask()), new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        if ((GUI.findReplaceDialog != null)
            && (GUI.findReplaceDialog.getTargetComponent() == c)) {
          GUI.findReplaceDialog.find();
        }
      }
    });
    GUI.setKeyBinding(c, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {

            if ((GUI.findReplaceDialog != null)
                && (GUI.findReplaceDialog.getTargetComponent() == c)) {
              GUI.findReplaceDialog.find();
            }
          }
        });
  }


  private static void addFindReplaceSupport(final JTextComponent c,
      final int keyCode, final boolean replace) {

    GUI.setKeyBinding(c, KeyStroke.getKeyStroke(keyCode, c.getToolkit()
        .getMenuShortcutKeyMask()), new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        if (GUI.findReplaceDialog == null) {
          GUI.findReplaceDialog = new FindReplaceDialog(c, replace);
        }
        else {
          GUI.findReplaceDialog.setTargetComponent(c, replace);
        }
        GUI.findReplaceDialog.setVisible(true);
        GUI.findReplaceDialog.toFront();
      }
    });
  }


  public static void removeKeyBinding(Container c, KeyStroke key) {

    if (c instanceof JComponent) {
      GUI.removeKeyBinding((JComponent)c, JComponent.WHEN_FOCUSED, key);
      GUI.removeKeyBinding((JComponent)c, JComponent.WHEN_IN_FOCUSED_WINDOW,
          key);
      GUI.removeKeyBinding((JComponent)c,
          JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, key);
    }
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component child = c.getComponent(i);
      if (child instanceof Container) {
        GUI.removeKeyBinding((Container)child, key);
      }
    }
  }


  private static void removeKeyBinding(JComponent c, int condition,
      KeyStroke key) {

    // c.getInputMap(condition).put(key, new Object());

    c.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        // empty action
      }
    }, key, condition);
  }


  public static void setKeyBinding(Container container, KeyStroke key,
      ActionListener action) {

    GUI.setKeyBinding(container, key, action, null, false);
  }


  public static void setKeyBinding(Container container, KeyStroke key,
      ActionListener action, Class<?>[] exceptions,
      boolean onlyWhenFocused) {

    if (container instanceof JComponent) {
      boolean exception = false;
      if (exceptions != null) {
        for (Class<?> c : exceptions) {
          if (c.isAssignableFrom(container.getClass())) {
            exception = true;
          }
        }
      }

      if (!exception) {
        GUI.setKeyBinding((JComponent)container, JComponent.WHEN_FOCUSED,
            key, action);
        if (!onlyWhenFocused) {
          GUI.setKeyBinding((JComponent)container,
              JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, key,
              action);
          GUI.setKeyBinding((JComponent)container,
              JComponent.WHEN_IN_FOCUSED_WINDOW, key, action);
        }
      }
    }
    for (int i = 0; i < container.getComponentCount(); i++) {
      Component child = container.getComponent(i);
      if (child instanceof Container) {
        GUI.setKeyBinding((Container)child, key, action, exceptions,
            onlyWhenFocused);
      }
    }
  }


  private static void setKeyBinding(JComponent c, int condition,
      KeyStroke key, ActionListener action) {

    c.registerKeyboardAction(action, key, condition);
  }


  public static Rectangle getRelativeBounds(Component c, Container parent) {

    Rectangle bounds = c.getBounds();

    Component p = c.getParent();

    while ((p != null) && (p != parent)) {
      Point offset = p.getLocation();
      bounds.translate(offset.x, offset.y);
      p = p.getParent();
    }

    if (p == null) {
      throw new IllegalArgumentException(
          "Container does not contain component");
    }

    return bounds;
  }


  public static void addMouseInputListener(Component c, MouseInputListener ml) {

    c.addMouseListener(ml);
    c.addMouseMotionListener(ml);
  }


  public static void addDocumentChangeListener(JTextComponent c,
      final DocumentChangeListener dl) {

    c.getDocument().addDocumentListener(dl);
    c.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().equals("document")) {
          ((Document)evt.getOldValue()).removeDocumentListener(dl);
          ((Document)evt.getNewValue()).addDocumentListener(dl);
        }
      }
    });
  }


  public static void assignMnemonics(Component component) {

    Set<Character> usedCharacters = new HashSet<Character>();
    GUI.assignMnemonicsImmediate(component, usedCharacters);
    GUI.assignMnemonicsChildren(component, usedCharacters);
  }


  private static boolean isMnemonic(char c) {

    return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
  }


  private static void assignMnemonicsImmediate(Component component,
      Set<Character> usedCharacters) {

    if (component instanceof AbstractButton) {
      String title = ((AbstractButton)component).getText();
      if ((title != null) && (title.length() > 0)) {
        char c = title.charAt(0);
        if (GUI.isMnemonic(c) && !usedCharacters.contains(c)) {
          ((AbstractButton)component).setMnemonic(c);
          usedCharacters.add(c);
        }
        else {
          String[] words = StringTools.split(title, ' ');
          boolean found = false;
          if (words.length > 1) {
            for (int i = 0; (i < words.length) && !found; i++) {
              if (words[i].length() > 0) {
                c = words[i].charAt(0);
                if (GUI.isMnemonic(c)
                    && !usedCharacters.contains(c)) {
                  ((AbstractButton)component).setMnemonic(c);
                  int pos = 0;
                  for (int j = 0; j < i; j++) {
                    pos += words[j].length() + 1;
                  }
                  ((AbstractButton)component)
                      .setDisplayedMnemonicIndex(pos);
                  usedCharacters.add(c);
                  found = true;
                }
              }
            }
          }

          if (!found) {
            for (int i = 1; (i < title.length()) && !found; i++) {
              c = title.charAt(i);
              if (GUI.isMnemonic(c) && !usedCharacters.contains(c)) {
                ((AbstractButton)component).setMnemonic(c);
                usedCharacters.add(c);
                found = true;
              }
            }
          }
        }
      }
    }
    else if (component instanceof Container) {
      if (!(component instanceof JMenu)
          && !(component instanceof JTabbedPane)) {
        Container container = (Container)component;
        for (int i = 0; i < container.getComponentCount(); i++) {
          GUI.assignMnemonicsImmediate(container.getComponent(i),
              usedCharacters);
        }
        for (int i = 0; i < container.getComponentCount(); i++) {
          GUI.assignMnemonicsChildren(container.getComponent(i),
              usedCharacters);
        }
      }
    }
  }


  private static void assignMnemonicsChildren(Component component,
      Set<Character> usedCharacters) {

    if (component instanceof JMenu) {
      JMenu m = (JMenu)component;
      for (int i = 0; i < m.getMenuComponentCount(); i++) {
        GUI.assignMnemonicsImmediate(m.getMenuComponent(i), usedCharacters);
      }
      for (int i = 0; i < m.getMenuComponentCount(); i++) {
        GUI.assignMnemonicsChildren(m.getMenuComponent(i),
            new HashSet<Character>(usedCharacters));
      }
    }
    else if (component instanceof JTabbedPane) {
      JTabbedPane tp = (JTabbedPane)component;
      for (int i = 0; i < tp.getTabCount(); i++) {
        Set<Character> subMnemonics = new HashSet<Character>(
            usedCharacters);
        GUI.assignMnemonicsImmediate(tp.getComponentAt(i), subMnemonics);
        GUI.assignMnemonicsChildren(tp.getComponentAt(i), subMnemonics);
      }
    }
  }


  public static Frame getFrameForComponent(Component c) {

    return JOptionPane.getFrameForComponent(c);
  }


  public static Window getWindowForComponent(Component c) {

    if (c == null) {
      return JOptionPane.getRootFrame();
    }
    else if ((c instanceof Frame) || (c instanceof Dialog)) {
      return (Window)c;
    }
    else {
      return GUI.getWindowForComponent(c.getParent());
    }
  }


  public static MenuOwner getMenuOwnerForComponent(Component c) {

    if (c == null) {
      return null;
    }
    else if (c instanceof MenuOwner) {
      return (MenuOwner)c;
    }
    else {
      return GUI.getMenuOwnerForComponent(c.getParent());
    }
  }


  @SuppressWarnings("unchecked")
  public static <T> T getParent(Component c, Class<T> cls) {

    if (c == null) {
      return null;
    }
    else if (cls.isAssignableFrom(c.getClass())) {
      return (T)c;
    }
    else {
      return GUI.getParent(c.getParent(), cls);
    }
  }


  public static Color slightlyDarker(Color c) {

    Color darker = c.darker();
    return new Color((c.getRed() + darker.getRed()) / 2,
        (c.getGreen() + darker.getGreen()) / 2, (c.getBlue() + darker
            .getBlue()) / 2, (c.getAlpha() + darker.getAlpha()) / 2);
  }


  /**
   * This method executes a runnable asynchronously in the event dispatching
   * thread. Attention: this method is not thread-safe. This method throws no
   * exceptions. Use
   * <code>invokeAndWaitCanThrow(Runnable r) throws Throwable</code> if you want
   * to catch exceptions.
   * 
   * @param r
   *          Runnable to be executed by the event dispatching thread.
   */
  public static void invokeAndWait(Runnable r) {

    try {
      GUI.invokeAndWaitCanThrow(r);
    } catch (ThreadDeath d) {
      throw d;
    } catch (Throwable t) {
      // ignore all errors.
      // If you want errors, use invokeAndWaitCanThrow()
    }
  }


  /**
   * This method executes a runnable asynchronously in the event dispatching
   * thread. Attention: this method is not thread-safe.
   * 
   * @param r
   *          Runnable to be executed by the event dispatching thread.
   */
  public static void invokeAndWaitCanThrow(Runnable r)
      throws Throwable {

    if (SwingUtilities.isEventDispatchThread()) {
      r.run();
    }
    else {
      try {
        SwingUtilities.invokeAndWait(r);
      } catch (InvocationTargetException exn) {
        throw exn.getTargetException();
      }
    }
  }


  /**
   * Thread safe method that a runnable only executes in the event dispatching
   * thread.
   * 
   * @param r
   *          Runnable to be executed by the event dispatching thread.
   */
  public static void invokeSafeWork(Runnable r) {

    if (SwingUtilities.isEventDispatchThread()) {
      r.run();
    }
    else {
      SwingUtilities.invokeLater(r);
    }
  }


  private static void localizeLookAndFeel() {

    UIManager.put("FileChooser.cancelButtonText", GUI.getString("Cancel"));
    UIManager.put("FileChooser.cancelButtonToolTipText", GUI
        .getString("Cancel"));
    UIManager.put("FileChooser.openButtonText", GUI.getString("Open"));
    UIManager.put("FileChooser.openButtonToolTipText", GUI
        .getString("Open"));
    UIManager.put("FileChooser.saveButtonText", GUI.getString("Save"));
    UIManager.put("FileChooser.saveButtonToolTipText", GUI
        .getString("Save"));

    UIManager.put("OptionPane.yesButtonText", GUI.getString("Yes"));
    UIManager.put("OptionPane.noButtonText", GUI.getString("No"));
    UIManager.put("OptionPane.cancelButtonText", GUI.getString("Cancel"));

    UIManager.put("ColorChooser.okText", GUI.getString("OK"));
    UIManager.put("ColorChooser.cancelText", GUI.getString("Cancel"));
    UIManager.put("ColorChooser.rgbRedText", GUI.getString("Red"));
    UIManager.put("ColorChooser.rgbGreenText", GUI.getString("Green"));
    UIManager.put("ColorChooser.rgbBlueText", GUI.getString("Blue"));
  }


  public static void tweakLookAndFeel() {

    // Swing Look&Feel Defaults manipulieren

    // die folgenden drei Bloecke aendern die Standard-Schriftzuege
    // und die entsprechende Mnemonics im FileChooser und im
    // OptionPane
    GUI.localizeLookAndFeel();

    UIManager.put("FileChooser.lookInLabelText", "Suchen in:");
    UIManager.put("FileChooser.fileNameLabelText", "Dateiname");
    UIManager.put("FileChooser.filesOfTypeLabelText", "Dateityp");
    UIManager.put("FileChooser.newFolderToolTipText",
        "Neuen Ordner anlegen");
    UIManager.put("FileChooser.listViewButtonToolTipText", "Listenansicht");
    UIManager.put("FileChooser.detailsViewButtonToolTipText",
        "Detailansicht");
    UIManager.put("FileChooser.upFolderToolTipText",
        "\u00dcbergeordnetes Verzeichnis");
    UIManager.put("FileChooser.homeFolderToolTipText", "Homeverzeichnis");

    UIManager.put("FileChooser.lookInLabelMnemonic", "");
    UIManager.put("FileChooser.fileNameLabelMnemonic", "");
    UIManager.put("FileChooser.filesOfTypeLabelMnemonic", "");
    UIManager.put("FileChooser.saveButtonMnemonic", "");
    UIManager.put("FileChooser.openButtonMnemonic", "");
    UIManager.put("FileChooser.cancelButtonMnemonic", "");

    UIManager.put("ColorChooser.resetText", "Zur\u00fccksetzen");
    UIManager.put("ColorChooser.swatchesNameText", "Farbfelder");
    UIManager.put("ColorChooser.swatchesRecentText", "Vorherige:");
    UIManager.put("ColorChooser.previewText", "Vorschau:");

    if (Platform.isWindows()) {
      UIManager.put("Button.margin", new Insets(1, 9, 1, 9));
      UIManager.put("TabbedPane.tabInsets", new Insets(0, 8, 3, 8));
    }

    String[] SchwarzMachen = { "TitledBorder.titleColor",
        "Label.foreground" };
    for (int i = 0; i < SchwarzMachen.length; i++) {
      UIManager.put(SchwarzMachen[i], Color.black);
    }
  }
}