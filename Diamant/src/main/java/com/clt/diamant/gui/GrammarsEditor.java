package com.clt.diamant.gui;

import java.awt.Component;
import java.util.List;

import com.clt.diamant.Grammar;
import com.clt.diamant.Resources;
import com.clt.gui.ListEditor;
import com.clt.gui.ListEditorDialog;

/**
 * @author dabo
 *
 */
public class GrammarsEditor extends ListEditor {

    public GrammarsEditor(List<Grammar> grammars) {

        super(GrammarsEditor.createModel(grammars), true);
    }

    public static void showDialog(Component parent, List<Grammar> grammars) {

        new ListEditorDialog(parent, Resources.getString("Grammars"),
                GrammarsEditor.createModel(grammars)).setVisible(true);
    }

    private static ListEditor.Model createModel(final List<Grammar> grammars) {

        return new ListEditor.Model() {

            private boolean edit(Grammar g, Component parent) {

                return ScriptEditorDialog.editGrammar(parent, g);
            }

            public int getSize() {

                return grammars.size();
            }

            public Object getElementAt(int index) {

                return grammars.get(index);
            }

            @Override
            public void editItemAt(Component parent, int index) {

                this.edit(grammars.get(index), parent);
            }

            @Override
            public int addElement(Component parent) {

                Grammar g = new Grammar(Resources.getString("Untitled"));
                if (this.edit(g, parent)) {
                    grammars.add(g);
                    return grammars.indexOf(g);
                } else {
                    return -1;
                }
            }

            @Override
            public boolean removeElement(Component parent, int index) {

                grammars.remove(index);
                return true;
            }

            @Override
            public boolean moveElement(int from, int to) {

                Grammar o = grammars.get(from);
                if (from < to) {
                    for (int i = from; i < to; i++) {
                        grammars.set(i, grammars.get(i + 1));
                    }
                } else {
                    for (int i = from; i > to; i--) {
                        grammars.set(i, grammars.get(i - 1));
                    }
                }
                grammars.set(to, o);
                this.fireContentsChanged(this, Math.min(from, to), Math.max(from, to));
                return true;
            }
        };
    }
}
