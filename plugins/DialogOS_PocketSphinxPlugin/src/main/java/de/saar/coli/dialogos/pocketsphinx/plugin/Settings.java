/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.dialogos.modelcache.ModelCache;
import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import org.json.simple.parser.ParseException;

import org.xml.sax.SAXException;

/**
 *
 * @author koller
 */
public class Settings extends PluginSettings {

    private DefaultBooleanProperty dummyMode;
    private DefaultEnumProperty<LanguageName> defaultLanguage;
    private DefaultEnumProperty<Model> model;

    public Settings() {
        this.dummyMode = new DefaultBooleanProperty("dummyMode", Resources.getString("DummyMode"), null, false);

        Model[] models = Plugin.getAvailableModels();
        this.model = new DefaultEnumProperty<>("model", Resources.getString("Model"), null, models);

        if (models.length > 0) {
            this.model.setValue(models[0]);
        }

        List<LanguageName> languages = Plugin.getAvailableLanguages();

        this.defaultLanguage = new DefaultEnumProperty<LanguageName>("language",
                Resources.getString("DefaultLanguage"), null,
                languages.toArray(new LanguageName[languages.size()])) {

            @Override
            public String getName() {
                return Resources.getString("DefaultLanguage");
            }

            @Override
            public void setValueFromString(String value) {
                for (LanguageName n : this.getPossibleValues()) {
                    if (n.toString().equals(value) || n.getName().equals(value)) {
                        this.setValue(n);
                        break;
                    }
                }
            }
        };

        if (!languages.isEmpty()) {
            this.defaultLanguage.setValue(languages.iterator().next());
        }
    }

    public LanguageName getDefaultLanguage() {
        return this.defaultLanguage.getValue();
    }

    public boolean isDummyMode() {
        return this.dummyMode.getValue();
    }

    public Model getModel() {
        return model.getValue();
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
    }

    @Override
    public JComponent createEditor() {
        JPanel p = new JPanel(new BorderLayout(12, 12));

        p.add(new PropertySet<Property<?>>(this.defaultLanguage,
                this.model,
                this.dummyMode).createPropertyPanel(false),
                BorderLayout.NORTH);

        // Add button below properties
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JButton installButton = new JButton(Resources.getString("Install more models"));
        installButton.addActionListener((ActionEvent e) -> installMoreModels());

        bottom.add(installButton);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private void installMoreModels() {
        ModelDownloader md = new ModelDownloader(Plugin.getPluginId());

        Model[] installedModels = Plugin.getAvailableModels();
        Set<String> installedModelIds = new HashSet<String>();
        for (Model m : installedModels) {
            installedModelIds.add(m.getId());
        }

        List<Model> availableForInstallation = new ArrayList<>();
        for (Model m : md.getAllAvailableModels().values()) {
            if (!installedModelIds.contains(m.getId())) {
                availableForInstallation.add(m);
            }
        }

        if (availableForInstallation.isEmpty()) {
            // TODO - localize
            JOptionPane.showMessageDialog(null, "You have already installed all available models.");
        } else {
            ModelDownloadSelectionDialog dialog = new ModelDownloadSelectionDialog(availableForInstallation);
            dialog.setVisible(true);

            if (dialog.getSelectedModel() != null) {
                SwingWorker<Boolean, Void> w = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        ModelCache mc = new ModelCache(Plugin.getPluginId());
                        try {
                            md.download(mc, dialog.getSelectedModel().getId());
                            // TODO - localize
                            JOptionPane.showMessageDialog(null, "The model was installed successfully.");
                            return true;
                        } catch (IOException | ParseException ex) {
                            // TODO - localize
                            JOptionPane.showMessageDialog(null, "An error occurred while downloading the model: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                Model selectedModel = Settings.this.model.getValue();
                                Settings.this.model.setPossibleValues(Plugin.getAvailableModels());
                                Settings.this.model.setValue(selectedModel);
                                // This doesn't work -- see issue #19.
                            }
                        } catch (InterruptedException ex) {

                        } catch (ExecutionException ex) {

                        }
                    }
                };
                
                w.execute();
            }
        }
    }

    @Override
    protected PluginRuntime createRuntime(Component parent) throws Exception {
        return new Runtime();
    }

}
