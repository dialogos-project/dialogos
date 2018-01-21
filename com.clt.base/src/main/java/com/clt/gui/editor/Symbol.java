package com.clt.gui.editor;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Symbol {

    /**
     * Get the style of this symbol. The id of the default style must always be
     * 0.
     */
    public int getStyle();

    /**
     * Get the starting location of this symbol.
     */
    public int getStart();
}
