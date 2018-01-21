package com.clt.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class AbstractEdit extends AbstractUndoableEdit implements Runnable {

    private String name;
    private boolean significant;

    public AbstractEdit(String name) {
        this(name, true);
    }

    public AbstractEdit(String name, boolean significant) {
        this.name = name;
        this.significant = significant;
    }

    @Override
    public final synchronized void undo() throws CannotUndoException {
        super.undo();
        this.unrun();
    }

    @Override
    public final synchronized void redo() throws CannotRedoException {
        super.redo();
        this.run();
    }

    @Override
    public boolean isSignificant() {
        return this.significant;
    }

    @Override
    public String getPresentationName() {
        return this.name;
    }

    public abstract void run();

    public abstract void unrun();
}
