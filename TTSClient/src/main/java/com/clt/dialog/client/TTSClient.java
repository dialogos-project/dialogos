/*
 * @(#)TTSClient.java
 * Created by Daniel Bobbert
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.clt.event.ProgressListener;
import com.clt.gui.ProgressDialog;
import com.clt.gui.border.GroupBorder;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.properties.BooleanProperty;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.FloatProperty;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.properties.StringProperty;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;
import com.clt.speech.G2P;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.AbstractSynthesizer;
import com.clt.util.AbstractLongAction;
import com.clt.util.UserCanceledException;

public abstract class TTSClient<SynthesizerType extends AbstractSynthesizer>
    extends SpeechClient {

  private TextPane output;
  private JTextField test;
  private JPanel optionsPanel;

  private List<JLabel> externalOutputFields = new LinkedList<JLabel>();
  private SynthesizerType synthesizer = null;

  private String[] quickSpeak = new String[9];

  private BooleanProperty mute = new DefaultBooleanProperty("mute", "Mute",
        "Check this option to mute sound output", false);


  @Override
  public Map<String, String> getAcceptedInput() {

    Map<String, String> map = new LinkedHashMap<String, String>();

    map.put("<string>", "Speak the given sentence.");
    map.put("<list>", "Speak the given list of sentences.");

    if (this.synthesizer != null) {
      Property<?>[] properties = this.synthesizer.getProperties();
      for (int i = 0; i < properties.length; i++) {
        String key = "{ " + properties[i].getID() + " = ";
        if (properties[i] instanceof IntegerProperty) {
          key += "<int>";
        }
        else if (properties[i] instanceof FloatProperty) {
          key += "<real>";
        }
        else {
          key += "<string>";
        }
        key += " }";
        String value = properties[i].getDescription();
        if (value == null) {
          value = "Set the " + properties[i].getName() + " of the synthesizer.";
        }
        map.put(key, value);
      }
    }

    return map;
  }


  public final void speak(String text)
      throws SpeechException {

    for (JLabel externalOutput : this.externalOutputFields) {
      externalOutput.setText(text);
    }

    if (!this.mute.getValue() && this.canSpeak()) {
      this.synthesizer.speak(text);
    }
  }


  protected SynthesizerType getSynthesizer() {

    return this.synthesizer;
  }


  @Override
  protected G2P getG2P() {

    return this.getSynthesizer();
  }


  @Override
  public String getDescription() {

    StringBuilder b = new StringBuilder();

    b.append(this.getDescriptionImpl());
    b.append("\n");

    b
      .append("Type some text into the \"Try\" field and hit <enter> to synthesize text with the current settings. ");
    b
      .append("Hold the shift key while pressing <enter> to save the synthesized output to a file.");

    return b.toString();
  }


  protected abstract String getDescriptionImpl();


  protected abstract SynthesizerType createSynthesizer(
      ProgressListener progress,
            Properties properties)
      throws Exception;


  protected boolean canSpeak() {

    return this.synthesizer != null;
  }


  @Override
  public boolean dispose() {

    try {
      if (super.dispose()) {
        if (this.synthesizer != null) {
          this.synthesizer.dispose();
          this.synthesizer = null;
        }
        return true;
      }
    } catch (Exception exn) {
      this.error(exn);
    }

    return false;
  }


  @Override
  public JComponent createUI() {

    JComponent c = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = gbc.gridy = 0;
    gbc.weighty = 1.0;
    gbc.weightx = 1.0;

    this.output = new TextPane(this);
    c.add(this.output, gbc);

    this.optionsPanel = new JPanel(new GridBagLayout());
    this.optionsPanel.setBorder(new GroupBorder("Synthesizer settings"));
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weighty = 0;
    c.add(this.optionsPanel, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weighty = 0;

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new GroupBorder("Try:"));

    this.test = new JTextField();
    this.test.setEnabled(false);
    this.test.setEditable(false);
    this.test.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        TTSClient.this.guiSpeak(TTSClient.this.getUI(), TTSClient.this
          .getSynthesizer(), TTSClient.this.test.getText(), evt.getModifiers());
      }
    });
    this.test.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent evt) {

        if ((evt.getKeyCode() == KeyEvent.VK_ENTER)
                        && ((evt.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)) {
          TTSClient.this.test.postActionEvent();
        }
      }
    });

    p.add(this.test, BorderLayout.CENTER);
    c.add(p, gbc);

    return c;
  }


  public void guiSpeak(Component parent, final AbstractSynthesizer synthesizer,
            final String text, int modifiers) {

    if ((synthesizer == null) || (text == null) || (text.trim().length() == 0)) {
      return;
    }

    try {
      synthesizer.stop();

      File file = null;
      if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
        file =
          this.getFileChooser().standardPutFile(parent, "Output.wav",
            "Save output to file");
        if (file == null) {
          return;
        }
      }

      final File outputFile = file;
      new ProgressDialog(parent, 100).run(new AbstractLongAction() {

        @Override
        public String getDescription() {

          return outputFile == null ? "Speaking..." : "Generating file...";
        }


        @Override
        public void cancel() {

          try {
            synthesizer.stop();
          }
                    catch (Exception exn) {
                      TTSClient.this.error(exn);
                    }
                  }


        @Override
        public boolean canCancel() {

          return true;
        }


        @Override
        public void run(ProgressListener l)
            throws SpeechException, IOException {

          if (TTSClient.this.canSpeak()) {
            if (outputFile == null) {
              synthesizer.speak(text);
            }
            else {
              synthesizer.synthesizeToFile(text, outputFile);
            }
          }
        }
      });
    } catch (InvocationTargetException exn) {
      this.error(exn.getTargetException());
    } catch (Exception exn) {
      this.error(exn);
    }
  }


  public void addExternalOutput(JLabel externalOutput) {

    this.externalOutputFields.add(externalOutput);
  }


  @Override
  public void error(Throwable t) {

    t.printStackTrace();
    this.output.println(t.toString());
  }


  @Override
  public boolean initialize(ProgressListener progress, final String[] args,
      Properties properties) {

    try {
      this.synthesizer = this.createSynthesizer(progress, properties);
      if (this.synthesizer == null) {
        return false;
      }

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = gbc.gridy = 0;
      gbc.insets = new Insets(3, 3, 3, 3);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      PropertySet<Property<?>> ps =
        new PropertySet<Property<?>>(this.synthesizer.getProperties());
      ps.add(this.mute);

      for (Property<?> p : ps) {
        String initialValue = properties.getProperty(p.getID());
        if (initialValue == null) {
          initialValue = properties.getProperty(p.getName());
        }

        try {
          if (initialValue != null) {
            p.setValueFromString(initialValue);
          }
        } catch (Exception ignore) {
        }
      }

      ps.fillPropertyPanel(this.optionsPanel, gbc, false);
      this.getUI().invalidate();
      this.getUI().validate();

      for (int i = 0; i < this.quickSpeak.length; i++) {
        this.quickSpeak[i] = properties.getProperty("Quick" + (i + 1), null);
      }

      return true;
    } catch (ThreadDeath d) {
      throw d;
    } catch (Throwable exn) {
      if (!(exn instanceof UserCanceledException)) {
        this.error(exn);
      }
      return false;
    }
  }


  @Override
  protected void initMenu(CmdMenu menu) {

    CmdMenu quickies = menu.addSubMenu("Quick Speak");
    for (int i = 0; i < this.quickSpeak.length; i++) {
      final int n = i;
      quickies.add(new CmdMenuItem("Quick Speak " + (i + 1), KeyEvent.VK_1 + i,
                new Runnable() {

                  public void run() {

                    if (TTSClient.this.quickSpeak[n] != null) {
                      TTSClient.this.test.setText(TTSClient.this.quickSpeak[n]);
                      TTSClient.this.test.selectAll();
                      TTSClient.this.guiSpeak(TTSClient.this.getUI(),
                        TTSClient.this.getSynthesizer(),
                        TTSClient.this.quickSpeak[n], 0);
                    }
                  }
                }));
    }

    super.initMenu(menu);
  }


  protected void println(String s) {

    this.output.println(s);
  }


  protected void println(String s, Color c) {

    this.output.println(s, c);
  }


  @Override
  public void updateMenus() {

    this.test.setEnabled(this.canSpeak());
    this.test.setEditable(this.canSpeak());

    super.updateMenus();
  }


  @Override
  public void output(Value value) {

    // output.println("Received: " + value);
    try {
      if (value instanceof StringValue) {
        final String text = ((StringValue)value).getString();
        this.println(text);
        this.speak(text);
      }
      else if (value instanceof ListValue) {
        ListValue l = (ListValue)value;
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
          if (l.get(i) instanceof StringValue) {
            b.append(((StringValue)l.get(i)).getString());
          }
          else {
            b.append(l.get(i).toString());
          }
          b.append("\n");
        }
        this.println(b.toString());
        this.speak(b.toString());
      }
      else if (value instanceof StructValue) {
        StructValue data = (StructValue)value;
        if (this.synthesizer != null) {
          if (data.containsLabel("language")) {
            String language =
              ((StringValue)data.getValue("language")).getString();
            this.synthesizer.setVoice(this.synthesizer.findVoice(new Language(
              language)));
          }

          Property<?>[] properties = this.synthesizer.getProperties();
          for (int i = 0; i < properties.length; i++) {
            if (data.containsLabel(properties[i].getID())) {
              Property<?> p = properties[i];
              Value val = data.getValue(properties[i].getID());

              if (val instanceof StringValue) {
                p.setValueFromString(((StringValue)val).getString());
              }
              else if (p instanceof IntegerProperty) {
                ((IntegerProperty)p).setValue((int)((IntValue)val).getInt());
              }
              else if (p instanceof FloatProperty) {
                ((FloatProperty)p).setValue((float)((RealValue)val).getReal());
              }
              else if (p instanceof StringProperty) {
                ((StringProperty)p).setValue(val.toString());
              }
            }
          }
        }

        if (data.containsLabel(this.mute.getID())) {
          this.mute.setValue(((BoolValue)data.getValue(this.mute.getID()))
            .getBool());
        }

        if (data.containsLabel("echo")) {
          try {
            this.send(data.getValue("echo"));
          } catch (Exception ignore) {
          }
        }
      }
    } catch (Exception exn) {
      this.error(exn);
    }
  }


  @Override
  public Value rpc(String procedure, Value[] arguments)
      throws Exception {

    if (procedure.equals("echo") && (arguments.length == 1)) {
      return arguments[0];
    }
    else if (procedure.equals("speak") && (arguments.length == 1)) {
      if (arguments[0] instanceof StringValue) {
        this.speak(((StringValue)arguments[0]).getString());
      }
      else if (arguments[0] instanceof ListValue) {
        ListValue l = (ListValue)arguments[0];
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
          if (l.get(i) instanceof StringValue) {
            b.append(((StringValue)l.get(i)).getString());
          }
          else {
            b.append(l.get(i).toString());
          }
          b.append("\n");
        }
        this.speak(b.toString());
      }
      else {
        throw new IllegalArgumentException(
          "Wrong type of argument in function speak(): "
                      + arguments[0]);
      }
      return null;
    }
    else if (procedure.equals("setParameter") && (arguments.length == 2)) {
      String param = ((StringValue)arguments[0]).getString();

      if (param.equals(this.mute.getID())) {
        this.mute.setValue(((BoolValue)arguments[1]).getBool());
      }
      else if (this.synthesizer != null) {
        Property<?>[] properties = this.synthesizer.getProperties();
        for (int i = 0; i < properties.length; i++) {
          if (properties[i].getID().equals(param)) {
            Property<?> p = properties[i];
            Value val = arguments[1];

            if (val instanceof StringValue) {
              p.setValueFromString(((StringValue)val).getString());
            }
            else if (p instanceof IntegerProperty) {
              ((IntegerProperty)p).setValue((int)((IntValue)val).getInt());
            }
            else if (p instanceof FloatProperty) {
              ((FloatProperty)p).setValue((float)((RealValue)val).getReal());
            }
            else if (p instanceof StringProperty) {
              ((StringProperty)p).setValue(val.toString());
            }
          }
        }
      }
      return null;
    }
    else {
      return super.rpc(procedure, arguments);
    }
  }


  @Override
  public void sessionStarted() {

  }


  @Override
  public void reset() {

    try {
      if (this.synthesizer != null) {
        this.synthesizer.stop();
      }
    } catch (Exception exn) {
      this.error(exn);
    }
  }
}