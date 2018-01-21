package com.clt.gui.border;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * A GroupBorder is used to represent a named group of interface elements. It
 * will draw an etched border an the group title.
 */
public class GroupBorder extends CompoundBorder {

    public GroupBorder(String title) {

        this(title, 0, 6, 4, 6);
    }

    public GroupBorder(String title, int top, int left, int bottom, int right) {

        super(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                title != null ? " " + title + " " : null),
                new EmptyBorder(top, left, bottom, right));
    }
}
