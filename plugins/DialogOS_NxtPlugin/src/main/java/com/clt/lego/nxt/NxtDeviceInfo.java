package com.clt.lego.nxt;

import com.clt.lego.BrickInfo;
import com.clt.util.StringTools;

/**
 * @author dabo
 *
 */
public class NxtDeviceInfo extends BrickInfo {

    private byte[] bluetoothAddress;
    private int[] signalStrength;
    private int availableMemory;
    private int protocolVersion;

    public NxtDeviceInfo(String name, byte[] bluetoothAddress, int[] signalStrength, int availableMemory, int firmwareVersion, int protocolVersion) {
        super(name, firmwareVersion);

        this.bluetoothAddress = bluetoothAddress;
        this.signalStrength = signalStrength;
        this.availableMemory = availableMemory;
        this.protocolVersion = protocolVersion;
    }

    public int getAvailableMemory() {

        return this.availableMemory;
    }

    public byte[] getBluetoothAddress() {

        return this.bluetoothAddress;
    }

    public int[] getSignalStrength() {

        return this.signalStrength;
    }

    public int getProtocolVersion() {

        return this.protocolVersion;
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();

        b.append("Name    : " + this.getName() + "\n");
        b.append("Address : " + StringTools.toHexString(this.getBluetoothAddress(), ":") + "\n");
        b.append("Firmware: 0x" + StringTools.toHexString(this.getFirmwareVersion(), 4) + "\n");
        b.append("Protocol: 0x" + StringTools.toHexString(this.getProtocolVersion(), 4) + "\n");
        for (int i = 0; i < this.getSignalStrength().length; i++) {
            b.append("Signal " + i + " : " + this.getSignalStrength()[i] + "\n");
        }
        b.append("Memory  : " + this.getAvailableMemory() + " bytes");

        return b.toString();
    }
}
