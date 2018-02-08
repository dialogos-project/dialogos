package com.clt.speech.recognition.test;

/**
 * @author dabo
 *
 */
public class Parameter {

    private String name;
    private Values values;

    public Parameter(String name, Values values) {

        this.name = name;
        this.values = values;
    }

    public String getName() {

        return this.name;
    }

    public int[] getValues() {

        return this.values.getValues();
    }
}
