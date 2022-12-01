package com.clt.speech;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.clt.util.StringTools;

public class Language {
  private static Map<String, Locale> localeNames = new HashMap<String, Locale>();

  static {
    Language.localeNames.put("Deutsch", Locale.GERMANY);
    Language.localeNames.put("US English", Locale.US);
    Language.localeNames.put("english", Locale.US);
    Language.localeNames.put("British English", Locale.UK);
    Language.localeNames.put("Italiano", Locale.ITALY);
    Language.localeNames.put("Fran\u00E7ais", Locale.FRANCE);
    Language.localeNames.put("Espa\u00F1ol", new Locale("es", "ES"));
    Language.localeNames.put("Nederlands", new Locale("nl", "NL"));
  }

  private String name;
  private Locale locale;


  public Language(Locale locale) {
    this(locale, null);
  }


  public Language(String name) {
    this(name == null ? Locale.getDefault() : Language.findLocale(name), null);
  }


  public Language(Locale locale, String name) {
    if (locale == null) {
      throw new IllegalArgumentException();
    }

    if (name != null) {
      this.name = name;
    }
    else {
      if (locale.equals(Locale.GERMANY)) {
        this.name = "Deutsch";
      }
      else if (locale.equals(Locale.US)) {
        this.name = "US English";
      }
      else if (locale.equals(Locale.UK)) {
        this.name = "British English";
      }
      else if (locale.equals(Locale.FRANCE)) {
        this.name = "Fran\u00E7ais";
      }
      else if (locale.equals(Locale.ITALIAN) || locale.equals(Locale.ITALY)) {
        this.name = "Italiano";
      }
      else if (locale.equals(new Locale("nl", "NL"))) {
        this.name = "Nederlands";
      }
      else if (locale.equals(new Locale("es", "ES"))) {
        this.name = "Espa\u00F1ol";
      }
      else {
        try {
          this.name = locale.getDisplayLanguage(locale);
          if ((this.name == null) || (this.name.length() == 0)) {
            throw new NullPointerException();
          }
        } catch (Exception exn) {
          this.name = locale.getDisplayLanguage();
          if ((this.name == null) || (this.name.length() == 0)) {
            this.name = locale.getLanguage();
          }
        }

        try {
          if (locale.getDisplayCountry(locale).length() > 0) {
            this.name += " (" + locale.getDisplayCountry(locale) + ")";
          }
          else {
            throw new NullPointerException();
          }
        } catch (Exception exn) {
          try {
            if (locale.getDisplayCountry().length() > 0) {
              this.name += " (" + locale.getDisplayCountry() + ")";
            }
            else {
              throw new NullPointerException();
            }
          } catch (Exception exn2) {
            if (locale.getCountry().length() > 0) {
              this.name += " (" + locale.getCountry() + ")";
            }
          }
        }
      }
    }
    this.locale = locale;
  }


  public String getName() {
    return this.name;
  }


  public Locale getLocale() {
    return this.locale;
  }


  @Override
  public String toString() {
    return this.getName();
  }


  public static Locale findLocale(String name) {
    Locale locale = Language.localeNames.get(name);
    if (locale == null) {
      locale = StringTools.parseLocale(name);
      Language.localeNames.put(name, locale);
    }
    return locale;
  }


  @Override
  public boolean equals(Object o) {
    try {
      return this.locale.equals(((Language)o).locale);
    } catch (Exception exn) {
      return false;
    }
  }


  @Override
  public int hashCode() {
    return this.locale.hashCode();
  }
}