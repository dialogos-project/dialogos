package com.clt.speech.recognition;

import java.util.Iterator;

/**
 * Speech recognizers must return recognition results that conform to this
 * interface.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class AbstractRecognitionResult implements RecognitionResult {

    private boolean showConfidences = false;

    /**
     * Return the number of recognition alternatives
     */
    public abstract int numAlternatives();

    /**
     * Return the words of the nth alternative as a String
     */
    public abstract Utterance getAlternative(int index);

    public Iterator<Utterance> iterator() {

        return new Iterator<Utterance>() {

            private int index = 0;
            private int size = AbstractRecognitionResult.this.numAlternatives();

            public boolean hasNext() {

                return this.index < this.size;
            }

            public Utterance next() {

                return AbstractRecognitionResult.this.getAlternative(this.index++);
            }

            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean getShowConfidences() {

        return this.showConfidences;
    }

    public void setShowConfidences(boolean showConfidences) {

        this.showConfidences = showConfidences;
    }

    /**
     * Return a textual representation of the recognition result
     */
    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        b.append("Alternatives:\n");
        for (int i = 0; i < this.numAlternatives(); i++) {
            if (i + 1 < 10) {
                b.append(" ");
            }
            b.append(i + 1);
            b.append(": ");
            Utterance utt = this.getAlternative(i);
            b.append(utt.toString());
            if (this.getShowConfidences()) {
                b.append(" (");
                b.append(AbstractRecognitionResult
                        .confidenceString(utt.getConfidence()));
                b.append(")");
            }
            b.append("\n");
        }
        return b.toString();
    }

    public static String confidenceString(float confidence) {

        StringBuilder b = new StringBuilder();
        int conf = Math.round(confidence * 1000);
        b.append(conf / 10);
        b.append('.');
        b.append(conf % 10);
        b.append("%");
        return b.toString();
    }
}
