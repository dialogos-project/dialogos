package com.clt.speech.recognition.test;

import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 *
 */
public abstract class Sample {
    public abstract void collect(AbstractRecognizer recognizer);
    
    public abstract void print(XMLWriter out);
}
