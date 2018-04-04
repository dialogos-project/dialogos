package edu.cmu.lti.dialogos.sphinx.client;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;

public class VADListener extends BaseDataProcessor {

    Sphinx recognizer;

    @Override
    public Data getData() throws DataProcessingException {
        Data d = getPredecessor().getData();
        if (recognizer != null)
            recognizer.evesdropOnFrontend(d);
        return d;
    }

    public void setRecognizer(Sphinx recognizer) {
        this.recognizer = recognizer;
    }
}
