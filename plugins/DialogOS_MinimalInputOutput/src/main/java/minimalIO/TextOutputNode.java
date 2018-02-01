package minimalIO;

import de.saar.coli.dialogos.marytts.plugin.Resources;
import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.AbstractOutputNode;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.VoiceName;
import com.clt.xml.XMLWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by timo on 09.10.17.
 */
public class TextOutputNode extends AbstractOutputNode {

    public enum PromptType implements IPromptType {
        text("Text"),
        expression("Expression"),
        groovy("GroovyScript");

        public IPromptType groovy() { return groovy; }
        public IPromptType expression() { return expression; }
        private String key;
        public IPromptType[] getValues() { return values(); };

        private PromptType(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return Resources.getString(this.key);
        }
    }

    @Override
    protected IPromptType getDefaultPromptType() {
        return PromptType.text;
    }

    @Override
    protected List<VoiceName> getAvailableVoices() {
        return Collections.singletonList(new VoiceName("", null));
    }

    @Override
    protected void speak(String prompt, Map<String, Object> properties) throws SpeechException {
        System.out.println("speech output: " + prompt);
    }

    @Override
    public String getResourceString(String key) {
        return key;
    }

    @Override
    protected void stopSynthesis() {
        // nothing to be done for text output
        System.err.println("being asked to stop speaking");
    }

}
