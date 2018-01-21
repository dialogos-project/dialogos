package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;

public interface Command {

    public void execute(Debugger dbg);

    public ReturnInfo check(Collection<String> warnings);
}
