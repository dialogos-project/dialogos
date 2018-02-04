package com.clt.speech;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * @author dabo
 *
 */
public class ConversionFactory {

    private static ConversionFactory factory;

    private Map<String, Map<Locale, Map<String, Map<Locale, PhonemeConverter>>>> phonemeConverters = null;

    private ConversionFactory() {

        this.phonemeConverters = new HashMap<String, Map<Locale, Map<String, Map<Locale, PhonemeConverter>>>>();
    }

    public static ConversionFactory getInstance() {

        if (ConversionFactory.factory == null) {
            ConversionFactory.factory = new ConversionFactory();
        }
        return ConversionFactory.factory;
    }

    public void registerConverter(PhonemeConverter converter) {

        Map<Locale, Map<String, Map<Locale, PhonemeConverter>>> sourceLocales = this.phonemeConverters.get(converter.getSourceEngine());
        if (sourceLocales == null) {
            sourceLocales = new HashMap<Locale, Map<String, Map<Locale, PhonemeConverter>>>();
            this.phonemeConverters.put(converter.getSourceEngine(), sourceLocales);
        }

        Map<String, Map<Locale, PhonemeConverter>> targetEngines = sourceLocales.get(converter.getSourceLocale());
        if (targetEngines == null) {
            targetEngines = new HashMap<String, Map<Locale, PhonemeConverter>>();
            sourceLocales.put(converter.getSourceLocale(), targetEngines);
        }

        Map<Locale, PhonemeConverter> targetLocales = targetEngines.get(converter.getTargetEngine());
        if (targetLocales == null) {
            targetLocales = new HashMap<Locale, PhonemeConverter>();
            targetEngines.put(converter.getTargetEngine(), targetLocales);
        }

        targetLocales.put(converter.getTargetLocale(), converter);
    }

    public String convert(String sourceEngine, Locale sourceLocale,
            String targetEngine,
            Locale targetLocale, String phonemes) {

        PhonemeConverter converter = null;

        // find a matching converter or a sequence of conversions
        Map<Locale, Map<String, Map<Locale, PhonemeConverter>>> sourceLocales
                = this.phonemeConverters.get(sourceEngine);
        if (sourceLocales != null) {
            Map<String, Map<Locale, PhonemeConverter>> targetEngines
                    = sourceLocales.get(sourceLocale);
            if (targetEngines == null) {
                // no exact match for source locale. Try approximation
                for (Iterator<Locale> it = sourceLocales.keySet().iterator(); it
                        .hasNext()
                        && (targetEngines == null);) {
                    Locale l = it.next();
                    if (l.getLanguage().equals(sourceLocale.getLanguage())) {
                        targetEngines = sourceLocales.get(l);
                    }
                }
            }

            if (targetEngines != null) {
                Map<Locale, PhonemeConverter> targetLocales
                        = targetEngines.get(targetEngine);
                if (targetLocales != null) {
                    converter = targetLocales.get(targetLocale);
                }
            }
        }

        if (converter != null) {
            return converter.convert(phonemes);
        } else {
            return null;
        }
    }
}
