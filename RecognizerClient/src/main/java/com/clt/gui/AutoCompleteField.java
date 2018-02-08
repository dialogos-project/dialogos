package com.clt.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;

import com.clt.util.Trie;

public class AutoCompleteField extends JTextField {

    private List<CompletionListener> completionListeners;
    private Trie words;

    public AutoCompleteField() {

        this(false);
    }

    public AutoCompleteField(int columns) {

        this(columns, false);
    }

    public AutoCompleteField(boolean allowOnlyLegalWords) {

        this(0, allowOnlyLegalWords);
    }

    public AutoCompleteField(int columns, final boolean allowOnlyLegalWords) {

        super(columns);
        this.words = new Trie();
        this.completionListeners = new ArrayList<CompletionListener>();

        this.setFocusTraversalKeysEnabled(false);

        Keymap keymap = this.getKeymap();

        Action textInput = new TextAction(DefaultEditorKit.defaultKeyTypedAction) {

            public void actionPerformed(ActionEvent e) {

                JTextComponent target = this.getTextComponent(e);
                if ((target != null) && (e != null)) {
                    String content = e.getActionCommand();
                    int mod = e.getModifiers();
                    if ((content != null)
                            && (content.length() > 0)
                            && ((mod & ActionEvent.ALT_MASK) == (mod & ActionEvent.CTRL_MASK))) {
                        for (int i = 0; i < content.length(); i++) {
                            this.insertChar(target, content.charAt(i));
                        }
                    }
                }
            }

            private void insertChar(JTextComponent target, char c) {

                Document d = target.getDocument();

                try {
                    // System.out.println((int) c);
                    if (c == '\t') { // tab
                        int pos = target.getSelectionEnd();
                        String content = d.getText(0, pos);
                        int start = pos;
                        while ((start > 0)
                                && !Character.isWhitespace(content.charAt(start - 1))) {
                            start--;
                        }
                        String prefix = content.substring(start);
                        if (AutoCompleteField.this.isWord(prefix)) {
                            target.select(pos, pos);
                            target.replaceSelection(" ");
                        }
                    } else {
                        int pos = target.getSelectionStart();
                        String content = d.getText(0, pos);
                        int start = pos;
                        while ((start > 0)
                                && !Character.isWhitespace(content.charAt(start - 1))) {
                            start--;
                        }
                        String prefix = content.substring(start);
                        switch (c) {
                            case ' ':
                                if (AutoCompleteField.this.isWord(prefix)
                                        || !allowOnlyLegalWords) {
                                    target.replaceSelection(" ");
                                } else {
                                    // if (start <= 0 ||
                                    // Character.isWhitespace(content.charAt(start-1)))
                                    // target.getToolkit().beep();
                                }
                                break;

                            case '\n': // enter, return
                            case '\r': // enter, return
                            case '\u0008': // backspace
                            case '\u007F': // delete
                                break;

                            default:
                                if (Character.isLetterOrDigit(c)
                                        || (".,:;()[]{}+-*/\\%&$\"'#_=<>|?!@~"
                                                .indexOf(c) >= 0)) {
                                    if (AutoCompleteField.this
                                            .isValidPrefix(prefix + c)
                                            || !allowOnlyLegalWords) {
                                        target.replaceSelection(String.valueOf(c));
                                    }
                                }
                                // else
                                // target.getToolkit().beep();
                                break;
                        }
                    }
                } catch (BadLocationException exn) {
                }
            }
        };

        keymap.setDefaultAction(textInput);

        Document d = new DefaultStyledDocument() {

            boolean inUpdate = false;

            @Override
            public void insertString(int offs, String str, AttributeSet a)
                    throws BadLocationException {

                super.insertString(offs, str, a);
                if (!this.inUpdate) {
                    this.inUpdate = true;
                    this.documentChanged(offs + str.length(), false);
                    this.inUpdate = false;
                }
            }

            @Override
            public void remove(int offs, int len)
                    throws BadLocationException {

                super.remove(offs, len);
                if (!this.inUpdate) {
                    this.inUpdate = true;
                    this.documentChanged(offs, true);
                    this.inUpdate = false;
                }
            }

            private void documentChanged(int pos, boolean delete) {

                AutoCompleteField.this.showCompletions(pos, delete);
            }
        };
        this.setDocument(d);
        /*
     * getDocument().addDocumentListener(new DocumentChangeListener() { public
     * void documentChanged(DocumentEvent evt) { int length = evt.getLength();
     * if (evt.getType() == DocumentEvent.EventType.REMOVE) length = 0;
     * showCompletions(evt.getOffset() + length); } });
         */
    }

    public void setWords(String[] words) {

        this.setWords(Arrays.asList(words));
    }

    public void setWords(Collection<String> words) {

        this.words.clear();
        if (words != null) {
            for (String word : words) {
                this.words.add(this.normalize(word));
            }
        }
    }

    private boolean isValidPrefix(String prefix) {

        return this.words.containsPrefix(this.normalize(prefix));
    }

    private boolean isWord(String word) {

        return this.words.contains(this.normalize(word));
    }

    private void showCompletions(int pos, boolean delete) {

        Document d = this.getDocument();
        try {
            List<String> prefix = new ArrayList<String>();
            String content = d.getText(0, pos);
            String shortestPrefix = null;
            while (pos > 0) {
                while ((pos > 0) && !Character.isWhitespace(content.charAt(pos - 1))) {
                    pos--;
                }
                prefix.add(content.substring(pos));
                if (shortestPrefix == null) {
                    shortestPrefix = content.substring(pos);
                }
                while ((pos > 0) && Character.isWhitespace(content.charAt(pos - 1))) {
                    pos--;
                }
            }
            if (shortestPrefix == null) {
                shortestPrefix = "";
            }

            this.showCompletions(shortestPrefix, prefix.toArray(new String[prefix
                    .size()]), delete);
        } catch (BadLocationException cantHappen) {
        }
    }

    private void showCompletions(String lastWordPrefix, String[] prefix,
            boolean delete) {

        Collection<String> completions = new TreeSet<String>();
        for (int i = 0; i < prefix.length; i++) {
            if (prefix[i].length() > 0) {
                for (Iterator<String> it
                        = this.words.iterator(this.normalize(prefix[i])); it.hasNext();) {
                    completions.add(it.next());
                }
            } else {
                completions.add("");
            }
        }

        for (CompletionListener listener : this.completionListeners) {
            listener.possibleCompletions(lastWordPrefix, completions, delete);
        }
    }

    private String normalize(String s) {

        return s.toLowerCase();
    }

    public void addCompletionListener(CompletionListener l) {

        this.completionListeners.add(l);
    }

    public void removeCompletionListener(CompletionListener l) {

        this.completionListeners.remove(l);
    }

    public static interface CompletionListener {

        public void possibleCompletions(String prefix,
                Collection<String> completions,
                boolean delete);
    }
}
