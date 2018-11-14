package edu.cmu.lti.dialogos.sphinx.client;

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
    boolean revalidateG2P = true;

    private String acousticModelPath;
    private String dictionaryPath;
    private String garbageRulesText;


    public List<G2PEntry> getG2PList() {
        return g2pList;
    }

    public void g2pListUpdate() {
        revalidateG2P = true;
    }

    private SphinxLanguageSettings() {}

    /** return the default settings for a given language, or null if no such default exists */
    public static SphinxLanguageSettings createDefaultSettingsForLanguage(Language l) {
        SphinxLanguageSettings sls = new SphinxLanguageSettings();
        // put language-relevant initializations in here (this could be the place to initialize g2pLists with digits)
        if (l.equals(en_US)) {
            sls.acousticModelPath = "resource:/edu/cmu/sphinx/models/en-us/en-us";
            sls.dictionaryPath = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
            sls.garbageRulesText = "\n\n<GARBAGE> = /0.999/ <NULL> | /0.001/ (<WEIGHTED_PHONE> <WEIGHTED_PHONE> [<WEIGHTED_PHONE>]) <GARBAGE> ;\n" +
                    "\n" +
                    "<WEIGHTED_PHONE>\n" +
                    "= /0.0293313/ \"<PHONE_AA>\"\n" +
                    "| /0.0255437/ \"<PHONE_AE>\"\n" +
                    "| /0.0825311/ \"<PHONE_AH>\"\n" +
                    "| /0.0134171/ \"<PHONE_AO>\"\n" +
                    "| /0.00391433/ \"<PHONE_AW>\"\n" +
                    "| /0.0133624/ \"<PHONE_AY>\"\n" +
                    "| /0.0248555/ \"<PHONE_B>\"\n" +
                    "| /0.00574187/ \"<PHONE_CH>\"\n" +
                    "| /0.0377366/ \"<PHONE_D>\"\n" +
                    "| /0.000677771/ \"<PHONE_DH>\"\n" +
                    "| /0.0321389/ \"<PHONE_EH>\"\n" +
                    "| /0.0337944/ \"<PHONE_ER>\"\n" +
                    "| /0.015898/ \"<PHONE_EY>\"\n" +
                    "| /0.0161898/ \"<PHONE_F>\"\n" +
                    "| /0.015877/ \"<PHONE_G>\"\n" +
                    "| /0.0108897/ \"<PHONE_HH>\"\n" +
                    "| /0.058451/ \"<PHONE_IH>\"\n" +
                    "| /0.040693/ \"<PHONE_IY>\"\n" +
                    "| /0.00741711/ \"<PHONE_JH>\"\n" +
                    "| /0.049962/ \"<PHONE_K>\"\n" +
                    "| /0.0579941/ \"<PHONE_L>\"\n" +
                    "| /0.0344779/ \"<PHONE_M>\"\n" +
                    "| /0.0710136/ \"<PHONE_N>\"\n" +
                    "| /0.011593/ \"<PHONE_NG>\"\n" +
                    "| /0.0224211/ \"<PHONE_OW>\"\n" +
                    "| /0.00149156/ \"<PHONE_OY>\"\n" +
                    "| /0.0231477/ \"<PHONE_P>\"\n" +
                    "| /0.0538775/ \"<PHONE_R>\"\n" +
                    "| /0.058415/ \"<PHONE_S>\"\n" +
                    "| /0.0102224/ \"<PHONE_SH>\"\n" +
                    "| /0.0568107/ \"<PHONE_T>\"\n" +
                    "| /0.00342024/ \"<PHONE_TH>\"\n" +
                    "| /0.00270876/ \"<PHONE_UH>\"\n" +
                    "| /0.0115512/ \"<PHONE_UW>\"\n" +
                    "| /0.0125335/ \"<PHONE_V>\"\n" +
                    "| /0.0104293/ \"<PHONE_W>\"\n" +
                    "| /0.00610459/ \"<PHONE_Y>\"\n" +
                    "| /0.0326981/ \"<PHONE_Z>\"\n" +
                    "| /0.000667308/ \"<PHONE_ZH>\"\n" +
                    ";";
        } else if (l.equals(de_DE)) {
            sls.acousticModelPath = "resource:/edu/cmu/sphinx/models/de-de/"; // Preferences.getInstallationDirectory() + "/pluginData/dialogos.plugin.sphinx/model_de";
            sls.dictionaryPath = "resource:/edu/cmu/sphinx/models/de-de/dic"; // Preferences.getInstallationDirectory() + "/pluginData/dialogos.plugin.sphinx/model_de/dic";
            sls.garbageRulesText = "\n\n<GARBAGE> = /0.999/ <NULL> | /0.001/ (<WEIGHTED_PHONE> <WEIGHTED_PHONE> [<WEIGHTED_PHONE>]) <GARBAGE> ;\n" +
                    "\n" +
                    "<WEIGHTED_PHONE>\n" +
                    "= /0.0503988/ \"<PHONE_@>\"\n" +
                    "| /0.00235297/ \"<PHONE_2:>\"\n" +
                    "| /0.0469168/ \"<PHONE_6>\"\n" +
                    "| /0.00114369/ \"<PHONE_9>\"\n" +
                    "| /0.0316212/ \"<PHONE_a>\"\n" +
                    "| /0.0314295/ \"<PHONE_a:>\"\n" +
                    "| /0.0154863/ \"<PHONE_aI>\"\n" +
                    "| /0.00762885/ \"<PHONE_aU>\"\n" +
                    "| /0.0230534/ \"<PHONE_b>\"\n" +
                    "| /0.00956313/ \"<PHONE_C+>\"\n" +
                    "| /0.0215747/ \"<PHONE_d>\"\n" +
                    "| /0.017241/ \"<PHONE_e:>\"\n" +
                    "| /0.0296197/ \"<PHONE_E+>\"\n" +
                    "| /0.00476294/ \"<PHONE_E+:>\"\n" +
                    "| /0.0302973/ \"<PHONE_f>\"\n" +
                    "| /0.0254595/ \"<PHONE_g>\"\n" +
                    "| /0.010569/ \"<PHONE_h>\"\n" +
                    "| /0.0281118/ \"<PHONE_i:>\"\n" +
                    "| /0.029536/ \"<PHONE_I+>\"\n" +
                    "| /0.00839779/ \"<PHONE_j>\"\n" +
                    "| /0.035158/ \"<PHONE_k>\"\n" +
                    "| /0.049669/ \"<PHONE_l>\"\n" +
                    "| /0.0293402/ \"<PHONE_m>\"\n" +
                    "| /0.08541/ \"<PHONE_n>\"\n" +
                    "| /0.0160477/ \"<PHONE_N+>\"\n" +
                    "| /0.0254236/ \"<PHONE_o:>\"\n" +
                    "| /0.0145203/ \"<PHONE_O+>\"\n" +
                    "| /0.00297735/ \"<PHONE_OY>\"\n" +
                    "| /0.0210812/ \"<PHONE_p>\"\n" +
                    "| /0.045634/ \"<PHONE_r>\"\n" +
                    "| /0.0581308/ \"<PHONE_s>\"\n" +
                    "| /0.0243814/ \"<PHONE_S+>\"\n" +
                    "| /0.0912421/ \"<PHONE_t>\"\n" +
                    "| /0.0104183/ \"<PHONE_u:>\"\n" +
                    "| /0.0204247/ \"<PHONE_U+>\"\n" +
                    "| /0.0161932/ \"<PHONE_v>\"\n" +
                    "| /0.00388288/ \"<PHONE_x>\"\n" +
                    "| /0.00409239/ \"<PHONE_y:>\"\n" +
                    "| /0.00393351/ \"<PHONE_Y+>\"\n" +
                    "| /0.016311/ \"<PHONE_z>\"\n" +
                    "| /0.000563249/ \"<PHONE_Z+>\"\n" +
                    ";\n" +
                    "\n" +
                    "<CONSONANT>\n" +
                    "= /0.0230534/ \"<PHONE_b>\"\n" +
                    "| /0.00956313/ \"<PHONE_C+>\"\n" +
                    "| /0.0215747/ \"<PHONE_d>\"\n" +
                    "| /0.0302973/ \"<PHONE_f>\"\n" +
                    "| /0.0254595/ \"<PHONE_g>\"\n" +
                    "| /0.010569/ \"<PHONE_h>\"\n" +
                    "| /0.00839779/ \"<PHONE_j>\"\n" +
                    "| /0.035158/ \"<PHONE_k>\"\n" +
                    "| /0.049669/ \"<PHONE_l>\"\n" +
                    "| /0.0293402/ \"<PHONE_m>\"\n" +
                    "| /0.08541/ \"<PHONE_n>\"\n" +
                    "| /0.0160477/ \"<PHONE_N+>\"\n" +
                    "| /0.0210812/ \"<PHONE_p>\"\n" +
                    "| /0.045634/ \"<PHONE_r>\"\n" +
                    "| /0.0581308/ \"<PHONE_s>\"\n" +
                    "| /0.0243814/ \"<PHONE_S+>\"\n" +
                    "| /0.0912421/ \"<PHONE_t>\"\n" +
                    "| /0.0161932/ \"<PHONE_v>\"\n" +
                    "| /0.00388288/ \"<PHONE_x>\"\n" +
                    "| /0.016311/ \"<PHONE_z>\"\n" +
                    "| /0.000563249/ \"<PHONE_Z+>\"\n" +
                    ";\n" +
                    "<VOWEL>\n" +
                    "= /0.0503988/ \"<PHONE_@>\"\n" +
                    "| /0.00235297/ \"<PHONE_2:>\"\n" +
                    "| /0.0469168/ \"<PHONE_6>\"\n" +
                    "| /0.00114369/ \"<PHONE_9>\"\n" +
                    "| /0.0316212/ \"<PHONE_a>\"\n" +
                    "| /0.0314295/ \"<PHONE_a:>\"\n" +
                    "| /0.0154863/ \"<PHONE_aI>\"\n" +
                    "| /0.00762885/ \"<PHONE_aU>\"\n" +
                    "| /0.017241/ \"<PHONE_e:>\"\n" +
                    "| /0.0296197/ \"<PHONE_E+>\"\n" +
                    "| /0.00476294/ \"<PHONE_E+:>\"\n" +
                    "| /0.0281118/ \"<PHONE_i:>\"\n" +
                    "| /0.029536/ \"<PHONE_I+>\"\n" +
                    "| /0.0254236/ \"<PHONE_o:>\"\n" +
                    "| /0.0145203/ \"<PHONE_O+>\"\n" +
                    "| /0.00297735/ \"<PHONE_OY>\"\n" +
                    "| /0.0104183/ \"<PHONE_u:>\"\n" +
                    "| /0.0204247/ \"<PHONE_U+>\"\n" +
                    "| /0.00409239/ \"<PHONE_y:>\"\n" +
                    "| /0.00393351/ \"<PHONE_Y+>\"\n" +
                    ";\n";
        } else
            throw new IllegalArgumentException("unknown language " + l);
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

    public String getGarbageRulesText() {
        return garbageRulesText;
    }
}
