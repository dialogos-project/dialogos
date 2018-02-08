package de.saar.coli.dialogos.pocketsphinx.client;

import com.clt.dialog.client.RecognizerClient;
import com.clt.event.ProgressListener;
import com.clt.properties.Property;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.StructValue;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.RecognitionContext;
import de.saar.coli.dialogos.pocketsphinx.PocketSphinx;
import java.util.Properties;
import javax.sound.sampled.AudioFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author koller
 */
public class PocketSphinxClient extends RecognizerClient<PocketSphinx> {

    @Override
    protected PocketSphinx createRecognizer(Properties properties, ProgressListener progress) throws SpeechException {
        // TODO: Vocon recognizer extracts paths to available language models from the
        // "properties" at this point, so they can be passed to the ASR constructor.
        // For now, we skip this and just use the default US English LM.

        return new PocketSphinx();
    }

    @Override
    protected void activateContextImpl(RecognitionContext context, StructValue addWords, ListValue phonetics) throws SpeechException {
        // TODO I don't understand contexts yet -- perhaps fill this in
        // once it becomes clearer. AK
    }

    @Override
    protected boolean initializeImpl(ProgressListener progress, String[] args, Properties properties) {
        PocketSphinx recognizer = this.getRecognizer();

        try {
            this.levelMeter.setAudioFormat(new AudioFormat(16000, 16, 1, true, false));
            this.levelMeter.setDecay(0);
            if (this.showLevels.getValue()) {
                PocketSphinxClient.this.getRecognizer().setLevelMeter(this.levelMeter);
            }
            this.showLevels.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {

                    if (PocketSphinxClient.this.showLevels.getValue()) {
                        PocketSphinxClient.this.getRecognizer().setLevelMeter(PocketSphinxClient.this.levelMeter);
                    } else {
                        PocketSphinxClient.this.getRecognizer().setLevelMeter(null);
                    }
                }
            });

            this.numAlternatives.setRange(1, 1000);
            this.numAlternatives.setEditType(Property.EDIT_TYPE_NUMBERFIELD);
            this.numAlternatives.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    PocketSphinxClient.this.getRecognizer().setNBest(PocketSphinxClient.this.numAlternatives.getValue());
                }
            });
            recognizer.setNBest(this.numAlternatives.getValue());

            // TODO - add all these features back in when they are implemented AK
            
//            String property = properties.getProperty("Grammars");
//            if (property != null) {
//                this.grammarDir = new File(property);
//            }

//            boolean ignoreCache = properties.getProperty("IgnoreCache", "false").equalsIgnoreCase("true");
//
//            if (properties.getProperty("Record") != null) {
//                recognizer.setRecording(properties.getProperty("Record")
//                        .equalsIgnoreCase("true"));
//            }
//
//            if (properties.getProperty("ClearRecordings", "false").equals("true")) {
//                recognizer.clearRecordings();
//            }
//
//            if (properties.getProperty("AGC") != null) {
//                recognizer.setAGC(properties.getProperty("AGC").equalsIgnoreCase("true"));
//            }

//            recognizer.loadGrammars(this.grammarDir, progress, ignoreCache);
        } catch (ThreadDeath d) {
            throw d;
        } catch (Throwable exn) {
            this.error(exn);
            return false;
        }

        this.updateMenus();
        return true;
    }

    
    
    
    
    
    @Override
    public String getRecognizerName() {
        return "pocketsphinx";
    }

    @Override
    public int getPreferredPort() {
        return 2074;
    }

    @Override
    public String getDescription() {
        return "A wrapper for the CMU PocketSphinx recognizer";
    }

    @Override
    public String getVersion() {
        return "2017-12-27";
    }

    @Override
    public String getName() {
        return "CMU PocketSphinx";
    }

}
