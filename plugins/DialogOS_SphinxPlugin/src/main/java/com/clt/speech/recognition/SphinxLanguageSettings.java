package com.clt.speech.recognition;

import com.clt.speech.Language;
import edu.cmu.sphinx.api.Configuration;

import java.util.*;

/**
 * Created by timo on 18.11.17.
 */
public class SphinxLanguageSettings {

    private static final Language en_US = new Language(Locale.US);
    private static final Language de_DE = new Language(Locale.GERMANY);

    final List<G2PEntry> g2pList  = new ArrayList<>();

    private String acousticModelPath;
    private String dictionaryPath;


    public List<G2PEntry> getG2PList() {
        return g2pList;
    }

    private SphinxLanguageSettings() {}

    /** return the default settings for a given language, or null if no such default exists */
    public static SphinxLanguageSettings createDefaultSettingsForLanguage(Language l) {
        SphinxLanguageSettings sls = new SphinxLanguageSettings();
        // TODO: put language-relevant initializations in here (this would be the place to initialize g2pLists with digits)
        if (l.equals(en_US)) {
            sls.acousticModelPath = "resource:/edu/cmu/sphinx/models/en-us/en-us";
            sls.dictionaryPath = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
        } else if (l.equals(de_DE)) {
            sls.acousticModelPath = "resource:/com/clt/speech/recognition/sphinx/model_de";
            sls.dictionaryPath = "resource:/com/clt/speech/recognition/sphinx/model_de/dic";
        } else
            throw new RuntimeException("unknown language " + l);
        return sls;
    }

    public Configuration getBaseConfiguration() {
        assert acousticModelPath != null;
        assert dictionaryPath != null;
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(acousticModelPath);
        configuration.setDictionaryPath(dictionaryPath);

        configuration.setUseGrammar(true);
        configuration.setGrammarPath("");
        configuration.setGrammarName("");
        configuration.setLanguageModelPath("");

        return configuration;
    }

    public static Map<Language,SphinxLanguageSettings> createDefault() {
        Map<Language, SphinxLanguageSettings> languageSettings = new HashMap<>();
        for (Language l : Arrays.asList(
                en_US,
                de_DE
        )) {
            languageSettings.putIfAbsent(l, SphinxLanguageSettings.createDefaultSettingsForLanguage(l));
        }
        return languageSettings;
    }
}
