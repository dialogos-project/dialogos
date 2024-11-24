
package de.saar.coli.dialogos.marytts;

import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.Property;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.AbstractSynthesizer;
import com.clt.speech.tts.Voice;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.sound.sampled.AudioInputStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * MaryTTS class contains as field the class called MaryInterface.
 * Through this class we communicate with the Mary text-to-speech platform.
 * This class contains a Document which is (or should always be) a
 * mary-xml compatible document that is sent to the MaryInterface to be evaluated.
 *
 * @author Nicolas and Phil (taking over the Realspeak class of the old Realspeak Plugin by Daniel Bobbert)
 * 
 */
public class MaryTTS
    extends AbstractSynthesizer {

  public static final int MIN_PITCH = 0;
  public static final int MAX_PITCH = 100;
  public static final int DEFAULT_PITCH = 50;
  public static final int MIN_SPEED = 0;
  public static final int MAX_SPEED = 100;
  public static final int DEFAULT_SPEED = 50;
  //volume: silent soft medium loud default
  public static final String MIN_VOLUME = "silent";
  public static final String MAX_VOLUME = "loud";
  public static final String DEFAULT_VOLUME = "default";

  private MaryInterface mary;
  private DefaultEnumProperty<MaryTTSVoice> voice;
  private AudioPlayer audioPlayer;
  private Document maryXML;

  public MaryTTS() {
    try {
      this.mary = new LocalMaryInterface();
    } catch (MaryConfigurationException e) {
      e.printStackTrace();
    }
    initProperties();
  }

  /* initProperties: */
  private void initProperties() {
    Collection<MaryTTSVoice> voices = getMaryVoices();
    MaryTTSVoice[] voicesArray = getMaryVoices().toArray(new MaryTTSVoice[voices.size()]);

    this.voice = new DefaultEnumProperty<MaryTTSVoice>("voice", "Voice", null, voicesArray) {

      @Override
      public void setValueFromString(String name) {
        try {
          super.setValueFromString(name);
        } catch (RuntimeException exn) {
          // try to find the voice by name
          for (MaryTTSVoice v : this.getPossibleValues()) {
            if (v.getName().equals(name)) {
              this.setValue(v);
              return;
            }
          }

          System.err
              .println("ERROR: Attempt to activate unknown voice '"
                  + name + "'");
          throw exn;
        }
      }
    };
    //Important setting for generating Audio. Always through maryXML.
    mary.setInputType("RAWMARYXML");
    String xmlStr = this.getMaryTemplateStrXML("", DEFAULT_VOLUME, DEFAULT_PITCH, DEFAULT_SPEED);
    setMaryXML(xmlStr);
  }

  /*
  * getMaryTemplateStrXML: Given a text to be spoken, a volume, pitch and speed,
  * returns a valid maryXML in String.
  * */
  private String getMaryTemplateStrXML(String text, String volume, int pitch, int speed) {

    String loc = mary.getLocale().toString();
    String xmlStr =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<maryxml version=\"0.4\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xmlns=\"http://mary.dfki.de/2002/MaryXML\"\n" +
            "xml:lang=\"" + loc + "\">\n" +
            "<prosody " +
            "rate=\"" + speed  + "%\" " + //\"+20%\" " +
            "pitch=\"" + pitch + "\" " +
            "range=\"-10%\" " +
            "volume=\"" + volume +"\">\n" +
            text +//Insert user text here!
            "</prosody>\n" +
            "</maryxml>";
    return xmlStr;
  }

  /*
  * resetMaryXML: Resets maryXML configs to default.
  * */
  public void resetMaryXML(){
    String xmlStr = getMaryTemplateStrXML("",DEFAULT_VOLUME,DEFAULT_PITCH,DEFAULT_SPEED);
    setMaryXML(xmlStr);
  }

  /*
  * Sets maryXML variable.
  * */
  private void setMaryXML(String str){
    this.maryXML = XMLHelper.str2Document(str, "MaryXML coult not be generated!");
  }

  /*
  * createMaryXMLStr: Returns String representing the XML file with
  * speakText (text to be spoken) appropiately inserted.
  * Modifies maryXML (inserts text to be spoken)
  * */
  public String createMaryXMLStr(String speakText){
    setUserText2MaryXML(speakText);
    return XMLHelper.xml2str(maryXML);
  }

  @Override
  public MaryTTSVoice findVoice(Language language) {
    return findVoice(language.getLocale());
  }

  @Override
  public MaryTTSVoice findVoice(Locale locale) {
    for (String voiceName : mary.getAvailableVoices(locale)) {
      return new MaryTTSVoice(voiceName, new Language(locale, null));
    }
    return null;
  }

  @Override
  public MaryTTSVoice findVoice(String name) {
    for (MaryTTSVoice v : this.voice.getPossibleValues()) {
      if (v.getName().equals(name) || v.toString().equals(name)) {
        return v;
      }
    }
    return null;
  }


  @Override
  public Property<?>[] getProperties() {
    return new Property[] {voice};
  }

  @Override
  public MaryTTSVoice[] getAvailableVoices() {
    return this.voice.getPossibleValues();
  }

  /*
  * setProsody2MaryXML: Inserts the volume, pitch and the speed into
  * the Document maryXML.
  * TODO: volume is not working.
  * */
  public void setProsody2MaryXML(String volume, int pitch, int speed) {

    NodeList prosody = maryXML.getElementsByTagName("prosody");
    Element p;
    //loop for each prosody tag (could there be more than one?)
    for(int i=0; i<prosody.getLength(); i++){
      p = (Element) prosody.item(i);
      p.setAttribute("pitch", "+" + Integer.toString(pitch) + "%");
      p.setAttribute("volume", volume); //TODO
      p.setAttribute("rate", speed + "%");
    }
  }

  /*
  * setVoice2MaryXML: Inserts the locale into the
  * xml Document (maryXML).
  * */
  private void setVoice2MaryXML(){
    String loc = mary.getLocale().toString();
    NodeList maryXMLTag = maryXML.getElementsByTagName("maryxml");
    Element l;
    //loop for each maryXMLTag tag (could there be more than one?)
    for(int i=0; i<maryXMLTag.getLength(); i++){
      l = (Element) maryXMLTag.item(i);
      l.setAttribute("xml:lang", loc);
    }
  }

  /*
  * getMaryVoices: Returns the available voices of all locales.
  * */
  private Collection<MaryTTSVoice> getMaryVoices() {
    Collection<MaryTTSVoice> voices = new ArrayList<MaryTTSVoice>();
    for (Locale locale : mary.getAvailableLocales()) {
      for (String voiceName : mary.getAvailableVoices(locale)) {
        voices.add(new MaryTTSVoice(voiceName, new Language(locale, null)));
      }
    }
    return voices;
  }

  /*
  * speak: Synthesizes given String. Further audioplayers won't be
  * able to reproduce audio until the first one is finished.
  * Modifies the Document maryXML (inserting the text to be spoken).*/
  public void speak(String text) throws SpeechException{
    speak(text,true);
  }

  /*
  * speak: Like speak(String text). Difference: If waitUntilDone is
  * true then it will lock the resources, ie: further audioplayers
  * won't be able to reproduce audio until the first one is finished.
  * */
  public void speak(String text, boolean waitUntilDone)
      throws SpeechException {
    this.setUserText2MaryXML(text);
    speakMaryXML(this.maryXML, waitUntilDone);
  }

  /*
  * speakMaryXML: Synthesizes given XML. If waitUntilDone is true then
  * it will lock the resources, ie: further audioplayers won't be
  * able to reproduce audio until the first one is finished.
  * */
  private void speakMaryXML(Document xml, boolean waitUntilDone) {
    try {
      audioPlayer = new AudioPlayer();
      AudioInputStream audioIS = mary.generateAudio(xml);
      audioPlayer.setAudio(audioIS);
      audioPlayer.start();
      if (waitUntilDone){
        awaitEndOfSpeech();
      }
    } catch (SynthesisException e) {
      e.printStackTrace();
    }
  }

  public double estimateDurationToSay(String text) {
    try {
      this.setUserText2MaryXML(text);
      AudioInputStream audioIS = mary.generateAudio(this.maryXML);
      return ((double) audioIS.getFrameLength()) / audioIS.getFormat().getFrameRate();
    } catch (SynthesisException e) {
      e.printStackTrace();
    }
    return 0f;
  }

  public void awaitEndOfSpeech() {
    if (audioPlayer != null) {
      try {
        audioPlayer.join();
      } catch (InterruptedException e) {
        //Something in audioPlayer.join() went wrong.
        e.printStackTrace();
      }
    }
  }

  /*
  * setUserText2MaryXML: Inserts the text (that is meant to be spoken)
  * appropiately into the (mary) xml Document maryXML.
  * */
  private void setUserText2MaryXML(String text) {
    NodeList prosody =  this.maryXML.getElementsByTagName("prosody");

    Element p;
    //loop for each prosody tag (TODO : Necessary?)
    for(int i=0; i < prosody.getLength(); i++){
      p = (Element) prosody.item(i);
      //Delete previous (text) children nodes (if there were any. There should always be just one)
      for (int j=0; j < p.getChildNodes().getLength(); j++)
      {
          p.removeChild(p.getFirstChild());
      }
      p.appendChild(this.maryXML.createTextNode(text));
    }
  }

  @Override
  synchronized public void stop() {
    if (audioPlayer != null)
      audioPlayer.cancel();
  }

  @Override
  public void dispose() {
    // nothing to be done
  }

  @Override
  public MaryTTSVoice getVoice() {
    return voice.getValue();
  }

  @Override
  public void setVoice(Voice voice) {
    if (!(voice instanceof MaryTTSVoice)) {
      throw new IllegalArgumentException("Unsupported type of voice");
    }
    this.voice.setValue((MaryTTSVoice) voice);
    this.mary.setVoice(voice.getName());
    //The value of the locale is updated in mary, but since we're managing
    //our own maryxml Document, we have to update it, too.
    setVoice2MaryXML();
  }

  /*
  * speakStrMaryXML: Synthesizes String which is XML and possibly
  * waits for the synthesis to finish before generating new audio again.
  *
  * TODO: If string is not a valid xml DialogOS will throw a somewhat cryptic error Dialog.
  * */
  public void speakStrMaryXML(String str, boolean wait) {
    speakMaryXML(XMLHelper.str2Document(str, "MaryXML could not be generated."), wait);
  }

  @Override
  public void synthesizeToFile(String text, File f) {
    throw new UnsupportedOperationException("synthesis to file is not yet supported");
  }

  @Override
  public Language[] getLanguages() {
    return new Language[0];
  }

  @Override
  public String[] transcribe(String word, Language language) {
    return new String[0];
  }

}