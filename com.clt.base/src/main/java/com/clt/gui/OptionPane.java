package com.clt.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.clt.event.DataEntryEvent;
import com.clt.event.DataEntryListener;
import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.menus.AbstractMenuCommander;
import com.clt.gui.menus.CmdMenu;
import com.clt.gui.menus.MenuCommander;
import com.clt.util.AbstractLongCallable;
import com.clt.util.LongCallable;
import com.clt.util.Misc;
import com.clt.util.StringTools;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;


/*
 * OptionPane is not directly derived from JOptionPane, because this would force
 * us to overwrite every single static method in JOptionPane. Otherwise the user
 * could call OptionPane.someOtherMethod() and still get only JOptionPane
 * functionality. By using the private inner class MyOptionPane instead, only
 * explicitly exported functionality is available to the user.
 */
/**
 * An advanced replacement for JOptionPane. Features:
 * <ul>
 * <li>thread-safe! Can be used from any thread, not just the AWT thread.</li>
 * <li>Dialogs are no longer resizable</li>
 * <li>Modal window management enhancements on Microsoft Windows</li>
 * <li>Buttons can be activated via single keypresses (no focus mangling)</li>
 * <li>Display enhancements a la Mac OS StandardAlert</li>
 * </ul>
 *
 * @author Daniel Bobbert
 */
public class OptionPane {

    /**
     * Possible value for
     *
     * <pre>
     * optionType
     * </pre>
     *
     * . Defines which options are given.
     */
    public static final int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
    public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
    public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
    public static final int SAVE_OPTION = 99;

    /**
     * Possible value for
     *
     * <pre>
     * messageType
     * </pre>
     *
     * . Defines the default message icon.
     */
    public static final int PLAIN = JOptionPane.PLAIN_MESSAGE;
    public static final int ERROR = JOptionPane.ERROR_MESSAGE;
    public static final int INFORMATION = JOptionPane.INFORMATION_MESSAGE;
    public static final int WARNING = JOptionPane.WARNING_MESSAGE;
    public static final int QUESTION = JOptionPane.QUESTION_MESSAGE;

    /**
     * This value is returned if the approproate option was chosen from the
     * dialog.
     *
     * @see #confirm(Component, Object)
     */
    public static final int YES = JOptionPane.YES_OPTION;
    public static final int NO = JOptionPane.NO_OPTION;
    public static final int CANCEL = JOptionPane.CANCEL_OPTION;
    public static final int OK = JOptionPane.OK_OPTION;
    public static final int CLOSED = JOptionPane.CLOSED_OPTION;
    public static final int SAVE = OptionPane.YES;
    public static final int DONTSAVE = OptionPane.NO;

    /**
     * Default position for dialogs.
     *
     * @see WindowUtils#CENTER_ON_SCREEN
     * @see WindowUtils#CENTER_ON_PARENT
     * @see WindowUtils#ALERT_POSITION
     */
    public static final int placing = WindowUtils.ALERT_POSITION;

    // placing = Platform.isMac() ?
    // WindowUtils.ALERT_POSITION :
    // WindowUtils.CENTER_ON_PARENT;
    private static final int SOFT_WRAP_LIMIT = 70;
    private static final int HARD_WRAP_LIMIT = 140;

    // **************************************************************************
    // ************
    // Input Dialogs
    // **************************************************************************
    // ************
    public static String input(Object message) {

        return OptionPane.input(null, message);
    }

    public static String input(Component parentComponent, Object message) {

        return OptionPane.input(parentComponent, message, GUI.getString("Input"),
                                OptionPane.QUESTION);
    }

    public static String input(Component parentComponent, Object message,
            String title, int messageType) {

        return (String) OptionPane.input(parentComponent, message, title,
                                         messageType,
                                         null, null, null);
    }

    public static Object input(Component parentComponent, Object message,
            String title, int messageType, Icon icon, Object[] selectionValues,
            Object initialSelectionValue) {

        final MyOptionPane pane = OptionPane.createOptionPane(message, messageType,
                                                              OptionPane.OK_CANCEL_OPTION, icon, null, null);

        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues);
        pane.setInitialSelectionValue(initialSelectionValue);

        final JDialog dialog = pane.createDialog(parentComponent, title);
        /*
     * WindowListener l = new WindowAdapter() { public void
     * windowOpened(WindowEvent e) { pane.selectInitialValue(); } };
     * dialog.addWindowListener(l);
         */
        pane.showDialog(dialog);
        // dialog.removeWindowListener(l);

        Object value = pane.getInputValue();

        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }
        return value;
    }

    // **************************************************************************
    // ************
    // Message Dialogs
    // **************************************************************************
    // ************
    public static void error(Component parentComponent, Object message) {

        OptionPane.message(parentComponent, message, GUI.getString("Error"),
                           OptionPane.ERROR);
    }

    public static void warning(Component parentComponent, Object message) {

        OptionPane.message(parentComponent, message, GUI.getString("Warning"),
                           OptionPane.WARNING);
    }

    public static void message(Component parentComponent, Object message) {

        OptionPane.message(parentComponent, message, GUI.getString("Information"),
                           OptionPane.INFORMATION);
    }

    public static void message(Component parentComponent, Object message,
            String title, int messageType) {

        OptionPane.message(parentComponent, message, title, messageType, null);
    }

    public static void message(Component parentComponent, Object message,
            String title, int messageType, Icon icon) {

        OptionPane.showOptionDialog(parentComponent, message, title,
                                    OptionPane.DEFAULT_OPTION,
                                    messageType, icon, null, null);
    }

    public static void timedMessage(Component parentComponent, Object message,
            final String title, int messageType, final long timeout) {

        final LongCallable<?> a = new AbstractLongCallable<Object>() {

            @Override
            public String getDescription() {

                return title;
            }

            @Override
            protected Object call(ProgressListener l) {

                ProgressEvent evt = new ProgressEvent(this, title, 0, 1, 0);
                if (l != null) {
                    l.progressChanged(evt);
                }
                Misc.sleep(timeout);
                evt.setCurrent(evt.getEnd());
                if (l != null) {
                    l.progressChanged(evt);
                }
                return null;
            }
        };
        new Thread(() -> {
            try {
                a.call();
            } catch (Exception exn) {
                // ignore
            }
        }).start();
        OptionPane.showOptionDialog(parentComponent, message, title,
                                    OptionPane.DEFAULT_OPTION,
                                    messageType, null, null, null, a);
    }

    // **************************************************************************
    // ************
    // Confirm Dialogs
    // **************************************************************************
    // ************
    public static int confirm(Component parentComponent, Object message) {

        return OptionPane.confirm(parentComponent, message, GUI
                                  .getString("ChooseOption"),
                                  OptionPane.YES_NO_CANCEL_OPTION);
    }

    public static int confirm(Component parentComponent, Object message,
            String title, int optionType) {

        return OptionPane.confirm(parentComponent, message, title, optionType,
                                  OptionPane.QUESTION);
    }

    public static int confirm(Component parentComponent, Object message,
            String title, int optionType, int messageType) {

        return OptionPane.confirm(parentComponent, message, title, optionType,
                                  messageType, null);
    }

    public static int confirm(Component parentComponent, Object message,
            String title, int optionType, int messageType, Icon icon) {

        return OptionPane.showOptionDialog(parentComponent, message, title,
                                           optionType,
                                           messageType, icon, null, null);
    }

    // **************************************************************************
    // ************
    // Special Dialogs
    // **************************************************************************
    // ************
    public static String edit(Component parentComponent, Object message,
            String title, String initialValue) {

        return (String) OptionPane.input(parentComponent, message, title,
                                         OptionPane.PLAIN, null,
                                         null, initialValue);
    }

    public static int showCancelDialog(Component parentComponent,
            Object message, LongCallable<?> action) {

        return OptionPane.showOptionDialog(parentComponent, message, GUI
                                           .getString("CancelAction?"), OptionPane.YES_NO_OPTION,
                                           OptionPane.QUESTION, null,
                                           null, null, action);
    }

    // **************************************************************************
    // ************
    // Option Dialog
    // **************************************************************************
    // ************
    public static int showOptionDialog(Component parentComponent,
            Object message, String title, int optionType, int messageType,
            Icon icon, Object[] options, Object initialValue) {

        return OptionPane.showOptionDialog(parentComponent, message, title,
                                           optionType,
                                           messageType, icon, options, initialValue, null);
    }

    public static int showOptionDialog(Component parentComponent,
            Object message, String title, int optionType, int messageType,
            Icon icon, Object[] options, Object initialValue,
            final LongCallable<?> action) {

        if (optionType == OptionPane.SAVE_OPTION) {
            optionType = OptionPane.YES_NO_CANCEL_OPTION;

            if ((options == null) && (initialValue == null)) {
                options = new String[]{GUI.getString("Save"),
                    GUI.getString("DontSave"), GUI.getString("Cancel")};
                initialValue = options[0];
            }
        }

        final MyOptionPane pane = OptionPane.createOptionPane(message, messageType,
                                                              optionType, icon, options, initialValue);

        final JDialog dialog = pane.createDialog(null, title);

        ProgressListener progress = null;
        if (action != null) {
            progress = new ProgressListener() {

                public void progressChanged(ProgressEvent evt) {

                    if (evt.getCurrent() == evt.getEnd()) {
                        action.removeProgressListener(this);
                        pane.setValue(null);
                        dialog.setVisible(false);
                    }
                }
            };
            action.addProgressListener(progress);
        }

        pane.showDialog(dialog);

        Object selectedValue = pane.getValue();

        if (action != null) {
            action.removeProgressListener(progress);
        }

        if (selectedValue == null) {
            return OptionPane.CLOSED;
        }
        if (options == null) {
            if (selectedValue instanceof Integer) {
                return ((Integer) selectedValue).intValue();
            }
            return OptionPane.CLOSED;
        }
        for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
            if (options[counter].equals(selectedValue)) {
                return counter;
            }
        }
        return OptionPane.CLOSED;
    }

    private static MyOptionPane createOptionPane(Object message,
            int messageType, int optionType, Icon icon, Object[] options,
            Object initialValue) {

        if (message instanceof String) {
            message = new Object[]{message, Box.createVerticalStrut(5)};

        } else if (message instanceof Throwable) {
            Throwable exn = (Throwable) message;
            String errorLine = exn.getLocalizedMessage();

            if ((errorLine == null) || (errorLine.length() == 0)) {
                StackTraceElement[] trace = exn.getStackTrace();

                if ((trace != null) && (trace.length > 0)) {
                    for (int i = 0; i < trace.length; i++) {
                        if (trace[i].getClassName().startsWith("com.clt")) {
                            errorLine = trace[i].toString();
                            break;
                        }
                    }
                    if (errorLine.length() == 0) {
                        errorLine = trace[0].toString();
                    }
                }
            }

            message = new String[]{
                GUI.format("UnexpectedException", GUI.getString(message.getClass().getName())),
                errorLine
            };

            if (options == null) {
                JButton b = new JButton("Details");
                options = new Object[]{b, "Ok"};
                initialValue = 1;

                MyOptionPane ret = new MyOptionPane(message, messageType, optionType, icon, options, initialValue);

                StringBuilder stackTraceStr = new StringBuilder();
                for (StackTraceElement e : exn.getStackTrace()) {
                    stackTraceStr.append(e.toString());
                    stackTraceStr.append("\n");
                }
                
                String body = GUI.format("DetailedErrorReportBody", exn.getClass().toString(), exn.toString(), stackTraceStr.toString());
                String header = GUI.getString("DetailedErrorReport");
                String details = GUI.format("ErrorReportBodyForGithub", exn.getClass().toString(), exn.toString(), stackTraceStr.toString()).replaceAll("±", "\n");
                
                b.addActionListener(e -> {
                    JButton bb = new JButton(GUI.getString("Copy"));
                    Object[] infoOptions = new Object[] { bb, "Ok" };
                    
                    bb.addActionListener(ee -> {
                        StringTools.copyToClipboard(details);
                    });
                    
                    JOptionPane.showOptionDialog(ret, body, header, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, infoOptions, 1);
                });

                return ret;

            }
        }

        if (message instanceof String[] && ((String[]) message).length == 2) {
            StaticText s1 = new StaticText(((String[]) message)[0], OptionPane.SOFT_WRAP_LIMIT);
            s1.setFont((Font) UIManager.get("Label.font"));
            StaticText s2 = new StaticText(((String[]) message)[1], OptionPane.SOFT_WRAP_LIMIT);
            s2.setFont(GUI.getSmallSystemFont());

            message = new Object[]{s1, s2, Box.createVerticalStrut(5)};
        }

        return new MyOptionPane(message, messageType, optionType, icon, options, initialValue);
    }

    private static class MyOptionPane extends JOptionPane {

        private Collection<DataEntry> dataEntries;

        public MyOptionPane(Object message, int messageType, int optionType,
                Icon icon, Object[] options, Object initialValue) {

            super(message, messageType, optionType, icon, options, initialValue);

            this.dataEntries = new Vector<DataEntry>();
            this.collectDataEntries(message);
        }

        private void collectDataEntries(Object o) {

            if (o instanceof DataEntry) {
                this.dataEntries.add((DataEntry) o);
            }

            if (o instanceof Object[]) {
                for (int i = 0; i < ((Object[]) o).length; i++) {
                    this.collectDataEntries(((Object[]) o)[i]);
                }
            } else if (o instanceof Container) {
                Container c = (Container) o;
                for (int i = c.getComponentCount() - 1; i >= 0; i--) {
                    this.collectDataEntries(c.getComponent(i));
                }
            }
        }

        private void showDialog(final JDialog dialog) {

            this.selectInitialValue();

            final Object[] optns = this.getOptions();

            if (optns != null) {
                JMenuBar mbar = new JMenuBar();

                MenuCommander commander = new AbstractMenuCommander() {

                    @Override
                    public boolean doCommand(int cmd) {

                        MyOptionPane.this.setValue(optns[cmd]);
                        dialog.setVisible(false);
                        return true;
                    }
                };

                CmdMenu menu = new CmdMenu("Keys", Commands.cmdMenu, commander);

                for (int i = 0; i < optns.length; i++) {
                    if (optns[i] instanceof String) {
                        String s = (String) optns[i];
                        char c = s.charAt(0);
                        boolean single = true;
                        for (int j = i + 1; j < optns.length; j++) {
                            if (optns[j] instanceof String && ((String) optns[j]).charAt(0) == c) {
                                single = false;
                            }
                        }
                        if (single) {
                            menu.addItem(
                                    s,
                                    i,
                                    KeyStroke.getKeyStroke(s.toUpperCase()
                                            .charAt(0), 0)).setEnabled(true);
                        }
                    }
                }

                mbar.add(menu);
                dialog.getLayeredPane().add(mbar);
            }

            try {
                dialog.setResizable(false);
            } catch (ThreadDeath d) {
                // wird bei System.exit() geworfen. Muss laut Java Spezifikation
                // immer weitergegeben werden.
                throw d;
            } catch (Throwable t) {
                // Mac OS X DP3 hat Probleme, wenn das Fenster schon gepackt ist
                // Loesung: peer wegschmeissen, und danach wieder neu packen
                dialog.dispose();
                dialog.setResizable(false);
                dialog.pack();
            }

            Dimension d = dialog.getSize();
            if ((d.width < 350) && (d.height > 100)) {
                dialog.setSize(350, d.height);
            }

            WindowUtils.setLocation(dialog, OptionPane.placing);

            WindowListener l = new WindowAdapter() {

                boolean select(Component c) {

                    if ((c instanceof JScrollPane)
                            && (((JScrollPane) c).getViewport().getView() != null)) {
                        return this.select(((JScrollPane) c).getViewport().getView());
                    } else if (c instanceof JPanel) {
                        JPanel p = (JPanel) c;
                        for (int i = 0; i < p.getComponentCount(); i++) {
                            if (this.select(p.getComponent(i))) {
                                return true;
                            }
                        }
                        return false;
                    } else if (c instanceof JLabel) {
                        return false;
                    } else if (c.isFocusable()) {
                        c.requestFocus();
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public void windowOpened(WindowEvent evt) {

                    Component c = dialog.getFocusOwner();
                    if (!(c instanceof JTextComponent)) {
                        if (MyOptionPane.this.getMessage() instanceof Component) {
                            this.select(((Component) MyOptionPane.this.getMessage()));
                        } else if (MyOptionPane.this.getMessage() instanceof Object[]) {
                            Object[] objects = (Object[]) MyOptionPane.this.getMessage();
                            for (int i = 0; i < objects.length; i++) {
                                if ((objects[i] instanceof Component)
                                        && this.select((Component) objects[i])) {
                                    return;
                                }
                            }
                        }
                    }
                }
            };
            dialog.addWindowListener(l);

            if (this.getMessageType() == OptionPane.ERROR) {
                this.getToolkit().beep();
            }

            GUI.invokeSafeWork(new DialogPresenter(dialog, this.dataEntries));
            dialog.removeWindowListener(l);
        }

        private static class DialogPresenter extends WindowAdapter implements
                Runnable, DataEntryListener {

            JDialog dialog;

            Collection<DataEntry> dataEntries;

            boolean firstTime = true;

            DialogPresenter(JDialog dialog, Collection<DataEntry> dataEntries) {

                this.dialog = dialog;
                this.dataEntries = dataEntries;
            }

            public void run() {

                for (DataEntry e : this.dataEntries) {
                    e.addDataEntryListener(this);
                }

                this.dialog.addWindowListener(this);

                WindowUtils.installModalDialog(this.dialog);
                this.dialog.setVisible(true);

                this.dialog.removeWindowListener(this);
                this.dialog.dispose();

                for (DataEntry e : this.dataEntries) {
                    e.removeDataEntryListener(this);
                }
            }

            public void dataChanged(DataEntryEvent evt) {

                JButton defaultButton = this.dialog.getRootPane().getDefaultButton();
                if (defaultButton != null) {
                    boolean ok = true;
                    for (Iterator<DataEntry> it = this.dataEntries.iterator(); it
                            .hasNext()
                            && ok;) {
                        if (!it.next().dataEntered()) {
                            ok = false;
                        }
                    }
                    defaultButton.setEnabled(ok);
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {

                this.dataChanged(null);
                this.firstTime = true;
            }

            @Override
            public void windowActivated(WindowEvent e) {

                if ((this.dataEntries.size() > 0) && this.firstTime) {
                    this.firstTime = false;
                    this.dataEntries.iterator().next().requestFocus();
                }
            }
        }

        @Override
        public int getMaxCharactersPerLineCount() {

            Object msg = this.getMessage();
            boolean hardwrap = false;
            if (msg instanceof String && ((String) msg).indexOf('\n') >= 0) {
                hardwrap = true;
            } else if (msg instanceof Object[]) {
                Object[] obj = (Object[]) msg;
                for (int i = 0; (i < obj.length) && !hardwrap; i++) {
                    if (obj[i] instanceof String && ((String) obj[i]).indexOf('\n') >= 0) {
                        hardwrap = true;
                    }
                }

            }

            return hardwrap ? OptionPane.HARD_WRAP_LIMIT : OptionPane.SOFT_WRAP_LIMIT;
        }
    }

}
