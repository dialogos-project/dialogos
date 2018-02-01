package com.clt.dialogos.sphinx.gui;

import com.clt.speech.recognition.G2PEntry;
import com.clt.gui.table.ItemTableModel;
import com.clt.gui.table.TableEditDialog;
import com.clt.gui.table.TableEditor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * TODO: emable this dialog to generate pronunciations through G2P from Sphinx (if available for a language)
 * TODO: check that all entries are "valid" wrt phoneme set
 */
public class PronDictDialog extends TableEditor<G2PEntry> {

    public PronDictDialog(List<G2PEntry> items, String info, JButton[] additionalButtons) {
        super(PronDictDialog.createModel(items), info, true, additionalButtons);
    }

    private static ItemTableModel<G2PEntry> createModel(List<G2PEntry> items) {
        return new ItemTableModel<G2PEntry>(items) {
            @Override
            public int getColumnCount() {
                return 2;
            }
            @Override
            public String getColumnName(int columnIndex) {
                switch(columnIndex) {
                    case 0: return "Buchstaben"; //TODO: i10n
                    case 1: return "Laute";
                    default:
                        throw new RuntimeException("something is wrong");
                }
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            protected G2PEntry createNewItem(Component parent) {
                return new G2PEntry();
            }

            @Override
            protected Object getValue(G2PEntry item, int columnIndex) {
                return columnIndex == 0 ? item.getGraphemes() : item.getPhonemes();
            }

            @Override
            protected void setValue(G2PEntry item, int columnIndex, Object value) {
                if (columnIndex == 0)
                    item.setGraphemes(value.toString());
                else
                    item.setPhonemes(value.toString());
            }
        };
    }

    public static void showDialog(Component parent, List<G2PEntry> g2pList, String title) {
        new TableEditDialog<G2PEntry>(parent, PronDictDialog.createModel(g2pList),  title, null, false).setVisible(true);
    }


}
