package com.clt.gui.plaf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import com.clt.gui.Images;
import com.clt.gui.border.ImageBorder;

/**
 * @author dabo
 *
 */
public class ImageSliderUI extends javax.swing.plaf.basic.BasicSliderUI {

    private ImageBorder track_horizontal;
    private ImageBorder track_vertical;
    private Icon thumb, thumbDisabled, thumbPressed;

    private boolean thumbInside = false;

    public ImageSliderUI(JSlider slider) {

        this(slider, Images.loadBuiltin("slider_track_white.png"), null, Images.rotate(Images.loadBuiltin("slider_track_white.png"), true), null,
                Images.loadBuiltin("slider_thumb_white.png"), null, null, false);
    }

    public ImageSliderUI(JSlider slider, Icon track, Icon trackDisabled,
            Icon thumb,
            Icon thumbDisabled, Icon thumbPressed,
            boolean thumbInside) {

        this(slider, track, trackDisabled, track, trackDisabled, thumb,
                thumbDisabled,
                thumbPressed, thumbInside);
    }

    public ImageSliderUI(JSlider slider, Icon trackHorizontal,
            Icon trackHorizontalDisabled,
            Icon trackVertical, Icon trackVerticalDisabled,
            Icon thumb,
            Icon thumbDisabled, Icon thumbPressed,
            boolean thumbInside) {

        super(slider);

        this.thumbInside = thumbInside;

        this.track_horizontal
                = new ImageBorder(trackHorizontal, trackHorizontalDisabled, null,
                        null);
        this.track_vertical
                = new ImageBorder(trackVertical, trackVerticalDisabled, null, null);

        this.thumb = thumb;
        this.thumbDisabled = thumbDisabled;
        this.thumbPressed = thumbPressed;
    }

    @Override
    public void installUI(JComponent c) {

        super.installUI(c);

        c.setOpaque(false);
        c.setBorder(null);
        this.focusInsets = new Insets(0, 0, 0, 0);
    }

    private int getThumbWidth() {

        return this.thumb.getIconWidth();
    }

    private int getThumbHeight() {

        return this.thumb.getIconHeight();
    }

    @Override
    protected Dimension getThumbSize() {

        return new Dimension(this.getThumbWidth(), this.getThumbHeight());
    }

    @Override
    public void paintFocus(Graphics g) {

    }

    @Override
    public void paintThumb(Graphics g) {

        Icon icon = this.thumb;
        if (!this.slider.isEnabled() && (this.thumbDisabled != null)) {
            icon = this.thumbDisabled;
        } else if (this.slider.getValueIsAdjusting() && (this.thumbPressed != null)) {
            icon = this.thumbPressed;
        }

        icon.paintIcon(this.slider, g, this.thumbRect.x, this.thumbRect.y);
    }

    @Override
    public void paintTrack(Graphics g) {

        if (this.slider.getOrientation() == SwingConstants.HORIZONTAL) {
            int offset = this.thumbInside ? this.getThumbWidth() : 0;
            this.track_horizontal.paintBorder(this.slider, g, this.trackRect.x
                    - offset / 2, this.trackRect.y,
                    this.trackRect.width + offset, this.getThumbHeight());
        } else {
            int offset = this.thumbInside ? this.getThumbHeight() : 0;
            this.track_vertical.paintBorder(this.slider, g, this.trackRect.x,
                    this.trackRect.y - offset / 2,
                    this.getThumbWidth(), this.trackRect.height + offset);
        }
    }

    @Override
    public Dimension getMinimumHorizontalSize() {

        Dimension d = this.track_horizontal.getPreferredSize();
        return new Dimension(Math.max(d.width, this.getThumbWidth()), Math.max(
                d.height,
                this.getThumbHeight()));
    }

    @Override
    public Dimension getMinimumVerticalSize() {

        Dimension d = this.track_vertical.getPreferredSize();
        return new Dimension(Math.max(d.width, this.getThumbWidth()), Math.max(
                d.height,
                this.getThumbHeight()));
    }

    public Dimension getPreferredHorizontalSize(JComponent component) {

        Dimension d = this.getMinimumHorizontalSize();
        d.width = 200;
        return d;
    }

    public Dimension getPreferredVerticalSize(JComponent component) {

        Dimension d = this.getMinimumVerticalSize();
        d.height = 200;
        return d;
    }
}
