package com.clt.speech;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author dabo
 *
 */
public class PhonemeMap extends PhonemeConverter {

    protected Map<String, String> entries;
    private int maxKeyLength;

    public PhonemeMap(String sourceEngine, Locale sourceLocale, String targetEngine, Locale targetLocale) {

        super(sourceEngine, sourceLocale, targetEngine, targetLocale);

        this.entries = new HashMap<String, String>();
        this.maxKeyLength = 0;
    }

    public void add(String key, String value) {

        this.entries.put(key, value);
        this.maxKeyLength = Math.max(this.maxKeyLength, key.length());
    }

    @Override
    public String convert(String phonemes) {

        StringBuilder b = new StringBuilder(Math.max(10, phonemes.length() * 2));

        int length = phonemes.length();
        int pos = 0;
        while (pos < length) {
            boolean found = false;
            for (int n = Math.min(this.maxKeyLength, length - pos); !found && (n > 0); n--) {
                String converted = this.entries.get(phonemes.substring(pos, pos + n));

                if (converted != null) {
                    b.append(converted);
                    pos += n;
                    found = true;
                    break;
                }
            }

            if (!found) {
                b.append(phonemes.charAt(pos++));
            }
        }

        return b.toString();
    }
}
