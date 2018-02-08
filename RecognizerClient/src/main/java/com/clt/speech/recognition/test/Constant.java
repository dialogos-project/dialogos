package com.clt.speech.recognition.test;

/**
 * @author dabo
 *
 */
public class Constant implements Values {

    private int value;

    public Constant(int value) {

        this.value = value;
    }

    public int[] getValues() {

        return new int[]{this.value};
    }

}
