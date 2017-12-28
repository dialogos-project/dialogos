package com.clt.audio;

import java.io.File;

import javax.sound.sampled.AudioFormat;

import com.clt.gui.GUI;
import com.clt.util.StringTools;

public class JPortAudio {

  private static boolean inited;

  private int numDevices = 0;


  public JPortAudio()
      throws InstantiationException {

    /*
     * Unter Window JRE 1.4.0 muss init() unbedingt vom Event-Thread aus
     * aufgerufen werden! Sonst stuerzt die JVM beim beenden des Threads, der
     * init() aufgerufen hat, total ab. Vermutlich haengt das mit
     * Variableninitialisierungen in der DLL zusammen?! Welcher Thread die
     * anderen Methoden aufruft, scheint egal zu sein.
     */
    try {
      GUI.invokeAndWaitCanThrow(new Runnable() {

        public void run() {

          try {
            if (!JPortAudio.inited) {
              System.loadLibrary("JPortAudio");
              JPortAudio.inited = true;
            }
          }
                    catch (ThreadDeath d) {
                      throw d;
                    }
                    catch (Throwable t) {
                      throw new RuntimeException();
                    }

                    int err = JPortAudio.this.init();
                    if (err != 0) {
                      throw new RuntimeException(JPortAudio.this
                        .getErrorDescription(err));
                    }

                    JPortAudio.this.numDevices =
                      JPortAudio.this.getNumDevices();
                  }
      });
    } catch (ThreadDeath d) {
      throw d;
    } catch (Throwable t) {
      if (StringTools.isEmpty(t.getLocalizedMessage())) {
        throw new InstantiationException("JPortAudio "
          + Audio.getString("notInitialized"));
      }
      else {
        throw new InstantiationException(t.getLocalizedMessage());
      }
    }
  }


  @Override
  protected void finalize()
      throws Throwable {

    this.exit();
    super.finalize();
  }


  private native int init();


  private native int exit();


  private native int getNumDevices();


  private native String getDeviceName(int index);


  private native int getDeviceInputs(int index);


  private native int getDeviceOutputs(int index);


  private native int getNumSampleRates(int index);


  private native double[] getSampleRates(int index);


  private native int startRecording(int index, int numChannels, int sampleSize,
            double sampleRate, String file);


  private native int stopRecording(int index);


  @SuppressWarnings("unused")
  private native int startPlayback(int index, int numChannels, int sampleSize,
      double sampleRate,
            String file);


  @SuppressWarnings("unused")
  private native int stopPlayback(int index);


  @SuppressWarnings("unused")
  private native boolean isRecording(int index);


  @SuppressWarnings("unused")
  private native boolean isPlaying(int index);


  private native String getErrorDescription(int error_id);


  @SuppressWarnings("unused")
  private native double getCPULoad(int index);


  public int numDevices() {

    return this.numDevices;
  }


  public DeviceInfo getDeviceInfo(int index) {

    return new DeviceInfo(this, index);
  }


  public void startRecording(final Integer device, final AudioFormat settings,
      final File file)
        throws AudioException {

    try {
      GUI.invokeAndWaitCanThrow(new Runnable() {

        public void run() {

          int result =
            JPortAudio.this.startRecording(device.intValue(), settings
              .getChannels(),
                        settings.getSampleSizeInBits(), settings
                          .getSampleRate(),
                        file.getAbsolutePath());
          if (result != 0) {
            throw new RuntimeException(JPortAudio.this
              .getErrorDescription(result));
          }
        }
      });
    } catch (ThreadDeath d) {
      throw d;
    } catch (Throwable t) {
      throw new AudioException(t.getLocalizedMessage());
    }
  }


  public void stopRecording(final Integer device)
      throws AudioException {

    try {
      GUI.invokeAndWaitCanThrow(new Runnable() {

        public void run() {

          int result = JPortAudio.this.stopRecording(device.intValue());
          if (result != 0) {
            throw new RuntimeException(JPortAudio.this
              .getErrorDescription(result));
          }
        }
      });
    } catch (ThreadDeath d) {
      throw d;
    } catch (Throwable t) {
      throw new AudioException(t.getLocalizedMessage());
    }
  }

  public static class AudioException
        extends Exception {

    public AudioException(String s) {

      super(s);
    }
  }

  public static class DeviceInfo {

    private String name;
    private int inputChannels;
    private int outputChannels;
    private boolean supportsSampleRange;
    private double[] sampleRates;


    public DeviceInfo(final JPortAudio pa, final int index)
        throws InstantiationError {

      try {
        GUI.invokeAndWaitCanThrow(new Runnable() {

          public void run() {

            DeviceInfo.this.name = pa.getDeviceName(index);
            DeviceInfo.this.inputChannels = pa.getDeviceInputs(index);
            DeviceInfo.this.outputChannels = pa.getDeviceOutputs(index);
            DeviceInfo.this.supportsSampleRange =
              pa.getNumSampleRates(index) == -1;
            DeviceInfo.this.sampleRates = pa.getSampleRates(index);
          }
        });
      } catch (ThreadDeath d) {
        throw d;
      } catch (Throwable t) {
        throw new InstantiationError(t.getLocalizedMessage());
      }
    }


    public String getName() {

      return this.name;
    }


    public int getInputs() {

      return this.inputChannels;
    }


    public int getOutputs() {

      return this.outputChannels;
    }


    public double[] getSampleRates() {

      if (this.supportsSampleRange) {
        double[] srs = { 11025.0, 16000.0, 22050.0, 32000.0, 44100.0 };
        int start = 0;
        int end = srs.length - 1;
        while ((start < srs.length) && (srs[start] < this.sampleRates[0])) {
          start++;
        }
        while ((end >= 0) && (srs[end] > this.sampleRates[1])) {
          end--;
        }
        if (start > end) {
          return new double[0];
        }
        else {
          double[] d = new double[end - start + 1];
          System.arraycopy(srs, start, d, 0, end - start + 1);
          return d;
        }
      }
      else {
        double[] d = new double[this.sampleRates.length];
        System.arraycopy(this.sampleRates, 0, d, 0, this.sampleRates.length);
        return d;
      }
    }

  }
}