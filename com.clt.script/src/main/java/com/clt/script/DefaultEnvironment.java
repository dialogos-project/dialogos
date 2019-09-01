package com.clt.script;

import java.util.*;
import java.util.stream.Collectors;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.*;
import com.clt.script.exp.expressions.Function;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DefaultEnvironment extends BasicEnvironment {

    // function descriptions per class
    private Map<String, List<ExecutableFunctionDescriptor>> groupedFunctionDescriptions = new HashMap<>();
    // function descriptions by callable name (for efficient lookup; TODO: we should allow multiple functions with the same name but different argument types
    private Map<String, ExecutableFunctionDescriptor> functionDescriptionsByName = new HashMap<>();

    public List<String> getFunctionGroups() {
        List<String> groups = new ArrayList<>(groupedFunctionDescriptions.keySet());
        groups.add(0, "Builtin");
        return groups;
    }

    public void registerFunction(String group, ExecutableFunctionDescriptor efd) {
        if (!groupedFunctionDescriptions.containsKey(group))
            groupedFunctionDescriptions.put(group, new ArrayList<>());
        groupedFunctionDescriptions.get(group).add(efd);
        assert !functionDescriptionsByName.containsKey(efd.getName()) : "A function of this name already exists";
        functionDescriptionsByName.put(efd.getName(), efd);
    }

    public List<String> getFunctions(String group, boolean html) {
        Collection<? extends FunctionDescriptor> functions;
        if (group.equals("Builtin")) {
            functions = builtin.getMethods();
        } else {
            assert groupedFunctionDescriptions.containsKey(group);
            functions = groupedFunctionDescriptions.get(group);
        }
        return functions.stream().map(f -> f.getDescription(html)).collect(Collectors.toList());
    }

    @Override
    public Expression createFunctionCall(final String name,
                                         final Expression[] arguments) {
        if (functionDescriptionsByName.containsKey(name)) {
            final ExecutableFunctionDescriptor efd = functionDescriptionsByName.get(name);
            if (efd.getParameterTypes().length != arguments.length)
                throw new TypeException(
                        "Wrong number of arguments in call to function rpc()");
            return new Function(name, arguments) {
                @Override protected Value eval(Debugger dbg, Value[] args) {
                    return efd.eval(args);
                }
                @Override public Type getType() {
                    return efd.getReturnType();
                }
            };
        } else {
            return super.createFunctionCall(name, arguments);
        }
    }

}
