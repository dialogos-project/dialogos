package com.clt.diamant;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.SAXException;

import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.GraphNode;
import com.clt.diamant.graph.nodes.ReturnNode;

/**
 * Stuff to do: TODO what is the initial logging path? TODO why save the path as
 * a field?
 *
 * Wish List: TODO log more specific node information (e.g. speech input failed)
 *
 * @author Bri Burr / Till Kollenda
 *
 */
public class ExecutionLogger {
    // Filename of the XML schema

    private static final String XML_SCHEMA = "log_schema.rnc";

    // String constants for the XML tags
    private static final String ROOT = "log";

    private static final String SETUP = "setup";
    private static final String EXECUTION = "execution";

    private static final String PROJECT = "project";
    private static final String START = "start";
    private static final String GLOBAL_VARS = "global_variables";

    private static final String VAR = "var";

    private static final String NODE = "node";
    private static final String VAR_UPDATE = "variable_updated";
    private static final String ERROR = "error";

    private static final String NAME = "name";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String TRANS_TIME = "transition_time_ms";
    private static final String SOURCE = "source";
    private static final String MESSAGE = "message";

    // Just for testing. This should always be true.
    // set to false because of a conflict with Jing and the MaryTTSPlugin
    // TODO set to true again when this problem is fixed and Jing is being build again
    private final boolean _validationEnabled = false;

    private String _logFilePath;
    private boolean _loggingEnabled = false;
    private long _startTime;
    private String _projectName;

    // the jdom document
    private Document _doc;
    private Element _activeElement;

    /**
     * Creates an ExecutionLogger instance. Gets the file path from the
     * preferences.
     *
     * @param graph Graph that is executed
     * @param projectName name of the project file
     * @param loggingEnabled the ExecutionLogger just logs and saves the file if
     * this is true
     */
    public ExecutionLogger(Graph graph, String projectName, boolean loggingEnabled) {
        _loggingEnabled = loggingEnabled;
        if (!_loggingEnabled) {
            return;
        }
        _logFilePath = readFilePath();
        _projectName = projectName;
        _startTime = System.currentTimeMillis();
        // creating the jdom document with the root Element
        _doc = new Document(new Element(ROOT));
        setHeader();
        Element executionEl = new Element(EXECUTION);
        _doc.getRootElement().addContent(executionEl);
        _activeElement = executionEl;
    }

    /**
     * Sets the header of the document.
     */
    private void setHeader() {
        String date = new Date(_startTime).toString();
        String startTime = new Time(_startTime).toString();

        // getting the root element of _doc
        Element rootEl = _doc.getRootElement();

        // creation of the header and associated Elements
        Element headerEl = new Element(SETUP);
        rootEl.addContent(headerEl);

        Element pNameEl = new Element(PROJECT);
        pNameEl.setAttribute(NAME, _projectName);
        headerEl.addContent(pNameEl);

        Element timeEl = new Element(START);
        timeEl.setAttribute(TIME, startTime);
        timeEl.setAttribute(DATE, date);
        headerEl.addContent(timeEl);

        Element varsEl = new Element(GLOBAL_VARS);
        headerEl.addContent(varsEl);
    }

    /**
     * Logs the initial states of the variables to the header of the log and
     * adds a ChangeListener for each variable to log changes in the variables.
     *
     * @param globalVars List of global variables
     */
    public void logInitialVariables(List<AbstractVariable> globalVars) {
        if (!_loggingEnabled) {
            return;
        }

        Element varsEl = _doc.getRootElement().getChild(SETUP).getChild(GLOBAL_VARS);

        logVariables(globalVars, varsEl);
    }

    /**
     * Logs changes in a variable. Gets called by the ChangeListener on a
     * variable.
     *
     * @param abstractVariable updated variable
     */
    private void logUpdatedVariable(AbstractVariable abstractVariable) {
        if (!_loggingEnabled) {
            return;
        }
        Element varEl = new Element(VAR_UPDATE);
        _activeElement.addContent(varEl);
        setVarAttributes(varEl, abstractVariable);
    }

    /**
     * Logs the execution of a Graph node with name, type, transition time, and
     * global variables.
     *
     * @param graphNode node to be logged
     */
    public void logGraphNode(GraphNode graphNode) {
        if (!_loggingEnabled) {
            return;
        }
        Element nodeEl = logNodeGetElement(graphNode);
        _activeElement = nodeEl;

        List<AbstractVariable> globalSubgraphVariables = new ArrayList<AbstractVariable>(graphNode.getAllGlobalVariables());
        if (!globalSubgraphVariables.isEmpty()) {
            Element vars = new Element(GLOBAL_VARS);
            nodeEl.addContent(vars);
            logVariables(globalSubgraphVariables, vars);
        }
    }

    /**
     * Logs the the state of each variable in vars to the parent Element.
     *
     * @param globalVars List of global variables
     * @param parentElement Element the variables are logged to
     */
    private void logVariables(List<AbstractVariable> globalVars, Element parentElement) {
        for (AbstractVariable var : globalVars) {
            Element varEl = new Element(VAR);
            parentElement.addContent(varEl);
            setVarAttributes(varEl, var);

            // add ChangeListeners for each variable
            var.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    logUpdatedVariable((AbstractVariable) e.getSource());
                }
            });
        }
    }

    /**
     * Private method that sets the attributes for a variable
     *
     * @param el variable element the attributes are added to
     * @param var the variable
     */
    private void setVarAttributes(Element el, AbstractVariable var) {
        String value = var.getValue().toString();
        Object type = var.getType();
        Boolean isGroovyOnly = var instanceof GroovyVariable;
        // String values (and sometimes other values?!?) are saved with double
        // quotes. They get removed here to prevent quadruple quotes.
        value = value.replaceAll("\\\"", "");
        el.setAttribute("is_groovy_only", isGroovyOnly.toString());
        el.setAttribute(TYPE, type.toString());
        el.setAttribute(VALUE, value);
        el.setAttribute(NAME, var.getName());
    }

    /**
     * Logs the execution of a node with name, type and transition time.
     *
     * @param node node to be logged
     */
    public void logNode(Node node) {
        if (!_loggingEnabled) {
            return;
        }
        logNodeGetElement(node);
        if (node instanceof ReturnNode) {
            _activeElement = _activeElement.getParentElement();
        }
    }

    /**
     * Logs the execution of a node with name, type and transition time.
     *
     * @param node node to be logged
     * @return jdom element of the node
     */
    private Element logNodeGetElement(Node node) {
        Element nodeEl = new Element(NODE);
        _activeElement.addContent(nodeEl);

        long tranitionTime = System.currentTimeMillis() - _startTime;
        nodeEl.setAttribute(TRANS_TIME, Long.toString(tranitionTime));
        nodeEl.setAttribute(TYPE, node.getClassName());
        nodeEl.setAttribute(NAME, node.getTitle());
        return nodeEl;
    }

    /**
     * Logs an exception.
     *
     * @param name Name of the exception
     * @param source Source of the exception
     * @param message exception message
     */
    public void logException(String name, String source, String message) {
        if (!_loggingEnabled) {
            return;
        }
        Element exceptionEl = new Element(ERROR);
        _activeElement.addContent(exceptionEl);

        long tranitionTime = System.currentTimeMillis() - _startTime;
        exceptionEl.setAttribute(TRANS_TIME, Long.toString(tranitionTime));
        exceptionEl.setAttribute(MESSAGE, message);
        exceptionEl.setAttribute(SOURCE, source);
        exceptionEl.setAttribute(NAME, name);
    }

    /**
     * Validates the document against the log schema.
     *
     * @return if XML is valid
     */
    private boolean validateXML() {
        if (!_validationEnabled) {
            return true;
        }

        // Creating the factory for RELAX NG compact
        System.setProperty(SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI,
                "com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);

        // Load the schema
        File schemaLocation = new File(XML_SCHEMA);

        boolean valid;

        // Compile the schema.
        try {
            Schema schema = factory.newSchema(schemaLocation);

            // Get a validator from the schema.
            Validator validator = schema.newValidator();
            // Check the document
            validator.validate(new JDOMSource(_doc));
            valid = true;
        } catch (SAXException e) {
            valid = false;
            // getting the initial cause to get the initial ex exception message
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            throw new LoggingException("XML is not valid because: " + cause.getMessage(), cause);
        } catch (IOException e) {
            valid = false;
            throw new LoggingException("Could not validate the XML log file. ", e);
        }
        return valid;
    }

    /**
     * Saves the jdom document as an xml file to the path in _logFilePath.
     */
    public void saveDocAsXML() {
        if (!_loggingEnabled || !validateXML()) {
            return;
        }

        String logFileName = new Date(_startTime) + "_" + new Time(_startTime) + "_" + _projectName.split("\\.")[0]
                + ".xml";

        logFileName = logFileName.replace(':', '-');
        File file = new File(new File(_logFilePath).getAbsoluteFile() + "/" + logFileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

            outputter.output(_doc, new FileOutputStream(file));
            System.out.println("Log file saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new LoggingException(Resources.getString("CouldNotCreateLogFile"), e);
        }

    }

    /**
     * Gets the file path from the Preferences
     *
     * @return file path for the log files
     */
    private String readFilePath() {
        return Preferences.getPrefs().loggingDirectory.getValue().getPath();
    }

}
