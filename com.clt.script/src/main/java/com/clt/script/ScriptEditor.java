package com.clt.script;

import java.awt.Color;
import java.awt.Font;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;

import com.clt.gui.GUI;
import com.clt.gui.editor.Scanner;
import com.clt.gui.editor.Symbol;
import com.clt.gui.editor.SyntaxEditorKit;
import com.clt.script.parser.SyntaxColor;
import com.clt.script.parser.SyntaxColor.Token;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class ScriptEditor extends JEditorPane {

    public enum Type {
        SCRIPT, SRGF, FUNCTIONS
    }

    public ScriptEditor() {

        this(Type.SCRIPT);
    }

    public ScriptEditor(final Type type) {

        EditorKit kit = new SyntaxEditorKit(new Scanner() {

            SyntaxColor sc = new SyntaxColor() {

                @Override
                public Color getColor(int style) {

                    switch (style) {
                        case STRING:
                            return Color.red.darker().darker();
                        case CONSTANT:
                            return Color.blue.darker();
                        case COMMENT:
                            return Color.green.darker().darker();
                        case KEYWORD:
                            return Color.magenta.darker().darker();
                        case ERROR:
                            return Color.black;
                        default:
                            return Color.black;
                    }
                }
            };

            public Symbol[] parse(Reader in) {

                try {
                    List<Symbol> tokens = new ArrayList<Symbol>();
                    Iterator<Token> tks;
                    switch (type) {
                        case SRGF:
                            tks = this.sc.parseGrammar(in);
                            break;

                        case FUNCTIONS:
                            tks = this.sc.parseFunctions(in);
                            break;
                        default:
                            tks = this.sc.parseScript(in);
                            break;
                    }
                    while (tks.hasNext()) {
                        final Token t = tks.next();
                        tokens.add(new Symbol() {

                            public int getStyle() {

                                return t.getSymbol();
                            }

                            public int getStart() {

                                return t.getStart();
                            }
                        });
                    }

                    return tokens.toArray(new Symbol[tokens.size()]);
                } catch (Exception exn) {
                    return new Symbol[0];
                }
            }

            public int numStyles() {

                return SyntaxColor.NUM_STYLES;
            }

            public Color getStyleColor(int index) {

                return this.sc.getColor(index);
            }
        });

        this.setEditorKit(kit);
        this.setBackground(Color.white);
        // setFont(new Font("Courier", 0, 12));
        this.setFont(new Font("Monospaced", 0, 12));
        this.setEditable(true);

        // Make sure the line model is initialized.
        // Failing to do so will result in arbitrary errors
        // from the ui when calculating line numbers.
        this.setText(" ");
        this.setText("");

        GUI.addFindReplaceSupport(this, true);
    }

    @Override
    public void setText(String t) {

        super.setText(t);
        this.setCaretPosition(0);
    }
}
