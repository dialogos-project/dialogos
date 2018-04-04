package com.clt.audio;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;

/**
 * Level meter for PCM encoded audio data
 */
public class LevelMeter extends JComponent {

    private static final int CHANNEL_SPACING = 1;

    private int levels[];
    private int noise[];
    private int maxLevels[];
    private long timestamps[];

    private OutputStream stream;

    private boolean bigEndian;
    private boolean signed;
    private int bytesPerSample;
    private int numChannels;
    private int samplesPerSecond;

    private Color[] colors = new Color[0];

    private int decay = 20;

    public LevelMeter() {
        this.numChannels = 0;
        this.stream = null;
    }

    public LevelMeter(AudioFormat format) throws UnsupportedAudioFileException {
        this();
        this.setAudioFormat(format);
    }

    public void setAudioFormat(AudioFormat format) throws UnsupportedAudioFileException {
        if (!((format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) || (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED))) {
            throw new UnsupportedAudioFileException(
                    "LevelMeter only supports PCM encoded sound");
        }

        if (!((format.getSampleSizeInBits() == 8)
                || (format.getSampleSizeInBits() == 16) || (format.getSampleSizeInBits() == 24))) {
            throw new UnsupportedAudioFileException("LevelMeter only supports 8, 16 and 24 bit sound");
        }

        this.bigEndian = format.isBigEndian();
        this.signed = format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;
        this.bytesPerSample = (format.getSampleSizeInBits() - 1) / 8 + 1;
        this.numChannels = format.getChannels();
        this.samplesPerSecond = Math.round(format.getSampleRate());

        this.levels = new int[this.numChannels];
        this.maxLevels = new int[this.numChannels];
        this.noise = new int[this.numChannels];
        this.timestamps = new long[this.numChannels];
        for (int i = 0; i < this.numChannels; i++) {
            this.levels[i] = 0;
            this.maxLevels[i] = 0;
            this.noise[i] = 0;
            this.timestamps[i] = 0;
        }

        this.stream = new OutputStream() {

            @Override
            public void write(int data) {
                throw new UnsupportedOperationException("use write(byte[]) instead");
            }

            @Override
            public void write(byte[] data) {
                this.write(data, 0, data.length);
            }

            @Override
            public void write(byte[] data, int start, int length) {
                int min[] = new int[LevelMeter.this.numChannels];
                int max[] = new int[LevelMeter.this.numChannels];

                if (length
                        % (LevelMeter.this.numChannels * LevelMeter.this.bytesPerSample) != 0) {
                    throw new IllegalArgumentException(
                            "Bytes must be a multiple of the number of channels and bytes per sample");
                }

                for (int i = 0; i < LevelMeter.this.numChannels; i++) {
                    min[i] = 256;
                    max[i] = 0;
                }
                int channel = 0;
                for (int j = start; j <= length - LevelMeter.this.bytesPerSample; j
                        += LevelMeter.this.bytesPerSample) {
                    int hibyte
                            = LevelMeter.this.bigEndian ? data[j] : data[j
                                    + LevelMeter.this.bytesPerSample - 1];

                    if (LevelMeter.this.signed) {
                        hibyte = hibyte + 128;
                    } else if (hibyte < 0) {
                        hibyte = hibyte + 256;
                    }

                    if (hibyte < min[channel]) {
                        min[channel] = hibyte;
                    }
                    if (hibyte > max[channel]) {
                        max[channel] = hibyte;
                    }

                    channel = (channel + 1) % LevelMeter.this.numChannels;
                }

                for (int i = 0; i < LevelMeter.this.numChannels; i++) {
                    max[i] = max[i] - min[i];
                }
                LevelMeter.this.setLevels(max, length
                        / (LevelMeter.this.numChannels * LevelMeter.this.bytesPerSample));
            }
        };
    }

    public void processDoubleData(double data[]) {
        assert this.numChannels == 1 : "I only deal with single-channel data";
        assert this.signed : "I only deal with signed data";
        assert this.bytesPerSample == 2 : "I (deliberately) only deal with 16bit samples";
        double max = -1;
        for (double d : data) {
            max = Math.max(max, Math.abs(d));
        }
        // this is a logarithmic scale (log(2^15)*56.6 = 255, the maximum value setLevels likes)
        double dbLevel = -20 * Math.log10(max/32767);
        int[] maxValue = new int[] { (int)(Math.log10(max) * 56.6)};
        System.err.println(max + " " + dbLevel + " " + maxValue[0]);
        // this would be linear:
        //int[] maxValue = new int[] { (int)(max / 128 + 0.5) };
        setLevels(maxValue, data.length);
    }

    private int getNumBlocks() {
        int numBlocks;
        if (this.getBorder() == null) {
            numBlocks = this.getWidth() / 2;
        } else {
            Insets insets = this.getBorder().getBorderInsets(this);
            numBlocks = (this.getWidth() - insets.left - insets.right) / 2;
        }

        if (numBlocks != this.colors.length) {
            this.colors = new Color[numBlocks];
            for (int i = 0; i < numBlocks; i++) {
                if (i >= numBlocks * 0.93) {
                    this.colors[i] = Color.red;
                } else if (i >= numBlocks * 0.5) {
                    this.colors[i] = Color.yellow;
                } else {
                    this.colors[i] = Color.green;
                }
            }
        }

        return numBlocks;
    }

    public OutputStream getStream() {
        return this.stream;
    }

    public void setLevels(int[] levels, int numSamples) {
        this.setLevels(levels, null, numSamples);
    }

    public void setLevels(int[] levels, int[] noise, int numSamples) {
        if (this.numChannels == 0) {
            throw new IllegalStateException("LevelMeter not initialized");
        }

        if ((noise != null) && (noise.length != levels.length)) {
            throw new IllegalArgumentException(
                    "Level and noise arrays must have the same size");
        }

        if (levels.length != this.numChannels) {
            throw new IllegalArgumentException(
                    "Number of levels must match number of channels");
        }

        long time = System.currentTimeMillis();

        // "numSamples" samples are equivalent to "millis" milliseconds
        int millis = 1000 * numSamples / this.samplesPerSecond;

        // level portato fallend in <code>decay</code> Millisekunden
        int factor = this.samplesPerSecond * this.decay / 1000;
        if (factor > 0) {
            factor /= numSamples;
        }

        for (int i = 0; i < this.numChannels; i++) {
            if ((levels[i] < 0) || (levels[i] >= 256)) {
                throw new IllegalArgumentException("Illegal level value: " + levels[i]
                        + ". Must be >=0 and <=255");
            }

            if ((levels[i] >= this.levels[i]) || (this.decay == 0)) {
                this.levels[i] = levels[i];
            } else {
                this.levels[i] = (this.levels[i] * factor + levels[i]) / (factor + 1);
            }

            if (noise != null) {
                if ((noise[i] >= this.noise[i]) || (this.decay == 0)) {
                    this.noise[i] = noise[i];
                } else {
                    this.noise[i] = (this.noise[i] * factor + levels[i]) / (factor + 1);
                }
            } else {
                this.noise[i] = 0;
            }

            // peak decay
            if (this.levels[i] > this.maxLevels[i]) {
                this.maxLevels[i] = levels[i];
                // make the maximum level stay for 2000ms
                this.timestamps[i] = time + 2000;
            } else if (time > this.timestamps[i] + millis) {
                // then let the maximum level decay in 500ms
                this.maxLevels[i] -= Math.min(256 * millis / 500, this.maxLevels[i]);
                this.timestamps[i] = time;
            }
        }

        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int height = this.getHeight();

        Insets insets
                = this.getBorder() != null ? this.getBorder().getBorderInsets(this)
                : new Insets(0, 0,
                        0, 0);

        int numBlocks = this.getNumBlocks();
        int channelheight;
        if (this.numChannels == 0) {
            channelheight = height - insets.top - insets.bottom;
        } else {
            channelheight
                    = (height - insets.top - insets.bottom - (this.numChannels - 1)
                    * LevelMeter.CHANNEL_SPACING)
                    / this.numChannels;
        }

        Color offColor = new Color(64, 32, 32);

        Rectangle clip = g.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        }

        if (!this.isEnabled()) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
            return;
        }

        g.setColor(Color.black);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        for (int n = 0; n < this.numChannels; n++) {
            int y = n * (channelheight + LevelMeter.CHANNEL_SPACING) + insets.top;
            if ((y < clip.y + clip.height) && (y + channelheight >= clip.y)) {
                int level = this.levels[n] * numBlocks / 256;
                int noise = this.noise[n] * numBlocks / 256;
                int maxLevel = this.maxLevels[n] * numBlocks / 256;

                for (int i = 0; i < numBlocks; i++) {
                    int x = 2 * i + insets.left;
                    if ((x >= clip.x) && (x <= clip.x + clip.width)) {
                        Color c = this.colors[i];

                        if (((i > level) || (level == 0)) && (i != maxLevel)) {
                            c = offColor;
                        } else {
                            if (i <= noise) {
                                c = c.darker().darker();
                            }
                        }
                        g.setColor(c);
                        g.drawLine(x, y + 1, x, y + channelheight - 1);
                    }
                }
            }
        }
    }

    public int getNumChannels() {
        return this.numChannels;
    }

    public void setDecay(int decay) {
        this.decay = decay;
    }

    @Override
    public Dimension getPreferredSize() {
        /*
     * Dimension d = new Dimension(NUMBLOCKS*(BLOCKSIZE + SPACING) - SPACING,
     * numChannels * (16 + CHANNEL_SPACING) - CHANNEL_SPACING); if (getBorder()
     * != null) { Insets i = getBorder().getBorderInsets(this); d.width +=
     * i.left + i.right; d.height += i.top + i.bottom; } return d;
         */
        return new Dimension(200, 20);
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
        // return new Dimension(26*5 + 25*SPACING, 16);
    }
}
