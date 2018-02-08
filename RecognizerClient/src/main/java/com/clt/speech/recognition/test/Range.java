package com.clt.speech.recognition.test;

/**
 * @author dabo
 *
 */
public class Range implements Values {

    private int minimum;
    private int maximum;
    private int step;

    public Range(int minimum, int maximum) {

        this(minimum, maximum, 1);
    }

    public Range(int minimum, int maximum, int step) {

        if (step <= 0) {
            throw new IllegalArgumentException();
        }

        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
    }

    public int[] getValues() {

        int[] values = new int[(this.maximum - this.minimum) / this.step + 1];

        for (int i = 0; i < values.length; i++) {
            values[i] = this.minimum + i * this.step;
        }

        return values;
    }
}
