package com.clt.audio;

public class Settings {
    public int device;
    public int numChannels;
    public int sampleSizeInBits;
    public double sampleRate;

    public Settings(int device, int numChannels, int sampleSizeInBits, double sampleRate) {
        this.device = device;
        this.numChannels = numChannels;
        this.sampleSizeInBits = sampleSizeInBits;
        this.sampleRate = sampleRate;
    }
}
