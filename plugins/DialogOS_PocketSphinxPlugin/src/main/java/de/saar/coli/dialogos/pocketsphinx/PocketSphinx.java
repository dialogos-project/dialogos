/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx;

import com.clt.audio.LevelMeter;
import com.clt.gui.KeyboardInputDialog;
import com.clt.properties.Property;
import com.clt.resources.NativeLibraryLoader;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.RecognizerEvent;
import com.clt.speech.recognition.simpleresult.SimpleRecognizerResult;
import com.clt.srgf.Grammar;
import com.clt.util.Platform;
import de.saar.coli.dialogos.pocketsphinx.plugin.Model;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import edu.cmu.pocketsphinx.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author koller
 */
public class PocketSphinx extends AbstractRecognizer {
    
    // Load native libraries for PocketSphinx.
    static {
        try {
            if (Platform.isMac()) {
                // dynamic lib is 64-bit
                NativeLibraryLoader.loadLibraryFromJar("/native/macos/libpocketsphinx_jni.dylib");
            } else if (Platform.isLinux()) {
                // dynamic lib is 64-bit
                NativeLibraryLoader.loadLibraryFromJar("/native/linux/libpocketsphinx_jni.so");
            } else if (Platform.isWindows()) {
                if (Platform.is64Bit()) {
                    NativeLibraryLoader.loadLibraryFromJar("/native/windows64/sphinxbase.dll");
                    NativeLibraryLoader.loadLibraryFromJar("/native/windows64/pocketsphinx.dll");
                    NativeLibraryLoader.loadLibraryFromJar("/native/windows64/pocketsphinx_jni.dll");
                } else {
                    NativeLibraryLoader.loadLibraryFromJar("/native/windows32/sphinxbase.dll");
                    NativeLibraryLoader.loadLibraryFromJar("/native/windows32/pocketsphinx.dll");
                    NativeLibraryLoader.loadLibraryFromJar("/native/windows32/pocketsphinx_jni.dll");
                }
            } else {
                System.err.println("The PocketSphinx plugin is currently not supported on your platform.");
                System.exit(1);
            }
        } catch (IOException ex) {
            Logger.getLogger(PocketSphinx.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
    
    
    private Model model;
    private boolean dummyMode = false; // true => inputs are typed into the keyboard, instead of calling the recognizer
    private RecognitionContext defaultContext;
    private PocketSphinxDefaultDomain defaultDomain;

    private RecognitionContext currentContext;
    private Domain currentDomain;

    // TODO - configure later

    private Decoder decoder;
    private boolean recognizerInterrupted;

    public PocketSphinx() {
        // TODO - support JSGF grammars
////        c.setString("-jsgf", "test.jsgf");

        // The "decoder" field is not initialized here. This is done
        // whenever setModel is called with a different model than before
        // (see setModel for details).
        decoder = null;

        defaultDomain = new PocketSphinxDefaultDomain();
        defaultContext = new RecognitionContext("Default context", defaultDomain, PocketSphinxDefaultDomain.US_ENGLISH, null);
        defaultDomain.addContext(defaultContext);

        try {
            setContext(defaultContext);
            setDomain(defaultDomain);
        } catch (SpeechException e) {
            // This should not happen - AK
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void resetDecoder(Model model) {
        Config c = Decoder.defaultConfig();
        
        c.setString("-hmm", model.getAcousticModelDir().getAbsolutePath());
        c.setString("-dict", model.getDictionaryFile().getAbsolutePath());
        c.setString("-lm", model.getLanguageModelFile().getAbsolutePath());

        decoder = new Decoder(c);
    }

    // TODO - make configurable?
    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }

    @Override
    protected RecognitionResult startImpl() throws SpeechException {
        if (isDummyMode()) {
            String input = KeyboardInputDialog.getInput();
            return new SimpleRecognizerResult(input);
        } else {
            recognizerInterrupted = false;

            try {
                Microphone mic = new Microphone(getAudioFormat());
                mic.start();
                InputStream ais = mic.getAudioStream();

                decoder.startUtt();
                decoder.setRawdataSize(300000);
                byte[] b = new byte[4096];
                int nbytes;

                boolean isInSpeech = false;

                this.fireRecognizerEvent(RecognizerEvent.RECOGNIZER_READY);

                while ((nbytes = ais.read(b)) >= 0) {
                    ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    short[] s = new short[nbytes / 2];
                    bb.asShortBuffer().get(s);
                    decoder.processRaw(s, nbytes / 2, false, false);

                    // Recognizer cancelled from GUI
                    if (recognizerInterrupted) {
                        decoder.endUtt();
                        mic.stop();
                        return null;
                    }

//                    int volumeLevel = getLevel(s);
//                    System.err.println(volumeLevel);
//                    controller.setAudioLevel(volumeLevel / 50);
                    if (isInSpeech != decoder.getInSpeech()) {
                        isInSpeech = decoder.getInSpeech();

                        if (isInSpeech) {
                            this.fireRecognizerEvent(RecognizerEvent.RECOGNIZER_ACTIVATED);
                        } else {
                            this.fireRecognizerEvent(RecognizerEvent.RECOGNIZER_DEACTIVATED);
                            break;
                        }
                    }
                }

                decoder.endUtt();
                mic.stop();

                NBestList nbest = decoder.nbest();
                return new PocketSphinxResult(nbest);
            } catch (IOException e) {
                throw new SpeechException(e);
            } catch (LineUnavailableException ex) {
                throw new SpeechException(ex);
            }
        }
    }

    @Override
    protected void stopImpl() throws SpeechException {
        recognizerInterrupted = true;
    }

    // Access global PocketSphinx settings. Because we do not have access
    // here to the Settings object, the #getRecognizer method in PocketSphinxNode
    // updates our settings each time a recognizer is requested, using the
    // methods below.
    
    public boolean isDummyMode() {
        return dummyMode;
    }

    public void setDummyMode(boolean dummyMode) {
        this.dummyMode = dummyMode;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        if( model != this.model ) {
            // model was changed in the settings => reinitialize decoder with new model
            resetDecoder(model);
        }
        
        this.model = model;
    }
    
    

    @Override
    protected Map<Domain, Map<String, RecognitionContext.Info>> loadContextCache(File grammarDirectory) throws SpeechException, IOException {
        System.err.println("load cc");
        return null;
    }

    @Override
    protected void saveContextCache(Collection<RecognitionContext> contexts, File grammarDirectory) throws SpeechException, IOException {
        System.err.println("save cc");

    }

    @Override
    protected RecognitionContext createContext(String name, Grammar g, Domain domain, long timestamp) throws SpeechException {
        System.err.println("create con");
        return null;
    }

    @Override
    public RecognitionContext createTemporaryContext(Grammar g, Domain domain) throws SpeechException {
        System.err.println("crate tmp con");
        return null;
    }

    @Override
    public Property<?>[] getProperties() {
        System.err.println("getprop");
        return null;
    }

    @Override
    public Domain[] getDomains() throws SpeechException {
        System.err.println("getdom");
        return null;
    }

    @Override
    public Domain createDomain(String name) throws SpeechException {
        System.err.println("create dom");
        return null;
    }

    @Override
    public void setDomain(Domain domain) throws SpeechException {
        currentDomain = domain;

    }

    @Override
    public Domain getDomain() throws SpeechException {
        return currentDomain;
    }

    @Override
    public void setContext(RecognitionContext context) throws SpeechException {
        currentContext = context;
    }

    @Override
    public RecognitionContext getContext() throws SpeechException {
        return currentContext;
    }

    @Override
    public String[] transcribe(String word, Language language) throws SpeechException {
        System.err.println("transcript");
        return null;
    }

    public void setLevelMeter(LevelMeter levelMeter) {
        System.err.println("set lvl");
    }

    public void setNBest(int value) {
        System.err.println("set nbest");
    }

}
