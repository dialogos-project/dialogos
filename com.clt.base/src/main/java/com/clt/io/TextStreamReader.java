package com.clt.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.clt.util.Platform;

public class TextStreamReader extends Reader {

    private int codepage;
    private InputStream in;
    private UTFReader utf = null;
    private byte[] byteBuffer;
    private char[] charBuffer;

    public TextStreamReader(InputStream in, int codepage) {
        this(in, codepage, 50);
    }

    public TextStreamReader(InputStream in, int codepage, int bufferSize) {
        if (in == null) {
            throw new IllegalArgumentException("input stream is null");
        }

        this.in = in;
        this.setCodepage(codepage);
        this.byteBuffer = new byte[bufferSize];
        this.charBuffer = new char[bufferSize];
    }

    public void setCodepage(int codepage) {
        if (codepage == Codepages.PLATFORM) {
            if (Platform.isMac()) {
                this.codepage = Codepages.MAC;
            } else {
                this.codepage = Codepages.ANSI;
            }
        } else {
            this.codepage = codepage;
        }

        if ((this.codepage == Codepages.UTF8) && (this.utf == null)) {
            this.utf = new UTFReader(this.in);
        }
    }

    public int getCodepage() {

        return this.codepage;
    }

    @Override
    public int read()
            throws IOException {

        char[] cb = new char[1];
        if (this.read(cb, 0, 1) == -1) {
            return -1;
        } else {
            return cb[0];
        }
    }

    @Override
    public int read(char cbuf[]) throws IOException {
        return this.read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (this.lock) {
            if (this.codepage == Codepages.UTF8) {
                return this.utf.read(cbuf, off, len);
            } else {
                if (this.byteBuffer.length < len) {
                    this.byteBuffer = new byte[len];
                }
                int result;
                if (this.in instanceof DataInputStream) {
                    ((DataInputStream) this.in).readFully(this.byteBuffer, 0, len);
                    result = len;
                } else {
                    result = this.in.read(this.byteBuffer, 0, len);
                }
                for (int i = 0; i < result; i++) {
                    cbuf[off + i] = Codepages.ToUnicode[this.codepage][this.byteBuffer[i] & 0x000000ff];
                }

                return result;
            }
        }
    }

    public String read(int len) throws IOException {
        if (this.charBuffer.length < len) {
            this.charBuffer = new char[len];
        }
        int numChars = this.read(this.charBuffer, 0, len);
        if (numChars < 0) {
            throw new EOFException();
        }
        return new String(this.charBuffer, 0, numChars);

    }

    public String readAll() throws IOException {
        return this.read(this.in.available());
    }

    public String[] readArray() throws IOException {
        int len, num;

        len = this.readUnsignedShort();
        num = this.readUnsignedByte();

        String s = this.read(len);
        String[] a = new String[num];
        int start = 0, length = 0;
        for (int i = 0; i < num - 1; i++) {
            length = this.readUnsignedByte();
            a[i] = s.substring(start, start + length);
            start += length;
        }
        a[num - 1] = s.substring(start);
        s = null;

        return a;
    }

    private final int readUnsignedByte() throws IOException {
        int ch = this.in.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    private final int readUnsignedShort() throws IOException {
        int ch1 = this.readUnsignedByte();
        int ch2 = this.readUnsignedByte();
        return (ch1 << 8) | (ch2 << 0);
    }

    @SuppressWarnings("unused")
    private final int readInt() throws IOException {
        int ch1 = this.readUnsignedByte();
        int ch2 = this.readUnsignedByte();
        int ch3 = this.readUnsignedByte();
        int ch4 = this.readUnsignedByte();
        return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
    }

    @Override
    public long skip(long n) throws IOException {
        synchronized (this.lock) {
            return this.in.skip(n);
        }
    }

    @Override
    public boolean ready() throws IOException {
        synchronized (this.lock) {
            return this.in.available() > 0;
        }
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) {
        this.in.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        this.in.reset();
    }

    @Override
    public void close() throws IOException {
        this.byteBuffer = null;
        this.charBuffer = null;
        if (this.utf != null) {
            this.utf.dispose();
            this.utf = null;
        }
        synchronized (this.lock) {
            if (this.in != null) {
                this.in.close();
            }
            this.in = null;
        }
    }

    private static class UTFReader {

        private int needed, result, additional, index;
        private byte[] bytes = new byte[20];
        private InputStream in;

        public UTFReader(InputStream in) {
            this.in = in;
        }

        // liest weitere bytes falls erforderlich und gibt zurueck,
        // ob bytesNeeded bytes vorhanden sind
        private boolean checkBytes(int bytesNeeded) throws IOException {
            if (this.index > this.result - this.needed) {
                if ((this.result < this.needed) || (this.additional == 0)) {
                    // zu lesen
                    return false;
                } else {
                    this.needed += this.additional;
                    this.result += this.in.read(this.bytes, this.result, this.additional);
                    this.additional = 0;
                    if (this.index > this.result - this.needed) {
                        return false;
                    }
                }
            }
            return true; // genug bytes vorhanden
        }

        public int read(char cbuf[], int off, int len) throws IOException {
            if (this.bytes.length < len * 3) {
                this.bytes = new byte[len * 3]; // Speicher fuer worst case anlegen
            }

            this.index = 0;
            this.additional = 0;

            this.needed = len;
            this.result = this.in.read(this.bytes, 0, len); // erstmal nur best case
            // lesen

            int j;
            for (j = 0; j < len; j++) {
                if (!this.checkBytes(1)) {
                    break;
                }

                int c = this.bytes[j - off] & 0x000000FF;
                if (c >= 0xE0) {
                    // wir brauchen zwei bytes mehr als normal
                    this.additional += 2;
                    if (!this.checkBytes(3)) {
                        break;
                    }
                    cbuf[j + off] = (char) (((this.bytes[this.index] & 0x0F) << 12)
                            | ((this.bytes[this.index + 1] & 0x3F) << 6) | (this.bytes[this.index + 2] & 0x3F));

                    this.index += 3;
                } else if (c >= 0x80) {
                    // wir brauchen ein byte mehr als normal
                    this.additional += 1;
                    if (!this.checkBytes(2)) {
                        break;
                    }
                    cbuf[j + off] = (char) (((this.bytes[this.index] & 0x1F) << 6) | (this.bytes[this.index + 1] & 0x3F));
                    this.index += 2;
                } else {
                    cbuf[j + off] = (char) c;
                    this.index++;
                }
            }

            return j;
        }

        public void dispose() {
            this.bytes = null;
        }
    }

}
