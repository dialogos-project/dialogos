package de.saar.coli.dialogos.marytts;

import marytts.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Nicolas on 7/12/17.
 */
public final class XMLHelper {

  private XMLHelper() {
    throw new IllegalStateException("Utility class must not be instantiated");
  }

  /*
   * str2Document: Given a valid XML in String, produces a
   * corresponding Document Element.
   **/
  public static Document str2Document(String str, String errorMsg){
    Object d = new Object();
    try {
      d = DomUtils.parseDocument(new StringReader(str));
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
        System.out.println(errorMsg + " SAX error.");
      e.printStackTrace();
    } catch (IOException e) {
        System.out.println(errorMsg + " IO error.");
        e.printStackTrace();
    }
    return (Document) d;
  }

    /*
  * xml2Str: Reproduces a String from a Document-type
  *
  * Taken from:
  * https://stackoverflow.com/questions/5456680/xml-document-to-string
  * (WhiteFang34 answer)
  * */
  public static String xml2str(Document xml) {
    TransformerFactory tf = TransformerFactory.newInstance();
    StringWriter writer = new StringWriter();
    try {
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.transform(new DOMSource(xml), new StreamResult(writer));
    } catch (TransformerException e) {
      System.err.println("Conversion from XML to string failed.");
      e.printStackTrace();
    }
    return writer.getBuffer().toString().replaceAll("\n|\r", "");
  }
}
