/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.lego.nxt;

import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.util.UserCanceledException;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;

/**
 *
 * @author koller
 */
public class NxtUsb extends AbstractNxt {
    private static final boolean DEBUG_DATATRANSFERS = false;
    private static Map<String, UsbDevice> nxtDevices;
    private final String port;
    private final UsbDevice device;
    private final UsbInterface iface;
    private static final Pattern USB_LOCATION_PATTERN = Pattern.compile(".*?(\\d+).*?(\\d+).*");

    static {
        // At program startup time, restore the USB devices. This makes it possible
        // to save a dialogue to a file (where the old portname is stored) and then
        // load it again, and run it without having to go to "Find NXT bricks".
        // This will work as long as there is always at most one NXT brick attached
        // over USB, which is then called "USB1". If there are multiple bricks,
        // the user will have to do "Find NXT bricks" manually each time.
        discoverNxtDevices();
    }

    /**
     * Refresh the list of known attached NXT devices. This removes previously
     * discovered devices if they are no longer attached. Each device is
     * assigned a unique "port" name.
     *
     */
    private static void discoverNxtDevices() {
        nxtDevices = new HashMap<>();

        try {
            List<UsbDevice> nxtDev = getUsbNxtDevices();
            int id = 1;

            for (UsbDevice dev : nxtDev) {
                String port = makeUsbLocation(dev);
                nxtDevices.put(port, dev);
                id++;
            }
        } catch (UsbException ex) {
            Logger.getLogger(NxtUsb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String makeUsbLocation(UsbDevice dev) {
        String baseString = dev.toString();
        Matcher m = USB_LOCATION_PATTERN.matcher(baseString);

        if (m.matches()) {
            return String.format("Bus %s, Device %s", m.group(1), m.group(2));
        } else {
            return baseString;
        }
    }

    private static void traverseUsbTreeForNxt(UsbHub hub, List<UsbDevice> nxts) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                traverseUsbTreeForNxt((UsbHub) device, nxts);
            } else {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == 0x0694 && desc.idProduct() == 0x0002) {
                    nxts.add(device);
                }
            }
        }
    }

    private static List<UsbDevice> getUsbNxtDevices() throws UsbException {
        List<UsbDevice> ret = new ArrayList<>();
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub hub = services.getRootUsbHub();

        traverseUsbTreeForNxt(hub, ret);
        return ret;
    }

    private NxtUsb(String port) throws IOException {
        this.port = port;
        device = nxtDevices.get(port);

        if (device == null) {
            throw new IOException(Resources.getString("UsbNxtDisconnected"));
        }

        UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
        if (configuration == null) {
            throw new IOException(Resources.getString("UsbNxtDisconnected"));
        }

        iface = configuration.getUsbInterface((byte) 0);

        try {
            iface.claim();
        } catch (UsbException | UsbNotActiveException | UsbDisconnectedException ex) {
            throw new IOException(Resources.getString("UsbNxtDisconnected"));
        }
    }

    public static BrickFactory<Nxt> getFactory() throws IOException {
        return new BrickFactory<Nxt>() {
            public String[] getAvailablePorts() {
                discoverNxtDevices();

                String[] ret = new String[nxtDevices.keySet().size()];
                int i = 0;
                for (String port : nxtDevices.keySet()) {
                    ret[i++] = port;
                }

                return ret;
            }

            public BrickDescription<Nxt> getBrickInfo(Component parent, String port) throws IOException {
                Nxt nxt = new NxtUsb(port);
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
        protected NxtUsb createBrickImpl(Component parent) throws IOException {
            return new NxtUsb(this.getURI());
        }
    }

    @Override
    protected byte[] sendSystemCommand(byte[] command, int expectedResponseLength) throws IOException {
        try {
            // prepend with 0x01 for system command
            byte[] cmd = new byte[command.length + 1];
            cmd[0] = 0x01;
            System.arraycopy(command, 0, cmd, 1, command.length);
            int bytesSent = send(cmd);

            if (bytesSent != cmd.length) {
                throw new IOException("Command sent incompletely: only transmitted " + bytesSent + " bytes instead of " + cmd.length);
            }

            // receive response
            byte[] response = read(expectedResponseLength);

            if (response[0] != 0x02) {
                throw new IOException("First byte of response should have been 0x02");
            }

            if (response[1] != cmd[1]) {
                throw new IOException("Second byte of response should have been system command (" + Integer.toHexString(cmd[1]) + ")");
            }

            return response;
        } catch (UsbException ex) {
            throw new IOException(ex);
        }
    }


    @Override
    public String[] getModules() throws IOException {
        throw new UnsupportedOperationException("getModules: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getModuleID(String name) throws IOException {
        throw new UnsupportedOperationException("getModuleID: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] readIOMap(String module, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("readIOMap: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeIOMap(String module, int offset, byte[] data) throws IOException {
        throw new UnsupportedOperationException("writeIOMap: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InterfaceType getInterfaceType() {
        return InterfaceType.USB;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getResourceString() throws IOException {
        return getPort();
    }

    @Override
    public void close() throws IOException {
        if (iface != null) {
            try {
                iface.release();
            } catch (UsbException | UsbNotActiveException | UsbDisconnectedException ex) {
                throw new IOException("Error while releasing USB interface", ex);
            }
        }
    }

    @Override
    protected byte[] sendDirectCommand(byte[] command, int expectedResponseSize) throws IOException {
        try {
            // prepend command with 0x00 or 0x80, depending on whether response is expected
            byte[] cmd = new byte[command.length + 1];
            cmd[0] = expectedResponseSize > 0 ? (byte) 0x00 : (byte) 0x80;
            System.arraycopy(command, 0, cmd, 1, command.length);

            int bytesSent = send(cmd);

            if (bytesSent != cmd.length) {
                throw new IOException("Error while sending direct command to NXT: Sent " + bytesSent + " instead of " + cmd.length + " bytes.");
            }

            byte[] response = read(64);
            if (response[0] != 2) {
                throw new IOException("Invalid first byte in response: expected 2, got " + response[0]);
            }

            if (response[1] != cmd[1]) {
                NxtBluetooth.hexdump(cmd);
                NxtBluetooth.hexdump(response);
                throw new IOException("First byte of answer is not the command ID");
            }

            byte[] ret = new byte[expectedResponseSize];
            System.arraycopy(response, 2, ret, 0, expectedResponseSize);

            return ret;
        } catch (UsbException ex) {
            throw new IOException(ex);
        }
    }

    private UsbEndpoint findEndpoint(byte direction) {
        UsbEndpoint ret = null;

        for (UsbEndpoint ep : (List<UsbEndpoint>) iface.getUsbEndpoints()) {
            if ((ep.getDirection() & UsbConst.ENDPOINT_DIRECTION_MASK) == direction) {
                ret = ep;
            }
        }

        return ret;
    }

    private int send(byte[] data) throws UsbException {
        UsbEndpoint endpoint = findEndpoint(UsbConst.ENDPOINT_DIRECTION_OUT);

        if (endpoint == null) {
            throw new UsbException("Could not find endpoint for sending");
        }

        if (DEBUG_DATATRANSFERS) {
            System.err.println("send:");
            NxtBluetooth.hexdump(data);
        }

        UsbPipe pipe = endpoint.getUsbPipe();
        pipe.open();
        try {
            int sent = pipe.syncSubmit(data);
            return sent;
        } finally {
            pipe.close();
        }
    }

    private void send(int... data) throws UsbException {
        byte[] d = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = (byte) data[i];
        }
        send(d);
    }

    private byte[] read(int numBytes) throws UsbException {
        UsbEndpoint endpoint = findEndpoint(UsbConst.ENDPOINT_DIRECTION_IN);

        if (endpoint == null) {
            throw new UsbException("Could not find endpoint for receiving");
        }

        UsbPipe pipe = endpoint.getUsbPipe();
        pipe.open();

        try {
            byte[] data = new byte[numBytes];
            int received = pipe.syncSubmit(data);
            
            if( DEBUG_DATATRANSFERS ) {
                System.err.println("receive:");
                NxtBluetooth.hexdump(data);
            }
            
            return data;
        } finally {
            pipe.close();
        }
    }

    public static void main(String[] args) throws IOException, IOException, UserCanceledException {
        BrickFactory<Nxt> factory = NxtUsb.getFactory();

        for (String port : factory.getAvailablePorts()) {
            System.err.println("info: " + factory.getBrickInfo(null, port));
        }

    }

}
