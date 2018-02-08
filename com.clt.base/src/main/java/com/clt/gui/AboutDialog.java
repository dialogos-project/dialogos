package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.clt.util.StringTools;

/**
 * This class can show a nice About... dialog. You can specify the applications
 * name, version, disclaimer and credits.
 *
 * @author dabo
 */
public class AboutDialog {

    /**
     * Name of the software.
     */
    private String name;

    /**
     * Version of this software.
     */
    private String version;

    /**
     * disclaimer.
     */
    private String disclaimer;

    /**
     * Name of the developers who contributed to develop this software.
     */
    private String credits;

    /**
     * Path leading to the icon of CLT.
     */
//    private static final String CLT_ICON = "com/clt/resources/CLT.png";

    /**
     * Creates an About Dialog containing the name and the version of the
     * application, a disclaimer, as well as the credits (e.g. who developed the
     * software).
     *
     * @param name Name of the software.
     * @param version Version of this software
     * @param disclaimer A disclaimer.
     * @param credits The name of the developers who contributed to this
     * software.
     */
    public AboutDialog(final String name, final String version, final String disclaimer, final String credits) {
        this.name = name;
        this.version = version;
        this.disclaimer = disclaimer;
        this.credits = credits;
    }

    public final void show(final Frame parent) {

        this.show(parent, false, false);
    }

    public final void show(final Frame parent, final boolean disposeOnFocusLost,
            final boolean disposeOnClick) {

        Container content = this.create(true, disposeOnClick);

        Window w;
        if (disposeOnFocusLost) {
            final JDialog d = new JDialog(parent, GUI.getString("About") + ' ' + this.name + "...",
                            false);
            d.addWindowListener(new WindowAdapter() {

                @Override
                public void windowDeactivated(final WindowEvent evt) {

                    d.dispose();
                }
            });
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(content);
            d.setResizable(false);
            w = d;
        } else {
            JFrame f = new JFrame(GUI.getString("About") + ' ' + this.name + "...");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setContentPane(content);
            f.setResizable(false);
            w = f;
        }

        w.pack();
        WindowUtils.setLocationRelativeTo(w, parent);
        w.setVisible(true);
    }

    public final Window showStartupScreen() {

        Container content = this.create(false, false);

        JWindow w = new JWindow();
        w.setContentPane(content);
        w.pack();
        WindowUtils.setLocationRelativeTo(w, null);
        w.setVisible(true);
        return w;
    }

    private static JComponent createLine(final int height) {

        JPanel line = new JPanel() {

            @Override
            public boolean isOpaque() {

                return true;
            }
        };
        line.setBackground(new Color(40, 100, 180));
        line.setPreferredSize(new Dimension(400, height));
        return line;
    }

    public static JComponent createHeader(final String name, final String version) {
        PatternPanel p = new PatternPanel(new ImageIcon(ClassLoader.getSystemResource("com/clt/resources/CLT_BackgroundPattern.jpg")));
        p.setLayout(new BorderLayout());
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.white);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        nameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        int descent = nameLabel.getFontMetrics(nameLabel.getFont()).getDescent();
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5 + descent, 0, 5, 5));

        JLabel versionLabel = new JLabel(StringTools.isEmpty(version) ? "" : version);
        versionLabel.setForeground(Color.white);
        // version_label.setFont(GUI.getSmallSystemFont());
        versionLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        // make them stand on one line by adjusting for the different descent
        versionLabel.setBorder(BorderFactory.createEmptyBorder(5 + descent, 5, 5 + descent - versionLabel.getFontMetrics(versionLabel.getFont()).getDescent() + 1, 10));

        p.add(nameLabel, BorderLayout.WEST);
        p.add(versionLabel, BorderLayout.CENTER);

        p.setMinimumSize(p.getPatternSize());
        p.setPreferredSize(new Dimension(Math.max(p.getPatternSize().width,
                p.getPreferredSize().width), p.getPatternSize().height));

        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(Color.white);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        PatternPanel logo = new PatternPanel(new ImageIcon(ClassLoader.getSystemResource("com/clt/resources/UdS_BackgroundLogo.png")));
        logo.setPreferredSize(logo.getPatternSize());
        logo.setMinimumSize(logo.getPatternSize());

        // header.add(top, gbc);
        // gbc.gridy++;
        header.add(AboutDialog.createLine(4), gbc);
        gbc.gridy++;

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        header.add(logo, gbc);
        gbc.weightx = 1.0;
        gbc.gridx++;
        header.add(p, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        header.add(AboutDialog.createLine(2), gbc);
        gbc.gridy++;

        return header;
    }

    public static JComponent createStripes(final String position) {

        return new PatternPanel(new ImageIcon(ClassLoader.getSystemResource("com/clt/resources/stripes_" + position + ".gif"))) {

            @Override
            public Dimension getPreferredSize() {

                return this.getPatternSize();
            }

            @Override
            public Dimension getMinimumSize() {

                return new Dimension(this.getPatternSize().width, 0);
            }

            @Override
            public Dimension getMaximumSize() {

                return new Dimension(this.getPatternSize().width, Integer.MAX_VALUE);
            }
        };
    }

    /**
     * Returns the background color of this Widget.
     *
     * @return Background color.
     */
    public static Color getBackground() {

        return new Color(240, 240, 240);
    }

    private Container create(final boolean aboutDialog, final boolean disposeOnClick) {
        final Container c = new JPanel();
        c.setLayout(new GridBagLayout());
        c.setBackground(AboutDialog.getBackground());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Container header = AboutDialog.createHeader(this.name, this.version);
        c.add(header, gbc);

        JComponent stripes_left = AboutDialog.createStripes("left");
        JComponent stripes_right = AboutDialog.createStripes("right");

        gbc.gridy++;

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTH;

        c.add(stripes_left, gbc);
        gbc.gridx++;

        if (aboutDialog) {
            JPanel licence = new JPanel(new GridBagLayout());
            licence.setOpaque(false);
            GridBagConstraints lgbc = new GridBagConstraints();
            lgbc.fill = GridBagConstraints.VERTICAL;
            Font f = GUI.getSmallSystemFont();

            lgbc.gridx = 0;
            lgbc.gridy = 0;
            lgbc.anchor = GridBagConstraints.EAST;

            lgbc.weighty = 1.0;
            licence.add(Box.createVerticalGlue(), lgbc);
            lgbc.weighty = 0.0;

            StaticText l3 = new StaticText(this.disclaimer);
            l3.setFont(f);
            l3.setAlignment(StaticText.LEFT);
            lgbc.gridy++;
            lgbc.anchor = GridBagConstraints.SOUTHWEST;
            licence.add(l3, lgbc);

            licence.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            c.add(licence, gbc);

            gbc.gridx++;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;

            // final VerticalTextScroller vts = new VerticalTextScroller();
            // vts.setText(credits);
            CreditsPane creditsPane = new CreditsPane();
            creditsPane.setText(this.credits);

            final VerticalScroller vts = new VerticalScroller(creditsPane);
            vts.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            vts.start();
            // vts.setBackground(Color.yellow);
            // vts.setForeground(Color.blue);
            // vts.setFlashColor(new Color(40, 100, 180));

            vts.setPreferredSize(new Dimension(200, 160));
            c.add(vts, gbc);
        } else {
            JPanel main = new JPanel(new GridBagLayout());
            main.setOpaque(false);
            main.setMinimumSize(new Dimension(400, 160));
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.insets = new Insets(10, 10, 10, 10);
            gbc2.gridx = 0;
            gbc2.gridy = 0;
            gbc2.gridwidth = 2;
            gbc2.weightx = 1.0;
            gbc2.weighty = 1.0;
            gbc2.fill = GridBagConstraints.NONE;
            gbc2.anchor = GridBagConstraints.CENTER;

            JProgressBar progress = new JProgressBar();
            // progress.setIndeterminate(true);
            progress.setPreferredSize(new Dimension(300,
                    progress.getPreferredSize().height));
            main.add(progress, gbc2);

            gbc2.gridy++;
            gbc2.gridwidth = 1;
            gbc2.weighty = 0.0;
            gbc2.fill = GridBagConstraints.BOTH;
            gbc2.anchor = GridBagConstraints.SOUTH;

            Font font = GUI.getSmallSystemFont();

            StaticText l1 = new StaticText(this.disclaimer);
            l1.setFont(font);
            main.add(l1, gbc2);
            gbc2.gridx++;

            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            c.add(main, gbc);
        }

        gbc.gridx++;
        gbc.weightx = 0.0;
        c.add(stripes_right, gbc);
        /*
     * addWindowListener(new WindowAdapter() { public void
     * windowOpened(WindowEvent evt) { vts.start(); } });
         */

        if (disposeOnClick) {
            c.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent evt) {

                    GUI.getWindowForComponent(c).dispose();
                }
            });
        }

        return c;
    }
}
