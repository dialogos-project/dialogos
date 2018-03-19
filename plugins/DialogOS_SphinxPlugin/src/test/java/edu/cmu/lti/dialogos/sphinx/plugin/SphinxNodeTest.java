package edu.cmu.lti.dialogos.sphinx.plugin;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.Device;
import com.clt.diamant.Grammar;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.script.Environment;
import com.clt.speech.recognition.LanguageName;
import org.junit.Test;

import java.io.*;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by timo on 09.10.17.
 */
public class SphinxNodeTest {

    @Test(timeout = 10000) public void testAvailableLanguages() {
        SphinxNode node = createNode();
        List<LanguageName> langs = node.getAvailableLanguages();
        assertNotNull(langs);
        assertFalse(langs.isEmpty());
    }
    @Test(timeout = 10000) public void testDefaultLanguage() {
        SphinxNode node = createNode();
        assertNotNull(node.getDefaultLanguage());
        List<LanguageName> langs = node.getAvailableLanguages(); // this fails but should of course work!
        assertTrue(langs.contains(node.getDefaultLanguage()));
    }

    /** test recognition node's recognition capability (not yet the pattern matching) */
    @Test(timeout = 10000) public void recognize() {
        SphinxNode node = createNode();
        node.setProperty("grammar", new Grammar("zahl", "language \"English\";\n" +
                "root $zahl;\n"
                +"$zahl"
                +" = zero  { $ = 0; }"
                +" | one   { $ = 1; }"
                +" | two   { $ = 2; }"
                +" | three { $ = 3; }"
                +" | four  { $ = 4; }"
                +" | five  { $ = 5; }"
                +" | six   { $ = 6; }"
                +" | seven { $ = 7; }"
                +" | eight { $ = 8; }"
                +" | nine  { $ = 9; };"));
        try {
            node.execute(null, null, null);
        } catch (NodeExecutionException nee) {
            nee.printStackTrace();
            System.err.println(nee.getMessage());
            assertTrue(nee.getMessage().startsWith("RecognizerError.:\ncom.clt.speech.recognition.RecognizerException: No match for recognition result"));
        }
    }

    @Test(timeout = 10000) public void recognizeAndMatch() {
        SphinxNode node = createNode();
        node.setProperty("grammar", new Grammar("zahl", "language \"English\";\n" +
                "root $zahl;\n"
                +"$zahl"
                +" = zero  { $ = 0; }"
                +" | one   { $ = 1; }"
                +" | two   { $ = 2; }"
                +" | three { $ = 3; }"
                +" | four  { $ = 4; }"
                +" | five  { $ = 5; }"
                +" | six   { $ = 6; }"
                +" | seven { $ = 7; }"
                +" | eight { $ = 8; }"
                +" | nine  { $ = 9; };"));
        Edge edge = new Edge(node, null);
        edge.setCondition("_");
        node.addEdge(edge);
        node.execute(null, null, null);
    }

    private SphinxNode createNode() {
        SphinxNode node = new SphinxNode();
        node.setGraph(new Graph(new TrivialGraphOwner()));
        return node;
    }

    private class TrivialGraphOwner implements GraphOwner {

        Plugin sphinxPlugin = new Plugin();
        PluginSettings sphinxSettings = sphinxPlugin.createDefaultSettings();

        @Override
        public PluginSettings getPluginSettings(Class<? extends com.clt.dialogos.plugin.Plugin> pluginClass) {
            return sphinxSettings;
        }

        @Override
        public Graph getSuperGraph() {
            return null;
        }

        @Override
        public Graph getOwnedGraph() {
            return null;
        }

        @Override
        public Collection<Device> getDevices() {
            return null;
        }

        @Override
        public List<Grammar> getGrammars() {
            return null;
        }

        @Override
        public Environment getEnvironment(boolean local) {
            return null;
        }

        @Override
        public void setDirty(boolean dirty) {

        }

        @Override
        public void export(Graph g, File f) throws IOException {

        }

        @Override
        public String getGraphName() {
            return null;
        }

        @Override
        public void setGraphName(String name) {

        }
    }

}
