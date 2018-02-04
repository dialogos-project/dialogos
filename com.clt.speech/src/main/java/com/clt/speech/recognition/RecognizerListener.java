package com.clt.speech.recognition;

/**
 * The listener interface for receiving recognizer events.
 *
 * The class that is interested in processing a recognizer event implements this
 * interface, and the object created with that class is registered with a
 * recognizer, using the recognizer's <code>addRecognizerListener</code> method.
 *
 * Recognizer events are produced when the recognizer changes its state, for
 * example after the successful recognition of a phrase. Recognizers usually
 * send event objects that are derived from RecognizerEvent and carry additional
 * information that depends on the actual recognizer.
 *
 * @see RecognizerEvent
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface RecognizerListener {

    public void recognizerStateChanged(RecognizerEvent evt);
}
