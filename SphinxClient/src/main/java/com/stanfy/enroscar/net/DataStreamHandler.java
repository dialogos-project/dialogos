package com.stanfy.enroscar.net;

import org.apache.commons.codec.binary.Base64InputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stream handler for {@code data} scheme.
 */
public class DataStreamHandler extends URLStreamHandler {

  /** Scheme. */
  public static final String PROTOCOL = "data";

  /** Encoding constsant. */
  private static final String BASE64 = "base64";

  /** Data URI pattern. */
  private static final Pattern URI_PATTERN =
      Pattern.compile("^data:((.+?)(;(\\w+))?,)?(.+)$", Pattern.DOTALL);

  @Override
  protected URLConnection openConnection(final URL u) throws IOException {
    return new DataUrlConnection(u);
  }

  @Override
  protected void parseURL(final URL url, String spec, final int start, final int end) {
    if (spec.endsWith("/.gram"))
      spec = spec.substring(0, spec.length() - "/.gram".length());
    Matcher m = URI_PATTERN.matcher(spec);
    if (!m.matches()) {
      throw new RuntimeException("Cannot parse url " + spec);
    }
    final int contentTypeGroup = 2, encodingGroup = 4, dataGroup = 5;

    String contentType = m.group(contentTypeGroup);
    String encoding = m.group(encodingGroup);
    String data = m.group(dataGroup);
    if (!BASE64.equals(encoding)) {
      try {
        data = URLDecoder.decode(data, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new AssertionError(e);
      }
    }
    setURL(url, PROTOCOL, PROTOCOL, 0, contentType, encoding, data, null, null);
  }

  private static class DataUrlConnection extends URLConnection {

    /** Data in bytes. */
    private byte[] data;

    public DataUrlConnection(final URL url) {
      super(url);
    }

    @Override
    public void connect() throws IOException {
      data = url.getPath().getBytes(StandardCharsets.UTF_8);
      connected = true;
    }

    private String contentType() {
      return url.getAuthority();
    }

    private String encoding() {
      return url.getUserInfo();
    }

    @Override
    public String getHeaderField(final String key) {
      if ("Content-Type".equalsIgnoreCase(key)) {
        return contentType();
      }
      if ("Content-Encoding".equalsIgnoreCase(key)) {
        String enc = encoding();
        return BASE64.equals(enc) ? null : enc;
      }
      return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      if (!connected) {
        connect();
      }
      InputStream stream = new ByteArrayInputStream(data);
      if (BASE64.equals(encoding())) {
        stream = new Base64InputStream(stream);
      }
      return stream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new UnsupportedOperationException("Can't write to data scheme URL");
    }
  }

}
