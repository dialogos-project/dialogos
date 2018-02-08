package com.clt.lego.nxt;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.lego.BrickUtils;
import com.clt.lego.SerialPort;
import com.clt.util.Platform;
import java.util.List;

/**
 * Implementation of the communication with the Lego NXT brick over Bluetooth.
 * (This was called NxtSerial in the commercial version.)
 *
 * @author dabo
 *
 */
public class NxtBluetooth extends AbstractNxt {

    private SerialPort port;

    private NxtBluetooth(String port) throws IOException {
        this.port = new SerialPort(port);
        this.port.openForNxt();
    }

    @Override
    public String getPort() {
        return this.port.getPortname();
    }

    @Override
    public String getResourceString() {
        return this.port.getPortname();
    }

    @Override
    public void close() {
        if (this.port != null) {
            this.port.close();
        }
    }

    @Override
    protected byte[] sendDirectCommand(byte[] command, int expectedResponseSize) throws IOException {
        boolean includeLengthHeader = this.getInterfaceType() == InterfaceType.Bluetooth;
        int offset = includeLengthHeader ? 2 : 0;

        byte[] cmd = new byte[command.length + 1 + offset];

        if (includeLengthHeader) {
            int msgSize = command.length + 1;
            cmd[0] = (byte) (msgSize & 0xFF);
            cmd[1] = (byte) (msgSize >>> 8);
        }

        cmd[offset] = expectedResponseSize > 0 ? (byte) 0x00 : (byte) 0x80;
        System.arraycopy(command, 0, cmd, offset + 1, command.length);

        this.port.getOutputStream().write(cmd);

        if (expectedResponseSize > 0) {
            byte[] response = readResponse();

            if (response.length != expectedResponseSize + 2) {
                throw new IOException("Received response of invalid length: expected " + (expectedResponseSize + 2) + ", got " + response.length);
            }

            if (response[0] != 2) {
                throw new IOException("Invalid first byte in response: expected 2, got " + response[0]);
            }

            if (response[1] != command[0]) {
                throw new IOException("First byte of answer is not the command ID");
            }

            byte[] ret = new byte[expectedResponseSize];
            System.arraycopy(response, 2, ret, 0, expectedResponseSize);

            return ret;
        } else {
            return null;
        }
    }

    @Override
    protected byte[] sendSystemCommand(byte[] command, int expectedResponseLength) throws IOException {
        // prepend with Bluetooth length header, plus 0x01 for system command
        byte[] cmd = new byte[command.length + 3];

        int msgSize = command.length + 1;
        cmd[0] = (byte) (msgSize & 0xFF);
        cmd[1] = (byte) (msgSize >>> 8);
        cmd[2] = 0x01;

        System.arraycopy(command, 0, cmd, 3, command.length);

        this.port.getOutputStream().write(cmd);

        // receive response
        byte[] response = readResponse();

        if (response[0] != 0x02) {
            throw new IOException("First byte of response should have been 0x02");
        }

        if (response[1] != cmd[3]) {
            throw new IOException("Second byte of response should have been system command (" + Integer.toHexString(cmd[1]) + ")");
        }

        return response;
    }

    private byte[] send(int d1, int d2) throws IOException {
        if (this.getInterfaceType() == InterfaceType.Bluetooth) {
            this.port.getOutputStream().write(new byte[]{0x02, 0x00, (byte) d1, (byte) d2});
        } else {
            this.port.getOutputStream().write(new byte[]{(byte) d1, (byte) d2});
        }

        return readResponse();
    }

    private byte[] readResponse() throws IOException {
        int responseLength = (int) BrickUtils.readNum(port.getInputStream(), 2, false);
        byte[] response = new byte[responseLength];
        port.getInputStream().read(response);

        return response;
    }

    public static void hexdump(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            int val = data[i];

            if (val < 0) {
                val += 256;
            }

            System.err.printf("%d: %d\t0x %s\tchar %c\n", i, val, Integer.toHexString(val), val);
        }
    }

    @Override
    public String[] getModules() throws IOException {

        // find first
        byte[] findFirst = new byte[22];
        findFirst[0] = 0x01;
        findFirst[1] = (byte) 0x90;
        findFirst[2] = (byte) '*';
        for (int i = 0; i < Nxt.MODULE_EXTENSION.length(); i++) {
            findFirst[3 + i] = (byte) Nxt.MODULE_EXTENSION.charAt(i);
        }
        findFirst[3 + Nxt.MODULE_EXTENSION.length()] = 0;
        this.port.getOutputStream().write(findFirst);

        int answer = this.port.getInputStream().read();
        if (answer != 0x02) {
            throw new IOException();
        }
        answer = this.port.getInputStream().read();
        if (answer != 0x90) {
            throw new IOException("First byte of answer is not the command id");
        }

        byte[] response = new byte[32];
        this.port.getInputStream().read(response);

        Collection<String> files = new ArrayList<String>();
        while (response[0] == 0) {
            byte fileHandle = response[1];
            files.add(BrickUtils.readString(response, 2, 20));

            byte[] findNext = new byte[]{0x01, (byte) 0x91, fileHandle};
            this.port.getOutputStream().write(findNext);

            answer = this.port.getInputStream().read();
            if (answer != 0x02) {
                throw new IOException();
            }
            answer = this.port.getInputStream().read();
            if (answer != 0x91) {
                throw new IOException("First byte of answer is not the command id");
            }

            response = new byte[32];
            this.port.getInputStream().read(response);

            // close previous handle
            this.port.getOutputStream().write(
                    new byte[]{0x01, (byte) 0x92, fileHandle});
            this.port.getInputStream().read(new byte[4]);
        }
        if (response[0] != (byte) 0xBD) {
            AbstractNxt.checkStatus(response);
        }

        return files.toArray(new String[files.size()]);
    }

    public InterfaceType getInterfaceType() {

        return InterfaceType.Bluetooth;
    }

    public int getModuleID(String module) {

        // TODO: Implement getModuleID
        throw new UnsupportedOperationException();
    }

    public void writeIOMap(String module, int offset, byte[] data) {

        // TODO: Implement writeIOMap
        throw new UnsupportedOperationException();
    }

    public byte[] readIOMap(String module, int offset, int length) {

        // TODO: Implement readIOMap
        throw new UnsupportedOperationException();
    }

    private static void link() throws IOException {
        try {
            NxtBluetooth.class.getClassLoader().loadClass("purejavacomm.CommPort");
        } catch (Throwable error) {
            String msg = error.getLocalizedMessage();
            if ((msg != null) && (msg.length() > 0)) {
                throw new IOException(msg);
            } else {
                throw new IOException("The serial driver could not be loaded");
            }
        }
    }

    private static NxtBluetooth createBrick(Component parent, String uri) throws IOException {
        return new NxtBluetooth(uri);
    }

    public static BrickFactory<Nxt> getFactory() throws IOException {
        NxtBluetooth.link();

        return new BrickFactory<Nxt>() {
            public String[] getAvailablePorts() {
                List<String> relevantAvailablePorts = new ArrayList<>();

                for (String port : SerialPort.getAvailablePorts()) {
                    if (Platform.isMac()) {
                        // On MacOS, all serial ports that are meant for outgoing connections
                        // have a name starting with "cu." (as opposed to "tty.". Skip the
                        // others, to avoid duplicate copies of the NXT brick in the GUI.

                        if (port.startsWith("cu.") && port.contains("NXT")) {
                            relevantAvailablePorts.add(port);
                        }
                    } else {
                        relevantAvailablePorts.add(port);
                    }
                }

                return relevantAvailablePorts.toArray(new String[0]);
            }

            public BrickDescription<Nxt> getBrickInfo(Component parent, String port) throws IOException {
                Nxt nxt = NxtBluetooth.createBrick(parent, port);
                BrickDescription<Nxt> info;
                try {
                    InterfaceType type = nxt.getInterfaceType();
                    NxtDeviceInfo di = nxt.getDeviceInfo();
                    info = (di == null) ? null : new Description(port, di, type, nxt.getPort());
                } finally {
                    nxt.close();
                }
                return info;
            }
        };
    }

    public static class Description extends BrickDescription<Nxt> {

        public Description(String uri, NxtDeviceInfo brickInfo, InterfaceType type, String port) {
            super(uri, brickInfo, type, port);
        }

        @Override
        protected NxtBluetooth createBrickImpl(Component parent) throws IOException {
            return NxtBluetooth.createBrick(parent, this.getURI());
        }
    }

    public static void main(String[] args) throws IOException {
        for (String port : SerialPort.getAvailablePorts()) {
            NxtBluetooth nx = null;
            if (port.contains("NXT") && port.startsWith("cu")) {
                try {
                    System.err.println("\n\n\n");
                    System.err.println(port);
                    nx = new NxtBluetooth(port);
                    NxtDeviceInfo info = nx.getDeviceInfo();
                    System.out.println(info);

//                    nx.getPrograms();
//                    nx.playTone(440, 1000);
                    nx.startProgram("Unbenannt-1");

                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    if (nx != null) {
                        nx.close();
                    }
                }
            }
        }
    }

}
