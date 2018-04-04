package edu.cmu.lti.dialogos.sphinx.client;

import edu.cmu.sphinx.api.*;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.recognizer.*;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

/**
 * takes a SphinxContext and configures the speech recognizer accordingly
 *
 * Created by timo on 30.10.17.
 */
public class ConfigurableSpeechRecognizer extends AbstractSpeechRecognizer {

    private final Microphone microphone;

    public ConfigurableSpeechRecognizer(Context context, InputStream audioSource) throws IOException {
        super(context);
        recognizer.allocate();
        AudioFormat af = Sphinx.getAudioFormat();
        microphone = new Microphone(af.getSampleRate(), af.getSampleSizeInBits(), af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED, af.isBigEndian());
        InputStream input = audioSource != null ? audioSource : microphone.getStream();
        this.context.getInstance(StreamDataSource.class).setInputStream(input);
    }

    public void startRecognition() {
        if (recognizer.getState() != Recognizer.State.READY)
            recognizer.allocate();
        microphone.startRecording();
    }

    public void stopRecognition() {
        microphone.stopRecording();
        if (recognizer.getState() == Recognizer.State.RECOGNIZING) {
            // allow the recognizer some slack to calm down
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        recognizer.deallocate();
    }

    /**
     * Returns result of the recognition.
     *
     * @return recognition result or {@code null} if there is no result, e.g., because the
     * 			microphone or input stream has been closed
     */
    public SpeechResult getResult() {
        Result result = recognizer.recognize();
        return null == result ? null : new SpeechResult(result);
    }


}
