package com.clt.xml;

import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler
    implements ErrorHandler {

  Collection<String> warnings;


  public DefaultErrorHandler() {

    this.warnings = new ArrayList<String>();
  }


  private String getParseExceptionInfo(SAXParseException spe) {

    return XML.getString("ErrorInLine") + ' ' + spe.getLineNumber() + ":\n"
                + spe.getLocalizedMessage();
  }


  // The following methods are standard SAX ErrorHandler methods.
  // See SAX documentation for more info.

  public void warning(SAXParseException spe) {

    String message = this.getParseExceptionInfo(spe);
    this.warnings.add(message);
  }


  public void error(SAXParseException spe)
      throws SAXException {

    String message = this.getParseExceptionInfo(spe);
    throw new SAXException(message);
  }


  public void fatalError(SAXParseException spe)
      throws SAXException {

    String message = this.getParseExceptionInfo(spe);
    throw new SAXException(message);
  }


  public String[] getWarnings() {

    return this.warnings.toArray(new String[this.warnings.size()]);
  }
}