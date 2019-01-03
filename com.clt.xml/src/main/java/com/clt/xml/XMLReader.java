package com.clt.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLReader {

    private Stack<ContentHandler> handlers;
    private Collection<CompletionRoutine> completionRoutines;
    private boolean validating;
    private org.xml.sax.XMLReader xmlReader;

    private ContentHandler contentHandler;
    private Locator locator;
    private Collection<XMLProgressListener> progressListeners = new ArrayList<>();
    private String systemID;

    /**
     * Construct a new SAX XML reader.
     *
     * @param validating Whether the parser should be validating.
     */
    public XMLReader(boolean validating) {

        this(validating, null);
    }

    public XMLReader(boolean validating, String systemID) {

        this.validating = validating;
        this.systemID = systemID;
        this.handlers = new Stack<>();
    }

    public String getSystemID() {

        return this.systemID;
    }

    public void setSystemID(String systemID) {

        this.systemID = systemID;
    }

    /**
     * Add a progress listener that should be notified of progress during a
     * parse.
     *
     * @param l an XMLProgressListener that should be notified of progress
     * during a parse.
     */
    public void addProgressListener(XMLProgressListener l) {

        synchronized (this.progressListeners) {
            this.progressListeners.add(l);
        }
    }

    /**
     * Remove a previously registered progress listener.
     *
     * @param l Reference on the XMLProgressListener to be removed.
     */
    public void removeProgressListener(XMLProgressListener l) {

        synchronized (this.progressListeners) {
            this.progressListeners.remove(l);
        }
    }

    private void fireProgress(float progress) {

        synchronized (this.progressListeners) {
            for (XMLProgressListener l : this.progressListeners) {
                l.percentComplete(progress);
            }
        }
    }

    /**
     * Parse the given file using the given top level content handler. The
     * handler's name may be <code>null</code>. In this case the document will
     * be parsed, no matter what the main element is. If the name is not
     * <code>null</code> it must match the main element of the document.
     *
     * @throws IOException
     */
    public void parse(File f, XMLHandler handler) throws IOException {
        this.parse(f, handler, new DefaultErrorHandler());
    }

    /**
     * Parse the given file using the given top level content and error
     * handlers. The handler's name may be <code>null</code>. In this case the
     * document will be parsed, no matter what the main element is. If the name
     * is not <code>null</code> it must match the main element of the document.
     *
     * @throws IOException
     */
    public void parse(File f, XMLHandler handler, ErrorHandler errHandler) throws IOException {

        InputStream in = new CountingInputStream(
                new BufferedInputStream(new FileInputStream(f)),
                f.length());

        try {
            this.parse(in, handler, errHandler);
        } finally {
            in.close();
        }
    }

    /**
     * Parse the given stream using the given top level content handler. The
     * handler's name may be <code>null</code>. In this case the document will
     * be parsed, no matter what the main element is. If the name is not
     * <code>null</code> it must match the main element of the document.
     *
     * @throws IOException
     */
    public void parse(InputStream in, XMLHandler handler) throws IOException {
        this.parse(in, handler, new DefaultErrorHandler());
    }

    /**
     * Parse the given stream using the given top level content and error
     * handlers. The handler's name may be <code>null</code>. In this case the
     * document will be parsed, no matter what the main element is. If the name
     * is not <code>null</code> it must match the main element of the document.
     *
     * @throws IOException
     */
    public void parse(InputStream input, final XMLHandler handler, ErrorHandler errHandler) throws IOException {

        this.xmlReader = null;

        if ((input == null) || (handler == null) || (errHandler == null)) {
            throw new IllegalArgumentException();
        }

        final CountingInputStream in;
        if (input instanceof CountingInputStream) {
            in = (CountingInputStream) input;
        } else {
            in = new CountingInputStream(input);
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();
        if (spf == null) {
            throw new IOException(XML.getString("CouldNotCreateFactory"));
        }
        spf.setValidating(this.validating);

        try {
            // Create a JAXP SAXParser and get the encapsulated SAX XMLReader
            SAXParser saxParser = spf.newSAXParser();
            this.xmlReader = saxParser.getXMLReader();
        } catch (Exception exn) {
            throw new IOException(exn.getLocalizedMessage());
        }

        if (this.xmlReader == null) {
            throw new IOException(XML.getString("CouldNotGetReader"));
        }

        this.xmlReader.setErrorHandler(errHandler);
        this.contentHandler = new DefaultHandler() {

            @Override
            public void startElement(String namespaceURI, String localName,
                    String qName,
                    Attributes atts)
                    throws SAXException {

                String name = handler.getElementName();
                if (name != null) {
                    if (!name.equals(qName)) {
                        XMLReader.this.raiseUnexpectedElementException(qName);
                    }
                    XMLReader.this.setHandler(handler);
                }

                handler.startElement(namespaceURI, localName, qName, atts);
            }

            @Override
            public void endElement(String namespaceURI, String localName, String qName)
                    throws SAXException {

                handler.endElement(namespaceURI, localName, qName);
            }

            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {

                handler.characters(ch, start, length);
            }
        };

        this.xmlReader.setContentHandler(new ContentHandler() {

            public void characters(char[] ch, int start, int length)
                    throws SAXException {

                XMLReader.this.contentHandler.characters(ch, start, length);
            }

            public void endDocument()
                    throws SAXException {

                XMLReader.this.contentHandler.endDocument();
            }

            public void startDocument()
                    throws SAXException {

                XMLReader.this.contentHandler.startDocument();
            }

            public void startElement(String namespaceURI, String localName,
                    String qName,
                    Attributes atts)
                    throws SAXException {

                // avoid possible overhead of exception, if there are no
                // listeners
                long totalLength = in.getLength();
                if (!XMLReader.this.progressListeners.isEmpty() && totalLength > 0) {
                    XMLReader.this.fireProgress((float) in.getPosition()
                            / (float) totalLength * 0.9f);
                }
                XMLReader.this.contentHandler.startElement(namespaceURI, localName,
                        qName, atts);
            }

            public void endElement(String namespaceURI, String localName, String qName)
                    throws SAXException {

                XMLReader.this.contentHandler
                        .endElement(namespaceURI, localName, qName);
            }

            public void startPrefixMapping(String prefix, String uri)
                    throws SAXException {

                XMLReader.this.contentHandler.startPrefixMapping(prefix, uri);
            }

            public void endPrefixMapping(String prefix)
                    throws SAXException {

                XMLReader.this.contentHandler.endPrefixMapping(prefix);
            }

            public void ignorableWhitespace(char[] ch, int start, int length)
                    throws SAXException {

                XMLReader.this.contentHandler.ignorableWhitespace(ch, start, length);
            }

            public void processingInstruction(String target, String data)
                    throws SAXException {

                XMLReader.this.contentHandler.processingInstruction(target, data);
            }

            public void setDocumentLocator(Locator locator) {

                XMLReader.this.contentHandler.setDocumentLocator(locator);
                XMLReader.this.locator = locator;
            }

            public void skippedEntity(String name)
                    throws SAXException {

                XMLReader.this.contentHandler.skippedEntity(name);
            }
        });

        this.completionRoutines = new ArrayList<>();

        try {
            InputSource source = new InputSource(in);

            if (source.getSystemId() == null) {
                String systemID = this.systemID;
                if (systemID == null) {
                    URL dtd = ClassLoader.getSystemResource("dtd/");
                    if (dtd != null) {
                        systemID = dtd.toString();
                    }
                }
                if (systemID != null) {
                    source.setSystemId(systemID);
                }
            }

            if (!this.validating) {
                this.xmlReader.setEntityResolver(new EntityResolver() {

                    public InputSource resolveEntity(String publicId, String systemId) {

                        if (systemId.startsWith("http://") || true) {
                            InputSource is;
                            try {
                                is = new InputSource(new URL(systemId).openConnection()
                                        .getInputStream());
                            } catch (Exception exn) {
                                // ignore unresolvable external entities
                                is = new InputSource(new StringReader(""));
                            }
                            is.setPublicId(publicId);
                            is.setSystemId(systemId);
                            return is;
                        } else {
                            return null;
                        }
                    }
                });
            }

            this.xmlReader.parse(source);
            if (!this.handlers.empty()) {
                throw new IllegalStateException("Handler stack not empty");
            }

            int i = 0;
            for (CompletionRoutine r : this.completionRoutines) {
                r.run();
                this.fireProgress(0.9f + (i++) * 0.1f / this.completionRoutines.size());
            }
        } catch (SAXException exn) {
            // exn.printStackTrace();
            throw new XMLFormatException(exn.getLocalizedMessage(), exn);
        }
    }

    /**
     * Install a subordinate content handler for the current element. This
     * method should be called from within the {@link XMLHandler#startElement}
     * callback during a parse to handle that element. As soon as the end tag
     * for that element is reached, the handler is discarded and control is
     * returned to the previously active handler. Several handlers can be
     * stacked in such a way. Keep in mind though, that the parser will simply
     * look for the name of an end tag in order to decide whether it should
     * discard the current handler. This is a problem when you try to parse
     * elements that can recursively contain elements of the same name. You can
     * circumvent this problem be recursively installing subordinate handlers
     * whenever you encounter the start of an element with the same name.
     *
     * @param handler
     */
    public void setHandler(final XMLHandler handler) {

        if (handler.getElementName() == null) {
            throw new IllegalArgumentException("Handler must have a name");
        }

        ContentHandler h = new DefaultHandler() {

            @Override
            public void startElement(String namespaceURI, String localName,
                    String qName,
                    Attributes atts)
                    throws SAXException {

                handler.startElement(namespaceURI, localName, qName, atts);
            }

            @Override
            public void endElement(String namespaceURI, String localName, String qName)
                    throws SAXException {

                handler.endElement(namespaceURI, localName, qName);
                if (handler.getElementName().equals(qName)) {
                    XMLReader.this.contentHandler = XMLReader.this.handlers.pop();
                }
            }

            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {

                handler.characters(ch, start, length);
            }
        };

        this.handlers.push(this.contentHandler);
        this.contentHandler = h;
    }

    /**
     * Install a subordinate handler that will silently read all input until the
     * end of the given element is reached. This method handles recursive
     * elements correctly.
     *
     * @param element
     */
    public void skipElement(final String element) {

        this.setHandler(new AbstractHandler(element) {

            @Override
            protected void start(String name, Attributes atts) {

                if (name.equals(element)) {
                    XMLReader.this.skipElement(element);
                }
            }
        });
    }

    /**
     * Add a completion routine that will automatically be run when the parser
     * finishes parsing the current document. You can use this routine in the
     * callback of an {@link XMLHandler} to register any cleanup action that you
     * need to run at the end of the parse.
     */
    public void addCompletionRoutine(final Runnable r) {

        this.completionRoutines.add(new CompletionRoutine() {

            public void run() {

                r.run();
            }
        });
    }

    /**
     * Add a completion routine that will automatically be run when the parser
     * finishes parsing the current document. You can use this routine in the
     * callback of an {@link XMLHandler} to register any cleanup action that you
     * need to run at the end of the parse.
     */
    public void addCompletionRoutine(CompletionRoutine r) {

        this.completionRoutines.add(r);
    }

    public interface CompletionRoutine {

        public abstract void run()
                throws SAXException;
    }

    public void raiseException(Exception exn)
            throws SAXException {

        String message = exn.getLocalizedMessage();
        if ((message == null) || (message.length() == 0)) {
            message = exn.toString();
        }
        this.raiseException(message, exn);
    }

    public void raiseException(String message)
            throws SAXException {

        this.raiseException(message, null);
    }

    public void raiseException(String message, Exception source)
            throws SAXException {

        if (this.locator == null) {
            throw new SAXException(message, source);
        } else {
            throw new SAXException(MessageFormat.format(XML
                    .getString("XMLErrorInLine"),
                    this.locator.getLineNumber())
                    + ": " + message, source);
        }
    }

    public void raiseUnexpectedElementException(String elementName)
            throws SAXException {

        throw new SAXException(MessageFormat.format(XML
                .getString("UnexpectedElement"),
                elementName));
    }

    public void raiseAttributeValueException(String attributeName)
            throws SAXException {

        throw new SAXException(MessageFormat.format(XML
                .getString("IllegalAttributeValue"),
                attributeName));
    }

    public static InputStream createStream(String xml) {

        if (!xml.startsWith("<?xml")) {
            throw new IllegalArgumentException("Not an XML document");
        }

        int pos = "<?xml".length();
        if ((pos >= xml.length())
                || ((xml.charAt(pos) != '?') && !Character
                .isWhitespace(xml.charAt(pos)))) {
            throw new IllegalArgumentException("Not an XML document");
        }

        int headerEnd = xml.indexOf('>');
        if (headerEnd < 0) {
            throw new IllegalArgumentException("Not an XML document");
        }

        String attributes = xml.substring("<?xml".length(), headerEnd).trim();
        if (!attributes.endsWith("?")) {
            throw new IllegalArgumentException("Not an XML document");
        }
        attributes = attributes.substring(0, attributes.length() - 1);

        String encoding = null;
        pos = 0;
        while ((pos < attributes.length()) && (encoding == null)) {
            int end = attributes.indexOf('=', pos);
            if (end < 0) {
                throw new IllegalArgumentException(
                        "Not an XML document: Illegal argument");
            }

            String attName = attributes.substring(pos, end).trim();
            pos = end + 1;
            while ((pos < attributes.length())
                    && Character.isWhitespace(attributes.charAt(pos))) {
                pos++;
            }

            if ((pos >= attributes.length()) && (attributes.charAt(pos) != '"')) {
                throw new IllegalArgumentException("Not an XML document");
            } else {
                pos++;
            }

            end = attributes.indexOf('"', pos);
            if (end < 0) {
                throw new IllegalArgumentException(
                        "Not an XML document: Unterminated argument");
            }

            String attValue = attributes.substring(pos, end);

            pos = end + 1;
            while ((pos < attributes.length())
                    && Character.isWhitespace(attributes.charAt(pos))) {
                pos++;
            }

            if (attName.equals("encoding")) {
                encoding = attValue;
            }
        }

        final String body = "<?xml version=\"1.0\" encoding=\"UTF16\" ?>\n "
                + xml.substring(headerEnd + 1);

        return new InputStream() {

            private int pos = 0;
            private boolean high = true;

            @Override
            public int read() {

                if (this.pos >= body.length()) {
                    return -1;
                } else {
                    char c = body.charAt(this.pos);
                    if (this.high) {
                        this.high = false;
                        return (c & 0xFF00) >> 8;
                    } else {
                        this.high = true;
                        this.pos++;
                        return (c & 0x00FF);
                    }
                }
            }
        };
    }
}
