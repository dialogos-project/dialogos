package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.values.ListValue;

/**
 * @author dabo
 *
 */
public class ForExtended implements Loop {

    private Definition init;
    private Expression iterable;
    private Command body;

    public ForExtended(Definition init, Expression iterable) {

        this.init = init;
        this.iterable = iterable;

        this.body = new EmptyCommand();
    }

    public void setBody(Command body) {

        this.body = body;
    }

    public void execute(Debugger dbg) {

        Value source = this.iterable.evaluate(dbg);
        if (source instanceof ListValue) {
            for (Value initValue : ((ListValue) source)) {
                this.init.getBlock().setVariableValue(this.init.getName(), initValue);
                if (this.body != null) {
                    this.body.execute(dbg);
                }
            }
        } else {
            throw new ExecutionException("Source of extended for loop is not a list");
        }
    }

    public ReturnInfo check(Collection<String> warnings) {

        ReturnInfo info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);

        info = this.init.check(warnings);
        if (info.info == ReturnInfo.ON_ALL_PATHS) {
            throw new ExecutionException("Body of \"for\" loop is never reached.");
        }

        Type t = this.iterable.getType();
        try {
            t = Type.unify(t, new ListType());
        } catch (TypeException exn) {
            throw new TypeException("source <" + this.iterable
                    + "> of extended \"for\" loop is iterable");
        }

        try {
            if (t instanceof ListType) {
                Type.unify(((ListType) t).getElementType(), this.init.getType());
            }
        } catch (TypeException exn) {
            throw new TypeException(
                    "The types of definition and source in the extended \"for\" loop do not match");
        }

        ReturnInfo loopInfo = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);

        ReturnInfo bodyInfo = this.body.check(warnings);

        loopInfo = loopInfo.merge(bodyInfo);

        info = info.append(loopInfo);

        info.breakInfo = ReturnInfo.ON_NO_PATH;

        return info;
    }

    @Override
    public String toString() {

        return "for (" + this.init + " : " + this.iterable + ")";
    }

}
