package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.clt.gui.AutoCompleteField;
import com.clt.gui.GUI;
import com.clt.gui.WindowUtils;
import com.clt.speech.recognition.AbstractRecognitionResult;
import com.clt.speech.recognition.AbstractUtterance;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.Utterance;
import com.clt.util.Platform;
import com.clt.util.StringTools;
import com.clt.util.Timeout;

class SimulationDialog
    extends JFrame {

  private static final int buttonBorderWidth = 4;

  private JButton okButton, timeoutButton = null;
  private JPanel okButtonPanel, timeoutButtonPanel;
  private AutoCompleteField inputField[] = null;
  private JTextField confField[] = null;
  private JTextComponent completionFields[] = new JTextComponent[8];
  private JPanel inputPanel;

  private transient RecognitionResult result = null;
  private transient boolean timedOut = false;
  private final Object modalLock = new Object();


  public SimulationDialog(Component parent) {

    // super(JOptionPane.getFrameForComponent(parent), "Input", true);
    super("Simulated Input");
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    this.okButton = new JButton(GUI.getString("OK"));
    this.okButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        SimulationDialog.this.result = SimulationDialog.this.collectResult();
        SimulationDialog.this.setVisible(false);
      }
    });

    this.timeoutButton = new JButton("Timeout");
    this.timeoutButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        SimulationDialog.this.timedOut = true;
        SimulationDialog.this.setVisible(false);
      }
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JLabel("What did the user say?"), BorderLayout.NORTH);

    JPanel in = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(1, 3, 1, 3);
    gbc.weighty = 0.0;
    gbc.gridx++;
    gbc.weightx = 1.0;

    this.inputPanel = new JPanel();
    this.initInputPanel(1);
    in.add(this.inputPanel, gbc);
    gbc.gridy++;

    this.okButtonPanel = new JPanel(new BorderLayout());
    this.okButtonPanel.add(this.okButton, BorderLayout.CENTER);

    this.timeoutButtonPanel = new JPanel(new BorderLayout());
    this.timeoutButtonPanel.add(this.timeoutButton, BorderLayout.CENTER);

    Insets r =
      this.inputField[0].getBorder() == null ? null
                : this.inputField[0].getBorder().getBorderInsets(
                  this.inputField[0]);
    for (int i = 0; i < this.completionFields.length; i++) {
      gbc.gridx = 0;
      gbc.weightx = 0.0;
      in.add(new JLabel(String.valueOf(i + 1) + ":"), gbc);
      gbc.gridx++;
      gbc.weightx = 1.0;
      this.completionFields[i] = new JTextPane() {

        @Override
        public boolean isFocusTraversable() {

          return false;
        }
      };
      this.completionFields[i].setFont(this.inputField[0].getFont());
      this.completionFields[i].setEditable(false);
      this.completionFields[i].setBackground(in.getBackground());
      if (r != null) {
        this.completionFields[i].setBorder(BorderFactory.createEmptyBorder(
          r.top, r.left,
                  r.bottom, r.right));
      }
      in.add(this.completionFields[i], gbc);
      gbc.gridy++;
    }

    p.add(in, BorderLayout.CENTER);

    JPanel bp = new JPanel();
    if (Platform.isMac()) {
      bp.setLayout(new BorderLayout());
      // bp.add(timeoutButton, BorderLayout.WEST);

      JPanel btns =
        new JPanel(new GridLayout(1, 0, Math.max(4,
          12 - 2 * SimulationDialog.buttonBorderWidth),
                12));
      btns.add(this.timeoutButtonPanel);
      btns.add(this.okButtonPanel);
      bp.add(btns, BorderLayout.EAST);
    }
    else {
      bp.setLayout(new FlowLayout());
      JPanel btns =
        new JPanel(new GridLayout(1, 0, Math.max(4,
          12 - 2 * SimulationDialog.buttonBorderWidth),
                12));
      btns.add(this.okButtonPanel);
      btns.add(this.timeoutButtonPanel);
      bp.add(btns);
    }

    p.add(bp, BorderLayout.SOUTH);

    p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    this.setContentPane(p);

    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowOpened(WindowEvent evt) {

        SimulationDialog.this.focus();
      }


      @Override
      public void windowActivated(WindowEvent evt) {

        SimulationDialog.this.focus();
      }


      @Override
      public void windowClosing(WindowEvent evt) {

        if (SimulationDialog.this.timeoutButton.isEnabled()) {
          SimulationDialog.this.timeoutButton.doClick();
        }
      }
    });

    this.addFocusListener(new FocusAdapter() {

      @Override
      public void focusGained(FocusEvent e) {

        SimulationDialog.this.focus();
      }
    });

    this.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentHidden(ComponentEvent e) {

        synchronized (SimulationDialog.this.modalLock) {
          SimulationDialog.this.modalLock.notifyAll();
        }
      }
    });

    /*
     * KeyListener kl = new KeyAdapter() { public void keyPressed(KeyEvent evt)
     * { inputField.requestFocus(); inputField.dispatchEvent(new
     * KeyEvent(inputField, evt.getID(), evt.getWhen(), evt.getModifiers(),
     * evt.getKeyCode(), evt.getKeyChar())); } }; addKeyListener(kl);
     * p.addKeyListener(kl);
     */

    JMenuBar mbar = new JMenuBar();
    JMenu m = new JMenu("Auto completion");
    for (int i = 0; i < this.completionFields.length; i++) {
      final int index = i;
      JMenuItem item = new JMenuItem("Choose Proposed Word " + (i + 1));
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i, 0));
      item.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          Component c = SimulationDialog.this.getFocusOwner();
          if (c instanceof AutoCompleteField) {
            AutoCompleteField f = (AutoCompleteField)c;
            String s = SimulationDialog.this.completionFields[index].getText();
            f.replaceSelection("");
            String content = f.getText();
            int pos = content.length();
            while ((pos > 0)
              && !Character.isWhitespace(content.charAt(pos - 1))) {
              pos--;
            }
            f.setText(content.substring(0, pos) + s + " ");
          }
        }
      });
      m.add(item);
    }
    mbar.add(m);

    m = new JMenu("Navigation");
    for (int i = 0; i < 10; i++) {
      final int index = i;
      JMenuItem item = new JMenuItem("Edit Alternative " + (i + 1));
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1
        + (i < 9 ? i : -1),
                this.getToolkit().getMenuShortcutKeyMask()));
      item.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          if (index < SimulationDialog.this.inputField.length) {
            SimulationDialog.this.okButton.requestFocus();
            SimulationDialog.this.inputField[index].requestFocus();
          }
        }
      });
      m.add(item);
    }

    m.addSeparator();

    JMenuItem item = new JMenuItem("Next alternative");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
            this.getToolkit().getMenuShortcutKeyMask()));
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Component c = SimulationDialog.this.getFocusOwner();
        for (int i = 0; i < SimulationDialog.this.inputField.length; i++) {
          if (SimulationDialog.this.inputField[i] == c) {
            int n = i + 1;
            if (n >= SimulationDialog.this.inputField.length) {
              n = 0;
            }
            SimulationDialog.this.inputField[n].requestFocus();
            SimulationDialog.this.inputField[n].selectAll();
            break;
          }
          else if (SimulationDialog.this.confField[i] == c) {
            int n = i + 1;
            if (n >= SimulationDialog.this.confField.length) {
              n = 0;
            }
            SimulationDialog.this.confField[n].requestFocus();
            SimulationDialog.this.confField[n].selectAll();
            break;
          }
        }
      }
    });
    m.add(item);

    item = new JMenuItem("Previous alternative");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,
            this.getToolkit().getMenuShortcutKeyMask()));
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Component c = SimulationDialog.this.getFocusOwner();
        for (int i = 0; i < SimulationDialog.this.inputField.length; i++) {
          if (SimulationDialog.this.inputField[i] == c) {
            int n = i - 1;
            if (n < 0) {
              n = SimulationDialog.this.inputField.length - 1;
            }
            SimulationDialog.this.inputField[n].requestFocus();
            SimulationDialog.this.inputField[n].selectAll();
          }
          else if (SimulationDialog.this.confField[i] == c) {
            int n = i - 1;
            if (n < 0) {
              n = SimulationDialog.this.confField.length - 1;
            }
            SimulationDialog.this.confField[n].requestFocus();
            SimulationDialog.this.confField[n].selectAll();
          }
        }
      }
    });
    m.add(item);
    mbar.add(m);

    m = new JMenu("Confidences");

    item = new JMenuItem("Increase Confidence");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
            this.getToolkit().getMenuShortcutKeyMask()));
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Component c = SimulationDialog.this.getFocusOwner();
        for (int i = 0; i < SimulationDialog.this.inputField.length; i++) {
          if (SimulationDialog.this.inputField[i] == c) {
            try {
              Integer confidence =
                Integer.parseInt(SimulationDialog.this.confField[i].getText());
              SimulationDialog.this.confField[i].setText(String.valueOf(Math
                .min(100, confidence + 25)));
            }
                        catch (Exception exn) {
                          SimulationDialog.this.confField[i].setText("100");
                        }
                        break;
                      }
                    }
                  }
    });
    m.add(item);

    item = new JMenuItem("Decrease Confidence");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
            this.getToolkit().getMenuShortcutKeyMask()));
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Component c = SimulationDialog.this.getFocusOwner();
        for (int i = 0; i < SimulationDialog.this.inputField.length; i++) {
          if ((SimulationDialog.this.inputField[i] == c)
            || (SimulationDialog.this.confField[i] == c)) {
            try {
              Integer confidence =
                Integer.parseInt(SimulationDialog.this.confField[i].getText());
              SimulationDialog.this.confField[i].setText(String.valueOf(Math
                .max(0, confidence - 25)));
            }
                        catch (Exception exn) {
                          SimulationDialog.this.confField[i].setText("75");
                        }
                        break;
                      }
                    }
                  }
    });
    m.add(item);
    m.addSeparator();

    int steps = 4;
    for (int i = 0; i < steps; i++) {
      final int confidence = 100 - i * (100 / steps);
      item = new JMenuItem("Set to " + confidence + "%");
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i,
                this.getToolkit().getMenuShortcutKeyMask()));
      item.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          Component c = SimulationDialog.this.getFocusOwner();
          for (int i = 0; i < SimulationDialog.this.inputField.length; i++) {
            if ((SimulationDialog.this.inputField[i] == c)
              || (SimulationDialog.this.confField[i] == c)) {
              SimulationDialog.this.confField[i].setText(String
                .valueOf(confidence));
              break;
            }
          }
        }
      });
      m.add(item);
    }

    /*
     * m.addSeparator(); for (int i=9; i<=12; i++) { JMenuItem item = new
     * JMenuItem("Focus");
     * item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 -1 + i, 0));
     * item.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent evt) { focus(); } }); m.add(item); }
     */

    mbar.add(m);
    this.setJMenuBar(mbar);
    // getLayeredPane().add(mbar);

    mbar.addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {

        SimulationDialog.this.focus();
      }
    });

    this.getRootPane().addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {

        SimulationDialog.this.focus();
      }
    });

    this.allowTimeout(false);

    this.pack();

    GUI.setDefaultButtons(this, this.okButton, this.timeoutButton);
    WindowUtils.setLocationRelativeTo(this, GUI.getWindowForComponent(parent));

  }


  private void initInputPanel(int numAlternatives) {

    this.inputPanel.removeAll();
    this.inputPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.insets = new Insets(2, 1, 2, 1);
    this.inputField = new AutoCompleteField[numAlternatives];
    this.confField = new JTextField[this.inputField.length];

    this.inputPanel.add(new JLabel("Input"), gbc);
    gbc.gridx++;
    gbc.gridwidth = 2;
    this.inputPanel.add(new JLabel("Confidence"), gbc);
    gbc.gridwidth = 1;

    Object returnKeyAction = new Object();
    for (int i = 0; i < this.inputField.length; i++) {
      final AutoCompleteField f =
        this.inputField[i] = new AutoCompleteField(40, false);
      final JTextField cf = this.confField[i] = new JTextField(5);
      gbc.gridy++;
      gbc.gridx = 0;
      this.inputPanel.add(f, gbc);
      gbc.gridx++;
      this.inputPanel.add(cf, gbc);
      gbc.gridx++;
      this.inputPanel.add(new JLabel("%"), gbc);

      InputMap im = f.getInputMap(JComponent.WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), returnKeyAction);
      f.getActionMap().put(returnKeyAction, new AbstractAction("Enter") {

        public void actionPerformed(ActionEvent evt) {

          SimulationDialog.this.okButton.doClick();
        }
      });
      cf.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          SimulationDialog.this.okButton.doClick();
        }
      });

      f.addCompletionListener(new AutoCompleteField.CompletionListener() {

        public void possibleCompletions(String prefix,
            Collection<String> completions,
                        boolean delete)
                {

                  Iterator<String> it = completions.iterator();
                  for (int n = 0; n < SimulationDialog.this.completionFields.length; n++) {
                    if (it.hasNext()) {
                      // completionFields[n].setText(inputField.getText() +
                      // it.next());
                      try {
                        Document d =
                          SimulationDialog.this.completionFields[n]
                            .getDocument();
                        MutableAttributeSet attr = new SimpleAttributeSet();
                        StyleConstants.setForeground(attr,
                          new Color(64, 64, 64));
                        d.remove(0, d.getLength());
                        // d.insertString(0, inputField.getText().substring(0,
                        // inputField.getText().length() - prefix.length()),
                        // attr);
                        StyleConstants
                          .setForeground(attr, new Color(192, 0, 0));
                        d.insertString(d.getLength(), prefix + it.next(), attr);
                      }
                            catch (Exception ignore) {
                              System.out.println(ignore.toString());
                            }
                          }
                          else {
                            SimulationDialog.this.completionFields[n]
                              .setText("");
                          }
                        }

                        if ((completions.size() == 1) && !delete) {
                          String suffix = completions.iterator().next();
                          int pos = f.getSelectionStart();
                          f.replaceSelection(suffix);
                          f.select(pos, pos + suffix.length());
                        }
                      }
      });
    }
  }


  public RecognitionResult show(final Object lock, Collection<String> words,
      int numAlternatives)
        throws Timeout {

    if (SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException(
              "You may not invoke a simulation dialog from the event thread.");
    }

    numAlternatives = Math.max(numAlternatives, 5);

    this.initInputPanel(numAlternatives);

    for (int i = 0; i < this.completionFields.length; i++) {
      this.completionFields[i].setText("");
    }

    List<String> keywords =
      new ArrayList<String>(Arrays.asList(new String[] { "ja", "nein",
                "zur\u00fcck", "stop", "unbekannt" }));

    keywords.retainAll(words);

    for (int i = 0; (i < this.completionFields.length) && (i < keywords.size()); i++) {
      this.completionFields[i].setText(keywords.get(i));
    }

    this.allowTimeout(false);
    for (int i = 0; i < this.inputField.length; i++) {
      this.inputField[i].setWords(words);
      this.inputField[i].setText("");
    }
    this.getContentPane().validate();

    if (numAlternatives > 0) {
      this.inputField[0].selectAll();
    }

    this.setSize(this.getPreferredSize().width,
      this.getPreferredSize().height + 30);

    synchronized (this.modalLock) {
      this.result = null;
      this.timedOut = false;

      try {
        // setAlwaysOnTop(true);

        this.setVisible(true);
        this.toFront();
      } finally {
        synchronized (lock) {
          lock.notifyAll();
        }
      }

      try {
        while (this.isShowing()) {
          this.modalLock.wait();
        }
      } catch (InterruptedException exn) {
      }
    }

    if (this.timedOut) {
      throw new Timeout();
    }
    else {
      return this.result;
    }
  }


  private RecognitionResult collectResult() {

    final List<Utterance> input =
      new ArrayList<Utterance>(this.numAlternatives());
    for (int i = 0; i < this.numAlternatives(); i++) {
      String s = StringTools.normalize(this.getAlternative(i));
      if (s.length() > 0) {
        input
          .add(new AbstractUtterance(s.toLowerCase(), this.getConfidence(i)));
      }
    }

    if (input.size() == 0) {
      input.add(new AbstractUtterance("", 0.0f));
    }

    AbstractRecognitionResult result = new AbstractRecognitionResult() {

      @Override
      public int numAlternatives() {

        return input.size();
      }


      @Override
      public Utterance getAlternative(int alternative) {

        return input.get(alternative);
      }
    };
    result.setShowConfidences(true);
    return result;
  }


  public void allowTimeout(boolean allowTimeout) {

    synchronized (this.timeoutButton) {
      this.timeoutButton.setEnabled(allowTimeout);

      this.okButtonPanel
        .setBorder(BorderFactory.createEmptyBorder(
          SimulationDialog.buttonBorderWidth,
                SimulationDialog.buttonBorderWidth,
          SimulationDialog.buttonBorderWidth,
          SimulationDialog.buttonBorderWidth));
      this.timeoutButtonPanel
        .setBorder(BorderFactory.createEmptyBorder(
          SimulationDialog.buttonBorderWidth,
                SimulationDialog.buttonBorderWidth,
          SimulationDialog.buttonBorderWidth,
          SimulationDialog.buttonBorderWidth));
    }
  }


  public void signalTimeout() {

    synchronized (this.timeoutButton) {
      this.allowTimeout(true);
      this.timeoutButtonPanel.setBorder(BorderFactory.createLineBorder(
        Color.red,
                SimulationDialog.buttonBorderWidth));
    }
  }


  public void focus() {

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {

        if (SimulationDialog.this.isShowing()) {
          Component c = SimulationDialog.this.getFocusOwner();
          if (!(c instanceof AutoCompleteField)
            && (SimulationDialog.this.inputField.length > 0)) {
            c = SimulationDialog.this.inputField[0];
            SimulationDialog.this.okButton.requestFocus();
            try {
              c.requestFocusInWindow();
            }
                        catch (Exception ignore) {
                        }
                        c.requestFocus();
                      }
                    }
                  }
    });
  }


  private int numAlternatives() {

    return this.inputField.length;
  }


  private String getAlternative(int alternative) {

    return this.inputField[alternative].getText();
  }


  private float getConfidence(int alternative) {

    try {
      return Float.parseFloat(this.confField[alternative].getText()) / 100.0f;
    } catch (NumberFormatException exn) {
      return 1.0f;
    }
  }

}