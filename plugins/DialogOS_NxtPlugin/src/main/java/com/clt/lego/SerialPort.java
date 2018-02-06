package com.clt.lego;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import javax.usb.UsbException;

import purejavacomm.CommPort;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

public class SerialPort {

    private static final int CONNECTION_TIMEOUT = 3000;

    // private static final int BAUDRATE_BLUETOOTH = 460800;
    private static final int BAUDRATE_RCX = 4800;
    private static final int BAUDRATE_BLUETOOTH = 115200;

    private CommPortIdentifier portIdentifier;
    private purejavacomm.SerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    public SerialPort(String portName) throws IOException {

        try {
            this.portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException exn) {
            throw new IOException("Unknown port " + portName);
        }

        if (this.portIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL) {
            throw new IOException(portName + " is not a serial port");
        }
    }

    public String getPortname() {
        return this.portIdentifier.getName();
    }

    public void openForNxt() throws IOException {
        this.open(SerialPort.BAUDRATE_BLUETOOTH, purejavacomm.SerialPort.DATABITS_8,
                purejavacomm.SerialPort.STOPBITS_1, purejavacomm.SerialPort.PARITY_NONE);
    }

    public void openForRcx() throws IOException {
        this.open(SerialPort.BAUDRATE_RCX, purejavacomm.SerialPort.DATABITS_8,
                purejavacomm.SerialPort.STOPBITS_1, purejavacomm.SerialPort.PARITY_NONE);
    }

    public void open(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
        if (this.portIdentifier.isCurrentlyOwned()) {
            throw new IOException("Port is currently in use by " + this.portIdentifier.getCurrentOwner());
        } else {
            CommPort port;
            try {
                port = this.portIdentifier.open(this.portIdentifier.getName(), SerialPort.CONNECTION_TIMEOUT);
            } catch (PortInUseException exn) {
                throw new IOException("Port is already in use by " + this.portIdentifier.getCurrentOwner());
            }

            if (port instanceof purejavacomm.SerialPort) {
                this.serialPort = (purejavacomm.SerialPort) port;

                try {
                    serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
                    serialPort.enableReceiveTimeout(SerialPort.CONNECTION_TIMEOUT);
                } catch (UnsupportedCommOperationException exn) {
                    port.close();
                    throw new IOException("Unsupported port parameters");
                }

                // This allows NXT programs to be run without resetting NXT each time.
                serialPort.setRTS(true);
                serialPort.setDTR(true);

                this.in = serialPort.getInputStream();
                this.out = serialPort.getOutputStream();
            } else {
                port.close();
                throw new IOException(this.portIdentifier.getName() + " is not a serial port.");
            }
        }
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public void close() {
        this.serialPort.close();

        try {
            this.in.close();
        } catch (IOException exn) {
            // ignore
        }

        try {
            this.out.close();
        } catch (IOException exn) {
            // ignore
        }
    }

    public static String[] getAvailablePorts() {
        Collection<String> ports = new ArrayList<String>();
        for (Enumeration<?> e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements();) {
            CommPortIdentifier info = (CommPortIdentifier) e.nextElement();
            if (info.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                ports.add(info.getName());
            }
        }

        return ports.toArray(new String[ports.size()]);
    }


    public static void main(String[] args) throws IOException, InterruptedException, UsbException {

        for (String x : getAvailablePorts()) {
            System.err.println("\n\n");
            System.err.println(x);

//            if (x.contains("NXT")) {
            SerialPort sp = new SerialPort(x);

            try {
                sp.openForNxt();
                System.err.println("opened");
                System.err.println(sp.in);
            } catch (Exception e) {
                System.err.println(e);
            } finally {
                if (sp.in != null) {
                    sp.close();
                }
            }

            /*
                System.err.println("  id: " + sp.portIdentifier);
                System.err.println("  owned: " + sp.portIdentifier.isCurrentlyOwned());

                CommPort port = null;

                try {
                    port = sp.portIdentifier.open(sp.portIdentifier.getName(), 1000);
                } catch (PortInUseException ex) {
                    Logger.getLogger(SerialPort.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (port != null) {
                        port.close();
                        Thread.sleep(500);
                    }
                }
             */
//            }
        }
    }
}
