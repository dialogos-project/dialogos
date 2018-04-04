package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.speech.recognition.AbstractRecognizer;
import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.Signal;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;

public class VADListener extends BaseDataProcessor {

    Sphinx recognizer;

    @Override
    public Data getData() throws DataProcessingException {
        Data d = getPredecessor().getData();
        recognizer.evesdropOnData(d);
        return d;
    }

    public void setRecognizer(Sphinx recognizer) {
        this.recognizer = recognizer;
    }
}
