package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.clt.util.Platform;

public class InputDialog extends JDialog implements Commander {

    private static final long serialVersionUID = 1L;

    private static final int TEXTFIELDSIZE = 24;

    public static final int RESULT_OK = 1, RESULT_CANCEL = 2;

    private int result = 0;

    private CmdButton buttonOK, buttonCancel;

    private JTextField[] input;

    public InputDialog(Component parent, String title, String prompt, String[] labels) {
        this(parent, title, prompt, labels, true);
    }

    public InputDialog(Component parent, String title, String prompt, String[] labels, String[] values) {
        this(parent, title, prompt, labels, values, InputDialog.bArray(true,
                labels.length));
    }

    public InputDialog(Component parent, String title, String prompt, String[] labels, boolean showChars) {
        this(parent, title, prompt, labels, InputDialog.bArray(showChars,
                labels.length));
    }

    public InputDialog(Component parent, String title, String prompt, String[] labels, boolean[] showChars) {
        this(parent, title, prompt, labels, null, showChars);
    }

    public InputDialog(Component parent, String title, String prompt, String[] labels, String[] values, boolean[] showChars) {
        super(GUI.getFrameForComponent(parent), title, true);

        this.init(parent, prompt, labels, values, showChars);
    }

    private static boolean[] bArray(boolean value, int size) {

        boolean[] tmp = new boolean[size];
        for (int i = 0; i < size; i++) {
            tmp[i] = value;
        }
        return tmp;
    }

    private void init(Component parent, String prompt, String[] labels, String[] values, boolean[] showChars) {
        if (labels.length != showChars.length) {
            throw new IllegalArgumentException();
        }

        if ((values != null) && (values.length != labels.length)) {
            throw new IllegalArgumentException();
        }

        this.setResizable(false);
        //this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {

                InputDialog.this.doCommand(InputDialog.RESULT_CANCEL);
            }

            @Override
            public void windowActivated(WindowEvent evt) {

                if (!(InputDialog.this.getFocusOwner() instanceof JTextField)
                        && (InputDialog.this.input.length > 0)) {
                    InputDialog.this.input[0].requestFocus();
                }
            }
        });

        JPanel Inhalt = new JPanel();
        Inhalt.setBorder(new EmptyBorder(5, 10, 10, 10));

        Inhalt.setLayout(new BorderLayout(0, 10));

        if (prompt != null) {
            Inhalt.add(new StaticText(prompt), BorderLayout.NORTH);
            // Inhalt.add(new JLabel(prompt), "North");
        }

        this.input = new JTextField[labels.length];

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.ipadx = 10;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i + 1;

            gbc.gridx = 1;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            inputPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 2;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            if (showChars[i]) {
                this.input[i] = new JTextField(InputDialog.TEXTFIELDSIZE);
            } else {
                this.input[i] = new JPasswordField(InputDialog.TEXTFIELDSIZE);
            }

            if (values != null) {
                this.input[i].setText(values[i]);
            }

            this.input[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    InputDialog.this.doCommand(InputDialog.RESULT_OK);
                }
            });

            inputPanel.add(this.input[i], gbc);
        }

        Inhalt.add(inputPanel, "Center");

        this.buttonOK
                = new CmdButton(this, InputDialog.RESULT_OK, GUI.getString("OK"));
        this.buttonCancel
                = new CmdButton(this, InputDialog.RESULT_CANCEL, GUI.getString("Cancel"));

        if (Platform.isWindows()) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JPanel Innen = new JPanel(new GridLayout());
            ((GridLayout) (Innen.getLayout())).setHgap(10);
            Innen.add(this.buttonOK);
            Innen.add(this.buttonCancel);
            buttonPanel.add(Innen);
            Inhalt.add(buttonPanel, "South");
        } else {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JPanel Innen = new JPanel(new GridLayout());
            ((GridLayout) (Innen.getLayout())).setHgap(10);
            Innen.add(this.buttonCancel);
            Innen.add(this.buttonOK);
            buttonPanel.add(Innen);
            Inhalt.add(buttonPanel, "South");
        }

        this.setContentPane(Inhalt);

        this.pack();

        WindowUtils.setLocationRelativeTo(this, parent);
        GUI.setDefaultButtons(this, this.buttonOK, this.buttonCancel);
        WindowUtils.installModalDialog(this);

        this.setVisible(true);
    }

    public int getResult() {

        return this.result;
    }

    public String getInput(int i) {

        return this.input[i].getText();
    }

    public boolean doCommand(int cmdNum) {

        this.result = cmdNum;
        this.dispose();
        return true;
    }

    /**
     * Creates a dialog for entering a password and returns that password. After
     * the users entered the password, the dialog closes again.
     *
     * @param prompt a string that is displayed in the dialog
     * @param confirm if true, the user is prompted to enter the password two
     * times
     * @return the password entered by the user
     */
    public static String getPassword(String prompt, boolean confirm) {

        InputDialog d = null;

        if (confirm) {
            d
                    = new InputDialog(null, GUI.getString("EnterPassword"), prompt,
                            new String[]{
                                GUI.getString("EnterPassword") + ':',
                                GUI.getString("RetypePassword") + ':'},
                            false);

            if (d.getResult() == InputDialog.RESULT_OK) {
                String s = d.getInput(0);
                if (s.equals(d.getInput(1))) {
                    return s;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            d
                    = new InputDialog(null, GUI.getString("EnterPassword"), prompt,
                            new String[]{GUI
                                        .getString("EnterPassword") + ':'}, false);

            if (d.getResult() == InputDialog.RESULT_OK) {
                return d.getInput(0);
            } else {
                return null;
            }
        }
    }

}
