package com.clt.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;

public class RawToWave {

  // output routines for little endian
  private static void writeInt16(OutputStream out, int v)
      throws IOException {

    out.write((v >>> 0) & 0xFF);
    out.write((v >>> 8) & 0xFF);
  }


  private static void writeInt32(OutputStream out, int v)
      throws IOException {

    out.write((v >>> 0) & 0xFF);
    out.write((v >>> 8) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>> 24) & 0xFF);
  }


  private static void writeType(DataOutputStream out, String s)
      throws IOException {

    if (s.length() != 4) {
      throw new IllegalArgumentException();
    }
    out.writeBytes(s);
  }


  public static void split(File file, AudioFormat settings, ProgressListener l)
      throws IOException {

    DataOutputStream out[] = new DataOutputStream[settings.getChannels()];
    int i;

    File parent = new File(file.getParent());
    String name = file.getName();
    if (name.endsWith(".raw")) {
      name = name.substring(0, name.length() - 4);
    }
    for (i = 0; i < settings.getChannels(); i++) {
      out[i] =
        new DataOutputStream(new BufferedOutputStream(
              new FileOutputStream(new File(parent, name + "-#" + (i + 1)
                + ".wav"))));
    }

    InputStream in = new BufferedInputStream(new FileInputStream(file));
    int bytesPerFile = in.available() / settings.getChannels();
    int sampleSize = (settings.getSampleSizeInBits() - 1) / 8 + 1;

    for (i = 0; i < settings.getChannels(); i++) {
      RawToWave.writeType(out[i], "RIFF");
      RawToWave.writeInt32(out[i], bytesPerFile + 36);
      RawToWave.writeType(out[i], "WAVE");
    }

    // format block. 24 bytes.
    for (i = 0; i < settings.getChannels(); i++) {
      RawToWave.writeType(out[i], "fmt ");
      RawToWave.writeInt32(out[i], 16); // fmt data length
      RawToWave.writeInt16(out[i], 1); // PCM encoding
      RawToWave.writeInt16(out[i], 1); // 1 channel
      RawToWave.writeInt32(out[i], (int)settings.getSampleRate());
      RawToWave
        .writeInt32(out[i], (int)(sampleSize * settings.getSampleRate())); // bytes/second
      RawToWave.writeInt16(out[i], sampleSize);
      RawToWave.writeInt16(out[i], settings.getSampleSizeInBits());
    }

    for (i = 0; i < settings.getChannels(); i++) {
      RawToWave.writeType(out[i], "data");
      RawToWave.writeInt32(out[i], bytesPerFile);
    }

    int samplesPerFile = bytesPerFile / sampleSize;
    byte[] b = new byte[sampleSize];

    ProgressEvent e =
      new ProgressEvent(file, Audio.getString("ConvertingAudio") + "...", 0,
        samplesPerFile, 0);
    if (l != null) {
      l.progressChanged(e);
    }

    for (int k = 0; k < samplesPerFile; k++) {
      for (i = 0; i < settings.getChannels(); i++) {
        in.read(b);
        out[i].write(b);
      }
      if (k % 100 == 0) {
        e.setCurrent(k);
        if (l != null) {
          l.progressChanged(e);
        }
      }
    }

    e.setCurrent(samplesPerFile);
    if (l != null) {
      l.progressChanged(e);
    }

    for (i = 0; i < settings.getChannels(); i++) {
      out[i].close();
    }
  }

}