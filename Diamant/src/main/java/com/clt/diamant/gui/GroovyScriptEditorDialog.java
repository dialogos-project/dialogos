package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import com.clt.diamant.Resources;
import com.clt.gui.CmdButton;
import com.clt.gui.GUI;
import com.clt.gui.table.TableEditor;
import com.clt.script.UndoRedoTextComponent;

import org.fife.ui.rsyntaxtextarea.*;

/**
 * Editor dialog for Groovy functions
 *
 * @author Bri Burr / Till Kollenda
 * @version 1.0
 */
public class GroovyScriptEditorDialog extends JDialog {

    private String result;
    private RSyntaxTextArea textArea;

    /**
     * Opens the Groovy Script Editor Dialog
     *
     * @param parent parent component
     * @param windowTitle Title of the Dialog
     * @param text Text which will be shown in the editor window
     */
    public GroovyScriptEditorDialog(Component parent, String windowTitle, String text) {

        super(GUI.getFrameForComponent(parent), windowTitle, true);

        this.result = text;

        //this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setResizable(true);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // final JPanel panel = new JPanel(new BorderLayout());
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textArea.setCodeFoldingEnabled(true);
        textArea.setText(result);

        p.add(new UndoRedoTextComponent(textArea), BorderLayout.CENTER);
        CmdButton ok = new CmdButton(new Runnable() {

            public void run() {

                GroovyScriptEditorDialog.this.result
                        = GroovyScriptEditorDialog.this.textArea.getText();
                GroovyScriptEditorDialog.this.dispose();
            }
        }, Resources.getString("OK"));
        CmdButton cancel = new CmdButton(new Runnable() {

            public void run() {

                GroovyScriptEditorDialog.this.dispose();
            }
        }, Resources.getString("Cancel"));
        p.add(TableEditor.createButtonPanel(new JButton[0], new JButton[]{cancel,
            ok}),
                BorderLayout.SOUTH);

        this.setContentPane(p);
        GUI.setDefaultButtons(this, ok, cancel);
        GUI.assignMnemonics(this.getContentPane());
        this.pack();
        this.setSize(400, 300);
        this.setLocationRelativeTo(parent);
    }

    /**
     * Opens the edit window
     *
     * @param parent
     * @param script
     * @return updated Groovy script
     */
    public static String editScript(Component parent, String script) {

        GroovyScriptEditorDialog d
                = new GroovyScriptEditorDialog(parent, Resources.getString("EditScript"), script);
        d.setVisible(true);

        return d.result;
    }

}
