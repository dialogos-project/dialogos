package com.clt.dialog.client;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * @author Daniel Bobbert
 * @version 6.5
 */
class Rendezvous {

    private static final boolean VERBOSE = false;

    // All instances share a single JmDNS instance.
    private static JmDNS[] rendezvous = null;

    private final static String SERVICE_TYPE = "_cltdialog._tcp.local.";

    static final String SERVICE_PROPERTY_NAME = "name";
    static final String SERVICE_PROPERTY_LOCAL = "local";
    static final String SERVICE_PROPERTY_PROTOCOL = "protocol";
    static final String SERVICE_PROPERTY_VERSION = "interface_version";

    private static Map<String, ServiceInfo> clients = null;
    private static ClientListModel clientList = null;

    static {
        // Cache service information for faster access
        try {
            Rendezvous.initRendezvous();

            Rendezvous.addServiceListener(new ServiceListener() {
                private String normalizeName(String name, String type) {
                    if (name.endsWith("." + type)) {
                        name = name.substring(0, name.length() - (type.length() + 1));
                    } else if (name.endsWith(".")) {
                        name = name.substring(0, name.length() - 1);
                    }
                    return name;
                }

                @Override
                public void serviceAdded(ServiceEvent event) {
//                    name = this.normalizeName(name, type);
//                    jmdns.requestServiceInfo(type, name + "." + type);
                }

                @Override
                public void serviceRemoved(ServiceEvent e) {
                    if (e.getType().equals(Rendezvous.SERVICE_TYPE)) {
                        String name = this.normalizeName(e.getName(), e.getType());

                        if (Rendezvous.VERBOSE) {
                            System.err.println("Service removed: " + e);
                        }

                        synchronized (Rendezvous.clientList) {
                            Rendezvous.clients.remove(name);
                            Rendezvous.clientList.update();
                        }
                    }
                }

                @Override
                public void serviceResolved(ServiceEvent e) {
                    if (e.getType().equals(Rendezvous.SERVICE_TYPE) && (e.getInfo() != null)) {
                        String name = this.normalizeName(e.getName(), e.getType());

                        if (Rendezvous.VERBOSE) {
                            System.err.println("Service resolved: " + e);
                        }

                        synchronized (Rendezvous.clientList) {
                            Rendezvous.clients.put(name, e.getInfo());
                            Rendezvous.clientList.update();
                        }
                    }
                }
            });
        } catch (Exception exn) {
            System.err.println("Failed to initialize Rendezvous.");
            exn.printStackTrace(System.err);
        }
    }

    // just an empty method that one can call to force loading of the class
    public static void initialize() {

    }

    private static void initRendezvous() throws IOException {
        InetAddress addr[] = null;

        Rendezvous.clients = new HashMap<String, ServiceInfo>();
        Rendezvous.clientList = new ClientListModel();

        try {
            Collection<InetAddress> addrs = new HashSet<InetAddress>();
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            if (!nifs.hasMoreElements()) {
                System.err.println("No network interfaces found.");
            }

            while (nifs.hasMoreElements()) {
                NetworkInterface nif = nifs.nextElement();
                String name = nif.getDisplayName();
                if (Rendezvous.VERBOSE) {
                    System.out.println("Found network interface " + name + ":");
                }
                Enumeration<InetAddress> ips = nif.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress address = ips.nextElement();
                    if (Rendezvous.VERBOSE) {
                        System.out.println("  " + address);
                    }
                    if (address instanceof Inet4Address) {
                        addrs.add(address);
                    }
                }
            }
            // addrs.add(InetAddress.getLocalHost());
            addr = addrs.toArray(new InetAddress[addrs.size()]);
        } catch (Exception exn) {
            if (Rendezvous.VERBOSE) {
                System.err.println("Could not determine network interfaces. Using localhost.");
            }
            addr = new InetAddress[]{InetAddress.getLocalHost()};
            // addr[] = new InetAddress[] { new ServerSocket(0).getInetAddress()
            // };
        }

        addr = new InetAddress[]{InetAddress.getLocalHost()};

        Rendezvous.rendezvous = new JmDNS[addr.length];
        for (int i = 0; i < addr.length; i++) {
            Rendezvous.rendezvous[i] = JmDNS.create(addr[i]); //  new JmDNS(addr[i]);
            Rendezvous.rendezvous[i].registerServiceType(Rendezvous.SERVICE_TYPE);
            if (Rendezvous.VERBOSE) {
                System.out.println("jmdns created on address " + addr[i]);
                System.out.println("rendezvous type: " + Rendezvous.SERVICE_TYPE);
                System.out.println("Rendezvous activated on interface " + Rendezvous.rendezvous[i].getInterface());
            }
        }
    }

    public static ServiceInfo createService(String name, int port, String version, boolean localhostOnly) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Rendezvous.SERVICE_PROPERTY_VERSION, version);
        properties.put(Rendezvous.SERVICE_PROPERTY_PROTOCOL, "XML, BIN");
        if (localhostOnly) {
            properties.put(Rendezvous.SERVICE_PROPERTY_LOCAL, "yes");
        }
        properties.put(Rendezvous.SERVICE_PROPERTY_NAME, name);

        return ServiceInfo.create(Rendezvous.SERVICE_TYPE, name, port, 0, 0, properties);
        // AK 01/18: Removed duplicate ".<SERVICE_TYPE>" from service name,
        // because it confused (modern?) Bonjour browsers.
    }

    public static void addService(ServiceInfo service) throws IOException {
        if (Rendezvous.rendezvous == null) {
            throw new IOException("Rendezvous not initialized");
        }

        for (int i = 0; i < Rendezvous.rendezvous.length; i++) {
            Rendezvous.rendezvous[i].registerService(service);

            if (Rendezvous.VERBOSE) {
                System.err.println("Rendezvous service activated: " + service);
            }
        }
    }

    public static void removeService(ServiceInfo service) throws IOException {
        if (Rendezvous.rendezvous == null) {
            throw new IOException("Rendezvous not initialized");
        }

        for (int i = 0; i < Rendezvous.rendezvous.length; i++) {
            Rendezvous.rendezvous[i].unregisterService(service);
        }
    }

    public static void addServiceListener(ServiceListener l)
            throws IOException {

        if (Rendezvous.rendezvous == null) {
            throw new IOException("Rendezvous not initialized");
        }

        for (int i = 0; i < Rendezvous.rendezvous.length; i++) {
            Rendezvous.rendezvous[i].addServiceListener(Rendezvous.SERVICE_TYPE, l);
        }
    }

    static ListModel getAllClients() throws IOException {
        if (Rendezvous.rendezvous == null) {
            throw new IOException("Rendezvous not initialized");
        }

        return Rendezvous.clientList;
    }
    
    private static boolean contains(byte[] value, byte[][] array) {
        for( int i = 0; i < array.length; i++ ) {
            if( Arrays.equals(value, array[i])) {
                return true;
            }
        }
        
        return false;
    }

    static ServiceInfo[] getServiceInfo(String name, String server) throws IOException {
        if (Rendezvous.rendezvous == null) {
            throw new IOException("Rendezvous not initialized");
        }

        Collection<ServiceInfo> infos = new ArrayList<ServiceInfo>();

        if (server != null) {
            server = server.trim();
            if (server.length() == 0) {
                server = null;
            }
        }

        for (ServiceInfo info : Rendezvous.clients.values()) {
            String clientName = info.getPropertyString(Rendezvous.SERVICE_PROPERTY_NAME);
            boolean local = "yes".equals(info.getPropertyString(Rendezvous.SERVICE_PROPERTY_LOCAL));

            byte[] localhostAddress = InetAddress.getLocalHost().getAddress();
            byte[][] infoAddresses = new byte[info.getInetAddresses().length][];
            for (int i = 0; i < info.getInetAddresses().length; i++) {
                byte[] v = info.getInetAddresses()[i].getAddress();
                infoAddresses[i] = v;
            }
            
            if ((name == null) || info.getName().equals(name) || ((clientName != null) && clientName.equals(name))) {
                if (local ? contains(localhostAddress, infoAddresses) : true) {
                    if (server != null) {
                        byte[] serverAddress;
                        try {
                            InetAddress addr;
                            if (server.equalsIgnoreCase("localhost")) {
                                addr = InetAddress.getLocalHost();
                            } else {
                                addr = InetAddress.getByName(server);
                            }
                            serverAddress = addr.getAddress();
                        } catch (IOException exn) {
                            System.err.println(exn);
                            throw exn;
                        }

                        if (contains(serverAddress, infoAddresses) ) {
                            infos.add(info);
                        }
                    } else {
                        infos.add(info);
                    }
                }
            }
        }

        return infos.toArray(new ServiceInfo[infos.size()]);
    }

    public static String[] getLocalHostAddresses() throws IOException {
        String[] adr = new String[Rendezvous.rendezvous.length];
        for (int i = 0; i < Rendezvous.rendezvous.length; i++) {
            adr[i] = Rendezvous.rendezvous[i].getInterface().getHostAddress();
        }
        return adr;
    }

    private static class ClientListModel extends AbstractListModel {
        List<ServiceInfoDescription> cs = new ArrayList<ServiceInfoDescription>();

        public synchronized int getSize() {

            return this.cs.size();
        }

        public synchronized Object getElementAt(int index) {

            return this.getService(index);
        }

        public synchronized ServiceInfoDescription getService(int index) {

            return this.cs.get(index);
        }

        public synchronized void update() {

            int oldSize = this.getSize();
            if (oldSize > 0) {
                this.cs.clear();
                this.fireIntervalRemoved(this, 0, oldSize - 1);
            }

            try {
                for (ServiceInfo info : Rendezvous.getServiceInfo(null, null)) {
                    this.cs.add(new ServiceInfoDescription(info, true));
                }
            } catch (Exception ignore) {
            }
            if (this.getSize() > 0) {
                this.fireIntervalAdded(this, 0, this.getSize() - 1);
            }
        }
    }
}
