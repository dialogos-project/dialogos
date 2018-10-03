package com.clt.gui.plaf;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.clt.gui.GUI;

/**
 * An implementation of the TabbedPaneUI that looks like the tabs that are used
 * in Photoshop palette windows.
 * <p>
 * Copyright (C) 2005 by Jon Lipsky
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.<p>
 *
 * Changes by Daniel Bobbert:
 * <p>
 * 2005-11-14: Change package to com.clt.gui.plaf
 * <p>
 * 2005-11-14: Install a PropertyChangeListener on the tabbed pane to catch font
 * changes
 * <p>
 * 2005-11-14: Make the tab background a bit brighter by using
 * {@link GUI#slightlyDarker} instead of {@link java.awt.Color#darker}.
 * <p>
 * 2006-11-30: Always reserve tab width space for the bold font.
 * <p>
 * 2006-11-30: Add underscan to tab border to remove single black pixels.
 * <p>
 * 2006-11-30: Fix several bugs that occurred with empty tabpanes
 */
public class AdobeTabbedPaneUI extends BasicTabbedPaneUI {

    private static final Insets NO_INSETS = new Insets(2, 0, 0, 0);

    /**
     * The font to use for the selected tab
     */
    private Font boldFont;

    /**
     * The font metrics for the selected font
     */
    private FontMetrics boldFontMetrics;

    /**
     * The color to use to fill in the background
     */
    private Color fillColor;

    // ---------------------------------------------------------------------------------------------------
    // Custom installation methods
    // ---------------------------------------------------------------------------------------------------
    public static ComponentUI createUI(JComponent c) {

        return new AdobeTabbedPaneUI();
    }

    @Override
    protected void installDefaults() {

        super.installDefaults();
        this.tabAreaInsets.left = 4;
        this.selectedTabPadInsets = new Insets(0, 0, 0, 0);
        this.tabInsets = this.selectedTabPadInsets;

        this.updateDefaults();
    }

    private void updateDefaults() {

        Color background = this.tabPane.getBackground();
        this.fillColor = GUI.slightlyDarker(background);

        this.boldFont = this.tabPane.getFont().deriveFont(Font.BOLD);
        this.boldFontMetrics = this.tabPane.getFontMetrics(this.boldFont);
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {

        return new ExtendedPropertyChangeHandler();
    }

    // ---------------------------------------------------------------------------------------------------
    // Custom sizing methods
    // ---------------------------------------------------------------------------------------------------
    @Override
    public int getTabRunCount(JTabbedPane pane) {

        return 1;
    }

    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {

        return AdobeTabbedPaneUI.NO_INSETS;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        int vHeight = fontHeight;
        if (vHeight % 2 > 0) {
            vHeight += 1;
        }
        return vHeight;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        if (this.boldFontMetrics != null) {
            metrics = this.boldFontMetrics;
        }
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + metrics.getHeight();
    }

    // ---------------------------------------------------------------------------------------------------
    // Custom painting methods
    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    // Methods that we want to suppress the behaviour of the superclass
    // ---------------------------------------------------------------------------------------------------
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
            int x, int y,
            int w, int h, boolean isSelected) {

        Polygon shape = new Polygon();

        shape.addPoint(x, y + h);
        shape.addPoint(x, y);
        shape.addPoint(x + w - (h / 2), y);

        if (isSelected || (tabIndex == (this.rects.length - 1))) {
            shape.addPoint(x + w + (h / 2), y + h);
        } else {
            shape.addPoint(x + w, y + (h / 2));
            shape.addPoint(x + w, y + h);
        }

        g.setColor(this.tabPane.getBackground());
        g.fillPolygon(shape);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w,
            int h, boolean isSelected) {

        int underscan = isSelected ? 0 : 1;

        g.setColor(Color.black);
        g.drawLine(x, y, x, y + h - underscan);
        g.drawLine(x, y, x + w - (h / 2), y);
        g.drawLine(x + w - (h / 2), y, x + w + (h / 2) - underscan, y + h
                - underscan);

        if (isSelected) {
            g.setColor(Color.white);
            g.drawLine(x + 1, y + 1, x + 1, y + h);
            g.drawLine(x + 1, y + 1, x + w - (h / 2), y + 1);

            g.setColor(this.shadow);
            g.drawLine(x + w - (h / 2), y + 1, x + w + (h / 2) - 1, y + h);
        }
    }

    @Override
    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {

        if (selectedIndex >= 0) {
            Rectangle selectedRect = this.getTabBounds(this.tabPane, selectedIndex);

            selectedRect.width = selectedRect.width + (selectedRect.height / 2) - 1;

            g.setColor(Color.black);

            g.drawLine(x, y, selectedRect.x, y);
            g.drawLine(selectedRect.x + selectedRect.width + 1, y, x + w, y);

            g.setColor(Color.white);

            g.drawLine(x, y + 1, selectedRect.x, y + 1);
            g.drawLine(selectedRect.x + 1, y + 1, selectedRect.x + 1, y);
            g.drawLine(selectedRect.x + selectedRect.width + 2, y + 1, x + w, y + 1);

            g.setColor(this.shadow);
            g.drawLine(selectedRect.x + selectedRect.width, y, selectedRect.x
                    + selectedRect.width
                    + 1, y + 1);
        }
    }

    @Override
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {

        // Do nothing
    }

    @Override
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {

        // Do nothing
    }

    @Override
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {

        // Do nothing
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
            Rectangle[] rects,
            int tabIndex, Rectangle iconRect, Rectangle textRect,
            boolean isSelected) {

        // Do nothing
    }

    // Fill the background of the tabs with a slightly darker color
    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {

        if ((this.rects != null) && (this.rects.length > 0)) {
            int tw = this.tabPane.getBounds().width;

            g.setColor(this.fillColor);
            g.fillRect(0, 0, tw, this.rects[0].height + 3);
        }

        super.paintTabArea(g, tabPlacement, selectedIndex);
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font,
            FontMetrics metrics,
            int tabIndex, String title, Rectangle textRect, boolean isSelected) {

        if (isSelected) {
            int vDifference
                    = (int) (this.boldFontMetrics.getStringBounds(title, g).getWidth())
                    - textRect.width;
            textRect.x -= (vDifference / 2);
            super.paintText(g, tabPlacement, this.boldFont, this.boldFontMetrics,
                    tabIndex, title, textRect,
                    isSelected);
        } else {
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title,
                    textRect, isSelected);
        }
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
            boolean isSelected) {

        return 0;
    }

    // ---------------------------------------------------------------------------------------------------
    // The extended PropertyChangeHandler watches for font updates
    // ---------------------------------------------------------------------------------------------------
    public class ExtendedPropertyChangeHandler
            extends PropertyChangeHandler {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {

            String property = evt.getPropertyName();
            if (property.equals("font") || property.equals("background")) {
                AdobeTabbedPaneUI.this.updateDefaults();
            }

            super.propertyChange(evt);
        }
    }
}
