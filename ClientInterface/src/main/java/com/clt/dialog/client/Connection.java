package com.clt.dialog.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

interface Connection {

    public InetAddress getTargetAddress();

    public int getPort();

    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public void close() throws IOException;

    public int getProtocol();
}
