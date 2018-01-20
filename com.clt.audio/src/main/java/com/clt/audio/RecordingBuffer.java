package com.clt.audio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

public class RecordingBuffer {

    private List<byte[]> buffers = new ArrayList<byte[]>();
    private int samples = 0;
    private boolean open = false;

    private AudioFormat format;
    private Collection<Sample> playbackStreams = new LinkedList<Sample>();

    public RecordingBuffer(AudioFormat format) {

        this(format, false);
    }

    public RecordingBuffer(AudioFormat format, boolean open) {

        this.format = format;
        if (open) {
            this.open();
        }
    }

    public void add(byte[] buffer) {

        int length = buffer.length;
        if (length % (this.format.getSampleSizeInBits() / 8) != 0) {
            throw new IllegalArgumentException("Incomplete samples");
        }
        synchronized (this.buffers) {
            if (this.open) {
                this.buffers.add(buffer);
                this.samples
                        += length / ((this.format.getSampleSizeInBits() - 1) / 8 + 1);
                this.buffers.notifyAll();
            }
        }
    }

    public void append(byte[] buffer) {

        this.append(buffer, 0, buffer.length);
    }

    public void append(byte[] buffer, int start, int length) {

        synchronized (this.buffers) {
            if ((start < 0) || (length < 0) || (buffer.length < start + length)) {
                throw new IllegalArgumentException("Illegal buffer length");
            }
            if (length % (this.format.getSampleSizeInBits() / 8) != 0) {
                throw new IllegalArgumentException("Incomplete samples");
            }

            if (this.open) {
                byte[] b = new byte[length];
                System.arraycopy(buffer, start, b, 0, length);
                this.buffers.add(b);
                this.samples
                        += length / ((this.format.getSampleSizeInBits() - 1) / 8 + 1);
                this.buffers.notifyAll();
            }
        }
    }

    public AudioFormat getFormat() {

        return this.format;
    }

    public int numSamples() {

        synchronized (this.buffers) {
            return this.samples;
        }
    }

    public int length() {

        synchronized (this.buffers) {
            return Math.round(this.numSamples() * 1000.0f
                    / this.format.getFrameRate());
        }
    }

    public void open() {

        synchronized (this.buffers) {
            this.open = true;
        }
    }

    public void close() {

        synchronized (this.buffers) {
            this.open = false;
            this.buffers.notifyAll();
        }
    }

    public void clear() {

        synchronized (this.buffers) {
            this.buffers.clear();
            this.samples = 0;
        }
    }

    public void play()
            throws LineUnavailableException {

        this.play(null);
    }

    public void play(Object startlock)
            throws LineUnavailableException {

        Sample sample
                = Sample.create(new AudioInputStream(this.getStream(), this.getFormat(),
                        AudioSystem.NOT_SPECIFIED));
        synchronized (this.playbackStreams) {
            this.playbackStreams.add(sample);
        }
        if (startlock != null) {
            synchronized (startlock) {
                startlock.notifyAll();
            }
        }
        sample.play();
        synchronized (this.playbackStreams) {
            this.playbackStreams.remove(sample);
            this.playbackStreams.notifyAll();
        }
    }

    public void save(File file, AudioFileFormat.Type fileType)
            throws IOException {

        AudioSystem.write(new AudioInputStream(this.getStream(), this.getFormat(),
                AudioSystem.NOT_SPECIFIED), fileType, file);
    }

    public boolean isPlaying() {

        synchronized (this.playbackStreams) {
            return !this.playbackStreams.isEmpty();
        }
    }

    public void waitPlayback()
            throws InterruptedException {

        synchronized (this.playbackStreams) {
            while (!this.playbackStreams.isEmpty()) {
                this.playbackStreams.wait();
            }
        }
    }

    public void stopPlayback() {

        synchronized (this.playbackStreams) {
            for (Sample sample : this.playbackStreams) {
                sample.stop();
            }
        }
    }

    public InputStream getStream() {

        return new InputStream() {

            private int index = 0;
            private int position = 0;
            private byte[] buffer = null;

            private int markedIndex = 0;
            private int markedPosition = 0;

            @Override
            public int read() {

                synchronized (RecordingBuffer.this.buffers) {
                    if (this.buffer == null) {
                        if (this.index >= RecordingBuffer.this.buffers.size()) {
                            return -1;
                        } else {
                            this.buffer = RecordingBuffer.this.buffers.get(this.index++);
                            this.position = 0;
                            return this.read();
                        }
                    } else if (this.position >= this.buffer.length) {
                        this.buffer = null;
                        return this.read();
                    } else {
                        int i = this.buffer[this.position++];
                        if (i < 0) {
                            i += 256;
                        }
                        return i;
                    }
                }
            }

            @Override
            public int read(byte b[])
                    throws IOException {

                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte b[], int off, int len)
                    throws IOException {

                synchronized (RecordingBuffer.this.buffers) {
                    if (this.buffer == null) {
                        try {
                            while ((this.index >= RecordingBuffer.this.buffers.size())
                                    && RecordingBuffer.this.open) {
                                RecordingBuffer.this.buffers.wait();
                            }
                        } catch (InterruptedException exn) {
                        }
                        if (this.index >= RecordingBuffer.this.buffers.size()) {
                            return -1;
                        } else {
                            this.buffer = RecordingBuffer.this.buffers.get(this.index++);
                            this.position = 0;
                            return this.read(b, off, len);
                        }
                    } else if (this.position >= this.buffer.length) {
                        this.buffer = null;
                        return this.read(b, off, len);
                    } else {
                        int bytesRead = Math.min(len, this.buffer.length - this.position);
                        System.arraycopy(this.buffer, this.position, b, off, bytesRead);
                        this.position += bytesRead;
                        if (bytesRead == len) {
                            return len;
                        } else {
                            int additionalBytes
                                    = this.read(b, off + bytesRead, len - bytesRead);
                            if (additionalBytes <= 0) {
                                return bytesRead;
                            } else {
                                return bytesRead + additionalBytes;
                            }
                        }
                    }
                }
            }

            @Override
            public synchronized void mark(int readlimit) {

                this.markedIndex = this.index;
                this.markedPosition = this.position;
            }

            @Override
            public boolean markSupported() {

                return true;
            }

            @Override
            public synchronized void reset() {

                this.index = this.markedIndex;
                this.buffer = RecordingBuffer.this.buffers.get(this.index);
                this.position = this.markedPosition;
            }

        };
    }
}
