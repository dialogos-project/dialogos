/*
 * @(#)AbstractRecognizer.java
 * Created on 08.06.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */
package com.clt.speech.recognition;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.io.PreprocessingReader;
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
import com.clt.srgf.GrammarContext;
import com.clt.util.DiskTools;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class AbstractRecognizer implements Recognizer, G2P {

    private Collection<RecognizerListener> listeners = new ArrayList<RecognizerListener>();

    private boolean active = false;
    private boolean live = true;

    private Domain internalDomain = null;
    private GrammarContext internalGrammarContext = null;

    private Map<String, Property<?>> additionalParameters
            = new HashMap<String, Property<?>>();

    public void addRecognizerListener(RecognizerListener l) {

        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    public void removeRecognizerListener(RecognizerListener l) {

        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    protected void fireRecognizerEvent(int state) {

        this.fireRecognizerEvent(new RecognizerEvent(this, state));
    }

    protected void fireRecognizerEvent(RecognitionResult result) {

        this.fireRecognizerEvent(new RecognizerEvent(this, result));
    }

    protected void fireRecognizerWarning(String message) {

        this.fireRecognizerEvent(new RecognizerEvent(this, message));
    }

    protected void fireRecognizerEvent(RecognizerEvent evt) {

        synchronized (this.listeners) {
            for (RecognizerListener listener : this.listeners) {
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
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
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
     * Stops the speech recognizer. This method should do whatever is needed to
     * terminate the recognizer and perform any necessary cleanup.
     *
     * @throws SpeechException
     */
    protected abstract void stopImpl() throws SpeechException;

    public Collection<RecognitionContext> loadGrammars(File grammarDir, ProgressListener l) throws SpeechException, GrammarException {
        return this.loadGrammars(grammarDir, l, false);
    }

    public Collection<RecognitionContext> loadGrammars(File grammarDir, ProgressListener l, boolean forceRecompile)
            throws SpeechException, GrammarException {
        Domain[] ds = this.getDomains();
        for (int i = 0; i < ds.length; i++) {
            ds[i].dispose();
        }

        Collection<ContextCreator> contextCreators = new ArrayList<ContextCreator>();

        ProgressEvent evt = null;
        File domains[] = grammarDir.listFiles(AbstractRecognizer.grammarFilter);
        Map<Domain, Map<String, RecognitionContext.Info>> contextCache;
        if (domains != null) {
            if (forceRecompile) {
                contextCache
                        = new HashMap<Domain, Map<String, RecognitionContext.Info>>();
            } else {
                try {
                    if (l != null) {
                        evt
                                = new ProgressEvent(this, "Loading context database...", 0, 0, 0);
                        l.progressChanged(evt);
                    }
                    contextCache = this.loadContextCache(grammarDir);
                } catch (Exception exn) {
                    contextCache
                            = new HashMap<Domain, Map<String, RecognitionContext.Info>>();
                }
            }

            if (l != null) {
                evt
                        = new ProgressEvent(this, "Loading grammars...", 0, DiskTools
                                .countFiles(
                                        grammarDir, AbstractRecognizer.grammarFilter) * 2, 0);
                l.progressChanged(evt);
            }

            for (int i = 0; i < domains.length; i++) {
                if (domains[i].isDirectory()) {
                    GrammarContext grammarContext
                            = new GrammarContext(domains[i].getName());
                    Domain domain = this.findDomain(domains[i].getName(), true);
                    Map<String, RecognitionContext.Info> cache = contextCache.get(domain);
                    if (cache == null) {
                        cache = new HashMap<String, RecognitionContext.Info>();
                    }
                    this.loadGrammars(domains[i], "", contextCreators, domain, cache,
                            grammarContext, l,
                            evt);
                    grammarContext.optimize(false);
                }
            }
        }

        List<RecognitionContext> contexts = new ArrayList<RecognitionContext>(
                contextCreators.size());
        for (ContextCreator creator : contextCreators) {
            evt.setMessage("Creating context " + creator.name);
            evt.setCurrent(evt.getCurrent() + 1);
            if (l != null) {
                l.progressChanged(evt);
            }
            RecognitionContext context = creator.create();
            if (context != null) {
                contexts.add(context);
            }
        }

        try {
            if (l != null) {
                evt = new ProgressEvent(this, "Saving context database...", 0, 1, 1);
                l.progressChanged(evt);
            }
            this.saveContextCache(contexts, grammarDir);
        } catch (Exception ignore) {
        }

        return contexts;
    }

    private void loadGrammars(File grammarDir, String prefix,
            Collection<ContextCreator> contexts,
            Domain domain, Map<String, RecognitionContext.Info> contextCache,
            GrammarContext grammarContext, ProgressListener l, ProgressEvent evt)
            throws RecognizerException, GrammarException {

        File files[] = grammarDir.listFiles(AbstractRecognizer.grammarFilter);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    this.loadGrammars(files[i], prefix + files[i].getName() + ".",
                            contexts, domain,
                            contextCache, grammarContext, l, evt);
                } else {
                    String context = files[i].getName();
                    int ending = context.lastIndexOf('.');
                    if (ending < 0) {
                        ending = context.length();
                    }
                    context = context.substring(0, ending);

                    try {
                        if (l != null) {
                            evt.setMessage("Loading " + domain.getName() + ": " + prefix
                                    + context);
                            l.progressChanged(evt);
                        }

                        Reader r = new PreprocessingReader(files[i]);
                        try {
                            Grammar g = Grammar.create(r, this.getEnvironment());
                            if (g.getName() == null) {
                                if ((context.indexOf(' ') >= 0) || (context.indexOf('.') >= 0)) {
                                    throw new RecognizerException(
                                            "Grammar names may not contain spaces or '.'");
                                }

                                g.setName(prefix + context);
                            }

                            if (grammarContext != null) {
                                grammarContext.add(g);
                            } else {
                                g.optimizeInPlace(false);
                            }

                            RecognitionContext.Info info = contextCache.get(g.getName());
                            Set<File> includedFiles
                                    = PreprocessingReader.getIncludedFiles(files[i]);
                            long timestamp = files[i].lastModified();
                            for (File f : includedFiles) {
                                timestamp = Math.max(timestamp, f.lastModified());
                            }

                            contexts.add(new ContextCreator(g.getName(), g, domain,
                                    timestamp, info));
                            if (l != null) {
                                evt.setCurrent(evt.getCurrent() + 1);
                                l.progressChanged(evt);
                            }
                        } finally {
                            r.close();
                        }
                    } catch (Exception exn) {
                        throw new GrammarException(files[i], exn);
                    }
                }
            }
        }
    }

    public void loadInternalGrammars(String encoding)
            throws Exception {

        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("grammars.txt");
        } catch (Exception exn) {
        }

        if (is != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            Map<String, Grammar> grammars = new HashMap<String, Grammar>();

            String grammarName;
            while ((grammarName = in.readLine()) != null) {
                grammarName = grammarName.trim();
                if ((grammarName.length() > 0) && !grammarName.startsWith("#")) {
                    InputStream gStream
                            = this.getClass().getClassLoader().getResourceAsStream(
                                    "grammars/" + grammarName + ".bnf");
                    if (gStream == null) {
                        throw new IOException("Internal grammar " + grammarName
                                + " not found");
                    }

                    if (this.internalDomain == null) {
                        this.internalDomain = this.createDomain("Internal");
                    }

                    if (this.internalGrammarContext == null) {
                        this.internalGrammarContext = new GrammarContext();
                    }

                    Reader r
                            = new InputStreamReader(new BufferedInputStream(gStream), encoding);
                    Grammar g = Grammar.create(r, this.getEnvironment());
                    r.close();
                    if (g.getName() == null) {
                        g.setName(grammarName);
                    }
                    this.internalGrammarContext.add(g);

                    grammars.put(g.getName(), g);
                }
            }

            for (String name : grammars.keySet()) {
                RecognitionContext ctx
                        = this.createContext(name, grammars.get(name), this.internalDomain,
                                System.currentTimeMillis());
                if (ctx != null) {
                    this.internalDomain.addContext(ctx);
                }
            }
        }

    }

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

    protected void registerAdditionalParameter(Property<?> parameter) {

        this.additionalParameters.put(parameter.getID(), parameter);
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

    /**
     * @return a Map&lt;Domain, Map&lt;String, RecognitionContext.Info&gt;&gt;
     */
    protected abstract Map<Domain, Map<String, RecognitionContext.Info>> loadContextCache(
            File grammarDirectory)
            throws SpeechException, IOException;

    /**
     * @param contexts A collection of {@link RecognitionContext}s
     */
    protected abstract void saveContextCache(Collection<RecognitionContext> contexts, File grammarDirectory) throws SpeechException, IOException;

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

    private static FileFilter grammarFilter = new FileFilter() {
        public boolean accept(File path) {
            // skip files and directories starting with "."
            // accept files ending in .gram or .bnf or .srgf
            String name = path.getName();
            return !name.startsWith(".")
                    && (path.isDirectory() || (name.endsWith(".bnf")
                    || name.endsWith(".gram") || name.endsWith(".srgf")));
        }
    };

    public static class GrammarException
            extends Exception {

        File grammar;
        Exception exn;

        public GrammarException(File grammar, Exception exn) {

            super("Error while reading grammar file " + grammar + ":\n"
                    + exn.toString());
            this.grammar = grammar;
            this.exn = exn;
        }

        @Override
        public void printStackTrace() {

            this.exn.printStackTrace();
        }

        @Override
        public void printStackTrace(PrintStream s) {

            this.exn.printStackTrace(s);
        }

        @Override
        public String toString() {

            return this.getLocalizedMessage();
        }
    }

    private class ContextCreator {

        private String name;
        private Grammar g;
        private Domain domain;
        private long timestamp;
        private RecognitionContext.Info info;

        public ContextCreator(String name, Grammar g, Domain domain,
                long timestamp,
                RecognitionContext.Info info) {

            this.name = name;
            this.g = g;
            this.domain = domain;
            this.timestamp = timestamp;
            this.info = info;
        }

        public RecognitionContext create()
                throws SpeechException {

            boolean compile
                    = (this.info == null) || (this.info.getTimestamp() < this.timestamp);
            RecognitionContext ctx;
            if (compile) {
                ctx
                        = AbstractRecognizer.this.createContext(this.g.getName(), this.g,
                                this.domain, this.timestamp);
            } else {
                ctx = this.info.createContext(this.g, this.domain);
            }
            if (ctx != null) {
                this.domain.addContext(ctx);
            }
            return ctx;
        }
    }
}
