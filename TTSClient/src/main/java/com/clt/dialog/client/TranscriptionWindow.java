/*
 * @(#)TranscriptionWindow.java
 * Created on 17.05.2006 by dabo
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

package com.clt.dialog.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import com.clt.event.DocumentChangeListener;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.Property;
import com.clt.speech.LanguageDetector;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.AbstractSynthesizer;
import com.clt.speech.tts.Voice;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class TranscriptionWindow<SynthesizerType extends AbstractSynthesizer>
    // extends JDialog
    extends JFrame {

  private static final String NO_TRANSCRIPTION = "<no transcription available>";

  private JTextField input;
  private JTextField output;
  private JTextField convertedOutput;

  final AbstractSynthesizer transcriber;


  public TranscriptionWindow(final TTSClient<SynthesizerType> parent,
                               final SynthesizerType transcriber)
      throws SpeechException {

    // super(GUI.getFrameForComponent(parent), "Transcribe", true);
    super("Transcription");
    this.setResizable(false);

    this.transcriber = transcriber;

    final EnumProperty<Voice> tvoice =
      new DefaultEnumProperty<Voice>("tvoice",
            "Foreign Transcription Voice", null, transcriber
              .getAvailableVoices());
    final EnumProperty<Voice> gvoice = new DefaultEnumProperty<Voice>("gvoice",
            "Guessed Voice", null, transcriber.getAvailableVoices());
    final EnumProperty<Voice> svoice =
      new DefaultEnumProperty<Voice>("svoice", "Target Voice",
            null, transcriber.getAvailableVoices());

    tvoice.setValue(transcriber.findVoice(Locale.ENGLISH));
    svoice.setValue(transcriber.findVoice(Locale.GERMAN));
    gvoice.setEditable(false);

    this.input = new JTextField(20);
    this.output = new JTextField(20);
    this.convertedOutput = new JTextField(20);

    this.output.setEditable(false);
    this.convertedOutput.setEditable(false);

    final ChangeListener update = new ChangeListener() {

      private String showTranscription(String text, Voice voice, JTextField f) {

        StringBuilder converted = new StringBuilder();
        try {
          String sampa[] = transcriber.transcribe(text, voice.getLanguage());
          if ((sampa == null) || (sampa.length == 0)) {
            f.setText(TranscriptionWindow.NO_TRANSCRIPTION);
          }
          else {
            StringBuilder b = new StringBuilder();
            String[] result = StringTools.split(sampa[0], ' ');
            for (int i = 0; i < result.length; i++) {
              if (i > 0) {
                b.append(' ');
              }
              b.append(result[i]);

              converted.append("<phon sampa=\"" + result[i] + "\">x</phon> ");
            }
            f.setText(b.toString());
          }
        }
                catch (Exception exn) {
                  f.setText("<" + exn.getLocalizedMessage() + ">");
                }
                return converted.toString();
              }


      public void stateChanged(ChangeEvent ignore) {

        if (transcriber instanceof LanguageDetector) {
          try {
            Voice optimalVoice =
              transcriber.findVoice(((LanguageDetector)transcriber)
                .detectLanguage(TranscriptionWindow.this.input.getText()));
            gvoice.setValue(optimalVoice);
          }
                    catch (Exception exn) {
                      gvoice.setValue(null);
                    }
                  }

                  String sampa =
                    this.showTranscription(TranscriptionWindow.this.input
                      .getText(), tvoice.getValue(),
                      TranscriptionWindow.this.output);
                  this.showTranscription(sampa, svoice.getValue(),
                    TranscriptionWindow.this.convertedOutput);
                }
    };
    GUI.addDocumentChangeListener(this.input, new DocumentChangeListener() {

      @Override
      public void documentChanged(DocumentEvent evt) {

        update.stateChanged(null);
      }
    });
    this.input.addFocusListener(new FocusAdapter() {

      @Override
      public void focusGained(FocusEvent e) {

        update.stateChanged(null);
      }
    });

    final Collection<Property<?>> properties = new LinkedList<Property<?>>();

    properties.add(tvoice);
    properties.add(svoice);

    for (Property<?> property : properties) {
      property.addChangeListener(update);
    }

    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosed(WindowEvent evt) {

        for (Property<?> property : properties) {
          property.removeChangeListener(update);
        }
      }
    });

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(3, 3, 3, 3);
    p.add(new JLabel("Input text:"), gbc);
    gbc.gridx++;
    p.add(this.input, gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    p.add(new JLabel(gvoice.getName()), gbc);
    gbc.gridx++;
    p.add(gvoice.createEditor(false), gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    p.add(new JLabel(tvoice.getName()), gbc);
    gbc.gridx++;
    p.add(tvoice.createEditor(false), gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    p.add(new JLabel("Foreign Transcription"), gbc);
    gbc.gridx++;
    p.add(this.output, gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    p.add(new JLabel(svoice.getName()), gbc);
    gbc.gridx++;
    p.add(svoice.createEditor(false), gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    p.add(new JLabel("Converted Target Transcription"), gbc);
    gbc.gridx++;
    p.add(this.convertedOutput, gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    JButton b = new JButton("Speak");

    ActionListener al = new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        String phons = TranscriptionWindow.this.output.getText();
        if (!phons.equals(TranscriptionWindow.NO_TRANSCRIPTION)) {
          String words[] = phons.split(" ");
          if (words != null) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
              if (words[i].length() > 0) {
                b.append("<phon sampa=\"");
                b.append(words[i]);
                b.append("\">x</phon> ");
              }
            }
            try {
              transcriber.setVoice(svoice.getValue());

              parent.guiSpeak(TranscriptionWindow.this, transcriber, b
                .toString(),
                                evt.getModifiers());
              // transcriber.speak(b.toString());

              // speak causes a property update that will
              // trigger a transcription.
              TranscriptionWindow.this.output.setText(phons);
            }
                        catch (Exception exn) {
                          OptionPane.error(TranscriptionWindow.this, exn);
                        }
                      }
                    }
                  }
    };
    b.addActionListener(al);
    this.input.addActionListener(al);
    this.input.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent evt) {

        if ((evt.getKeyCode() == KeyEvent.VK_ENTER)
                        && ((evt.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)) {
          TranscriptionWindow.this.input.postActionEvent();
        }
      }
    });

    this.output.addActionListener(al);
    this.convertedOutput.addActionListener(al);
    p.add(b, gbc);

    p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    this.getContentPane().add(p);
    this.pack();
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.setLocationRelativeTo(parent.getUI());
  }
}
