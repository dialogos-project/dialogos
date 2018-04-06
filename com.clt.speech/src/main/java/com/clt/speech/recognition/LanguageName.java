package com.clt.speech.recognition;

import com.clt.speech.Language;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class LanguageName
    implements Comparable<LanguageName> {

  private String name;
  private Language language;


  public LanguageName(String name, Language language) {

    this.name = name;
    this.language = language;
  }


  public String getName() {

    return this.name;
  }


  public Language getLanguage() {

    return this.language;
  }


  @Override
  public boolean equals(Object o) {

    if (o instanceof LanguageName) {
      LanguageName ln = (LanguageName)o;
      if (ln.name.equals(this.name)) {
        return this.language == null ? ln.language == null : this.language
          .equals(ln.language);
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }


  @Override
  public int hashCode() {

    return this.name.hashCode();
  }


  @Override
  public String toString() {

    if (StringTools.isEmpty(this.name)) {
      return "<" + "DefaultLanguage>"; // TODO: Resources.getString("DefaultLanguage") + ">";
    }
    else {
      String name = this.getName();
      if (this.language == null) {
        return name + " " + "unavailable";// TODO: Resources.getString("NotAvailable");
      }
      else {
        return this.language.getName();
      }
    }
  }


  public int compareTo(LanguageName o) {

    int result = this.name.compareTo(o.name);
    if (result != 0) {
      return result;
    }
    else if (this.language == null) {
      return o.language == null ? 0 : 1;
    }
    else if (o.language == null) {
      return this.language == null ? 0 : -1;
    }
    else {
      return this.language.toString().compareTo(o.language.toString());
    }
  }
}
