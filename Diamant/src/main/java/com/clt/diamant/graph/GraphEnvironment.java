package com.clt.diamant.graph;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.clt.diamant.Grammar;
import com.clt.diamant.Slot;
import com.clt.script.Environment;
import com.clt.script.Script;
import com.clt.script.cmd.Proc;
import com.clt.script.cmd.Prototype;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;
import com.clt.script.exp.expressions.Function;
import com.clt.script.exp.values.StringValue;

class GraphEnvironment implements Environment {

    private Graph graph;
    private boolean scope;

    public GraphEnvironment(Graph graph, boolean scope) {

        this.graph = graph;
        this.scope = scope;
    }

    private Slot getSlot(String name) {

        List<Slot> l = this.graph.getAllVariables(this.scope);
        for (int i = l.size() - 1; i >= 0; i--) {
            Slot v = l.get(i);
            if (name.equals(v.getName())) {
                return v;
            }
        }
        return null;
    }

    public Variable createVariableReference(final String name) {

        final Slot s = this.getSlot(name);
        if (s != null) {
            return new Variable() {

                public Type getType() {

                    return s.getType();
                }

                public Value getValue() {

                    return s.getValue();
                }

                public void setValue(Value value) {

                    s.setValue(value);
                }

                public String getName() {

                    return name;
                }

                @Override
                public String toString() {

                    return GraphEnvironment.this.graph.graphPath(false).toString() + ":"
                            + this.getName();
                }
            };
        } else {
            return this.graph.getOwner().getEnvironment(Graph.GLOBAL)
                    .createVariableReference(name);
        }
    }

    public Expression createFunctionCall(final String name,
            final Expression[] arguments) {

        if (name.equals("getGrammar") && (arguments.length == 1)) {
            return new Function(name, arguments) {

                @Override
                protected Value eval(Debugger dbg, Value[] args) {

                    if (args[0] instanceof StringValue) {
                        String grammar = ((StringValue) args[0]).getString();
                        if (GraphEnvironment.this.graph.getOwner() != null) {
                            for (Grammar g : GraphEnvironment.this.graph.getLocalGrammars()) {
                                if (g.getName().equals(grammar)) {
                                    return new StringValue(g.getGrammar());
                                }
                            }
                        }
                        throw new EvaluationException("Unknown grammar '" + grammar + "'");
                    } else {
                        throw new EvaluationException(
                                "Argument to getGrammar() must be a string.");
                    }
                }

                @Override
                public Type getType() {

                    return Type.String;
                }
            };
        } else {
            try {
                Script s = this.graph.getCompiledScript();
                final Prototype pt = s.getProcedure(name, arguments.length);
                boolean matches = (pt != null);
                for (int i = 0; matches && (i < pt.numParameters()); i++) {
                    try {
                        Type.unify(pt.getParameterType(i), arguments[i].getType());
                    } catch (Exception exn) {
                        matches = false;
                    }
                }
                if ((pt != null) && matches) {
                    final Proc p = pt.getProcedure();
                    if (p == null) {
                        throw new TypeException("Function " + name
                                + "() is defined in graph \""
                                + this.graph.getName()
                                + "\" but not implemented.");
                    }
                    return new Function(p.getName(), arguments) {

                        @Override
                        protected Value eval(Debugger dbg, Value[] args) {

                            return p.call(dbg, args);
                        }

                        @Override
                        public Type getType() {

                            for (int i = 0; (pt != null) && (i < pt.numParameters()); i++) {
                                Type.unify(pt.getParameterType(i), arguments[i].getType());
                            }

                            return p.getReturnType();
                        }
                    };
                }
            } catch (Exception exn) {
            }

            return this.graph.getOwner().getEnvironment(Graph.GLOBAL)
                    .createFunctionCall(name, arguments);
        }
    }

    public Type getType(String typeName) {

        return this.graph.getOwner().getEnvironment(Graph.GLOBAL).getType(typeName);
    }

    public Reader include(String id)
            throws IOException {

        return this.graph.getOwner().getEnvironment(Graph.GLOBAL).include(id);
    }
}
