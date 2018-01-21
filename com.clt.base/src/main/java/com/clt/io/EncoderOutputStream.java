package com.clt.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class EncoderOutputStream extends FilterOutputStream {
    static byte[] rkeys;

    static {
        EncoderOutputStream.rkeys = new byte[DecoderInputStream.keys.length];
        for (int i = 0; i < DecoderInputStream.keys.length; i++) {
            int key = DecoderInputStream.keys[i];
            if (key < 0) {
                key += 256;
            }
            EncoderOutputStream.rkeys[key] = (byte) i;
        }
    }

    private static byte getKeyIndex(byte key) {
        return EncoderOutputStream.rkeys[key < 0 ? key + 256 : key];
    }

    private int index;

    private byte lastbyte;

    private byte[] seed;

    private byte[] encodingBuffer;

    public EncoderOutputStream(OutputStream out) {
        this(out, null);
    }

    public EncoderOutputStream(OutputStream out, int seed) {
        this(out, new byte[]{(byte) (seed >> 24), (byte) (seed >> 16),
            (byte) (seed >> 8),
            (byte) (seed >> 0)});
    }

    public EncoderOutputStream(OutputStream out, byte[] seed) {
        super(out);

        this.index = 0;
        this.lastbyte = 0;
        this.seed = seed;
        if ((seed == null) || (seed.length == 0)) {
            this.seed = new byte[]{0};
        }

        this.encodingBuffer = new byte[256];
    }

    private byte step() {
        int result = DecoderInputStream.getKey(this.index) ^ this.seed[this.index % this.seed.length];
        this.index = (this.index + 1) % (DecoderInputStream.keys.length * this.seed.length);
        return (byte) result;
    }

    @Override
    public void write(int b) throws IOException {
        this.lastbyte = (byte) (EncoderOutputStream.getKeyIndex((byte) b) ^ this.lastbyte ^ this.step());
        this.out.write(this.lastbyte);
    }

    @Override
    public void write(byte b[]) throws IOException {
        this.write(b, 0, b.length);
    }

    // do NOT use super.write() , as this will use write(int), leading to double
    // encoding
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        while (len > 0) {
            int numBytes = Math.min(len, this.encodingBuffer.length);
            for (int i = 0; i < numBytes; i++) {
                this.lastbyte = (byte) (EncoderOutputStream.getKeyIndex(b[off + i]) ^ this.lastbyte ^ this.step());
                this.encodingBuffer[i] = this.lastbyte;
            }
            this.out.write(this.encodingBuffer, 0, numBytes);
            off += numBytes;
            len -= numBytes;
        }
    }
}
