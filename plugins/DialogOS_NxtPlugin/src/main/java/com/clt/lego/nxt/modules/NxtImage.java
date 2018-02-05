package com.clt.lego.nxt.modules;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * @author dabo
 *
 */
public class NxtImage {

    private static final int NUMLINES = 6;

    public static final int SCREEN_WIDTH = 100;
    public static final int SCREEN_HEIGHT = NxtImage.NUMLINES * 8;

    private byte[] data;

    public NxtImage() {

        this.data = new byte[NxtImage.NUMLINES * NxtImage.SCREEN_WIDTH];
    }

    public NxtImage(Image image) {

        this();

        BufferedImage im;
        if ((image instanceof BufferedImage)
                && (image.getWidth(null) == NxtImage.SCREEN_WIDTH)
                && (image.getHeight(null) == NxtImage.SCREEN_HEIGHT)) {
            im = (BufferedImage) image;
        } else {
            im = new BufferedImage(NxtImage.SCREEN_WIDTH, NxtImage.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics g = im.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, NxtImage.SCREEN_WIDTH, NxtImage.SCREEN_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawImage(image, (NxtImage.SCREEN_WIDTH - image.getWidth(null)) / 2,
                    (NxtImage.SCREEN_HEIGHT - image.getHeight(null)) / 2, null);
            g.dispose();
        }

        // TODO: dither image
        for (int y = 0; y < NxtImage.SCREEN_HEIGHT; y++) {
            for (int x = 0; x < NxtImage.SCREEN_WIDTH; x++) {
                // Use NTSC conversion formula.
                int rgb = im.getRGB(x, y);
                int gray
                        = (int) ((0.30 * ((rgb >> 16) & 0xff) + 0.59 * ((rgb >> 8) & 0xff) + 0.11 * ((rgb) & 0xff)));
                if (gray >= 0x80) {
                    this.clearPixel(x, y);
                } else {
                    this.setPixel(x, y);
                }
            }
        }
    }

    public int getWidth() {

        return NxtImage.SCREEN_WIDTH;
    }

    public int getHeight() {

        return NxtImage.SCREEN_HEIGHT;
    }

    public int getLineCount() {

        return NxtImage.NUMLINES;
    }

    public void drawLine(int x1, int y1, int x2, int y2) {

        if (Math.abs(y2 - y1) > Math.abs(x2 - x1)) {
            if (y2 < y1) {
                this.drawLine(x2, y2, x1, y1);
            } else {
                for (int y = y1; y <= y2; y++) {
                    int x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
                    this.setPixel(x, y);
                }
            }
        } else {
            if (x2 < x1) {
                this.drawLine(x2, y2, x1, y1);
            } else {
                for (int x = x1; x <= x2; x++) {
                    int y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
                    this.setPixel(x, y);
                }
            }
        }
    }

    public void setPixel(int x, int y) {

        int line = y / 8;
        int bit = y % 8;

        this.data[line * 100 + x] |= (byte) (1 << bit);
    }

    public void clearPixel(int x, int y) {

        int line = y / 8;
        int bit = y % 8;

        this.data[line * NxtImage.SCREEN_WIDTH + x] &= (byte) ~(1 << bit);
    }

    public byte[] getData() {

        return this.data;
    }
}
