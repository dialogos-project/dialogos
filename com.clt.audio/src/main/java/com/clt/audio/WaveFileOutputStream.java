package com.clt.audio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

public class WaveFileOutputStream extends OutputStream {

  RandomAccessFile out;
  long totalSizePointer;
  long dataSizePointer;
  int dataBytes;
  int sampleSize;
  int frameSize;
  boolean swapBytes;

  byte buffer[] = new byte[65536];
  int bufferpos = 0;


  public WaveFileOutputStream(File f, AudioFormat format)
      throws IOException {

    if ((format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
      && (format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED)) {
      throw new IllegalArgumentException(
        "WaveFileOutputStream only supports PCM sound");
    }

    this.out = new RandomAccessFile(f, "rw");

    this.out.setLength(0);

    // write header
    this.writeType("RIFF");
    this.totalSizePointer = this.out.getFilePointer();
    this.writeInt32(0);
    this.writeType("WAVE");

    this.sampleSize = ((format.getSampleSizeInBits() - 1) / 8) + 1;
    this.frameSize = this.sampleSize * format.getChannels();
    if (this.frameSize != format.getFrameSize()) {
      this.swapBytes = format.isBigEndian() && (this.sampleSize > 1);
    }

    // write format chunk
    this.writeType("fmt ");
    this.writeInt32(16); // fmt data length
    this.writeInt16(1); // PCM encoding
    this.writeInt16(format.getChannels());
    this.writeInt32((int)format.getSampleRate());
    this.writeInt32((int)(this.frameSize * format.getSampleRate())); // bytes/second
    this.writeInt16(this.frameSize);
    this.writeInt16(format.getSampleSizeInBits());

    // write data chunk header
    this.writeType("data");
    this.dataSizePointer = this.out.getFilePointer();
    this.writeInt32(0);
    this.dataBytes = 0;
  }


  @Override
  public synchronized void write(int data)
      throws IOException {

    this.buffer[this.bufferpos++] = (byte)data;
    if (this.bufferpos == this.buffer.length) {
      this.flush();
    }
  }


  @Override
  public synchronized void write(byte[] data)
      throws IOException {

    this.write(data, 0, data.length);
  }


  @Override
  public synchronized void write(byte[] data, int start, int length)
      throws IOException {

    while (length >= this.buffer.length - this.bufferpos) {
      int numBytes = this.buffer.length - this.bufferpos;
      System.arraycopy(data, start, this.buffer, this.bufferpos, numBytes);
      this.bufferpos += numBytes;
      this.flush();
      start += numBytes;
      length -= numBytes;
    }

    if (length > 0) {
      System.arraycopy(data, start, this.buffer, this.bufferpos, length);
      this.bufferpos += length;
    }
  }


  @Override
  public synchronized void flush()
      throws IOException {

    if (this.bufferpos > this.frameSize) {
      int end = this.bufferpos - this.bufferpos % this.frameSize;

      if (this.swapBytes) {
        for (int base = 0; base < end; base += this.sampleSize) {
          for (int j = this.sampleSize / 2 - 1; j >= 0; j--) {
            byte tmp = this.buffer[base + j];
            this.buffer[base + j] = this.buffer[base + this.sampleSize - 1 - j];
            this.buffer[base + this.sampleSize - 1 - j] = tmp;
          }
        }
      }

      this.out.write(this.buffer, 0, end);
      this.dataBytes += end;
      if (end < this.bufferpos) {
        System
          .arraycopy(this.buffer, end, this.buffer, 0, this.bufferpos - end);
      }
      this.bufferpos = this.bufferpos - end;
    }
  }


  @Override
  public synchronized void close()
      throws IOException {

    this.flush();

    this.out.seek(this.totalSizePointer);
    this.writeInt32(this.dataBytes + 36);

    this.out.seek(this.dataSizePointer);
    this.writeInt32(this.dataBytes);

    this.out.close();
  }


  private void writeType(String s)
      throws IOException {

    if (s.length() != 4) {
      throw new IllegalArgumentException();
    }
    this.out.writeBytes(s);
  }


  // output routines, that write in little endian format
  private void writeInt16(int v)
      throws IOException {

    this.out.write((v >>> 0) & 0xFF);
    this.out.write((v >>> 8) & 0xFF);
  }


  private void writeInt32(int v)
      throws IOException {

    this.out.write((v >>> 0) & 0xFF);
    this.out.write((v >>> 8) & 0xFF);
    this.out.write((v >>> 16) & 0xFF);
    this.out.write((v >>> 24) & 0xFF);
  }
}