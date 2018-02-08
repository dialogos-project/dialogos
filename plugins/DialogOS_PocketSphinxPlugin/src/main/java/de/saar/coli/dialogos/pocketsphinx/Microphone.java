package de.saar.coli.dialogos.pocketsphinx;

import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author koller
 */
public class Microphone {

    private AudioFormat format;
    private TargetDataLine line;

    public Microphone(AudioFormat format) {
        this.format = format;
    }

    public void start() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
    }

    public void stop() {
        line.close();

        // In some Java audio tutorials, it is recommended to call
        // line.drain() and then line.stop(). This does not work on
        // Windows, where apparently some other thread keeps writing
        // into the line, and so drain never returns. Just closing
        // the line seems to work fine, especially given that we always
        // allocate a new Microphone object for each node call anyway.
    }

    /**
     * Call this only after {@link #start() } has been called.
     *
     * @return
     */
    public InputStream getAudioStream() {
        return new AudioInputStream(line);
    }
}
