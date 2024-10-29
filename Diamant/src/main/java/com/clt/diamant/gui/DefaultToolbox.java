package com.clt.diamant.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.diamant.Preferences;
import com.clt.diamant.Resources;
import com.clt.gui.Buttons;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.menus.MenuCommander;
import com.clt.properties.BooleanProperty;
import com.clt.properties.DefaultIntegerProperty;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;

/**
 * @author Daniel Bobbert
 *
 */
public class DefaultToolbox
        extends Toolbox {

    public static final Color startColor = new Color(235, 235, 235);
    public static final Color endColor = new Color(215, 215, 215);

    private static final int toolPopupTime = 500;

    private static final int[] BUTTON_STATES
            = {Buttons.NORMAL, Buttons.DISABLED, Buttons.PRESSED,
                Buttons.ROLLOVER};

    private static final String BUTTON_PREFIX = "toolbar/TB_";
    // private static final String BUTTON_PREFIX = "toolbar/TBFlat_";

    // private static final int[] BUTTON_STATES =
    // { Buttons.NORMAL, Buttons.DISABLED, Buttons.PRESSED, Buttons.ROLLOVER,
    // Buttons.SELECTED, Buttons.DISABLED_SELECTED, Buttons.ROLLOVER_SELECTED };
    // private static final String BUTTON_PREFIX = "toolbar/TBBlue_";
    public static final int ANCHOR = 0;
    public static final int HAND = 1;
    public static final int DELETE = 2;

    public static final int ADD_NODE = 100;

    private final Map<Property<?>, ButtonGroup> optionGroups
            = new HashMap<Property<?>, ButtonGroup>();

    private IntegerProperty currentTool
            = new DefaultIntegerProperty("tool", "Tool", null, DefaultToolbox.ANCHOR);

    @SuppressWarnings("unused")
    private IntegerProperty nodeTool
            = new DefaultIntegerProperty("node", "Add Node", null, 0);

    private Image toolbarIcons[][];
    private Image menuIndicator;
    private int leftButtonCapWidth;
    private int rightButtonCapWidth;
    private MenuCommander commander;

    private Collection<Tool> tools = new ArrayList<Tool>();

    public DefaultToolbox(final MenuCommander commander) {

        this.setName(Resources.getString("Tools"));
        this.setFloatable(false);
        this.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        this.commander = commander;

        this.initIcons();

        AbstractButton tools[] = this.addOptionGroup(this.currentTool, new Tool[]{
            new Tool("Selection", DefaultToolbox.ANCHOR, "toolbar/T_Arrow.png"),
            new Tool("Scroll", DefaultToolbox.HAND, "toolbar/T_Hand.png"),
            new Tool("Delete", DefaultToolbox.DELETE, "toolbar/T_Delete.png")});
        this.addSeparator();

        if (commander != null) {
            this.addActionButtons(new Tool[]{
                new Tool("Run", SingleDocumentWindow.cmdRun, "toolbar/T_Run.png"),
                new Tool("Debug", SingleDocumentWindow.cmdDebug, "toolbar/T_Debug.png"),
                new Tool("Woz", SingleDocumentWindow.cmdWoz, "toolbar/T_Woz.png")});
        }

        this.addSeparator();

        this.addToggleButton(Preferences.getPrefs().showGrid, "toolbar/T_Grid.png");

        this.currentTool.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {

                DefaultToolbox.this.notifyState();
            }
        });
        this.setTool(DefaultToolbox.ANCHOR);
    }

    @Override
    public void addSeparator() {

        this.add(Box.createHorizontalStrut(24));
    }

    @Override
    protected void paintComponent(Graphics g) {

        int width = this.getWidth();
        int height = this.getHeight();

        Graphics2D gfx = (Graphics2D) g;
        Paint oldPaint = gfx.getPaint();
        gfx.setPaint(new GradientPaint(0, 0, DefaultToolbox.startColor, 0,
                height - 1, DefaultToolbox.endColor));
        gfx.fillRect(0, 0, width, height - 1);
        gfx.setPaint(oldPaint);

        gfx.setColor(new Color(140, 140, 140));
        gfx.drawLine(0, height - 1, width - 1, height - 1);
    }

    private void initIcons() {

        this.toolbarIcons = new Image[5][];

        this.toolbarIcons[0]
                = Images.split(Images.load(DefaultToolbox.BUTTON_PREFIX + "LeftCap.png")
                        .getImage(),
                        DefaultToolbox.BUTTON_STATES.length, false);
        this.toolbarIcons[1]
                = Images.split(Images.load(DefaultToolbox.BUTTON_PREFIX + "Left.png")
                        .getImage(),
                        DefaultToolbox.BUTTON_STATES.length, false);
        this.toolbarIcons[2]
                = Images.split(Images.load(DefaultToolbox.BUTTON_PREFIX + "Middle.png")
                        .getImage(),
                        DefaultToolbox.BUTTON_STATES.length, false);
        this.toolbarIcons[3]
                = Images.split(Images.load(DefaultToolbox.BUTTON_PREFIX + "Right.png")
                        .getImage(),
                        DefaultToolbox.BUTTON_STATES.length, false);
        this.toolbarIcons[4]
                = Images.split(Images.load(DefaultToolbox.BUTTON_PREFIX + "RightCap.png")
                        .getImage(),
                        DefaultToolbox.BUTTON_STATES.length, false);

        this.menuIndicator
                = Images.load("toolbar/TB_MenuIndicatorSmall.png").getImage();

        this.leftButtonCapWidth = Math.max(this.toolbarIcons[0][0].getWidth(this),
                this.toolbarIcons[1][0].getWidth(this));
        this.rightButtonCapWidth = Math.max(this.toolbarIcons[3][0].getWidth(this),
                this.toolbarIcons[4][0].getWidth(this));

        // make sure the caps are at least 12 pixels wide
        this.leftButtonCapWidth = Math.max(this.leftButtonCapWidth, 12);
        this.rightButtonCapWidth = Math.max(this.rightButtonCapWidth, 12);
    }

    private AbstractButton[] addActionButtons(Tool tools[]) {
        return this.addButtonGroup(tools, false, (value) -> {
            {
                DefaultToolbox.this.commander.doCommand(value);
            }
        }, null);
    }

    private AbstractButton addToggleButton(final BooleanProperty p, String icon) {

        AbstractButton b = this.addButtonGroup(new Tool[]{new Tool(p.getName(), 0, icon) {

            @Override
            public String getName() {

                return p.getName();
            }
        }}, true, new OptionSelector() {

            public void optionSelected(int value) {

                p.setValue(value != 0);
            }
        }, null)[0];
        b.setSelected(p.getValue());
        return b;
    }

    private AbstractButton[] addOptionGroup(final IntegerProperty option, final Tool tools[]) {
        ButtonGroup group = this.optionGroups.get(option);
        if (group == null) {
            group = new ButtonGroup();
            this.optionGroups.put(option, group);
        }

        final AbstractButton[] buttons = this.addButtonGroup(tools, true, (value) -> {
            option.setValue(value);
        }, group);

        ChangeListener l = new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {
                for (int i = 0; i < buttons.length; i++) {
                    buttons[i].setSelected(true);
                    if (Preferences.getPrefs().getShowToolboxText()) {
                        buttons[i].setText(Resources.getString(tools[i].getName()));
                    }
                    if (Preferences.getPrefs().getShowToolboxIcons()) {
                        setIcons(buttons[i], i, buttons.length, tools[i].getIcon(), buttons[i].getIcon().getIconWidth(), buttons[i].getIcon().getIconHeight());
                    }
                }
            }
        };

        option.addChangeListener(l);
        l.stateChanged(new ChangeEvent(option));
        return buttons;
    }

    private void setIcons(AbstractButton button, int i, int maxI, ImageIcon icon, int width, int height) {

        Image tb_left[] = (i == 0) ? this.toolbarIcons[0] : this.toolbarIcons[1];
        Image tb_middle[] = this.toolbarIcons[2];
        //Image tb_right[] = (i == n - 1) ? this.toolbarIcons[4] : this.toolbarIcons[3];
        Image tb_right[] = (i == maxI - 1) ? this.toolbarIcons[4] : this.toolbarIcons[3];

        button.setSelectedIcon(null);
        Buttons.setIcons(button,
                createImages(tb_left, tb_middle, tb_right, icon, BUTTON_STATES, width, height, false),
                BUTTON_STATES);

        if (button.getSelectedIcon() == null) {
            button.setSelectedIcon(button.getPressedIcon());
        }
    }

    private AbstractButton[] addButtonGroup(Tool tools[], boolean toggle,
            final OptionSelector actionListener, ButtonGroup group) {

        final AbstractButton groupButtons[] = new AbstractButton[tools.length];

        int maxTextWidth = 0;
        int maxIconWidth = 0;
        for (int i = 0; i < groupButtons.length; i++) {
            final Tool tool = tools[i];
            this.tools.add(tool);

            if (toggle) {
                groupButtons[i] = new JToggleButton();
            } else {
                groupButtons[i] = new JButton();
            }

            final AbstractButton button = groupButtons[i];
            if (group != null) {
                group.add(button);
            }

            tool.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("enabled")) {
                        button.setEnabled(tool.isEnabled());
                    } else if (evt.getPropertyName().equals("description")) {
                        button.setText(tool.getDescription());
                    }
                }
            });

            button.setFont(GUI.getSmallSystemFont());
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setVerticalAlignment(SwingConstants.CENTER);
            button.setHorizontalTextPosition(SwingConstants.CENTER);

            if (Preferences.getPrefs().getShowToolboxIcons()) {
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
            } else {
                button.setVerticalTextPosition(SwingConstants.CENTER);
            }

            if (Preferences.getPrefs().getShowToolboxText()) {
                button.setText(Resources.getString(tool.getName()));
            }

            // showOnlyIcon() must be called AFTER setText(). Otherwise
            // there will be
            // a border on the button in Mac OS X.
            Buttons.showOnlyIcon(button);
            button.setFocusPainted(false);
            button.setBorderPainted(false);

            if (tool.getIcon() != null) {
                int width = tool.getIcon().getIconWidth();
                maxIconWidth = Math.max(maxIconWidth, width);

                maxTextWidth = Math.max(maxTextWidth, button.getPreferredSize().width + 1);
            }

        }

        int height = this.toolbarIcons[2][0].getHeight(this);

        for (int i = 0; i < groupButtons.length; i++) {
            int width;
            if (groupButtons.length == 1) {
                // for single buttons the text may be wider than the icon
                width = maxIconWidth + this.leftButtonCapWidth + this.rightButtonCapWidth;
            } else {
                // For button groups, the icons must be as wide as the text.
                // Also make sure, that the width is an odd number. Otherwise
                // there will be an empty line between buttons with some LAFs,
                // only god knows why.
                width = Math.max(maxIconWidth
                        + Math.max(this.leftButtonCapWidth, this.rightButtonCapWidth), maxTextWidth + 6);
                if (width % 2 == 0) {
                    width += 1;
                }
            }

            final Tool tool = tools[i];
            final AbstractButton b = groupButtons[i];
            this.setIcons(b, i, groupButtons.length, tool.getIcon(), width, height);

            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (b.isSelected() && (groupButtons.length == 1)) {
                        actionListener.optionSelected(-1);
                    } else {
                        actionListener.optionSelected(tool.getValue());
                    }
                }
            });

            this.add(b);
        }

        return groupButtons;
    }

    private Image[] createImages(Image left[], Image middle[], Image right[],
            ImageIcon buttonIcon,
            int[] states, int width, int height, boolean popup) {

        Icon icons[] = new Icon[left.length];
        if (buttonIcon != null) {
            for (int i = 0; i < icons.length; i++) {
                if (states[i] == Buttons.DISABLED) {
                    icons[i]
                            = new ImageIcon(GrayFilter.createDisabledImage(buttonIcon.getImage()));
                } else {
                    icons[i] = buttonIcon;
                }
            }
        }
        return this.createImages(left, middle, right, icons, width, height, popup);
    }

    private Image[] createImages(Image left[], Image middle[], Image right[],
            Icon icons[],
            int width, int height, boolean popup) {

        Image im[] = new Image[left.length];

        for (int i = 0; i < im.length; i++) {
            im[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            int w_left = left[i].getWidth(this);
            int w_right = right[i].getWidth(this);
            int w_middle = middle[i].getWidth(this);

            Graphics g = im[i].getGraphics();
            g.drawImage(left[i], 0, 0, this);
            g.drawImage(right[i], width - w_right, 0, this);

            int x = w_left;
            while (x < width - w_right - w_middle) {
                g.drawImage(middle[i], x, 0, this);
                x += w_middle;
            }

            if (x < width - w_right) {
                g.drawImage(middle[i], x, 0, width - w_right, height, 0, 0, width
                        - w_right - x,
                        height, this);
            }

            if ((icons != null) && (icons[i] != null)) {
                Icon icon = icons[i];

                int y = (height - icon.getIconHeight()) / 2;
                if (popup) {
                    if (this.menuIndicator != null) {
                        x
                                = (width - icon.getIconWidth() - this.menuIndicator.getWidth(this)) / 2;
                    } else {
                        x = (width - icon.getIconWidth()) / 2 - 2;
                    }
                } else {
                    x = (width - icon.getIconWidth()) / 2;
                }

                icon.paintIcon(this, g, x, y);

                if (popup) {
                    if (this.menuIndicator != null) {
                        g.drawImage(this.menuIndicator, x + icon.getIconWidth(), y
                                + icon.getIconHeight()
                                - this.menuIndicator.getHeight(this), this);
                    } else {
                        int size = 3;
                        int h = x + icon.getIconWidth() + size;
                        int v = y + icon.getIconHeight();

                        g.setColor(Color.black);
                        for (int k = 1; k < size; k++) {
                            g.drawLine(h - size, v - k, h - (size - k), v - k);
                        }
                        for (int k = 0; k < size; k++) {
                            g.drawLine(h - size, v - size - k, h - k, v - size - k);
                        }
                    }
                }
            }

            g.dispose();
        }

        return im;
    }

    @Override
    public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);

        for (int i = 0; i < this.getComponentCount(); i++) {
            this.getComponent(i).setEnabled(enabled);
        }
    }

    public int getTool() {

        return this.currentTool.getValue();
    }

    public void setTool(int tool) {

        this.currentTool.setValue(tool);
    }

    @Override
    public void notifyState() {

        this.firePropertyChange("CurrentTool", -1, this.currentTool.getValue());
    }

    private interface OptionSelector {

        public void optionSelected(int value);
    }

    private static class Tool {

        private String toolName;
        private String description;
        private boolean enabled;
        private int value;
        private ImageIcon icon;

        private Collection<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();

        public Tool(String name, int value, Object newIcon) {
            this.toolName = name;
            this.enabled = true;
            this.description = Resources.getString(name);

            this.value = value;

            if (newIcon != null) {
                if (newIcon instanceof String) {
                    this.icon = Images.load((String) newIcon);
                } else if (newIcon instanceof ImageIcon) {
                    this.icon = (ImageIcon) newIcon;
                } else if (icon instanceof Icon) {
                    Icon icon = (Icon) newIcon;
                    BufferedImage im = new BufferedImage(icon.getIconWidth(),
                            icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics g = im.getGraphics();
                    g.drawImage(im, 0, 0, null);
                    g.dispose();
                    this.icon = new ImageIcon(im);
                } else if (newIcon instanceof Image) {
                    this.icon = new ImageIcon((Image) newIcon);
                }
            }
        }

        public int getValue() {

            return this.value;
        }

        public String getDescription() {
            return this.description;
        }

        public void updateDescription() {
            String oldDescription = this.description;
            this.description = Resources.getString(this.toolName);
            this.firePropertyChange("description", oldDescription, this.description);
        }

        public void setValue(int value) {

            int oldVal = this.value;
            this.value = value;
            //this.firePropertyChange("enabled", new Integer(oldVal), new Integer(value));
        }

        public String getName() {

            return this.toolName;
        }

        public ImageIcon getIcon() {

            return this.icon;
        }

        public boolean isEnabled() {

            return this.enabled;
        }

        public void setEnabled(boolean enabled) {

            if (enabled != this.enabled) {
                this.enabled = enabled;
                this.firePropertyChange("enabled", Boolean.valueOf(!enabled), Boolean.valueOf(enabled));
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {

            synchronized (this.listeners) {
                this.listeners.add(listener);
            }
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {

            synchronized (this.listeners) {
                this.listeners.remove(listener);
            }
        }

        protected void firePropertyChange(String property, Object oldValue, Object newValue) {
            synchronized (this.listeners) {
                PropertyChangeEvent evt = new PropertyChangeEvent(this, property, oldValue, newValue);
                for (PropertyChangeListener l : this.listeners) {
                    l.propertyChange(evt);
                }
            }
        }
    }

    private static class CmdTool extends Tool {

        private int cmd;

        public CmdTool(int value, String name, String icon, int cmd) {

            super(name, value, icon);

            this.cmd = cmd;
        }

        public int getCommand() {

            return this.cmd;
        }
    }

    @Override
    public void update() {
        for (Tool tool : this.tools) {
            tool.updateDescription();
            if (tool instanceof CmdTool) {
                CmdTool t = (CmdTool) tool;
                t.setEnabled(this.commander.menuItemState(t.getCommand()));
            }
        }
    }
}
