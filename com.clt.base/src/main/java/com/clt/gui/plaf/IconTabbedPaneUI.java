package com.clt.gui.plaf;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

import com.clt.gui.Buttons;

/**
 * An implementation of the TabbedPaneUI that uses an icon bar to display tabs.
 * This UI is intended for a small number of tabs that fit into one row. If the
 * list of icons need to be wrapped or scrolled, several drawing errors will
 * occur.
 * 
 * @author Daniel Bobbert
 */
public class IconTabbedPaneUI
    extends BasicTabbedPaneUI {

  protected Color fillColor = Color.WHITE;
  protected Color separatorColor = Color.BLACK;
  protected Color separatorLightColor = Color.WHITE;
  protected Color selectedTabBackground = new Color(192, 212, 255);
  protected int iconAlignment;


  public IconTabbedPaneUI() {

    this(SwingConstants.TOP);
  }


  public IconTabbedPaneUI(int iconAlignment) {

    this.iconAlignment = iconAlignment;
  }


  // ---------------------------------------------------------------------------------------------------
  // Custom installation methods
  // ---------------------------------------------------------------------------------------------------

  public static ComponentUI createUI(JComponent c) {

    return new IconTabbedPaneUI();
  }


  @Override
  protected void installDefaults() {

    super.installDefaults();

    this.tabInsets = new Insets(4, 6, 4, 6);
    this.tabAreaInsets = new Insets(0, 0, 0, 0);
    this.selectedTabPadInsets = new Insets(0, 0, 0, 0);
    this.contentBorderInsets = new Insets(1, 0, 0, 0);
  }


  /** Set the background color of unselected tabs */
  public void setBackground(Color color) {

    this.fillColor = color;
  }


  /** Set the background color of selected tabs */
  public void setSelectionbackground(Color color) {

    this.selectedTabBackground = color;
  }


  @Override
  protected JButton createScrollButton(int direction) {

    Icon icon;
    switch (direction) {
      case SOUTH:
        icon =
          new ImageIcon(
                    ClassLoader
                      .getSystemResource("com/clt/resources/ArrowDown.png"));
        break;
      case NORTH:
        icon =
          new ImageIcon(ClassLoader
            .getSystemResource("com/clt/resources/ArrowUp.png"));
        break;
      case EAST:
        icon =
          new ImageIcon(
                    ClassLoader
                      .getSystemResource("com/clt/resources/ArrowLeft.png"));
        break;
      case WEST:
        icon =
          new ImageIcon(
                    ClassLoader
                      .getSystemResource("com/clt/resources/ArrowRight.png"));
        break;
      default:
        throw new IllegalArgumentException("Direction must be one of: "
                        + "SOUTH, NORTH, EAST or WEST");
    }

    // return super.createScrollButton(direction);
    return Buttons.createImageButton(icon);
  } // ---------------------------------------------------------------------------------------------------


  @Override
  protected LayoutManager createLayoutManager() {

    // return new IconPaneLayout();
    return super.createLayoutManager();
  }


  // Custom sizing methods
  // ---------------------------------------------------------------------------------------------------

  @Override
  protected int calculateTabHeight(int tabPlacement, int tabIndex,
      int fontHeight) {

    Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
    int height = tabInsets.top + tabInsets.bottom;

    int iconHeight = 0;

    Icon icon = this.getIconForTab(tabIndex);
    if (icon != null) {
      iconHeight = icon.getIconHeight();
      if (!((tabPlacement == SwingConstants.LEFT) || (tabPlacement == SwingConstants.RIGHT))) {
        iconHeight += this.textIconGap;
      }
    }

    if ((this.iconAlignment == SwingConstants.LEFT)
      || (this.iconAlignment == SwingConstants.RIGHT)) {
      return height + Math.max(iconHeight, fontHeight);
    }
    else {
      return height + iconHeight + fontHeight;
    }
  }


  @Override
  protected int calculateTabWidth(int tabPlacement, int tabIndex,
      FontMetrics metrics) {

    Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
    int width = tabInsets.left + tabInsets.right;

    int iconWidth = 0;
    int textWidth = 0;

    Icon icon = this.getIconForTab(tabIndex);
    if (icon != null) {
      iconWidth = icon.getIconWidth();
      if ((tabPlacement == SwingConstants.LEFT)
        || (tabPlacement == SwingConstants.RIGHT)) {
        iconWidth += this.textIconGap;
      }
    }

    View v = this.getTextViewForTab(tabIndex);
    if (v != null) {
      // html
      textWidth = (int)v.getPreferredSpan(View.X_AXIS);
    }
    else {
      // plain text
      String title = this.tabPane.getTitleAt(tabIndex);
      if ((title != null) && (metrics != null)) {
        textWidth = metrics.stringWidth(title);
        if (textWidth % 2 != iconWidth % 2) {
          textWidth++;
        }
      }
    }

    if ((this.iconAlignment == SwingConstants.LEFT)
      || (this.iconAlignment == SwingConstants.RIGHT)) {
      return width + iconWidth + textWidth;
    }
    else {
      return width + Math.max(iconWidth, textWidth);
    }
  }


  @Override
  protected int getTabLabelShiftX(int tabPlacement, int tabIndex,
      boolean isSelected) {

    return 0;
  }


  @Override
  protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
      boolean isSelected) {

    return 0;
  }


  @Override
  protected void layoutLabel(int tabPlacement, FontMetrics metrics,
      int tabIndex, String title,
            Icon icon, Rectangle tabRect, Rectangle iconRect,
      Rectangle textRect, boolean isSelected) {

    Insets insets = this.getTabInsets(tabPlacement, tabIndex);
    textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

    View v = this.getTextViewForTab(tabIndex);
    if (v != null) {
      this.tabPane.putClientProperty("html", v);
    }

    int hTextAlignment, vTextAlignment;
    int hAlignment;
    if ((this.iconAlignment == SwingConstants.TOP)
      || (this.iconAlignment == SwingConstants.BOTTOM)) {
      hTextAlignment = SwingConstants.CENTER;
      vTextAlignment =
        this.iconAlignment == SwingConstants.TOP ? SwingConstants.BOTTOM
          : SwingConstants.TOP;
      hAlignment = SwingConstants.CENTER;
    }
    else if ((this.iconAlignment == SwingConstants.LEFT)
      || (this.iconAlignment == SwingConstants.RIGHT)) {
      hTextAlignment =
        this.iconAlignment == SwingConstants.LEFT ? SwingConstants.RIGHT
          : SwingConstants.LEFT;
      vTextAlignment = SwingConstants.CENTER;
      hAlignment = this.iconAlignment;
    }
    else {
      hTextAlignment = SwingConstants.CENTER;
      vTextAlignment = SwingConstants.BOTTOM;
      hAlignment = SwingConstants.CENTER;
    }

    // honour tab insets!
    Rectangle r = new Rectangle(tabRect);
    r.x += insets.left;
    r.width -= (insets.left + insets.right);
    r.y += insets.top;
    r.height -= (insets.top + insets.bottom);

    SwingUtilities.layoutCompoundLabel(this.tabPane, metrics, title, icon,
      SwingConstants.CENTER,
            hAlignment, vTextAlignment, hTextAlignment, r, iconRect, textRect,
      this.textIconGap);

    this.tabPane.putClientProperty("html", null);

    int xNudge = this.getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int yNudge = this.getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    iconRect.x += xNudge;
    iconRect.y += yNudge;
    textRect.x += xNudge;
    textRect.y += yNudge;
  }


  @Override
  protected Insets getContentBorderInsets(int tabPlacement) {

    Insets targetInsets = new Insets(0, 0, 0, 0);
    BasicTabbedPaneUI.rotateInsets(this.contentBorderInsets, targetInsets,
      tabPlacement);
    return targetInsets;
  }


  @Override
  protected Insets getTabInsets(int tabPlacement, int tabIndex) {

    Insets targetInsets = new Insets(0, 0, 0, 0);
    BasicTabbedPaneUI.rotateInsets(this.tabInsets, targetInsets, tabPlacement);
    return targetInsets;
  }


  // ---------------------------------------------------------------------------------------------------
  // Custom painting methods
  // ---------------------------------------------------------------------------------------------------

  // Fill the background of all tabs with the background color
  @Override
  protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {

    Rectangle bounds = this.tabPane.getBounds();
    Insets insets = this.getTabAreaInsets(tabPlacement);

    int rows = this.getTabRunCount(this.tabPane);

    g.setColor(this.fillColor);
    if (tabPlacement == SwingConstants.TOP) {
      g.fillRect(0, 0, bounds.width, this.maxTabHeight * rows + insets.top);
    }
    else if (tabPlacement == SwingConstants.BOTTOM) {
      g.fillRect(0, bounds.height - (this.maxTabHeight * rows + insets.bottom),
        bounds.width,
              this.maxTabHeight * rows + insets.bottom);
    }
    else if (tabPlacement == SwingConstants.LEFT) {
      g.fillRect(0, 0, this.maxTabWidth * rows + insets.left, bounds.height);
    }
    else if (tabPlacement == SwingConstants.RIGHT) {
      g.fillRect(bounds.width - (this.maxTabWidth * rows + insets.right), 0,
        this.maxTabWidth * rows
                  + insets.right, bounds.height);
    }

    super.paintTabArea(g, tabPlacement, selectedIndex);
  }


  @Override
  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
      int x, int y,
            int w, int h, boolean isSelected) {

    // draw background only for selected tabs
    if (isSelected) {
      g.setColor(this.selectedTabBackground);
      g.fillRect(x, y, w, h);
    }
  }


  @Override
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
      int x, int y, int w,
            int h, boolean isSelected) {

    // don't draw a tab border
  }


  @Override
  protected void paintFocusIndicator(Graphics g, int tabPlacement,
      Rectangle[] rects,
            int tabIndex, Rectangle iconRect, Rectangle textRect,
      boolean isSelected) {

    // don't draw the focus
  }


  @Override
  protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
      int selectedIndex,
            int x, int y, int w, int h) {

    if (tabPlacement == SwingConstants.BOTTOM) {
      g.setColor(this.separatorColor);
      g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
      g.setColor(this.separatorLightColor);
      g.drawLine(x, y + h - 2, x + w - 1, y + h - 2);
    }
  }


  @Override
  protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
      int selectedIndex,
            int x, int y, int w, int h) {

    if (tabPlacement == SwingConstants.TOP) {
      g.setColor(this.separatorColor);
      g.drawLine(x, y, x + w - 1, y);
      g.setColor(this.separatorLightColor);
      g.drawLine(x, y + 1, x + w - 1, y + 1);
    }
  }


  @Override
  protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
      int selectedIndex,
            int x, int y, int w, int h) {

    if (tabPlacement == SwingConstants.LEFT) {
      g.setColor(this.separatorColor);
      g.drawLine(x, y, x, y + h - 1);
      g.setColor(this.separatorLightColor);
      g.drawLine(x + 1, y, x + 1, y + h - 1);
    }
  }


  @Override
  protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
      int selectedIndex,
            int x, int y, int w, int h) {

    if (tabPlacement == SwingConstants.RIGHT) {
      g.setColor(this.separatorColor);
      g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
      g.setColor(this.separatorLightColor);
      g.drawLine(x + w - 2, y, x + w - 2, y + h - 1);
    }
  }

  protected class IconPaneLayout
        extends TabbedPaneLayout {

    // only one row
    @Override
    protected int preferredTabAreaHeight(int tabPlacement, int width) {

      return IconTabbedPaneUI.this.calculateMaxTabHeight(tabPlacement);
    }


    // only one row
    @Override
    protected int preferredTabAreaWidth(int tabPlacement, int height) {

      return IconTabbedPaneUI.this.calculateMaxTabWidth(tabPlacement);
    }

  }
}
