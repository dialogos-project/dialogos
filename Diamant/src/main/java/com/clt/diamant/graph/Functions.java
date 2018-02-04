package com.clt.diamant.graph;

public class Functions {

    private String name;
    private String script;

    public Functions(String name, String script) {

        this.name = name;
        this.script = script;
    }

    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getScript() {

        return this.script;
    }

    public void setScript(String script) {

        this.script = script;
    }

    @Override
    public String toString() {

        return this.name;
    }
}
