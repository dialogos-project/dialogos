package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.gui.ProgressWheel;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.DefaultStringProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.StringProperty;
import com.clt.speech.G2P;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.util.StringTools;

/**
 * @author dabo
 *
 */
public class SimpleTranscriptionWindow extends JFrame {

    private Thread transcriber = null;
    private final Object transcriptionLock = new Object();

    private StringProperty input, output;
    private EnumProperty<Language> languages;

    private JComponent progress = new ProgressWheel();

    private G2P g2p;

    public SimpleTranscriptionWindow(Component parent, G2P g2p) {

        super("Transcription");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.g2p = g2p;

        Language languages[] = new Language[0];
        if (g2p != null) {
            try {
                languages = g2p.getLanguages();
            } catch (SpeechException exn) {
                this.output.setValue(exn.toString());
            }
        }

        this.languages
                = new DefaultEnumProperty<Language>("language", "Language", null,
                        languages);
        if (languages.length > 0) {
            this.languages.setValue(languages[0]);
        }
        this.input = new DefaultStringProperty("input", "Input", null, "");
        this.output
                = new DefaultStringProperty("output", "Transcription", null, "");
        this.output.setEditable(false);

        this.input.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                synchronized (SimpleTranscriptionWindow.this.transcriptionLock) {
                    SimpleTranscriptionWindow.this.transcriptionLock.notifyAll();
                }
            }
        });

        this.languages.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                synchronized (SimpleTranscriptionWindow.this.transcriptionLock) {
                    SimpleTranscriptionWindow.this.transcriptionLock.notifyAll();
                }
            }
        });

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;

        gbc.gridwidth = 3;
        content.add(new JLabel("Please enter the text to transcribe:"), gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        content.add(new JLabel(this.languages.getName()), gbc);
        gbc.gridy++;
        content.add(new JLabel(this.input.getName()), gbc);
        gbc.gridy++;
        content.add(new JLabel(this.output.getName()), gbc);

        gbc.gridy = 1;
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        content.add(this.languages.createEditor(false), gbc);
        gbc.gridy++;
        content.add(this.input.createEditor(false), gbc);
        gbc.gridy++;
        content.add(this.output.createEditor(false), gbc);

        gbc.gridx++;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(6, 0, 6, 0);
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(16, 16));
        p.add(this.progress);
        this.progress.setVisible(false);
        content.add(p, gbc);

        this.setContentPane(content);
        this.pack();
        this.setLocationRelativeTo(parent);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {

                if (SimpleTranscriptionWindow.this.transcriber == null) {
                    SimpleTranscriptionWindow.this.transcriber
                            = SimpleTranscriptionWindow.this.createTranscriber();
                    SimpleTranscriptionWindow.this.transcriber.start();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {

                synchronized (SimpleTranscriptionWindow.this.transcriptionLock) {
                    SimpleTranscriptionWindow.this.transcriptionLock.notifyAll();
                }
            }
        });
    }

    private Thread createTranscriber() {

        return new Thread(new Runnable() {

            public void run() {

                while (SimpleTranscriptionWindow.this.isShowing()) {
                    if (SimpleTranscriptionWindow.this.g2p == null) {
                        SimpleTranscriptionWindow.this.output
                                .setValue("<No G2P component available");
                    } else if (StringTools.isEmpty(SimpleTranscriptionWindow.this.input
                            .getValue())) {
                        SimpleTranscriptionWindow.this.output.setValue("");
                    } else {
                        final AtomicBoolean b = new AtomicBoolean(true);
                        new Thread(new Runnable() {

                            public void run() {

                                try {
                                    Thread.sleep(200);
                                } catch (Exception ignore) {
                                }
                                synchronized (b) {
                                    if (b.get()) {
                                        SimpleTranscriptionWindow.this.progress
                                                .setVisible(true);
                                    }
                                }
                            }
                        }).start();
                        try {
                            String transcriptions[]
                                    = SimpleTranscriptionWindow.this.g2p.transcribe(
                                            SimpleTranscriptionWindow.this.input.getValue(),
                                            SimpleTranscriptionWindow.this.languages
                                                    .getValue());
                            if ((transcriptions == null) || (transcriptions.length == 0)) {
                                SimpleTranscriptionWindow.this.output
                                        .setValue("<No transcription available>");
                            } else {
                                // output.setValue(transcriptions[0]);
                                SimpleTranscriptionWindow.this.output.setValue(Arrays
                                        .toString(transcriptions));
                            }
                        } catch (Exception exn) {
                            SimpleTranscriptionWindow.this.output.setValue(exn
                                    .toString());
                        }
                        synchronized (b) {
                            b.set(false);
                            SimpleTranscriptionWindow.this.progress
                                    .setVisible(false);
                        }
                    }
                    try {
                        synchronized (SimpleTranscriptionWindow.this.transcriptionLock) {
                            if (SimpleTranscriptionWindow.this.isShowing()) {
                                SimpleTranscriptionWindow.this.transcriptionLock
                                        .wait();
                            }
                        }
                    } catch (Exception ignore) {
                    }

                }
            }
        }, this.g2p.getClass().getName() + " G2P");
    }

}
