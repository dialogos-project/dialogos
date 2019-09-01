package com.clt.diamant;

import com.clt.dialog.client.ConnectionState;
import com.clt.script.DefaultEnvironment;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.*;
import com.clt.script.exp.expressions.Function;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.StringValue;

import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.Collection;

public class DeviceAwareEnvironment extends DefaultEnvironment {

    Collection<Device> devices;

    public DeviceAwareEnvironment(Collection<Device> devices) {
        super();
        this.devices = devices;
        registerFunction("DialogOS", new ExecutableFunctionDescriptor("rpc", new TypeVariable(), new Type[] {DeviceValue.TYPE, Type.String}) {
            @Override
            public Value eval(Value[] args) {
                if (!(args[0] instanceof DeviceValue)
                        || !(args[1] instanceof StringValue)) {
                    throw new EvaluationException(
                            "Wrong type of arguments in call to function rpc()");
                }
                try {
                    Device d = ((DeviceValue) args[0]).getDevice();
                    String procedure = ((StringValue) args[1]).getString();
                    Value[] as = new Value[args.length - 2];
                    System.arraycopy(args, 2, as, 0, args.length - 2);
                    try {
                        return d.rpc(procedure, as);
                    } catch (ConnectException exn) {
                        throw new EvaluationException(
                                "RPC failed because the device \""
                                        + d.getName() + "\" is not connected");
                    } catch (RemoteException exn) {
                        throw new EvaluationException(
                                "RPC failed because the remote procedure " + procedure
                                        + "() raised an error: "
                                        + exn.getLocalizedMessage());
                    }
                } catch (Exception exn) {
                    throw new EvaluationException(exn.getLocalizedMessage());
                }
            }
        });
        registerFunction("DialogOS", new ExecutableFunctionDescriptor("isConnected", Type.Bool, new Type[]{}) {
            @Override
            public Value eval(Value[] args) {
                if (!(args[0] instanceof DeviceValue)) {
                    throw new EvaluationException(
                            "Wrong type of arguments in call to function isConnected()");
                }
                DeviceValue d = (DeviceValue) args[0];
                return new BoolValue(
                        d.getDevice().getState() == ConnectionState.CONNECTED);
            }
        });

    }

    @Override
    public Variable createVariableReference(final String name) {
        for (final Device d : devices) {
            if (d.getName().equals(name)) {
                return new Variable() {

                    public String getName() {
                        return name;
                    }

                    public Value getValue() {
                        return new DeviceValue(d);
                    }

                    public void setValue(Value value) {

                    }

                    public Type getType() {
                        return DeviceValue.TYPE;
                    }
                };
            }
        }
        return super.createVariableReference(name);
    }

}
