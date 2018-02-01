
package com.clt.speech.recognition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.clt.properties.Property;
import com.clt.script.Environment;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.expressions.Function;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.Undefined;
import com.clt.speech.G2P;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.srgf.Grammar;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class AbstractRecognizer implements Recognizer, G2P {

    private final Collection<RecognizerListener> listeners = new ArrayList<RecognizerListener>();

    private boolean active = false;
    private boolean live = true;

    private Map<String, Property<?>> additionalParameters
            = new HashMap<String, Property<?>>();

    public void addRecognizerListener(RecognizerListener l) {

        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeRecognizerListener(RecognizerListener l) {

        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected void fireRecognizerEvent(int state) {

        this.fireRecognizerEvent(new RecognizerEvent(this, state));
    }

    protected void fireRecognizerEvent(RecognitionResult result) {

        this.fireRecognizerEvent(new RecognizerEvent(this, result));
    }

    protected void fireRecognizerEvent(RecognizerEvent evt) {

        synchronized (listeners) {
            for (RecognizerListener listener : listeners) {
                listener.recognizerStateChanged(evt);
            }
        }
    }

    @SuppressWarnings("unused")
    public void dispose()
            throws SpeechException {

    }

    public boolean supportsOfflineRecognition() {

        return false;
    }

    public synchronized final boolean isActive() {

        return this.active;
    }

    public synchronized final boolean isLive() {

        return this.live;
    }

    public final RecognitionResult startLiveRecognition() throws SpeechException {

        return this.startLiveRecognition(null);
    }

    public final RecognitionResult startLiveRecognition(final Object lock) throws SpeechException {

        RecognitionResult result = null;
        RecognizerListener startupListener = new RecognizerListener() {

            public void recognizerStateChanged(RecognizerEvent evt) {

                synchronized (lock) {
                    if (evt.getType() == RecognizerEvent.RECOGNIZER_ACTIVATED) {
                        lock.notifyAll();
                    }
                }
            }
        };
        if (lock != null) {
            this.addRecognizerListener(startupListener);
        }
        try {
            synchronized (this) {
                if (this.isActive()) {
                    throw new RecognizerException("Recognizer already active");
                }
                this.active = true;
                this.live = true;
            }

            try {
                result = this.startImpl();
            } finally {
                synchronized (this) {
                    this.active = false;
                    this.notifyAll();
                }
            }
        } finally {
            if (lock != null) {
                this.removeRecognizerListener(startupListener);

                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }
        return result;
    }

    public final RecognitionResult startOfflineRecognition(File file)
            throws SpeechException, IOException, UnsupportedAudioFileException {

        return this.startOfflineRecognition(null, file);
    }

    public final RecognitionResult startOfflineRecognition(final Object lock,
            File file)
            throws SpeechException, IOException, UnsupportedAudioFileException {

        RecognitionResult result = null;
        RecognizerListener startupListener = new RecognizerListener() {

            public void recognizerStateChanged(RecognizerEvent evt) {

                synchronized (lock) {
                    if (evt.getType() == RecognizerEvent.RECOGNIZER_ACTIVATED) {
                        lock.notifyAll();
                    }
                }
            }
        };
        if (lock != null) {
            this.addRecognizerListener(startupListener);
        }
        try {
            synchronized (this) {
                if (this.isActive()) {
                    throw new RecognizerException("Recognizer already active");
                }
                this.active = true;
                this.live = false;
            }

            try {
                result = this.startImpl(file);
            } finally {
                synchronized (this) {
                    this.active = false;
                    this.live = true;
                    this.notifyAll();
                }
            }
        } finally {
            if (lock != null) {
                this.removeRecognizerListener(startupListener);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }
        return result;
    }

    public synchronized final void stopRecognition()
            throws SpeechException {

        try {
            while (this.isActive()) {
                this.stopImpl();
                this.fireRecognizerEvent(RecognizerEvent.RECOGNIZER_DEACTIVATED);
                this.wait();
            }
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * Starts the speech recognizer. This method should do whatever is needed to
     * start up the underlying speech recognition engine. It should then read
     * audio input from the microphone and feed it to the speech recognizer.
     * Once speech is finished, it should return a {@link RecognitionResult}
     * containing the recognized string. Note that one RecognitionResult may
     * contain a number of different hypotheses, each with its own score. Note
     * also that DialogOS can be configured to reject recognition results below
     * a certain confidence threshold. The confidences of each alternative in
     * the recognition result will be measured against this.<p>
     *
     * An implementation of startImpl should call {@link #fireRecognizerEvent(com.clt.speech.recognition.RecognizerEvent)
     * }
     * to indicate the status of the speech recognizer. The status will be
     * displayed in the DialogOS GUI (green microphone, etc.). In particular,
     * events should be fired when the recognizer becomes READY, ACTIVATED, or
     * DEACTIVATED (see {@link RecognizerEvent} for details)
     * .<p>
     *
     * When the recognizer is cancelled from the GUI, {@link #stopImpl() } will
     * be called to terminate the recognizer and initiate whatever cleanup is
     * necessary. Some of this cleanup may take place in startImpl. In such a
     * case, startImpl is expected to return null, in order to indicate to the
     * recognition thread that the recognizer GUI can be closed and the dialog
     * finished.
     *
     * @return
     * @throws SpeechException
     */
    protected abstract RecognitionResult startImpl() throws SpeechException;

    @SuppressWarnings("unused")
    protected RecognitionResult startImpl(File soundFile)
            throws SpeechException, IOException, UnsupportedAudioFileException {

        throw new RecognizerException(
                "Offline processing is not supported by this recognizer.");
    }

    /**
     * Stops the speech recognizer (and makes it return null).
     * This method should do whatever is needed to
     * terminate the recognizer and perform any necessary cleanup.
     *
     * @throws SpeechException
     */
    protected abstract void stopImpl() throws SpeechException;

    protected Environment getEnvironment() {

        return new com.clt.script.DefaultEnvironment() {

            @Override
            public Expression createFunctionCall(String name, Expression[] arguments) {

                if (name.equals("saveRecording") && (arguments.length == 2)) {
                    return new Function(name, arguments) {

                        @Override
                        protected Value eval(Debugger dbg, Value[] args) {

                            File recording = AbstractRecognizer.this.getRecordingFile();
                            if (recording == null) {
                                return new Undefined();
                            }

                            long start = ((IntValue) args[0]).getInt();
                            long end = ((IntValue) args[1]).getInt();

                            try {
                                File saved
                                        = AbstractRecognizer.saveRecording(recording,
                                                AbstractRecognizer.this.createRecordingFile(), start,
                                                end);
                                return new StringValue(saved.getAbsolutePath());
                            } catch (Exception exn) {
                                throw new EvaluationException(
                                        "Could not save recording section. "
                                        + exn);
                            }
                        }

                        @Override
                        public Type getType() {

                            Type t1 = Type.unify(this.getArgument(0).getType(), Type.Int);
                            Type t2 = Type.unify(this.getArgument(1).getType(), Type.Int);
                            if ((t1 != Type.Int) || (t2 != Type.Int)) {
                                throw new TypeException("Arguments of function "
                                        + this.getName()
                                        + " must both be integers");
                            }

                            return Type.String;
                        }
                    };
                } else {
                    return super.createFunctionCall(name, arguments);
                }
            }

        };
    }

    protected File getRecordingFile() {

        return null;
    }

    protected File createRecordingFile()
            throws IOException {

        return File.createTempFile("Recording", ".wav");
    }

    private static File saveRecording(File source, File output, long start,
            long end)
            throws UnsupportedAudioFileException, IOException {

        AudioInputStream in = AudioSystem.getAudioInputStream(source);
        if (end <= 0) {
            throw new IllegalArgumentException("End position too small (" + end + ")");
        }
        if (end > in.getFrameLength()) {
            throw new IllegalArgumentException("End position too big (" + end + ")");
        }
        AudioFormat format = in.getFormat();
        int frameSize = format.getFrameSize();
        in.skip(start * frameSize);
        long length = end - start;
        byte[] data = new byte[(int) length * frameSize];
        in.read(data);

        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(data),
                format, length),
                AudioFileFormat.Type.WAVE, output);

        return output;
    }

    public Domain findDomain(String name, boolean create)
            throws SpeechException {

        Domain[] domains = this.getDomains();
        for (int i = 0; i < domains.length; i++) {
            if (domains[i].getName().equals(name)) {
                return domains[i];
            }
        }
        if (create) {
            return this.createDomain(name);
        } else {
            return null;
        }
    }

    public final Property<?>[] getParameters() {

        return this.additionalParameters.values().toArray(
                new Property<?>[this.additionalParameters.size()]);
    }

    public final Property<?> getParameter(String id) {

        Property<?> p = this.additionalParameters.get(id);
        if (p != null) {
            return p;
        } else {
            for (Property<?> property : this.additionalParameters.values()) {
                if (id.equals(property.getName())) {
                    return property;
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unused")
    public void optimizeParameters()
            throws RecognizerException {

        // nothing to do
    }

    public String getEngineInfo(boolean listContexts)
            throws SpeechException {

        StringBuilder b = new StringBuilder();

        Domain[] domains = this.getDomains();
        b.append(domains.length + " users:\n");

        for (int j = 0; j < domains.length; j++) {
            RecognitionContext contexts[] = domains[j].getContexts();
            b.append("  User '" + domains[j].getName() + "': " + contexts.length
                    + " contexts.\n");
            if (listContexts) {
                for (int k = 0; k < contexts.length; k++) {
                    b.append("    context '" + contexts[k].getName() + "'\n");
                }
            }

            b.append("  Supported languages:\n");
            Language[] languages = domains[j].getLanguages();
            for (int k = 0; k < languages.length; k++) {
                if (k == 0) {
                    b.append("    ");
                } else {
                    b.append(", ");
                }
                b.append(languages[k].getName());
                if (k == languages.length - 1) {
                    b.append("\n");
                }
            }
        }

        return b.toString();
    }

    public Language[] getLanguages() throws SpeechException {

        if (this.getContext() != null) {
            return this.getContext().getDomain().getLanguages();
        } else {
            return new Language[0];
        }
    }


    protected abstract RecognitionContext createContext(String name, Grammar g, Domain domain, long timestamp) throws SpeechException;

    public abstract RecognitionContext createTemporaryContext(Grammar g, Domain domain) throws SpeechException;

    public final void setContext(Grammar grammar) throws SpeechException {
        if (grammar == null) {
            this.setContext((RecognitionContext) null);
        } else {
            if (this.getDomain() == null) {
                throw new RecognizerException("You must choose a domain first");
            }
            this.setContext(this.createTemporaryContext(grammar, this.getDomain()));
        }
    }

    public Map<String, String[]> transcribe(Collection<String> words, Language language) throws SpeechException {
        Map<String, String[]> transcriptions = new HashMap<String, String[]>();
        for (String word : words) {
            transcriptions.put(word, this.transcribe(word, language));
        }
        return transcriptions;
    }

}
