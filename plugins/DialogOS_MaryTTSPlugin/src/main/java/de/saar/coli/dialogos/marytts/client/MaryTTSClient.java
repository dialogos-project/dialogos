
package de.saar.coli.dialogos.marytts.client;

import com.clt.dialog.client.TTSClient;
import java.util.Properties;

import com.clt.event.ProgressListener;
import de.saar.coli.dialogos.marytts.MaryTTS;

/**
 * @author dabo
 * 
 */
public class MaryTTSClient extends TTSClient<MaryTTS> {

  private static final int cmdTranscribePro = 2102;


  @Override
  protected String getDescriptionImpl() {
    return null;
  }

  @Override
  protected MaryTTS createSynthesizer(ProgressListener progress, Properties properties) throws Exception {
      return new MaryTTS();
  }

  @Override
  public String getName() {
    return "MaryTTS";
  }

  @Override
  public int getPreferredPort() {
    return 3947;
  }

  @Override
  public String getVersion() {
    return "1.0";
  }
}
