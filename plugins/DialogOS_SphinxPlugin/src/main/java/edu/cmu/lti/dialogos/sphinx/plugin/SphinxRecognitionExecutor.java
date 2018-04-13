package edu.cmu.lti.dialogos.sphinx.plugin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.script.exp.Pattern;
import com.clt.speech.recognition.*;
import com.clt.speech.recognition.RecognitionExecutor;
import com.clt.srgf.Grammar;
import edu.cmu.lti.dialogos.sphinx.client.Sphinx;
import edu.cmu.lti.dialogos.sphinx.client.SphinxContext;

/**
 *
 */
public class SphinxRecognitionExecutor implements RecognitionExecutor {

    private AbstractRecognizer recognizer;

    public SphinxRecognitionExecutor(AbstractRecognizer recognizer) {
        assert recognizer instanceof Sphinx;
        this.recognizer = recognizer;
    }

    public MatchResult start(final Grammar grammar, final Pattern[] patterns, long timeout, final RecognizerListener stateListener, final float recognitionThreshold)
            throws InterruptedException, ExecutionException, TimeoutException {
        // works by submitting a Future job and waiting for it to return (forever or until the timeout is reached)
        // in the case of a timeout, stop() will be called externally.

        Future<MatchResult> result = Executors.newSingleThreadExecutor().submit(() -> {
            // TODO: all that is relevant to context, thus including the recognition threshold!
            recognizer.setContext(grammar);
            ((SphinxContext) recognizer.getContext()).setThreshold(recognitionThreshold);
            if (stateListener != null) {
                recognizer.addRecognizerListener(stateListener);
            }
            RecognitionResult recognitionResult = null;
            try {
//                do {
                    recognitionResult = recognizer.startLiveRecognition();
                    if (recognitionResult == null) {
                        //TODO: this may break the contract of RecognitionExecutor if null is returned but it's not the timeout that is at fault
                        return null;
                    }
//                } while ((recognitionResult.numAlternatives() == 0)
//                        || (recognitionResult.getAlternative(0).getConfidence() < recognitionThreshold));
            } finally {
                if (stateListener != null) {
                    recognizer.removeRecognizerListener(stateListener);
                }
            }
            Utterance utterance = recognitionResult.getAlternative(0);
            MatchResult mr = AbstractInputNode.findMatch(utterance.getWords(), grammar, patterns);
            System.err.println("SphinxRecognitionExecutor, result is " + mr);
            // TODO unlocalized string
            if (mr == null)
                throw new RecognizerException("No match for recognition result '" + utterance.getWords() + "'");
//                throw new RecognizerException(Resources.format("NoMatchFor", r));
            return mr;
        });

        if (timeout <= 0) {
            return result.get();
        } else {
            return result.get(timeout, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        try {
            recognizer.stopRecognition();
        } catch (Exception exn) {
            exn.printStackTrace();
            System.out.println(System.currentTimeMillis() + ": exn");
        }
    }
}
