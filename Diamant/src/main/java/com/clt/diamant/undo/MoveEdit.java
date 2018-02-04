package com.clt.diamant.undo;

import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.undo.UndoableEdit;

import com.clt.diamant.Resources;
import com.clt.diamant.graph.MoveableElement;
import com.clt.undo.AbstractEdit;

public class MoveEdit extends AbstractEdit {

    private boolean alreadyDone;
    private Set<MoveableElement> selection;
    private Point old_location, new_location;

    public MoveEdit(final Component c, Point old_location, Point new_location) {

        this(Collections.singleton(new MoveableElement() {

            public void setLocation(int x, int y) {

                c.setLocation(x, y);
            }

            public void setSize(int width, int height) {

                c.setSize(width, height);
            }

            public int getX() {

                return c.getX();
            }

            public int getY() {

                return c.getY();
            }

            public int getWidth() {

                return c.getWidth();
            }

            public int getHeight() {

                return c.getHeight();
            }
        }), old_location, new_location);
    }

    public MoveEdit(Collection<? extends MoveableElement> selection,
            Point old_location,
            Point new_location) {

        this(selection, old_location, new_location, false);
    }

    public MoveEdit(Collection<? extends MoveableElement> selection,
            Point old_location,
            Point new_location, boolean alreadyDone) {

        super(Resources.getString("Move"));

        this.alreadyDone = alreadyDone;
        this.selection = new HashSet<MoveableElement>(selection);
        this.old_location = old_location;
        this.new_location = new_location;
    }

    @Override
    public void unrun() {

        if (this.alreadyDone) {
            this.move(this.selection, this.new_location, this.old_location);
            this.alreadyDone = false;
        }
    }

    @Override
    public void run() {

        if (!this.alreadyDone) {
            this.move(this.selection, this.old_location, this.new_location);
            this.alreadyDone = true;
        }
    }

    private void move(Collection<MoveableElement> objects, Point from, Point to) {

        for (MoveableElement o : objects) {
            int x = o.getX();
            int y = o.getY();
            o.setLocation(x + (to.x - from.x), y + (to.y - from.y));
        }
    }

    // this gives us the ability to collect several consecutive moves
    // into one undo operation
    @Override
    public boolean addEdit(UndoableEdit edit) {

        if (edit instanceof MoveEdit) {
            if (this.selection.equals(((MoveEdit) edit).selection)) {
                this.new_location.x += ((MoveEdit) edit).new_location.x
                        - ((MoveEdit) edit).old_location.x;
                this.new_location.y += ((MoveEdit) edit).new_location.y
                        - ((MoveEdit) edit).old_location.y;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
