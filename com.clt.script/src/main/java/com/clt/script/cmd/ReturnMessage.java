package com.clt.script.cmd;

import com.clt.script.exp.Value;

public class ReturnMessage extends RuntimeException {

    Value returnValue;

    public ReturnMessage(Value returnValue) {

        this.returnValue = returnValue;
    }

    public Value getReturnValue() {

        return this.returnValue;
    }
}
