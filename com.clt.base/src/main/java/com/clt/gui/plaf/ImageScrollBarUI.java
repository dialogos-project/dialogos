package com.clt.gui.plaf;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.clt.gui.Buttons;
import com.clt.gui.Images;
import com.clt.gui.border.ImageBorder;

/**
 * @author dabo
 *
 */
public class ImageScrollBarUI extends BasicScrollBarUI {

    private ImageBorder thumb_horizontal;
    private ImageBorder thumb_vertical;
    private ImageBorder track_horizontal;
    private ImageBorder track_vertical;

    private Icon button_left;
    private Icon button_right;
    private Icon button_up;
    private Icon button_down;

    private boolean proportionalThumb;

    private boolean alwaysShowThumb = true;

    public ImageScrollBarUI() {

        this(true);
    }

    public ImageScrollBarUI(boolean proportionalThumb) {

        this.setImages(Images.loadBuiltin("scroll_track_white.png"),
                Images.loadBuiltin("scroll_thumb_white.png"), true);

        this.button_left = null;
        this.button_right = null;
        this.button_up = null;
        this.button_down = null;

        this.proportionalThumb = proportionalThumb;
        if (!proportionalThumb) {
            this.alwaysShowThumb = false;
        }

        /*
     * button_left = GUI.loadBuiltinImage("scroll_button_left_white.png");
     * button_right = GUI.loadBuiltinImage("scroll_button_right_white.png");
     * 
     * button_down =
     * GUI.rotate(GUI.loadBuiltinImage("scroll_button_left_white.png"), true);
     * button_up =
     * GUI.rotate(GUI.loadBuiltinImage("scroll_button_right_white.png"), true);
     * 
     * inset = 2;
         */

 /*
     * button_left = GUI.loadBuiltinImage("ArrowLeft.png"); button_right =
     * GUI.loadBuiltinImage("ArrowRight.png"); button_up =
     * GUI.loadBuiltinImage("ArrowUp.png"); button_down =
     * GUI.loadBuiltinImage("ArrowDown.png");
         */
    }

    public static ImageScrollBarUI createUI(JComponent c) {

        return new ImageScrollBarUI();
    }

    public void setImages(Icon track, Icon thumb, boolean horizontal) {

        this.setImages(Images.getImage(track), Images.getImage(thumb), horizontal);
    }

    public void setImages(Image track, Image thumb, boolean horizontal) {

        if (horizontal) {
            this.track_horizontal = new ImageBorder(track);
            this.thumb_horizontal = new ImageBorder(thumb);

            this.track_vertical = new ImageBorder(Images.rotate(track, true));
            this.thumb_vertical = new ImageBorder(Images.rotate(thumb, true));
        } else {
            this.track_vertical = new ImageBorder(track);
            this.thumb_vertical = new ImageBorder(thumb);

            this.track_horizontal = new ImageBorder(Images.rotate(track, false));
            this.thumb_horizontal = new ImageBorder(Images.rotate(thumb, false));
        }
    }

    public void setImages(Image hTrack, Image hThumb, Image vTrack, Image vThumb) {

        this.track_horizontal = new ImageBorder(hTrack);
        this.thumb_horizontal = new ImageBorder(hThumb);
        this.track_vertical = new ImageBorder(vTrack);
        this.thumb_vertical = new ImageBorder(vThumb);
    }

    public void setImages(Icon hTrack, Icon hThumb, Icon vTrack, Icon vThumb) {

        this.track_horizontal = new ImageBorder(hTrack);
        this.thumb_horizontal = new ImageBorder(hThumb);
        this.track_vertical = new ImageBorder(vTrack);
        this.thumb_vertical = new ImageBorder(vThumb);
    }

    @Override
    public void installUI(JComponent c) {

        super.installUI(c);

        this.scrollbar.setOpaque(false);
        this.scrollbar.setDoubleBuffered(true);

        this.trackHighlightColor = new Color(0, 0, 0, 20);
    }

    @Override
    public void uninstallUI(JComponent c) {

        super.uninstallUI(c);
    }

    protected ImageBorder getThumb(boolean horizontal) {

        if (horizontal) {
            return this.thumb_horizontal;
        } else {
            return this.thumb_vertical;
        }
    }

    protected ImageBorder getTrack(boolean horizontal) {

        if (horizontal) {
            return this.track_horizontal;
        } else {
            return this.track_vertical;
        }
    }

    @Override
    protected Rectangle getThumbBounds() {

        Rectangle bounds = super.getThumbBounds();
        Rectangle track = super.getTrackBounds();
        if (this.proportionalThumb) {
            return bounds;
        } else {
            if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
                Dimension preferredSize = this.getThumb(true).getPreferredSize();
                double percentage = (double) (bounds.x - track.x)
                        / (double) (track.width - bounds.width);

                int x = (int) Math.round(bounds.x + percentage
                        * (bounds.width - preferredSize.width));
                return new Rectangle(x, bounds.y, preferredSize.width, bounds.height);
            } else {
                Dimension preferredSize = this.getThumb(false).getPreferredSize();
                double percentage = (double) (bounds.y - track.y)
                        / (double) (track.height - bounds.height);

                int y = (int) Math.round(bounds.y + percentage
                        * (bounds.height - preferredSize.height));
                return new Rectangle(bounds.x, y, bounds.width, preferredSize.height);
            }
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {

        if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
            this.getTrack(true).paintBorder(c, g, trackBounds.x, trackBounds.y,
                    trackBounds.width,
                    trackBounds.height);
        } else {
            this.getTrack(false).paintBorder(c, g, trackBounds.x, trackBounds.y,
                    trackBounds.width,
                    trackBounds.height);
        }

        if (this.trackHighlight == BasicScrollBarUI.DECREASE_HIGHLIGHT) {
            this.paintDecreaseHighlight(g);
        } else if (this.trackHighlight == BasicScrollBarUI.INCREASE_HIGHLIGHT) {
            this.paintIncreaseHighlight(g);
        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {

        if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
            this.getThumb(true).paintBorder(c, g, thumbBounds.x, thumbBounds.y,
                    thumbBounds.width,
                    thumbBounds.height);
        } else {
            this.getThumb(false).paintBorder(c, g, thumbBounds.x, thumbBounds.y,
                    thumbBounds.width,
                    thumbBounds.height);
        }
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {

        JButton b = null;
        switch (orientation) {
            case SwingConstants.SOUTH:
                if (this.button_down != null) {
                    b = Buttons.createImageButton(this.button_down);
                }
                break;
            case SwingConstants.NORTH:
                if (this.button_up != null) {
                    b = Buttons.createImageButton(this.button_up);
                }
                break;
            case SwingConstants.WEST:
                if (this.button_left != null) {
                    b = Buttons.createImageButton(this.button_left);
                }
                break;
            case SwingConstants.EAST:
                if (this.button_right != null) {
                    b = Buttons.createImageButton(this.button_right);
                }
                break;
        }

        if (b == null) {
            b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(b.getPreferredSize());
            b.setMaximumSize(b.getPreferredSize());
        }
        return b;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {

        return this.createDecreaseButton(orientation);
    }

    @Override
    protected Dimension getMinimumThumbSize() {

        if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
            return this.getThumb(true).getPreferredSize();
        } else {
            return this.getThumb(false).getPreferredSize();
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {

        if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
            return new Dimension(48, this.getTrack(true).getPreferredSize().height);
        } else {
            return new Dimension(this.getTrack(false).getPreferredSize().width, 48);
        }
    }

    @Override
    protected void setThumbBounds(int x, int y, int width, int height) {

        if ((x == 0) && (y == 0) && (width == 0) && (height == 0)
                && this.alwaysShowThumb) {
            if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
                x = this.trackRect.x;
                y = this.trackRect.y;
                width = this.trackRect.width;
                height = this.trackRect.height;
            } else {
                x = this.trackRect.x;
                y = this.trackRect.y;
                width = this.trackRect.width;
                height = this.trackRect.height;
            }
        }

        super.setThumbBounds(x, y, width, height);
    }
}
