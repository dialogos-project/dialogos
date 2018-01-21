package com.clt.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author dabo
 *
 */
public class ConcatInputStream extends InputStream {

    private InputStream[] streams;
    private int stream;

    public ConcatInputStream(InputStream[] streams) {

        this(Arrays.asList(streams));
    }

    public ConcatInputStream(Collection<? extends InputStream> streams) {

        this.streams = streams.toArray(new InputStream[streams.size()]);
        this.stream = 0;
    }

    @Override
    public int read() throws IOException {

        while (this.stream < this.streams.length) {
            int result = this.streams[this.stream].read();
            if (result == -1) {
                this.stream++;
            } else {
                return result;
            }
        }

        return -1;
    }

    @Override
    public int read(byte[] buffer, int pos, int length) throws IOException {
        if (this.stream >= this.streams.length) {
            return -1;
        } else {
            int result = this.streams[this.stream].read(buffer, pos, length);
            if (result == -1) {
                this.stream++;
                return this.read(buffer, pos, length);
            } else if (result < length) {
                this.stream++;
                int more = this.read(buffer, pos + result, length - result);
                if (more == -1) {
                    return result;
                } else {
                    return result + more;
                }
            } else {
                return result;
            }
        }
    }
}
