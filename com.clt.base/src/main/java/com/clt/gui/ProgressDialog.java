package com.clt.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.util.Cancellable;
import com.clt.util.DefaultLongAction;
import com.clt.util.LongAction;
import com.clt.util.LongCallable;

/*
 IMPORTANT: Updates from the progress Thread must be done using
 SwingUtilitites.invokeLater() because Swing is not thread-safe.
 */
public class ProgressDialog extends JDialog implements ProgressListener, Commander {

    private static final int steps = 300;

    private static final int cmdCancel = 0;

    private long initialDelay = 0;

    private LongCallable<?> action;

    private JProgressBar progress;

    private JLabel message;

    private JPanel info;

    private Thread thread;

    private int lastValue = 0;

    private String lastMessage = null;

    private boolean indeterminate = true;

    private CmdButton cancelButton = null;

    private boolean dying = false;

    private Throwable error = null;

    public ProgressDialog(Component parent) {

        this(parent, 800);
    }

    public ProgressDialog(Component parent, long initialDelay) {

        super(JOptionPane.getFrameForComponent(parent), true);
        this.setResizable(false);
        this.initialDelay = initialDelay;

        //this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Container content = this.getContentPane();
        content.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        this.info = new JPanel(new GridLayout(1, 1));
        gbc.weighty = 1;

        content.add(this.info, gbc);

        gbc.gridy++;
        gbc.weighty = 0;
        this.message = new JLabel();
        content.add(this.message, gbc);

        gbc.gridy++;

        this.progress = new JProgressBar(SwingConstants.HORIZONTAL, 0, ProgressDialog.steps) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(ProgressDialog.steps,
                        super.getPreferredSize().height);
            }
        };
        content.add(this.progress, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 10, 10);

        this.cancelButton = new CmdButton(this, ProgressDialog.cmdCancel, GUI.getString("Cancel"));
        content.add(this.cancelButton, gbc);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {

                if (ProgressDialog.this.cancelButton.isEnabled()) {
                    ProgressDialog.this.requestCancel();
                }
            }

            @Override
            public void windowOpened(WindowEvent evt) {

                synchronized (ProgressDialog.this) {
                    if (ProgressDialog.this.dying) {
                        ProgressDialog.this.dispose();
                    }
                }
            }
        });
        
        pack();
        validate();
    }

    public void run(LongAction a) throws InvocationTargetException {

        this.run(a, null);
    }

    public void run(final LongAction a, Component info)
            throws InvocationTargetException {

        LongCallable<?> callable = DefaultLongAction.asCallable(a);
        this.run(callable);
    }

    public <V> V run(LongCallable<? extends V> a)
            throws InvocationTargetException {

        return this.run(a, null);
    }

    public <V> V run(final LongCallable<? extends V> action, Component info)
            throws InvocationTargetException {

        this.action = action;
        this.error = null;

        String description = action.getDescription();
        if (description.endsWith("...")) {
            description = description.substring(0, description.length() - 3);
        }

        this.setTitle(description);

        this.indeterminate = true;
        this.lastValue = 0;
        this.lastMessage = description + "...";

        this.info.removeAll();
        if (info != null) {
            this.info.add(info);
        }

        this.cancelButton.setEnabled(action instanceof Cancellable
                ? ((Cancellable) action).canCancel()
                : false);

        final Result<V> result = new Result<V>();
        final Object threadStartLock = new Object();
        this.thread = new Thread("com.clt.gui.ProgressDialog action thread") {

            @Override
            public void run() {

                synchronized (threadStartLock) {
                    threadStartLock.notifyAll();
                }
                action.addProgressListener(ProgressDialog.this);
                try {
                    result.set(action.call());
                } catch (OutOfMemoryError e) {
                    System.gc();
                    ProgressDialog.this.error = e;
                } catch (ThreadDeath e) {
                    throw e;
                } catch (Throwable t) {
                    ProgressDialog.this.error = t;
                }
                action.removeProgressListener(ProgressDialog.this);

                // dispose on the Swing thread to prevent deadlocks
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {

                        synchronized (ProgressDialog.this) {
                            ProgressDialog.this.dying = true;
                            ProgressDialog.this.dispose();
                        }
                    }
                });
            }
        };
        this.thread.setPriority(Thread.MIN_PRIORITY); // das sorgt fuer ein
        // fluessiges Update der
        // Oberflaeche
        // ohne dass die Aktion langsamer dadurch wird

        synchronized (threadStartLock) {
            this.thread.start();
            try {
                threadStartLock.wait();
            } catch (InterruptedException ignore) {
                // we got interrupted. so what?
            }
        }
        if (this.initialDelay > 0) {
            try {
                this.thread.join(this.initialDelay);
            } catch (InterruptedException ignore) {
                // we got interrupted. so what?
            }
        }

        // open on the swing thread to avoid concurrency exceptions in swing
        GUI.invokeAndWait(new Runnable() {

            public void run() {

                if (!ProgressDialog.this.dying
                        && (((float) ProgressDialog.this.lastValue / (float) ProgressDialog.steps) < 0.8f)) {
                    ProgressDialog.this.updater.run();
                    ProgressDialog.this.pack();
                    WindowUtils.setLocationRelativeTo(ProgressDialog.this,
                            ProgressDialog.this.getParent());
                    WindowUtils.installModalDialog(ProgressDialog.this);
                    ProgressDialog.this.setVisible(true);
                }
            }
        });

        try {
            this.thread.join();
        } catch (InterruptedException ignore) {
            // we got interrupted. so what?
        }

        if (this.error != null) {
            throw new InvocationTargetException(this.error);
        }
        return result.get();
    }

    // requestCancel wird nur aus dem EventThread aufgerufen (nach Button-Klick)
    // daher brauchen wir hier kein "invokeLater"
    private void requestCancel() {

        if (this.action instanceof Cancellable) {
            Cancellable c = (Cancellable) this.action;
            int result = OptionPane.YES;

            if (c.getCancelConfirmationPrompt() != null) {
                result
                        = OptionPane.showCancelDialog(this, c.getCancelConfirmationPrompt(),
                                this.action);
            }

            if (result == OptionPane.YES) {
                this.message.setText(GUI.getString("Aborting") + "...");
                this.progress.setIndeterminate(true);
                this.dying = true;
                if (this.cancelButton != null) {
                    this.cancelButton.setEnabled(false);
                }
                c.cancel();
            }
        }
    }

    public boolean doCommand(int cmd) {

        switch (cmd) {
            case cmdCancel:
                this.requestCancel();
                break;
            default:
                return false;
        }
        return true;
    }

    // statusChanged wird von unserem eigenen Thread aufgerufen
    // daher muessen alle Swing-calls eingepackt werden in "invokeLater"
    public void progressChanged(final ProgressEvent e) {
        if (!this.dying) {
            int value;
            if (e.getEnd() - e.getStart() > 0) {
                long current = e.getCurrent();
                if (current < e.getStart()) {
                    current = e.getStart();
                } else if (current > e.getEnd()) {
                    current = e.getEnd();
                }

                value = (int) (((current - e.getStart()) * ProgressDialog.steps) / (e.getEnd() - e.getStart()));
                this.indeterminate = false;
            } else {
                value = 0;
                this.indeterminate = true;
            }
            this.lastValue = value;
            if (e.getMessage() != null) {
                this.lastMessage = e.getMessage();
            }
            this.update();
        }
    }

    private void update() {

        if (this.isShowing()
                && ((this.lastMessage != this.message.getText()) || (this.lastValue != this.progress.getValue()))) {
            SwingUtilities.invokeLater(this.updater);
        }
    }

    private Runnable updater = new Runnable() {
        public void run() {
            ProgressDialog.this.message.setText(ProgressDialog.this.lastMessage);
            ProgressDialog.this.progress.setIndeterminate(ProgressDialog.this.indeterminate);
            ProgressDialog.this.progress.setValue(ProgressDialog.this.lastValue);

            if (ProgressDialog.this.action instanceof Cancellable) {
                ProgressDialog.this.cancelButton.setEnabled(((Cancellable) ProgressDialog.this.action).canCancel());
            }
        }
    };

    private static class Result<V> {

        private V value;

        public void set(V value) {

            this.value = value;
        }

        public V get() {

            return this.value;
        }
    }
}
