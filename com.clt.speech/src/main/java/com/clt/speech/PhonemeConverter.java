package com.clt.speech;

import java.util.Locale;

/**
 * @author dabo
 *
 */
public abstract class PhonemeConverter {

    private String sourceEngine;
    private Locale sourceLocale;
    private String targetEngine;
    private Locale targetLocale;

    public PhonemeConverter(String sourceEngine, Locale sourceLocale, String targetEngine, Locale targetLocale) {
        this.sourceEngine = sourceEngine;
        this.sourceLocale = sourceLocale;
        this.targetEngine = targetEngine;
        this.targetLocale = targetLocale;
    }

    public String getSourceEngine() {

        return this.sourceEngine;
    }

    public Locale getSourceLocale() {

        return this.sourceLocale;
    }

    public String getTargetEngine() {

        return this.targetEngine;
    }

    public Locale getTargetLocale() {

        return this.targetLocale;
    }

    public abstract String convert(String phonemes);
}
