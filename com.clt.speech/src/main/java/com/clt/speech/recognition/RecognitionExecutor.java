package com.clt.speech.recognition;

import com.clt.script.exp.Pattern;
import com.clt.srgf.Grammar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * interface for one-time execution of speech recognition.
 * Your mileage on multiple recognitions (start->stop->start again with different settings) will probably vary.
 * Better create a new RecognitionExecutor for every call to recognition.
 */
public interface RecognitionExecutor {

    /**
     * blocking recognition (until recognition succeeds, i.e. until the recognition output machtes against a pattern or the timeout is reached)
     * recognize as long and often as necessary with recGrammar until a match against patterns is found or timeout is reached
     * @param grammar the recognition grammar to use
     * @param patterns the list of patterns to be used to construct the MatchResult against
     * @param timeout timeout after which null should be returned to indicate timeout
     * @param stateListener listener that should be informed about relevant recognition events
     * @param recognitionThreshold threshold that must be met for recognition to be considered successful
     * @return a MatchResult which encodes the actual match (name-value pairs) and the ID of the matching pattern, or null if timeout was reached (timeouts can also be signified by throwing a TimeoutException)
     */
    public MatchResult start(final Grammar grammar, final Pattern[] patterns, long timeout,
                             final RecognizerListener stateListener, final float recognitionThreshold)
            throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * should interrupt any ongoing recognition
     *
     * will probably trigger an InterruptedException in any active calls to start above
     * implementations should be prepared that stop() could be called multiple times and also if recognition has already terminated successfully
     */
    public void stop();

}
