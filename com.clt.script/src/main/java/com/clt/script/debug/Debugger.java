package com.clt.script.debug;

import com.clt.script.cmd.Command;
import com.clt.script.exp.Expression;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Debugger {

    public void preExecute(Command c);

    public void preEvaluate(Expression e);

    public void log(String s);
}
