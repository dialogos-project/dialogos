package com.clt.speech.recognition;

/**
 * An extended listener interface that can also communicate along audio for displaying a loudness meter
 * @author timo
 */
public interface AudioAwareRecognizerListener extends RecognizerListener {

    public void newAudio(byte[] audio);
    public void newAudio(double[] audio);
}