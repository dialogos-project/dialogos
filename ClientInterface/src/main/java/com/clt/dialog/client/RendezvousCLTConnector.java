package com.clt.dialog.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import javax.jmdns.ServiceInfo;

import com.clt.properties.DefaultStringProperty;
import com.clt.properties.Property;
import com.clt.properties.StringProperty;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 6.5
 */
public class RendezvousCLTConnector extends CLTConnector {

    private StringProperty SERVICE_NAME = new DefaultStringProperty("Service Name", "Service Name", null);
    private StringProperty SERVER_NAME = new DefaultStringProperty("Server", "Server", null);

    static {
        Rendezvous.initialize();
    }

    public RendezvousCLTConnector() {
        this("", "");
    }

    public RendezvousCLTConnector(String name, String server) {
        this.SERVICE_NAME.setValue(name);
        this.SERVER_NAME.setValue(server);
    }

    public RendezvousCLTConnector copy() {
        return new RendezvousCLTConnector(this.SERVICE_NAME.getValue(), this.SERVER_NAME.getValue());
    }

    public Property<?>[] getProperties() {
        return new Property<?>[]{this.SERVICE_NAME, this.SERVER_NAME};
    }

    public String getName() {
        return "CLT Connector (Rendezvous)";
    }

    public String getInfo() {
        StringBuilder b = new StringBuilder();
        b.append("Rendezvous: ");
        String p = this.SERVICE_NAME.getValue();
        if ((p != null) && (p.trim().length() > 0)) {
            b.append("\"" + p + "\"");
        } else {
            b.append("?");
        }

        p = this.SERVER_NAME.getValue();
        if ((p != null) && (p.trim().length() > 0)) {
            b.append(" on " + p);
        }

        return b.toString();
    }

    public Object getHelp() {

        return new String[]{
            "This connector works with clients using the CLT Client Interface Protocol.",
            "The client must advertise its service using Rendezvous with the given service name. "
            + "If you leave the Server field empty, the whole subnet will be searched, otherwise "
            + "only clients on the specified server are accepted."};
    }

    @Override
    protected Connection createConnection(TargetSelector selector) throws IOException {
        ServiceInfo[] services = Rendezvous.getServiceInfo(this.SERVICE_NAME.getValue().trim(), this.SERVER_NAME.getValue().trim());
        
        if (services.length == 0) {
            throw new ConnectException("Service " + this.SERVICE_NAME.getValue() + " not found");
        } else {
            ServiceInfo service = null;

            if (services.length == 1) {
                service = services[0];
            } else {
                // System.err.println(services.length + " services named " +
                // getProperty(SERVICE_NAME));
                ServiceInfoDescription[] sid = new ServiceInfoDescription[services.length];
                ServiceInfoDescription preferredService = null;

                List<String> localAddresses = Arrays.asList(Rendezvous.getLocalHostAddresses());

                for (int i = 0; i < services.length; i++) {
                    // System.out.println("  " + services[i]);
                    // System.out.println("Matching address " + services[i].getAddress() +
                    // " against local addresses");
                    sid[i] = new ServiceInfoDescription(services[i]);
                    // prefer local services
                    if (localAddresses.contains(services[i].getAddress())) {
                        preferredService = sid[i];
                    }
                }

                // System.out.println("Preferred service is " + preferredService);
                if (selector != null) {
                    service = selector.choose(sid, preferredService).getService();
                }

                // no preferred service, so just take the first
                if (service == null) {
                    service = services[0];
                }

            }

            String protocols = service.getPropertyString(Rendezvous.SERVICE_PROPERTY_PROTOCOL);
            int protocol = Protocol.RAW_XML;
            if ((protocols != null) && (protocols.indexOf("BIN") >= 0)) {
                protocol = Protocol.BINARY;
                // System.out.println("Using binary protocol");
            } else {
                // System.out.println("Using XML protocol");
            }

            final ServiceInfoDescription selectedService = new ServiceInfoDescription(service, false);
            return new SocketConnection(new Socket(service.getAddress(), service.getPort()), protocol) {
                @Override
                public String toString() {
                    return selectedService.toString();
                }
            };
        }
    }

}
