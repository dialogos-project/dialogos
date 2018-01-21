package com.clt.util;

import java.awt.event.ActionEvent;

import com.clt.undo.AbstractEdit;
import com.clt.undo.Undo;

/**
 * @author dabo
 */
public abstract class UndoableAction extends AbstractAction {

    private Undo undoManager = null;

    public UndoableAction() {

        this(null);
    }

    public UndoableAction(Undo undoManager) {

        this.undoManager = undoManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (this.undoManager != null) {
            this.undoManager.addEdit(new AbstractEdit(this.getName()) {

                @Override
                public void run() {

                    UndoableAction.this.run();
                }

                @Override
                public void unrun() {

                    UndoableAction.this.unrun();
                }
            });
        } else {
            this.run();
        }
    }

    @Override
    public abstract void run();

    public abstract void unrun();
}
