package com.clt.diamant;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.diamant.graph.Node;
import com.clt.gui.ColorIcon;
import com.clt.gui.OptionPane;
import com.clt.properties.BooleanProperty;
import com.clt.properties.ColorProperty;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.DefaultColorProperty;
import com.clt.properties.DefaultFileProperty;
import com.clt.properties.DefaultIntegerProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.FileProperty;
import com.clt.properties.IntegerProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class Preferences {

    private static final int MAX_MRU_LENGTH = 4;

    public final BooleanProperty showSubgraphPath;
    public final BooleanProperty showEdgeLabels;
    public final BooleanProperty showSelectionNeighbours;
    public final BooleanProperty supportMultiView;
    public final BooleanProperty showToolbox;
    public final BooleanProperty showProcedureTree;
    public final BooleanProperty showNodePanel;
    public final BooleanProperty showEdgeHandles;
    public final BooleanProperty useTransparency;
    public final BooleanProperty groupNodeToolbox;
    public final BooleanProperty loggingEnabled;
    public final BooleanProperty snapToGrid;
    public final BooleanProperty showGrid;
    public final IntegerProperty gridSize;

    public final ColorProperty selectionColor;
    public final ColorProperty neighbourColor;
    public final ColorProperty gridColor;

    public final FileProperty lastUsedFile;
    public final FileProperty loggingDirectory;
    public final List<File> additional_mru;

    public final EnumProperty<Locale> locale;

    private final Map<String, Color> defaultNodeColors;

    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    private static final File defaultDirectory;

    static {
        defaultDirectory = new File(System.getProperty("user.home") + File.separatorChar + ".dialogos" + File.separatorChar);

        if (!defaultDirectory.exists()) {
            defaultDirectory.mkdirs();
        }
    }

    /**
     * Returns the directory in which DialogOS stores its preferences, models,
     * etc.
     *
     * @return
     */
    public static File getBaseDirectory() {
        return defaultDirectory;
    }

    private Preferences() {

        this.showSubgraphPath = this.createBooleanProperty("showSubgraphPath", true);
        this.showEdgeLabels = this.createBooleanProperty("showEdgeLabels", false);
        this.showSelectionNeighbours = this.createBooleanProperty("showSelectionNeighbours", false);
        this.supportMultiView = this.createBooleanProperty("supportMultiView", true);
        this.supportMultiView.setEditable(false);

        this.showToolbox = this.createBooleanProperty("showToolbox", true);
        this.showProcedureTree = this.createBooleanProperty("showProcedureTree", true);
        this.showNodePanel = this.createBooleanProperty("showNodePanel", true);
        this.showEdgeHandles = this.createBooleanProperty("showEdgeHandles", false);
        this.useTransparency = this.createBooleanProperty("useTransparency", true);
        this.groupNodeToolbox = this.createBooleanProperty("groupNodeToolbox", false);

        this.showGrid = this.createBooleanProperty("ShowGrid", false);
        this.snapToGrid = this.createBooleanProperty("snapToGrid", false);
        this.loggingEnabled = this.createBooleanProperty("loggingEnabled", false);

        this.gridSize = new DefaultIntegerProperty("gridSize", "gridSize", null, 30) {

            @Override
            public String getName() {

                return Resources.getString(this.getID());
            }
        };

        this.selectionColor = this.createColorProperty("selectionColor", new Color(255, 40, 40));
        this.neighbourColor = this.createColorProperty("neighbourColor", new Color(255, 192, 40));
        this.gridColor = this.createColorProperty("gridColor", new Color(224, 224, 255));

        this.locale = new EnumProperty<Locale>("locale") {

            private Locale[] locales = Locale.getAvailableLocales();

            @Override
            protected void setValueImpl(Locale locale) {

                Locale.setDefault(locale);
            }

            @Override
            public Locale getValue() {

                return Locale.getDefault();
            }

            @Override
            public String getName() {

                return Resources.getString("locale");
            }

            @Override
            public String getDescription() {

                return null;
            }

            @Override
            public Locale[] getPossibleValues() {

                return this.locales;
            }
        };

        File logDirectory = new File(defaultDirectory.getPath() + "/logs");
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
        this.loggingDirectory = new DefaultFileProperty("loggingDirectory", null, null, logDirectory);

        this.lastUsedFile = new DefaultFileProperty("lastUsedFile1", null, null, defaultDirectory);
        this.additional_mru = new ArrayList<File>();

        ChangeListener listener = new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {

                Preferences.this.firePropertyChange(evt);
            }
        };
        for (Property<?> p : this.getProperties()) {
            p.addChangeListener(listener);
        }

        this.defaultNodeColors = new HashMap<String, Color>();
    }

    private BooleanProperty createBooleanProperty(String name, boolean value) {

        BooleanProperty property = new DefaultBooleanProperty(name, name, null, value) {

            @Override
            public String getName() {

                return Resources.getString(this.getID());
            }
        };

        return property;
    }

    private ColorProperty createColorProperty(String name, Color value) {

        ColorProperty property = new DefaultColorProperty(name, name, null, value) {

            @Override
            public String getName() {

                return Resources.getString(this.getID());
            }
        };

        return property;
    }

    public int getConnectionTimeout() {

        return 10000;
    }

    public boolean getShowToolboxIcons() {

        return true;
    }

    public boolean getShowToolboxText() {

        return true;
    }

    public File getLastUsedFile() {

        return this.lastUsedFile.getValue();
    }

    public File getLoggingDirectory() {

        return this.loggingDirectory.getValue();
    }

    public void setLastUsedFile(File f) {

        if ((f != null) && !f.equals(this.lastUsedFile.getValue())) {
            // remove the file, if it was in the list already
            this.additional_mru.remove(f);
            // prepend it
            if (this.lastUsedFile.getValue() != null) {
                this.additional_mru.add(0, this.lastUsedFile.getValue());
            }

            // make sure, we don't exceed maximum size
            while (this.additional_mru.size() > Preferences.MAX_MRU_LENGTH - 1) {
                this.additional_mru.remove(this.additional_mru.size() - 1);
            }

            this.lastUsedFile.setValue(f);
            this.firePropertyChange(new ChangeEvent(this.lastUsedFile));
        }
    }

    public void setLoggingDirectory(File f) {
        if ((f != null) && !f.equals(this.loggingDirectory.getValue())) {
            this.loggingDirectory.setValue(f);
        }
    }

    public void setLoggingEnabled(boolean isEnabled) {
        if (isEnabled != loggingEnabled.getValue()) {
            this.loggingEnabled.setValue(isEnabled);
            this.firePropertyChange(new ChangeEvent(this.loggingEnabled));
        }
    }

    private PropertySet<Property<?>> getProperties() {

        PropertySet<Property<?>> ps = new PropertySet<Property<?>>();
        ps.add(this.showSubgraphPath);
        ps.add(this.showEdgeLabels);
        ps.add(this.showSelectionNeighbours);
        ps.add(this.supportMultiView);
        ps.add(this.showToolbox);
        ps.add(this.showProcedureTree);
        ps.add(this.showNodePanel);
        ps.add(this.showEdgeHandles);
        ps.add(this.useTransparency);

        ps.add(this.snapToGrid);
        ps.add(this.showGrid);
        ps.add(this.gridSize);

        ps.add(this.gridColor);
        ps.add(this.selectionColor);
        ps.add(this.neighbourColor);

        ps.add(this.locale);

        ps.add(this.lastUsedFile);

        ps.add(this.loggingEnabled);
        ps.add(this.loggingDirectory);

        return ps;
    }

    // *************************************************************
    private static Preferences prefs = null;

    private static File getPrefsFile() {
        File userPrefs = new File(defaultDirectory, "Preferences.prf");
        return userPrefs;
    }

    public static Preferences getPrefs() {

        if (Preferences.prefs == null) {
            Preferences.prefs = new Preferences();
            try {
                PropertySet<Property<?>> ps = Preferences.prefs.getProperties();

                FileProperty mru[] = new FileProperty[Preferences.MAX_MRU_LENGTH - 1];
                for (int i = 0; i < mru.length; i++) {
                    mru[i] = new DefaultFileProperty("lastUsedFile" + (i + 2), null, null, null);
                    ps.add(mru[i]);
                }

                Collection<ColorProperty> defaultColors = new ArrayList<ColorProperty>();
                for (List<Class<Node>> nodeTypes : Node.getAvailableNodeTypes().values()) {
                    for (Class<Node> nodeType : nodeTypes) {
                        ColorProperty c = new DefaultColorProperty("nodeColor_" + nodeType.getName(),
                                nodeType.getName(), null, null);
                        defaultColors.add(c);
                        ps.add(c);
                    }
                }

                FileInputStream in = new FileInputStream(Preferences.getPrefsFile());
                ps.read(in);
                in.close();

                Preferences.prefs.additional_mru.clear();
                for (int i = 0; i < mru.length; i++) {
                    if (mru[i].getValue() != null) {
                        if (mru[i].getValue().isFile()) {
                            Preferences.prefs.additional_mru.add(mru[i].getValue());
                        }
                    }
                }

                for (ColorProperty p : defaultColors) {
                    if (p.getValue() != null) {
                        Preferences.prefs.defaultNodeColors.put(p.getName(), p.getValue());
                    }
                }

            } catch (Exception ignore) {
            }
        }
        return Preferences.prefs;
    }

    public static void edit(Component parent) {

        final Preferences prefs = Preferences.getPrefs();

        try {
            final JTabbedPane tp = new JTabbedPane();

            JPanel p = new PropertySet<Property<?>>(
                    new Property[]{prefs.showGrid, prefs.snapToGrid, prefs.gridSize, prefs.gridColor})
                    .createPropertyPanel(false);
            tp.addTab(Resources.getString("Grid"), p);

            p = new PropertySet<Property<?>>(new Property[]{prefs.showToolbox, prefs.showProcedureTree,
                prefs.showNodePanel, prefs.groupNodeToolbox}).createPropertyPanel(false);
            tp.addTab(Resources.getString("Toolbars"), p);

            p = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.weightx = gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(3, 3, 3, 3);

            final int swatchWidth = 30;
            final int swatchHeight = 16;
            for (List<Class<Node>> nodeTypes : Node.getAvailableNodeTypes().values()) {
                for (final Class<Node> nodeType : nodeTypes) {
                    final JLabel swatch = new JLabel(
                            new ColorIcon(prefs.getDefaultNodeColor(nodeType), swatchWidth, swatchHeight));
                    swatch.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseClicked(MouseEvent e) {

                            if (e.getClickCount() == 1) {
                                Color c = JColorChooser.showDialog(tp, Resources.getString("ChooseColor"),
                                        prefs.getDefaultNodeColor(nodeType));
                                if (c != null) {
                                    prefs.defaultNodeColors.put(nodeType.getName(), c);
                                    swatch.setIcon(new ColorIcon(c, swatchWidth, swatchHeight));
                                }
                            }
                        }
                    });
                    p.add(swatch, gbc);
                    gbc.gridx++;
                    p.add(new JLabel(Node.getLocalizedNodeTypeName(nodeType)), gbc);
                    gbc.gridx = 0;
                    gbc.gridy++;
                }
            }

            tp.addTab(Resources.getString("NodeColors"), new JScrollPane(p) {

                @Override
                public Dimension getPreferredSize() {

                    return new Dimension(super.getPreferredSize().width, 200);
                }
            });

            OptionPane.message(parent, tp, Resources.getString("Preferences"), OptionPane.PLAIN);
        } catch (Exception exn) {
            OptionPane.error(parent, exn);
        }
    }

    public static void save(Component parent) {

        Preferences prefs = Preferences.getPrefs();

        try {
            PropertySet<Property<?>> ps = prefs.getProperties();
            for (int i = 0; i < prefs.additional_mru.size(); i++) {
                ps.add(new DefaultFileProperty("lastUsedFile" + (i + 2), null, null, prefs.additional_mru.get(i)));
            }

            for (String nodeType : prefs.defaultNodeColors.keySet()) {
                ps.add(new DefaultColorProperty("nodeColor_" + nodeType, nodeType, null,
                        prefs.defaultNodeColors.get(nodeType)));
            }

            FileOutputStream out = new FileOutputStream(Preferences.getPrefsFile());
            ps.write(out);
            out.close();
        } catch (Exception exn) {
            // OptionPane.error(parent, exn);
        }
    }

    private void firePropertyChange(ChangeEvent evt) {

        for (ChangeListener l : this.changeListeners) {
            l.stateChanged(evt);
        }
    }

    public void addPropertyChangeListener(ChangeListener listener) {

        this.changeListeners.add(listener);
    }

    public void removePropertyChangeListener(ChangeListener listener) {

        this.changeListeners.remove(listener);
    }

    public Color getDefaultNodeColor(Class<? extends Node> nodeType) {

        Color color = this.defaultNodeColors.get(nodeType.getName());
        if (color == null) {
            try {
                color = (Color) nodeType.getMethod("getDefaultColor", new Class<?>[0]).invoke(null, new Object[0]);
            } catch (Exception exn) {
                // ignore
            }
        }

        if (color != null) {
            return color;
        } else {
            return Color.lightGray;
        }
    }

    public static List<String> getModelUrls() {
        File modelUrlsFile = new File(getBaseDirectory(), "model_urls.json");

        try {
            // create file with default values if necessary
            if (!modelUrlsFile.exists()) {
                JSONArray list = new JSONArray();
                list.add("http://www.coli.uni-saarland.de/~koller/dialogos/models/pocketsphinx.json");

                FileWriter w = new FileWriter(modelUrlsFile);
                w.write(list.toJSONString());
                w.write("\n");
                w.flush();
                w.close();
            }

            // read URLs from config file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(modelUrlsFile));
            List<String> ret = new ArrayList<>();
            Iterator<String> iterator = ((JSONArray) obj).iterator();
            while (iterator.hasNext()) {
                ret.add(iterator.next());
            }

            return ret;

        } catch (Exception ex) {
            System.err.println("A fatal error occurred while attempting to read the configuration files:");
            System.err.println(ex);
            System.exit(1);
            return null;
        }
    }
}
