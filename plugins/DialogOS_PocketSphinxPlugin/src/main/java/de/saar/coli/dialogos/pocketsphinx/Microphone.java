/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        line.drain();
        line.stop();
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
