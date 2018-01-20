package com.clt.dialog.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import com.clt.properties.DefaultIntegerProperty;
import com.clt.properties.DefaultStringProperty;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;
import com.clt.properties.StringProperty;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 6.5
 */
public class ManualCLTConnector extends CLTConnector {

    private StringProperty SERVER_NAME = new DefaultStringProperty("Server Name", "Server Name", null);
    private IntegerProperty PORT = new DefaultIntegerProperty("Port", "Port", null);

    public ManualCLTConnector() {

        this("localhost", 0);
    }

    public ManualCLTConnector(String server, int port) {

        this.SERVER_NAME.setValue(server);
        this.PORT.setValue(port);

        this.PORT.setEditType(Property.EDIT_TYPE_TEXTFIELD);
    }

    public ManualCLTConnector copy() {

        return new ManualCLTConnector(this.SERVER_NAME.getValue(), this.PORT.getValue());
    }

    public Property<?>[] getProperties() {

        return new Property<?>[]{ this.SERVER_NAME, this.PORT };
    }

    public String getName() {
        return "CLT Connector (Fixed Server)";
    }

    public String getInfo() {

        StringBuilder b = new StringBuilder();
        b.append("Fixed IP: ");
        String p = this.SERVER_NAME.getValue();
        if ((p != null) && (p.trim().length() > 0)) {
            b.append(p);
        } else {
            b.append("?");
        }
        b.append(":");
        b.append(this.PORT.getValue());
        return b.toString();
    }

    public Object getHelp() {

        return new String[]{
            "This connector works with clients using the CLT Client Interface Protocol.",
            "The client must be running on the specified server and port. "
            + "The server name can be a host name or IP address. For clients running on the "
            + "same computer simply use \"localhost\"."};
    }

    @Override
    protected Connection createConnection(TargetSelector selector)
            throws IOException {

        try {
            return new SocketConnection(
                    new Socket(this.SERVER_NAME.getValue(), this.PORT.getValue()),
                    Protocol.RAW_XML);
        } catch (Exception exn) {
            throw new ConnectException("Could not connect to "
                    + this.SERVER_NAME.getValue() + ":" + this.PORT.getValue());
        }
    }
}
