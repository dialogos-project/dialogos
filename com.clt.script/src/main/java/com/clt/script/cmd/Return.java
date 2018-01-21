package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;

/**
 * Commands of the form <code>return;</code> and <code>return <i>exp</i>;</code>
 */
public class Return implements Command {

    Expression returnValue;

    public Return(Expression returnValue) {

        this.returnValue = returnValue;
    }

    public void execute(Debugger dbg) {

        Value v = null;
        dbg.preExecute(this);

        if (this.returnValue != null) {
            v = this.returnValue.evaluate(dbg);
        }

        throw new ReturnMessage(v);
    }

    public ReturnInfo check(Collection<String> warnings) {

        Type t = null;

        if (this.returnValue != null) {
            t = this.returnValue.getType();
        } else {
            t = Type.Void;
        }

        return new ReturnInfo(ReturnInfo.ON_ALL_PATHS, t);
    }

    @Override
    public String toString() {

        return "return " + this.returnValue;
    }
}
