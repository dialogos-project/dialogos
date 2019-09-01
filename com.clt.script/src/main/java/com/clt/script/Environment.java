package com.clt.script;

import java.io.IOException;
import java.io.Reader;

import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Variable;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Environment {

    public Type getType(String typeName);

    public Variable createVariableReference(String id);

    public Expression createFunctionCall(String name, Expression[] arguments);

    public Reader include(String name) throws IOException;

    static String functionSignature(String name, Expression[] arguments) {
        StringBuilder f = new StringBuilder();
        f.append(name);
        f.append('(');
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                f.append(", ");
            }
            f.append(arguments[i].getType().resolve());
        }
        f.append(')');
        return f.toString();
    }

    public class NoSuchFunctionException extends TypeException {

        public NoSuchFunctionException(String name, Expression[] arguments) {
            super("Unknown function or wrong number/type of arguments: "
                    + functionSignature(name, arguments));
        }

        public NoSuchFunctionException(String message) {
            super(message);
        }
    }
}
