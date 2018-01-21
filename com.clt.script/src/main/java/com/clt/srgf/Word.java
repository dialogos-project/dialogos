package com.clt.srgf;

/**
 * A single word of a sentence to parse.
 *
 * @see Grammar
 * @see Terminal
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Word {

    public String getWord();

    public float getConfidence();
}
