package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Break implements Command {

    public Break() {

    }

    public void execute(Debugger dbg) {

        dbg.preExecute(this);
        throw new BreakMessage();
    }

    public ReturnInfo check(Collection<String> warnings) {

        ReturnInfo info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
        info.breakInfo = ReturnInfo.ON_ALL_PATHS;
        return info;
    }
}
