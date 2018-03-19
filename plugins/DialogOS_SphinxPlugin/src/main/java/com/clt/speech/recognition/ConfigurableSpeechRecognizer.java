package com.clt.speech.recognition;

import edu.cmu.sphinx.api.*;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.recognizer.*;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * takes a SphinxContext and configures a speech recognizer accordingly
 *
 * Created by timo on 30.10.17.
 */
public class ConfigurableSpeechRecognizer extends AbstractSpeechRecognizer {

    private final Microphone microphone;

    public ConfigurableSpeechRecognizer(Context context, InputStream audioSource) throws IOException {
        super(context);
        recognizer.allocate();
        microphone = new Microphone(16000, 16, true, false);
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
