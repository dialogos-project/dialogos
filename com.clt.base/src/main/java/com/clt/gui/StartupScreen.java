package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;

/**
 * @author dabo
 */
public class StartupScreen extends JDialog {

    private ProgressListener progressListener;

    public StartupScreen(String title) {

        this(title, null);
    }

    public StartupScreen(String title, String version) {

        super((Frame) null, title, false);

        JPanel p = new JPanel(new GridBagLayout());

        final int steps = 300;
        final JLabel status = new JLabel("Starting up...");
        final JProgressBar progress = new JProgressBar(SwingConstants.HORIZONTAL, 0, steps);
        progress.setIndeterminate(true);

        JComponent header = AboutDialog.createHeader(title, version);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        p.add(status, gbc);
        gbc.gridy++;
        p.add(progress, gbc);
        gbc.gridy++;

        p.add(Box.createVerticalStrut(header.getPreferredSize().height / 2), gbc);
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        this.progressListener = new ProgressListener() {

            public void progressChanged(final ProgressEvent evt) {

                final int value;
                final boolean indeterminate;
                if (evt.getEnd() - evt.getStart() > 0) {
                    long current = evt.getCurrent();
                    if (current < evt.getStart()) {
                        current = evt.getStart();
                    } else if (current > evt.getEnd()) {
                        current = evt.getEnd();
                    }

                    value
                            = (int) (((current - evt.getStart()) * steps) / (evt.getEnd() - evt
                            .getStart()));
                    indeterminate = false;
                } else {
                    value = 0;
                    indeterminate = true;
                }

                if ((indeterminate != progress.isIndeterminate())
                        || (value != progress.getValue())) {
                    GUI.invokeAndWait(new Runnable() {

                        public void run() {

                            progress.setIndeterminate(indeterminate);
                            progress.setValue(value);
                        }
                    });
                }
                if (evt.getMessage() != status.getText()) {
                    GUI.invokeAndWait(new Runnable() {

                        public void run() {

                            status.setText(evt.getMessage());
                        }
                    });
                }
            }
        };

        JPanel content = new JPanel(new BorderLayout());
        content.add(header, BorderLayout.NORTH);
        content.add(AboutDialog.createStripes("left"), BorderLayout.WEST);
        content.add(AboutDialog.createStripes("right"), BorderLayout.EAST);
        content.add(p, BorderLayout.CENTER);

        this.setContentPane(content);
        this.pack();
        WindowUtils.setLocation(this, WindowUtils.CENTER_ON_SCREEN);
    }

    public ProgressListener getProgressListener() {

        return this.progressListener;
    }
}
