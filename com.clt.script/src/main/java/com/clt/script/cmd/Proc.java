package com.clt.script.cmd;

import java.util.Collection;
import java.util.List;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;

/**
 * A procedure is just a special block that takes parameters
 */
public class Proc extends Block {

    private String name;
    private Type returnType;

    public Proc(String name, Type returnType, List<VarDef> parameters) {

        super(null);

        this.name = name;
        if (returnType == null) {
            this.returnType = Type.Void;
        } else {
            this.returnType = returnType;
        }

        if (parameters != null) {
            for (VarDef v : parameters) {
                if (v.name != null) {
                    if (this.containsVariable(v.name)) {
                        throw new IllegalArgumentException("Duplicate parameter '" + v.name
                                + "'");
                    }
                }
                this.addVariable(v.name, v.type);
            }
        }
    }

    public String getName() {

        return this.name;
    }

    public Type getReturnType() {

        return this.returnType;
    }

    public Type[] getParameterTypes() {

        Type[] parameterTypes = new Type[this.varDefs.size()];
        for (int i = 0; i < this.varDefs.size(); i++) {
            parameterTypes[i] = this.varDefs.get(i).type;
        }

        return parameterTypes;
    }

    public Value call(Debugger dbg, Value[] arguments) {

        try {
            if (this.getVarDefs().size() != arguments.length) {
                throw new EvaluationException(
                        "Wrong number of arguments in call to function '"
                        + this.getName() + "'");
            }

            super.execute(dbg, arguments);
        } catch (ReturnMessage msg) {
            return msg.getReturnValue();
        } catch (BreakMessage msg) {
            throw new ExecutionException("Caught break without surrounding context");
        }
        return Value.Void;
    }

    @Override
    public ReturnInfo check(Collection<String> warnings) {

        try {
            ReturnInfo info = super.check(warnings);
            if (this.returnType.equals(Type.Void)) {
                if (info.type != null ? !info.type.equals(Type.Void) : false) {
                    throw new ExecutionException("Incompatible return types");
                }
            } else {
                if (info.info != ReturnInfo.ON_ALL_PATHS) {
                    throw new MissingReturnException();
                }

                if (info.breakInfo != ReturnInfo.ON_NO_PATH) {
                    throw new ExecutionException("break without context");
                }

                try {
                    info.type = Type.unify(this.returnType, info.type);
                } catch (TypeException exn) {
                    throw new ExecutionException("Incompatible return types.\nt1 = "
                            + this.returnType
                            + "\nt2 = " + info.type);
                }
            }

            return info;
        } catch (MissingReturnException exn) {
            throw new MissingReturnException("In function '" + this + "':\n"
                    + exn.getLocalizedMessage());
        } catch (RuntimeException exn) {
            try {
                throw exn.getClass().getConstructor(new Class[]{String.class})
                        .newInstance(
                                new Object[]{"In function '" + this + "':\n"
                                    + exn.getLocalizedMessage()});
            } catch (RuntimeException x) {
                throw x;
            } catch (Exception x) {
                throw new ExecutionException("In function '" + this + "':\n"
                        + exn.getLocalizedMessage());
            }
        }
    }

    @Override
    public String toString() {

        Type[] ptypes = this.getParameterTypes();
        StringBuilder b = new StringBuilder();
        b.append(this.getReturnType());
        b.append(' ');
        b.append(this.getName());
        b.append("(");
        for (int i = 0; i < ptypes.length; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(ptypes[i]);
        }
        b.append(')');
        return b.toString();
    }

    protected static class MissingReturnException
            extends ExecutionException {

        public MissingReturnException() {

            this("Missing return statement");
        }

        public MissingReturnException(String message) {

            super(message);
        }
    }
}
