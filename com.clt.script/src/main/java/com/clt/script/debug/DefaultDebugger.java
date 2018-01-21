package com.clt.script.debug;

import com.clt.script.cmd.Command;
import com.clt.script.exp.Expression;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DefaultDebugger implements Debugger {

    public void preExecute(Command c) {

    }

    public void preEvaluate(Expression e) {
//     System.err.println("evaluate: " + e);
    }

    public void log(String s) {

        System.out.println(s);
    }
}
