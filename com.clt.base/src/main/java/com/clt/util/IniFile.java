package com.clt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * An IniFile represents the contents of an .ini file on disk which is a list of
 * categories where each category describes a set of properties.
 * 
 * @author dabo
 */
public class IniFile {

  private Map<String, Properties> categories;


  public IniFile() {

    this.categories = new LinkedHashMap<String, Properties>();
  }


  public Set<String> getCategories() {

    return this.categories.keySet();
  }


  public void addCategory(String category, Properties properties) {

    this.categories.put(category, properties);
  }


  public void removeCategory(String category) {

    this.categories.remove(category);
  }


  public Properties getProperties(String category) {

    return this.categories.get(category);
  }


  public void setProperties(String category, Properties properties) {

    this.categories.put(category, properties);
  }


  public String get(String category, String key) {

    Properties p = this.getProperties(category);
    if (p == null) {
      return null;
    }
    else {
      return p.getProperty(key);
    }
  }


  public Collection<String> getMultiValues(String category, String key) {

    Collection<String> values = new ArrayList<String>();

    Properties properties = this.getProperties(category);
    for (Object property : properties.keySet()) {
      String keyName = String.valueOf(property);
      if (keyName.startsWith(key)) {
        try {
          // make sure that the remaining characters are digits only
          if (keyName.length() > key.length()) {
            Integer.parseInt(keyName.substring(key.length()));
          }
          values.add(properties.getProperty(keyName));
        } catch (NumberFormatException exn) {
          // ignore this key
        }
      }
    }

    return values;
  }


  public void put(String category, String key, String value) {

    Properties p = this.getProperties(category);
    if (p == null) {
      p = new Properties();
      this.addCategory(category, p);
    }
    p.setProperty(key, value);
  }

  private static final String keyValueSeparators = "=: \t\r\n\f";

  private static final String strictKeyValueSeparators = "=:";

  private static final String whiteSpaceChars = " \t\r\n\f";


  public static IniFile read(File f)
      throws IOException {

    return IniFile.read(new FileInputStream(f));
  }


  public static IniFile read(InputStream inStream)
      throws IOException {

    IniFile f = new IniFile();

    BufferedReader in =
      new BufferedReader(new InputStreamReader(inStream, "8859_1"));
    Properties p = new Properties();
    String category = "";

    String line;
    while ((line = in.readLine()) != null) {
      if (line.length() > 0) {
        // Continue lines that end in slashes if they are not comments
        char firstChar = line.charAt(0);
        if (firstChar == '[') {
          if ((category.length() > 0) || p.propertyNames().hasMoreElements()) {
            f.addCategory(category, p);
          }
          p = new Properties();
          category =
            IniFile.loadConvert(line.substring(1, line.indexOf(']')).trim());
        }
        else if ((firstChar != '#') && (firstChar != '!')) {
          while (IniFile.continueLine(line)) {
            String nextLine = in.readLine();
            if (nextLine == null) {
              nextLine = new String("");
            }
            String loppedLine = line.substring(0, line.length() - 1);
            // Advance beyond whitespace on new line
            int startIndex = 0;
            for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
              if (IniFile.whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1) {
                break;
              }
            }
            nextLine = nextLine.substring(startIndex, nextLine.length());
            line = new String(loppedLine + nextLine);
          }

          // Find start of key
          int len = line.length();
          int keyStart;
          for (keyStart = 0; keyStart < len; keyStart++) {
            if (IniFile.whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1) {
              break;
            }
          }

          // Blank lines are ignored
          if (keyStart == len) {
            continue;
          }

          // Find separation between key and value
          int separatorIndex;
          for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
            char currentChar = line.charAt(separatorIndex);
            if (currentChar == '\\') {
              separatorIndex++;
            }
            else if (IniFile.keyValueSeparators.indexOf(currentChar) != -1) {
              break;
            }
          }

          // Skip over whitespace after key if any
          int valueIndex;
          for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
            if (IniFile.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
              break;
            }
          }

          // Skip over one non whitespace key value separators if any
          if (valueIndex < len) {
            if (IniFile.strictKeyValueSeparators.indexOf(line
              .charAt(valueIndex)) != -1) {
              valueIndex++;
            }
          }

          // Skip over white space after other separators if any
          while (valueIndex < len) {
            if (IniFile.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
              break;
            }
            valueIndex++;
          }
          String key = line.substring(keyStart, separatorIndex);
          String value =
            (separatorIndex < len) ? line.substring(valueIndex, len) : "";

          // Convert then store key and value
          key = IniFile.loadConvert(key);
          value = IniFile.loadConvert(value);
          p.setProperty(key, value);
        }
      }
    }

    if ((category.length() > 0) || p.propertyNames().hasMoreElements()) {
      f.addCategory(category, p);
    }

    return f;
  }


  private static boolean continueLine(String line) {

    int slashCount = 0;
    int index = line.length() - 1;
    while ((index >= 0) && (line.charAt(index--) == '\\')) {
      slashCount++;
    }
    return (slashCount % 2 == 1);
  }


  /*
   * Converts encoded &#92;uxxxx to unicode chars and changes special saved
   * chars to their original forms
   */
  private static String loadConvert(String theString) {

    char aChar;
    int len = theString.length();
    StringBuilder outBuffer = new StringBuilder(len);

    for (int x = 0; x < len;) {
      aChar = theString.charAt(x++);
      if (aChar == '\\') {
        aChar = theString.charAt(x++);
        if (aChar == 'u') {
          // Read the xxxx
          int value = 0;
          for (int i = 0; i < 4; i++) {
            aChar = theString.charAt(x++);
            switch (aChar) {
              case '0':
              case '1':
              case '2':
              case '3':
              case '4':
              case '5':
              case '6':
              case '7':
              case '8':
              case '9':
                value = (value << 4) + aChar - '0';
                break;
              case 'a':
              case 'b':
              case 'c':
              case 'd':
              case 'e':
              case 'f':
                value = (value << 4) + 10 + aChar - 'a';
                break;
              case 'A':
              case 'B':
              case 'C':
              case 'D':
              case 'E':
              case 'F':
                value = (value << 4) + 10 + aChar - 'A';
                break;
              default:
                throw new IllegalArgumentException(
                  "Malformed \\uxxxx encoding.");
            }
          }
          outBuffer.append((char)value);
        }
        else {
          if (aChar == 't') {
            aChar = '\t';
          }
          else if (aChar == 'r') {
            aChar = '\r';
          }
          else if (aChar == 'n') {
            aChar = '\n';
          }
          else if (aChar == 'f') {
            aChar = '\f';
          }
          outBuffer.append(aChar);
        }
      }
      else {
        outBuffer.append(aChar);
      }
    }
    return outBuffer.toString();
  }
}