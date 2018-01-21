package com.clt.script.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;
import com.clt.script.exp.values.Undefined;

/**
 * A block is a sequence of definitions and commands enclosed in curly brackets
 */
public class Block  implements Command {

    Block superBlock;
    List<Command> commands;

    List<VarDef> varDefs;
    Stack<Map<String, Variable>> varInstances;

    public Block(Block superBlock) {

        this.superBlock = superBlock;
        this.commands = new ArrayList<Command>();

        this.varDefs = new ArrayList<VarDef>();
        this.varInstances = new Stack<Map<String, Variable>>();
    }

    public Block getSuperBlock() {

        return this.superBlock;
    }

    public void addCommand(Command command) {

        if (command == this) {
            throw new IllegalArgumentException("WARNING: adding block to itself: "
                    + this);
        }
        this.commands.add(command);
    }

    public void addCommands(List<Command> commands) {

        this.commands.addAll(commands);
    }

    public void addVariable(String name, Type type) {

        if (name != null) {
            if (this.containsVariable(name)) {
                throw new IllegalArgumentException("Variable '" + name
                        + "' already defined at this level");
            }
        }

        this.varDefs.add(new VarDef(name, type));
    }

    public boolean containsVariable(String name) {

        for (int i = 0; i < this.varDefs.size(); i++) {
            if (name.equals(this.varDefs.get(i).name)) {
                return true;
            }
        }

        if (this.superBlock == null) {
            return false;
        } else {
            return this.superBlock.containsVariable(name);
        }
    }

    protected List<VarDef> getVarDefs() {

        return this.varDefs;
    }

    public Type getVariableType(String name) {

        for (int i = 0; i < this.varDefs.size(); i++) {
            VarDef v = this.varDefs.get(i);
            if (name.equals(v.name)) {
                return v.type;
            }
        }

        if (this.superBlock == null) {
            throw new TypeException("Unknown variable: " + name);
        } else {
            return this.superBlock.getVariableType(name);
        }
    }

    public Value getVariableValue(String name) {

        Map<String, Variable> vars = this.varInstances.peek();
        Variable v = vars.get(name);
        if (v != null) {
            return v.getValue();
        }

        if (this.superBlock == null) {
            throw new ExecutionException("Unknown variable: " + name);
        } else {
            return this.superBlock.getVariableValue(name);
        }
    }

    public void setVariableValue(String name, Value value) {

        Map<String, Variable> vars = this.varInstances.peek();
        Variable v = vars.get(name);
        if (v != null) {
            v.setValue(value);
            return;
        }

        if (this.superBlock == null) {
            throw new ExecutionException("Unknown variable: " + name);
        } else {
            this.superBlock.setVariableValue(name, value);
        }
    }

    public void execute(Debugger dbg) {

        this.execute(dbg, null);
    }

    protected void execute(Debugger dbg, Value[] initValues) {

        if (initValues != null) {
            if (this.varDefs.size() != initValues.length) {
                throw new IllegalArgumentException("Wrong number of initializers");
            }
        }

        // push stack frame for local variables
        Map<String, Variable> values
                = new HashMap<String, Variable>(this.varDefs.size());
        for (int i = 0; i < this.varDefs.size(); i++) {
            final VarDef def = this.varDefs.get(i);
            if (def.name != null) {
                Variable v = new Variable() {

                    Value value = new Undefined();

                    public String getName() {

                        return def.name;
                    }

                    public Value getValue() {

                        return this.value;
                    }

                    public void setValue(Value value) {

                        this.value = value;
                    }

                    public Type getType() {

                        return def.type;
                    }
                };

                if (initValues != null) {
                    v.setValue(initValues[i]);
                }
                values.put(def.name, v);
            }
        }

        this.varInstances.push(values);

        dbg.preExecute(this);

        try {
            for (int i = 0; i < this.commands.size(); i++) {
                // System.out.println("Executing: " + commands.get(i));
                this.commands.get(i).execute(dbg);
            }
        } finally {
            this.varInstances.pop();
        }
    }

    public ReturnInfo check(Collection<String> warnings) {

        ReturnInfo info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);

        for (int i = 0; i < this.commands.size(); i++) {
            if ((info.info == ReturnInfo.ON_ALL_PATHS)
                    || (info.breakInfo == ReturnInfo.ON_ALL_PATHS)) {
                throw new ExecutionException("Statement not reached");
            }
            info = info.append(this.commands.get(i).check(warnings));
        }

        return info;
    }

    @Override
    public String toString() {

        return "body of " + this.getSuperBlock();
    }

}
