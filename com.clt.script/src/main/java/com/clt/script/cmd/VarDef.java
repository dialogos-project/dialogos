package com.clt.script.cmd;

import com.clt.script.exp.Type;

public class VarDef {

    public String name;
    public Type type;

    public VarDef(String name, Type type) {

        this.name = name;
        this.type = type;
    }
}
