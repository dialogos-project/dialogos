package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class EmptyCommand implements Command {

    public void execute(Debugger dbg) {

        dbg.preExecute(this);
    }

    public ReturnInfo check(Collection<String> warnings) {

        return new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
    }
}
