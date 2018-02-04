package com.clt.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is a common base class for all content handlers uses with an
 * {@link XMLReader}.
 *
 * @author Daniel Bobbert
 */
public class AbstractHandler implements XMLHandler {

    private String name;
    private StringBuilder value;

    /**
     * Create a new handler for any kind of element.
     */
    public AbstractHandler() {

        this(null);
    }

    /**
     * Create a handler for the element with the given name.
     *
     * @param elementName
     */
    public AbstractHandler(String elementName) {

        this.name = elementName;
    }

    /**
     * Return the name of the element, that this handler was created for.
     *
     * @return The name of this handler's main element
     */
    public String getName() {

        return this.name;
    }

    /**
     * This method handles the start of an element. Subclasses may override
     * {@link #start} to receive a nofication.
     *
     * @see #start(String, Attributes)
     */
    public final void startElement(String namespaceURI, String localName,
            String qName,
            Attributes atts)
            throws SAXException {

        this.value = null;
        this.start(qName, atts);
    }

    public final String getElementName() {

        return this.name;
    }

    /**
     * This method handles the end of an element. Subclasses may override
     * {@link #end} to receive a nofication.
     *
     * @see #end(String)
     */
    public final void endElement(String namespaceURI, String localName,
            String qName)
            throws SAXException {

        this.end(qName);
        this.value = null;
    }

    /**
     * This method handles any CDATA. Call {@link #getValue} to retrieve the
     * characters read so far.
     *
     * @see #getValue()
     */
    public final void characters(char[] ch, int start, int length) {

        if (this.value == null) {
            this.value = new StringBuilder(length);
        }

        this.value.append(ch, start, length);
    }

    /**
     * Return any CDATA text that has been read since the start of the current
     * element.
     *
     * @return The CDATA as a string
     */
    public final String getValue() {

        return this.value == null ? "" : this.value.toString();
    }

    /**
     * Notification that the start on an element has been read. The attributes
     * object is only valid during this callback. If you need to store
     * attributes for later access (e.g. in the end callback) use
     * {@link #copyAttributes} to obtain a permanent copy.
     *
     * @param name The name of the element
     * @param atts The attributes of the element
     * @throws SAXException
     */
    @SuppressWarnings("unused")
    protected void start(String name, Attributes atts)
            throws SAXException {

        // the default implementation does nothing
    }

    /**
     * Notification that the end on an element has been read. You can use
     * {@link #getValue} to obtain any CDATA text that has been read between the
     * start and end tags.
     *
     * @param name
     * @throws SAXException
     */
    @SuppressWarnings("unused")
    protected void end(String name)
            throws SAXException {

        // the default implementation does nothing
    }

    /**
     * Create a copy of the current attributes. This is useful if you need to
     * access the start element's attributes in your end callback.
     *
     * @param atts
     * @return A copy of the argument attributes
     */
    public static Attributes copyAttributes(Attributes atts) {

        return new AttributesImpl(atts);
    }

}
