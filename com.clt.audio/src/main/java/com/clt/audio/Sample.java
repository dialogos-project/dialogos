package com.clt.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author dabo
 *
 */
public class Sample {

    private static final boolean DEBUG = false;

    private AudioInputStream stream;
    private SourceDataLine line;

    public Sample(AudioInputStream stream, SourceDataLine line) {

        this.stream = stream;
        this.line = line;
    }

    public boolean isOpen() {

        return this.line.isOpen();
    }

    public void start(final Object lineLock) {

        final LineListener lineListener = new LineListener() {

            public void update(LineEvent event) {

                Sample.log("Audio event: " + event.getType());
                if (event.getType() == LineEvent.Type.STOP) {
                    synchronized (lineLock) {
                        Sample.log("Playback stopped");
                        // wenn man direkt ein close() macht, werden die
                        // letzten Samples abgeschnitten
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException exn) {
                        }

                        Sample.log("Closing line");
                        Sample.this.line.close();
                        try {
                            Sample.this.stream.close();
                        } catch (Exception ignore) {
                        }

                        Sample.log("Line closed");
                        this.uninstallListener();
                        lineLock.notifyAll();
                    }
                }
            }

            private void uninstallListener() {

                Sample.this.line.removeLineListener(this);
            }
        };
        this.line.addLineListener(lineListener);

        Sample.log("Starting line");
        this.line.start();
        Sample.log("Line started");

        new Thread(new Runnable() {

            public void run() {

                AudioFormat format = Sample.this.line.getFormat();
                Sample.log("start");
                long bytesPerSecond
                        = (long) (format.getFrameRate() * format.getFrameSize());
                byte[] buffer = new byte[(int) Math.min(bytesPerSecond, 384000)];
                try {
                    boolean emptyStream = true;
                    int bytesRead = 0;
                    do {
                        bytesRead = Sample.this.stream.read(buffer, 0, buffer.length);
                        Sample.log(bytesRead + " bytes read");
                        if (bytesRead <= 0) {
                            Sample.this.line.drain();
                            Sample.this.line.stop();

                            // if the stream was empty, we need to notify the callback
                            // manually
                            if (emptyStream) {
                                lineListener.update(new LineEvent(Sample.this.line,
                                        LineEvent.Type.STOP, 0));
                            }
                        } else {
                            emptyStream = false;
                            Sample.this.line.write(buffer, 0, bytesRead);
                        }

                    } while (Sample.this.line.isOpen() && (bytesRead > 0));
                } catch (Exception ignore) {
                }
                Sample.log("stop");
            }
        }, "com.clt.audio.Sample stream").start();
    }

    public void stop() {

        this.line.stop();
    }

    public void play() {

        final Object lineLock = new Object();

        try {
            synchronized (lineLock) {
                Sample.log("Start playing");
                this.start(lineLock);
                Sample.log("Start playing2");

                while (this.isOpen()) {
                    lineLock.wait();
                }
            }
        } catch (InterruptedException exn) {
        }
    }

    public static Sample load(File file)
            throws IOException, UnsupportedAudioFileException,
            LineUnavailableException {

        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        return Sample.create(stream);
    }

    public static Sample create(AudioInputStream stream)
            throws LineUnavailableException {

        AudioFormat format = stream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        final SourceDataLine sample;

        if (AudioSystem.isLineSupported(info)) {
            sample = (SourceDataLine) AudioSystem.getLine(info);
        } else {
            // we hope, that the system can convert the format
            AudioFormat decodedFormat
                    = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(), 16, format.getChannels(), format
                            .getChannels() * 2,
                            format.getSampleRate(), false);

            stream = AudioSystem.getAudioInputStream(decodedFormat, stream);
            sample
                    = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(
                            SourceDataLine.class,
                            stream.getFormat()));
        }
        sample.open();

        return new Sample(stream, sample);
    }

    public static void play(File file)
            throws IOException, UnsupportedAudioFileException,
            LineUnavailableException {

        Sample sample = Sample.load(file);
        sample.play();
    }

    private static void log(String message) {

        if (Sample.DEBUG) {
            synchronized (System.out) {
                System.out.println(message);
                System.out.flush();
            }
        }
    }

}
