package com.clt.gui;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListSelectionDialog<E> {

    JList list;

    boolean ok;

    int lastIndex;

    E lastObject;

    public ListSelectionDialog(Component parent, String title, String prompt, E[] items) {

        this(parent, title, prompt, items, 0, true);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Collection<? extends E> items) {

        this(parent, title, prompt, items, 0, true);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Map<? extends E, ?> items) {

        this(parent, title, prompt, items, 0, true);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, E[] items, int selection) {
        this(parent, title, prompt, items, selection, true);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Collection<? extends E> items, int selection) {
        this(parent, title, prompt, items, selection, true);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Map<? extends E, ?> items, int selection) {
        this(parent, title, prompt, items, selection, true);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, E[] items, int selection, boolean canCancel) {
        this(parent, title, prompt, items, selection, canCancel, OptionPane.QUESTION);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Collection<? extends E> items, int selection, boolean canCancel) {
        this(parent, title, prompt, items, selection, canCancel, OptionPane.QUESTION);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Map<? extends E, ?> items, int selection, boolean canCancel) {
        this(parent, title, prompt, items, selection, canCancel, OptionPane.QUESTION);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, E[] items, int selection, boolean canCancel, int messageType) {
        this(parent, title, prompt, new JList(items), selection, canCancel, messageType);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Collection<? extends E> items, int selection, boolean canCancel, int messageType) {
        this(parent, title, prompt, new JList(new Vector<E>(items)), selection, canCancel, messageType);
    }

    public ListSelectionDialog(Component parent, String title, String prompt, Map<? extends E, ?> items, int selection, boolean canCancel, int messageType) {
        this(parent, title, prompt, new JList(new Vector<E>(items.keySet())), selection, canCancel, messageType);
    }

    private ListSelectionDialog(Component parent, String title, String prompt, JList items, int selection, boolean canCancel, int messageType) {
        this.list = items;

        int AnzahlObjekte = this.list.getModel().getSize();
        if (AnzahlObjekte == 0) {
            throw new IllegalArgumentException("Can't choose from an empty list");
        }
        if (AnzahlObjekte < 4) {
            this.list.setVisibleRowCount(4);
        } else if (AnzahlObjekte >= 8) {
            this.list.setVisibleRowCount(8);
        } else {
            this.list.setVisibleRowCount(AnzahlObjekte);
        }
        this.list.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        this.list.addListSelectionListener(new ListSelectionListener() {

            @SuppressWarnings("unchecked")
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionDialog.this.lastIndex
                        = ListSelectionDialog.this.list.getSelectedIndex();
                ListSelectionDialog.this.lastObject
                        = (E) ListSelectionDialog.this.list.getSelectedValue();
            }
        });
        this.list.setSelectedIndex(selection);

        if (canCancel) {
            this.ok
                    = (OptionPane.confirm(parent, new Object[]{prompt,
                new JScrollPane(this.list)}, title,
                            OptionPane.OK_CANCEL_OPTION, messageType) == OptionPane.OK);
        } else {
            OptionPane.confirm(parent, new Object[]{prompt,
                new JScrollPane(this.list)}, title,
                    OptionPane.DEFAULT_OPTION, messageType);
            this.ok = true;
        }
    }

    public E getSelectedItem() {

        return this.ok ? this.lastObject : null;
    }

    public int getSelectedIndex() {

        return this.ok ? this.lastIndex : -1;
    }
}
