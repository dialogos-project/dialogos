package com.clt.speech.recognition.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;
import com.clt.script.exp.Value;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.AbstractRecognizer;
import com.clt.speech.recognition.Domain;
import com.clt.speech.recognition.RecognitionContext;
import com.clt.speech.recognition.RecognitionResult;
import com.clt.speech.recognition.RecognizerEvent;
import com.clt.speech.recognition.RecognizerException;
import com.clt.speech.recognition.RecognizerListener;
import com.clt.speech.recognition.Utterance;
import com.clt.srgf.Grammar;
import com.clt.srgf.ParseOptions;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * @author dabo
 * 
 */
public class OptimizeParameters {

  private Map<String, Parameter> parameters;
  private Collection<File> input;
  private String domain;
  private String context;


  public OptimizeParameters() {

    this.parameters = new HashMap<String, Parameter>();
    this.input = new ArrayList<File>();
  }


  public void addParameter(Parameter p) {

    this.parameters.put(p.getName(), p);
  }


  public void addInput(File file) {

    this.input.add(file);
  }


  public Collection<Sample> run(AbstractRecognizer recognizer,
      ProgressListener progress)
        throws SpeechException {

    Parameter[] parameters = this.parameters.values().toArray(
            new Parameter[this.parameters.size()]);

    int numSamples = 1;
    for (int i = 0; i < parameters.length; i++) {
      numSamples += parameters[i].getValues().length;
    }

    ProgressEvent evt =
      new ProgressEvent(this, "Offline test", 0, numSamples, 0);
    if (progress != null) {
      progress.progressChanged(evt);
    }

    Collection<Sample> samples = new ArrayList<Sample>();
    Map<String, Integer> paramValues = new HashMap<String, Integer>();
    this.run(recognizer, parameters, 0, paramValues, samples, progress, evt);

    return samples;
  }


  private void run(AbstractRecognizer recognizer, Parameter[] parameters,
      int index,
            Map<String, Integer> paramValues, Collection<Sample> samples,
            ProgressListener progress, ProgressEvent evt)
      throws SpeechException {

    if (index < parameters.length) {
      Parameter p = parameters[index];
      int[] values = p.getValues();
      for (int i = 0; i < values.length; i++) {
        Map<String, Integer> pValues =
          new HashMap<String, Integer>(paramValues);
        pValues.put(p.getName(), new Integer(values[i]));
        this.run(recognizer, parameters, index + 1, pValues, samples, progress,
          evt);
      }
    }
    else {
      evt.setCurrent(evt.getCurrent() + 1);
      if (progress != null) {
        progress.progressChanged(evt);
      }

      Domain domain = recognizer.findDomain(this.domain, false);
      recognizer.setDomain(domain);

      try {
        RecognitionContext context = domain.findContext(this.context);
        recognizer.setContext(context);

        for (int i = 0; i < parameters.length; i++) {
          String pName = parameters[i].getName();
          Property<?> parameter = recognizer.getParameter(pName);
          if (parameter == null) {
            throw new RecognizerException("Unknown parameter \"" + pName + "\"");
          }
          ((IntegerProperty)parameter).setValue(paramValues.get(pName));
        }

        for (File input : this.input) {
          Sample s = new ParameterSample(paramValues, input);
          s.collect(recognizer);
          samples.add(s);
        }
      } catch (RecognizerException exn) {
        // unknown context
      }
    }
  }

  private class ParameterSample
        extends Sample {

    private Map<String, Integer> parameterValues;
    private File input;
    private Collection<String> warnings;
    private Collection<String> errors;
    private RecognitionResult result;
    private Value[] interpretation;


    public ParameterSample(Map<String, Integer> parameterValues, File input) {

      this.parameterValues = parameterValues;
      this.input = input;

      this.warnings = new ArrayList<String>();
      this.errors = new ArrayList<String>();
      this.result = null;
    }


    @Override
    public void collect(AbstractRecognizer recognizer) {

      RecognizerListener callback = new RecognizerListener() {

        public void recognizerStateChanged(RecognizerEvent evt) {

          switch (evt.getType()) {
            case RecognizerEvent.RECOGNIZER_WARNING:
                            ParameterSample.this.warnings.add(evt
                              .getErrorMessage());
                            break;
                        }
                      }
      };

      recognizer.addRecognizerListener(callback);
      try {
        this.result = recognizer.startOfflineRecognition(this.input);
        Grammar g = recognizer.getContext().getGrammar();
        this.interpretation = new Value[this.result.numAlternatives()];
        ParseOptions options = new ParseOptions();
        options.buildParseTree = false;
        options.allowSparseParses = false;
        options.evaluateTags = true;

        // options.dynamicVocabulary = substitutions;
        options.allowSparseParses = false;
        options.maxParses = ParseOptions.BEST_PARSE;

        for (int i = 0; i < this.result.numAlternatives(); i++) {
          this.interpretation[i] =
            g.match(this.result.getAlternative(i).getWords(), null, options);
        }
      } catch (Exception exn) {
        this.errors.add(exn.toString());
      }
      recognizer.removeRecognizerListener(callback);

    }


    @Override
    public void print(XMLWriter out) {

      out.openElement("sample");
      out.printElement("domain", OptimizeParameters.this.domain);
      out.printElement("context", OptimizeParameters.this.context);
      for (String pName : this.parameterValues.keySet()) {
        out.printElement("parameter", new String[] { "name" },
          new String[] { pName },
                    this.parameterValues.get(pName));
      }
      out.printElement("input", this.input.toString());

      out.openElement("result", new String[] { "alternatives" },
                new String[] { String.valueOf(this.result.numAlternatives()) });
      for (int i = 0; i < this.result.numAlternatives(); i++) {
        Utterance utt = this.result.getAlternative(i);
        out.openElement("alternative", new String[] { "confidence" },
                    new String[] { String.valueOf(utt.getConfidence()) });
        out.printElement("value", this.interpretation[i]);
        for (int j = 0; j < utt.length(); j++) {
          com.clt.speech.recognition.Word word = utt.getWord(j);
          if ((word.getStart() != 0) && (word.getEnd() != 0)) {
            out.printElement("word", new String[] { "start", "end",
              "confidence" },
                          new String[] { String.valueOf(word.getStart()),
                                  String.valueOf(word.getEnd()),
                                  String.valueOf(word.getConfidence()) }, word
                            .getWord());
          }
          else {
            out
              .printElement(
                "word",
                new String[] { "confidence" },
                          new String[] { String.valueOf(word.getConfidence()) },
                word.getWord());
          }
          out.closeElement("alternative");
        }
      }
      out.closeElement("result");

      out.closeElement("sample");
    }
  }


  public static OptimizeParameters load(File f)
      throws IOException {

    final OptimizeParameters test = new OptimizeParameters();

    final XMLReader r = new XMLReader(false);
    r.parse(f, new AbstractHandler() {

      String paramName;
      Values values;


      @Override
      protected void start(String name, Attributes atts)
            {

              if (name.equals("domain")) {
                test.domain = atts.getValue("name");
              }
              else if (name.equals("context")) {
                test.context = atts.getValue("name");
              }
              else if (name.equals("parameter")) {
                this.paramName = atts.getValue("name");
              }
              else if (name.equals("range")) {
                int min = Integer.parseInt(atts.getValue("min"));
                int max = Integer.parseInt(atts.getValue("max"));

                if (atts.getValue("step") != null) {
                  this.values =
                    new Range(min, max, Integer.parseInt(atts.getValue("step")));
                }
                else {
                  this.values = new Range(min, max);
                }
              }
              else if (name.equals("constant")) {
                this.values =
                  new Constant(Integer.parseInt(atts.getValue("value")));
              }
              else if (name.equals("fileset")) {
                final File dir = new File(atts.getValue("dir"));
                r.setHandler(new AbstractHandler(name) {

                  @Override
                  protected void start(String name, Attributes atts) {

                    if (name.equals("include")) {
                      String pattern = atts.getValue("name");

                      if (pattern.startsWith("*")) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                          for (int i = 0; i < files.length; i++) {
                            if (files[i].isFile()
                                                    && files[i].getName()
                                                      .endsWith(
                                                        pattern.substring(1))) {
                              test.addInput(files[i]);
                            }
                          }
                        }
                      }
                      else {
                        test.addInput(new File(dir, pattern));
                      }
                    }
                  }
                });
              }
            }


      @Override
      protected void end(String name)
            {

              if (name.equals("parameter")) {
                test.addParameter(new Parameter(this.paramName, this.values));
              }
            }
    });

    return test;
  }
}
