package com.clt.gui;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

import javax.swing.ImageIcon;

/**
 * @author dabo
 *
 */
public class RawIcon extends ImageIcon {

    public RawIcon(int[] iconData) {
        this(iconData, 0, 0, 0, 0);
    }

    public RawIcon(int[] iconData, int width) {
        this(iconData, width, iconData.length / width, 0, width);
    }

    public RawIcon(int[] iconData, int width, int height, int offset, int scan) {
        if (iconData == null) {
            width = 16;
            height = 16;
            offset = 0;
            scan = width;
            iconData = new int[width * height];
            for (int i = 0; i < iconData.length; i++) {
                iconData[i] = 0x00FFFFFF;
            }
        } else if (width == 0) {
            width = (int) Math.floor(Math.sqrt(iconData.length));
            height = width;
            offset = 0;
            scan = width;
        }

        ImageProducer im = new MemoryImageSource(width, height, iconData, offset, scan);
        this.setImage(Toolkit.getDefaultToolkit().createImage(im));
    }

    public int[] getIconData() {

        BufferedImage im = Images.getBufferedImage(this.getImage());
        return im.getRGB(0, 0, this.getIconWidth(), this.getIconHeight(), null, 0, this.getIconWidth());
    }
}
