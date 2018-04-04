package com.clt.speech.recognition;

import java.util.EventObject;

import com.clt.speech.Resources;

/**
 * The base class for recognizer events.
 *
 * Recognizers should derive their own event class that carries additional
 * information about the event.
 *
 * @see RecognizerEvent
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class RecognizerEvent extends EventObject {

    public static final int RECOGNIZER_READY = 0;
    public static final int RECOGNIZER_ACTIVATED = 1;
    public static final int RECOGNIZER_DEACTIVATED = 2;
    public static final int RECOGNIZER_WARNING = 3;
    public static final int START_OF_SPEECH = 4;
    public static final int END_OF_SPEECH = 5;
    public static final int PARTIAL_RESULT = 6;
    public static final int RECOGNIZER_LOADING = 7;

    private int type;
    private String errorMessage;
    private RecognitionResult result;

    public RecognizerEvent(Recognizer source, int type) {

        super(source);

        this.type = type;
    }

    public RecognizerEvent(Recognizer source, RecognitionResult result) {

        this(source, RecognizerEvent.PARTIAL_RESULT);

        this.result = result;
    }

    public RecognizerEvent(Recognizer source, String errorMessage) {

        this(source, RecognizerEvent.RECOGNIZER_WARNING);

        this.errorMessage = errorMessage;
    }

    public Recognizer getRecognizer() {

        return (Recognizer) this.getSource();
    }

    public int getType() {

        return this.type;
    }

    public String getErrorMessage() {

        return this.errorMessage;
    }

    public RecognitionResult getResult() {

        return this.result;
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();

        switch (this.getType()) {
            case RECOGNIZER_READY:
                b.append(Resources.getString("RecognizerReady"));
                break;
            case RECOGNIZER_ACTIVATED:
                b.append(Resources.getString("RecognizerActivated"));
                break;
            case RECOGNIZER_DEACTIVATED:
                b.append(Resources.getString("RecognizerDeactivated"));
                break;
            case RECOGNIZER_WARNING:
                b.append(Resources.getString("RecognizerWarning"));
                break;
            case START_OF_SPEECH:
                b.append(Resources.getString("RecognizerStartOfSpeech"));
                break;
            case END_OF_SPEECH:
                b.append(Resources.getString("RecognizerEndOfSpeech"));
                break;
            case PARTIAL_RESULT:
                b.append(Resources.getString("RecognizerPartialResult"));
                break;
            case RECOGNIZER_LOADING:
                b.append(Resources.getString("RecognizerLoading"));
                break;
            default:
                b.append("Recognizer event #");
                b.append(this.getType());
                break;
        }
        if (this.errorMessage != null) {
            b.append(": " + this.errorMessage);
        } else if (this.result != null) {
            b.append(":\n" + this.result);
        }

        return b.toString();
    }
}
