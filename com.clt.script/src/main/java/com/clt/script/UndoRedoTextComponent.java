package com.clt.script;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.editor.EditorScrollPane;

/**
 *
 *
 * @author Daniel Beck
 */
public class UndoRedoTextComponent extends JPanel {
    private static final long serialVersionUID = -1842096049779398768L;

    /**
     * Reference on the TextComponent.
     */
    private JTextComponent mTextComponent;

    /**
     * Reference on the scrollpane containing the editor.
     */
    private JScrollPane mJsp;

    /**
     * UndoManager.
     */
    private UndoManager mUndoManager = new UndoManager();

    /**
     * Undo Button.
     */
    private JButton mUndo = new JButton(GUI.getString("Undo"), Images
            .load("Undo_16pt.png"));

    /**
     * Redo Button.
     */
    private JButton mRedo = new JButton(GUI.getString("Redo"), Images
            .load("Redo_16pt.png"));

    /**
     * Constructor taking a JTextComponent as parameter.
     *
     * @param textComponent Reference on a JTextComponent
     */
    public UndoRedoTextComponent(final JTextComponent textComponent) {

        this.mTextComponent = textComponent;
        this.mJsp = new EditorScrollPane(this.getTextComponent());

        this.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.mUndo.setEnabled(false);
        this.mRedo.setEnabled(false);
        GUI.setKeyBinding(this, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), e -> mUndo.doClick());
        GUI.setKeyBinding(this, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK), e -> mRedo.doClick());
        this.mUndo.setToolTipText(GUI.getString("CtrlZ"));
        this.mRedo.setToolTipText(GUI.getString("CtrlShiftZ"));
        buttonPanel.add(this.mUndo);
        buttonPanel.add(this.mRedo);

        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(this.mJsp, BorderLayout.CENTER);

        this.getTextComponent().getDocument().addUndoableEditListener(
                new UndoableEditListener() {

            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {

                UndoRedoTextComponent.this.mUndoManager.addEdit(e.getEdit());
                UndoRedoTextComponent.this.updateButtons();
            }
        });

        this.mUndo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                UndoRedoTextComponent.this.mUndoManager.undo();
                UndoRedoTextComponent.this.updateButtons();
            }
        });

        this.mRedo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                UndoRedoTextComponent.this.mUndoManager.redo();
                UndoRedoTextComponent.this.updateButtons();
            }
        });
    }

    /**
     * Updates the "Undo" and "Redo" button.
     */
    private void updateButtons() {

        this.mUndo.setEnabled(this.mUndoManager.canUndo());
        this.mRedo.setEnabled(this.mUndoManager.canRedo());
    }

    /**
     * Test method.
     *
     * @param args Empty array.
     */
    public static void main(final String[] args) {

        JFrame frame = new JFrame();
        frame.setSize(300, 400);
        UndoRedoTextComponent r = new UndoRedoTextComponent(new JEditorPane());
        frame.add(r);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Returns the JTextComponent.
     *
     * @return Returns the JTextComponent
     */
    public final JTextComponent getTextComponent() {

        return this.mTextComponent;
    }
}
