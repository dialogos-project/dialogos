package com.clt.gui.editor;

import java.awt.Color;
import java.io.Reader;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Scanner {
    /**
     * Parse the contents supplied by the reader and return a number of lexical
     * symbols.
     */
    public Symbol[] parse(Reader in);

    /**
     * Return the number of available styles. There must be at least one style.
     * The default style must always have index 0.
     */
    public int numStyles();

    /**
     * Get the Color for style <code>index</code>.
     */
    public Color getStyleColor(int index);
}
