package com.clt.diamant;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;

class DeviceValue extends Value {

    public static final Type TYPE = Type.createType("device", DeviceValue.class);

    private Device device;

    public DeviceValue(Device device) {

        if (device == null) {
            throw new IllegalArgumentException();
        }
        this.device = device;
    }

    @Override
    public Value copyValue() {

        return new DeviceValue(this.device);
    }

    @Override
    public int hashCode() {

        return this.device.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof DeviceValue) {
            return this.device.equals(((DeviceValue) o).getDevice());
        } else {
            return false;
        }
    }

    public Device getDevice() {

        return this.device;
    }

    @Override
    public String toString() {

        return this.device.toString();
    }

    @Override
    public Type getType() {

        return DeviceValue.TYPE;
    }

    @Override
    public Object getReadableValue() {
        return null;
    }
}
