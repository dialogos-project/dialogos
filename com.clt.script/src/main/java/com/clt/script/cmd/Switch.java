package com.clt.script.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
// the "default" case ist stored as value null!
public class Switch implements Command {

    Expression e;
    List<Command> commands;
    Map<Value, Integer> cases;

    public Switch(Expression e, List<Command> commands) {

        this.e = e;

        this.commands = new ArrayList<Command>();
        this.cases = new HashMap<Value, Integer>();
        for (Command c : commands) {
            if (c instanceof CaseLabel) {
                Value value = ((CaseLabel) c).evaluate();
                if (this.cases.containsKey(value)) {
                    throw new TypeException("Duplicate case label: " + c);
                } else {
                    this.cases.put(value, new Integer(this.commands.size()));
                }
            }

            this.commands.add(c);
        }
    }

    public void execute(Debugger dbg) {

        dbg.preExecute(this);
        Value v = this.e.evaluate(dbg);

        Integer index = this.cases.get(v);
        if (index == null) {
            index = this.cases.get(null);
        }

        if (index != null) {
            try {
                for (int i = index.intValue(); i < this.commands.size(); i++) {
                    this.commands.get(i).execute(dbg);
                }
            } catch (BreakMessage m) {
            }
        }
    }

    public ReturnInfo check(Collection<String> warnings) {

        List<ReturnInfo> infos = new ArrayList<ReturnInfo>();

        ReturnInfo info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
        for (Command c : this.commands) {
            if ((c instanceof Break) || (c instanceof Return)) {
                if ((info.info == ReturnInfo.ON_ALL_PATHS)
                        || (info.breakInfo == ReturnInfo.ON_ALL_PATHS)) {
                    throw new ExecutionException("Statement not reached");
                }
                info = info.append(c.check(warnings));
                infos.add(info);
            } else if (c instanceof CaseLabel) {
                ReturnInfo case_info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
                if ((info.breakInfo == ReturnInfo.ON_ALL_PATHS)
                        || (info.info == ReturnInfo.ON_ALL_PATHS)) {
                    // man kann nur direkt hierhingelangen
                    info = case_info;
                } else {
                    // man kann von oben oder direkt hierhingelangen
                    info = info.append(case_info);
                    info = info.merge(case_info);
                }
            } else {
                if ((info.info == ReturnInfo.ON_ALL_PATHS)
                        || (info.breakInfo == ReturnInfo.ON_ALL_PATHS)) {
                    throw new ExecutionException("Statement not reached");
                }
                info = info.append(c.check(warnings));
            }
        }

        if (infos.size() == 0) {
            info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
        } else {
            info = null;
            for (ReturnInfo i : infos) {
                if (info == null) {
                    info = i;
                } else {
                    info = info.merge(i);
                }
            }
        }

        info.breakInfo = ReturnInfo.ON_NO_PATH;
        // die Frage, ob es immer ein return gibt, haengt davon ab, ob eines
        // der Labels "default" ist
        if ((info.info == ReturnInfo.ON_ALL_PATHS)
                && (this.cases.get(null) == null)) {
            info.info = ReturnInfo.ON_SOME_PATHS;
        }

        return info;
    }

}
