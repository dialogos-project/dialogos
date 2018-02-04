package com.clt.diamant.undo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.Group;
import com.clt.diamant.graph.GroupElement;

public class GroupEdit extends AbstractUndoableEdit {

    private Group group;
    private Set<GroupElement> elements;

    public GroupEdit(Group g) {

        this.group = g;
        this.elements = null;
    }

    public GroupEdit(Collection<? extends GroupElement> elements) {

        this.elements = new HashSet<GroupElement>(elements);
        this.group = null;
    }

    @Override
    public void undo() throws CannotUndoException {

        super.undo();

        if (this.group != null) {
            this.ungroup();
        } else {
            this.group();
        }
    }

    @Override
    public void redo() throws CannotRedoException {

        super.redo();

        if (this.group != null) {
            this.ungroup();
        } else {
            this.group();
        }
    }

    private void group() {

        this.group = Group.group(this.elements);
        this.elements = null;
    }

    private void ungroup() {

        this.elements = Group.ungroup(this.group);
        this.group = null;
    }

    @Override
    public String getPresentationName() {

        if (this.canUndo()) {
            return this.group != null ? Resources.getString("Group") : Resources
                    .getString("Ungroup");
        } else {
            return this.group != null ? Resources.getString("Ungroup") : Resources
                    .getString("Group");
        }
    }
}
