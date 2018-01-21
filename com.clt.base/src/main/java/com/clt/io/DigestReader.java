package com.clt.io;

import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author dabo
 *
 */
public class DigestReader extends Reader {

    private Reader in;
    private MessageDigest digest;

    public DigestReader(Reader in, String algorithm) throws NoSuchAlgorithmException {
        this.in = in;
        this.digest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    public byte[] digest() {
        MessageDigest d = this.digest;
        try {
            d = (MessageDigest) this.digest.clone();
        } catch (CloneNotSupportedException ignore) {
        }

        return d.digest();
    }

    @Override
    public int read(char[] buf, int off, int len) throws IOException {
        int result = this.in.read(buf, off, len);
        if (result <= 0) {
            return result;
        }
        byte[] bytes = new byte[result * 2];
        for (int i = 0; i < result; i++) {
            byte hi = (byte) (buf[off + i] >>> 8);
            byte lo = (byte) (buf[off + i] & 0xFF);

            bytes[2 * i] = hi;
            bytes[2 * i + 1] = lo;
        }

        this.digest.update(bytes);

        return result;
    }

}
