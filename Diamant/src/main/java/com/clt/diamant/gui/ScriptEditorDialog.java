package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.Collections;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import com.clt.diamant.Grammar;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.Functions;
import com.clt.event.DocumentChangeListener;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.table.TableEditor;
import com.clt.script.Script;
import com.clt.script.ScriptEditor;
import com.clt.script.UndoRedoTextComponent;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class ScriptEditorDialog extends JDialog {

    private ScriptEditor editor;
    private String result;
    private String name;

    private ScriptEditorDialog(Component parent, String windowTitle, ScriptEditor.Type type, String text, String name) {
        super(GUI.getFrameForComponent(parent), windowTitle, true);

        this.result = text;

        this.editor = new ScriptEditor(type);
        if (text != null) {
            this.editor.setText(text);
        } else {
            this.editor.setText("");
        }
//		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.setResizable(true);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // final JPanel panel = new JPanel(new BorderLayout());
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton undo = new JButton("undo");
        JButton redo = new JButton("redo");
        buttonPanel.add(undo);
        buttonPanel.add(redo);
        p.add(buttonPanel, BorderLayout.NORTH);

        this.name = name;
        if (name != null) {
            JPanel north = new JPanel(new BorderLayout(12, 12));
            north.add(new JLabel(Resources.getString("Name")), BorderLayout.WEST);
            final JTextComponent nameField = new JTextField(20);
            nameField.setText(name);
            nameField.selectAll();
            north.add(nameField, BorderLayout.CENTER);
            GUI.addDocumentChangeListener(nameField, new DocumentChangeListener() {

                @Override
                public void documentChanged(DocumentEvent evt) {
                    ScriptEditorDialog.this.name = nameField.getText();
                }
            });
            p.add(north, BorderLayout.NORTH);
        }

        p.add(new UndoRedoTextComponent(this.editor), BorderLayout.CENTER);
        CmdButton ok = new CmdButton(() -> {
            ScriptEditorDialog.this.result = ScriptEditorDialog.this.editor.getText();
            ScriptEditorDialog.this.dispose();
        }, Resources.getString("OK"));
        CmdButton cancel = new CmdButton(ScriptEditorDialog.this::dispose, Resources.getString("Cancel"));
        p.add(TableEditor.createButtonPanel(new JButton[0], new JButton[]{cancel, ok}), BorderLayout.SOUTH);

        this.setContentPane(p);
        GUI.setDefaultButtons(this, ok, cancel);
        GUI.assignMnemonics(this.getContentPane());
        this.pack();
        this.setSize(400, 300);
        this.setLocationRelativeTo(parent);

    }

    public static String editScript(Component parent, String script) {
        ScriptEditorDialog d = new ScriptEditorDialog(parent, Resources.getString("EditScript"),
                ScriptEditor.Type.SCRIPT, script, null);
        d.setVisible(true);

        return d.result;
    }

    public static boolean editFunctions(Component parent, Functions functions) {
        String original = functions.getScript();
        ScriptEditorDialog d = new ScriptEditorDialog(parent, Resources.getString("EditFunctions"),
                ScriptEditor.Type.FUNCTIONS, original, functions.getName());
        d.setVisible(true);

        String modified = d.result;
        try {
            Script.parseFunctions(modified);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(parent, Resources.getString("ScriptValidationError") + ":\n" + e.toString(), Resources.getString("Warning"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (modified != original) {
            functions.setScript(modified);
            functions.setName(d.name);
            return true;
        } else {
            return false;
        }
    }

    public static boolean editGrammar(Component parent, Grammar grammar) {
        String original = grammar.getGrammar();
        ScriptEditorDialog d = new ScriptEditorDialog(parent, Resources.getString("EditGrammar"),
                ScriptEditor.Type.SRGF, original, grammar.getName());
        d.setVisible(true);

        String modified = d.result;
        Collection<Exception> grammarIssues = null;
        try {
            com.clt.srgf.Grammar gr = com.clt.srgf.Grammar.create(modified);
            grammarIssues = gr.check(true);
        } catch (Exception e) {
            grammarIssues = Collections.singleton(e);
        }
        if (grammarIssues != null && !grammarIssues.isEmpty()) {
            StringBuilder sb = new StringBuilder(Resources.getString("GrammarValidationError") + ":\n");
            for (Exception e : grammarIssues) {
                sb.append(e.getLocalizedMessage());
                sb.append("\n");
            }
            JOptionPane.showMessageDialog(parent, sb.toString(), Resources.getString("Warning"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (modified != original) {
            grammar.setGrammar(modified);
            grammar.setName(d.name);
            return true;
        } else {
            return false;
        }
    }

}
