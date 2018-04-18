package edu.cmu.lti.dialogos.sphinx.client;

import edu.cmu.sphinx.api.AbstractSpeechRecognizer;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * takes a SphinxContext and configures the speech recognizer accordingly
 *
 * Created by timo on 30.10.17.
 */
public class ConfigurableSpeechRecognizer extends AbstractSpeechRecognizer {

    private Microphone microphone;

    public ConfigurableSpeechRecognizer(Context context, InputStream audioSource) throws IOException {
        super(context);
        recognizer.allocate();

/*        recognizer.addStateListener(new StateListener() {
            @Override public void statusChanged(edu.cmu.sphinx.recognizer.Recognizer.State status) {
                System.err.println("listener defined in configurable speech recognizer: " + status);
            }
            @Override public void newProperties(PropertySheet ps) throws PropertyException { }
        });*/

        StreamDataSource sds = context.getInstance(StreamDataSource.class);
        if (audioSource != null) {
            sds.setInputStream(audioSource);
        } else {
            microphone = context.getInstance(Microphone.class);
            microphone.initialize();
            sds.setPredecessor(microphone);
        }
    }

    public synchronized void startRecognition() {
        if (recognizer.getState() == Recognizer.State.DEALLOCATED)
            recognizer.allocate();
        if (microphone != null)
            microphone.startRecording();
    }

    public synchronized void stopRecognition() {
        if (microphone != null && microphone.isRecording())
            microphone.stopRecording();
    }

    /**
     * Returns result of the recognition.
     *
     * @return recognition result or {@code null} if there is no result, e.g., because the
     * 			microphone or input stream has been closed
     */
    @Override
    public SpeechResult getResult() {
        Result result = recognizer.recognize();
        return null == result ? null : new SpeechResult(result);
    }


}
